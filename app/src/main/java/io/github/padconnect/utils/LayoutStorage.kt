package io.github.padconnect.utils

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File

object LayoutStorage {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    private const val FILE_NAME = "layouts.json"

    fun load(context: Context): List<ControllerLayout> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        return json.decodeFromString(file.readText())
    }

    fun load(context: Context, name: String): ControllerLayout? {
        return load(context).firstOrNull { it.name == name }
    }

    fun save(context: Context, layouts: List<ControllerLayout>) {
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(json.encodeToString(layouts))
    }

    fun createDefault(name: String): ControllerLayout {
        return ControllerLayout(
            name = name,
            elements = listOf(
                ButtonElement(
                    id = "btn_a",
                    x = 0.78f,
                    y = 0.62f,
                    size = 0.09f,
                    opacity = 0.85f,
                    key = GamepadKey.A
                ),
                ButtonElement(
                    id = "btn_b",
                    x = 0.87f,
                    y = 0.52f,
                    size = 0.09f,
                    opacity = 0.85f,
                    key = GamepadKey.B
                ),
                ButtonElement(
                    id = "btn_x",
                    x = 0.69f,
                    y = 0.52f,
                    size = 0.09f,
                    opacity = 0.85f,
                    key = GamepadKey.X
                ),
                ButtonElement(
                    id = "btn_y",
                    x = 0.78f,
                    y = 0.42f,
                    size = 0.09f,
                    opacity = 0.85f,
                    key = GamepadKey.Y
                ),

                DPadElement(
                    id = "dpad",
                    x = 0.22f,
                    y = 0.55f,
                    size = 0.18f,
                    opacity = 0.8f
                ),

                ButtonElement(
                    id = "btn_lb",
                    x = 0.20f,
                    y = 0.18f,
                    size = 0.12f,
                    opacity = 0.7f,
                    key = GamepadKey.LB
                ),
                ButtonElement(
                    id = "btn_rb",
                    x = 0.80f,
                    y = 0.18f,
                    size = 0.12f,
                    opacity = 0.7f,
                    key = GamepadKey.RB
                ),

                ButtonElement(
                    id = "btn_select",
                    x = 0.42f,
                    y = 0.45f,
                    size = 0.07f,
                    opacity = 0.6f,
                    key = GamepadKey.SELECT
                ),
                ButtonElement(
                    id = "btn_start",
                    x = 0.58f,
                    y = 0.45f,
                    size = 0.07f,
                    opacity = 0.6f,
                    key = GamepadKey.START
                )
            )
        )
    }
}
