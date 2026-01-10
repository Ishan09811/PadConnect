
package io.github.padconnect.utils

import kotlinx.serialization.Serializable

@Serializable
data class GamepadEvent(
    val type: String,
    val key: String,
    val value: Float = 1f,
    val timestamp: Long = System.nanoTime()
)

interface GamepadTransport {
    fun send(event: GamepadEvent)
    fun isAvailable(): Boolean
}
