package com.example.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Signature rounded corners
val OneUISquircleLarge = RoundedCornerShape(32.dp)
val OneUISquircleMedium = RoundedCornerShape(24.dp)
val OneUISquircleSmall = RoundedCornerShape(16.dp)
val OneUIPillShape = RoundedCornerShape(percent = 50)

// Glass aesthetic colors
val GlassBackgroundDark = Color(0x38181A2F)
val GlassBackgroundLight = Color(0x1AFFFFFF)
val GlassBorderTop = Color(0x66FFFFFF)
val GlassBorderBottom = Color(0x14FFFFFF)

data class ExpressivePalette(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val surface: Color,
    val onSurface: Color,
    val ambientGlow: Color
)

fun getAndroid17MonetPalette(context: Context, darkTheme: Boolean = true): Triple<Color, Color, Color> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val scheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        Triple(scheme.primary, scheme.secondary, scheme.tertiary)
    } else {
        // High fidelity Android 17 Monet Expressive fallback
        Triple(Color(0xFF6C8CFF), Color(0xFFB0C6FF), Color(0xFFD6E3FF))
    }
}

@Composable
fun rememberAnimatedExpressivePalette(
    primaryTarget: Color,
    secondaryTarget: Color,
    accentTarget: Color
): ExpressivePalette {
    val primary by animateColorAsState(primaryTarget, animationSpec = MotionSpecs.expressiveSpring(), label = "primary")
    val secondary by animateColorAsState(secondaryTarget, animationSpec = MotionSpecs.expressiveSpring(), label = "secondary")
    val accent by animateColorAsState(accentTarget, animationSpec = MotionSpecs.expressiveSpring(), label = "accent")

    return ExpressivePalette(
        primary = primary,
        secondary = secondary,
        accent = accent,
        surface = Color(0xFF131422),
        onSurface = Color(0xFFF0F0F8),
        ambientGlow = primary.copy(alpha = 0.35f)
    )
}

fun Modifier.glassmorphism(
    shape: Shape = OneUISquircleMedium,
    backgroundColor: Color = Color(0x40181A32),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 0.dp
): Modifier = this
    .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape, clip = false) else Modifier)
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                backgroundColor.copy(alpha = 0.55f),
                backgroundColor.copy(alpha = 0.35f)
            )
        ),
        shape = shape
    )
    .border(
        border = BorderStroke(
            borderWidth,
            Brush.verticalGradient(
                colors = listOf(
                    GlassBorderTop,
                    GlassBorderBottom
                )
            )
        ),
        shape = shape
    )
    .clip(shape)

fun parseHexColor(hexString: String): Color? {
    val clean = hexString.trim().removePrefix("#")
    if (clean.length == 6) {
        val colorInt = clean.toLongOrNull(16) ?: return null
        return Color((0xFF000000 or colorInt).toInt())
    } else if (clean.length == 8) {
        val colorInt = clean.toLongOrNull(16) ?: return null
        return Color(colorInt.toInt())
    }
    return null
}

fun generatePaletteFromHex(primaryHex: Color): Triple<Color, Color, Color> {
    val r = primaryHex.red
    val g = primaryHex.green
    val b = primaryHex.blue
    val secondary = Color(
        red = (r * 0.75f + 0.25f).coerceIn(0f, 1f),
        green = (g * 0.75f + 0.25f).coerceIn(0f, 1f),
        blue = (b * 0.75f + 0.25f).coerceIn(0f, 1f),
        alpha = 1f
    )
    val accent = Color(
        red = (r * 0.45f + 0.55f).coerceIn(0f, 1f),
        green = (g * 0.45f + 0.55f).coerceIn(0f, 1f),
        blue = (b * 0.45f + 0.55f).coerceIn(0f, 1f),
        alpha = 1f
    )
    return Triple(primaryHex, secondary, accent)
}

