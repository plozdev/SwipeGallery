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

    override suspend fun restorePendingDeletion(photoId: String) {
        preferences.removePendingDeletion(photoId)
        preferences.removeProcessed(photoId)
        preferences.removeKept(photoId)
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
        preferences.markAsKept(photoId)
    }

    override suspend fun unmarkAsKept(photoId: String) {
        preferences.removeKept(photoId)
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

    override suspend fun getStreakDays(): Int {
        return preferences.getStreakDays()
    }

    override suspend fun recordStreakActivity(): Int {
        return preferences.recordStreakActivity()
    }

    override suspend fun getTotalCleanedBytes(): Long {
        return preferences.getTotalCleanedBytes()
    }

    override suspend fun addCleanedBytes(bytes: Long) {
        preferences.addCleanedBytes(bytes)
    }

    override suspend fun getTriagedCount(): Int {
        return preferences.getProcessedIds().size + preferences.getPendingDeletionIds().size
    }

    override suspend fun getKeptCount(): Int {
        return preferences.getKeptIds().size
    }

    override suspend fun isSafeStagingEnabled(): Boolean {
        return preferences.isSafeStagingEnabled()
    }

    override suspend fun setSafeStagingEnabled(enabled: Boolean) {
        preferences.setSafeStagingEnabled(enabled)
    }

    override suspend fun isHapticsEnabled(): Boolean {
        return preferences.isHapticsEnabled()
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        preferences.setHapticsEnabled(enabled)
    }

    override suspend fun isAutoAdvanceEnabled(): Boolean {
        return preferences.isAutoAdvanceEnabled()
    }

    override suspend fun setAutoAdvanceEnabled(enabled: Boolean) {
        preferences.setAutoAdvanceEnabled(enabled)
    }

    override suspend fun isBurstGroupingEnabled(): Boolean {
        return preferences.isBurstGroupingEnabled()
    }

    override suspend fun setBurstGroupingEnabled(enabled: Boolean) {
        preferences.setBurstGroupingEnabled(enabled)
    }

    override suspend fun clearCache() {
        preferences.clearCache()
    }
}
