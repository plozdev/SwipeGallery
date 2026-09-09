package plozdev.swipegallery.theme

import androidx.compose.ui.graphics.Color

/**
 * Design Tokens for SwipeGallery OLED Dark Theme.
 * Strictly aligned with Material Design 3 and OLED dark requirements:
 * - Pure deep background: #0B0E14
 * - Surfaces: #161B22 (surface) and #1C212B (surfaceVariant)
 * - Primary/Keep: Emerald (#10B981 / #34D399)
 * - Error/Destructive: Coral (#EF4444 / #F87171)
 */
object SwipeColors {
    // OLED Dark Surfaces & Backgrounds
    val Background = Color(0xFF0B0E14)
    val Surface = Color(0xFF161B22)
    val SurfaceVariant = Color(0xFF1C212B)
    val SurfaceContainerHighest = Color(0xFF21262D)

    // Primary Emerald (Keep / Like / Action)
    val Primary = Color(0xFF10B981)
    val PrimaryLight = Color(0xFF34D399)
    val PrimaryContainer = Color(0xFF064E3B)
    val OnPrimaryContainer = Color(0xFF34D399)

    // Destructive Coral (Reject / Delete / Error)
    val Error = Color(0xFFEF4444)
    val ErrorLight = Color(0xFFF87171)
    val ErrorContainer = Color(0xFF3D1518)
    val OnErrorContainer = Color(0xFFF87171)

    // Accent Sky / Secondary
    val Secondary = Color(0xFF38BDF8)
    val SecondaryContainer = Color(0xFF0C4A6E)
    val OnSecondaryContainer = Color(0xFFBAE6FD)

    // Neutral Typography & Borders
    val TextPrimary = Color(0xFFF0F6FC)
    val TextSecondary = Color(0xFF8B949E)
    val TextMuted = Color(0xFF6E7681)
    val Outline = Color(0xFF30363D)
    val OutlineVariant = Color(0xFF21262D)

    // Semantic mappings
    val Keep = PrimaryLight
    val Delete = ErrorLight
}
