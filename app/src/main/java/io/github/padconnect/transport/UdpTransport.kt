/*
 * Copyright (C) 2026 Ishan
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed without any warranty. See the GNU General Public License for more details.
 */

package io.github.padconnect.transport

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import io.github.padconnect.PadConnectApplication
import io.github.padconnect.utils.GamepadKey
import io.github.padconnect.utils.HapticHandler
import io.github.padconnect.utils.settings.GlobalConfig
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.locks.LockSupport
import kotlin.math.roundToInt

class UdpTransport(
    host: String,
    port: Int,
    private val onLatencyStatsReceive: ((Double) -> Unit)? = null
) : GamepadTransport {
    private val socket = DatagramSocket()

    private val address = InetAddress.getByName(host)
    private val sendBuffer = ByteBuffer.allocate(PACKET_SIZE).order(ByteOrder.LITTLE_ENDIAN)
    private val packet = DatagramPacket(sendBuffer.array(), PACKET_SIZE, address, port)

    private val stateLock = Any()
    private val state = GamepadState()

    @Volatile
    private var isRunning = true

    private val hapticHandler = HapticHandler()

    @Volatile
    private var lastResponseTime = 0L

    companion object {
        private const val LOG_TAG = "UdpTransport"
        private const val PACKET_SIZE = 21 // 1(type) + 2+2+2+2+2(axes) + 1+1(triggers) + 8(timestamp)
        private const val RECV_BUFFER_SIZE = 64
        private const val TRIGGER_PRESSED: Byte = 100
        private const val TRIGGER_RELEASED: Byte = 0
        private const val INITIAL_RETRY_DELAY_MS = 500L
        private const val MAX_RETRY_DELAY_MS = 5000L
        private const val RECEIVER_TIMEOUT_MS = 2000L
        private const val LOG_THROTTLE_THRESHOLD_MS = 2000L
    }

    fun isWifiAvailable(): Boolean {
        val cm = PadConnectApplication.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun waitForReconnect(e: Exception, retryDelayHolder: LongArray) {
        while (isRunning) {
            if (isWifiAvailable()) break
            if (retryDelayHolder[0] < LOG_THROTTLE_THRESHOLD_MS) {
                Log.e(LOG_TAG, "Operation failed: ${e.message}")
            }
            Thread.sleep(retryDelayHolder[0])
            retryDelayHolder[0] = (retryDelayHolder[0] * 2).coerceAtMost(MAX_RETRY_DELAY_MS)
        }
        retryDelayHolder[0] = INITIAL_RETRY_DELAY_MS
    }

    private val senderThread = Thread {
        var next = System.nanoTime()
        val sendRetryDelay = longArrayOf(INITIAL_RETRY_DELAY_MS)

        Log.i(LOG_TAG, "senderThread Started")
        while (isRunning) {
            val intervalNs = 1_000_000_000L / GlobalConfig.INPUT_UPDATE_RATE.int
            sendBuffer.clear()
            sendBuffer.put(0) // type = input
            synchronized(stateLock) {
                sendBuffer.putShort(state.buttons.toShort())
                sendBuffer.putShort(state.lx)
                sendBuffer.putShort(state.ly)
                sendBuffer.putShort(state.rx)
                sendBuffer.putShort(state.ry)
                sendBuffer.put(state.lt)
                sendBuffer.put(state.rt)
            }
            sendBuffer.putLong(System.nanoTime())

            packet.length =sendBuffer.position()

            try {
                socket.send(packet)
            } catch (e: IOException) {
                if (!isRunning) break
                waitForReconnect(e, sendRetryDelay)
                next = System.nanoTime()
                continue
            }

            next += intervalNs
            val sleep = next - System.nanoTime()
            if (sleep > 0) {
                LockSupport.parkNanos(sleep)
            } else {
                next = System.nanoTime()
            }
        }
    }

    private val ioThread = Thread {
        val buffer = ByteArray(RECV_BUFFER_SIZE)
        val packet = DatagramPacket(buffer, buffer.size)
        val recvRetryDelay = longArrayOf(INITIAL_RETRY_DELAY_MS)

        Log.i("UdpTransport:", "ioThread Started")

        while (isRunning && !socket.isClosed) {
            try {
                socket.receive(packet)
            } catch (e: IOException) {
                if (!isRunning) break
                waitForReconnect(e, recvRetryDelay)
                continue
            }

            val bb = ByteBuffer.wrap(packet.data, 0, packet.length)
                .order(ByteOrder.LITTLE_ENDIAN)

            val type = bb.get().toInt()
            try {
                when (type) {
                    1 -> { // rumble
                        Log.w("UdpTransport", "Rumble!")
                        val large = bb.get().toInt() and 0xFF
                        val small = bb.get().toInt() and 0xFF
                        hapticHandler.onRumble(large, small)
                    }

                    2 -> { // latency
                        val sentTime = bb.long
                        val now = System.nanoTime()

                        val roundTripNs = now - sentTime
                        val oneWayNs = roundTripNs / 2

                        onLatencyStatsReceive?.invoke(oneWayNs / 1_000_000.0)
                        lastResponseTime = System.currentTimeMillis()
                    }
                }
            } catch (e: BufferUnderflowException) {
                Log.e(LOG_TAG, "Malformed packet (type=$type): ${e.message}")
            }
        }
    }

    fun start(): Boolean {
        try {
            isRunning = true
            senderThread.start()
            ioThread.start()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to start: ${e.message}")
            return false
        }
        return true
    }

    fun stop() {
        isRunning = false
        socket.close()
        senderThread.join(1000)
        ioThread.join(1000)
        Log.i(LOG_TAG, "Stopped")
    }

    override fun setButton(mask: Int, down: Boolean) {
        synchronized(stateLock) {
            if (mask == GamepadKey.LT.id) {
                state.lt = if (down) TRIGGER_PRESSED else TRIGGER_RELEASED
                return@setButton
            }

            if (mask == GamepadKey.RT.id) {
                state.rt = if (down) TRIGGER_PRESSED else TRIGGER_RELEASED
                return@setButton
            }

            if (down)
                state.buttons = state.buttons or mask
            else
                state.buttons = state.buttons and mask.inv()

        }
    }

    override fun setLeftAxis(x: Float, y: Float) {
        synchronized(stateLock) {
            state.lx = (x * Short.MAX_VALUE).roundToInt().toShort()
            state.ly = (y * Short.MAX_VALUE).roundToInt().toShort()
        }
    }

    override fun setRightAxis(x: Float, y: Float) {
        synchronized(stateLock) {
            state.rx = (x * Short.MAX_VALUE).roundToInt().toShort()
            state.ry = (y * Short.MAX_VALUE).roundToInt().toShort()
        }
    }

    fun isReceiverActive(): Boolean {
        return (System.currentTimeMillis() - lastResponseTime) < RECEIVER_TIMEOUT_MS
    }

    override fun isAvailable(): Boolean {
        // TODO: ?
        return true
    }
}