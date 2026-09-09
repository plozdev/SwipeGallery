package plozdev.swipegallery.data.local

import plozdev.swipegallery.getEpochDay

interface SwipePreferences {
    fun markAsProcessed(photoId: String)
    fun removeProcessed(photoId: String)
    fun isProcessed(photoId: String): Boolean
    fun getProcessedIds(): Set<String>
    
    fun markAsKept(photoId: String)
    fun removeKept(photoId: String)
    fun getKeptIds(): Set<String>

    fun markAsPendingDeletion(photoId: String)
    fun removePendingDeletion(photoId: String)
    fun getPendingDeletionIds(): Set<String>
    fun clearAllPendingDeletions()
    
    fun setPendingDeletionsPersisted(enabled: Boolean)
    fun isPendingDeletionsPersisted(): Boolean
    
    // Streak tracking
    fun getStreakDays(currentEpochDay: Long = getEpochDay()): Int
    fun recordStreakActivity(currentEpochDay: Long = getEpochDay()): Int

    // Cleaned metrics
    fun getTotalCleanedBytes(): Long
    fun addCleanedBytes(bytes: Long)

    // Settings toggles
    fun isSafeStagingEnabled(): Boolean
    fun setSafeStagingEnabled(enabled: Boolean)

    fun isHapticsEnabled(): Boolean
    fun setHapticsEnabled(enabled: Boolean)

    fun isAutoAdvanceEnabled(): Boolean
    fun setAutoAdvanceEnabled(enabled: Boolean)

    fun isBurstGroupingEnabled(): Boolean
    fun setBurstGroupingEnabled(enabled: Boolean)

    // Cache clearing
    fun clearCache()

    fun clearAll()
}
