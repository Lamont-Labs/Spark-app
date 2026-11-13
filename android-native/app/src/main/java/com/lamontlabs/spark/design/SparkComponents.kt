package com.lamontlabs.spark.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SparkMoment(val title: String, val description: String)

@Composable
fun SparkMomentCard(moment: SparkMoment, modifier: Modifier = Modifier) {
    val isWin = moment.title == "Daily Win"
    val isAffirm = moment.title == "Affirmation"
    val accent = when {
        isWin -> MaterialTheme.colorScheme.secondary
        isAffirm -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary
    }
    val icon = when {
        isWin -> Icons.Filled.StarOutline
        isAffirm -> Icons.Filled.AutoAwesome
        else -> Icons.Filled.FavoriteBorder
    }
    val category = when {
        isWin -> "CELEBRATE YOURSELF"
        isAffirm -> "A THOUGHT TO CARRY"
        else -> "NOTICE THE GOOD"
    }
    Card(
        modifier = modifier.fillMaxWidth().heightIn(min = 160.dp),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.24f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = SparkPalette.NightCard
        )
    ) {
        Column(
            modifier = Modifier.background(Brush.linearGradient(listOf(accent.copy(alpha = 0.13f), SparkPalette.NightCard, SparkPalette.Midnight))).padding(horizontal = 22.dp, vertical = 21.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = accent.copy(alpha = 0.12f)) {
                    Icon(icon, contentDescription = null, tint = accent,
                        modifier = Modifier.padding(11.dp).size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(category, style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.3.sp, color = accent, fontWeight = FontWeight.SemiBold)
            }
            Text(moment.title, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold, color = SparkPalette.Ice)
            Text(moment.description, style = MaterialTheme.typography.bodyLarge,
                color = SparkPalette.MutedBlue, lineHeight = 24.sp)
            if (!isAffirm) {
                Text("TAP TO REFLECT", style = MaterialTheme.typography.labelSmall,
                    color = accent, letterSpacing = 1.1.sp)
            }
        }
    }
}

/**
 * Reusable midnight glass layer. Uses native Compose gradients rather than
 * blur or external render dependencies, keeping offline rendering deterministic.
 */
@Composable
fun SparkGlassSurface(
    modifier: Modifier = Modifier,
    accent: Color = SparkPalette.Cyan,
    prominent: Boolean = false,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(if (prominent) 28.dp else 22.dp)
    val edge = accent.copy(alpha = if (prominent) 0.42f else 0.22f)
    Surface(
        modifier = modifier,
        shape = shape,
        color = SparkPalette.NightCard,
        border = BorderStroke(1.dp, edge),
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            accent.copy(alpha = if (prominent) 0.19f else 0.09f),
                            SparkPalette.RaisedCard.copy(alpha = 0.74f),
                            SparkPalette.NightCard
                        )
                    )
                )
                .drawBehind {
                    val highlight = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            accent.copy(alpha = if (prominent) 0.55f else 0.28f),
                            Color.White.copy(alpha = if (prominent) 0.23f else 0.12f),
                            Color.Transparent
                        )
                    )
                    drawLine(
                        brush = highlight,
                        start = Offset(size.width * 0.07f, 1.dp.toPx()),
                        end = Offset(size.width * 0.93f, 1.dp.toPx()),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
        ) {
            content()
        }
    }
}
