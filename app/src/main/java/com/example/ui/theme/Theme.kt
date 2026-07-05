package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = BoldPrimary,
    background = BoldBackground,
    surface = BoldSurfaceVar,
    onBackground = BoldTextPrimary,
    onSurface = BoldTextPrimary
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BoldPrimary,
    secondary = BoldActivePurple,
    tertiary = BoldSoftBlue,
    background = BoldBackground,
    surface = BoldSurfaceVar,
    onPrimary = Color.White,
    onSecondary = BoldTextAmethyst,
    onTertiary = BoldTextNavy,
    onBackground = BoldTextPrimary,
    onSurface = BoldTextPrimary,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Enforce Bold Typography theme default lights
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme


  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
