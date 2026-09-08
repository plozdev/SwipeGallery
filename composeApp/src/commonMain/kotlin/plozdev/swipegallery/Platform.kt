package plozdev.swipegallery

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun formatEpochSeconds(seconds: Long): String

expect fun getCurrentEpochSeconds(): Long