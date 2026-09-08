package plozdev.swipegallery.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import plozdev.swipegallery.components.LocalPhotoView
import plozdev.swipegallery.screens.viewModels.DiscoverViewModel
import plozdev.swipegallery.screens.viewModels.CleanupType
import plozdev.swipegallery.domain.models.PhotoItem

@Composable
fun CleanupScreen(viewModel: DiscoverViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Slate 900
                        Color(0xFF020617)  // Slate 950
                    )
                )
            )
    ) {
        AnimatedContent(
            targetState = uiState.activeCleanupType,
            transitionSpec = {
                if (targetState != null) {
                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> -width } + fadeOut())
                } else {
                    (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> width } + fadeOut())
                }
            }
        ) { activeType ->
            if (activeType != null) {
                CleanupDetailView(
                    type = activeType,
                    photos = when (activeType) {
                        CleanupType.DUPLICATES -> uiState.duplicatePhotos
                        CleanupType.BLURRY -> uiState.blurryPhotos
                        CleanupType.LARGE_VIDEOS -> uiState.largeVideos
                    },
                    selectedIds = uiState.cleanupSelectedIds,
                    onToggleSelect = { viewModel.toggleCleanupSelection(it) },
                    onSelectAll = { viewModel.selectAllCleanup(it) },
                    onApply = { viewModel.applyCleanupDeletions() },
                    onBack = { viewModel.closeCleanupDetail() },
                    onPhotoClick = { viewModel.openFullscreen(it) }
                )
            } else {
                CleanupDashboardView(
                    duplicatePhotos = uiState.duplicatePhotos,
                    blurryPhotos = uiState.blurryPhotos,
                    videoPhotos = uiState.largeVideos,
                    onOpenType = { viewModel.openCleanupDetail(it) }
                )
            }
        }
    }
}

@Composable
fun CleanupDashboardView(
    duplicatePhotos: List<PhotoItem>,
    blurryPhotos: List<PhotoItem>,
    videoPhotos: List<PhotoItem>,
    onOpenType: (CleanupType) -> Unit
) {
    val duplicateSizeBytes = duplicatePhotos.sumOf { it.fileSize }
    val blurrySizeBytes = blurryPhotos.sumOf { it.fileSize }
    val videoSizeBytes = videoPhotos.sumOf { it.fileSize }
    val totalSizeBytes = duplicateSizeBytes + blurrySizeBytes + videoSizeBytes

    val displaySizeText = formatSizeBytes(totalSizeBytes)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 100.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00D2FC))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "SẮP XẾP",
                color = Color(0xFF00D2FC),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                letterSpacing = 1.5.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats Circle (Visual indicators)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glow overlay behind the circle
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(CircleShape)
                    .border(
                        width = 4.dp,
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFF6366F1),
                                Color(0xFF00D2FC),
                                Color(0xFF1E293B),
                                Color(0xFF6366F1)
                            )
                        ),
                        shape = CircleShape
                    )
                    .background(Color(0xFF020617).copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = displaySizeText,
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (totalSizeBytes > 0) "Có thể giải phóng" else "Thiết bị sạch sẽ",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "Thống kê dọn dẹp",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (totalSizeBytes > 0) "Nhấn vào các mục bên dưới để dọn dẹp bộ nhớ" else "Không còn tệp dư thừa nào được tìm thấy",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "GỢI Ý DỌN DẸP",
            color = Color(0xFF64748B),
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Suggestions List
        CleanupSuggestionItem(
            icon = Icons.Default.Info,
            title = "Ảnh trùng lặp",
            subtitle = "${formatSizeBytes(duplicateSizeBytes)} • ${duplicatePhotos.size} tệp",
            iconTint = Color(0xFF818CF8),
            bgTint = Color(0xFF818CF8).copy(alpha = 0.15f),
            enabled = duplicatePhotos.isNotEmpty(),
            onClick = { onOpenType(CleanupType.DUPLICATES) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        CleanupSuggestionItem(
            icon = Icons.Default.Warning,
            title = "Ảnh mờ",
            subtitle = "${formatSizeBytes(blurrySizeBytes)} • ${blurryPhotos.size} tệp",
            iconTint = Color(0xFF34D399),
            bgTint = Color(0xFF34D399).copy(alpha = 0.15f),
            enabled = blurryPhotos.isNotEmpty(),
            onClick = { onOpenType(CleanupType.BLURRY) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        CleanupSuggestionItem(
            icon = Icons.Default.PlayArrow,
            title = "Video dung lượng lớn",
            subtitle = "${formatSizeBytes(videoSizeBytes)} • ${videoPhotos.size} tệp",
            iconTint = Color(0xFFF87171),
            bgTint = Color(0xFFF87171).copy(alpha = 0.15f),
            enabled = videoPhotos.isNotEmpty(),
            onClick = { onOpenType(CleanupType.LARGE_VIDEOS) }
        )
    }
}

@Composable
fun CleanupSuggestionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    bgTint: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) Color(0xFF1E293B) else Color(0xFF1E293B).copy(alpha = 0.4f))
            .border(1.dp, Color.White.copy(alpha = if (enabled) 0.05f else 0.02f), RoundedCornerShape(16.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(bgTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, 
                color = if (enabled) Color.White else Color.Gray, 
                fontWeight = FontWeight.Bold, 
                fontSize = 16.sp
            )
            Text(
                text = subtitle, 
                color = if (enabled) Color(0xFF94A3B8) else Color.DarkGray, 
                fontSize = 12.sp
            )
        }
        if (enabled) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF64748B))
        }
    }
}

@Composable
fun CleanupDetailView(
    type: CleanupType,
    photos: List<PhotoItem>,
    selectedIds: Set<String>,
    onToggleSelect: (String) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onApply: () -> Unit,
    onBack: () -> Unit,
    onPhotoClick: (PhotoItem) -> Unit
) {
    val title = when (type) {
        CleanupType.DUPLICATES -> "Ảnh trùng lặp"
        CleanupType.BLURRY -> "Ảnh mờ"
        CleanupType.LARGE_VIDEOS -> "Video dung lượng lớn"
    }

    val selectedSizeBytes = photos.filter { it.id in selectedIds }.sumOf { it.fileSize }
    val selectedSizeText = formatSizeBytes(selectedSizeBytes)
    val allSelected = selectedIds.size == photos.size && photos.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$title (${photos.size})",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        if (photos.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✨", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Không tìm thấy tệp nào",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Select All Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = allSelected,
                    onCheckedChange = { onSelectAll(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFFF5252),
                        uncheckedColor = Color.Gray,
                        checkmarkColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Chọn tất cả để xóa (${selectedIds.size}/${photos.size})",
                    color = Color.LightGray,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Grid of Photo cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(photos, key = { it.id }) { photo ->
                    val isSelected = photo.id in selectedIds
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                2.dp,
                                if (isSelected) Color(0xFFFF5252) else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onPhotoClick(photo) }
                    ) {
                        LocalPhotoView(
                            photoUri = photo.uri,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Duration overlay badge for video type
                        if (photo.isVideo && photo.duration != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = formatDuration(photo.duration),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Size badge overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = formatSizeBytes(photo.fileSize),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Selection Checkbox in the top corner
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .clickable { onToggleSelect(photo.id) }
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onToggleSelect(photo.id) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFFFF5252),
                                    uncheckedColor = Color.Black.copy(alpha = 0.5f),
                                    checkmarkColor = Color.White
                                ),
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                    }
                }
            }

            // Bottom Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cancel Button
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("Bỏ qua", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                // Delete Button
                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedIds.isNotEmpty()) Color(0xFFFF5252) else Color(0xFF64748B)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(52.dp)
                ) {
                    Text(
                        text = if (selectedIds.isNotEmpty()) "Xóa $selectedSizeText" else "Giữ tất cả",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

private fun formatSizeBytes(bytes: Long): String {
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

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        "${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${minutes}:${seconds.toString().padStart(2, '0')}"
    }
}
