/*
 * Copyright (C) 2026 Ishan
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed without any warranty. See the GNU General Public License for more details.
 */

package io.github.padconnect.ui.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ishan09811.compose_preferences.core.PreferenceSubtitle
import com.github.ishan09811.compose_preferences.preference.HomePreference
import com.github.ishan09811.compose_preferences.preference.SingleSelectionDialog
import io.github.padconnect.R
import io.github.padconnect.utils.settings.GlobalConfig

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navigateTo: ((String) -> Unit)? = null
) {
    Scaffold { contentPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item(key = "advanced_settings") {
                HomePreference(
                    title = stringResource(R.string.advanced_settings),
                    icon = { Icon(painterResource(R.drawable.ic_tune), null) },
                    description = stringResource(R.string.advanced_settings_description),
                    onClick = {
                        navigateTo?.invoke("advanced_settings")
                    }
                )
            }
        }
    }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsScreen(
    modifier: Modifier = Modifier,
    navigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current

    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
            .then(modifier), topBar = {
            LargeTopAppBar(title = {
                    Text(
                        text = stringResource(R.string.advanced_settings),
                        fontFamily = FontFamily.SansSerif
                    )
            }, scrollBehavior = topBarScrollBehavior, navigationIcon = {
                if (navigateBack != null) {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painterResource(id = R.drawable.ic_keyboard_arrow_left),
                            contentDescription = null
                        )
                    }
                }
            })
        }) { contentPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item(key = "input_update_rate") {
                val hzOptions = listOf(120, 240, 360, 500, 1000)
                var currentHz by rememberSaveable { mutableIntStateOf(GlobalConfig.INPUT_UPDATE_RATE.int) }
                SingleSelectionDialog(
                    currentValue = currentHz,
                    values = hzOptions,
                    icon = null,
                    title = "Input Update Rate",
                    onValueChange = { value ->
                        currentHz = value
                        GlobalConfig.INPUT_UPDATE_RATE.int = value
                    },
                    subtitle = { PreferenceSubtitle("How many times per second input is sent") },
                    valueToText = { "${it}Hz" }
                )
            }
        }
    }
}
