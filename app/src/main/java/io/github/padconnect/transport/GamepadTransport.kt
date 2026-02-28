package io.github.padconnect.transport

data class GamepadState(
    var buttons: Int = 0,
    var lx: Short = 0,
    var ly: Short = 0,
    var rx: Short = 0,
    var ry: Short = 0,
    var lt: Byte = 0,
    var rt: Byte = 0
)


interface GamepadTransport {
    fun setButton(mask: Int, down: Boolean)
    fun setLeftAxis(x: Float, y: Float)
    fun setRightAxis(x: Float, y: Float)
    fun isAvailable(): Boolean
}
