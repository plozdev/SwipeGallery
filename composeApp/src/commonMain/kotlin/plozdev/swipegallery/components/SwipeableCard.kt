package plozdev.swipegallery.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import plozdev.swipegallery.domain.models.PhotoItem
import plozdev.swipegallery.formatEpochSeconds
import kotlin.math.roundToInt

/**
 * Direction in which the card can be swiped.
 */
enum class SwipeDirection {
    LEFT,
    RIGHT
}

/**
 * State holder for [SwipeableCard].
 *
 * Designed for 60fps+ physics-based animations:
 * - Uses [Animatable] to avoid standard recomposition during drag gestures.
 * - Exposes [swipe] to allow programmatic swiping from external UI buttons.
 * - Handles snap-back physics when release threshold is not met.
 */
@Stable
class SwipeableCardState {
    val offsetX = Animatable(0f)
    val offsetY = Animatable(0f)

    var cardWidthPx by mutableStateOf(0f)
    var cardHeightPx by mutableStateOf(0f)

    var isSwipingOut by mutableStateOf(false)
        internal set

    /**
     * Programmatically swipe the card out in the specified direction.
     * Useful for external button triggers (e.g., Like / Dislike buttons).
     */
    suspend fun swipe(
        direction: SwipeDirection,
        animationSpec: AnimationSpec<Float> = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        onSwiped: () -> Unit = {}
    ) {
        if (isSwipingOut) return
        isSwipingOut = true

        val targetX = if (direction == SwipeDirection.RIGHT) {
            if (cardWidthPx > 0f) cardWidthPx * 1.6f else 1500f
        } else {
            if (cardWidthPx > 0f) -cardWidthPx * 1.6f else -1500f
        }

        val targetY = offsetY.value + if (offsetY.value >= 0) 100f else -100f

        coroutineScope {
            launch { offsetX.animateTo(targetX, animationSpec) }
            launch { offsetY.animateTo(targetY, animationSpec) }
        }

        onSwiped()
    }

    /**
     * Snap the card back to the center (0, 0) with a bouncy physics spring.
     */
    suspend fun snapBack(
        animationSpec: AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    ) {
        coroutineScope {
            launch { offsetX.animateTo(0f, animationSpec) }
            launch { offsetY.animateTo(0f, animationSpec) }
        }
        isSwipingOut = false
    }

    /**
     * Update offset directly from drag delta without triggering composition.
     */
    suspend fun dragBy(delta: Offset) {
        if (isSwipingOut) return
        offsetX.snapTo(offsetX.value + delta.x)
        offsetY.snapTo(offsetY.value + delta.y)
    }

    /**
     * Reset offsets to zero immediately without animation.
     */
    suspend fun resetImmediate() {
        offsetX.snapTo(0f)
        offsetY.snapTo(0f)
        isSwipingOut = false
    }
}

/**
 * Remember a [SwipeableCardState] scoped to an optional key (such as `photo.id`).
 */
@Composable
fun rememberSwipeableCardState(key: Any? = null): SwipeableCardState {
    return remember(key) { SwipeableCardState() }
}

/**
 * High-performance Tinder-like swipeable card component following Material Design 3 OLED guidelines.
 *
 * Performance features:
 * 1. Reads `offsetX.value` and `offsetY.value` strictly inside lambda modifiers
 *    (`.offset { ... }` and `.graphicsLayer { ... }`) to bypass the composition phase.
 * 2. Physics-based rotation proportional to drag distance.
 * 3. Responsive threshold based on card width (default 40%).
 * 4. Strictly consumes [MaterialTheme.colorScheme] tokens with zero hardcoded hex colors.
 */
@Composable
fun SwipeableCard(
    photo: PhotoItem,
    modifier: Modifier = Modifier,
    state: SwipeableCardState = rememberSwipeableCardState(key = photo.id),
    enabled: Boolean = true,
    swipeThresholdRatio: Float = 0.40f,
    maxRotationDegrees: Float = 16f,
    hapticsEnabled: Boolean = true,
    onSwiped: (SwipeDirection) -> Unit = {},
    onClick: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                state.cardWidthPx = size.width.toFloat()
                state.cardHeightPx = size.height.toFloat()
            }
            // Layout phase: bypasses composition for 60fps drag performance
            .offset {
                IntOffset(
                    x = state.offsetX.value.roundToInt(),
                    y = state.offsetY.value.roundToInt()
                )
            }
            // Draw phase: rotates card based on drag distance
            .graphicsLayer {
                val effectiveWidth = if (state.cardWidthPx > 0f) state.cardWidthPx else 800f
                val dragFraction = (state.offsetX.value / effectiveWidth).coerceIn(-1.5f, 1.5f)
                rotationZ = dragFraction * maxRotationDegrees
            }
            // Pointer handling: only attached if card is top/enabled
            .pointerInput(enabled, photo.id) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onTap = { onClick?.invoke() }
                )
            }
            .pointerInput(enabled, photo.id) {
                if (!enabled) return@pointerInput

                detectDragGestures(
                    onDragStart = {
                        hasTriggeredHaptic = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            state.dragBy(dragAmount)
                            if (hapticsEnabled) {
                                val threshold = if (state.cardWidthPx > 0f) {
                                    state.cardWidthPx * swipeThresholdRatio
                                } else {
                                    350f
                                }
                                val isPastThreshold = kotlin.math.abs(state.offsetX.value) >= threshold
                                if (isPastThreshold && !hasTriggeredHaptic) {
                                    hasTriggeredHaptic = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                } else if (!isPastThreshold && hasTriggeredHaptic) {
                                    hasTriggeredHaptic = false
                                }
                            }
                        }
                    },
                    onDragEnd = {
                        hasTriggeredHaptic = false
                        coroutineScope.launch {
                            val threshold = if (state.cardWidthPx > 0f) {
                                state.cardWidthPx * swipeThresholdRatio
                            } else {
                                350f
                            }

                            when {
                                state.offsetX.value > threshold -> {
                                    state.swipe(SwipeDirection.RIGHT) {
                                        onSwiped(SwipeDirection.RIGHT)
                                    }
                                }
                                state.offsetX.value < -threshold -> {
                                    state.swipe(SwipeDirection.LEFT) {
                                        onSwiped(SwipeDirection.LEFT)
                                    }
                                }
                                else -> {
                                    state.snapBack()
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        hasTriggeredHaptic = false
                        coroutineScope.launch {
                            state.snapBack()
                        }
                    }
                )
            },
        shape = MaterialTheme.shapes.extraLarge, // 28.dp rounded corners
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Photo Content
            if (photo.uri.isNotBlank()) {
                LocalPhotoView(
                    photoUri = photo.uri,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                MockPhotoPlaceholder(photo = photo)
            }

            // Bottom Information Scrim (Translucent Dark Gradient)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                colorScheme.background.copy(alpha = 0.65f),
                                colorScheme.background.copy(alpha = 0.95f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    // File Name Header
                    Text(
                        text = photo.id.ifBlank { "Photo" },
                        color = colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Metadata Chips Row: Resolution, File Size, Date
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Resolution Chip
                        if (photo.width != null && photo.height != null) {
                            MetadataChip(text = "${photo.width} × ${photo.height}")
                        }

                        // Size Chip
                        val sizeText = formatCardFileSize(photo.fileSize)
                        if (sizeText.isNotBlank()) {
                            MetadataChip(text = sizeText)
                        }

                        // Date Chip
                        val dateText = formatEpochSeconds(photo.dateAdded)
                        if (dateText.isNotBlank()) {
                            MetadataChip(text = dateText)
                        }
                    }
                }
            }

            // --- Dynamic Badges (Rendered in draw phase via graphicsLayer for 60fps) ---

            // "GIỮ" (KEEP) Badge - Top Start (Emerald)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(24.dp)
                    .graphicsLayer {
                        val threshold = if (state.cardWidthPx > 0f) state.cardWidthPx * swipeThresholdRatio else 350f
                        alpha = (state.offsetX.value / threshold).coerceIn(0f, 1f)
                        val scale = 0.85f + 0.15f * (state.offsetX.value / threshold).coerceIn(0f, 1f)
                        scaleX = scale
                        scaleY = scale
                        rotationZ = -14f
                    }
                    .border(3.dp, colorScheme.primary, RoundedCornerShape(12.dp))
                    .background(colorScheme.primaryContainer.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "GIỮ",
                    color = colorScheme.onPrimaryContainer,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            // "XÓA" (REJECT/DELETE) Badge - Top End (Coral)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .graphicsLayer {
                        val threshold = if (state.cardWidthPx > 0f) state.cardWidthPx * swipeThresholdRatio else 350f
                        alpha = (-state.offsetX.value / threshold).coerceIn(0f, 1f)
                        val scale = 0.85f + 0.15f * (-state.offsetX.value / threshold).coerceIn(0f, 1f)
                        scaleX = scale
                        scaleY = scale
                        rotationZ = 14f
                    }
                    .border(3.dp, colorScheme.error, RoundedCornerShape(12.dp))
                    .background(colorScheme.errorContainer.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "XÓA",
                    color = colorScheme.onErrorContainer,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

/**
 * Chip component for bottom scrim metadata (Resolution, Size, Date).
 */
@Composable
private fun MetadataChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Placeholder gradient background used when rendering mock cards or while images load.
 */
@Composable
private fun MockPhotoPlaceholder(photo: PhotoItem) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        colorScheme.surfaceVariant,
                        colorScheme.surface,
                        colorScheme.surfaceVariant
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📸", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = photo.id,
                color = colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Helper to format file sizes cleanly.
 */
private fun formatCardFileSize(bytes: Long): String {
    if (bytes <= 0) return ""
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> "${(kotlin.math.round(gb * 10) / 10.0)} GB"
        mb >= 1.0 -> "${(kotlin.math.round(mb * 10) / 10.0)} MB"
        kb >= 1.0 -> "${kb.toInt()} KB"
        else -> "$bytes B"
    }
}
