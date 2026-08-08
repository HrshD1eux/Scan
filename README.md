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
- 🖼️ **Gallery Scanning:** Quickly parse codes from saved images or screenshots without launching the camera.
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
To build a signed release version locally:
- You must place your `release.jks` in the `app/` directory.
- The `build.gradle.kts` expects the following credentials by default:
  - Key Alias: `release`
  - Password: `release123`

## License

This project is open-source and free to use.
