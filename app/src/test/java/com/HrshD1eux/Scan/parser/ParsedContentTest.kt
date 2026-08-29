package com.HrshD1eux.Scan.parser

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class ParsedContentTest {

    @Test
    fun textSummary_truncatesLongStringsProperly() {
        val shortText = ParsedContent.Text("Short string")
        assertEquals("Short string", shortText.displaySummary)

        val longString = "A".repeat(50)
        val truncated = ParsedContent.Text(longString)
        assertEquals("A".repeat(30) + "...", truncated.displaySummary)
    }

    @Test
    fun upiSummary_includesPayeeNameWhenAvailable() {
        val mockUri = mock(Uri::class.java)
        val upiWithName = ParsedContent.Upi(
            upiId = "merchant@upi",
            payeeName = "Acme Store",
            amount = "150.00",
            rawUri = mockUri
        )
        assertEquals("Acme Store (merchant@upi)", upiWithName.displaySummary)
        assertEquals("merchant@upi", upiWithName.primaryValue)
    }

    @Test
    fun wifiSummary_displaysSsidAndSecurityType() {
        val wifi = ParsedContent.Wifi(
            ssid = "Office_5G",
            password = "SecretPassword123",
            securityType = "WPA/WPA2"
        )
        assertEquals("Office_5G (WPA/WPA2)", wifi.displaySummary)
        assertEquals("Office_5G", wifi.primaryValue)
    }

    @Test
    fun contactSummary_formatsNameAndPhoneCorrectly() {
        val contact = ParsedContent.Contact(
            name = "John Doe",
            phone = "+1234567890",
            email = "john@example.com",
            org = "Tech Corp"
        )
        assertEquals("John Doe (+1234567890)", contact.displaySummary)
        assertEquals("John Doe", contact.primaryValue)
    }

    @Test
    fun geoCoordinates_formatsLatitudeAndLongitude() {
        val geo = ParsedContent.Geo(
            latitude = 37.7749,
            longitude = -122.4194,
            query = "San Francisco"
        )
        assertEquals("37.7749, -122.4194", geo.primaryValue)
        assertEquals("37.7749, -122.4194", geo.displaySummary)
    }

    @Test
    fun smsSummary_includesPhoneAndTruncatedMessage() {
        val sms = ParsedContent.Sms(
            phoneNumber = "+919876543210",
            message = "Your verification code is 123456"
        )
        assertTrue(sms.displaySummary.startsWith("+919876543210: Your verification c"))
    }
}
