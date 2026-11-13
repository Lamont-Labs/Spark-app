package com.lamontlabs.spark.affirm

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.CardDefaults
import com.lamontlabs.spark.design.SparkPalette
import com.lamontlabs.spark.design.SparkGlassSurface
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
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
    var libraryExpanded by rememberSaveable { mutableStateOf(false) }
    LazyColumn(
        modifier = modifier.fillMaxSize().background(SparkPalette.Void),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("SPARK  /  AFFIRM", style = MaterialTheme.typography.labelMedium, color = SparkPalette.Cyan)
            Text("Words to carry", style = MaterialTheme.typography.headlineLarge, color = SparkPalette.Ice)
            Text("A quiet reminder for today", style = MaterialTheme.typography.bodyLarge, color = SparkPalette.MutedBlue)
        }
        item {
            SparkGlassSurface(modifier = Modifier.fillMaxWidth(), prominent = true) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("TODAY'S AFFIRMATION", style = MaterialTheme.typography.labelMedium, color = SparkPalette.Cyan)
                    Text(featured, style = MaterialTheme.typography.headlineSmall, color = SparkPalette.Ice)
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Your affirmation library", style = MaterialTheme.typography.titleLarge,
                        color = SparkPalette.Ice)
                    Text("${affirmations.size} offline reminders", style = MaterialTheme.typography.bodySmall,
                        color = SparkPalette.MutedBlue)
                }
                TextButton(onClick = { libraryExpanded = !libraryExpanded }) {
                    Text(if (libraryExpanded) "Show less" else "Explore")
                    Icon(
                        if (libraryExpanded) Icons.AutoMirrored.Filled.KeyboardArrowUp
                        else Icons.AutoMirrored.Filled.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }
        }
        if (libraryExpanded) {
            items(affirmations, key = { it }) { affirmation ->
                SparkGlassSurface(modifier = Modifier.fillMaxWidth(), accent = SparkPalette.Electric) {
                    Text(affirmation, modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyLarge, color = SparkPalette.Ice)
                }
            }
        }
        item {
            Text(
                "These affirmations are included with Spark and work offline. Custom affirmations are coming later.",
                style = MaterialTheme.typography.bodySmall, color = SparkPalette.MutedBlue
            )
        }
    }
}
