/*
 * Copyright (C) 2026 Ishan
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed without any warranty. See the GNU General Public License for more details.
 */

package io.github.padconnect.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import io.github.padconnect.PadConnectApplication
import kotlin.math.max

class HapticHandler {

    private val vibrator =
        PadConnectApplication.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    @Volatile
    private var latest = Pair(0, 0)

    @Volatile
    private var running = true

    private var lastAmplitude = 0
    private var lastUpdateTime = 0L

    private val thread = Thread {
        while (running && !Thread.interrupted()) {

            val (large, small) = latest

            val now = System.currentTimeMillis()

            if (now - lastUpdateTime >= 16) {
                lastUpdateTime = now
                applyVibration(large, small)
            }

            Thread.sleep(8)
        }
    }.apply {
        name = "haptic-thread"
        start()
    }

    fun onRumble(large: Int, small: Int) {
        latest = large to small
    }

    private fun applyVibration(large: Int, small: Int) {
        val intensity = max(large, small)

        if (intensity <= 0) {
            if (lastAmplitude != 0) {
                vibrator.cancel()
                lastAmplitude = 0
            }
            return
        }

        val boosted = (intensity * 2).coerceAtMost(255)
        val texture = small / 255.0
        val modulationStrength = 0.3 * texture

        val modulated = (
                boosted * (1.0 - modulationStrength +
                        modulationStrength * kotlin.math.sin(System.nanoTime() / 1_000_000.0))
                ).toInt().coerceIn(1, 255)

        val finalAmplitude = if (small > 0) modulated else boosted
        if (kotlin.math.abs(finalAmplitude - lastAmplitude) < 10) return

        lastAmplitude = finalAmplitude

        val effect = VibrationEffect.createOneShot(
            70,
            finalAmplitude
        )

        vibrator.vibrate(effect)
    }

    fun stop() {
        running = false
        thread.interrupt()
        vibrator.cancel()
    }
}