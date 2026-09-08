package plozdev.swipegallery.data.local

interface SwipePreferences {
    fun markAsProcessed(photoId: String)
    fun removeProcessed(photoId: String)
    fun isProcessed(photoId: String): Boolean
    fun getProcessedIds(): Set<String>
    
    fun markAsPendingDeletion(photoId: String)
    fun removePendingDeletion(photoId: String)
    fun getPendingDeletionIds(): Set<String>
    fun clearAllPendingDeletions()
    
    fun setPendingDeletionsPersisted(enabled: Boolean)
    fun isPendingDeletionsPersisted(): Boolean
    
    fun clearAll()
}
