package com.HrshD1eux.Scan.parser

import android.net.Uri

sealed class ParsedContent {
    data class Upi(val upiId: String, val payeeName: String?, val amount: String?, val rawUri: Uri) : ParsedContent()
    data class Url(val url: String, val isSecure: Boolean) : ParsedContent()
    data class Wifi(val ssid: String, val password: String?, val securityType: String) : ParsedContent()
    data class Phone(val number: String) : ParsedContent()
    data class Email(val address: String, val subject: String?, val body: String?) : ParsedContent()
    data class Text(val text: String) : ParsedContent()
    data class Product(val barcode: String, val format: Int) : ParsedContent()
    data class UnknownBarcode(val rawValue: String, val format: Int) : ParsedContent()
    data class Otp(val label: String, val secret: String?, val issuer: String?, val rawUri: Uri) : ParsedContent()
    data class Contact(val name: String, val phone: String?, val email: String?, val org: String?) : ParsedContent()
    data class Geo(val latitude: Double, val longitude: Double, val query: String?) : ParsedContent()
    data class Sms(val phoneNumber: String, val message: String?) : ParsedContent()
}
