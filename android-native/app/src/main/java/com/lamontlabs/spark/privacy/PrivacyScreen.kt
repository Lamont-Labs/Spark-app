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
import com.lamontlabs.spark.portability.PhotoArchive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.background
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.saveable.rememberSaveable
import com.lamontlabs.spark.design.SparkPalette
import com.lamontlabs.spark.design.SparkGlassSurface
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
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
    val photoArchive = remember(context) { PhotoArchive(context, SparkDatabase.get(context).reflectionDao()) }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var showLegacyBackup by rememberSaveable { mutableStateOf(false) }
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
    val photoExportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) scope.launch {
            busy = true
            try {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { photoArchive.exportTo(it) }
                        ?: error("Unable to open destination")
                }
                message = "Photo archive exported. ZIP is unencrypted; store it privately."
            } catch (exception: Exception) { message = "Photo export failed: ${exception.message}" }
            finally { busy = false }
        }
    }
    val photoImportPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            try {
                val result = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { photoArchive.importFrom(it) }
                        ?: error("Unable to open archive")
                }
                message = "Restored ${result.imported} reflections with photos; skipped ${result.skippedExisting} existing entries."
            } catch (exception: Exception) { message = "Photo restore failed: ${exception.message}" }
            finally { busy = false }
        }
    }
    Column(
        modifier = modifier.fillMaxSize().background(SparkPalette.Void).verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("YOUR SPACE  /  PRIVACY", style = MaterialTheme.typography.labelMedium, color = SparkPalette.Cyan)
        Text("Your data. Your choices.", style = MaterialTheme.typography.headlineLarge, color = SparkPalette.Ice, fontWeight = FontWeight.SemiBold)
        Text("Your reflections are saved in Spark's private app storage on this device.", style = MaterialTheme.typography.bodyLarge)
        SparkGlassSurface(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("YOUR MOMENTS", style = MaterialTheme.typography.labelSmall,
                    color = SparkPalette.Cyan)
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
            }
        }
        SparkGlassSurface(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("GENTLE REMINDERS", style = MaterialTheme.typography.labelSmall,
                    color = SparkPalette.Cyan)
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
            }
        }
        SparkGlassSurface(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("BACKUP & RESTORE", style = MaterialTheme.typography.labelSmall,
                    color = SparkPalette.Cyan)
        Text("Complete backup · ZIP", style = MaterialTheme.typography.titleMedium, color = SparkPalette.Ice)
        Text("Includes your reflections and photos. The ZIP is not encrypted; store it somewhere private. Restoring never overwrites matching entries.", style = MaterialTheme.typography.bodySmall, color = SparkPalette.MutedBlue)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { photoExportPicker.launch("spark-complete-backup.zip") }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Export ZIP") }
            OutlinedButton(onClick = { photoImportPicker.launch(arrayOf("application/zip", "application/octet-stream")) }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Restore ZIP") }
        }
        HorizontalDivider(color = SparkPalette.Hairline.copy(alpha = 0.4f))
        OutlinedButton(onClick = { showLegacyBackup = !showLegacyBackup }, modifier = Modifier.fillMaxWidth()) {
            Text(if (showLegacyBackup) "Hide text-only backup" else "Show text-only backup (legacy)")
        }
        if (showLegacyBackup) {
            Text("Text-only backup · JSON", style = MaterialTheme.typography.titleMedium, color = SparkPalette.Ice)
            Text("Unencrypted journal text only; photos are excluded. Export refuses photo-only entries. Import never overwrites matching IDs.", style = MaterialTheme.typography.bodySmall, color = SparkPalette.MutedBlue)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { exportPicker.launch("spark-reflections.json") }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Export JSON") }
                OutlinedButton(onClick = { importPicker.launch(arrayOf("application/json", "text/plain")) }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Import JSON") }
            }
        }
            }
        }
        message?.let { Text(it, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.bodySmall) }
        Text("Uninstalling Spark may erase locally stored reflections. Keep backups somewhere you control.", style = MaterialTheme.typography.bodySmall)
    }
}
