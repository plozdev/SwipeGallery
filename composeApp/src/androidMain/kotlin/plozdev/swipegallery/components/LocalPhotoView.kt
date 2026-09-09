package plozdev.swipegallery.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
actual fun LocalPhotoView(
    photoUri: String,
    modifier: Modifier,
    contentScale: ContentScale
) {
    AsyncImage(
        model = photoUri,
        contentDescription = "Local Photo",
        modifier = modifier,
        contentScale = contentScale
    )
}
