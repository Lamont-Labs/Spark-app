package com.lamontlabs.spark.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.spark.data.ReflectionRepository
import com.lamontlabs.spark.design.SparkGlassSurface
import com.lamontlabs.spark.design.SparkPalette
import androidx.compose.ui.text.font.FontWeight
import com.lamontlabs.spark.prompts.PromptLibrary
import com.lamontlabs.spark.media.PrivatePhotoStore
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun ReflectionEditor(
    kind: String,
    repository: ReflectionRepository,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val photoStore = androidx.compose.runtime.remember(context) { PrivatePhotoStore(context) }
    var selectedPhoto by rememberSaveable(kind) { mutableStateOf<String?>(null) }
    var body by rememberSaveable(kind) { mutableStateOf("") }
    var mood by rememberSaveable(kind) { mutableStateOf<String?>(null) }
    var saving by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val imported = photoStore.importPhoto(uri)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        selectedPhoto?.let { photoStore.delete(it) }
                        selectedPhoto = imported
                        error = null
                    }
                } catch (exception: Exception) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        error = "Could not attach this photo. Choose a smaller image (15 MB maximum)."
                    }
                }
            }
        }
    }
    val prompt = PromptLibrary.daily(kind, LocalDate.now())
    val label = if (kind == "GRATITUDE") "Gratitude" else "Daily Win"
    Column(
        modifier = Modifier.fillMaxSize().background(SparkPalette.Void).imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("YOUR MOMENT  /  $label", style = MaterialTheme.typography.labelMedium, color = SparkPalette.Cyan)
        Text(if (kind == "GRATITUDE") "Keep the good." else "Celebrate your progress.", style = MaterialTheme.typography.headlineLarge, color = SparkPalette.Ice, fontWeight = FontWeight.SemiBold)
        Text(
            if (kind == "GRATITUDE") "What are you grateful for today?"
            else "What did you accomplish, big or small?",
            style = MaterialTheme.typography.bodyLarge
        )
        if (prompt != null) {
            SparkGlassSurface(modifier = Modifier.fillMaxWidth(), prominent = true) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("A GENTLE STARTING POINT", style = MaterialTheme.typography.labelSmall, color = SparkPalette.Cyan)
                    Text(prompt.text, style = MaterialTheme.typography.bodyLarge, color = SparkPalette.Ice)
                }
            }
        }
        OutlinedTextField(
            value = body,
            onValueChange = { body = it; error = null },
            label = { Text("Your reflection") },
            minLines = 4,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            enabled = !saving
        )
        Text("How did this moment feel? (optional)", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Calm", "Happy", "Proud", "Hopeful", "Tough").forEach { option ->
                FilterChip(
                    selected = mood == option,
                    onClick = { mood = if (mood == option) null else option },
                    label = { Text(option) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SparkPalette.Cyan, selectedLabelColor = SparkPalette.Void, labelColor = SparkPalette.MutedBlue),
                    enabled = !saving
                )
            }
        }
        OutlinedButton(onClick = { picker.launch("image/*") }, enabled = !saving) {
            Text(if (selectedPhoto == null) "Attach a photo (optional)" else "Replace attached photo")
        }
        if (selectedPhoto != null) {
            Text("✓ Photo attached privately", style = MaterialTheme.typography.bodySmall, color = SparkPalette.Cyan)
            OutlinedButton(onClick = { selectedPhoto?.let { photoStore.delete(it) }; selectedPhoto = null }, enabled = !saving) {
                Text("Remove photo")
            }
        }
        if (error != null) {
            Text(error!!, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = {
                if (body.isBlank() && selectedPhoto == null) {
                    error = "Write a reflection or attach a photo before saving."
                } else if (!saving) {
                    saving = true
                    scope.launch {
                        try {
                            repository.create(kind, body, LocalDate.now(), mood = mood, photoId = selectedPhoto, promptId = prompt?.id)
                            onSaved()
                        } catch (exception: Exception) {
                            error = "Could not save. Your text is still here; please retry."
                        } finally {
                            saving = false
                        }
                    }
                }
            },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth()
        ) { if (saving) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Save reflection") }
        OutlinedButton(
            onClick = onCancel,
            enabled = !saving,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Back") }
        Text(
            "Saved privately on this device. No cloud account required.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
