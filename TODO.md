# Pre-Publishing Checklist & Technical Debt (TODO)

This document contains critical instructions that **MUST be completed before publishing Scan to the Google Play Store, Official F-Droid, or any mainstream app repository**.

---

## 🚨 1. Google Play Store Pre-Publishing Mandatory Task

### Issue: `REQUEST_INSTALL_PACKAGES` & In-App APK Downloader
* **Severity:** **Critical / Immediate Rejection & Policy Strike**
* **Google Play Policy Violated:**
  * **Device and Network Abuse Policy / Restricted Permissions:** Google Play strictly restricts `REQUEST_INSTALL_PACKAGES`. Only dedicated app stores or enterprise device management applications are allowed to request it. An open-source scanner requesting package installation permissions will be immediately rejected or flagged for policy violation.
  * **Google Play DDA (Section 4.5):** Prohibits apps distributed via Google Play from downloading, altering, or updating themselves outside of Google Play's official update pipeline.
  * **Play Protect / Antivirus Heuristics:** Self-downloading executable `.apk` files over the network without cryptographic signature verification can trigger "Dropper" / "Trojan-Downloader" heuristic flags.

### Exact Actions Required Before Publishing to Google Play:

1. **In [`app/src/main/AndroidManifest.xml`](app/src/main/AndroidManifest.xml):**
   * **DELETE** line:
     ```xml
     <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
     ```

2. **In [`app/src/main/java/com/HrshD1eux/Scan/updater/AppUpdateManager.kt`](app/src/main/java/com/HrshD1eux/Scan/updater/AppUpdateManager.kt):**
   * Remove or disable `downloadAndPrepareInstall(...)`.
   * For Google Play builds, replace the update intent with an external browser or Play Store link:
     ```kotlin
     fun openStoreOrReleasePage(context: Context) {
         val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")).apply {
             addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
         }
         try {
             context.startActivity(intent)
         } catch (_: Exception) {
             context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/HrshD1eux/Scan/releases/latest")))
         }
     }
     ```

3. **In [`app/src/main/java/com/HrshD1eux/Scan/ui/settings/AboutScreen.kt`](app/src/main/java/com/HrshD1eux/Scan/ui/settings/AboutScreen.kt):**
   * When an update is available, change the button text to **"View Update on GitHub"** or **"Update on Google Play"**, opening the external URL rather than downloading the binary internally.

---

## 🛡️ 2. Official F-Droid Pre-Submission Task

* **Anti-Feature Flag:** F-Droid manages its own updates and security signatures. Including an in-app binary updater will cause F-Droid to flag the app with the `UpstreamNonFree` or `NonFreeNet` anti-feature, or request its removal.
* **Action:** Disable the download button for F-Droid releases, or separate the build into flavors.

---

## 💡 Recommended Architecture: Product Flavors (Optional Future Refactor)

To keep the self-updater for direct GitHub APK releases while automatically complying with Google Play and F-Droid, consider setting up Gradle product flavors in `app/build.gradle.kts`:

```kotlin
flavorDimensions += "distribution"
productFlavors {
    create("standalone") {
        // Includes AppUpdateManager and REQUEST_INSTALL_PACKAGES for direct GitHub APK releases
        dimension = "distribution"
    }
    create("playstore") {
        // Excludes REQUEST_INSTALL_PACKAGES and delegates updates to Google Play
        dimension = "distribution"
    }
    create("fdroid") {
        // Pure FOSS, no binary self-updater, F-Droid manages updates
        dimension = "distribution"
    }
}
```

---

## 📋 3. Play Console Submission Checklist

- [ ] **Privacy Policy URL:** Enter `https://github.com/HrshD1eux/Scan/blob/main/PRIVACY.md` or `https://hrshd1eux.github.io/Scan/`.
- [ ] **App Access:** All functionality is available without credentials (no login).
- [ ] **Data Safety Form:**
  - Data collected: **No** (0 data collected).
  - Data shared with third parties: **No**.
  - Ephemeral processing: Camera feed is processed purely on-device in volatile RAM and not retained.
- [ ] **Target Audience & Content:** Rating questionnaire (Utility / Tool category).
- [ ] **Camera Permission Declaration:** Declare that the camera is used strictly as a core feature for real-time barcode scanning.
