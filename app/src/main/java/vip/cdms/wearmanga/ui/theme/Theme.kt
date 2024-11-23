package vip.cdms.wearmanga.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

val lightColors = Colors(
    primary = primaryLight,
    primaryVariant = primaryLightMediumContrast,
    secondary = secondaryLight,
    secondaryVariant = secondaryDarkMediumContrast,
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

val darkColors = Colors(
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
fun WearMangaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) darkColors else lightColors,
        content = content
    )
}
