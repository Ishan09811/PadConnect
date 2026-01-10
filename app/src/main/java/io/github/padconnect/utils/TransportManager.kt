
package io.github.padconnect.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransportManager(
    private val wifi: GamepadTransport,
    private val ble: GamepadTransport,
    private val scope: CoroutineScope
) {
    fun send(event: GamepadEvent) {
        scope.launch(Dispatchers.IO) {
            when {
                wifi.isAvailable() -> wifi.send(event)
                ble.isAvailable() -> ble.send(event)
            }
        }
    }
}
