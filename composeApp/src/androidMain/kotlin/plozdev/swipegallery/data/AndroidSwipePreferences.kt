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

    override fun markAsKept(photoId: String) {
        val ids = getKeptIds().toMutableSet()
        ids.add(photoId)
        sharedPrefs.edit().putStringSet("kept_ids", ids).apply()
        markAsProcessed(photoId)
    }

    override fun removeKept(photoId: String) {
        val ids = getKeptIds().toMutableSet()
        ids.remove(photoId)
        sharedPrefs.edit().putStringSet("kept_ids", ids).apply()
        removeProcessed(photoId)
    }

    override fun getKeptIds(): Set<String> {
        return sharedPrefs.getStringSet("kept_ids", emptySet()) ?: emptySet()
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

    override fun getStreakDays(currentEpochDay: Long): Int {
        val lastEpochDay = sharedPrefs.getLong("last_streak_epoch_day", 0L)
        val streak = sharedPrefs.getInt("current_streak", 0)
        if (lastEpochDay == 0L) return 0
        return if (currentEpochDay == lastEpochDay || currentEpochDay == lastEpochDay + 1) {
            streak
        } else {
            0
        }
    }

    override fun recordStreakActivity(currentEpochDay: Long): Int {
        val lastEpochDay = sharedPrefs.getLong("last_streak_epoch_day", 0L)
        val currentStreak = sharedPrefs.getInt("current_streak", 0)
        val newStreak = when {
            lastEpochDay == currentEpochDay -> if (currentStreak > 0) currentStreak else 1
            lastEpochDay == currentEpochDay - 1 -> currentStreak + 1
            else -> 1
        }
        sharedPrefs.edit()
            .putInt("current_streak", newStreak)
            .putLong("last_streak_epoch_day", currentEpochDay)
            .apply()
        return newStreak
    }

    override fun getTotalCleanedBytes(): Long {
        return sharedPrefs.getLong("total_cleaned_bytes", 0L)
    }

    override fun addCleanedBytes(bytes: Long) {
        if (bytes <= 0L) return
        val current = getTotalCleanedBytes()
        sharedPrefs.edit().putLong("total_cleaned_bytes", current + bytes).apply()
    }

    override fun isSafeStagingEnabled(): Boolean {
        return sharedPrefs.getBoolean("safe_staging_enabled", isPendingDeletionsPersisted())
    }

    override fun setSafeStagingEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("safe_staging_enabled", enabled).apply()
        setPendingDeletionsPersisted(enabled)
    }

    override fun isHapticsEnabled(): Boolean {
        return sharedPrefs.getBoolean("haptics_enabled", true)
    }

    override fun setHapticsEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("haptics_enabled", enabled).apply()
    }

    override fun isAutoAdvanceEnabled(): Boolean {
        return sharedPrefs.getBoolean("auto_advance_enabled", true)
    }

    override fun setAutoAdvanceEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("auto_advance_enabled", enabled).apply()
    }

    override fun isBurstGroupingEnabled(): Boolean {
        return sharedPrefs.getBoolean("burst_grouping_enabled", true)
    }

    override fun setBurstGroupingEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("burst_grouping_enabled", enabled).apply()
    }

    override fun clearCache() {
        try {
            context.cacheDir?.deleteRecursively()
        } catch (e: Exception) {
            // Ignore cache delete error
        }
    }

    override fun clearAll() {
        sharedPrefs.edit()
            .remove("processed_ids")
            .remove("pending_ids")
            .remove("kept_ids")
            .remove("total_cleaned_bytes")
            .remove("current_streak")
            .remove("last_streak_epoch_day")
            .apply()
    }
}
