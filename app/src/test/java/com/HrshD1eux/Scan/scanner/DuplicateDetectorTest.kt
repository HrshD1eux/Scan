package com.HrshD1eux.Scan.scanner

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DuplicateDetectorTest {

    @Test
    fun isDuplicate_firstScan_returnsFalse() {
        val detector = DuplicateDetector(debounceWindowMs = 2000L)
        assertFalse(detector.isDuplicate("https://example.com", currentTime = 1000L))
    }

    @Test
    fun isDuplicate_sameCodeWithinDebounceWindow_returnsTrue() {
        val detector = DuplicateDetector(debounceWindowMs = 2000L)
        detector.isDuplicate("https://example.com", currentTime = 1000L)
        assertTrue(detector.isDuplicate("https://example.com", currentTime = 1500L))
    }

    @Test
    fun isDuplicate_sameCodeAfterDebounceWindow_returnsFalse() {
        val detector = DuplicateDetector(debounceWindowMs = 2000L)
        detector.isDuplicate("https://example.com", currentTime = 1000L)
        assertFalse(detector.isDuplicate("https://example.com", currentTime = 3500L))
    }

    @Test
    fun isDuplicate_interleavedCodes_debouncesEachIndependently() {
        val detector = DuplicateDetector(debounceWindowMs = 2000L)
        
        // Scan Code A, then Code B
        assertFalse(detector.isDuplicate("CODE_A", currentTime = 1000L))
        assertFalse(detector.isDuplicate("CODE_B", currentTime = 1100L))

        // Interleaved repeat of Code A and Code B within 2s should be duplicate
        assertTrue(detector.isDuplicate("CODE_A", currentTime = 1200L))
        assertTrue(detector.isDuplicate("CODE_B", currentTime = 1300L))

        // After window expires for Code A, it should register again
        assertFalse(detector.isDuplicate("CODE_A", currentTime = 3100L))
    }

    @Test
    fun clear_resetsAllEntries() {
        val detector = DuplicateDetector(debounceWindowMs = 2000L)
        detector.isDuplicate("https://example.com", currentTime = 1000L)
        detector.clear()
        assertFalse(detector.isDuplicate("https://example.com", currentTime = 1200L))
    }
}
