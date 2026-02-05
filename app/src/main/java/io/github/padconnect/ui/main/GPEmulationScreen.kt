
package io.github.padconnect.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.github.padconnect.utils.ButtonElement
import io.github.padconnect.utils.ControllerLayout
import io.github.padconnect.utils.DPadElement
import io.github.padconnect.utils.GamepadEvent
import io.github.padconnect.utils.TransportManager
import kotlin.math.roundToInt

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun GPEmulationScreen(
    layout: ControllerLayout,
    transport: TransportManager
) {
    FullScreenEffect()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        layout.elements.forEach { element ->
            when (element) {
                is ButtonElement -> GamepadButton(
                    button = element,
                    transport = transport,
                    screenWidth = maxWidth,
                    screenHeight = maxHeight
                )

                is DPadElement -> GamepadDPad(
                    dpad = element,
                    transport = transport,
                    screenWidth = maxWidth,
                    screenHeight = maxHeight
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GamepadButton(
    button: ButtonElement,
    transport: TransportManager,
    screenWidth: Dp,
    screenHeight: Dp
) {
    val sizeDp = screenWidth * button.size
    var background by remember { mutableStateOf(Color.White.copy(alpha = 0.3f)) }

    Box(
        modifier = Modifier
            .offset(
                x = screenWidth * button.x - sizeDp / 2,
                y = screenHeight * button.y - sizeDp / 2
            )
            .size(sizeDp)
            .graphicsLayer { alpha = button.opacity }
            .background(background, CircleShape)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            if (change.changedToDown()) {
                                transport.send(GamepadEvent("button_down", button.key.name))
                                background = Color.Transparent
                            }
                            if (change.changedToUp()) {
                                transport.send(GamepadEvent("button_up", button.key.name))
                                background = Color.White.copy(alpha = 0.3f)
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        GamepadButtonLabel(button.key.name)
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GamepadDPad(
    dpad: DPadElement,
    transport: TransportManager,
    screenWidth: Dp,
    screenHeight: Dp
) {
    val sizeDp = screenWidth * dpad.size
    val sizePx = with(LocalDensity.current) { sizeDp.toPx() }
    val radius = sizePx / 2f

    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    var activePointer by remember { mutableStateOf<PointerId?>(null) }

    Box(
        modifier = Modifier
            .offset(
                x = screenWidth * dpad.x - sizeDp / 2,
                y = screenHeight * dpad.y - sizeDp / 2
            )
            .size(sizeDp)
            .graphicsLayer { alpha = dpad.opacity }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()

                        event.changes.forEach { change ->
                            if (change.pressed && activePointer == null) {
                                activePointer = change.id
                            }

                            if (change.id == activePointer && change.pressed) {
                                val center = Offset(radius, radius)
                                val delta = change.position - center

                                val dist = delta.getDistance()
                                val clamped =
                                    if (dist > radius) delta * (radius / dist)
                                    else delta

                                knobOffset = clamped

                                val x = (clamped.x / radius).coerceIn(-1f, 1f)
                                val y = (-clamped.y / radius).coerceIn(-1f, 1f)

                                transport.send(GamepadEvent("axis", "DPAD_X", x))
                                transport.send(GamepadEvent("axis", "DPAD_Y", y))
                            }

                            if (change.id == activePointer && change.changedToUp()) {
                                activePointer = null
                                knobOffset = Offset.Zero

                                transport.send(GamepadEvent("axis", "DPAD_X", 0f))
                                transport.send(GamepadEvent("axis", "DPAD_Y", 0f))
                            }
                        }
                    }
                }
            }
    ) {
        DPadVisual(knobOffset)
    }
}

@Composable
private fun DPadVisual(knobOffset: Offset) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.White.copy(alpha = 0.15f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Thumb
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        knobOffset.x.roundToInt(),
                        knobOffset.y.roundToInt()
                    )
                }
                .size(28.dp)
                .background(
                    Color.White.copy(alpha = 0.5f),
                    CircleShape
                )
        )
    }
}


@Composable
fun FullScreenEffect() {
    val context = LocalContext.current
    val window = (context as Activity).window
    val controller = remember {
        WindowCompat.getInsetsController(window, window.decorView)
    }

    DisposableEffect(Unit) {
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())

        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

@Composable
fun GamepadButtonLabel(keyName: String) {
    when (keyName) {
        "A" -> Text("A", style = labelStyle(), color = Color.Black)
        "B" -> Text("B", style = labelStyle(), color = Color.Black)
        "X" -> Text("X", style = labelStyle(), color = Color.Black)
        "Y" -> Text("Y", style = labelStyle(), color = Color.Black)

        "LB" -> Text("LB", style = smallLabelStyle())
        "RB" -> Text("RB", style = smallLabelStyle())

        "START" -> Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Start"
        )

        "SELECT" -> Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Select"
        )

        else -> Text(keyName, style = smallLabelStyle())
    }
}

@Composable
private fun labelStyle() = MaterialTheme.typography.titleLarge.copy(
    fontWeight = FontWeight.Bold
)

@Composable
private fun smallLabelStyle() = MaterialTheme.typography.labelMedium
