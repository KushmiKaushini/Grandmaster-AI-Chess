package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = WarmGold,
    secondary = WoodDark,
    tertiary = WoodLight,
    background = SlateDark,
    surface = SlateGrey,
    onPrimary = Color(0xFF070A10), // Solid high-contrast charcoal black on Gold
    onSecondary = SlateDarkText,
    onTertiary = SlateDarkText,
    onBackground = SlateDarkText,
    onSurface = SlateDarkText
  )

private val LightColorScheme =
  lightColorScheme(
    primary = WarmGold,
    secondary = WoodDark,
    tertiary = WoodLight,
    background = SlateDark,
    surface = SlateGrey,
    onPrimary = Color(0xFF070A10), // Solid high-contrast charcoal black on Gold
    onSecondary = SlateDarkText,
    onTertiary = SlateDarkText,
    onBackground = SlateDarkText,
    onSurface = SlateDarkText
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark/Royal-cream theme for high-end luxury view
  dynamicColor: Boolean = false, // Disable dynamic colors to ensure our tailored custom styling represents perfectly
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme // Royal premium dark theme base

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
