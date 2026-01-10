
package io.github.padconnect.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import io.github.padconnect.utils.BleTransport
import io.github.padconnect.utils.TransportManager
import io.github.padconnect.utils.UdpTransport

@Composable
fun rememberTransportManager(
    udpHost: String,
    udpPort: Int
): TransportManager {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    return remember {
        val bleTransport = BleTransport(context).apply {
            start()
        }

        val udpTransport = UdpTransport(
            host = udpHost,
            port = udpPort
        )

        TransportManager(
            wifi = udpTransport,
            ble = bleTransport,
            scope = scope
        )
    }
}


