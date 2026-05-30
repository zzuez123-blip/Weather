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
    primary = ImmersiveDarkPrimary,
    onPrimary = Color(0xFF381E72),
    primaryContainer = ImmersiveDarkPrimaryContainer,
    onPrimaryContainer = ImmersiveDarkOnPrimaryContainer,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = ImmersiveDarkBg,
    onBackground = ImmersiveDarkOnBg,
    surface = ImmersiveDarkSurface,
    onSurface = ImmersiveDarkOnSurface,
    surfaceVariant = ImmersiveDarkSurfaceVariant,
    onSurfaceVariant = ImmersiveDarkOnSurfaceVariant,
    outline = ImmersiveDarkOutline
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ImmersiveLightPrimary,
    onPrimary = Color.White,
    primaryContainer = ImmersiveLightPrimaryContainer,
    onPrimaryContainer = ImmersiveLightOnPrimaryContainer,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = ImmersiveLightBg,
    onBackground = ImmersiveLightOnBg,
    surface = ImmersiveLightSurface,
    onSurface = ImmersiveLightOnSurface,
    surfaceVariant = ImmersiveLightSurfaceVariant,
    onSurfaceVariant = ImmersiveLightOnSurfaceVariant,
    outline = ImmersiveLightOutline
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
