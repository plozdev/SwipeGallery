package plozdev.swipegallery.data.repository

import plozdev.swipegallery.data.local.SwipePreferences
import plozdev.swipegallery.data.media.MediaFetcherI
import plozdev.swipegallery.domain.models.Album
import plozdev.swipegallery.domain.models.PhotoItem

class PhotoRepoImpl(
    private val mediaFetcher: MediaFetcherI,
    private val preferences: SwipePreferences
) : PhotoRepo {

    override suspend fun getAlbums(): List<Album> = mediaFetcher.getAlbums()

    override suspend fun getUnprocessedPhotos(): List<PhotoItem> {
        val allPhotos = mediaFetcher.getPhotos()
        val processedIds = preferences.getProcessedIds()
        val pendingIds = preferences.getPendingDeletionIds()
        return allPhotos.filter { it.id !in processedIds && it.id !in pendingIds }
    }

    override suspend fun getPendingDeletions(): List<PhotoItem> {
        val allPhotos = mediaFetcher.getPhotos()
        val pendingIds = preferences.getPendingDeletionIds()
        return allPhotos.filter { it.id in pendingIds }
    }

    override suspend fun markAsPendingDeletion(photoId: String) {
        preferences.markAsPendingDeletion(photoId)
    }

    override suspend fun removePendingDeletion(photoId: String) {
        preferences.removePendingDeletion(photoId)
    }

    override suspend fun clearAllPendingDeletions() {
        preferences.clearAllPendingDeletions()
    }

    override suspend fun deletePhotos(isToDelete: List<String>): Boolean {
        val success = mediaFetcher.deletePhotos(isToDelete)
        if (success) {
            isToDelete.forEach { preferences.markAsProcessed(it) }
        }
        return success
    }

    override suspend fun markAsKept(photoId: String) {
        preferences.markAsProcessed(photoId)
    }

    override suspend fun unmarkAsKept(photoId: String) {
        preferences.removeProcessed(photoId)
    }

    override suspend fun clearHistory() {
        preferences.clearAll()
    }

    override suspend fun setPendingDeletionsPersisted(enabled: Boolean) {
        preferences.setPendingDeletionsPersisted(enabled)
    }

    override suspend fun isPendingDeletionsPersisted(): Boolean {
        return preferences.isPendingDeletionsPersisted()
    }
}

