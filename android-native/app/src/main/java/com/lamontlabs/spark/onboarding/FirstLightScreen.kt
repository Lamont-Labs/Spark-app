package com.lamontlabs.spark.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class IntroPage(val eyebrow: String, val title: String, val description: String)
private val pages = listOf(
    IntroPage("WELCOME TO SPARK", "Notice the good.", "A quiet place for gratitude, your daily wins, and the words you want to carry forward."),
    IntroPage("MAKE IT YOURS", "Your pace. Your story.", "Start with gratitude, celebrate a small win, or simply read an affirmation. There is no perfect way to begin."),
    IntroPage("YOUR FIRST SPARK", "Small moments matter.", "When you're ready, Today is where you can capture your first private reflection."),
    IntroPage("FIND YOUR RHYTHM", "Come back when it feels right.", "Gentle reminders are optional and off by default. You can choose them later in preferences."),
    IntroPage("ALL YOURS", "Your space is ready.", "Your reflections stay on this device. Take the good with you, one moment at a time.")
)

@Composable
fun FirstLightScreen(step: Int, focus: String, onFocus: (String) -> Unit, onWrite: (String) -> Unit, remindersEnabled: Boolean, reminderHour: Int, onReminderToggle: (Boolean) -> Unit, onReminderHour: (Int) -> Unit, reminderMessage: String?, onNext: () -> Unit, onBack: () -> Unit, onSkip: () -> Unit) {
    val page = pages[step.coerceIn(0, pages.lastIndex)]
    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("SPARK  /  FIRST LIGHT", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary, letterSpacing = 1.6.sp)
        Text("${step + 1} OF ${pages.size}", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (step == 4) {
            Box(modifier = Modifier.fillMaxWidth().height(132.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(122.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape))
                Box(Modifier.size(78.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape))
                Text("✦", style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Spacer(Modifier.height(36.dp))
        }
        Surface(shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.primaryContainer) {
            Column(modifier = Modifier.fillMaxWidth().padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(page.eyebrow, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary, letterSpacing = 1.3.sp)
                Text(page.title, style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold, lineHeight = 42.sp)
                Text(page.description, style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (step == 3) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Gentle daily reminder", style = MaterialTheme.typography.titleMedium)
                Switch(checked = remindersEnabled, onCheckedChange = onReminderToggle)
            }
            Text(if (remindersEnabled) "A quiet nudge, never your private journal text." else "Off by default. You can always decide later.",
                style = MaterialTheme.typography.bodyMedium)
            if (reminderMessage != null) {
                Text(reminderMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (remindersEnabled) {
                listOf(9 to "Morning", 14 to "Afternoon", 19 to "Evening").forEach { (hour, label) ->
                    FilterChip(selected = reminderHour == hour, onClick = { onReminderHour(hour) },
                        label = { Text(label) }, modifier = Modifier.fillMaxWidth())
                }
                Text("Delivery time is approximate.", style = MaterialTheme.typography.bodySmall)
            }
        }
        if (step == 2) {
            Text("CHOOSE YOUR FIRST MOMENT", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = { onWrite("GRATITUDE") }, modifier = Modifier.fillMaxWidth()) {
                Text("Write a gratitude")
            }
            OutlinedButton(onClick = { onWrite("WIN") }, modifier = Modifier.fillMaxWidth()) {
                Text("Celebrate a daily win")
            }
        }
        if (step == 1) {
            Text("WHAT WOULD YOU LIKE MORE OF?", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            listOf(
                "GRATITUDE" to "Noticing the good",
                "WINS" to "Celebrating my wins",
                "CONFIDENCE" to "Building confidence",
                "EVERYTHING" to "A little of everything"
            ).forEach { (value, label) ->
                FilterChip(
                    selected = focus == value,
                    onClick = { onFocus(value) },
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (step == 4) {
            Text("Your first moment is just the beginning.",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text(if (step == pages.lastIndex) "Open my Spark" else "Continue")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (step > 0) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
            }
            if (step < pages.lastIndex) {
                OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f)) { Text("Skip introduction") }
            }
        }
    }
}
