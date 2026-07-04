/*
 * Copyright (C) 2026 Ishan
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed without any warranty. See the GNU General Public License for more details.
 */

package io.github.padconnect.ui.main

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.github.padconnect.BuildConfig
import io.github.padconnect.R

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    appName: String = stringResource(id = R.string.app_name),
    version: String = BuildConfig.VERSION_NAME,
    author: String = "Ishan09811",
    about: String = "Turn your phone into a PC game controller",
    githubUrl: String = "https://github.com/Ishan09811/PadConnect",
    discordUrl: String = "https://discord.gg/BrMAZbEyXs",
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(painter = painterResource(R.drawable.ic_keyboard_arrow_left), contentDescription = null)
                        }
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = appName,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Text(
                            text = "Version $version",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                AboutCard(
                    title = "About",
                    content = about
                )
            }

            item {
                AboutCard(
                    title = "Author",
                    content = author
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        AboutLinkItem(
                            title = "GitHub",
                            subtitle = githubUrl,
                            icon = painterResource(R.drawable.ic_code)
                        ) {
                            openLink(context, githubUrl)
                        }

                        HorizontalDivider()

                        AboutLinkItem(
                            title = "Discord",
                            subtitle = discordUrl,
                            icon = painterResource(R.drawable.ic_chat)
                        ) {
                            openLink(context, discordUrl)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutCard(title: String, content: String) {
    Card(
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AboutLinkItem(
    title: String,
    subtitle: String,
    icon: Painter,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

fun openLink(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    context.startActivity(intent)
}