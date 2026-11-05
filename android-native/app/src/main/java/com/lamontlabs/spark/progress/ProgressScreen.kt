package com.lamontlabs.spark.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.spark.data.ReflectionRepository
import java.time.LocalDate

@Composable
fun ProgressScreen(repository: ReflectionRepository, modifier: Modifier = Modifier) {
    val flow = remember(repository) { repository.observeAll() }
    val reflections by flow.collectAsState(initial = emptyList())
    val progress = ProgressCalculator.calculate(reflections, LocalDate.now())
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Your progress", style = MaterialTheme.typography.headlineLarge) }
        item { Text("Every moment you notice counts.", style = MaterialTheme.typography.bodyLarge) }
        item { Text("${progress.totalReflections} reflections saved", style = MaterialTheme.typography.titleLarge) }
        item { Text("${progress.activeDays} days with a reflection", style = MaterialTheme.typography.titleMedium) }
        item { Text("${progress.currentGentleStreak} active days in your current gentle streak", style = MaterialTheme.typography.titleMedium) }
        item { Text("Longest gentle streak: ${progress.longestGentleStreak} active days", style = MaterialTheme.typography.bodyLarge) }
        item {
            Text(
                "A gentle streak allows one missed day between reflection days. Missed days never count as active days. Your progress is not a score of your worth.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
