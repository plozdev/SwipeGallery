package plozdev.swipegallery.data

import platform.Foundation.NSUserDefaults
import platform.Foundation.NSFileManager
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSUserDomainMask
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

    override fun markAsKept(photoId: String) {
        val currentIds = getKeptIds().toMutableSet()
        currentIds.add(photoId)
        defaults.setObject(currentIds.toList(), forKey = "kept_ids")
        markAsProcessed(photoId)
    }

    override fun removeKept(photoId: String) {
        val currentIds = getKeptIds().toMutableSet()
        currentIds.remove(photoId)
        defaults.setObject(currentIds.toList(), forKey = "kept_ids")
        removeProcessed(photoId)
    }

    override fun getKeptIds(): Set<String> {
        val list = defaults.stringArrayForKey("kept_ids") as? List<*>
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

    override fun getStreakDays(currentEpochDay: Long): Int {
        val lastEpochDay = defaults.integerForKey("last_streak_epoch_day")
        val streak = defaults.integerForKey("current_streak").toInt()
        if (lastEpochDay == 0L) return 0
        return if (currentEpochDay == lastEpochDay || currentEpochDay == lastEpochDay + 1) {
            streak
        } else {
            0
        }
    }

    override fun recordStreakActivity(currentEpochDay: Long): Int {
        val lastEpochDay = defaults.integerForKey("last_streak_epoch_day")
        val currentStreak = defaults.integerForKey("current_streak").toInt()
        val newStreak = when {
            lastEpochDay == currentEpochDay -> if (currentStreak > 0) currentStreak else 1
            lastEpochDay == currentEpochDay - 1 -> currentStreak + 1
            else -> 1
        }
        defaults.setInteger(newStreak.toLong(), forKey = "current_streak")
        defaults.setInteger(currentEpochDay, forKey = "last_streak_epoch_day")
        return newStreak
    }

    override fun getTotalCleanedBytes(): Long {
        return defaults.integerForKey("total_cleaned_bytes")
    }

    override fun addCleanedBytes(bytes: Long) {
        if (bytes <= 0L) return
        val current = getTotalCleanedBytes()
        defaults.setInteger(current + bytes, forKey = "total_cleaned_bytes")
    }

    override fun isSafeStagingEnabled(): Boolean {
        return if (defaults.objectForKey("safe_staging_enabled") == null) {
            isPendingDeletionsPersisted()
        } else {
            defaults.boolForKey("safe_staging_enabled")
        }
    }

    override fun setSafeStagingEnabled(enabled: Boolean) {
        defaults.setBool(enabled, forKey = "safe_staging_enabled")
        setPendingDeletionsPersisted(enabled)
    }

    override fun isHapticsEnabled(): Boolean {
        return if (defaults.objectForKey("haptics_enabled") == null) true else defaults.boolForKey("haptics_enabled")
    }

    override fun setHapticsEnabled(enabled: Boolean) {
        defaults.setBool(enabled, forKey = "haptics_enabled")
    }

    override fun isAutoAdvanceEnabled(): Boolean {
        return if (defaults.objectForKey("auto_advance_enabled") == null) true else defaults.boolForKey("auto_advance_enabled")
    }

    override fun setAutoAdvanceEnabled(enabled: Boolean) {
        defaults.setBool(enabled, forKey = "auto_advance_enabled")
    }

    override fun isBurstGroupingEnabled(): Boolean {
        return if (defaults.objectForKey("burst_grouping_enabled") == null) true else defaults.boolForKey("burst_grouping_enabled")
    }

    override fun setBurstGroupingEnabled(enabled: Boolean) {
        defaults.setBool(enabled, forKey = "burst_grouping_enabled")
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    override fun clearCache() {
        try {
            val tempPath = platform.Foundation.NSTemporaryDirectory()
            platform.Foundation.NSFileManager.defaultManager.removeItemAtPath(tempPath, null)
        } catch (e: Exception) {
            // Ignore cache delete error
        }
    }

    override fun clearAll() {
        defaults.removeObjectForKey("processed_ids")
        defaults.removeObjectForKey("pending_ids")
        defaults.removeObjectForKey("kept_ids")
        defaults.removeObjectForKey("total_cleaned_bytes")
        defaults.removeObjectForKey("current_streak")
        defaults.removeObjectForKey("last_streak_epoch_day")
    }
}
