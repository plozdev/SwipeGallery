package plozdev.swipegallery.screens.viewModels

import plozdev.swipegallery.domain.models.Album
import plozdev.swipegallery.domain.models.PhotoItem

enum class CleanupType {
    DUPLICATES, BLURRY, LARGE_VIDEOS
}

enum class MediaTypeFilter(val label: String) {
    ALL("Tất cả"),
    PHOTOS("Chỉ ảnh"),
    VIDEOS("Chỉ video")
}

enum class TimeFilter(val label: String) {
    ALL("Tất cả thời gian"),
    LAST_7_DAYS("7 ngày gần đây"),
    LAST_30_DAYS("30 ngày gần đây"),
    OLDER_THAN_30_DAYS("Hơn 30 ngày trước")
}

data class DiscoverState(
    val isLoading: Boolean = true,
    val hasPermission: Boolean = false,
    val photos: List<PhotoItem> = emptyList(),
    val albums: List<Album> = emptyList(),
    val pendingDeletions: List<PhotoItem> = emptyList(),
    val currentAlbum: Album? = null,
    val errorMsg: String? = null,
    val fullscreenPhoto: PhotoItem? = null,
    val isPendingDeletionsOpen: Boolean = false,
    val selectedDeletions: Set<String> = emptySet(),
    val mediaTypeFilter: MediaTypeFilter = MediaTypeFilter.ALL,
    val timeFilter: TimeFilter = TimeFilter.ALL,
    val isPendingPersisted: Boolean = true,
    val safeStagingEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val autoAdvanceEnabled: Boolean = true,
    val burstGroupingEnabled: Boolean = true,
    
    // Cleanup state
    val duplicatePhotos: List<PhotoItem> = emptyList(),
    val blurryPhotos: List<PhotoItem> = emptyList(),
    val largeVideos: List<PhotoItem> = emptyList(),
    val cleanupSelectedIds: Set<String> = emptySet(),
    val activeCleanupType: CleanupType? = null,
    
    // Sự kiện hoàn tác để animate kéo thẻ ảnh quay trở lại
    val lastUndoneEvent: UndoneSwipeEvent? = null,
    val canUndo: Boolean = false
)

data class UndoneSwipeEvent(
    val photoId: String,
    val wasRightSwipe: Boolean,
    val eventId: Long
)

