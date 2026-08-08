package com.HrshD1eux.Scan.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var updateStatus by remember { mutableStateOf<String?>(null) }
    
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

        Button(onClick = {
            scope.launch {
                updateStatus = "Checking for updates..."
                val latest = checkForUpdate()
                if (latest != null && latest != currentVersion) {
                    updateStatus = "New version ($latest) available! Check GitHub Releases."
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/HrshD1eux/Scan/releases"))
                    context.startActivity(intent)
                } else {
                    updateStatus = "You are on the latest version."
                }
            }
        }) {
            Text("Check for Updates")
        }

        updateStatus?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

suspend fun checkForUpdate(): String? = withContext(Dispatchers.IO) {
    try {
        val response = URL("https://api.github.com/repos/HrshD1eux/Scan/releases/latest").readText()
        // Extract tag_name using regex since we don't want to add a JSON dependency just for this
        val match = "\"tag_name\"\\s*:\\s*\"v?([^\"]+)\"".toRegex().find(response)
        match?.groupValues?.get(1)
    } catch (e: Exception) {
        null
    }
}
