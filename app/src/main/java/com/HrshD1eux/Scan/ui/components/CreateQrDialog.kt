package com.HrshD1eux.Scan.ui.components

import android.Manifest
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.HrshD1eux.Scan.utils.QrCodeGenerator
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQrDialog(
    initialText: String = "",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Text/URL", "Wi-Fi", "UPI Pay", "Contact")

    // Text state
    var textInput by remember { mutableStateOf(initialText) }

    // Wi-Fi state
    var wifiSsid by remember { mutableStateOf("") }
    var wifiPassword by remember { mutableStateOf("") }
    var wifiSecurity by remember { mutableStateOf("WPA") }
    var wifiHidden by remember { mutableStateOf(false) }

    // UPI state
    var upiVpa by remember { mutableStateOf("") }
    var upiName by remember { mutableStateOf("") }
    var upiAmount by remember { mutableStateOf("") }

    // Contact state
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactOrg by remember { mutableStateOf("") }

    // Computed payload
    val payload = remember(
        selectedTab, textInput,
        wifiSsid, wifiPassword, wifiSecurity, wifiHidden,
        upiVpa, upiName, upiAmount,
        contactName, contactPhone, contactEmail, contactOrg
    ) {
        when (selectedTab) {
            0 -> textInput.trim()
            1 -> {
                if (wifiSsid.isBlank()) ""
                else {
                    val sec = if (wifiSecurity == "Open") "nopass" else wifiSecurity
                    val hiddenStr = if (wifiHidden) "H:true;" else ""
                    val passStr = if (wifiPassword.isNotBlank()) "P:$wifiPassword;" else ""
                    "WIFI:T:$sec;S:$wifiSsid;${passStr}${hiddenStr};"
                }
            }
            2 -> {
                if (upiVpa.isBlank()) ""
                else {
                    val namePart = if (upiName.isNotBlank()) "&pn=${java.net.URLEncoder.encode(upiName, "UTF-8")}" else ""
                    val amPart = if (upiAmount.isNotBlank()) "&am=${upiAmount.trim()}" else ""
                    "upi://pay?pa=${upiVpa.trim()}${namePart}${amPart}&cu=INR"
                }
            }
            3 -> {
                if (contactName.isBlank() && contactPhone.isBlank()) ""
                else {
                    buildString {
                        appendLine("BEGIN:VCARD")
                        appendLine("VERSION:3.0")
                        if (contactName.isNotBlank()) appendLine("FN:$contactName")
                        if (contactPhone.isNotBlank()) appendLine("TEL:$contactPhone")
                        if (contactEmail.isNotBlank()) appendLine("EMAIL:$contactEmail")
                        if (contactOrg.isNotBlank()) appendLine("ORG:$contactOrg")
                        append("END:VCARD")
                    }
                }
            }
            else -> ""
        }
    }

    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    LaunchedEffect(payload) {
        if (payload.isNotBlank()) {
            isGenerating = true
            generatedBitmap = QrCodeGenerator.generateQr(payload)
            isGenerating = false
        } else {
            generatedBitmap = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Offline QR Studio", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tab Row
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input fields per tab
                when (selectedTab) {
                    0 -> {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            label = { Text("Text or Link") },
                            placeholder = { Text("Enter URL or text...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            trailingIcon = {
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                                    if (!clipText.isNullOrBlank()) textInput = clipText
                                }) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste from Clipboard")
                                }
                            }
                        )
                    }
                    1 -> {
                        OutlinedTextField(
                            value = wifiSsid,
                            onValueChange = { wifiSsid = it },
                            label = { Text("Network Name (SSID)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = wifiPassword,
                            onValueChange = { wifiPassword = it },
                            label = { Text("Wi-Fi Password") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("WPA", "WEP", "Open").forEach { sec ->
                                FilterChip(
                                    selected = wifiSecurity == sec,
                                    onClick = { wifiSecurity = sec },
                                    label = { Text(sec) }
                                )
                            }
                        }
                    }
                    2 -> {
                        OutlinedTextField(
                            value = upiVpa,
                            onValueChange = { upiVpa = it },
                            label = { Text("UPI ID (VPA) *") },
                            placeholder = { Text("e.g. merchant@upi") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = upiName,
                            onValueChange = { upiName = it },
                            label = { Text("Payee Name (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = upiAmount,
                            onValueChange = { upiAmount = it },
                            label = { Text("Amount in ₹ (Optional)") },
                            placeholder = { Text("e.g. 150.00") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    3 -> {
                        OutlinedTextField(
                            value = contactName,
                            onValueChange = { contactName = it },
                            label = { Text("Full Name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = contactEmail,
                            onValueChange = { contactEmail = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = contactOrg,
                            onValueChange = { contactOrg = it },
                            label = { Text("Company / Organization") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // QR Preview Card
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isGenerating -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        generatedBitmap != null -> {
                            Image(
                                bitmap = generatedBitmap!!.asImageBitmap(),
                                contentDescription = "Generated QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Text(
                                text = "Enter details above to generate high-contrast QR code",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions: Save to Gallery & Share Image
                if (generatedBitmap != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { saveQrToGallery(context, generatedBitmap!!) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save PNG", style = MaterialTheme.typography.labelSmall)
                        }
                        FilledTonalButton(
                            onClick = { shareQrImage(context, generatedBitmap!!) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share Image", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private fun saveQrToGallery(context: Context, bitmap: Bitmap) {
    try {
        val filename = "QR_${System.currentTimeMillis()}.png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Scan")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
                Toast.makeText(context, "QR saved to Pictures/Scan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to create MediaStore entry", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Android 7.0 - 9.0 (API 24-28)
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            val targetDir = if (hasPermission) {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Scan").apply { mkdirs() }
            } else {
                File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Scan").apply { mkdirs() }
            }

            val file = File(targetDir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // Index file into Android system gallery
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("image/png"),
                null
            )

            val msg = if (hasPermission) "QR saved to Pictures/Scan" else "QR saved to app storage"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to save: ${e.localizedMessage ?: "Storage error"}", Toast.LENGTH_SHORT).show()
    }
}

private fun shareQrImage(context: Context, bitmap: Bitmap) {
    try {
        val shareDir = File(context.cacheDir, "shares").apply { mkdirs() }
        val shareFile = File(shareDir, "qr_code.png")
        shareFile.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val authority = "${context.packageName}.provider"
        val contentUri = FileProvider.getUriForFile(context, authority, shareFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share QR Code").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to share: ${e.localizedMessage ?: "Share error"}", Toast.LENGTH_SHORT).show()
    }
}
