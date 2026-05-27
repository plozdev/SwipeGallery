package plozdev.swipegallery.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SwipeDarkColorScheme = darkColorScheme(
    primary = SwipeColors.Primary,
    onPrimary = SwipeColors.TextPrimary,

    secondary = SwipeColors.Secondary,
    onSecondary = SwipeColors.TextPrimary,

    tertiary = SwipeColors.Tertiary,
    onTertiary = SwipeColors.TextPrimary,

    background = SwipeColors.Background,
    onBackground = SwipeColors.TextPrimary,

    surface = SwipeColors.Surface,
    onSurface = SwipeColors.TextPrimary,

    surfaceVariant = SwipeColors.SurfaceVariant,
    onSurfaceVariant = SwipeColors.TextSecondary,

    outline = SwipeColors.Outline,
    error = SwipeColors.Delete,
    onError = SwipeColors.TextPrimary
)

@Composable
fun SwipeGalleryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SwipeDarkColorScheme,
        typography = SwipeTypography,
        content = content
    )
}
