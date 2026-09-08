package plozdev.swipegallery.data.repository

import plozdev.swipegallery.domain.models.Album
import plozdev.swipegallery.domain.models.PhotoItem

interface PhotoRepo {
    suspend fun getAlbums(): List<Album>
    suspend fun getUnprocessedPhotos() : List<PhotoItem>
    suspend fun deletePhotos(isToDelete: List<String>) : Boolean
    suspend fun markAsKept(photoId: String)
    suspend fun unmarkAsKept(photoId: String)
    suspend fun getPendingDeletions(): List<PhotoItem>
    suspend fun markAsPendingDeletion(photoId: String)
    suspend fun removePendingDeletion(photoId: String)
    suspend fun clearAllPendingDeletions()
    
    suspend fun clearHistory()
    suspend fun setPendingDeletionsPersisted(enabled: Boolean)
    suspend fun isPendingDeletionsPersisted(): Boolean
}