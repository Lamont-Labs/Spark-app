package com.lamontlabs.spark.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object SparkPalette {
    val Ivory = Color(0xFFFAF6F0)
    val Paper = Color(0xFFFFFDF9)
    val Ink = Color(0xFF29243A)
    val MutedInk = Color(0xFF514657)
    val Peach = Color(0xFFDDA58F)
    val Lavender = Color(0xFF9881C7)
    val Midnight = Color(0xFF101225)
    val NightCard = Color(0xFF22243C)
    val Apricot = Color(0xFFF2B58C)
    val NightLavender = Color(0xFFB5A0EF)
    val NightIvory = Color(0xFFF7F1EC)
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF654B8D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEE7F7),
    onPrimaryContainer = SparkPalette.Ink,
    secondary = Color(0xFF86534A),
    onSecondary = Color.White,
    background = SparkPalette.Ivory,
    onBackground = SparkPalette.Ink,
    surface = SparkPalette.Paper,
    onSurface = SparkPalette.Ink,
    onSurfaceVariant = SparkPalette.MutedInk,
    outline = Color(0xFF807681)
)

private val DarkColors = darkColorScheme(
    primary = SparkPalette.NightLavender,
    onPrimary = SparkPalette.Midnight,
    primaryContainer = Color(0xFF39304D),
    onPrimaryContainer = SparkPalette.NightIvory,
    secondary = SparkPalette.Apricot,
    onSecondary = SparkPalette.Midnight,
    background = SparkPalette.Midnight,
    onBackground = SparkPalette.NightIvory,
    surface = SparkPalette.NightCard,
    onSurface = SparkPalette.NightIvory,
    onSurfaceVariant = Color(0xFFD5CDDC),
    outline = Color(0xFFB9AEC7)
)

@Composable
fun SparkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
