package vip.cdms.wearmanga.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val lightColors = Colors(
    primary = primaryLight,
    primaryVariant = primaryLightMediumContrast,
    secondary = secondaryLight,
    secondaryVariant = secondaryLightMediumContrast,
    background = backgroundLight,
    surface = surfaceLight,
    error = errorLight,
    onPrimary = onPrimaryLight,
    onSecondary = onSecondaryLight,
    onBackground = onBackgroundLight,
    onSurface = onSurfaceLight,
    onSurfaceVariant = onSurfaceVariantLight,
    onError = onErrorLight
)

private val darkColors = Colors(
    primary = primaryDark,
    primaryVariant = primaryDarkMediumContrast,
    secondary = secondaryDark,
    secondaryVariant = secondaryDarkMediumContrast,
    background = backgroundDark,
    surface = surfaceDark,
    error = errorDark,
    onPrimary = onPrimaryDark,
    onSecondary = onSecondaryDark,
    onBackground = onBackgroundDark,
    onSurface = onSurfaceDark,
    onSurfaceVariant = onSurfaceVariantDark,
    onError = onErrorDark
)

@Composable
fun AppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) darkColors else lightColors,
        content = content
    )
}
