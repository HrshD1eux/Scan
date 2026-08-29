package com.HrshD1eux.Scan.parser

import android.net.Uri

sealed class ParsedContent {
    abstract val primaryValue: String
    abstract val displayTitle: String
    open val displaySummary: String get() = primaryValue

    data class Upi(
        val upiId: String,
        val payeeName: String?,
        val amount: String?,
        val rawUri: Uri? = null
    ) : ParsedContent() {
        override val displayTitle = "UPI Payment"
        override val primaryValue = upiId
        override val displaySummary = payeeName?.let { "$it ($upiId)" } ?: upiId
    }

    data class Url(
        val url: String,
        val isSecure: Boolean
    ) : ParsedContent() {
        override val displayTitle = "Website"
        override val primaryValue = url
        override val displaySummary = url
    }

    data class Wifi(
        val ssid: String,
        val password: String?,
        val securityType: String
    ) : ParsedContent() {
        override val displayTitle = "Wi-Fi Network"
        override val primaryValue = ssid
        override val displaySummary = "$ssid ($securityType)"
    }

    data class Phone(
        val number: String
    ) : ParsedContent() {
        override val displayTitle = "Phone Number"
        override val primaryValue = number
        override val displaySummary = number
    }

    data class Email(
        val address: String,
        val subject: String?,
        val body: String?
    ) : ParsedContent() {
        override val displayTitle = "Email"
        override val primaryValue = address
        override val displaySummary = address
    }

    data class Text(
        val text: String
    ) : ParsedContent() {
        override val displayTitle = "Scanned Text"
        override val primaryValue = text
        override val displaySummary = if (text.length > 30) text.take(30) + "..." else text
    }

    data class Product(
        val barcode: String,
        val format: Int
    ) : ParsedContent() {
        override val displayTitle = "Product Barcode"
        override val primaryValue = barcode
        override val displaySummary = barcode
    }

    data class UnknownBarcode(
        val rawValue: String,
        val format: Int
    ) : ParsedContent() {
        override val displayTitle = "Barcode"
        override val primaryValue = rawValue
        override val displaySummary = rawValue
    }

    data class Otp(
        val label: String,
        val secret: String?,
        val issuer: String?,
        val rawUri: Uri? = null
    ) : ParsedContent() {
        override val displayTitle = "Authenticator Code"
        override val primaryValue = label
        override val displaySummary = issuer?.let { "$it: $label" } ?: label
    }

    data class Contact(
        val name: String,
        val phone: String?,
        val email: String?,
        val org: String?
    ) : ParsedContent() {
        override val displayTitle = "Contact Details"
        override val primaryValue = name
        override val displaySummary = phone?.let { "$name ($it)" } ?: name
    }

    data class Geo(
        val latitude: Double,
        val longitude: Double,
        val query: String?
    ) : ParsedContent() {
        override val displayTitle = "Location Coordinates"
        override val primaryValue = "$latitude, $longitude"
        override val displaySummary = "$latitude, $longitude"
    }

    data class Sms(
        val phoneNumber: String,
        val message: String?
    ) : ParsedContent() {
        override val displayTitle = "SMS Message"
        override val primaryValue = phoneNumber
        override val displaySummary = message?.let { "$phoneNumber: ${it.take(20)}" } ?: phoneNumber
    }
}
