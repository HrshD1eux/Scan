package com.HrshD1eux.Scan.ui.result

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.HrshD1eux.Scan.actions.Action
import com.HrshD1eux.Scan.parser.ParsedContent
import com.HrshD1eux.Scan.ui.components.ExpandableText

@Composable
fun ResultScreen(
    content: ParsedContent,
    actions: List<Action>,
    onScanAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Scan Result", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        ParsedContentView(content = content)

        Spacer(modifier = Modifier.height(32.dp))

        actions.forEach { action ->
            if (action.isPrimary) {
                Button(
                    onClick = action.execute,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    action.icon?.let { Icon(it, contentDescription = null, modifier = Modifier.padding(end = 8.dp)) }
                    Text(action.label)
                }
            } else {
                OutlinedButton(
                    onClick = action.execute,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    action.icon?.let { Icon(it, contentDescription = null, modifier = Modifier.padding(end = 8.dp)) }
                    Text(action.label)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onScanAgain) {
            Text("Scan Again")
        }
    }
}

@Composable
private fun ParsedContentView(content: ParsedContent) {
    when (content) {
        is ParsedContent.Upi -> {
            Text(content.displayTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(content.upiId, style = MaterialTheme.typography.bodyLarge)
            content.payeeName?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            content.amount?.let { Text("₹$it", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold) }
        }
        is ParsedContent.Url -> {
            Text(content.displayTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(content.url, style = MaterialTheme.typography.bodyLarge)
            if (!content.isSecure) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Warning: Insecure Link (HTTP)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        is ParsedContent.Wifi -> {
            Text(content.displayTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(content.ssid, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(content.securityType, style = MaterialTheme.typography.bodyMedium)
        }
        is ParsedContent.Phone -> {
            Text(content.displayTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(content.number, style = MaterialTheme.typography.bodyLarge)
        }
        is ParsedContent.Email -> {
            Text(content.displayTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(content.address, style = MaterialTheme.typography.bodyLarge)
        }
        is ParsedContent.Product -> {
            Text(content.displayTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(content.barcode, style = MaterialTheme.typography.bodyLarge)
        }
        is ParsedContent.UnknownBarcode -> {
            Text(content.displayTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            ExpandableText(content.rawValue)
        }
        is ParsedContent.Text -> {
            Text(content.displayTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            ExpandableText(content.text)
        }
        is ParsedContent.Otp -> {
            Text(content.displayTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(content.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            content.issuer?.let { Text("Issuer: $it", style = MaterialTheme.typography.bodyMedium) }
        }
        is ParsedContent.Contact -> {
            Text(content.displayTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(content.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            content.org?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            content.phone?.let { Text("Phone: $it", style = MaterialTheme.typography.bodyMedium) }
            content.email?.let { Text("Email: $it", style = MaterialTheme.typography.bodyMedium) }
        }
        is ParsedContent.Geo -> {
            Text(content.displayTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text("${content.latitude}, ${content.longitude}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        is ParsedContent.Sms -> {
            Text(content.displayTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text("To: ${content.phoneNumber}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            content.message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
