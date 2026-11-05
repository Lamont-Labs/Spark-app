package com.lamontlabs.spark.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun PrivacyScreen(preferences: PrivacyPreferences, modifier: Modifier = Modifier) {
    val enabled by preferences.resurfacingEnabled.collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Privacy & preferences", style = MaterialTheme.typography.headlineMedium)
        Text("Your reflections are saved in Spark's private app storage on this device.", style = MaterialTheme.typography.bodyLarge)
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Remember past moments", style = MaterialTheme.typography.titleMedium)
                Text("Show an older reflection on Today. Off by default.", style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = enabled, onCheckedChange = { next -> scope.launch { preferences.setResurfacingEnabled(next) } })
        }
        Text("No account or cloud sync is required. This setting stays on your device.", style = MaterialTheme.typography.bodySmall)
        Text("Uninstalling Spark may erase locally stored reflections. Export is not available yet.", style = MaterialTheme.typography.bodySmall)
    }
}
