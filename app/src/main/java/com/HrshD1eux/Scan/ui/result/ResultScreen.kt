package com.HrshD1eux.Scan.ui.result

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateContentSize
import com.HrshD1eux.Scan.actions.Action
import com.HrshD1eux.Scan.parser.ParsedContent

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

        // Contextual Display based on content type
        when (content) {
            is ParsedContent.Upi -> {
                Text("UPI Payment", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(content.upiId, style = MaterialTheme.typography.bodyLarge)
                content.payeeName?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                content.amount?.let { Text("₹$it", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold) }
            }
            is ParsedContent.Url -> {
                Text("Website", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
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
                Text("Wi-Fi Network", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(content.ssid, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(content.securityType, style = MaterialTheme.typography.bodyMedium)
            }
            is ParsedContent.Phone -> {
                Text("Phone Number", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(content.number, style = MaterialTheme.typography.bodyLarge)
            }
            is ParsedContent.Email -> {
                Text("Email", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(content.address, style = MaterialTheme.typography.bodyLarge)
            }
            is ParsedContent.Product -> {
                Text("Product Barcode", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(content.barcode, style = MaterialTheme.typography.bodyLarge)
            }
            is ParsedContent.UnknownBarcode -> {
                Text("Barcode", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                ExpandableText(content.rawValue)
            }
            is ParsedContent.Text -> {
                Text("Scanned Text", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                ExpandableText(content.text)
            }
            is ParsedContent.Otp -> {
                Text("Authenticator Code", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(content.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                content.issuer?.let { Text("Issuer: $it", style = MaterialTheme.typography.bodyMedium) }
            }
            is ParsedContent.Contact -> {
                Text("Contact Details", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(content.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                content.org?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                content.phone?.let { Text("Phone: $it", style = MaterialTheme.typography.bodyMedium) }
                content.email?.let { Text("Email: $it", style = MaterialTheme.typography.bodyMedium) }
            }
            is ParsedContent.Geo -> {
                Text("Location Coordinates", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text("${content.latitude}, ${content.longitude}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
            is ParsedContent.Sms -> {
                Text("SMS Message", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text("To: ${content.phoneNumber}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                content.message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Display Actions
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

        Spacer(modifier = Modifier.height(32.dp))

        TextButton(onClick = onScanAgain) {
            Text("Scan Again")
        }
    }
}

@Composable
fun ExpandableText(text: String) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.animateContentSize()) {
        if (expanded) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
            TextButton(onClick = { expanded = false }, modifier = Modifier.align(Alignment.End)) {
                Text("Show Less")
            }
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            if (text.lines().size > 4 || text.length > 150) {
                TextButton(onClick = { expanded = true }, modifier = Modifier.align(Alignment.End)) {
                    Text("Show More")
                }
            }
        }
    }
}
