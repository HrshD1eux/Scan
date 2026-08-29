package com.HrshD1eux.Scan.scanner

import android.os.SystemClock

class DuplicateDetector(
    private val debounceWindowMs: Long = 2000L,
    private val maxCacheSize: Int = 50
) {
    private val recentScans = object : LinkedHashMap<String, Long>(maxCacheSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>?): Boolean {
            return size > maxCacheSize
        }
    }

    @Synchronized
    fun isDuplicate(rawValue: String, currentTime: Long = SystemClock.elapsedRealtime()): Boolean {
        pruneExpiredEntries(currentTime)
        val lastSeen = recentScans[rawValue]
        if (lastSeen != null && (currentTime - lastSeen) < debounceWindowMs) {
            return true
        }
        recentScans[rawValue] = currentTime
        return false
    }

    @Synchronized
    fun clear() {
        recentScans.clear()
    }

    private fun pruneExpiredEntries(currentTime: Long) {
        val iterator = recentScans.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (currentTime - entry.value >= debounceWindowMs) {
                iterator.remove()
            }
        }
    }
}
