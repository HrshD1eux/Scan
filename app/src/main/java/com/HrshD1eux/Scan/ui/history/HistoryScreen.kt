package com.HrshD1eux.Scan.ui.history

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.HrshD1eux.Scan.history.HistoryDatabase
import com.HrshD1eux.Scan.history.HistoryEntity
import com.HrshD1eux.Scan.history.HistoryExporter
import com.HrshD1eux.Scan.ui.components.EditNoteDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(onBack: () -> Unit, onShowQr: (String) -> Unit) {
    val context = LocalContext.current
    val historyDao = remember { HistoryDatabase.getDatabase(context).historyDao() }
    val historyItems by historyDao.getAllHistory().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var editingItem by remember { mutableStateOf<HistoryEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Scan History", style = MaterialTheme.typography.headlineMedium)
            Row {
                TextButton(
                    onClick = {
                        scope.launch {
                            HistoryExporter.exportToCsv(context, historyItems)
                                .onSuccess { intent -> context.startActivity(intent) }
                                .onFailure { e ->
                                    Toast.makeText(
                                        context,
                                        "Export failed: ${e.localizedMessage ?: "File error"}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    },
                    enabled = historyItems.isNotEmpty()
                ) {
                    Text("Export")
                }
                TextButton(onClick = { scope.launch { historyDao.clearHistory() } }) {
                    Text("Clear All")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (historyItems.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("History is empty.")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(historyItems, key = { it.id }) { item ->
                    HistoryItemView(
                        item = item,
                        onClick = { editingItem = item },
                        onShowQr = {
                            onShowQr(item.rawValue.ifEmpty { item.primaryValue })
                        },
                        onDelete = {
                            scope.launch { historyDao.deleteById(item.id) }
                        }
                    )
                }
            }
        }

        editingItem?.let { item ->
            EditNoteDialog(
                item = item,
                onSave = { newNote ->
                    scope.launch {
                        historyDao.updateNote(item.id, newNote)
                        editingItem = null
                    }
                },
                onDismiss = { editingItem = null }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
fun HistoryItemView(item: HistoryEntity, onClick: () -> Unit, onShowQr: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val dateString = remember(item.timestamp) { dateFormat.format(Date(item.timestamp)) }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.type, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                if (!item.note.isNullOrBlank()) {
                    Text(item.note, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(item.primaryValue, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(item.primaryValue, style = MaterialTheme.typography.bodyLarge)
                }
                Text(dateString, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onShowQr) {
                    Icon(Icons.Default.QrCode, contentDescription = "Show QR")
                }
                IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Copied Text", item.primaryValue)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
