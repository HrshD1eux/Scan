package com.HrshD1eux.Scan.ui.history

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.HrshD1eux.Scan.actions.ActionResolver
import com.HrshD1eux.Scan.history.HistoryDatabase
import com.HrshD1eux.Scan.history.HistoryEntity
import com.HrshD1eux.Scan.history.HistoryExporter
import com.HrshD1eux.Scan.parser.ContentClassifier
import com.HrshD1eux.Scan.parser.ParsedContent
import com.HrshD1eux.Scan.ui.components.EditNoteDialog
import com.HrshD1eux.Scan.ui.settings.SettingsManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onShowQr: (String) -> Unit,
    onOpenResult: (ParsedContent) -> Unit
) {
    val context = LocalContext.current
    val historyDao = remember { HistoryDatabase.getDatabase(context).historyDao() }
    val historyItems by historyDao.getAllHistory().collectAsState(initial = emptyList())
    val settingsManager = remember { SettingsManager(context) }
    val productSearchEngine by settingsManager.productSearchEngineFlow.collectAsState(initial = "DuckDuckGo")
    val customSearchUrl by settingsManager.customSearchUrlFlow.collectAsState(initial = "")
    val autoPurgeDays by settingsManager.autoPurgeDaysFlow.collectAsState(initial = 0)
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var editingItem by remember { mutableStateOf<HistoryEntity?>(null) }

    // Run auto-purge check if configured
    LaunchedEffect(autoPurgeDays) {
        if (autoPurgeDays > 0) {
            val cutoff = System.currentTimeMillis() - (autoPurgeDays * 24L * 60L * 60L * 1000L)
            historyDao.deleteOlderThan(cutoff)
        }
    }

    // Auto-heal legacy or obfuscated single-letter types ("j", "i", "k", "h", etc.) in SQLite
    LaunchedEffect(historyItems) {
        historyItems.forEach { item ->
            val cleanType = resolveDisplayType(item.type, item.rawValue)
            if (item.type != cleanType) {
                historyDao.updateType(item.id, cleanType)
            }
        }
    }

    // Auto-discover distinct clean types in history + "All"
    val availableFilters = remember(historyItems) {
        val discoveredTypes = historyItems.map { resolveDisplayType(it.type, it.rawValue) }.distinct()
        listOf("All") + discoveredTypes
    }

    // Filtered items based on search and type chip
    val filteredItems = remember(historyItems, searchQuery, selectedFilter) {
        historyItems.filter { item ->
            val itemType = resolveDisplayType(item.type, item.rawValue)
            val matchesFilter = selectedFilter == "All" || itemType.equals(selectedFilter, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    item.primaryValue.contains(searchQuery, ignoreCase = true) ||
                    item.rawValue.contains(searchQuery, ignoreCase = true) ||
                    (item.note?.contains(searchQuery, ignoreCase = true) == true)
            matchesFilter && matchesQuery
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Scanner"
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Scan History",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    enabled = historyItems.isNotEmpty(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Export", style = MaterialTheme.typography.labelMedium)
                }
                TextButton(
                    onClick = { scope.launch { historyDao.clearHistory() } },
                    enabled = historyItems.isNotEmpty(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Clear All", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar (Strictly 1 line, responsive across all screen widths)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    "Search scans or notes...",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            maxLines = 1,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp, max = 56.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Filter Chips (Auto-populated with clean human-readable tags)
        if (availableFilters.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableFilters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter.equals(filter, ignoreCase = true),
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // List or Empty View
        if (historyItems.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Scan history is empty.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (filteredItems.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No results match your filter.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { searchQuery = ""; selectedFilter = "All" }) {
                        Text("Reset Filters")
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredItems, key = { it.id }) { item ->
                    val parsedContent = remember(item) {
                        ContentClassifier.classifyRaw(item.rawValue.ifEmpty { item.primaryValue })
                    }
                    HistoryItemView(
                        item = item,
                        parsedContent = parsedContent,
                        productSearchEngine = productSearchEngine,
                        customSearchUrl = customSearchUrl,
                        onOpenResult = { onOpenResult(parsedContent) },
                        onEditNote = { editingItem = item },
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

        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
fun HistoryItemView(
    item: HistoryEntity,
    parsedContent: ParsedContent,
    productSearchEngine: String,
    customSearchUrl: String,
    onOpenResult: () -> Unit,
    onEditNote: () -> Unit,
    onShowQr: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val dateString = remember(item.timestamp) { dateFormat.format(Date(item.timestamp)) }

    val actionResolver = remember(context, productSearchEngine, customSearchUrl) {
        ActionResolver(
            context = context,
            productSearchEngine = productSearchEngine,
            customSearchUrl = customSearchUrl,
            onShowQr = onShowQr
        )
    }

    val primaryAction = remember(parsedContent, actionResolver) {
        actionResolver.resolve(parsedContent).firstOrNull { it.isPrimary }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onOpenResult)
    ) {
        Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val displayType = remember(item.type, item.rawValue) {
                        resolveDisplayType(item.type, item.rawValue)
                    }
                    Text(
                        displayType,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!item.note.isNullOrBlank()) {
                        Text(
                            item.note,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Text(
                            item.primaryValue,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(item.primaryValue, style = MaterialTheme.typography.bodyLarge)
                    }
                    Text(
                        dateString,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Primary Quick Action Button (Open Link / Pay / Call / Connect)
                primaryAction?.let { action ->
                    FilledTonalButton(
                        onClick = action.execute,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        action.icon?.let { icon ->
                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(action.label, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))

            // Tool action row: Open Details, Copy, Edit Note, Show QR, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onOpenResult) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "View Details", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Copied Text", item.rawValue.ifEmpty { item.primaryValue })
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onEditNote) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Note", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onShowQr) {
                    Icon(Icons.Default.QrCode, contentDescription = "Show QR", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

/**
 * Resolves a human-readable, un-obfuscated type for display and filtering.
 * Auto-repairs obfuscated single-letter types ("j", "i", "k", "h", etc.) by re-classifying rawValue.
 */
internal fun resolveDisplayType(type: String, rawValue: String): String {
    val clean = when (type.trim().lowercase()) {
        "url" -> "URL"
        "upi" -> "UPI"
        "wifi", "wi-fi" -> "Wi-Fi"
        "otp" -> "OTP"
        "sms" -> "SMS"
        "text" -> "Text"
        "phone" -> "Phone"
        "email" -> "Email"
        "product" -> "Product"
        "contact" -> "Contact"
        "geo", "location" -> "Location"
        "barcode", "unknownbarcode" -> "Barcode"
        else -> null
    }
    if (clean != null) return clean

    return ContentClassifier.classifyRaw(rawValue).typeName
}
