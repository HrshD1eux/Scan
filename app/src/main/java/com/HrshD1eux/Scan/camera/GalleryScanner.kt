package com.HrshD1eux.Scan.camera

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

object GalleryScanner {
    private const val TAG = "GalleryScanner"

    private val scanner: BarcodeScanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        BarcodeScanning.getClient(options)
    }

    suspend fun scanImage(context: Context, imageUri: Uri): List<Barcode> = withContext(Dispatchers.IO) {
        try {
            val image = InputImage.fromFilePath(context, imageUri)
            suspendCancellableCoroutine { continuation ->
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        if (continuation.isActive) continuation.resume(barcodes)
                    }
                    .addOnFailureListener { error ->
                        Log.w(TAG, "Barcode analysis failed for gallery image", error)
                        if (continuation.isActive) continuation.resume(emptyList())
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load image from URI: $imageUri", e)
            emptyList()
        }
    }
}
