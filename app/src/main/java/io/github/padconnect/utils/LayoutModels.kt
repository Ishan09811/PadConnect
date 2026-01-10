package io.github.padconnect.utils

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ControllerLayout(
    val name: String,
    val elements: List<ControllerElement>
)

@Serializable
sealed class ControllerElement {
    abstract val id: String
    abstract val x: Float
    abstract val y: Float
    abstract val size: Float
    abstract val opacity: Float
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
@SerialName("button")
data class ButtonElement(
    override val id: String,
    override val x: Float,
    override val y: Float,
    override val size: Float,
    override val opacity: Float,
    val key: GamepadKey
) : ControllerElement()

@SuppressLint("UnsafeOptInUsageError")
@Serializable
@SerialName("dpad")
data class DPadElement(
    override val id: String,
    override val x: Float,
    override val y: Float,
    override val size: Float,
    override val opacity: Float
) : ControllerElement()

enum class GamepadKey {
    A, B, X, Y, LB, RB, START, SELECT
}

