
package io.github.padconnect.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

sealed interface AppDialog {
    data class Message(
        val title: String,
        val message: String,
        val confirmText: String = "OK",
        val onConfirm: () -> Unit = {}
    ) : AppDialog

    data class Input(
        val title: String,
        val placeholder: String? = null,
        val confirmText: String = "OK",
        val initialValue: String = "",
        val isValid: (String) -> Boolean = { true },
        val onConfirm: (String) -> Unit
    ) : AppDialog
}

object AlertDialogQueue {

    private val _queue = mutableStateListOf<AppDialog>()
    val queue: List<AppDialog> = _queue

    fun show(dialog: AppDialog) {
        _queue.add(dialog)
    }

    fun dismissCurrent() {
        if (_queue.isNotEmpty()) {
            _queue.removeAt(0)
        }
    }

    val current: AppDialog?
        get() = _queue.firstOrNull()
}

@Composable
fun AlertDialogHost() {
    val dialog = AlertDialogQueue.current ?: return

    when (dialog) {
        is AppDialog.Message -> {
            AlertDialog(
                onDismissRequest = {
                    AlertDialogQueue.dismissCurrent()
                },
                title = { Text(dialog.title) },
                text = { Text(dialog.message) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dialog.onConfirm()
                            AlertDialogQueue.dismissCurrent()
                        }
                    ) {
                        Text(dialog.confirmText)
                    }
                }
            )
        }

        is AppDialog.Input -> {
            var text by remember { mutableStateOf(dialog.initialValue) }

            AlertDialog(
                onDismissRequest = {
                    AlertDialogQueue.dismissCurrent()
                },
                title = { Text(dialog.title) },
                text = {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { dialog.placeholder?.let { Text(it) } },
                        singleLine = true,
                        isError = !dialog.isValid(text)
                    )
                },
                confirmButton = {
                    TextButton(
                        enabled = dialog.isValid(text),
                        onClick = {
                            dialog.onConfirm(text)
                            AlertDialogQueue.dismissCurrent()
                        }
                    ) {
                        Text(dialog.confirmText)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { AlertDialogQueue.dismissCurrent() }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

