/*
 * Copyright (C) 2026 Ishan
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed without any warranty. See the GNU General Public License for more details.
 */

package io.github.padconnect.utils

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import io.github.padconnect.BuildConfig
import java.io.File

object Logger {
    private const val CURRENT = "padconnect-current.log"
    private const val PREVIOUS = "padconnect-previous.log"

    private var logFile: File? = null
    private const val MAX_SIZE = 1_000_000 // 1MB

    private val dispatcher = Dispatchers.IO.limitedParallelism(1)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private fun trimIfNeeded() {
        logFile?.let {
            if (it.length() > MAX_SIZE) {
                it.writeText("")
            }
        }
    }

    fun init(context: Context) {
        val dir = context.getExternalFilesDir(null) ?: return

        val currentFile = File(dir, CURRENT)
        val previousFile = File(dir, PREVIOUS)

        if (currentFile.exists()) {
            try {
                if (previousFile.exists()) {
                    previousFile.delete()
                }
                currentFile.renameTo(previousFile)
            } catch (e: Exception) {
                Log.e("Logger", "Rename failed: ${e.message}")
            }
        }

        logFile = File(dir, CURRENT)
        logFile?.createNewFile()
        logFile?.writeText("")
        info("Logger", "=== App session started ===")
        info("Logger", "Device: ${Build.MODEL}")
        info("Logger", "Android: ${Build.VERSION.RELEASE}")
        info("Logger", "App version: ${BuildConfig.VERSION_NAME}")
    }

    fun info(tag: String, message: String) {
        val line = "[$tag]: $message\n"

        scope.launch {
            try {
                trimIfNeeded()
                logFile?.appendText(line)
            } catch (e: Exception) {
                Log.e("Logger", "Write failed: ${e.message}")
            }
        }
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        val line = "[$tag][ERROR]: $message ${throwable?.message}\n"

        scope.launch {
            try {
                trimIfNeeded()
                logFile?.appendText(line)
            } catch (e: Exception) {
                Log.e("FileLogger", "Write failed: ${e.message}")
            }
        }
    }

    fun shutdown() {
        info("Logger", "=== App session ended ===")
        scope.cancel()
    }
}