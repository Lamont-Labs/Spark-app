package com.lamontlabs.spark.affirm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/**
 * Bundled offline affirmations. No model-generated or network content.
 * This starter set is intentionally small pending the governed content library.
 */
private val affirmations = listOf(
    "I can grow at my own pace.",
    "My effort matters, even when progress feels small.",
    "I can be kind to myself while I learn.",
    "Today offers another chance to begin.",
    "I am allowed to celebrate small victories.",
    "I can ask for help when I need it.",
    "I notice the good without ignoring what is hard."
)

@Composable
fun AffirmScreen(modifier: Modifier = Modifier) {
    val day = LocalDate.now().toEpochDay()
    val featured = affirmations[Math.floorMod(day, affirmations.size.toLong()).toInt()]
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Words to carry", style = MaterialTheme.typography.headlineLarge)
            Text("A quiet reminder for today", style = MaterialTheme.typography.bodyLarge)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("TODAY'S AFFIRMATION", style = MaterialTheme.typography.labelMedium)
                    Text(featured, style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
        item { Text("Explore more", style = MaterialTheme.typography.titleLarge) }
        items(affirmations, key = { it }) { affirmation ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(affirmation, modifier = Modifier.padding(20.dp), style = MaterialTheme.typography.bodyLarge)
            }
        }
        item {
            Text(
                "These affirmations are included with Spark and work offline. Custom affirmations are coming later.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
