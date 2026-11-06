package com.lamontlabs.spark.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
fun FirstLightScreen(step: Int, focus: String, onFocus: (String) -> Unit, onWrite: (String) -> Unit, onNext: () -> Unit, onBack: () -> Unit, onSkip: () -> Unit) {
    val page = pages[step.coerceIn(0, pages.lastIndex)]
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("SPARK  /  FIRST LIGHT", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary, letterSpacing = 1.6.sp)
        Text("${step + 1} OF ${pages.size}", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(0.35f))
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
        Spacer(Modifier.weight(0.65f))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text(if (step == pages.lastIndex) "Enter Spark" else "Continue")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (step > 0) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
            }
            OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f)) { Text("Skip introduction") }
        }
    }
}
