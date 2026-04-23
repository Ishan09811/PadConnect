/*
 * Copyright (C) 2026 Ishan
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed without any warranty. See the GNU General Public License for more details.
 */

package io.github.padconnect.transport

import io.github.padconnect.utils.GamepadKey
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

    private val senderThread = Thread {
        val buffer = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN)
        var next = System.nanoTime()

        while (isRunning && !socket.isClosed) {

            val intervalNs = 1_000_000_000L / GlobalConfig.INPUT_UPDATE_RATE.int

            buffer.clear()

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
            } catch (_: IOException) {
                break
            }

            next = System.nanoTime() + intervalNs
            val sleep = next - System.nanoTime()
            if (sleep > 0)
                LockSupport.parkNanos(sleep)
        }
    }

    private val latencyThread = Thread {
        val buffer = ByteArray(16)
        val packet = DatagramPacket(buffer, buffer.size)

        while (isRunning && !socket.isClosed) {
            try {
                socket.receive(packet)
            } catch(_: IOException) {
                break
            }

            val bb = ByteBuffer.wrap(packet.data, 0, packet.length)
                .order(ByteOrder.LITTLE_ENDIAN)
            val sentTime = bb.long
            val now = System.nanoTime()

            val roundTripNs = now - sentTime
            val oneWayNs = roundTripNs / 2

            onLatencyStatsReceive?.invoke(oneWayNs / 1_000_000.0)
        }
    }

    fun start(): Boolean {
        try {
            isRunning = true
            senderThread.start()
            latencyThread.start()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun stop() {
        isRunning = false
        socket.close()
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

    override fun isAvailable(): Boolean {
        return true
    }
}