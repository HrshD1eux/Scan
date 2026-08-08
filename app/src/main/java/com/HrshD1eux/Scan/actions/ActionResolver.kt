package com.HrshD1eux.Scan.actions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
                        // Wi-Fi connection logic using Android APIs goes here.
                        // For API >= 29, use WifiNetworkSpecifier.
                        // Opening settings as fallback for now
                        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                        launchIntent(intent)
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
            // Handle activity not found
        }
    }
}
