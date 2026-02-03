package io.github.padconnect.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

data class DiscoveryResult(
    val host: String,
    val port: Int
)

object DiscoverySender {
    suspend fun discoverReceiver(
        timeoutMs: Int = 2000
    ): DiscoveryResult? = withContext(Dispatchers.IO) {
        val socket = DatagramSocket().apply {
            broadcast = true
            soTimeout = timeoutMs
        }

        try {
            val requestData = "PADCONNECT_DISCOVER".toByteArray()
            val requestPacket = DatagramPacket(
                requestData,
                requestData.size,
                InetAddress.getByName("255.255.255.255"),
                8083
            )

            socket.send(requestPacket)

            val buffer = ByteArray(256)
            val responsePacket = DatagramPacket(buffer, buffer.size)

            socket.receive(responsePacket)

            val message = String(
                responsePacket.data,
                0,
                responsePacket.length
            )

            if (message.startsWith("PADCONNECT_HERE")) {
                val port = message.split(":")[1].toInt()
                return@withContext DiscoveryResult(
                    host = responsePacket.address.hostAddress,
                    port = port
                )
            }

            null
        } catch (e: Exception) {
            null
        } finally {
            socket.close()
        }
    }
}
