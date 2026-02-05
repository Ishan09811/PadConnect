
package io.github.padconnect.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.github.padconnect.utils.ButtonElement
import io.github.padconnect.utils.ControllerLayout
import io.github.padconnect.utils.DPadElement
import io.github.padconnect.utils.GamepadEvent
import io.github.padconnect.utils.InputResolver
import io.github.padconnect.utils.TransportManager

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

    Box(
        modifier = Modifier
            .offset(
                x = screenWidth * button.x - sizeDp / 2,
                y = screenHeight * button.y - sizeDp / 2
            )
            .size(sizeDp)
            .graphicsLayer { alpha = button.opacity }
            .background(Color.White.copy(alpha = 0.3f), CircleShape)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            if (change.changedToDown()) {
                                transport.send(GamepadEvent("button_down", button.key.name))
                            }
                            if (change.changedToUp()) {
                                transport.send(GamepadEvent("button_up", button.key.name))
                            }
                        }
                    }
                }
            }

    )
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

    Box(
        modifier = Modifier
            .offset(
                x = screenWidth * dpad.x - sizeDp / 2,
                y = screenHeight * dpad.y - sizeDp / 2
            )
            .size(sizeDp)
            .graphicsLayer { alpha = dpad.opacity }
            .background(
                Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(16.dp)
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()

                        event.changes.forEach { change ->
                            val id = change.id
                            val pos = change.position

                            if (change.pressed) {
                                val (x, y) = InputResolver.resolveDpadDirection(
                                    pos.x,
                                    pos.y,
                                    sizePx
                                )

                                transport.send(GamepadEvent("axis", "DPAD_X", x))
                                transport.send(GamepadEvent("axis", "DPAD_Y", y))
                            }

                            if (change.changedToUp()) {
                                transport.send(GamepadEvent("axis", "DPAD_X", 0f))
                                transport.send(GamepadEvent("axis", "DPAD_Y", 0f))
                            }
                        }
                    }
                }
            }

    ) {
        DPadVisual()
    }
}


@Composable
private fun DPadVisual() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Box(
            Modifier
                .align(Alignment.Center)
                .size(24.dp)
                .background(Color.White.copy(0.3f), CircleShape)
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



