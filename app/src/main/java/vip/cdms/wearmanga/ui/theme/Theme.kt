package vip.cdms.wearmanga.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme as Material3Theme

@Suppress("DuplicatedCode")
private val lightScheme = ColorScheme(
    primary = primaryLight,
    primaryDim = primaryLightMediumContrast,
    primaryContainer = primaryContainerLight,
    onPrimary = onPrimaryLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    secondaryDim = secondaryLightMediumContrast,
    secondaryContainer = secondaryContainerLight,
    onSecondary = onSecondaryLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    tertiaryDim = tertiaryLightMediumContrast,
    tertiaryContainer = tertiaryContainerLight,
    onTertiary = onTertiaryLight,
    onTertiaryContainer = onTertiaryContainerLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    onSurface = onSurfaceLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight
)
private val lightColors = Colors(
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

@Suppress("DuplicatedCode")
private val darkScheme = ColorScheme(
    primary = primaryDark,
    primaryDim = primaryDarkMediumContrast,
    primaryContainer = primaryContainerDark,
    onPrimary = onPrimaryDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    secondaryDim = secondaryDarkMediumContrast,
    secondaryContainer = secondaryContainerDark,
    onSecondary = onSecondaryDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    tertiaryDim = tertiaryDarkMediumContrast,
    tertiaryContainer = tertiaryContainerDark,
    onTertiary = onTertiaryDark,
    onTertiaryContainer = onTertiaryContainerDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    onSurface = onSurfaceDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark
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
fun WearMangaTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    Material3Theme(
        colorScheme = if (darkTheme) darkScheme else lightScheme,
    ) {
        MaterialTheme(
            colors = if (darkTheme) darkColors else lightColors,
            content = content
        )
    }
}
