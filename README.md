# Scan

A lightweight, privacy-focused Android QR and barcode scanner built with Jetpack Compose, CameraX, and ML Kit.

## Overview

Scan is designed for low latency and direct action execution. It starts directly into the viewfinder, detects multiple barcode formats on-device, classifies content into actionable types (UPI, URLs, Wi-Fi credentials, vCards, SMS, coordinates, OTPs), and triggers corresponding native Android system handlers.

### Key Capabilities

- **On-Device Recognition:** Powered by Google ML Kit and ZXing with local fallback parsers.
- **Action Resolution:** Direct intent dispatch for payments (UPI), network configuration (`WifiNetworkSuggestion`), contacts, navigation, and web URLs via Chrome Custom Tabs.
- **Low-Light Assistant:** Ambient light sensor integration with automatic or suggested torch controls.
- **Gallery & Share Target:** Scans images directly from the system gallery or incoming Android share sheets.
- **Local History & Export:** Room-backed SQLite database with search/notes and CSV export via Android `FileProvider`.
- **System Integration:** Quick Settings Tile and App Shortcuts for immediate scanning.

## Architecture

The project follows modern Android architecture principles:

- **UI Layer:** Jetpack Compose with Material 3 theming (supports Dynamic Color / Material You on Android 12+).
- **State Management:** `MainViewModel` utilizing Kotlin Coroutines and `StateFlow` for unidirectional state flow.
- **Vision Pipeline:** CameraX `ImageAnalysis` operating at 1080p resolution with debounced frame processing.
- **Storage:** Room Database for persistent scan records and Jetpack DataStore Preferences for user configuration.

## Project Structure

```
app/src/main/java/com/HrshD1eux/Scan/
├── MainActivity.kt        # Entry point and Compose root
├── MainViewModel.kt       # Application state and scan event handling
├── ScanTileService.kt     # Android Quick Settings tile
├── actions/               # Intent creation and system action dispatchers
├── camera/                # CameraX analyzer, preview, and sensor managers
├── history/               # Room entities, DAO, migrations, and CSV export
├── parser/                # ML Kit barcode classification and content models
├── scanner/               # Duplicate scan debounce logic
├── ui/                    # Jetpack Compose UI screens, components, and theme
├── updater/               # In-app update manager and installer
└── utils/                 # QR generation bitmap utilities
```

## Build & Testing

### Prerequisites
- Android Studio Iguana / Jellyfish or newer
- JDK 17
- Android SDK 34 (minSdk 24)

### Running Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Building APK
```bash
./gradlew assembleDebug
```

For release builds, configure signing credentials in `local.properties` or environment variables (`ORG_GRADLE_PROJECT_STORE_PASSWORD`, `ORG_GRADLE_PROJECT_KEY_ALIAS`, `ORG_GRADLE_PROJECT_KEY_PASSWORD`).

## CI/CD

Automated builds and GitHub release creation are handled by `.github/workflows/release.yml` on pushes to the `main` branch.

## License

This project is licensed under a **Custom Non-Commercial License**. 

```text
License Agreement

Copyright (c) 2026 HrshD1eux

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, and/or sublicense copies of the Software, subject to the following conditions:

1. Non-Commercial Use: You may NOT use this Software, or any modifications or derivatives of this Software, for commercial purposes. You may not sell, lease, or charge a fee for this Software or any part of it.

2. Attribution: If you modify, share, or distribute this Software in any form, you MUST provide clear and prominent credit to the original author (HrshD1eux) and include a link to the original repository.

THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```
