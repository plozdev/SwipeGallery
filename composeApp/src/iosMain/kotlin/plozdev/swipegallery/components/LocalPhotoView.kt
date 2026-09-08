package plozdev.swipegallery.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.pin
import org.jetbrains.skia.Image as SkiaImage
import platform.Foundation.NSData
import platform.Foundation.getBytes
import platform.Photos.*
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun LocalPhotoView(
    photoUri: String,
    modifier: Modifier,
    contentScale: ContentScale
) {
    var imageBitmap by remember(photoUri) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(photoUri) { mutableStateOf(true) }

    LaunchedEffect(photoUri) {
        val localId = photoUri.replace("phasset://", "")
        isLoading = true

        val assets = PHAsset.fetchAssetsWithLocalIdentifiers(listOf(localId), options = null)
        val asset = assets.firstObject as? PHAsset
        if (asset != null) {
            val manager = PHImageManager.defaultManager()
            val options = PHImageRequestOptions().apply {
                synchronous = false
                deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat
                networkAccessAllowed = true
            }

            manager.requestImageForAsset(
                asset = asset,
                targetSize = platform.CoreGraphics.CGSizeMake(600.0, 800.0),
                contentMode = PHImageContentModeAspectFill,
                options = options
            ) { uiImage, _ ->
                if (uiImage != null) {
                    val nsData = UIImageJPEGRepresentation(uiImage, 0.8)
                    if (nsData != null) {
                        try {
                            val bytes = nsData.toByteArray()
                            val skiaImage = SkiaImage.makeFromEncoded(bytes)
                            imageBitmap = skiaImage.toComposeImageBitmap()
                        } catch (e: Exception) {
                            // Handle parsing error
                        }
                    }
                }
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Box(modifier = modifier) {
        val bitmap = imageBitmap
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Local Photo",
                modifier = Modifier.matchParentSize(),
                contentScale = contentScale
            )
        } else {
            Box(
                modifier = Modifier.matchParentSize().background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = this.length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) {
        val pinned = bytes.pin()
        try {
            this.getBytes(pinned.addressOf(0), this.length)
        } finally {
            pinned.unpin()
        }
    }
    return bytes
}
