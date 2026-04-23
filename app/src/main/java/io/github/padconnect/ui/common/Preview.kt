/*
 * Copyright (C) 2026 Ishan
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed without any warranty. See the GNU General Public License for more details.
 */

package io.github.padconnect.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


/**
 * a composable function for previewing UI elements within the application's theme.
 *
 * this functions wraps the provided content within a Material3 `Surface` and applies the application's
 * theme for consistent M3 previews, otherwise, the preview defaults to using the system theme.
 *
 * @param modifier The modifier to be applied to the `Surface`.
 * @param content The content to be previewed.
 *
 * @see AppTheme
 */

@Composable
fun ComposePreview(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    MaterialTheme {
        Surface(
            modifier = modifier
        ) {
            content()
        }
    }
}