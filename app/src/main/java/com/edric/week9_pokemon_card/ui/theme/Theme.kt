package com.edric.week9_pokemon_card.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PokeDarkColorScheme = darkColorScheme(
    primary = PokeRed,
    secondary = PokeGreen,
    tertiary = PokeGold,
    background = PokeBackground,
    surface = PokeCardSurface,
    surfaceVariant = PokeHeaderSurface,
    onPrimary = PokeTextPrimary,
    onSecondary = PokeTextPrimary,
    onBackground = PokeTextPrimary,
    onSurface = PokeTextPrimary,
    onSurfaceVariant = PokeTextSecondary,
    outline = PokeCardBorder,
    error = PokeErrorRed,
)

@Composable
fun Week9pokemoncardTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = PokeDarkColorScheme,
        typography = Typography,
        content = content,
    )
}
