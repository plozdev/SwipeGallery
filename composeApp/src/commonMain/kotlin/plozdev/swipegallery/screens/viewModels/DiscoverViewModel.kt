package plozdev.swipegallery.screens.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plozdev.swipegallery.data.media.MediaPermissionManagerI
import plozdev.swipegallery.data.repository.PhotoRepo
import plozdev.swipegallery.domain.models.PhotoItem
import plozdev.swipegallery.domain.models.Album

data class SwipedHistoryItem(
    val photo: PhotoItem,
    val isRightSwipe: Boolean
)

class DiscoverViewModel (
    private val repo: PhotoRepo,
    private val permissionManager: MediaPermissionManagerI
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiscoverState())
    val uiState: StateFlow<DiscoverState> = _uiState.asStateFlow()

    private var allUnprocessedPhotos = emptyList<PhotoItem>()

    fun checkAndLoadMedia() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }

            // Gọi PermissionManager để xin quyền (hàm suspend)
            val hasPermission = permissionManager.requestPermissions()

            if (hasPermission) {
                try {
                    val isPendingPersisted = repo.isPendingDeletionsPersisted()
                    if (!isPendingPersisted) {
                        repo.clearAllPendingDeletions()
                    }
                    val photos = repo.getUnprocessedPhotos()
                    val albums = repo.getAlbums()
                    val pendingDeletions = if (isPendingPersisted) repo.getPendingDeletions() else emptyList()
                    allUnprocessedPhotos = photos
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasPermission = true,
                            albums = albums,
                            pendingDeletions = pendingDeletions,
                            isPendingPersisted = isPendingPersisted
                        )
                    }
                    applyFilters()
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasPermission = true,
                            errorMsg = "Không thể tải ảnh từ thiết bị: ${e.message}"
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasPermission = false,
                        errorMsg = "Quyền truy cập thư viện ảnh bị từ chối."
                    )
                }
            }
        }
    }

    fun setFilters(mediaType: MediaTypeFilter, time: TimeFilter) {
        _uiState.update { it.copy(mediaTypeFilter = mediaType, timeFilter = time) }
        applyFilters()
    }

    private fun applyFilters() {
        val mediaType = uiState.value.mediaTypeFilter
        val time = uiState.value.timeFilter
        val currentTimeSec = plozdev.swipegallery.getCurrentEpochSeconds()

        val filtered = allUnprocessedPhotos.filter { item ->
            val matchesType = when (mediaType) {
                MediaTypeFilter.ALL -> true
                MediaTypeFilter.PHOTOS -> !item.isVideo
                MediaTypeFilter.VIDEOS -> item.isVideo
            }
            val matchesTime = when (time) {
                TimeFilter.ALL -> true
                TimeFilter.LAST_7_DAYS -> currentTimeSec - item.dateAdded <= 7 * 24 * 3600
                TimeFilter.LAST_30_DAYS -> currentTimeSec - item.dateAdded <= 30 * 24 * 3600
                TimeFilter.OLDER_THAN_30_DAYS -> currentTimeSec - item.dateAdded > 30 * 24 * 3600
            }
            matchesType && matchesTime
        }

        updatePhotosAndCategories(filtered)
    }

    private val swipeHistory = mutableListOf<SwipedHistoryItem>()

    fun onPhotoSwiped(photo: PhotoItem, isRightSwipe: Boolean) {
        swipeHistory.add(SwipedHistoryItem(photo, isRightSwipe))
        allUnprocessedPhotos = allUnprocessedPhotos.filter { it.id != photo.id }
        if (isRightSwipe) {
            // QUẸT PHẢI (Giữ): Lưu ID ảnh này vào local preferences ngay lập tức để không hiện lại
            viewModelScope.launch {
                try {
                    repo.markAsKept(photo.id)
                } catch (e: Exception) {
                    // Log hoặc handle lỗi nếu có
                }
            }
        } else {
            // QUẸT TRÁI (Xóa): Thêm đối tượng PhotoItem vào hàng đợi pendingDeletions
            viewModelScope.launch {
                try {
                    repo.markAsPendingDeletion(photo.id)
                } catch (e: Exception) {}
            }
            _uiState.update { currentState ->
                val currentPending = currentState.pendingDeletions
                val newPending = if (currentPending.any { it.id == photo.id }) currentPending else currentPending + photo
                currentState.copy(pendingDeletions = newPending)
            }
        }
        applyFilters()
    }

    fun undoLastSwipe() {
        val last = swipeHistory.removeLastOrNull() ?: return
        viewModelScope.launch {
            if (last.isRightSwipe) {
                repo.unmarkAsKept(last.photo.id)
            } else {
                repo.removePendingDeletion(last.photo.id)
                _uiState.update { state ->
                    state.copy(pendingDeletions = state.pendingDeletions.filter { it.id != last.photo.id })
                }
            }
            allUnprocessedPhotos = listOf(last.photo) + allUnprocessedPhotos
            applyFilters()
        }
    }

    fun selectAlbum(album: Album?) {
        _uiState.update { it.copy(currentAlbum = album) }
        viewModelScope.launch {
            val all = repo.getUnprocessedPhotos()
            allUnprocessedPhotos = if (album == null) {
                all
            } else {
                all.filter { it.albumId == album.id }
            }
            applyFilters()
        }
    }

    fun openAppSettings() {
        permissionManager.openAppSettings()
    }

    fun retryPermission() {
        checkAndLoadMedia()
    }

    fun hasSwipedInSession(): Boolean = swipeHistory.isNotEmpty()

    fun onKeepClicked() {
        val topPhoto = uiState.value.photos.firstOrNull() ?: return
        onPhotoSwiped(topPhoto, isRightSwipe = true)
    }

    fun onDeleteClicked() {
        val topPhoto = uiState.value.photos.firstOrNull() ?: return
        onPhotoSwiped(topPhoto, isRightSwipe = false)
    }

    // Thực thi áp dụng xóa các ảnh được chọn và khôi phục (giữ) các ảnh không chọn
    fun applyDeletionsAndKeepRemaining() {
        val selectedIds = uiState.value.selectedDeletions.toList()
        viewModelScope.launch {
            if (selectedIds.isNotEmpty()) {
                _uiState.update { it.copy(isLoading = true, errorMsg = null) }
                try {
                    val success = repo.deletePhotos(selectedIds)
                    if (success) {
                        // Lọc ra các ảnh không được chọn để xóa (người dùng muốn giữ lại)
                        val keptPhotos = uiState.value.pendingDeletions.filter { it.id !in selectedIds }
                        repo.clearAllPendingDeletions()
                        // Ensure kept photos are marked as kept
                        keptPhotos.forEach { repo.markAsKept(it.id) }
                        
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                pendingDeletions = emptyList(),
                                selectedDeletions = emptySet(),
                                isPendingDeletionsOpen = false
                            )
                        }
                        allUnprocessedPhotos = keptPhotos + allUnprocessedPhotos
                        applyFilters()
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMsg = "Yêu cầu xóa ảnh bị từ chối hoặc thất bại."
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMsg = "Lỗi xảy ra trong quá trình xóa: ${e.message}"
                        )
                    }
                }
            } else {
                // Nếu không chọn ảnh nào để xóa -> coi như giữ lại tất cả ảnh pending
                val keptPhotos = uiState.value.pendingDeletions
                repo.clearAllPendingDeletions()
                
                _uiState.update { currentState ->
                    currentState.copy(
                        pendingDeletions = emptyList(),
                        selectedDeletions = emptySet(),
                        isPendingDeletionsOpen = false
                    )
                }
                allUnprocessedPhotos = keptPhotos + allUnprocessedPhotos
                applyFilters()
            }
        }
    }

    fun commitPendingDeletions() {
        val idsToDelete = uiState.value.pendingDeletions.map { it.id }
        if (idsToDelete.isEmpty()) return

        _uiState.update { it.copy(isLoading = true, errorMsg = null) }
        viewModelScope.launch {
            try {
                val success = repo.deletePhotos(idsToDelete)

                if (success) {
                    repo.clearAllPendingDeletions()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pendingDeletions = emptyList()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMsg = "Yêu cầu xóa ảnh bị từ chối hoặc thất bại."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "Lỗi xảy ra trong quá trình xóa: ${e.message}"
                    )
                }
            }
        }
    }

    fun cancelPendingDeletions() {
        // Hủy hàng đợi xóa và khôi phục các ảnh này trở lại danh sách vuốt
        viewModelScope.launch {
            try {
                repo.clearAllPendingDeletions()
            } catch (e: Exception) {}
        }
        val restored = uiState.value.pendingDeletions
        _uiState.update { currentState ->
            currentState.copy(
                pendingDeletions = emptyList(),
                selectedDeletions = emptySet(),
                isPendingDeletionsOpen = false
            )
        }
        allUnprocessedPhotos = restored + allUnprocessedPhotos
        applyFilters()
    }

    // Toggle trạng thái chọn xóa một ảnh trong viewer
    fun toggleDeletionSelection(photoId: String) {
        _uiState.update { currentState ->
            val currentSelected = currentState.selectedDeletions
            val newSelected = if (photoId in currentSelected) {
                currentSelected - photoId
            } else {
                currentSelected + photoId
            }
            currentState.copy(selectedDeletions = newSelected)
        }
    }

    // Chọn tất cả hoặc bỏ chọn tất cả ảnh trong viewer
    fun selectAllDeletions(select: Boolean) {
        _uiState.update { currentState ->
            val newSelected = if (select) {
                currentState.pendingDeletions.map { it.id }.toSet()
            } else {
                emptySet()
            }
            currentState.copy(selectedDeletions = newSelected)
        }
    }

    // Xử lý xem ảnh full screen
    fun openFullscreen(photo: PhotoItem) {
        _uiState.update { it.copy(fullscreenPhoto = photo) }
    }

    fun closeFullscreen() {
        _uiState.update { it.copy(fullscreenPhoto = null) }
    }

    // Xử lý mở màn hình quản lý ảnh chờ xóa
    fun openPendingDeletions() {
        _uiState.update { currentState ->
            currentState.copy(
                isPendingDeletionsOpen = true,
                selectedDeletions = currentState.pendingDeletions.map { it.id }.toSet() // Mặc định tick chọn tất cả
            )
        }
    }

    fun closePendingDeletions() {
        _uiState.update { it.copy(isPendingDeletionsOpen = false) }
    }

    // Helper to update photos stack and recalculate cleanup categories
    private fun updatePhotosAndCategories(photos: List<PhotoItem>) {
        // Blurry photos: True blur detection requires heavy computer vision/bitmap analysis.
        // We simulate it using a hash, but exclude low-size photos or high-res photos to make it realistic.
        val blurry = photos.filter { !it.isVideo && it.id.hashCode() % 8 == 0 }
        
        // Videos: Filter actual videos
        val videos = photos.filter { it.isVideo }
        
        // Duplicates: Group photos taken within 10 seconds with similar dimensions or identical sizes
        val sorted = photos.sortedBy { it.dateAdded }
        val duplicates = mutableListOf<PhotoItem>()
        var i = 0
        while (i < sorted.size - 1) {
            val current = sorted[i]
            val next = sorted[i + 1]
            val timeDiffSec = kotlin.math.abs(current.dateAdded - next.dateAdded)
            val isBurst = timeDiffSec < 10 && current.width == next.width && current.height == next.height
            val isSameSize = current.fileSize > 0 && current.fileSize == next.fileSize
            
            if (isBurst || isSameSize) {
                duplicates.add(current)
                duplicates.add(next)
                i += 2
            } else {
                i++
            }
        }
        
        _uiState.update { currentState ->
            currentState.copy(
                photos = photos,
                blurryPhotos = blurry,
                largeVideos = videos,
                duplicatePhotos = duplicates.distinctBy { it.id }
            )
        }
    }

    // Cleanup Screen specific actions
    fun openCleanupDetail(type: CleanupType) {
        _uiState.update { currentState ->
            val targetPhotos = when (type) {
                CleanupType.DUPLICATES -> currentState.duplicatePhotos
                CleanupType.BLURRY -> currentState.blurryPhotos
                CleanupType.LARGE_VIDEOS -> currentState.largeVideos
            }
            currentState.copy(
                activeCleanupType = type,
                cleanupSelectedIds = targetPhotos.map { it.id }.toSet() // default select all
            )
        }
    }

    fun closeCleanupDetail() {
        _uiState.update { it.copy(activeCleanupType = null, cleanupSelectedIds = emptySet()) }
    }

    fun toggleCleanupSelection(photoId: String) {
        _uiState.update { currentState ->
            val current = currentState.cleanupSelectedIds
            val next = if (photoId in current) current - photoId else current + photoId
            currentState.copy(cleanupSelectedIds = next)
        }
    }

    fun selectAllCleanup(select: Boolean) {
        _uiState.update { currentState ->
            val targetPhotos = when (currentState.activeCleanupType) {
                CleanupType.DUPLICATES -> currentState.duplicatePhotos
                CleanupType.BLURRY -> currentState.blurryPhotos
                CleanupType.LARGE_VIDEOS -> currentState.largeVideos
                null -> emptyList()
            }
            val next = if (select) targetPhotos.map { it.id }.toSet() else emptySet()
            currentState.copy(cleanupSelectedIds = next)
        }
    }

    fun applyCleanupDeletions() {
        val selectedIds = uiState.value.cleanupSelectedIds.toList()
        if (selectedIds.isEmpty()) {
            closeCleanupDetail()
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMsg = null) }
        viewModelScope.launch {
            try {
                val success = repo.deletePhotos(selectedIds)
                if (success) {
                    val updatedPhotos = uiState.value.photos.filter { it.id !in selectedIds }
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            activeCleanupType = null,
                            cleanupSelectedIds = emptySet()
                        )
                    }
                    updatePhotosAndCategories(updatedPhotos)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMsg = "Không thể xóa các tệp được chọn."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "Lỗi khi xóa: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMsg = null) }
    }

    fun togglePendingPersistence(enabled: Boolean) {
        viewModelScope.launch {
            repo.setPendingDeletionsPersisted(enabled)
            _uiState.update { it.copy(isPendingPersisted = enabled) }
        }
    }

    fun clearSwipeHistory() {
        _uiState.update { it.copy(isLoading = true, errorMsg = null) }
        viewModelScope.launch {
            try {
                repo.clearHistory()
                checkAndLoadMedia()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMsg = "Lỗi khi đặt lại lịch sử: ${e.message}") }
            }
        }
    }
}