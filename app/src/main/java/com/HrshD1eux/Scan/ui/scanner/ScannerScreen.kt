package com.HrshD1eux.Scan.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.HrshD1eux.Scan.camera.CameraView
import com.HrshD1eux.Scan.camera.GalleryScanner
import com.HrshD1eux.Scan.camera.LightSensorManager
import com.HrshD1eux.Scan.ui.settings.SettingsManager
import com.google.mlkit.vision.barcode.common.Barcode
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

@androidx.camera.core.ExperimentalGetImage
@Composable
fun ScannerScreen(
    onScanSuccess: (List<Barcode>) -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val autoFlashlightEnabled by settingsManager.autoFlashlightFlow.collectAsState(initial = true)

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var isFlashlightOn by remember { mutableStateOf(false) }
    var userToggledFlashlight by remember { mutableStateOf(false) }

    val lightSensorManager = remember { LightSensorManager(context) }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            GalleryScanner.scanImage(context, uri) { barcodes ->
                if (barcodes.isNotEmpty()) {
                    onScanSuccess(barcodes)
                }
            }
        }
    }

    DisposableEffect(autoFlashlightEnabled) {
        if (autoFlashlightEnabled) {
            lightSensorManager.startListening { isLowLight ->
                if (!userToggledFlashlight) {
                    isFlashlightOn = isLowLight
                }
            }
        } else {
            lightSensorManager.stopListening()
            if (!userToggledFlashlight) isFlashlightOn = false
        }
        onDispose {
            lightSensorManager.stopListening()
        }
    }

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraView(
                onBarcodeDetected = { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        onScanSuccess(barcodes)
                    }
                },
                isFlashlightOn = isFlashlightOn,
                modifier = Modifier.fillMaxSize()
            )

            // Viewfinder reticle overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp), // offset slightly upward from the bottom buttons
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(240.dp)) {
                    val strokeWidth = 4.dp.toPx()
                    val cornerLength = 40.dp.toPx()
                    val color = Color.White.copy(alpha = 0.8f)
                    
                    // Top-Left corner
                    val pathTl = Path().apply {
                        moveTo(0f, cornerLength)
                        lineTo(0f, 0f)
                        lineTo(cornerLength, 0f)
                    }
                    drawPath(pathTl, color, style = Stroke(strokeWidth))
                    
                    // Top-Right corner
                    val pathTr = Path().apply {
                        moveTo(size.width - cornerLength, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width, cornerLength)
                    }
                    drawPath(pathTr, color, style = Stroke(strokeWidth))
                    
                    // Bottom-Left corner
                    val pathBl = Path().apply {
                        moveTo(0f, size.height - cornerLength)
                        lineTo(0f, size.height)
                        lineTo(cornerLength, size.height)
                    }
                    drawPath(pathBl, color, style = Stroke(strokeWidth))
                    
                    // Bottom-Right corner
                    val pathBr = Path().apply {
                        moveTo(size.width - cornerLength, size.height)
                        lineTo(size.width, size.height)
                        lineTo(size.width, size.height - cornerLength)
                    }
                    drawPath(pathBr, color, style = Stroke(strokeWidth))
                }
            }

            // Controls Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onHistoryClick) { Text("History") }
                
                IconButton(
                    onClick = { 
                        isFlashlightOn = !isFlashlightOn
                        userToggledFlashlight = true
                    },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.medium)
                ) {
                    Icon(
                        imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = "Toggle Flashlight",
                        tint = Color.White
                    )
                }

                Button(onClick = { galleryLauncher.launch("image/*") }) { Text("Gallery") }
                Button(onClick = onSettingsClick) { Text("Settings") }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera access is required to scan.")
        }
    }
}
