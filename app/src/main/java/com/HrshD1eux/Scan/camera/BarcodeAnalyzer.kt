package com.HrshD1eux.Scan.camera

import android.os.SystemClock
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@ExperimentalGetImage
class BarcodeAnalyzer(
    private val throttleMs: Long = 500L,
    private val onBarcodeDetected: (List<Barcode>) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()

    private val scanner = BarcodeScanning.getClient(options)
    private var lastAnalyzedTimestamp = 0L
    private var isBusy = false

    override fun analyze(imageProxy: ImageProxy) {
        val currentTimestamp = SystemClock.elapsedRealtime()
        if (currentTimestamp - lastAnalyzedTimestamp < throttleMs || isBusy) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        isBusy = true
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    lastAnalyzedTimestamp = SystemClock.elapsedRealtime()
                    onBarcodeDetected(barcodes)
                }
            }
            .addOnCompleteListener {
                isBusy = false
                imageProxy.close()
            }
    }
}
