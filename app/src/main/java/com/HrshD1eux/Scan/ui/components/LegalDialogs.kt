package com.HrshD1eux.Scan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LicensesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Open Source Licenses", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LicenseItem(
                    name = "Scan",
                    copyright = "Copyright (C) 2024-2026 HrshD1eux",
                    license = "GNU General Public License v3.0 (GPL-3.0) with Classpath-style linking exception",
                    url = "https://github.com/HrshD1eux/Scan"
                )
                HorizontalDivider()
                LicenseItem(
                    name = "ZXing (Zebra Crossing) Core",
                    copyright = "Copyright (C) 2007-2024 ZXing authors",
                    license = "Apache License, Version 2.0",
                    url = "https://github.com/zxing/zxing"
                )
                HorizontalDivider()
                LicenseItem(
                    name = "Android Jetpack & Compose",
                    copyright = "Copyright (C) 2018-2024 The Android Open Source Project",
                    license = "Apache License, Version 2.0",
                    url = "https://source.android.com"
                )
                HorizontalDivider()
                LicenseItem(
                    name = "Kotlinx Coroutines",
                    copyright = "Copyright (C) 2017-2024 JetBrains s.r.o.",
                    license = "Apache License, Version 2.0",
                    url = "https://github.com/Kotlin/kotlinx.coroutines"
                )
                HorizontalDivider()
                LicenseItem(
                    name = "Coil Compose",
                    copyright = "Copyright (C) 2024 Coil Contributors",
                    license = "Apache License, Version 2.0",
                    url = "https://github.com/coil-kt/coil"
                )
                HorizontalDivider()
                LicenseItem(
                    name = "Google ML Kit Barcode Scanning",
                    copyright = "Copyright (C) Google LLC",
                    license = "Google APIs Terms of Service / Apache License 2.0 Client",
                    url = "https://developers.google.com/ml-kit"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun LicenseItem(name: String, copyright: String, license: String, url: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(copyright, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(license, style = MaterialTheme.typography.bodySmall)
        Text(url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun PrivacyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Privacy Guarantee", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Scan is designed with a strict zero-telemetry, 100% on-device architecture.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "• Camera frames are analyzed in volatile RAM and immediately discarded. No pictures, videos, or feeds are saved or transmitted.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "• History records are saved exclusively to a private local SQLite database on your device. You can export to CSV or erase them at any time.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "• Zero trackers, zero analytics, zero advertising SDKs, and no user accounts.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "• Network access is used solely for checking GitHub releases or opening links at your explicit direction.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
