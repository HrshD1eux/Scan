package com.HrshD1eux.Scan.parser

import com.google.mlkit.vision.barcode.common.Barcode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class ContentClassifierTest {

    @Test
    fun classify_plainUrlBarcode_returnsUrlContent() {
        val barcode = mock(Barcode::class.java)
        `when`(barcode.rawValue).thenReturn("https://github.com/HrshD1eux/Scan")
        `when`(barcode.valueType).thenReturn(Barcode.TYPE_URL)
        val urlBookmark = mock(Barcode.UrlBookmark::class.java)
        `when`(urlBookmark.url).thenReturn("https://github.com/HrshD1eux/Scan")
        `when`(barcode.url).thenReturn(urlBookmark)

        val result = ContentClassifier.classify(barcode)
        assertTrue(result is ParsedContent.Url)
        val urlContent = result as ParsedContent.Url
        assertEquals("https://github.com/HrshD1eux/Scan", urlContent.url)
        assertTrue(urlContent.isSecure)
    }

    @Test
    fun classify_insecureHttpUrl_identifiesInsecure() {
        val barcode = mock(Barcode::class.java)
        `when`(barcode.rawValue).thenReturn("http://example.com")
        `when`(barcode.valueType).thenReturn(Barcode.TYPE_URL)
        val urlBookmark = mock(Barcode.UrlBookmark::class.java)
        `when`(urlBookmark.url).thenReturn("http://example.com")
        `when`(barcode.url).thenReturn(urlBookmark)

        val result = ContentClassifier.classify(barcode)
        assertTrue(result is ParsedContent.Url)
        val urlContent = result as ParsedContent.Url
        assertEquals(false, urlContent.isSecure)
    }

    @Test
    fun classify_upiPaymentUrl_returnsUpiContent() {
        val barcode = mock(Barcode::class.java)
        val upiUri = "upi://pay?pa=merchant@upi&pn=Store&am=250.00"
        `when`(barcode.rawValue).thenReturn(upiUri)
        `when`(barcode.valueType).thenReturn(Barcode.TYPE_URL)
        val urlBookmark = mock(Barcode.UrlBookmark::class.java)
        `when`(urlBookmark.url).thenReturn(upiUri)
        `when`(barcode.url).thenReturn(urlBookmark)

        val result = ContentClassifier.classify(barcode)
        assertTrue(result is ParsedContent.Upi)
        val upi = result as ParsedContent.Upi
        assertEquals("merchant@upi", upi.upiId)
        assertEquals("Store", upi.payeeName)
        assertEquals("250.00", upi.amount)
    }

    @Test
    fun classify_wifiBarcode_returnsWifiContent() {
        val barcode = mock(Barcode::class.java)
        `when`(barcode.rawValue).thenReturn("WIFI:S:MyNetwork;T:WPA;P:Pass123;;")
        `when`(barcode.valueType).thenReturn(Barcode.TYPE_WIFI)
        val wifiMock = mock(Barcode.WiFi::class.java)
        `when`(wifiMock.ssid).thenReturn("MyNetwork")
        `when`(wifiMock.password).thenReturn("Pass123")
        `when`(wifiMock.encryptionType).thenReturn(Barcode.WiFi.TYPE_WPA)
        `when`(barcode.wifi).thenReturn(wifiMock)

        val result = ContentClassifier.classify(barcode)
        assertTrue(result is ParsedContent.Wifi)
        val wifi = result as ParsedContent.Wifi
        assertEquals("MyNetwork", wifi.ssid)
        assertEquals("Pass123", wifi.password)
        assertEquals("WPA/WPA2", wifi.securityType)
    }

    @Test
    fun classify_phoneBarcode_returnsPhoneContent() {
        val barcode = mock(Barcode::class.java)
        `when`(barcode.rawValue).thenReturn("tel:+1234567890")
        `when`(barcode.valueType).thenReturn(Barcode.TYPE_PHONE)
        val phone = mock(Barcode.Phone::class.java)
        `when`(phone.number).thenReturn("+1234567890")
        `when`(barcode.phone).thenReturn(phone)

        val result = ContentClassifier.classify(barcode)
        assertTrue(result is ParsedContent.Phone)
        assertEquals("+1234567890", (result as ParsedContent.Phone).number)
    }

    @Test
    fun classify_productEan13Barcode_returnsProductContent() {
        val barcode = mock(Barcode::class.java)
        `when`(barcode.rawValue).thenReturn("8901030384729")
        `when`(barcode.valueType).thenReturn(Barcode.TYPE_TEXT)
        `when`(barcode.format).thenReturn(Barcode.FORMAT_EAN_13)

        val result = ContentClassifier.classify(barcode)
        assertTrue(result is ParsedContent.Product)
        assertEquals("8901030384729", (result as ParsedContent.Product).barcode)
    }

    @Test
    fun classify_emptyOrNullRawValue_returnsEmptyText() {
        val barcode = mock(Barcode::class.java)
        `when`(barcode.rawValue).thenReturn(null)

        val result = ContentClassifier.classify(barcode)
        assertTrue(result is ParsedContent.Text)
        assertEquals("", (result as ParsedContent.Text).text)
    }
}
