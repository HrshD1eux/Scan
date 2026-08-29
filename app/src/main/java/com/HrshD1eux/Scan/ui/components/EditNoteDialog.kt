package com.HrshD1eux.Scan.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.HrshD1eux.Scan.history.HistoryEntity

@Composable
fun EditNoteDialog(
    item: HistoryEntity,
    onSave: (note: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var noteText by remember(item) { mutableStateOf(item.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Tag / Note") },
        text = {
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note (e.g. Wi-Fi at office)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                onSave(noteText.trim().ifEmpty { null })
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
