
package io.github.padconnect.utils

import kotlin.math.abs
import kotlin.math.atan2

object InputResolver {
    fun resolveDpadDirection(
        touchX: Float,
        touchY: Float,
        size: Float
    ): Pair<Float, Float> {
        val center = size / 2f
        val dx = touchX - center
        val dy = center - touchY

        val deadZone = size * 0.15f
        if (abs(dx) < deadZone && abs(dy) < deadZone) {
            return 0f to 0f
        }

        val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
        val direction = ((angle + 360 + 22.5) % 360 / 45).toInt()

        return when (direction) {
            0 -> 1f to 0f     // Right
            1 -> 1f to 1f     // Up-Right
            2 -> 0f to 1f     // Up
            3 -> -1f to 1f    // Up-Left
            4 -> -1f to 0f    // Left
            5 -> -1f to -1f   // Down-Left
            6 -> 0f to -1f    // Down
            7 -> 1f to -1f    // Down-Right
            else -> 0f to 0f
        }
    }
}

