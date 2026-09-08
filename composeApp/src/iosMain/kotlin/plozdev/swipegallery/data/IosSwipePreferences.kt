package plozdev.swipegallery.data

import platform.Foundation.NSUserDefaults
import plozdev.swipegallery.data.local.SwipePreferences

class IosSwipePreferences : SwipePreferences {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun markAsProcessed(photoId: String) {
        val currentIds = getProcessedIds().toMutableSet()
        currentIds.add(photoId)
        defaults.setObject(currentIds.toList(), forKey = "processed_ids")
    }

    override fun removeProcessed(photoId: String) {
        val currentIds = getProcessedIds().toMutableSet()
        currentIds.remove(photoId)
        defaults.setObject(currentIds.toList(), forKey = "processed_ids")
    }

    override fun isProcessed(photoId: String): Boolean {
        return getProcessedIds().contains(photoId)
    }

    override fun getProcessedIds(): Set<String> {
        val list = defaults.stringArrayForKey("processed_ids") as? List<*>
        return list?.filterIsInstance<String>()?.toSet() ?: emptySet()
    }

    override fun markAsPendingDeletion(photoId: String) {
        val currentIds = getPendingDeletionIds().toMutableSet()
        currentIds.add(photoId)
        defaults.setObject(currentIds.toList(), forKey = "pending_ids")
    }

    override fun removePendingDeletion(photoId: String) {
        val currentIds = getPendingDeletionIds().toMutableSet()
        currentIds.remove(photoId)
        defaults.setObject(currentIds.toList(), forKey = "pending_ids")
    }

    override fun getPendingDeletionIds(): Set<String> {
        val list = defaults.stringArrayForKey("pending_ids") as? List<*>
        return list?.filterIsInstance<String>()?.toSet() ?: emptySet()
    }

    override fun clearAllPendingDeletions() {
        defaults.removeObjectForKey("pending_ids")
    }

    override fun setPendingDeletionsPersisted(enabled: Boolean) {
        defaults.setBool(enabled, forKey = "persist_pending")
    }

    override fun isPendingDeletionsPersisted(): Boolean {
        return if (defaults.objectForKey("persist_pending") == null) true else defaults.boolForKey("persist_pending")
    }

    override fun clearAll() {
        defaults.removeObjectForKey("processed_ids")
        defaults.removeObjectForKey("pending_ids")
    }
}
