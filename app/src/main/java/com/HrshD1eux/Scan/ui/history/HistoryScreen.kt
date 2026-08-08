package com.HrshD1eux.Scan.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.HrshD1eux.Scan.history.HistoryDatabase
import com.HrshD1eux.Scan.history.HistoryEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val historyDao = HistoryDatabase.getDatabase(context).historyDao()
    val historyItems by historyDao.getAllHistory().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Scan History", style = MaterialTheme.typography.headlineMedium)
            Row {
                TextButton(
                    onClick = { exportHistoryToCsv(context, historyItems) },
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
                items(historyItems) { item ->
                    HistoryItemView(item) {
                        scope.launch { historyDao.deleteById(item.id) }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
fun HistoryItemView(item: HistoryEntity, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(item.timestamp))

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.type, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(item.primaryValue, style = MaterialTheme.typography.bodyLarge)
                Text(dateString, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

fun exportHistoryToCsv(context: Context, items: List<HistoryEntity>) {
    try {
        val csvString = StringBuilder()
        csvString.append("ID,Type,Scanned Value,Timestamp\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        items.forEach { item ->
            val cleanValue = item.primaryValue.replace("\"", "\"\"") // Escape quotes
            val dateString = dateFormat.format(Date(item.timestamp))
            csvString.append("${item.id},${item.type},\"${cleanValue}\",${dateString}\n")
        }
        
        val file = File(context.cacheDir, "scan_history.csv")
        file.writeText(csvString.toString())
        
        val authority = "${context.packageName}.provider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(intent, "Share Scan History")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Export failed: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
    }
}
