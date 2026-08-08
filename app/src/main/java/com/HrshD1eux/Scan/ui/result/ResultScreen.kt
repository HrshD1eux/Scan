package com.HrshD1eux.Scan.ui.result

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
            .padding(24.dp),
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
                Text(content.rawValue, style = MaterialTheme.typography.bodyLarge)
            }
            is ParsedContent.Text -> {
                Text("Scanned Text", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(content.text, style = MaterialTheme.typography.bodyLarge)
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
