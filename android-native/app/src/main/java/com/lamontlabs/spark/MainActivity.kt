package com.lamontlabs.spark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) { SparkTodayFoundation() }
            }
        }
    }
}

private data class TodayCard(val title: String, val prompt: String)

@Composable
private fun SparkTodayFoundation() {
    val cards = listOf(
        TodayCard("Gratitude", "What brought a little light into your day?"),
        TodayCard("Daily Win", "What deserves a little credit today?"),
        TodayCard("Affirmation", "I can grow at my own pace.")
    )
    Scaffold { insets ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(insets),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Find your spark.", style = MaterialTheme.typography.headlineLarge)
                    Text("There's something worth noticing.", style = MaterialTheme.typography.bodyLarge)
                }
            }
            items(cards) { card ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(card.title, style = MaterialTheme.typography.titleLarge)
                        Text(card.prompt, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            item {
                Text(
                    "Foundation preview only. Journal saving is not implemented.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
