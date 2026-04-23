/*
 * Copyright (C) 2026 Ishan
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed without any warranty. See the GNU General Public License for more details.
 */


package io.github.padconnect.utils.settings

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class ConfigModel(
    val inputUpdateRate: Int = 500,
    val initialSetupFinished: Boolean = false
)

object GlobalConfig {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private lateinit var configFile: File
    private var config: ConfigModel = ConfigModel()

    fun init(context: Context) {
        configFile = File(context.getExternalFilesDir(null), "config.json")

        if (configFile.exists()) {
            val content = configFile.readText()
            config = json.decodeFromString(content)
        } else {
            save()
        }
    }

    private fun save() {
        configFile.writeText(json.encodeToString(config))
    }

    object INPUT_UPDATE_RATE {
        var int: Int
            get() = config.inputUpdateRate
            set(value) {
                config = config.copy(inputUpdateRate = value)
                save()
            }
    }

    object INITIAL_SETUP_FINISHED {
        var boolean: Boolean
            get() = config.initialSetupFinished
            set(value) {
                config = config.copy(initialSetupFinished = value)
                save()
            }
    }
}