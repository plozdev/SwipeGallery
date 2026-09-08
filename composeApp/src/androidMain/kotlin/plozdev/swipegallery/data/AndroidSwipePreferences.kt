package plozdev.swipegallery.data

import android.content.Context
import plozdev.swipegallery.data.local.SwipePreferences

class AndroidSwipePreferences(private val context: Context) : SwipePreferences {
    private val sharedPrefs = context.getSharedPreferences("swipe_gallery_prefs", Context.MODE_PRIVATE)

    override fun markAsProcessed(photoId: String) {
        val ids = getProcessedIds().toMutableSet()
        ids.add(photoId)
        sharedPrefs.edit().putStringSet("processed_ids", ids).apply()
    }

    override fun removeProcessed(photoId: String) {
        val ids = getProcessedIds().toMutableSet()
        ids.remove(photoId)
        sharedPrefs.edit().putStringSet("processed_ids", ids).apply()
    }

    override fun isProcessed(photoId: String): Boolean {
        return getProcessedIds().contains(photoId)
    }

    override fun getProcessedIds(): Set<String> {
        return sharedPrefs.getStringSet("processed_ids", emptySet()) ?: emptySet()
    }

    override fun markAsPendingDeletion(photoId: String) {
        val ids = getPendingDeletionIds().toMutableSet()
        ids.add(photoId)
        sharedPrefs.edit().putStringSet("pending_ids", ids).apply()
    }

    override fun removePendingDeletion(photoId: String) {
        val ids = getPendingDeletionIds().toMutableSet()
        ids.remove(photoId)
        sharedPrefs.edit().putStringSet("pending_ids", ids).apply()
    }

    override fun getPendingDeletionIds(): Set<String> {
        return sharedPrefs.getStringSet("pending_ids", emptySet()) ?: emptySet()
    }

    override fun clearAllPendingDeletions() {
        sharedPrefs.edit().remove("pending_ids").apply()
    }

    override fun setPendingDeletionsPersisted(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("persist_pending", enabled).apply()
    }

    override fun isPendingDeletionsPersisted(): Boolean {
        return sharedPrefs.getBoolean("persist_pending", true)
    }

    override fun clearAll() {
        sharedPrefs.edit().remove("processed_ids").remove("pending_ids").apply()
    }
}
