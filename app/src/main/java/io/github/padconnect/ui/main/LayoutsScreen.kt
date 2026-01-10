package io.github.padconnect.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.padconnect.utils.ControllerLayout
import io.github.padconnect.utils.LayoutStorage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LayoutsScreen(onLayoutSelected: (ControllerLayout) -> Unit) {
    val context = LocalContext.current
    var layouts by remember {
        mutableStateOf(LayoutStorage.load(context))
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val newLayout = LayoutStorage.createDefault("layout 1")
                    layouts = layouts + newLayout
                    LayoutStorage.save(context, layouts)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Layout")
            }
        }
    ) { padding ->

        if (layouts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No layouts yet")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(layouts) { layout ->
                    LayoutCard(layout, onLayoutSelected)
                }
            }
        }
    }
}

@Composable
private fun LayoutCard(layout: ControllerLayout, onLayoutSelected: (ControllerLayout) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .clickable {
                onLayoutSelected(layout)
            }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(layout.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "Opacity: ${layout.name}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
