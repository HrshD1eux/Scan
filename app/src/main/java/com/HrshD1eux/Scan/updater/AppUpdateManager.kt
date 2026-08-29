package com.HrshD1eux.Scan.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(val version: String, val apkUrl: String)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class Available(val info: UpdateInfo) : UpdateState
    data object UpToDate : UpdateState
    data class Downloading(val progressMessage: String) : UpdateState
    data class ReadyToInstall(val installIntent: Intent) : UpdateState
    data class Error(val message: String) : UpdateState
}

object AppUpdateManager {

    private const val GITHUB_API_URL = "https://api.github.com/repos/HrshD1eux/Scan/releases/latest"

    fun isNewerVersion(remote: String, current: String): Boolean {
        val cleanRemote = remote.trim().removePrefix("v").substringBefore("-")
        val cleanCurrent = current.trim().removePrefix("v").substringBefore("-")

        val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until length) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "Scan-Android-App")
                connectTimeout = 10000
                readTimeout = 10000
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext null
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val tagName = json.optString("tag_name", "").removePrefix("v")
            val assets = json.optJSONArray("assets") ?: return@withContext null

            var downloadUrl: String? = null
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val urlCandidate = asset.optString("browser_download_url", "")
                if (urlCandidate.endsWith(".apk", ignoreCase = true)) {
                    downloadUrl = urlCandidate
                    break
                }
            }

            if (tagName.isNotBlank() && downloadUrl != null) {
                UpdateInfo(version = tagName, apkUrl = downloadUrl)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun downloadAndPrepareInstall(
        context: Context,
        apkUrl: String,
        onProgress: (String) -> Unit
    ): Result<Intent> = withContext(Dispatchers.IO) {
        runCatching {
            onProgress("Connecting to download server...")
            val url = URL(apkUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                setRequestProperty("User-Agent", "Scan-Android-App")
                connectTimeout = 15000
                readTimeout = 30000
                instanceFollowRedirects = true
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("Server returned HTTP $responseCode")
            }

            val contentLength = connection.contentLengthLong
            onProgress("Downloading update package...")

            val updateDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val apkFile = File(updateDir, "update.apk")
            if (apkFile.exists()) apkFile.delete()

            connection.inputStream.use { input ->
                apkFile.outputStream().use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (contentLength > 0) {
                            val percent = (totalRead * 100 / contentLength).toInt().coerceIn(0, 100)
                            onProgress("Downloading: $percent%")
                        }
                    }
                    output.flush()
                }
            }

            onProgress("Verifying installation package...")
            val authority = "${context.packageName}.provider"
            val uri = FileProvider.getUriForFile(context, authority, apkFile)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
            }
        }
    }
}
