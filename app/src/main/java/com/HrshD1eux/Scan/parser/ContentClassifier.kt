package com.HrshD1eux.Scan.parser

import android.net.Uri
import com.google.mlkit.vision.barcode.common.Barcode

object ContentClassifier {

    fun classify(barcode: Barcode): ParsedContent {
        val rawValue = barcode.rawValue ?: return ParsedContent.Text("")
        
        if (rawValue.startsWith("otpauth://", ignoreCase = true)) {
            return parseOtp(rawValue)
        }

        val type = barcode.valueType

        return when (type) {
            Barcode.TYPE_WIFI -> {
                val wifi = barcode.wifi
                val ssid = wifi?.ssid
                if (wifi != null && ssid != null) {
                    val securityType = when (wifi.encryptionType) {
                        Barcode.WiFi.TYPE_WPA -> "WPA/WPA2"
                        Barcode.WiFi.TYPE_WEP -> "WEP"
                        Barcode.WiFi.TYPE_OPEN -> "Open"
                        else -> "Unknown"
                    }
                    ParsedContent.Wifi(ssid, wifi.password, securityType)
                } else {
                    ParsedContent.Text(rawValue)
                }
            }
            Barcode.TYPE_URL -> {
                val url = barcode.url?.url ?: rawValue
                if (url.startsWith("upi://pay", ignoreCase = true)) {
                    parseUpi(url)
                } else {
                    ParsedContent.Url(url, url.startsWith("https://", ignoreCase = true))
                }
            }
            Barcode.TYPE_PHONE -> {
                ParsedContent.Phone(barcode.phone?.number ?: rawValue)
            }
            Barcode.TYPE_EMAIL -> {
                val email = barcode.email
                ParsedContent.Email(email?.address ?: rawValue, email?.subject, email?.body)
            }
            Barcode.TYPE_CONTACT_INFO -> {
                val contact = barcode.contactInfo
                val name = contact?.name?.formattedName ?: "Contact"
                val phone = contact?.phones?.firstOrNull()?.number
                val email = contact?.emails?.firstOrNull()?.address
                val org = contact?.organization
                ParsedContent.Contact(name, phone, email, org)
            }
            Barcode.TYPE_GEO -> {
                val geo = barcode.geoPoint
                if (geo != null) {
                    ParsedContent.Geo(geo.lat, geo.lng, rawValue)
                } else {
                    ParsedContent.Text(rawValue)
                }
            }
            Barcode.TYPE_SMS -> {
                val sms = barcode.sms
                if (sms != null) {
                    ParsedContent.Sms(sms.phoneNumber ?: "", sms.message)
                } else {
                    ParsedContent.Text(rawValue)
                }
            }
            else -> {
                if (rawValue.startsWith("upi://pay", ignoreCase = true)) {
                    parseUpi(rawValue)
                } else if (barcode.format in listOf(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)) {
                    ParsedContent.Product(rawValue, barcode.format)
                } else if (barcode.format != Barcode.FORMAT_QR_CODE) {
                    ParsedContent.UnknownBarcode(rawValue, barcode.format)
                } else {
                    ParsedContent.Text(rawValue)
                }
            }
        }
    }

    private fun parseOtp(uriString: String): ParsedContent {
        return try {
            val params = extractQueryParams(uriString)
            val label = extractPathLabel(uriString)
            val secret = params["secret"]
            val issuer = params["issuer"]
            val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: Uri.EMPTY
            ParsedContent.Otp(label.trim(), secret, issuer, uri)
        } catch (_: Exception) {
            ParsedContent.Text(uriString)
        }
    }

    private fun parseUpi(uriString: String): ParsedContent {
        return try {
            val params = extractQueryParams(uriString)
            val payeeAddress = params["pa"]
            val payeeName = params["pn"]
            val amount = params["am"]
            if (payeeAddress.isNullOrBlank()) {
                ParsedContent.Text(uriString)
            } else {
                val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: Uri.EMPTY
                ParsedContent.Upi(payeeAddress, payeeName, amount, uri)
            }
        } catch (_: Exception) {
            ParsedContent.Text(uriString)
        }
    }

    private fun extractPathLabel(uriString: String): String {
        val pathWithoutQuery = uriString.substringBefore('?').substringBefore('#')
        val cleanPath = if (pathWithoutQuery.contains("://")) {
            pathWithoutQuery.substringAfter("://").substringAfter("/")
        } else {
            pathWithoutQuery
        }
        return cleanPath.substringAfterLast(":")
            .ifEmpty { cleanPath.substringAfterLast("/") }
            .ifEmpty { "Authenticator Code" }
    }

    private fun extractQueryParams(uriString: String): Map<String, String> {
        val query = uriString.substringAfter('?', "").substringBefore('#')
        if (query.isEmpty()) return emptyMap()
        return query.split('&')
            .mapNotNull { pair ->
                val idx = pair.indexOf('=')
                if (idx != -1) {
                    val key = pair.substring(0, idx).trim().lowercase()
                    val rawVal = pair.substring(idx + 1)
                    val value = try {
                        java.net.URLDecoder.decode(rawVal, "UTF-8")
                    } catch (_: Exception) {
                        rawVal
                    }
                    key to value
                } else null
            }.toMap()
    }
}
