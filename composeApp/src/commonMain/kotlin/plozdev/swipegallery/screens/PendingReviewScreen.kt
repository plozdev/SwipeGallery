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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import plozdev.swipegallery.components.LocalPhotoView
import plozdev.swipegallery.domain.models.PhotoItem
import plozdev.swipegallery.screens.viewModels.DiscoverViewModel

/**
 * Immutable UI State for the Pending Review Screen.
 */
data class PendingReviewUiState(
    val pendingPhotos: List<PhotoItem> = emptyList(),
    val selectedPhotoIds: Set<String> = emptySet(),
    val isDeleting: Boolean = false
)

/**
 * Pending Review Screen (Presentation Layer).
 *
 * Implements:
 * 1. Top Bar: Back navigation, title "Deletion Review", and "Deselect All" button.
 * 2. Header Summary: Storage reclaim counter with safe staging notice card.
 * 3. Media Grid: 3-column thumbnail grid with top-right deletion badges and bottom file size tags.
 * 4. Sticky Bottom Action Bar: Prominent full-width button styled with [colorScheme.errorContainer].
 * 5. Strictly consumes [MaterialTheme.colorScheme] OLED Dark tokens.
 */
@Composable
fun PendingReviewScreen(
    uiState: PendingReviewUiState,
    onBackClick: () -> Unit,
    onTogglePhotoSelection: (String) -> Unit,
    onToggleSelectAll: () -> Unit,
    onDeleteCommit: () -> Unit,
    onPhotoClick: (PhotoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val selectedCount = uiState.selectedPhotoIds.size
    val totalCount = uiState.pendingPhotos.size
    val allSelected = selectedCount == totalCount && totalCount > 0

    val selectedSizeBytes = uiState.pendingPhotos
        .filter { it.id in uiState.selectedPhotoIds }
        .sumOf { it.fileSize }
    val reclaimSizeText = formatReviewFileSize(selectedSizeBytes).ifBlank { "0 B" }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colorScheme.background,
        bottomBar = {
            // Sticky Bottom Action Bar
            Surface(
                color = colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Button(
                        onClick = onDeleteCommit,
                        enabled = selectedCount > 0 && !uiState.isDeleting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.errorContainer,
                            contentColor = colorScheme.onErrorContainer,
                            disabledContainerColor = colorScheme.surfaceVariant,
                            disabledContentColor = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (uiState.isDeleting) {
                            CircularProgressIndicator(
                                color = colorScheme.onErrorContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedCount > 0) {
                                        "Xóa vĩnh viễn $selectedCount tệp (Giải phóng $reclaimSizeText)"
                                    } else {
                                        "Chọn tệp để xóa"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
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
                .statusBarsPadding()
        ) {
            // --- 1. Top Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Deletion Review",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }

                // Deselect All / Select All Button
                TextButton(
                    onClick = onToggleSelectAll,
                    enabled = totalCount > 0
                ) {
                    Text(
                        text = if (allSelected) "Bỏ chọn tất cả" else "Chọn tất cả",
                        color = colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- 2. Header Summary & Safe Staging Notice ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Reclaim Storage Header
                Text(
                    text = "Giải phóng $reclaimSizeText từ $selectedCount tệp",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Safe Staging Notice Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Các ảnh này đang ở hàng đợi an toàn. Chỉ khi nhấn xóa, hệ thống mới kích hoạt quyền xóa thật trên máy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- 3. Media Grid: 3-column Thumbnail Grid ---
            if (uiState.pendingPhotos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "✨", fontSize = 54.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Hàng đợi rỗng",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Không có ảnh nào chờ kiểm duyệt xóa.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
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
 * 3-Column Thumbnail Item with Deletion Badge and File Size Tag.
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
            .clip(RoundedCornerShape(10.dp))
            .background(colorScheme.surfaceVariant)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colorScheme.error else colorScheme.outlineVariant,
                shape = RoundedCornerShape(10.dp)
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

        // Selection Toggle Overlay (Top Right Checkbox)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isSelected) colorScheme.error else colorScheme.background.copy(alpha = 0.6f))
                .border(1.5.dp, if (isSelected) colorScheme.error else Color.White, CircleShape)
                .clickable { onToggleSelection() },
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = colorScheme.onError,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Bottom File Size Tag
        val sizeText = formatReviewFileSize(photo.fileSize)
        if (sizeText.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, colorScheme.background.copy(alpha = 0.85f))
                        )
                    )
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sizeText,
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
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
        isDeleting = false
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
        onDeleteCommit = {
            viewModel.applyDeletionsAndKeepRemaining()
            onNavigateBack()
        },
        onPhotoClick = { photo ->
            viewModel.openFullscreen(photo)
        },
        modifier = modifier
    )
}
