package plozdev.swipegallery.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import plozdev.swipegallery.domain.models.PhotoItem

@Composable
fun FullscreenPhotoViewer(
    photo: PhotoItem,
    onClose: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var translationX by remember { mutableStateOf(0f) }
    var translationY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .pointerInput(photo.id) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            translationX = 0f
                            translationY = 0f
                        } else {
                            scale = 3f
                        }
                    },
                    onTap = { onClose() }
                )
            }
            .pointerInput(photo.id) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    if (scale > 1f) {
                        translationX += pan.x
                        translationY += pan.y
                    } else {
                        translationX = 0f
                        translationY = 0f
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        LocalPhotoView(
            photoUri = photo.uri,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = translationX,
                    translationY = translationY
                ),
            contentScale = ContentScale.Fit
        )

        // Close button in top right
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Fullscreen",
                tint = Color.White
            )
        }
    }
}
