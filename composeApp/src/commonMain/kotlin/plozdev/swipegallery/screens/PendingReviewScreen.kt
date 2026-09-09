package plozdev.swipegallery.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import plozdev.swipegallery.PlatformBackHandler
import plozdev.swipegallery.components.LocalPhotoView
import plozdev.swipegallery.domain.models.PhotoItem
import plozdev.swipegallery.screens.viewModels.DiscoverViewModel

/**
 * Immutable UI State for the Pending Review Screen.
 */
data class PendingReviewUiState(
    val pendingPhotos: List<PhotoItem> = emptyList(),
    val selectedPhotoIds: Set<String> = emptySet(),
    val isLoading: Boolean = false
)

/**
 * Pending Review Screen (Presentation Layer).
 *
 * Implements:
 * 1. Back Navigation: Tích hợp PlatformBackHandler và phím mũi tên quay lại Album an toàn.
 * 2. Ngôn ngữ & Tiêu đề: "Duyệt Xóa Ảnh" đồng bộ tiếng Việt 100%.
 * 3. Thẻ tóm tắt thông minh: Hiển thị số lượng đã chọn, dung lượng giải phóng và giải thích hàng đợi.
 * 4. Lưới ảnh 3 cột: Bo góc 12dp, checkbox chọn ảnh dễ bấm, tag dung lượng ảnh mờ tinh gọn.
 * 5. Thanh tác vụ kép (Dual Action Bar):
 *    - "Khôi phục (X)": Đưa ảnh ra khỏi hàng chờ, đánh dấu giữ lại an toàn.
 *    - "Xóa (X)": Mở dialog xác nhận trước khi xóa vĩnh viễn khỏi thiết bị.
 */
@Composable
fun PendingReviewScreen(
    uiState: PendingReviewUiState,
    onBackClick: () -> Unit,
    onTogglePhotoSelection: (String) -> Unit,
    onToggleSelectAll: () -> Unit,
    onRestoreSelected: () -> Unit,
    onDeleteCommit: () -> Unit,
    onPhotoClick: (PhotoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept system back press/gesture so app doesn't exit
    PlatformBackHandler(enabled = true, onBack = onBackClick)

    val colorScheme = MaterialTheme.colorScheme

    val selectedCount = uiState.selectedPhotoIds.size
    val totalCount = uiState.pendingPhotos.size
    val allSelected = selectedCount == totalCount && totalCount > 0

    val selectedSizeBytes = uiState.pendingPhotos
        .filter { it.id in uiState.selectedPhotoIds }
        .sumOf { it.fileSize }
    val reclaimSizeText = formatReviewFileSize(selectedSizeBytes).ifBlank { "0 B" }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Dialog xác nhận xóa vĩnh viễn
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Xác nhận xóa vĩnh viễn",
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn xóa vĩnh viễn $selectedCount ảnh ($reclaimSizeText) khỏi thiết bị? Thao tác này không thể hoàn tác.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteCommit()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.error,
                        contentColor = colorScheme.onError
                    )
                ) {
                    Text("Xóa vĩnh viễn", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Hủy", color = colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Duyệt Xóa Ảnh",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (totalCount > 0) {
                        TextButton(
                            onClick = onToggleSelectAll,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (allSelected) "Bỏ chọn tất cả" else "Chọn tất cả",
                                color = colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Thanh công cụ kép Sticky Bottom Action Bar
            Surface(
                color = colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Nút Khôi phục (Đưa ra khỏi hàng chờ và Giữ lại)
                        FilledTonalButton(
                            onClick = onRestoreSelected,
                            enabled = selectedCount > 0 && !uiState.isLoading,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = colorScheme.secondaryContainer,
                                contentColor = colorScheme.onSecondaryContainer,
                                disabledContainerColor = colorScheme.surfaceVariant,
                                disabledContentColor = colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedCount > 0) "Khôi phục ($selectedCount)" else "Khôi phục",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }

                        // 2. Nút Xóa vĩnh viễn
                        Button(
                            onClick = { showDeleteConfirmDialog = true },
                            enabled = selectedCount > 0 && !uiState.isLoading,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.errorContainer,
                                contentColor = colorScheme.onErrorContainer,
                                disabledContainerColor = colorScheme.surfaceVariant,
                                disabledContentColor = colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    color = colorScheme.onErrorContainer,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (selectedCount > 0) "Xóa ($selectedCount)" else "Xóa",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- 2. Thẻ Tóm Tắt Thông Minh & An Toàn ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Đã chọn: ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$selectedCount / $totalCount tệp",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                        }
                        Surface(
                            color = colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Giải phóng $reclaimSizeText",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCount > 0) colorScheme.primary else colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ảnh đang ở hàng đợi an toàn. Khôi phục để giữ lại hoặc Xóa vĩnh viễn khỏi máy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- 3. Lưới Ảnh 3 Cột ---
            if (uiState.pendingPhotos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "✨", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Hàng đợi trống",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Không có ảnh nào trong hàng chờ xóa.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        FilledTonalButton(onClick = onBackClick) {
                            Text("Quay về Album")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.pendingPhotos, key = { it.id }) { photo ->
                        val isSelected = photo.id in uiState.selectedPhotoIds

                        ReviewThumbnailItem(
                            photo = photo,
                            isSelected = isSelected,
                            onToggleSelection = { onTogglePhotoSelection(photo.id) },
                            onPhotoClick = { onPhotoClick(photo) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Thumbnail Item với viền chọn nổi bật, checkbox góc trên và pill tag dung lượng.
 */
@Composable
private fun ReviewThumbnailItem(
    photo: PhotoItem,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onPhotoClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceVariant)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colorScheme.primary else colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onPhotoClick() }
    ) {
        // Thumbnail Image
        if (photo.uri.isNotBlank()) {
            LocalPhotoView(
                photoUri = photo.uri,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Lớp phủ nhẹ nếu không được chọn
        if (!isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
        }

        // Checkbox chọn / bỏ chọn ở góc trên bên phải với vùng bấm 40dp
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(40.dp)
                .clickable { onToggleSelection() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) colorScheme.primary else Color.Black.copy(alpha = 0.5f))
                    .border(1.5.dp, if (isSelected) colorScheme.primary else Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Đã chọn",
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Bottom File Size Pill Tag
        val sizeText = formatReviewFileSize(photo.fileSize)
        if (sizeText.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sizeText,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

private fun formatReviewFileSize(bytes: Long): String {
    if (bytes <= 0) return ""
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> "${(kotlin.math.round(gb * 10) / 10.0)} GB"
        mb >= 1.0 -> "${(kotlin.math.round(mb * 10) / 10.0)} MB"
        kb >= 1.0 -> "${kb.toInt()} KB"
        else -> "$bytes B"
    }
}

// --------------------------------------------------------------------------------------
// ViewModel-Connected Overload for SwipeGallery Integration
// --------------------------------------------------------------------------------------

@Composable
fun PendingReviewScreen(
    viewModel: DiscoverViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val presentationState = PendingReviewUiState(
        pendingPhotos = uiState.pendingDeletions,
        selectedPhotoIds = uiState.selectedDeletions,
        isLoading = uiState.isLoading
    )

    PendingReviewScreen(
        uiState = presentationState,
        onBackClick = {
            viewModel.closePendingDeletions()
            onNavigateBack()
        },
        onTogglePhotoSelection = { photoId ->
            viewModel.toggleDeletionSelection(photoId)
        },
        onToggleSelectAll = {
            val allSelected = uiState.selectedDeletions.size == uiState.pendingDeletions.size
            viewModel.selectAllDeletions(!allSelected)
        },
        onRestoreSelected = {
            viewModel.restoreSelectedPendingDeletions()
        },
        onDeleteCommit = {
            viewModel.deleteSelectedPendingDeletions()
        },
        onPhotoClick = { photo ->
            viewModel.openFullscreen(photo)
        },
        modifier = modifier
    )
}
