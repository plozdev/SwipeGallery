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

    // Stats & Streak
    suspend fun getStreakDays(): Int
    suspend fun recordStreakActivity(): Int
    suspend fun getTotalCleanedBytes(): Long
    suspend fun addCleanedBytes(bytes: Long)
    suspend fun getTriagedCount(): Int
    suspend fun getKeptCount(): Int

    // Settings
    suspend fun isSafeStagingEnabled(): Boolean
    suspend fun setSafeStagingEnabled(enabled: Boolean)
    suspend fun isHapticsEnabled(): Boolean
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun isAutoAdvanceEnabled(): Boolean
    suspend fun setAutoAdvanceEnabled(enabled: Boolean)
    suspend fun isBurstGroupingEnabled(): Boolean
    suspend fun setBurstGroupingEnabled(enabled: Boolean)

    // Cache
    suspend fun clearCache()
}