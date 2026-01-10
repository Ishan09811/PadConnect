
package io.github.padconnect.utils

import kotlinx.serialization.json.Json
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class UdpTransport(
    private val host: String,
    private val port: Int
) : GamepadTransport {

    private var socket: DatagramSocket? = null

    override fun isAvailable(): Boolean = true

    override fun send(event: GamepadEvent) {
        if (socket == null) socket = DatagramSocket()
        val data = Json.encodeToString(event).toByteArray()
        val packet = DatagramPacket(
            data,
            data.size,
            InetAddress.getByName(host),
            port
        )
        socket?.send(packet)
    }
}
