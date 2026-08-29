package com.HrshD1eux.Scan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.HrshD1eux.Scan.actions.ActionResolver
import com.HrshD1eux.Scan.camera.GalleryScanner
import com.HrshD1eux.Scan.ui.components.CreateQrDialog
import com.HrshD1eux.Scan.ui.components.MultiCodeSelectionDialog
import com.HrshD1eux.Scan.ui.components.QrCodeDialog
import com.HrshD1eux.Scan.ui.history.HistoryScreen
import com.HrshD1eux.Scan.ui.result.ResultScreen
import com.HrshD1eux.Scan.ui.scanner.ScannerScreen
import com.HrshD1eux.Scan.ui.settings.AboutScreen
import com.HrshD1eux.Scan.ui.settings.SettingsScreen
import com.HrshD1eux.Scan.ui.theme.ScanTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val currentIntent = mutableStateOf<Intent?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntent.value = intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntent.value = intent

        setContent {
            val dynamicColors by viewModel.dynamicColors.collectAsState()

            ScanTheme(dynamicColor = dynamicColors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val scope = rememberCoroutineScope()
                    val currentScreen by viewModel.currentScreen.collectAsState()
                    val scannedContent by viewModel.scannedContent.collectAsState()
                    val detectedBarcodes by viewModel.detectedBarcodes.collectAsState()
                    var qrCodeDialogText by remember { mutableStateOf<String?>(null) }
                    var showCreateQrDialog by remember { mutableStateOf(false) }

                    val actionResolver = remember {
                        ActionResolver(context) {
                            scannedContent?.primaryValue?.let { qrCodeDialogText = it }
                        }
                    }

                    val galleryLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        uri?.let {
                            scope.launch {
                                val barcodes = GalleryScanner.scanImage(context, it)
                                viewModel.onScanResult(barcodes)
                            }
                        }
                    }

                    val activeIntent by currentIntent
                    LaunchedEffect(activeIntent) {
                        val intent = activeIntent ?: return@LaunchedEffect
                        when (intent.action) {
                            Intent.ACTION_SEND -> {
                                if (intent.type?.startsWith("image/") == true) {
                                    val uri = androidx.core.content.IntentCompat.getParcelableExtra(
                                        intent,
                                        Intent.EXTRA_STREAM,
                                        Uri::class.java
                                    )
                                    uri?.let {
                                        val barcodes = GalleryScanner.scanImage(context, it)
                                        viewModel.onScanResult(barcodes)
                                    }
                                }
                            }
                            "com.HrshD1eux.Scan.ACTION_SCAN_GALLERY" -> {
                                galleryLauncher.launch("image/*")
                            }
                            "com.HrshD1eux.Scan.ACTION_VIEW_HISTORY" -> {
                                viewModel.navigateTo(Screen.History)
                            }
                        }
                    }

                    when (currentScreen) {
                        Screen.Scanner -> {
                            if (scannedContent == null && detectedBarcodes.isEmpty()) {
                                ScannerScreen(
                                    onScanSuccess = viewModel::onScanResult,
                                    onSettingsClick = { viewModel.navigateTo(Screen.Settings) },
                                    onHistoryClick = { viewModel.navigateTo(Screen.History) },
                                    onShareQrClick = { showCreateQrDialog = true }
                                )
                            } else if (detectedBarcodes.isNotEmpty()) {
                                MultiCodeSelectionDialog(
                                    barcodes = detectedBarcodes,
                                    onSelect = viewModel::selectBarcodeFromMultiple,
                                    onDismiss = viewModel::dismissMultiCodeSelection
                                )
                            } else {
                                scannedContent?.let { content ->
                                    ResultScreen(
                                        content = content,
                                        actions = actionResolver.resolve(content),
                                        onScanAgain = viewModel::resetScanner
                                    )
                                }
                            }
                        }
                        Screen.Settings -> {
                            SettingsScreen(
                                settingsManager = viewModel.settingsManager,
                                onBack = { viewModel.navigateTo(Screen.Scanner) },
                                onAboutClick = { viewModel.navigateTo(Screen.About) }
                            )
                        }
                        Screen.History -> {
                            HistoryScreen(
                                onBack = { viewModel.navigateTo(Screen.Scanner) },
                                onShowQr = { text -> qrCodeDialogText = text }
                            )
                        }
                        Screen.About -> {
                            AboutScreen(
                                onBack = { viewModel.navigateTo(Screen.Settings) }
                            )
                        }
                    }

                    qrCodeDialogText?.let { text ->
                        QrCodeDialog(text = text, onDismiss = { qrCodeDialogText = null })
                    }

                    if (showCreateQrDialog) {
                        CreateQrDialog(onDismiss = { showCreateQrDialog = false })
                    }
                }
            }
        }
    }
}

