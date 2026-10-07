package com.example.carrierlookup.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

class Palette(
    val background: Color,
    val card: Color,
    val cardHigh: Color,
    val border: Color,
    val accent: Color,
    val green: Color,
    val red: Color,
    val textPrimary: Color,
    val textSecondary: Color
)

private val DarkPalette = Palette(
    background = Color(0xFF0A1220),
    card = Color(0xFF121C2F),
    cardHigh = Color(0xFF1A2640),
    border = Color(0xFF223050),
    accent = Color(0xFF2F6BFF),
    green = Color(0xFF34D399),
    red = Color(0xFFF87171),
    textPrimary = Color(0xFFF1F5FF),
    textSecondary = Color(0xFF8FA0BF)
)

private val LightPalette = Palette(
    background = Color(0xFFF3F6FC),
    card = Color(0xFFFFFFFF),
    cardHigh = Color(0xFFE8EEF9),
    border = Color(0xFFD5DDEE),
    accent = Color(0xFF2F6BFF),
    green = Color(0xFF059669),
    red = Color(0xFFDC2626),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF5B6B88)
)

private val LocalPalette = staticCompositionLocalOf { DarkPalette }

/** Colori dell'app: cambiano da soli tra tema scuro e chiaro. */
object AppColors {
    val Background: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.background
    val Card: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.card
    val CardHigh: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.cardHigh
    val Border: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.border
    val Accent: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.accent
    val Green: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.green
    val Red: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.red
    val TextPrimary: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.textPrimary
    val TextSecondary: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.textSecondary
}

@Composable
fun CarrierLookupTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val p = if (darkTheme) DarkPalette else LightPalette
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = p.accent, onPrimary = Color.White, secondary = p.green,
            background = p.background, onBackground = p.textPrimary,
            surface = p.card, onSurface = p.textPrimary,
            surfaceVariant = p.cardHigh, onSurfaceVariant = p.textSecondary,
            outline = p.border, error = p.red
        )
    } else {
        lightColorScheme(
            primary = p.accent, onPrimary = Color.White, secondary = p.green,
            background = p.background, onBackground = p.textPrimary,
            surface = p.card, onSurface = p.textPrimary,
            surfaceVariant = p.cardHigh, onSurfaceVariant = p.textSecondary,
            outline = p.border, error = p.red
        )
    }
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
