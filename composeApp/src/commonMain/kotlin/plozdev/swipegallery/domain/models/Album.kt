package plozdev.swipegallery.domain.models

data class Album(
    val id: String,
    val name: String,
    val coverPhotoUri: String?,
    val photoCount: Int,
    val remainingCount: Int = photoCount
)
