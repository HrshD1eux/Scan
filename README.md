# Scan

<div align="center">
  <h3>An instant, uncompromising Android QR & Barcode scanner.</h3>
</div>

## Philosophy

**Open instantly. Scan anything. Understand what it is. Give the user the correct action immediately.**

Most barcode scanners are bloated with ads, telemetry, unnecessary onboarding screens, and premium subscriptions. **Scan** is built differently. It strips away all friction to provide the fastest possible scan-to-action pipeline. 

- **Zero Onboarding:** Opens directly to the camera viewfinder.
- **Privacy-First:** Absolutely no telemetry, trackers, or unnecessary permissions required for scanning.
- **Action-Oriented:** Instantly parses Wi-Fi networks, vCards, UPI links, and URLs, offering the native Android action immediately.

## Features

- ⚡ **Instant Boot:** Optimized initialization using CameraX and ML Kit.
- 🎯 **Multi-Code Selection:** When multiple codes are in frame, the app freezes the feed and lets you tap the exact one you want.
- 🔦 **Smart Flashlight:** Uses the device's ambient light sensor to automatically enable the flashlight in low-light environments.
- 🖼️ **Gallery Scanning & Share Target:** Quickly parse codes from saved images, or share an image from WhatsApp/Twitter directly to the Scan app.
- 🌐 **In-App Browser:** Web links open instantly in a hyper-fast Chrome Custom Tab without throwing you out of the app.
- 🎛️ **Quick Settings Tile:** Access the scanner instantly from your Android notification shade.
- 🤏 **Camera Gestures:** Pinch-to-zoom and tap-to-focus built directly into the viewfinder.
- 📋 **Auto-Copy:** Raw text and unknown barcodes are instantly copied to your clipboard on scan.
- 🗄️ **Local History:** Optional Room-backed database to keep track of previous scans, processed entirely offline.
- 📳 **Sensory Feedback:** Haptic and audio feedback upon successful scan, fully configurable.

## Tech Stack & Architecture

This project is a showcase of modern Android development standards:

- **100% Kotlin**
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** Unidirectional Data Flow / MVI-lite
- **Camera:** CameraX (optimized for low-latency preview and image analysis)
- **Vision:** Google ML Kit (on-device processing)
- **Local Storage:** Room Database (History) & Jetpack DataStore (Preferences)
- **Build System:** Gradle Kotlin DSL (`build.gradle.kts`)

## CI/CD Pipeline

This repository is equipped with a fully automated GitHub Actions pipeline.
- On every push to `main`, the CI workflow decodes a securely injected keystore.
- Builds a signed `release` APK.
- Automatically creates a new GitHub Release with the APK attached as a downloadable asset.
- The `versionCode` and `versionName` are automatically managed and injected during the build.

## Building Locally

1. Clone the repository:
   ```bash
   git clone git@github.com:HrshD1eux/Scan.git
   ```
2. Open the project in **Android Studio**.
3. Build the project to resolve dependencies.
4. Run on a physical device (CameraX performance is best tested on physical hardware).

### Signing the Release
To build a signed release version locally, you must configure your own signing key via `local.properties` or environment variables as defined in `build.gradle.kts`.

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
