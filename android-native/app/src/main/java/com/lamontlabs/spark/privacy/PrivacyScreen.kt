package com.lamontlabs.spark.privacy

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import com.lamontlabs.spark.reminders.ReminderScheduler
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.lamontlabs.spark.data.ReflectionRepository
import com.lamontlabs.spark.data.SparkDatabase
import com.lamontlabs.spark.portability.ReflectionBackup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    val remindersEnabled by preferences.remindersEnabled.collectAsState(initial = false)
    val reminderHour by preferences.reminderHour.collectAsState(initial = 19)
    val scope = rememberCoroutineScope()
    val contextForReminders = LocalContext.current
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) scope.launch {
            preferences.setRemindersEnabled(true)
            ReminderScheduler.configure(contextForReminders, true, reminderHour)
        }
    }

    val context = LocalContext.current
    val backup = remember(context) { ReflectionBackup(SparkDatabase.get(context).reflectionDao()) }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            busy = true
            try {
                withContext(Dispatchers.IO) {
                    val text = backup.exportJson()
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(text) }
                        ?: error("Unable to open destination")
                }
                message = "Text reflections exported. Photos are NOT included; the file is unencrypted."
            } catch (exception: Exception) { message = "Export failed: ${exception.message}" }
            finally { busy = false }
        }
    }
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            try {
                val result = withContext(Dispatchers.IO) {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
                        input.readNBytes(5_000_001)
                    } ?: error("Unable to open backup")
                    require(bytes.size <= 5_000_000) { "Backup exceeds 5 MB" }
                    backup.importJson(bytes.toString(Charsets.UTF_8))
                }
                message = "Imported ${result.imported} reflections; skipped ${result.skippedExisting} existing entries."
            } catch (exception: Exception) { message = "Import failed: ${exception.message}" }
            finally { busy = false }
        }
    }
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
        Text("Gentle daily reminder", style = MaterialTheme.typography.titleMedium)
        Text("Optional, local and never includes your journal text. Delivery time is approximate.", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(9 to "Morning", 14 to "Afternoon", 19 to "Evening").forEach { (hour, label) ->
                FilterChip(selected = reminderHour == hour, onClick = {
                    scope.launch {
                        preferences.setReminderHour(hour)
                        if (remindersEnabled) ReminderScheduler.configure(context, true, hour)
                    }
                }, label = { Text(label) })
            }
        }
        Switch(checked = remindersEnabled, onCheckedChange = { next ->
            if (next && Build.VERSION.SDK_INT >= 33) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else scope.launch {
                preferences.setRemindersEnabled(next)
                ReminderScheduler.configure(context, next, reminderHour)
            }
        })
        Text(if (remindersEnabled) "Reminders enabled" else "Reminders off", style = MaterialTheme.typography.bodySmall)
        Text("Manual text backup (unencrypted)", style = MaterialTheme.typography.titleMedium)
        Text("Backups contain private journal text. Photos are not included. Export refuses photo-only entries. Import never overwrites matching IDs.", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { exportPicker.launch("spark-reflections.json") }, enabled = !busy) { Text("Export reflections") }
        OutlinedButton(onClick = { importPicker.launch(arrayOf("application/json", "text/plain")) }, enabled = !busy) { Text("Import reflections") }
        message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        Text("Uninstalling Spark may erase locally stored reflections. Keep backups somewhere you control.", style = MaterialTheme.typography.bodySmall)
    }
}
