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
import io.github.padconnect.utils.Logger
import io.github.padconnect.utils.settings.GlobalConfig
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
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
    private val packet = DatagramPacket(ByteArray(20), 20, address, port)

    private val stateLock = Any()
    private var state = GamepadState()

    @Volatile
    private var isRunning = true

    private var retryDelay = 500L

    private val hapticHandler = HapticHandler()

    @Volatile
    private var lastResponseTime = 0L

    fun isWifiAvailable(): Boolean {
        val cm = PadConnectApplication.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private val senderThread = Thread {
        val buffer = ByteBuffer.allocate(21).order(ByteOrder.LITTLE_ENDIAN)
        var next = System.nanoTime()

        Logger.info("UdpTransport", "senderThread Started")
        while (isRunning) {

            val intervalNs = 1_000_000_000L / GlobalConfig.INPUT_UPDATE_RATE.int

            buffer.clear()

            buffer.put(0) // type = input
            synchronized(stateLock) {
                buffer.putShort(state.buttons.toShort())
                buffer.putShort(state.lx)
                buffer.putShort(state.ly)
                buffer.putShort(state.rx)
                buffer.putShort(state.ry)
                buffer.put(state.lt)
                buffer.put(state.rt)
            }
            buffer.putLong(System.nanoTime())

            packet.setData(buffer.array())
            packet.length = buffer.position()

            try {
                socket.send(packet)
            } catch (e: IOException) {
                while (isRunning) {
                    if (isWifiAvailable()) break

                    if (retryDelay < 2000L) {
                        Logger.error("UdpTransport", "Send failed: ${e.message}")
                    }
                    Thread.sleep(retryDelay)
                    retryDelay = (retryDelay * 2).coerceAtMost(5000)
                }

                retryDelay = 500L
                continue
            }

            next = System.nanoTime() + intervalNs
            val sleep = next - System.nanoTime()
            if (sleep > 0)
                LockSupport.parkNanos(sleep)
        }
    }

    private val ioThread = Thread {
        val buffer = ByteArray(64)
        val packet = DatagramPacket(buffer, buffer.size)

        Logger.info("UdpTransport:", "ioThread Started")

        while (isRunning && !socket.isClosed) {
            try {
                socket.receive(packet)
            } catch(e: IOException) {
                while (isRunning) {
                    if (isWifiAvailable()) break

                    if (retryDelay < 2000L) {
                        Logger.error("UdpTransport", "Receive failed: ${e.message}")
                    }
                    Thread.sleep(retryDelay)
                    retryDelay = (retryDelay * 2).coerceAtMost(5000)
                }

                retryDelay = 500L
                continue
            }

            val bb = ByteBuffer.wrap(packet.data, 0, packet.length)
                .order(ByteOrder.LITTLE_ENDIAN)

            val type = bb.get().toInt()
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
        }
    }

    fun start(): Boolean {
        try {
            isRunning = true
            senderThread.start()
            ioThread.start()
        } catch (e: Exception) {
            Logger.error("UdpTransport", "Failed to start: ${e.message}")
            return false
        }
        return true
    }

    fun stop() {
        isRunning = false
        socket.close()
        Logger.info("UdpTransport", "Stopped")
    }

    override fun setButton(mask: Int, down: Boolean) {
        synchronized(stateLock) {
            if (mask == GamepadKey.LT.id) {
                state.lt = if (down) 100 else 0
                return@setButton
            }

            if (mask == GamepadKey.RT.id) {
                state.rt = if (down) 100 else 0
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
        val timeoutMs = 2000L
        return (System.currentTimeMillis() - lastResponseTime) < timeoutMs
    }

    override fun isAvailable(): Boolean {
        // TODO: ?
        return true
    }
}