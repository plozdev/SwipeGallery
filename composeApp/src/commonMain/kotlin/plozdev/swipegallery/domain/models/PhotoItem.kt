package plozdev.swipegallery.domain.models

data class PhotoItem(
    val id: String,
    val uri: String,
    val dateAdded: Long,
    val albumId: String? = null,
    val fileSize: Long = 0L,
    val width: Int? = null,
    val height: Int? = null,
    val isVideo: Boolean = false,
    val duration: Long? = null, // in milliseconds
    val mimeType: String? = null
)