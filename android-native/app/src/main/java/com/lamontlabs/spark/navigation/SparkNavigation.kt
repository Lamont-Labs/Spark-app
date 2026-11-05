package com.lamontlabs.spark.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.spark.data.ReflectionRepository
import com.lamontlabs.spark.editor.ReflectionEditor
import com.lamontlabs.spark.journal.JournalScreen
import com.lamontlabs.spark.affirm.AffirmScreen
import com.lamontlabs.spark.progress.ProgressScreen
import com.lamontlabs.spark.resurface.ResurfacingCard
import com.lamontlabs.spark.design.SparkMoment
import com.lamontlabs.spark.design.SparkMomentCard

private enum class Destination(val label: String) {
    TODAY("Today"), JOURNAL("Journal"), AFFIRM("Affirm"), YOU("You")
}

@Composable
fun SparkNavigation(repository: ReflectionRepository) {
    var editingKind by rememberSaveable { mutableStateOf<String?>(null) }
    var selected by rememberSaveable { mutableStateOf(Destination.TODAY.name) }
    val destination = Destination.entries.firstOrNull { it.name == selected } ?: Destination.TODAY
    if (editingKind != null) {
        ReflectionEditor(kind = editingKind!!, repository = repository, onSaved = { editingKind = null }, onCancel = { editingKind = null })
        return
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Destination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { selected = item.name },
                        icon = { Text(item.label.take(1), style = MaterialTheme.typography.labelLarge) },
                        label = { Text(item.label) },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { padding ->
        when (destination) {
            Destination.TODAY -> TodayDestination(repository, Modifier.padding(padding), onEdit = { editingKind = it })
            Destination.JOURNAL -> JournalScreen(repository, Modifier.padding(padding))
            Destination.AFFIRM -> AffirmScreen(Modifier.padding(padding))
            Destination.YOU -> ProgressScreen(repository, Modifier.padding(padding))
        }
    }
}

@Composable
private fun TodayDestination(repository: ReflectionRepository, modifier: Modifier, onEdit: (String) -> Unit) {
    val moments = listOf(
        SparkMoment("Gratitude", "What brought a little light into your day?"),
        SparkMoment("Daily Win", "What deserves a little credit today?"),
        SparkMoment("Affirmation", "I can grow at my own pace.")
    )
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("YOUR DAILY MOMENTS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                Text("Find your spark.", style = MaterialTheme.typography.headlineLarge)
                Text("There's something worth noticing.", style = MaterialTheme.typography.bodyLarge)
            }
        }
        items(moments, key = { it.title }) { moment ->
            if (moment.title == "Affirmation") {
                SparkMomentCard(moment)
            } else {
                SparkMomentCard(
                    moment,
                    modifier = Modifier.clickable {
                        onEdit(if (moment.title == "Gratitude") "GRATITUDE" else "WIN")
                    }
                )
            }
        }
        item { ResurfacingCard(repository) }
        item { Text("Tap Gratitude or Daily Win to write a private reflection.", style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun EmptyDestination(title: String, message: String, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Text(message, style = MaterialTheme.typography.bodyLarge)
        Text("This destination is not yet implemented.", style = MaterialTheme.typography.bodySmall)
    }
}
