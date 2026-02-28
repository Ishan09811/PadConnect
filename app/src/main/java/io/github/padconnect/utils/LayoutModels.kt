package io.github.padconnect.utils

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ControllerLayout(
    var name: String,
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
data class AnalogStickElement(
    override val id: String,
    override val x: Float,
    override val y: Float,
    override val size: Float,
    override val opacity: Float
) : ControllerElement()

enum class GamepadKey(val id: Int) {
    A(0x1000), B(0x2000), X(0x4000), Y(0x8000), L3(0x0040), R3(0x0080), LT(7), RT(8), LB(0x0100), RB(0x0200), START(0x0010), SELECT(0x0020)
}

