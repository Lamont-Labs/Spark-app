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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Color
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
import com.lamontlabs.spark.design.SparkGlassSurface

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
            NavigationBar(
                containerColor = SparkPalette.Midnight,
                tonalElevation = 0.dp,
                modifier = Modifier.height(68.dp)
            ) {
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
                        label = {
                            Text(
                                item.label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SparkPalette.Void,
                            selectedTextColor = SparkPalette.Cyan,
                            indicatorColor = SparkPalette.Cyan.copy(alpha = 0.92f),
                            unselectedIconColor = SparkPalette.MutedBlue,
                            unselectedTextColor = SparkPalette.MutedBlue
                        )
                    )
                }
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = destination,
            modifier = Modifier.fillMaxSize().padding(padding),
            transitionSpec = {
                fadeIn(animationSpec = tween(180)) togetherWith
                    fadeOut(animationSpec = tween(100))
            },
            label = "Spark destination transition"
        ) { active ->
            when (active) {
                Destination.TODAY -> TodayDestination(repository, resurfacingEnabled, focus, Modifier,
                    onEdit = { editingKind = it }, onAffirm = { selected = Destination.AFFIRM.name }, onJournal = { selected = Destination.JOURNAL.name })
                Destination.JOURNAL -> JournalScreen(repository, Modifier)
                Destination.AFFIRM -> AffirmScreen(Modifier)
                Destination.YOU -> ProgressScreen(repository, onPrivacy = { privacyOpen = true })
            }
        }
    }
}

@Composable
private fun TodayDestination(
    repository: ReflectionRepository,
    resurfacingEnabled: Boolean,
    focus: String,
    modifier: Modifier,
    onEdit: (String) -> Unit,
    onAffirm: () -> Unit,
    onJournal: () -> Unit
) {
    val today = java.time.LocalDate.now()
    val dateLabel = today.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d"))
    val focusLine = when (focus) {
        "GRATITUDE" -> "Notice something worth keeping."
        "WINS" -> "Your progress is worth recognizing."
        "CONFIDENCE" -> "Grow at your own pace."
        else -> "A moment for what matters."
    }
    LazyColumn(
        modifier = modifier.fillMaxSize().background(SparkPalette.Void),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("SPARK  /  TODAY", style = MaterialTheme.typography.labelMedium,
                        color = SparkPalette.Cyan, letterSpacing = 1.8.sp)
                    Text(dateLabel, style = MaterialTheme.typography.bodyMedium,
                        color = SparkPalette.MutedBlue)
                }
                Surface(shape = CircleShape, color = SparkPalette.RaisedCard) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null,
                        tint = SparkPalette.Cyan, modifier = Modifier.padding(13.dp).size(22.dp))
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("Find your spark.", style = MaterialTheme.typography.headlineLarge,
                    color = SparkPalette.Ice, fontWeight = FontWeight.Bold)
                Text(focusLine, style = MaterialTheme.typography.bodyLarge,
                    color = SparkPalette.MutedBlue)
            }
        }
        item {
            SparkGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                accent = SparkPalette.Cyan,
                prominent = true
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(13.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null,
                            tint = SparkPalette.Cyan, modifier = Modifier.size(17.dp))
                        Text("A MOMENT WORTH KEEPING", style = MaterialTheme.typography.labelSmall,
                            color = SparkPalette.Cyan, letterSpacing = 1.4.sp)
                    }
                    Text("What was good today?", style = MaterialTheme.typography.headlineSmall,
                        color = SparkPalette.Ice, fontWeight = FontWeight.SemiBold)
                    Text("One small thing is enough.", style = MaterialTheme.typography.bodyMedium,
                        color = SparkPalette.MutedBlue)
                    Button(onClick = { onEdit("GRATITUDE") },
                        modifier = Modifier.fillMaxWidth()) {
                        Text("Capture a moment")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
        item {
            Text("KEEP GOING", style = MaterialTheme.typography.labelSmall,
                color = SparkPalette.MutedBlue, letterSpacing = 1.6.sp)
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TodayQuickAction(
                    title = "Your journal", subtitle = "Revisit your moments",
                    icon = Icons.Filled.List,
                    modifier = Modifier.weight(1f), onClick = onJournal
                )
                TodayQuickAction(
                    title = "Daily win", subtitle = "Celebrate progress",
                    icon = Icons.Filled.StarOutline,
                    modifier = Modifier.weight(1f), onClick = { onEdit("WIN") }
                )
            }
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth().clickable(role = Role.Button,
                    onClickLabel = "Open affirmations", onClick = onAffirm),
                shape = RoundedCornerShape(22.dp),
                color = SparkPalette.NightCard,
                border = BorderStroke(1.dp, SparkPalette.Hairline.copy(alpha = 0.28f))
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null,
                        tint = SparkPalette.Cyan, modifier = Modifier.size(26.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Words to carry", style = MaterialTheme.typography.titleMedium,
                            color = SparkPalette.Ice)
                        Text("A gentle affirmation for today",
                            style = MaterialTheme.typography.bodySmall, color = SparkPalette.MutedBlue)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null,
                        tint = SparkPalette.Cyan)
                }
            }
        }
        if (resurfacingEnabled) {
            item { ResurfacingCard(repository) }
        }
        item {
            Text("No pressure. No perfect days required.",
                style = MaterialTheme.typography.bodySmall, color = SparkPalette.MutedBlue)
        }
    }
}

@Composable
private fun TodayQuickAction(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SparkGlassSurface(
        modifier = modifier.clickable(role = Role.Button, onClickLabel = title, onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Icon(icon, contentDescription = null, tint = SparkPalette.Cyan,
                modifier = Modifier.size(25.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = SparkPalette.Ice)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = SparkPalette.MutedBlue, maxLines = 2)
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
