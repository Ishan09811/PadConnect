/*
 * Copyright (C) 2026 Ishan
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed without any warranty. See the GNU General Public License for more details.
 */

package io.github.padconnect.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface

const val FEATURE_RUMBLE = 1 shl 0
const val FEATURE_LATENCY = 1 shl 1

data class DiscoveryResult(
    val host: String?,
    val port: Int,
    val agreedVersion: Int,
    val features: Int
)

object DiscoverySender {
    const val MIN_SUPPORTED_VERSION = 2
    private const val CLIENT_VERSION = 2

    private const val LOG_TAG = "DiscoverySender"

    suspend fun discoverReceiver(
        clientFeatures: Int,
        timeoutMs: Int = 2000
    ): DiscoveryResult? = withContext(Dispatchers.IO) {
        val socket = DatagramSocket().apply {
            broadcast = true
            soTimeout = timeoutMs
        }

        try {
            val requestData = "PADCONNECT_DISCOVER:$CLIENT_VERSION:$clientFeatures".toByteArray()

            val broadcastAddresses = getBroadcastAddresses()

            for (address in broadcastAddresses) {
                try {
                    val requestPacket = DatagramPacket(
                        requestData,
                        requestData.size,
                        address,
                        8083
                    )
                    socket.send(requestPacket)
                } catch (e: Exception) {
                    Log.d(LOG_TAG, "Failed to send to $address", e)
                }
            }

            val buffer = ByteArray(256)
            val responsePacket = DatagramPacket(buffer, buffer.size)

            socket.receive(responsePacket)

            val message = String(
                responsePacket.data,
                0,
                responsePacket.length
            )

            if (message.startsWith("PADCONNECT_HERE")) {
                val parts = message.split(":")

                val port = parts[1].toInt()
                val serverVersion = parts[2].toInt()
                val serverFeatures = parts[3].toInt()

                if (parts.size < 4) {
                    return@withContext DiscoveryResult(
                        host = responsePacket.address.hostAddress,
                        port = port,
                        agreedVersion = 1,
                        features = 0
                    )
                }

                val agreedVersion = minOf(CLIENT_VERSION, serverVersion)
                val agreedFeatures = clientFeatures and serverFeatures

                return@withContext DiscoveryResult(
                    host = responsePacket.address.hostAddress,
                    port = port,
                    agreedVersion = agreedVersion,
                    features = agreedFeatures
                )
            }

            null
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Discovery failed", e)
            null
        } finally {
            socket.close()
        }
    }

    fun buildClientFeatures(
        enableRumble: Boolean,
        showLatency: Boolean
    ): Int {
        var features = 0

        if (enableRumble) {
            features = features or FEATURE_RUMBLE
        }

        if (showLatency) {
            features = features or FEATURE_LATENCY
        }

        return features
    }


    private fun getBroadcastAddresses(): List<InetAddress> {
        val addresses = mutableListOf<InetAddress>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                for (interfaceAddress in networkInterface.interfaceAddresses) {
                    val broadcast = interfaceAddress.broadcast
                    if (broadcast != null) {
                        addresses.add(broadcast)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error getting network interfaces", e)
        }

        if (addresses.isEmpty()) {
            addresses.add(InetAddress.getByName("255.255.255.255"))
        }

        return addresses
    }
}
