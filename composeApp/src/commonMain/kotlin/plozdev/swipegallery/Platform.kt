package plozdev.swipegallery

import androidx.compose.runtime.Composable

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun formatEpochSeconds(seconds: Long): String

expect fun getCurrentEpochSeconds(): Long

expect fun getEpochDay(): Long

@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)

expect fun triggerHapticFeedback(isThreshold: Boolean = false)