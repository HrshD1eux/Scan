package com.HrshD1eux.Scan.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.HrshD1eux.Scan.utils.QrCodeGenerator

@Composable
fun QrCodeDialog(
    text: String,
    onDismiss: () -> Unit
) {
    val bitmapState by produceState<Bitmap?>(initialValue = null, key1 = text) {
        value = QrCodeGenerator.generateQr(text)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = { Text("Scan QR Code") },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(260.dp),
                contentAlignment = Alignment.Center
            ) {
                val bitmap = bitmapState
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Generated QR code",
                        modifier = Modifier.size(240.dp)
                    )
                } else {
                    CircularProgressIndicator()
                }
            }
        }
    )
}
