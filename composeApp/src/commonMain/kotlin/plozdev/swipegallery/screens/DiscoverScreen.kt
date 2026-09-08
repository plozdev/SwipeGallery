package plozdev.swipegallery.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import plozdev.swipegallery.components.LocalPhotoView
import plozdev.swipegallery.components.SwipeDirection
import plozdev.swipegallery.components.SwipeableCard
import plozdev.swipegallery.components.rememberSwipeableCardState
import plozdev.swipegallery.domain.models.Album
import plozdev.swipegallery.domain.models.PhotoItem
import plozdev.swipegallery.screens.viewModels.DiscoverViewModel

/**
 * Immutable UI State cho màn hình Khám phá.
 * 100% tiếng Việt, hỗ trợ trạng thái cấp quyền, chọn Album trực tiếp và hiển thị số lượng ảnh rõ ràng.
 */
data class DiscoverUiState(
    val photos: List<PhotoItem> = emptyList(),
    val albums: List<Album> = emptyList(),
    val currentAlbumId: String? = null,
    val currentAlbumName: String = "Tất cả ảnh",
    val currentAlbumCoverUri: String? = null,
    val allPhotosCoverUri: String? = null,
    val totalCount: Int = 0,
    val remainingCount: Int = 0,
    val canUndo: Boolean = false,
    val isLoading: Boolean = false,
    val hasPermission: Boolean = true,
    val hasSwipedInSession: Boolean = false
)

/**
 * Màn hình Khám phá (Discover Screen) theo chuẩn OLED Dark Theme và ngôn ngữ tiếng Việt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    uiState: DiscoverUiState,
    onSwipeLeft: (PhotoItem) -> Unit,
    onSwipeRight: (PhotoItem) -> Unit,
    onUndo: () -> Unit,
    onSelectAlbum: (Album?) -> Unit,
    onPhotoClick: (PhotoItem) -> Unit,
    onOpenSettings: () -> Unit = {},
    onRetryPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    var showAlbumPickerSheet by remember { mutableStateOf(false) }

    val topPhoto = uiState.photos.firstOrNull()
    val topCardState = rememberSwipeableCardState(key = topPhoto?.id)

    // Khi Undo hoặc chuyển album, đảm bảo thẻ lập tức nằm ngay ngắn ở giữa
    LaunchedEffect(topPhoto?.id) {
        topCardState.resetImmediate()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Top Bar: Album Selector Pill & Huy hiệu số lượng ảnh ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút mở Popup chọn Album (Animated Bottom Sheet)
            Surface(
                color = colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant),
                modifier = Modifier.clickable { showAlbumPickerSheet = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 6.dp, end = 14.dp, top = 5.dp, bottom = 5.dp)
                ) {
                    val activeCoverUri = if (uiState.currentAlbumId == null) uiState.allPhotosCoverUri else uiState.currentAlbumCoverUri
                    if (!activeCoverUri.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(colorScheme.surface)
                        ) {
                            LocalPhotoView(
                                photoUri = activeCoverUri,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = uiState.currentAlbumName,
                        color = colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Chọn album",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Huy hiệu hiển thị số lượng ảnh trực quan, dễ hiểu
            Surface(
                color = colorScheme.surfaceVariant.copy(alpha = 0.75f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant)
            ) {
                val badgeText = when {
                    !uiState.hasPermission -> "Chưa cấp quyền"
                    uiState.remainingCount > 0 -> "Còn ${formatNumber(uiState.remainingCount)} ảnh"
                    else -> "0 ảnh"
                }
                Text(
                    text = badgeText,
                    color = if (!uiState.hasPermission) colorScheme.error else colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // --- 2. Center: Ngăn xếp 3D (~75% chiều cao màn hình) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 18.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        color = colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
                !uiState.hasPermission -> {
                    PermissionDeniedView(
                        onOpenSettings = onOpenSettings,
                        onRetry = onRetryPermission,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                uiState.photos.isEmpty() -> {
                    if (uiState.hasSwipedInSession || uiState.totalCount > 0) {
                        AllPhotosReviewedView(
                            canUndo = uiState.canUndo,
                            onReset = onUndo,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        EmptyGalleryView(
                            onRetry = onRetryPermission,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                else -> {
                    val visibleCards = uiState.photos.take(3)

                    for (index in visibleCards.indices.reversed()) {
                        val photo = visibleCards[index]
                        val isTopCard = (index == 0)

                        val targetScale = 1f - (index * 0.05f)
                        val targetOffsetY = (index * 12).dp
                        val targetAlpha = 1f - (index * 0.18f)

                        val animatedScale by animateFloatAsState(
                            targetValue = targetScale,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )
                        val animatedOffsetY by animateDpAsState(
                            targetValue = targetOffsetY,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )
                        val animatedAlpha by animateFloatAsState(
                            targetValue = targetAlpha,
                            animationSpec = tween(durationMillis = 200)
                        )

                        key(photo.id) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = animatedScale
                                        scaleY = animatedScale
                                        translationY = animatedOffsetY.toPx()
                                        alpha = animatedAlpha
                                    }
                            ) {
                                if (isTopCard) {
                                    SwipeableCard(
                                        photo = photo,
                                        state = topCardState,
                                        enabled = !topCardState.isSwipingOut,
                                        onSwiped = { direction ->
                                            if (direction == SwipeDirection.RIGHT) {
                                                onSwipeRight(photo)
                                            } else {
                                                onSwipeLeft(photo)
                                            }
                                        },
                                        onClick = { onPhotoClick(photo) }
                                    )
                                } else {
                                    SwipeableCard(
                                        photo = photo,
                                        enabled = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 3. Action Controls: Nút Hoàn Tác, Xóa, Giữ ---
        val undoEnabled = uiState.hasPermission && uiState.canUndo
        val actionEnabled = uiState.hasPermission && topPhoto != null && !topCardState.isSwipingOut

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Hoàn Tác (Undo)
            FloatingActionButton(
                onClick = { if (undoEnabled) onUndo() },
                containerColor = if (undoEnabled) colorScheme.surfaceVariant else colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = if (undoEnabled) colorScheme.onSurfaceVariant else colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = if (undoEnabled) 4.dp else 0.dp),
                modifier = Modifier
                    .size(54.dp)
                    .border(1.dp, colorScheme.outlineVariant.copy(alpha = if (undoEnabled) 1f else 0.25f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Hoàn tác",
                    modifier = Modifier.size(24.dp)
                )
            }

            // Nút Xóa / Quẹt trái (Coral Destructive)
            FloatingActionButton(
                onClick = {
                    if (actionEnabled) {
                        topPhoto?.let { photo ->
                            scope.launch {
                                topCardState.swipe(SwipeDirection.LEFT) {
                                    onSwipeLeft(photo)
                                }
                            }
                        }
                    }
                },
                containerColor = if (actionEnabled) colorScheme.surfaceVariant else colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = if (actionEnabled) colorScheme.error else colorScheme.error.copy(alpha = 0.3f),
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = if (actionEnabled) 6.dp else 0.dp),
                modifier = Modifier
                    .size(68.dp)
                    .border(2.dp, colorScheme.error.copy(alpha = if (actionEnabled) 0.45f else 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Bỏ vào hàng chờ xóa",
                    modifier = Modifier.size(34.dp)
                )
            }

            // Nút Giữ / Quẹt phải (Emerald Primary)
            FloatingActionButton(
                onClick = {
                    if (actionEnabled) {
                        topPhoto?.let { photo ->
                            scope.launch {
                                topCardState.swipe(SwipeDirection.RIGHT) {
                                    onSwipeRight(photo)
                                }
                            }
                        }
                    }
                },
                containerColor = if (actionEnabled) colorScheme.surfaceVariant else colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = if (actionEnabled) colorScheme.primary else colorScheme.primary.copy(alpha = 0.3f),
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = if (actionEnabled) 6.dp else 0.dp),
                modifier = Modifier
                    .size(68.dp)
                    .border(2.dp, colorScheme.primary.copy(alpha = if (actionEnabled) 0.45f else 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Giữ lại ảnh",
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }

    // --- 4. Popup Chọn Album dạng ModalBottomSheet với hiệu ứng chuyển cảnh mượt mà ---
    if (showAlbumPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAlbumPickerSheet = false },
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = colorScheme.outlineVariant) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Chọn Thư Mục Ảnh",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Lựa chọn 1: Tất cả ảnh (kèm thumbnail ảnh nhỏ bên cạnh)
                AlbumPickerRow(
                    title = "Tất cả ảnh",
                    photoCount = uiState.totalCount,
                    isSelected = uiState.currentAlbumId == null,
                    coverPhotoUri = uiState.allPhotosCoverUri,
                    onClick = {
                        onSelectAlbum(null)
                        showAlbumPickerSheet = false
                    }
                )

                HorizontalDivider(
                    color = colorScheme.outlineVariant.copy(alpha = 0.35f),
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Danh sách các Album thiết bị
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.albums, key = { it.id }) { album ->
                        AlbumPickerRow(
                            title = album.name,
                            photoCount = album.photoCount,
                            isSelected = uiState.currentAlbumId == album.id,
                            coverPhotoUri = album.coverPhotoUri,
                            onClick = {
                                onSelectAlbum(album)
                                showAlbumPickerSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Hàng chọn Album trong Popup Dropdown / Bottom Sheet với thumbnail ảnh nhỏ bên cạnh.
 */
@Composable
private fun AlbumPickerRow(
    title: String,
    photoCount: Int,
    isSelected: Boolean,
    coverPhotoUri: String? = null,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail ảnh nhỏ bên cạnh (UX enhancement)
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colorScheme.surfaceVariant)
                .border(
                    width = 1.dp,
                    color = if (isSelected) colorScheme.primary.copy(alpha = 0.5f) else colorScheme.outlineVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!coverPhotoUri.isNullOrBlank()) {
                LocalPhotoView(
                    photoUri = coverPhotoUri,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) colorScheme.primary else colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${formatNumber(photoCount)} ảnh",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Đang chọn",
                tint = colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Giao diện khi người dùng chưa cấp quyền truy cập ảnh trên thiết bị.
 */
@Composable
private fun PermissionDeniedView(
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier.padding(12.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.85f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colorScheme.errorContainer.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Cần Cấp Quyền Truy Cập",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "SwipeGallery cần quyền truy cập vào thư viện ảnh để bạn có thể xem, phân loại và dọn dẹp dung lượng. Vui lòng cấp quyền trong Cài đặt hệ thống.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                shape = RoundedCornerShape(percent = 50),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Mở Cài Đặt Thiết Bị",
                    color = colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onRetry
            ) {
                Text(
                    text = "Thử lại",
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Giao diện khi thư mục trên thiết bị hoàn toàn trống ảnh.
 */
@Composable
private fun EmptyGalleryView(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier.padding(12.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "🖼️", fontSize = 56.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Không Có Ảnh Nào",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Không tìm thấy ảnh nào trong thư mục này trên thiết bị.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant),
                shape = RoundedCornerShape(percent = 50),
                border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant)
            ) {
                Text(
                    text = "Quét lại thư viện",
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Màn hình thông báo khi người dùng đã duyệt hết tất cả ảnh trong phiên vuốt.
 */
@Composable
private fun AllPhotosReviewedView(
    canUndo: Boolean,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier.padding(12.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "🎉", fontSize = 56.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Đã duyệt hết ảnh!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tất cả ảnh trong thư mục này đã được phân loại xong.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (canUndo) {
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Text(
                        text = "Hoàn tác ảnh vừa vuốt",
                        color = colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatNumber(number: Int): String {
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

// --------------------------------------------------------------------------------------
// Overload tích hợp với DiscoverViewModel
// --------------------------------------------------------------------------------------

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    onNavigateToTab: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkAndLoadMedia()
    }

    val allPhotosCover = uiState.photos.firstOrNull()?.uri
        ?: uiState.albums.firstOrNull { !it.coverPhotoUri.isNullOrBlank() }?.coverPhotoUri
    val currentAlbumCover = uiState.currentAlbum?.coverPhotoUri ?: allPhotosCover

    val presentationState = DiscoverUiState(
        photos = uiState.photos,
        albums = uiState.albums,
        currentAlbumId = uiState.currentAlbum?.id,
        currentAlbumName = uiState.currentAlbum?.name ?: "Tất cả ảnh",
        currentAlbumCoverUri = currentAlbumCover,
        allPhotosCoverUri = allPhotosCover,
        totalCount = uiState.photos.size + uiState.pendingDeletions.size,
        remainingCount = uiState.photos.size,
        canUndo = viewModel.hasSwipedInSession(),
        isLoading = uiState.isLoading,
        hasPermission = uiState.hasPermission,
        hasSwipedInSession = viewModel.hasSwipedInSession()
    )

    DiscoverScreen(
        uiState = presentationState,
        onSwipeLeft = { photo -> viewModel.onPhotoSwiped(photo, isRightSwipe = false) },
        onSwipeRight = { photo -> viewModel.onPhotoSwiped(photo, isRightSwipe = true) },
        onUndo = { viewModel.undoLastSwipe() },
        onSelectAlbum = { album -> viewModel.selectAlbum(album) },
        onPhotoClick = { photo -> viewModel.openFullscreen(photo) },
        onOpenSettings = { viewModel.openAppSettings() },
        onRetryPermission = { viewModel.retryPermission() },
        modifier = modifier
    )
}
