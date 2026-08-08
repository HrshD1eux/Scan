package com.HrshD1eux.Scan.camera

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

object GalleryScanner {
    fun scanImage(context: Context, imageUri: Uri, onResult: (List<Barcode>) -> Unit) {
        try {
            val image = InputImage.fromFilePath(context, imageUri)
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()
            val scanner = BarcodeScanning.getClient(options)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    onResult(barcodes)
                }
                .addOnFailureListener {
                    // Handle failure
                    onResult(emptyList())
                }
        } catch (e: Exception) {
            onResult(emptyList())
        }
    }
}
