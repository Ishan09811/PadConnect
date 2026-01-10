
package io.github.padconnect.utils

import kotlin.math.atan2

object InputResolver {
    fun resolveDpadDirection(
        touchX: Float,
        touchY: Float,
        size: Float
    ): Pair<Float, Float> {
        val center = size / 2f
        val dx = touchX - center
        val dy = touchY - center

        val angle = atan2(dy.toDouble(), dx.toDouble())
        val direction = ((Math.toDegrees(angle) + 360 + 22.5) % 360 / 45).toInt()

        return when (direction) {
            0 -> Pair(1f, 0f)     // Right
            1 -> Pair(1f, -1f)    // Up-Right
            2 -> Pair(0f, -1f)    // Up
            3 -> Pair(-1f, -1f)   // Up-Left
            4 -> Pair(-1f, 0f)    // Left
            5 -> Pair(-1f, 1f)    // Down-Left
            6 -> Pair(0f, 1f)     // Down
            7 -> Pair(1f, 1f)     // Down-Right
            else -> Pair(0f, 0f)
        }
    }
}
