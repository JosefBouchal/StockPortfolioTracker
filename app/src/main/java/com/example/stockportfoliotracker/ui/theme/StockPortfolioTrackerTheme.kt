package com.example.stockportfoliotracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightThemeColors = lightColorScheme(
    primary = Purple500,
    secondary = Teal200,
    tertiary = Purple200
)

private val DarkThemeColors = darkColorScheme(
    primary = Purple200,
    secondary = Teal200,
    tertiary = Purple500
)

@Composable
fun StockPortfolioTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkThemeColors
    } else {
        LightThemeColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}