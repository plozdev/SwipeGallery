package plozdev.swipegallery.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import plozdev.swipegallery.components.LocalPhotoView
import plozdev.swipegallery.domain.models.Album
import plozdev.swipegallery.screens.viewModels.DiscoverViewModel

/**
 * Immutable UI State cho màn hình Albums.
 */
data class AlbumsUiState(
    val albums: List<Album> = emptyList(),
    val pendingDeletionsCount: Int = 0,
    val pendingDeletionsSizeBytes: Long = 0L,
    val hasPermission: Boolean = true,
    val isLoading: Boolean = false
)

/**
 * Màn hình Albums tóm gọn theo đúng thiết kế mẫu:
 * 1. Tiêu đề "Albums" và số lượng thư mục.
 * 2. Thẻ "Ảnh Chờ Xóa" nổi bật ở trên cùng kèm nút "Xem lại ->".
 * 3. Lưới 2 cột các Album máy (Camera, Screenshots, Facebook, Messenger,...).
 */
@Composable
fun AlbumScreen(
    uiState: AlbumsUiState,
    onReviewPendingClick: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // --- Top Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Albums",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = colorScheme.onBackground
            )

            // Số lượng thư mục
            Surface(
                color = colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant)
            ) {
                Text(
                    text = "${uiState.albums.size} thư mục",
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // --- Content Grid ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Thẻ Ảnh Chờ Xóa (Full Width Staging Banner)
            item(span = { GridItemSpan(2) }) {
                PendingDeletionsStagingBanner(
                    pendingCount = uiState.pendingDeletionsCount,
                    reclaimSizeBytes = uiState.pendingDeletionsSizeBytes,
                    onReviewClick = onReviewPendingClick
                )
            }

            // Thông báo nếu chưa được cấp quyền
            if (!uiState.hasPermission) {
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .border(1.dp, colorScheme.outlineVariant, MaterialTheme.shapes.extraLarge),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.8f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Chưa cấp quyền truy cập ảnh",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Vui lòng cấp quyền ở mục Khám phá để hiển thị toàn bộ album trên thiết bị.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 2. Lưới Album máy
            items(uiState.albums, key = { it.id }) { album ->
                AlbumFolderCard(
                    album = album,
                    onClick = { onAlbumClick(album) }
                )
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Thẻ Hàng Chờ Xóa nổi bật ở đầu màn hình Albums.
 */
@Composable
private fun PendingDeletionsStagingBanner(
    pendingCount: Int,
    reclaimSizeBytes: Long,
    onReviewClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val hasPending = pendingCount > 0
    val reclaimSizeText = formatAlbumFileSize(reclaimSizeBytes).ifBlank { "0 B" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (hasPending) colorScheme.error.copy(alpha = 0.4f) else colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.extraLarge
            ),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (hasPending) colorScheme.errorContainer.copy(alpha = 0.35f) else colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon thùng rác
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (hasPending) colorScheme.errorContainer else colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Ảnh chờ xóa",
                        tint = if (hasPending) colorScheme.onErrorContainer else colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "Ảnh Chờ Xóa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (hasPending) "$pendingCount tệp • $reclaimSizeText" else "Hàng đợi trống",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hasPending) colorScheme.onErrorContainer else colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Nút "Xem lại ->"
            Button(
                onClick = onReviewClick,
                enabled = hasPending,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasPending) colorScheme.error else colorScheme.surface,
                    contentColor = if (hasPending) colorScheme.onError else colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(percent = 50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Xem lại",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Thẻ Folder tỷ lệ 1:1 có ảnh bìa.
 */
@Composable
private fun AlbumFolderCard(
    album: Album,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ) { onClick() }
    ) {
        Card(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!album.coverPhotoUri.isNullOrBlank()) {
                    LocalPhotoView(
                        photoUri = album.coverPhotoUri,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                // Shadow Gradient đáy thẻ
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, colorScheme.background.copy(alpha = 0.85f))
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = album.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        val countText = if (album.remainingCount <= 0) {
            "Đã duyệt hết • ${formatAlbumNumber(album.photoCount)} ảnh"
        } else {
            "Còn ${formatAlbumNumber(album.remainingCount)} / ${formatAlbumNumber(album.photoCount)} ảnh"
        }
        Text(
            text = countText,
            style = MaterialTheme.typography.bodySmall,
            color = if (album.remainingCount <= 0) colorScheme.primary else colorScheme.onSurfaceVariant,
            fontWeight = if (album.remainingCount <= 0) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

private fun formatAlbumNumber(number: Int): String {
    val s = number.toString()
    val sb = StringBuilder()
    var count = 0
    for (i in s.length - 1 downTo 0) {
        sb.append(s[i])
        count++
        if (count % 3 == 0 && i > 0) {
            sb.append('.')
        }
    }
    return sb.reverse().toString()
}

private fun formatAlbumFileSize(bytes: Long): String {
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
// Overload tích hợp với DiscoverViewModel
// --------------------------------------------------------------------------------------

@Composable
fun AlbumScreen(
    viewModel: DiscoverViewModel,
    onNavigateToTab: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val pendingSizeBytes = uiState.pendingDeletions.sumOf { it.fileSize }
    val presentationState = AlbumsUiState(
        albums = uiState.albums,
        pendingDeletionsCount = uiState.pendingDeletions.size,
        pendingDeletionsSizeBytes = pendingSizeBytes,
        hasPermission = uiState.hasPermission,
        isLoading = uiState.isLoading
    )

    AlbumScreen(
        uiState = presentationState,
        onReviewPendingClick = {
            viewModel.openPendingDeletions()
        },
        onAlbumClick = { album ->
            viewModel.selectAlbum(album)
            onNavigateToTab(BottomTab.DISCOVER)
        },
        modifier = modifier
    )
}
