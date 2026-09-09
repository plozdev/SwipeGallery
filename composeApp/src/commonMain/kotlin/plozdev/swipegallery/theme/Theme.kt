package plozdev.swipegallery.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * OLED Dark Color Scheme strictly mapping to Material Design 3 tokens:
 * - background: #0B0E14
 * - surface: #161B22
 * - surfaceVariant: #1C212B
 * - primary: #10B981 (Emerald Keep)
 * - error: #EF4444 (Coral Destructive)
 * - errorContainer: #3D1518
 * - onErrorContainer: #F87171
 */
val SwipeOledDarkColorScheme = darkColorScheme(
    primary = SwipeColors.Primary,
    onPrimary = SwipeColors.Background,
    primaryContainer = SwipeColors.PrimaryContainer,
    onPrimaryContainer = SwipeColors.OnPrimaryContainer,

    secondary = SwipeColors.Secondary,
    onSecondary = SwipeColors.Background,
    secondaryContainer = SwipeColors.SecondaryContainer,
    onSecondaryContainer = SwipeColors.OnSecondaryContainer,

    error = SwipeColors.Error,
    onError = SwipeColors.Background,
    errorContainer = SwipeColors.ErrorContainer,
    onErrorContainer = SwipeColors.OnErrorContainer,

    background = SwipeColors.Background,
    onBackground = SwipeColors.TextPrimary,

    surface = SwipeColors.Surface,
    onSurface = SwipeColors.TextPrimary,

    surfaceVariant = SwipeColors.SurfaceVariant,
    onSurfaceVariant = SwipeColors.TextSecondary,

    outline = SwipeColors.Outline,
    outlineVariant = SwipeColors.OutlineVariant
)

@Composable
fun SwipeGalleryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SwipeOledDarkColorScheme,
        typography = SwipeTypography,
        content = content
    )
}
