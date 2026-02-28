package io.github.padconnect.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.padconnect.dialogs.AlertDialogQueue
import io.github.padconnect.dialogs.AppDialog
import io.github.padconnect.utils.ControllerLayout
import io.github.padconnect.utils.LayoutStorage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LayoutsScreen(onLayoutSelected: (ControllerLayout) -> Unit) {
    val context = LocalContext.current
    val layouts = remember {
        mutableStateListOf<ControllerLayout>().apply {
            addAll(LayoutStorage.load(context))
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                LayoutStorage.save(context, layouts)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.displayCutout),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    AlertDialogQueue.show(
                        AppDialog.Input(
                            title = "Create Layout",
                            placeholder = "Name",
                            isValid = { name -> name.isNotBlank() && layouts.find { it.name == name.trim() } == null },
                            confirmText = "Create",
                            onConfirm = { name ->
                                val newLayout = LayoutStorage.createDefault(name)
                                layouts.add(newLayout)
                            }
                        )
                    )
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
                    LayoutCard(
                        layout,
                        onLayoutSelected,
                        onDelete = {
                            layouts.remove(it)
                        },
                        onRename = {
                            AlertDialogQueue.show(
                                AppDialog.Input(
                                    title = "Rename",
                                    initialValue = it.name,
                                    onConfirm = { name ->
                                        layouts.remove(it)
                                        layouts.add(it.copy(name = name))
                                    }
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LayoutCard(
    layout: ControllerLayout,
    onLayoutSelected: (ControllerLayout) -> Unit,
    onRename: (ControllerLayout) -> Unit,
    onDelete: (ControllerLayout) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
                .combinedClickable(
                    onClick = {
                        onLayoutSelected(layout)
                    },
                    onLongClick = {
                        menuExpanded = true
                    }
                )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(layout.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Type: XInput, Method: Wifi",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Rename Layout") },
                    text = { Text("Rename") },
                    onClick = {
                        menuExpanded = false
                        onRename(layout)
                    }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete Layout") },
                    text = { Text("Delete") },
                    onClick = {
                        menuExpanded = false
                        onDelete(layout)
                    }
                )
            }
        }
    }
}
