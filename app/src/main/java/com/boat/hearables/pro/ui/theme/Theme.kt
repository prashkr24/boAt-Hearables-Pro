package com.boat.hearables.pro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.boat.hearables.pro.data.AccentTheme

@Composable
fun BoatHearablesProTheme(
    accentTheme: AccentTheme = AccentTheme.BOAT_CRIMSON,
    oledPitchBlack: Boolean = true,
    content: @Composable () -> Unit
) {
    val primary = Color(accentTheme.primaryHex)
    val secondary = Color(accentTheme.secondaryHex)
    val bg = if (oledPitchBlack) Color(0xFF040507) else Color(0xFF0D1017)
    val surface = if (oledPitchBlack) Color(0xFF10131A) else Color(0xFF171C26)
    val surfaceVariant = if (oledPitchBlack) Color(0xFF181D27) else Color(0xFF202736)

    val colorScheme = darkColorScheme(
        primary = primary,
        onPrimary = Color.Black,
        primaryContainer = primary.copy(alpha = 0.18f),
        onPrimaryContainer = primary,
        secondary = secondary,
        onSecondary = Color.Black,
        secondaryContainer = secondary.copy(alpha = 0.16f),
        onSecondaryContainer = secondary,
        tertiary = Color(0xFF00E676),
        background = bg,
        onBackground = Color(0xFFF3F6FC),
        surface = surface,
        onSurface = Color(0xFFF3F6FC),
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = Color(0xFF9BA5B7),
        outline = Color(0xFF2B3344)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
