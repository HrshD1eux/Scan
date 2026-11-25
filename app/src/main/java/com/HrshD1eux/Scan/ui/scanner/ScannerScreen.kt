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

import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape

@androidx.camera.core.ExperimentalGetImage
@Composable
fun ScannerScreen(
    onScanSuccess: (List<Barcode>) -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onShareQrClick: () -> Unit,
    isIncognito: Boolean = false,
    onToggleIncognito: () -> Unit = {},
    isBatchMode: Boolean = false,
    batchCount: Int = 0,
    onResetBatch: () -> Unit = {}
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

            // Safe Top Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onShareQrClick,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                        .size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Share or Generate QR",
                        tint = Color.White
                    )
                }

                if (isIncognito) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 3.dp
                    ) {
                        Text(
                            "Incognito (0 saved)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f, fill = false))
                }

                IconButton(
                    onClick = onToggleIncognito,
                    modifier = Modifier
                        .background(
                            if (isIncognito) MaterialTheme.colorScheme.primaryContainer else Color.Black.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .size(46.dp)
                ) {
                    Icon(
                        imageVector = if (isIncognito) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Incognito Mode",
                        tint = if (isIncognito) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
                    )
                }
            }

            if (torchSuggestionEnabled && isLowLightDetected && !isFlashlightOn) {
                TorchSuggestionButton(
                    onEnableFlashlight = {
                        isFlashlightOn = true
                        userToggledFlashlight = true
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 70.dp, end = 16.dp)
                )
            }

            ViewfinderReticleOverlay(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            )

            // Batch Mode Floating Counter HUD
            if (isBatchMode) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 88.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "⚡ Batch Mode: $batchCount scanned",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = onHistoryClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("View")
                        }
                        if (batchCount > 0) {
                            IconButton(onClick = onResetBatch, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset batch counter")
                            }
                        }
                    }
                }
            }

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
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .fillMaxWidth()
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
    Surface(
        color = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(26.dp),
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScannerBarItem(
                icon = Icons.Default.History,
                label = "History",
                onClick = onHistoryClick
            )

            ScannerBarItem(
                icon = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                label = "Torch",
                isActive = isFlashlightOn,
                onClick = onToggleFlashlight
            )

            ScannerBarItem(
                icon = Icons.Default.Image,
                label = "Gallery",
                onClick = onGalleryClick
            )

            ScannerBarItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun RowScope.ScannerBarItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else Color.White,
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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

