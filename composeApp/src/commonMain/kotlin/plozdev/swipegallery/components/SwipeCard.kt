package plozdev.swipegallery.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import plozdev.swipegallery.domain.models.PhotoItem
import kotlin.math.roundToInt

@Composable
fun SwipeCard(
    photo: PhotoItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onSwipedRight: () -> Unit,
    onSwipedLeft: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Reset offset when photo changes using photo.id as key
    val offsetX = remember(photo.id) { Animatable(0f) }
    val offsetY = remember(photo.id) { Animatable(0f) }

    val density = LocalDensity.current
    val swipeThreshold = with(density) { 140.dp.toPx() }

    // Dynamic rotation angle based on horizontal drag
    val rotation = offsetX.value / 25f

    // Calculate badge alpha values (max 1.0)
    val rightBadgeAlpha = (offsetX.value / swipeThreshold).coerceIn(0f, 1f)
    val leftBadgeAlpha = (-offsetX.value / swipeThreshold).coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .graphicsLayer {
                rotationZ = rotation
            }
            .pointerInput(photo.id) {
                detectTapGestures(
                    onTap = { onClick() }
                )
            }
            .pointerInput(photo.id) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            if (offsetX.value > swipeThreshold) {
                                // Fly away right
                                launch { offsetX.animateTo(1000f, tween(300)) }
                                launch { offsetY.animateTo(offsetY.value * 2f, tween(300)) }
                                onSwipedRight()
                            } else if (offsetX.value < -swipeThreshold) {
                                // Fly away left
                                launch { offsetX.animateTo(-1000f, tween(300)) }
                                launch { offsetY.animateTo(offsetY.value * 2f, tween(300)) }
                                onSwipedLeft()
                            } else {
                                // Spring bounce back to center
                                launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                                launch {
                                    offsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                            }
                        }
                    }
                )
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LocalPhotoView(
                photoUri = photo.uri,
                modifier = Modifier.fillMaxSize()
            )

            // Bottom Gradient Overlay for Details
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xE60F172A))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    Text(
                        text = plozdev.swipegallery.formatEpochSeconds(photo.dateAdded).uppercase(),
                        color = Color(0xFF818CF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val sizeText = formatSizeBytes(photo.fileSize)
                    val detailsText = if (photo.isVideo) {
                        val durationSeconds = (photo.duration ?: 0L) / 1000
                        val min = durationSeconds / 60
                        val sec = durationSeconds % 60
                        "Video • ${min}:${sec.toString().padStart(2, '0')} ($sizeText)"
                    } else {
                        val dims = if (photo.width != null && photo.height != null) "${photo.width}x${photo.height}" else ""
                        if (dims.isNotEmpty()) "Ảnh • $dims ($sizeText)" else "Ảnh • $sizeText"
                    }

                    Text(
                        text = detailsText,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Overlays: "GIỮ" and "XÓA" badges
            if (rightBadgeAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(24.dp)
                        .alpha(rightBadgeAlpha)
                        .border(4.dp, Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                        .background(Color(0x99000000), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "GIỮ",
                        color = Color(0xFF4CAF50),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (leftBadgeAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .alpha(leftBadgeAlpha)
                        .border(4.dp, Color(0xFFF44336), RoundedCornerShape(12.dp))
                        .background(Color(0x99000000), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "XÓA",
                        color = Color(0xFFF44336),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatSizeBytes(bytes: Long): String {
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
