package com.HrshD1eux.Scan.actions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.net.Uri
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Message
import com.HrshD1eux.Scan.parser.ParsedContent

class ActionResolver(private val context: Context) {

    fun resolve(content: ParsedContent): List<Action> {
        val actions = mutableListOf<Action>()

        when (content) {
            is ParsedContent.Upi -> {
                actions.add(
                    Action("Pay with UPI", Icons.Default.Payment, isPrimary = true) {
                        launchIntent(Intent(Intent.ACTION_VIEW, content.rawUri))
                    }
                )
                actions.add(createCopyAction(content.upiId))
            }
            is ParsedContent.Url -> {
                actions.add(
                    Action("Open Link", Icons.Default.Language, isPrimary = true) {
                        val uri = Uri.parse(content.url)
                        try {
                            androidx.browser.customtabs.CustomTabsIntent.Builder().build().launchUrl(context, uri)
                        } catch (e: Exception) {
                            launchIntent(Intent(Intent.ACTION_VIEW, uri))
                        }
                    }
                )
                actions.add(createCopyAction(content.url))
                actions.add(createShareAction(content.url))
            }
            is ParsedContent.Wifi -> {
                actions.add(
                    Action("Connect to Wi-Fi", Icons.Default.Wifi, isPrimary = true) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            try {
                                val suggestionBuilder = android.net.wifi.WifiNetworkSuggestion.Builder()
                                    .setSsid(content.ssid)
                                
                                if (content.password != null) {
                                    if (content.securityType.contains("WPA", ignoreCase = true)) {
                                        suggestionBuilder.setWpa2Passphrase(content.password)
                                    } else if (content.securityType.contains("WPA3", ignoreCase = true)) {
                                        suggestionBuilder.setWpa3Passphrase(content.password)
                                    }
                                }
                                
                                val suggestion = suggestionBuilder.build()
                                val list = arrayListOf(suggestion)
                                val bundle = android.os.Bundle().apply {
                                    putParcelableArrayList("android.provider.extra.WIFI_NETWORK_LIST", list)
                                }
                                val intent = Intent("android.settings.WIFI_ADD_NETWORKS").apply {
                                    putExtras(bundle)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                launchIntent(Intent(Settings.ACTION_WIFI_SETTINGS))
                            }
                        } else {
                            launchIntent(Intent(Settings.ACTION_WIFI_SETTINGS))
                        }
                    }
                )
                content.password?.let {
                    actions.add(createCopyAction(it, "Copy Password"))
                }
            }
            is ParsedContent.Phone -> {
                actions.add(
                    Action("Call", Icons.Default.Phone, isPrimary = true) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${content.number}"))
                        launchIntent(intent)
                    }
                )
                actions.add(createCopyAction(content.number))
            }
            is ParsedContent.Email -> {
                actions.add(
                    Action("Send Email", Icons.Default.Language, isPrimary = true) {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${content.address}"))
                        content.subject?.let { intent.putExtra(Intent.EXTRA_SUBJECT, it) }
                        content.body?.let { intent.putExtra(Intent.EXTRA_TEXT, it) }
                        launchIntent(intent)
                    }
                )
                actions.add(createCopyAction(content.address))
            }
            is ParsedContent.Product -> {
                actions.add(
                    Action("Search Web", Icons.Default.Search, isPrimary = true) {
                        val uri = Uri.parse("https://www.google.com/search?q=${content.barcode}")
                        try {
                            androidx.browser.customtabs.CustomTabsIntent.Builder().build().launchUrl(context, uri)
                        } catch (e: Exception) {
                            launchIntent(Intent(Intent.ACTION_VIEW, uri))
                        }
                    }
                )
                actions.add(createCopyAction(content.barcode))
            }
            is ParsedContent.UnknownBarcode -> {
                actions.add(createCopyAction(content.rawValue, "Copy Barcode", isPrimary = true))
                actions.add(createShareAction(content.rawValue))
            }
            is ParsedContent.Text -> {
                actions.add(createCopyAction(content.text, "Copy Text", isPrimary = true))
                actions.add(createShareAction(content.text))
            }
            is ParsedContent.Otp -> {
                actions.add(
                    Action("Add to Authenticator", Icons.Default.VpnKey, isPrimary = true) {
                        launchIntent(Intent(Intent.ACTION_VIEW, content.rawUri))
                    }
                )
                content.secret?.let {
                    actions.add(createCopyAction(it, "Copy Secret Key"))
                }
            }
            is ParsedContent.Contact -> {
                actions.add(
                    Action("Add to Contacts", Icons.Default.Contacts, isPrimary = true) {
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            type = android.provider.ContactsContract.Contacts.CONTENT_TYPE
                            putExtra(android.provider.ContactsContract.Intents.Insert.NAME, content.name)
                            putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, content.phone)
                            putExtra(android.provider.ContactsContract.Intents.Insert.EMAIL, content.email)
                            putExtra(android.provider.ContactsContract.Intents.Insert.COMPANY, content.org)
                        }
                        launchIntent(intent)
                    }
                )
                content.phone?.let { actions.add(createCopyAction(it, "Copy Phone")) }
                content.email?.let { actions.add(createCopyAction(it, "Copy Email")) }
            }
            is ParsedContent.Geo -> {
                actions.add(
                    Action("Open in Maps", Icons.Default.Place, isPrimary = true) {
                        val uri = Uri.parse("geo:${content.latitude},${content.longitude}?q=${content.latitude},${content.longitude}")
                        launchIntent(Intent(Intent.ACTION_VIEW, uri))
                    }
                )
                actions.add(createCopyAction("${content.latitude},${content.longitude}", "Copy Coordinates"))
            }
            is ParsedContent.Sms -> {
                actions.add(
                    Action("Send SMS", Icons.Default.Message, isPrimary = true) {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${content.phoneNumber}")).apply {
                            putExtra("sms_body", content.message)
                        }
                        launchIntent(intent)
                    }
                )
                content.message?.let { actions.add(createCopyAction(it, "Copy Message")) }
            }
        }
        return actions
    }

    private fun createCopyAction(text: String, label: String = "Copy", isPrimary: Boolean = false): Action {
        return Action(label, Icons.Default.ContentCopy, isPrimary) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", text)
            clipboard.setPrimaryClip(clip)
        }
    }

    private fun createShareAction(text: String, label: String = "Share", isPrimary: Boolean = false): Action {
        return Action(label, Icons.Default.Share, isPrimary) {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            launchIntent(shareIntent)
        }
    }

    private fun launchIntent(intent: Intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "No app found to handle this action", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
