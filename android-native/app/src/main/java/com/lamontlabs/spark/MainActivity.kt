package com.lamontlabs.spark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.spark.design.SparkMoment
import com.lamontlabs.spark.design.SparkMomentCard
import com.lamontlabs.spark.design.SparkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SparkTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SparkToday()
                }
            }
        }
    }
}

@Composable
private fun SparkToday() {
    val cards = listOf(
        SparkMoment("Gratitude", "What brought a little light into your day?"),
        SparkMoment("Daily Win", "What deserves a little credit today?"),
        SparkMoment("Affirmation", "I can grow at my own pace.")
    )
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { insets ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(insets),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "YOUR DAILY MOMENTS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        "Find your spark.",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "There's something worth noticing.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(cards, key = { it.title }) { moment ->
                SparkMomentCard(moment)
            }
            item {
                Text(
                    "Foundation preview only. Journal saving is not implemented.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
