# Privacy Policy for Scan

**Last Updated:** October 2026

**Scan** is a free and open-source, on-device QR and barcode utility developed by **HrshD1eux** ("we", "us", or "our").

This Privacy Policy explains how **Scan** handles information and reaffirms our commitment to your privacy.

---

## 1. Core Principle: 100% On-Device & Zero Telemetry

* **No Data Collection:** We do not collect, harvest, monetize, sell, or rent any personal data, scanning activity, location data, or device identifiers.
* **No Trackers or Analytics:** Scan contains **zero** analytics SDKs (e.g., Google Firebase, Flurry), **zero** advertising SDKs, and **zero** background telemetry loggers.
* **No User Accounts:** There is no registration, login, or cloud sync requirement.

---

## 2. Permissions & Hardware Usage

Scan requests only the minimal set of Android system permissions strictly necessary to deliver its core utility:

### Camera (`android.permission.CAMERA`)
* **Purpose:** Required exclusively to display the live viewfinder and detect barcode and QR code symbologies.
* **Processing:** Camera frames are streamed into transient, volatile memory (RAM) and processed in real time by the local vision pipeline.
* **Zero Persistence:** Camera frames are immediately discarded upon evaluation. Scan **never** records video, captures hidden snapshots, or transmits imagery to any remote server or third party.

### Internet (`android.permission.INTERNET`, `ACCESS_NETWORK_STATE`)
* **Purpose:** The app uses internet capabilities solely for user-directed interactions:
  1. **Manual In-App Update Checks:** If you explicitly choose "Check for Updates" in Settings, the app connects directly to the public GitHub Releases API (`api.github.com/repos/HrshD1eux/Scan/releases/latest`) to compare version numbers and fetch updated APK assets.
  2. **External Link Launching:** Opening scanned web URLs in your default web browser or Chrome Custom Tabs.
* **Zero Telemetry:** No user metrics, search queries, or diagnostic telemetry are transmitted over this connection.

### Wi-Fi Configuration (`ACCESS_WIFI_STATE`, `CHANGE_WIFI_STATE`)
* **Purpose:** Allows the app to assist you in connecting to Wi-Fi networks encoded in scanned Wi-Fi QR codes using the Android system `WifiNetworkSuggestion` API. No network credentials or network scan results are ever stored or transmitted outside your local device.

### Storage & Package Installation (`REQUEST_INSTALL_PACKAGES`)
* **Purpose:** Used only if you manually choose to download and install an in-app update from the project's official GitHub releases. The downloaded update file is stored temporarily in private app cache storage.

---

## 3. Local Data Storage & User Control

* **Scan History:** If "Save Scan History" is enabled, scanned values, timestamps, and optional user notes are stored strictly inside a private, sandboxed SQLite database on your device via the Room library.
* **Full Data Ownership & Deletion:**
  * You can view, search, edit, or delete individual history entries at any time.
  * You can export your history to a plain CSV file using Android's native share sheet.
  * You can completely erase all stored history at any time by clearing the history in Settings or uninstalling the application.

---

## 4. Third-Party Services & Intent Routing

When you scan a barcode containing actionable data (such as a UPI payment link, a geographic coordinate, a phone number, an SMS, or a vCard contact), Scan passes that data directly to the native Android Intent system. 

Your chosen third-party apps (e.g., your UPI payment app, dialer, or mapping app) handle the action according to their own independent privacy policies. Scan does not monitor, intercept, or record any payment transaction or external communication.

---

## 5. Regulatory Compliance

### General Data Protection Regulation (GDPR) — EU
Scan processes personal data exclusively on the user's physical terminal under Article 6 of the GDPR. Because no personal data is transferred, retained, or processed by us on external servers, the user maintains immediate, sovereign control over their data at all times.

### California Consumer Privacy Act (CCPA / CPRA) — United States
We do not sell, share, or disclose any personal information to third parties.

### Digital Personal Data Protection Act (DPDPA) — India
All processing of digital representations (such as UPI virtual payment addresses or contacts) occurs purely on the client side without automated centralized collection.

---

## 6. Open Source Verification

The complete source code of Scan is publicly auditable under the **GNU General Public License v3.0 (GPL-3.0)**:
👉 [https://github.com/HrshD1eux/Scan](https://github.com/HrshD1eux/Scan)

---

## 7. Contact & Inquiries

If you have questions, feedback, or concerns regarding this Privacy Policy, please open an issue on the official GitHub issue tracker:
👉 [https://github.com/HrshD1eux/Scan/issues](https://github.com/HrshD1eux/Scan/issues)
