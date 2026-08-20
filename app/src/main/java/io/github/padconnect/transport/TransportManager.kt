/*
 * Copyright (C) 2026 Ishan
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed without any warranty. See the GNU General Public License for more details.
 */

package io.github.padconnect.transport

import android.os.Build
import android.util.Log
import io.github.padconnect.PadConnectApplication

class TransportManager(
    udpHost: String,
    udpPort: Int,
    onLatencyStatsReceive: ((Double) -> Unit)? = null
) {
    private var wifi: GamepadTransport? = null
    private var ble: GamepadTransport? = null

    companion object {
        private const val LOG_TAG = "TransportManager"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ble = BleTransport(PadConnectApplication.context)
        wifi = UdpTransport(udpHost, udpPort, onLatencyStatsReceive)
        Log.i(LOG_TAG, "Initialized")
    }

    fun setButton(mask: Int, down: Boolean) {
        when {
           // ble?.isAvailable() == true -> ble!!.setButton(mask, down)
            wifi?.isAvailable() == true -> wifi!!.setButton(mask, down)
        }
    }

    fun setLeftAxis(x: Float, y: Float) {
        when {
            //ble?.isAvailable() == true -> ble!!.setLeftAxis(x, y)
            wifi?.isAvailable() == true -> wifi!!.setLeftAxis(x, y)
        }
    }

    fun setRightAxis(x: Float, y: Float) {
        when {
            //ble?.isAvailable() == true -> ble!!.setRightAxis(x, y)
            wifi?.isAvailable() == true -> wifi!!.setRightAxis(x, y)
        }
    }

    fun isReceiverActive(): Boolean {
        return (wifi as UdpTransport).isReceiverActive()
    }

    fun start() {
        (wifi as? UdpTransport)?.start()
    }

    fun stop() {
        (wifi as? UdpTransport)?.stop()
    }
}