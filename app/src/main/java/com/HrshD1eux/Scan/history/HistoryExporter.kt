package com.HrshD1eux.Scan.history

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HistoryExporter {

    suspend fun exportToCsv(context: Context, items: List<HistoryEntity>): Result<Intent> = withContext(Dispatchers.IO) {
        runCatching {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val exportFile = File(exportDir, "scan_history_${System.currentTimeMillis()}.csv")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            exportFile.bufferedWriter().use { writer ->
                writer.write("ID,Type,Scanned Value,Note,Timestamp\n")
                items.forEach { item ->
                    val cleanValue = item.primaryValue.replace("\"", "\"\"")
                    val cleanNote = (item.note ?: "").replace("\"", "\"\"")
                    val dateString = dateFormat.format(Date(item.timestamp))
                    writer.write("${item.id},\"${item.type}\",\"${cleanValue}\",\"${cleanNote}\",\"${dateString}\"\n")
                }
                writer.flush()
            }

            val authority = "${context.packageName}.provider"
            val contentUri = FileProvider.getUriForFile(context, authority, exportFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            Intent.createChooser(shareIntent, "Share Scan History").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}
