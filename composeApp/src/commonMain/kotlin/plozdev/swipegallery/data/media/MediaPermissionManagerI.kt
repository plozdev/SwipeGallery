package plozdev.swipegallery.data.media

interface MediaPermissionManagerI {
    suspend fun requestPermissions(): Boolean
    fun openAppSettings()
}