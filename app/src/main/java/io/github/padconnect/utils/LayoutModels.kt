/*
 * Copyright (C) 2026 Ishan
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed without any warranty. See the GNU General Public License for more details.
 */

package io.github.padconnect.utils

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ControllerLayout(
    var name: String,
    var elements: List<ControllerElement>
)

@Serializable
sealed class ControllerElement {
    abstract val id: String
    abstract val x: Float
    abstract val y: Float
    abstract val size: Float
    abstract val opacity: Float
    abstract val enabled: Boolean
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
    override val enabled: Boolean = true,
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
    override val opacity: Float,
    override val enabled: Boolean = true
) : ControllerElement()

enum class GamepadKey(val id: Int) {
    A(0x1000), B(0x2000), X(0x4000), Y(0x8000), L3(0x0040), R3(0x0080), LT(7), RT(8), LB(0x0100), RB(0x0200), START(0x0010), SELECT(0x0020)
}

