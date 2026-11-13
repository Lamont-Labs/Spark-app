package com.lamontlabs.spark.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Spark visual identity v1: cinematic midnight, electric cyan, quiet reflective surfaces.
// Legacy palette aliases are retained until all existing screens are migrated.
object SparkPalette {
    val Void = Color(0xFF010916)
    val Midnight = Color(0xFF041B36)
    val NightCard = Color(0xFF0A2543)
    val RaisedCard = Color(0xFF103153)
    val Electric = Color(0xFF008DFF)
    val Cyan = Color(0xFF22DFFF)
    val Ice = Color(0xFFE8FAFF)
    val MutedBlue = Color(0xFFB3CCDD)
    val Hairline = Color(0xFF54758E)

    // Transitional aliases for previously implemented screens.
    val Ivory = Ice
    val Paper = Color(0xFFF4FAFF)
    val Ink = Color(0xFF071A2D)
    val MutedInk = Color(0xFF365267)
    val Peach = Cyan
    val Lavender = Electric
    val Apricot = Cyan
    val NightLavender = Cyan
    val NightIvory = Ice
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF005DAF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8EEFF),
    onPrimaryContainer = SparkPalette.Ink,
    secondary = Color(0xFF006C80),
    onSecondary = Color.White,
    background = Color(0xFFF1F8FF),
    onBackground = SparkPalette.Ink,
    surface = Color.White,
    onSurface = SparkPalette.Ink,
    onSurfaceVariant = SparkPalette.MutedInk,
    outline = Color(0xFF567083)
)

private val DarkColors = darkColorScheme(
    primary = SparkPalette.Cyan,
    onPrimary = SparkPalette.Void,
    primaryContainer = SparkPalette.RaisedCard,
    onPrimaryContainer = SparkPalette.Ice,
    secondary = SparkPalette.Electric,
    onSecondary = SparkPalette.Void,
    background = SparkPalette.Void,
    onBackground = SparkPalette.Ice,
    surface = SparkPalette.NightCard,
    onSurface = SparkPalette.Ice,
    onSurfaceVariant = SparkPalette.MutedBlue,
    outline = SparkPalette.Hairline,
    surfaceVariant = SparkPalette.RaisedCard,
    outlineVariant = Color(0xFF2B4A65)
)

object SparkSpacing {
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 12.dp
    val Lg = 16.dp
    val Xl = 24.dp
    val Xxl = 32.dp
}

object SparkMotion {
    const val QuickMs = 120
    const val StandardMs = 220
    const val DeliberateMs = 320
}

private val SparkShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val SparkTypography = Typography().let { base ->
    base.copy(
        headlineLarge = base.headlineLarge.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.7).sp,
            lineHeight = 40.sp
        ),
        headlineSmall = base.headlineSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.3).sp
        ),
        titleLarge = base.titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp
        ),
        labelMedium = base.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.copy(lineHeight = 25.sp),
        bodyMedium = base.bodyMedium.copy(lineHeight = 22.sp)
    )
}

@Composable
fun SparkTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = SparkShapes,
        typography = SparkTypography,
        content = content
    )
}
