package com.HrshD1eux.Scan.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.HrshD1eux.Scan.updater.AppUpdateManager
import com.HrshD1eux.Scan.updater.UpdateInfo
import kotlinx.coroutines.launch

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val currentVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Scan", style = MaterialTheme.typography.headlineLarge)
        Text("Version $currentVersion", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "An instant, privacy-first QR & barcode scanner built with Jetpack Compose, CameraX, and ML Kit.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (updateInfo == null) {
            Button(
                onClick = {
                    scope.launch {
                        isChecking = true
                        statusMessage = "Checking for updates..."
                        val info = AppUpdateManager.checkForUpdates()
                        isChecking = false
                        if (info != null && AppUpdateManager.isNewerVersion(info.version, currentVersion)) {
                            updateInfo = info
                            statusMessage = "Update v${info.version} is available."
                        } else {
                            statusMessage = "You have the latest version."
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
                        AppUpdateManager.downloadAndPrepareInstall(context, updateInfo!!.apkUrl) { progress ->
                            statusMessage = progress
                        }.onSuccess { intent ->
                            statusMessage = "Package installer ready."
                            context.startActivity(intent)
                        }.onFailure { error ->
                            statusMessage = "Update installation failed: ${error.localizedMessage ?: "Network error"}"
                        }
                        isDownloading = false
                    }
                },
                enabled = !isDownloading
            ) {
                Text(if (isDownloading) "Downloading..." else "Download & Install Update")
            }
        }

        statusMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }

        if (isDownloading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
