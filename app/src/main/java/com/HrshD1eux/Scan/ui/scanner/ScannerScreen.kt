package com.HrshD1eux.Scan.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch

@androidx.camera.core.ExperimentalGetImage
@Composable
fun ScannerScreen(
    onScanSuccess: (List<Barcode>) -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onShareQrClick: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val autoFlashlightEnabled by settingsManager.autoFlashlightFlow.collectAsState(initial = true)
    val torchSuggestionEnabled by settingsManager.torchSuggestionFlow.collectAsState(initial = true)
    var isLowLightDetected by remember { mutableStateOf(false) }

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
    
    val scope = rememberCoroutineScope()
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val barcodes = GalleryScanner.scanImage(context, uri)
                if (barcodes.isNotEmpty()) {
                    onScanSuccess(barcodes)
                }
            }
        }
    }

    DisposableEffect(autoFlashlightEnabled, torchSuggestionEnabled) {
        if (autoFlashlightEnabled || torchSuggestionEnabled) {
            lightSensorManager.startListening { isLowLight ->
                isLowLightDetected = isLowLight
                if (autoFlashlightEnabled && !userToggledFlashlight) {
                    isFlashlightOn = isLowLight
                }
            }
        } else {
            lightSensorManager.stopListening()
            isLowLightDetected = false
            if (autoFlashlightEnabled && !userToggledFlashlight) {
                isFlashlightOn = false
            }
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

            // Top action: Share / Create QR
            IconButton(
                onClick = onShareQrClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 40.dp, start = 20.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "Share or Generate QR",
                    tint = Color.White
                )
            }

            if (torchSuggestionEnabled && isLowLightDetected && !isFlashlightOn) {
                TorchSuggestionButton(
                    onEnableFlashlight = {
                        isFlashlightOn = true
                        userToggledFlashlight = true
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 36.dp, end = 20.dp)
                )
            }

            ViewfinderReticleOverlay(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            )

            ScannerControlBar(
                isFlashlightOn = isFlashlightOn,
                onToggleFlashlight = {
                    isFlashlightOn = !isFlashlightOn
                    userToggledFlashlight = true
                },
                onGalleryClick = { galleryLauncher.launch("image/*") },
                onHistoryClick = onHistoryClick,
                onSettingsClick = onSettingsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 32.dp)
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission is required to scan.")
        }
    }
}

@Composable
private fun ScannerControlBar(
    isFlashlightOn: Boolean,
    onToggleFlashlight: () -> Unit,
    onGalleryClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(onClick = onHistoryClick) { Text("History") }

        IconButton(
            onClick = onToggleFlashlight,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.medium)
        ) {
            Icon(
                imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                contentDescription = "Flashlight",
                tint = Color.White
            )
        }

        FilledTonalButton(onClick = onGalleryClick) { Text("Gallery") }
        FilledTonalButton(onClick = onSettingsClick) { Text("Settings") }
    }
}

@Composable
private fun TorchSuggestionButton(
    onEnableFlashlight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "torch_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "torch_scale"
    )

    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = onEnableFlashlight,
            modifier = Modifier
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .size(56.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            Icon(Icons.Default.FlashlightOn, contentDescription = "Turn on Flashlight")
        }
    }
}

@Composable
private fun ViewfinderReticleOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val strokeWidth = 4.dp.toPx()
            val cornerLength = 36.dp.toPx()
            val reticleColor = Color.White.copy(alpha = 0.85f)
            val w = size.width
            val h = size.height

            val corners = listOf(
                Path().apply { moveTo(0f, cornerLength); lineTo(0f, 0f); lineTo(cornerLength, 0f) },
                Path().apply { moveTo(w - cornerLength, 0f); lineTo(w, 0f); lineTo(w, cornerLength) },
                Path().apply { moveTo(0f, h - cornerLength); lineTo(0f, h); lineTo(cornerLength, h) },
                Path().apply { moveTo(w - cornerLength, h); lineTo(w, h); lineTo(w, h - cornerLength) }
            )

            corners.forEach { drawPath(it, reticleColor, style = Stroke(strokeWidth)) }
        }
    }
}

