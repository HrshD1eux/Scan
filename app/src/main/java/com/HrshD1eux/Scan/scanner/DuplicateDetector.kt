package com.HrshD1eux.Scan.scanner

class DuplicateDetector(private val debounceWindowMs: Long = 2000L) {
    private var lastScannedValue: String? = null
    private var lastScanTime: Long = 0L

    fun isDuplicate(rawValue: String): Boolean {
        val currentTime = System.currentTimeMillis()
        if (rawValue == lastScannedValue && (currentTime - lastScanTime) < debounceWindowMs) {
            return true // It's a duplicate
        }
        
        // Not a duplicate or window expired
        lastScannedValue = rawValue
        lastScanTime = currentTime
        return false
    }

    fun clear() {
        lastScannedValue = null
        lastScanTime = 0L
    }
}
