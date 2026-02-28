
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
import io.github.padconnect.transport.TransportManager
import io.github.padconnect.utils.AnalogStickElement
import io.github.padconnect.utils.ButtonElement
import io.github.padconnect.utils.ControllerLayout
import io.github.padconnect.viewmodel.GPEmulationViewModel
import kotlin.math.roundToInt

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun GPEmulationScreen(
    layout: ControllerLayout,
    viewModel: GPEmulationViewModel
) {
    val lastLatency by viewModel.lastLatency.collectAsState()
    val controlPointers = remember { mutableSetOf<PointerId>() }
    val buttonBounds = remember { mutableStateMapOf<ButtonElement, Rect>() }
    val activeButtonPointers = remember { mutableStateMapOf<PointerId, ButtonElement>() }
    FullScreenEffect()
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(Color.Black).pointerInput(Unit) {
            awaitPointerEventScope {
                var cameraPointer: PointerId? = null
                var lastPos = Offset.Zero
                var currentVelocityX = 0f
                var currentVelocityY = 0f
                val sensitivity = 0.02f

                while (true) {
                    val event = awaitPointerEvent()

                    event.changes.forEach { change ->
                        if (change.changedToDown()) {
                            val hit = buttonBounds.entries
                                .firstOrNull { it.value.contains(change.position) }
                                ?.key

                            if (hit != null) {
                                activeButtonPointers[change.id] = hit
                                viewModel.transport?.setButton(hit.key.id, true)
                                controlPointers.add(change.id)
                                return@forEach
                            }
                        }

                        if (change.pressed && activeButtonPointers.containsKey(change.id)) {
                            val oldButton = activeButtonPointers[change.id]

                            val hit = buttonBounds.entries
                                .firstOrNull { it.value.contains(change.position) }
                                ?.key

                            if (hit != oldButton) {
                                hit?.let {
                                    oldButton?.let {
                                        viewModel.transport?.setButton(it.key.id, false)
                                    }
                                    viewModel.transport?.setButton(it.key.id, true)
                                }

                                if (hit != null) {
                                    activeButtonPointers[change.id] = hit
                                }
                            }

                            return@forEach
                        }

                        if (change.changedToUp()) {
                            if (activeButtonPointers.containsKey(change.id)) {
                                activeButtonPointers[change.id]?.let {
                                    viewModel.transport?.setButton(it.key.id, false)
                                }
                                activeButtonPointers.remove(change.id)
                                controlPointers.remove(change.id)
                                return@forEach
                            }
                        }

                        if (cameraPointer == null && change.pressed && !controlPointers.contains(change.id)) {
                            cameraPointer = change.id
                            lastPos = change.position
                        }

                        if (change.id == cameraPointer && change.pressed) {
                            val delta = change.position - lastPos
                            lastPos = change.position

                            currentVelocityX += delta.x * sensitivity
                            currentVelocityY -= delta.y * sensitivity

                            currentVelocityX = currentVelocityX.coerceIn(-1f, 1f)
                            currentVelocityY = currentVelocityY.coerceIn(-1f, 1f)

                            viewModel.transport?.setRightAxis(
                                currentVelocityX,
                                currentVelocityY
                            )
                        }

                        if (change.id == cameraPointer && !change.pressed) {
                            cameraPointer = null
                            currentVelocityX = 0f
                            currentVelocityY = 0f
                            viewModel.transport?.setRightAxis(0f, 0f)
                        }
                    }
                }
            }
        }
    ) {
        Text(text = "${lastLatency?.roundToInt()}ms", modifier = Modifier.align(Alignment.TopStart).padding(start = 25.dp), color = Color.White)
        layout.elements.forEach { element ->
            when (element) {
                is ButtonElement -> GamepadButton(
                    modifier = Modifier,
                    button = element,
                    screenWidth = maxWidth,
                    screenHeight = maxHeight,
                    buttonBounds = buttonBounds,
                    isPressed = activeButtonPointers.containsValue(element)
                )

                is AnalogStickElement -> AnalogStick(
                    dpad = element,
                    transport = viewModel.transport,
                    screenWidth = maxWidth,
                    screenHeight = maxHeight,
                    controlPointers
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GamepadButton(
    modifier: Modifier,
    button: ButtonElement,
    screenWidth: Dp,
    screenHeight: Dp,
    buttonBounds: MutableMap<ButtonElement, Rect>,
    isPressed: Boolean = false
) {
    val sizeDp = screenWidth * button.size

    val density = LocalDensity.current
    val screenWidthPx = with(density) { screenWidth.toPx() }
    val screenHeightPx = with(density) { screenHeight.toPx() }

    val sizePx = screenWidthPx * button.size
    val xPx = screenWidthPx * button.x - sizePx / 2f
    val yPx = screenHeightPx * button.y - sizePx / 2f

    buttonBounds[button] = Rect(
        xPx,
        yPx,
        xPx + sizePx,
        yPx + sizePx
    )

    Box(
        modifier = modifier
            .offset(
                x = screenWidth * button.x - sizeDp / 2,
                y = screenHeight * button.y - sizeDp / 2
            )
            .size(sizeDp)
            .graphicsLayer { alpha = button.opacity }
            .background(if (!isPressed) Color.White.copy(alpha = 0.3f) else Color.Transparent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        GamepadButtonLabel(button.key.name)
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AnalogStick(
    dpad: AnalogStickElement,
    transport: TransportManager?,
    screenWidth: Dp,
    screenHeight: Dp,
    controlPointers: MutableSet<PointerId>
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
                                controlPointers.add(change.id)
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

                                transport?.setLeftAxis(x, y)
                            }

                            if (change.id == activePointer && change.changedToUp()) {
                                activePointer = null
                                controlPointers.remove(change.id)
                                knobOffset = Offset.Zero

                                transport?.setLeftAxis(0f, 0f)
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
        "A" -> Text("A", style = labelStyle(), color = Color.White)
        "B" -> Text("B", style = labelStyle(), color = Color.White)
        "X" -> Text("X", style = labelStyle(), color = Color.White)
        "Y" -> Text("Y", style = labelStyle(), color = Color.White)

        "LB" -> Text("LB", style = smallLabelStyle(), color = Color.White)
        "RB" -> Text("RB", style = smallLabelStyle(), color = Color.White)

        "START" -> Icon(
            imageVector = Icons.Default.PlayArrow,
            tint = Color.White,
            contentDescription = "Start"
        )

        "SELECT" -> Icon(
            imageVector = Icons.Default.Menu,
            tint = Color.White,
            contentDescription = "Select"
        )

        else -> Text(keyName, style = smallLabelStyle(), color = Color.White)
    }
}

@Composable
private fun labelStyle() = MaterialTheme.typography.titleLarge.copy(
    fontWeight = FontWeight.Bold
)

@Composable
private fun smallLabelStyle() = MaterialTheme.typography.labelMedium
