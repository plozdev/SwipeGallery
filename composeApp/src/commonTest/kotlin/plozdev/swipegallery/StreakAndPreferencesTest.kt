package plozdev.swipegallery

import plozdev.swipegallery.data.local.SwipePreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FakeSwipePreferences : SwipePreferences {
    private val processedIds = mutableSetOf<String>()
    private val keptIds = mutableSetOf<String>()
    private val pendingIds = mutableSetOf<String>()
    private var persistPending = true
    private var safeStaging = true
    private var haptics = true
    private var autoAdvance = true
    private var burstGrouping = true
    private var totalCleanedBytes = 0L
    private var currentStreak = 0
    private var lastStreakEpochDay = 0L

    override fun markAsProcessed(photoId: String) {
        processedIds.add(photoId)
    }

    override fun removeProcessed(photoId: String) {
        processedIds.remove(photoId)
    }

    override fun isProcessed(photoId: String): Boolean = photoId in processedIds

    override fun getProcessedIds(): Set<String> = processedIds.toSet()

    override fun markAsKept(photoId: String) {
        keptIds.add(photoId)
        markAsProcessed(photoId)
    }

    override fun removeKept(photoId: String) {
        keptIds.remove(photoId)
        removeProcessed(photoId)
    }

    override fun getKeptIds(): Set<String> = keptIds.toSet()

    override fun markAsPendingDeletion(photoId: String) {
        pendingIds.add(photoId)
    }

    override fun removePendingDeletion(photoId: String) {
        pendingIds.remove(photoId)
    }

    override fun getPendingDeletionIds(): Set<String> = pendingIds.toSet()

    override fun clearAllPendingDeletions() {
        pendingIds.clear()
    }

    override fun setPendingDeletionsPersisted(enabled: Boolean) {
        persistPending = enabled
    }

    override fun isPendingDeletionsPersisted(): Boolean = persistPending

    override fun getStreakDays(currentEpochDay: Long): Int {
        if (lastStreakEpochDay == 0L) return 0
        return if (currentEpochDay == lastStreakEpochDay || currentEpochDay == lastStreakEpochDay + 1) {
            currentStreak
        } else {
            0
        }
    }

    override fun recordStreakActivity(currentEpochDay: Long): Int {
        val newStreak = when {
            lastStreakEpochDay == currentEpochDay -> if (currentStreak > 0) currentStreak else 1
            lastStreakEpochDay == currentEpochDay - 1 -> currentStreak + 1
            else -> 1
        }
        currentStreak = newStreak
        lastStreakEpochDay = currentEpochDay
        return newStreak
    }

    override fun getTotalCleanedBytes(): Long = totalCleanedBytes

    override fun addCleanedBytes(bytes: Long) {
        if (bytes <= 0L) return
        totalCleanedBytes += bytes
    }

    override fun isSafeStagingEnabled(): Boolean = safeStaging

    override fun setSafeStagingEnabled(enabled: Boolean) {
        safeStaging = enabled
        persistPending = enabled
    }

    override fun isHapticsEnabled(): Boolean = haptics

    override fun setHapticsEnabled(enabled: Boolean) {
        haptics = enabled
    }

    override fun isAutoAdvanceEnabled(): Boolean = autoAdvance

    override fun setAutoAdvanceEnabled(enabled: Boolean) {
        autoAdvance = enabled
    }

    override fun isBurstGroupingEnabled(): Boolean = burstGrouping

    override fun setBurstGroupingEnabled(enabled: Boolean) {
        burstGrouping = enabled
    }

    override fun clearCache() {}

    override fun clearAll() {
        processedIds.clear()
        keptIds.clear()
        pendingIds.clear()
        totalCleanedBytes = 0L
        currentStreak = 0
        lastStreakEpochDay = 0L
    }
}

class StreakAndPreferencesTest {

    @Test
    fun testInitialStreakIsZero() {
        val prefs = FakeSwipePreferences()
        assertEquals(0, prefs.getStreakDays(currentEpochDay = 1000L))
    }

    @Test
    fun testFirstActivitySetsStreakToOne() {
        val prefs = FakeSwipePreferences()
        val result = prefs.recordStreakActivity(currentEpochDay = 1000L)
        assertEquals(1, result)
        assertEquals(1, prefs.getStreakDays(currentEpochDay = 1000L))
    }

    @Test
    fun testMultipleActivitiesOnSameDayDoesNotIncreaseStreak() {
        val prefs = FakeSwipePreferences()
        prefs.recordStreakActivity(currentEpochDay = 1000L)
        val secondResult = prefs.recordStreakActivity(currentEpochDay = 1000L)
        assertEquals(1, secondResult)
        assertEquals(1, prefs.getStreakDays(currentEpochDay = 1000L))
    }

    @Test
    fun testConsecutiveDayIncrementsStreak() {
        val prefs = FakeSwipePreferences()
        prefs.recordStreakActivity(currentEpochDay = 1000L)
        
        // Next day before activity: still alive (yesterday was active)
        assertEquals(1, prefs.getStreakDays(currentEpochDay = 1001L))
        
        // User records activity on next day
        val day2Result = prefs.recordStreakActivity(currentEpochDay = 1001L)
        assertEquals(2, day2Result)
        assertEquals(2, prefs.getStreakDays(currentEpochDay = 1001L))

        // Day 3 activity
        val day3Result = prefs.recordStreakActivity(currentEpochDay = 1002L)
        assertEquals(3, day3Result)
        assertEquals(3, prefs.getStreakDays(currentEpochDay = 1002L))
    }

    @Test
    fun testMissingDayResetsStreak() {
        val prefs = FakeSwipePreferences()
        prefs.recordStreakActivity(currentEpochDay = 1000L)
        prefs.recordStreakActivity(currentEpochDay = 1001L)
        assertEquals(2, prefs.getStreakDays(currentEpochDay = 1001L))

        // Skip day 1002 completely. On day 1003:
        // Before activity: streak should be 0 because streak expired
        assertEquals(0, prefs.getStreakDays(currentEpochDay = 1003L))

        // User does activity on day 1003: restarts at 1
        val restartResult = prefs.recordStreakActivity(currentEpochDay = 1003L)
        assertEquals(1, restartResult)
        assertEquals(1, prefs.getStreakDays(currentEpochDay = 1003L))
    }

    @Test
    fun testCleanedBytesAccumulation() {
        val prefs = FakeSwipePreferences()
        assertEquals(0L, prefs.getTotalCleanedBytes())

        prefs.addCleanedBytes(1024L * 1024L * 50L) // 50 MB
        assertEquals(52428800L, prefs.getTotalCleanedBytes())

        prefs.addCleanedBytes(1024L * 1024L * 25L) // +25 MB
        assertEquals(78643200L, prefs.getTotalCleanedBytes())

        prefs.addCleanedBytes(-100L) // should not decrease
        assertEquals(78643200L, prefs.getTotalCleanedBytes())
    }

    @Test
    fun testTriagedAndKeptRatioCalculation() {
        val prefs = FakeSwipePreferences()
        prefs.markAsKept("photo_1")
        prefs.markAsKept("photo_2")
        prefs.markAsKept("photo_3")
        prefs.markAsPendingDeletion("photo_4")

        val triaged = prefs.getProcessedIds().size + prefs.getPendingDeletionIds().size
        val kept = prefs.getKeptIds().size
        assertEquals(4, triaged)
        assertEquals(3, kept)

        val ratio = (kept * 100) / triaged
        assertEquals(75, ratio)
    }

    @Test
    fun testSettingToggles() {
        val prefs = FakeSwipePreferences()
        assertTrue(prefs.isSafeStagingEnabled())
        assertTrue(prefs.isHapticsEnabled())
        assertTrue(prefs.isAutoAdvanceEnabled())
        assertTrue(prefs.isBurstGroupingEnabled())

        prefs.setSafeStagingEnabled(false)
        assertFalse(prefs.isSafeStagingEnabled())
        assertFalse(prefs.isPendingDeletionsPersisted())

        prefs.setHapticsEnabled(false)
        assertFalse(prefs.isHapticsEnabled())

        prefs.setAutoAdvanceEnabled(false)
        assertFalse(prefs.isAutoAdvanceEnabled())

        prefs.setBurstGroupingEnabled(false)
        assertFalse(prefs.isBurstGroupingEnabled())
    }

    @Test
    fun testClearAllResetsEverything() {
        val prefs = FakeSwipePreferences()
        prefs.recordStreakActivity(100L)
        prefs.addCleanedBytes(10000L)
        prefs.markAsKept("p1")
        prefs.markAsPendingDeletion("p2")

        prefs.clearAll()
        assertEquals(0, prefs.getStreakDays(100L))
        assertEquals(0L, prefs.getTotalCleanedBytes())
        assertTrue(prefs.getProcessedIds().isEmpty())
        assertTrue(prefs.getKeptIds().isEmpty())
        assertTrue(prefs.getPendingDeletionIds().isEmpty())
    }

    @Test
    fun testPendingDeletionRestoreLogic() {
        val prefs = FakeSwipePreferences()
        prefs.markAsPendingDeletion("photo_1")
        prefs.markAsPendingDeletion("photo_2")
        
        assertEquals(setOf("photo_1", "photo_2"), prefs.getPendingDeletionIds())
        assertFalse(prefs.isProcessed("photo_1"))
        assertFalse(prefs.isProcessed("photo_2"))

        // Khôi phục photo_1 ra khỏi hàng chờ xóa
        prefs.removePendingDeletion("photo_1")
        prefs.removeProcessed("photo_1")
        prefs.removeKept("photo_1")

        // Xác nhận photo_1 không còn trong pending và hoàn toàn unprocessed
        assertEquals(setOf("photo_2"), prefs.getPendingDeletionIds())
        assertFalse(prefs.isProcessed("photo_1"))
        assertFalse(prefs.getKeptIds().contains("photo_1"))
    }
}
