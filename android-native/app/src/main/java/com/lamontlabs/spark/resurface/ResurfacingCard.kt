package com.lamontlabs.spark.resurface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
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
fun ResurfacingCard(repository: ReflectionRepository, modifier: Modifier = Modifier) {
    val stream = remember(repository) { repository.observeAll() }
    val reflections by stream.collectAsState(initial = emptyList())
    val selected = Resurfacing.select(reflections, LocalDate.now()) ?: return
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("A moment worth remembering", style = MaterialTheme.typography.titleMedium)
            Text(selected.reflectionDate, style = MaterialTheme.typography.labelMedium)
            if (selected.body.isNotBlank()) {
                Text(selected.body, style = MaterialTheme.typography.bodyLarge)
            } else {
                Text("You saved a photo on this day.", style = MaterialTheme.typography.bodyLarge)
            }
            Text("From your private journal", style = MaterialTheme.typography.bodySmall)
        }
    }
}
