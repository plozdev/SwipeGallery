package plozdev.swipegallery.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.ui.layout.ContentScale

@Composable
expect fun LocalPhotoView(
    photoUri: String,
    modifier: Modifier,
    contentScale: ContentScale = ContentScale.Crop
)
