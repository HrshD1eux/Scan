package com.HrshD1eux.Scan.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ExpandableText(text: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.animateContentSize()) {
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
