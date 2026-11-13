package com.lamontlabs.spark.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamontlabs.spark.data.ReflectionRepository
import com.lamontlabs.spark.design.SparkGlassSurface
import com.lamontlabs.spark.design.SparkPalette
import java.time.LocalDate

@Composable
fun ProgressScreen(repository: ReflectionRepository, modifier: Modifier = Modifier) {
    val flow = remember(repository) { repository.observeAll() }
    val reflections by flow.collectAsState(initial = emptyList())
    val progress = ProgressCalculator.calculate(reflections, LocalDate.now())
    LazyColumn(
        modifier = modifier.fillMaxSize().background(SparkPalette.Void),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("YOUR SPACE  /  PROGRESS", style = MaterialTheme.typography.labelMedium,
                    color = SparkPalette.Cyan, letterSpacing = 1.6.sp)
                Text("Every moment counts.", style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold, color = SparkPalette.Ice)
                Text("Your story, at your pace.", style = MaterialTheme.typography.bodyLarge,
                    color = SparkPalette.MutedBlue)
            }
        }
        item {
            SparkGlassSurface(modifier = Modifier.fillMaxWidth(), prominent = true) {
                Column(modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("MOMENTS SAVED", style = MaterialTheme.typography.labelSmall,
                        color = SparkPalette.Cyan, letterSpacing = 1.3.sp)
                    Text("${progress.totalReflections}", style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold, color = SparkPalette.Ice)
                    Text("Small moments make a meaningful collection.",
                        style = MaterialTheme.typography.bodyMedium, color = SparkPalette.MutedBlue)
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProgressMetric("${progress.activeDays}", "Active days", Modifier.weight(1f))
                ProgressMetric("${progress.currentGentleStreak}", "Gentle streak", Modifier.weight(1f))
            }
        }
        item {
            SparkGlassSurface(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("LONGEST GENTLE STREAK", style = MaterialTheme.typography.labelSmall,
                        color = SparkPalette.Cyan, letterSpacing = 1.1.sp)
                    Text("${progress.longestGentleStreak} active days",
                        style = MaterialTheme.typography.titleLarge, color = SparkPalette.Ice)
                    Text("A gentle streak allows one missed day between reflection days. Missed days never count as active days.",
                        style = MaterialTheme.typography.bodySmall, color = SparkPalette.MutedBlue)
                }
            }
        }
        item {
            Text("Your progress is not a score of your worth.",
                style = MaterialTheme.typography.bodySmall, color = SparkPalette.MutedBlue)
        }
    }
}

@Composable
private fun ProgressMetric(value: String, label: String, modifier: Modifier = Modifier) {
    SparkGlassSurface(modifier = modifier) {
        Column(modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold, color = SparkPalette.Ice)
            Text(label, style = MaterialTheme.typography.bodySmall,
                color = SparkPalette.MutedBlue)
        }
    }
}
