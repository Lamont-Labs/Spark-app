package com.lamontlabs.spark.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
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
import com.lamontlabs.spark.privacy.PrivacyPreferences
import com.lamontlabs.spark.privacy.PrivacyScreen
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.lamontlabs.spark.onboarding.OnboardingPreferences
import com.lamontlabs.spark.design.SparkMoment
import com.lamontlabs.spark.design.SparkPalette
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import com.lamontlabs.spark.design.SparkMomentCard

private enum class Destination(val label: String) {
    TODAY("Today"), JOURNAL("Journal"), AFFIRM("Affirm"), YOU("You")
}

@Composable
fun SparkNavigation(repository: ReflectionRepository) {
    val context = LocalContext.current
    val preferences = remember(context) { PrivacyPreferences(context) }
    val resurfacingEnabled by preferences.resurfacingEnabled.collectAsState(initial = false)
    val onboarding = remember(context) { OnboardingPreferences(context) }
    val focus by onboarding.focus.collectAsState(initial = "EVERYTHING")
    var privacyOpen by rememberSaveable { mutableStateOf(false) }
    var editingKind by rememberSaveable { mutableStateOf<String?>(null) }
    var selected by rememberSaveable { mutableStateOf(Destination.TODAY.name) }
    val destination = Destination.entries.firstOrNull { it.name == selected } ?: Destination.TODAY
    if (privacyOpen) {
        Column {
            TextButton(onClick = { privacyOpen = false }) { Text("Back to Spark") }
            PrivacyScreen(preferences)
        }
        return
    }
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
                        icon = {
                            Icon(
                                imageVector = when (item) {
                                    Destination.TODAY -> Icons.Filled.Home
                                    Destination.JOURNAL -> Icons.Filled.List
                                    Destination.AFFIRM -> Icons.Filled.Favorite
                                    Destination.YOU -> Icons.Filled.Person
                                },
                                contentDescription = null
                            )
                        },
                        label = { Text(item.label) },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = destination,
            modifier = Modifier.fillMaxSize().padding(padding),
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith
                    fadeOut(animationSpec = tween(110))
            },
            label = "Spark destination transition"
        ) { active ->
            when (active) {
                Destination.TODAY -> TodayDestination(repository, resurfacingEnabled, focus, Modifier, onEdit = { editingKind = it })
                Destination.JOURNAL -> JournalScreen(repository, Modifier)
                Destination.AFFIRM -> AffirmScreen(Modifier)
                Destination.YOU -> Column {
                    TextButton(onClick = { privacyOpen = true }) { Text("Privacy & preferences") }
                    ProgressScreen(repository)
                }
            }
        }
    }
}

@Composable
private fun TodayDestination(repository: ReflectionRepository, resurfacingEnabled: Boolean, focus: String, modifier: Modifier, onEdit: (String) -> Unit) {
    val today = java.time.LocalDate.now()
    val dateLabel = today.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d"))
    val focusLine = when (focus) {
        "GRATITUDE" -> "There is good worth noticing."
        "WINS" -> "Your progress deserves recognition."
        "CONFIDENCE" -> "Give yourself room to grow."
        else -> "Take a moment. The rest can wait."
    }
    val moments = listOf(
        SparkMoment("Gratitude", "What brought a little light into your day?"),
        SparkMoment("Daily Win", "What deserves a little credit today?"),
        SparkMoment("Affirmation", "I can grow at my own pace.")
    )
    LazyColumn(
        modifier = modifier.fillMaxSize().background(SparkPalette.Void),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "SPARK  /  TODAY",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
                Text(dateLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("A little good,\\na little every day.".replace("\\n", "\n"), style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold, lineHeight = 42.sp)
                Text(focusLine, style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = SparkPalette.NightCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.background(Brush.linearGradient(listOf(SparkPalette.RaisedCard, SparkPalette.NightCard, SparkPalette.Midnight))).padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("YOUR MOMENT OF PAUSE", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary, letterSpacing = 1.5.sp)
                    Text("What felt good today?", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold)
                    Text("It doesn't need to be a big thing.", style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { onEdit("GRATITUDE") }) {
                        Text("Capture a gratitude")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
        item {
            Text("MAKE SPACE FOR THE GOOD", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.5.sp)
        }
        items(moments, key = { it.title }) { moment ->
            if (moment.title == "Affirmation") {
                SparkMomentCard(moment)
            } else {
                SparkMomentCard(
                    moment,
                    modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClickLabel = "Write ${moment.title}") {
                        onEdit(if (moment.title == "Gratitude") "GRATITUDE" else "WIN")
                    }
                )
            }
        }
        if (resurfacingEnabled) {
            item { ResurfacingCard(repository) }
        }
        item {
            Text("No pressure. No perfect days required.", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
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
