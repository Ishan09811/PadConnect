package io.github.padconnect.transport

import android.os.Build
import io.github.padconnect.PadConnectApplication

class TransportManager(
    udpHost: String,
    udpPort: Int,
    onLatencyStatsReceive: ((Double) -> Unit)? = null
) {
    private var wifi: GamepadTransport? = null
    private var ble: GamepadTransport? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ble = BleTransport(PadConnectApplication.context)
        wifi = UdpTransport(udpHost, udpPort, onLatencyStatsReceive)
    }

    fun setButton(mask: Int, down: Boolean) {
        when {
            ble?.isAvailable() == true -> ble!!.setButton(mask, down)
            wifi?.isAvailable() == true -> wifi!!.setButton(mask, down)
        }
    }

    fun setLeftAxis(x: Float, y: Float) {
        when {
            ble?.isAvailable() == true -> ble!!.setLeftAxis(x, y)
            wifi?.isAvailable() == true -> wifi!!.setLeftAxis(x, y)
        }
    }

    fun setRightAxis(x: Float, y: Float) {
        when {
            ble?.isAvailable() == true -> ble!!.setRightAxis(x, y)
            wifi?.isAvailable() == true -> wifi!!.setRightAxis(x, y)
        }
    }

    fun start() {
        (wifi as? UdpTransport)?.start()
    }

    fun stop() {
        (wifi as? UdpTransport)?.stop()
    }
}