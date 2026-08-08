package com.HrshD1eux.Scan.ui.settings

import android.content.Intent
import android.net.Uri
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    
    val currentVersion = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Scan", style = MaterialTheme.typography.headlineLarge)
        Text("Version $currentVersion", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Open instantly. Scan anything. Understand what it is. Give the user the correct action immediately.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        if (updateInfo == null) {
            Button(
                onClick = {
                    scope.launch {
                        isChecking = true
                        statusText = "Checking for updates..."
                        val info = checkForUpdate()
                        isChecking = false
                        if (info != null && info.version != currentVersion) {
                            updateInfo = info
                            statusText = "New version (${info.version}) available!"
                        } else {
                            statusText = "You are on the latest version."
                        }
                    }
                },
                enabled = !isChecking
            ) {
                Text(if (isChecking) "Checking..." else "Check for Updates")
            }
        } else {
            Button(
                onClick = {
                    scope.launch {
                        isDownloading = true
                        downloadAndInstallApk(context, updateInfo!!.apkUrl) { progress ->
                            statusText = progress
                        }
                        isDownloading = false
                    }
                },
                enabled = !isDownloading
            ) {
                Text(if (isDownloading) "Downloading..." else "Download & Install Update")
            }
        }

        statusText?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }

        if (isDownloading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
    try {
        val response = URL("https://api.github.com/repos/HrshD1eux/Scan/releases/latest").readText()
        val tagMatch = "\"tag_name\"\\s*:\\s*\"v?([^\"]+)\"".toRegex().find(response)
        val latestVersion = tagMatch?.groupValues?.get(1)
        
        val apkUrlMatch = "\"browser_download_url\"\\s*:\\s*\"([^\"]+\\.apk)\"".toRegex().find(response)
        val downloadUrl = apkUrlMatch?.groupValues?.get(1)
        
        if (latestVersion != null && downloadUrl != null) {
            UpdateInfo(version = latestVersion, apkUrl = downloadUrl)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

suspend fun downloadAndInstallApk(context: Context, urlString: String, onProgress: (String) -> Unit) = withContext(Dispatchers.IO) {
    try {
        onProgress("Downloading update APK...")
        val url = URL(urlString)
        val connection = url.openConnection()
        connection.connect()
        
        val file = File(context.cacheDir, "update.apk")
        if (file.exists()) {
            file.delete()
        }
        
        url.openStream().use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        onProgress("Opening Installer...")
        
        val authority = "${context.packageName}.provider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                onProgress("Permission required! Directing to settings to enable installation from 'Scan'...")
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
                return@withContext
            }
        }
        
        context.startActivity(intent)
        onProgress("Install request sent.")
    } catch (e: Exception) {
        onProgress("Failed to download or install update: ${e.localizedMessage}")
    }
}

data class UpdateInfo(val version: String, val apkUrl: String)
