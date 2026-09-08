package plozdev.swipegallery.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import plozdev.swipegallery.components.FullscreenPhotoViewer
import plozdev.swipegallery.screens.viewModels.DiscoverViewModel

/**
 * 3 Tab điều hướng chính tinh gọn (đã lược bỏ mục Sắp xếp theo yêu cầu):
 * 1. Khám phá (Discover)
 * 2. Album (Bao gồm hộp Ảnh chờ xóa & danh sách Album máy)
 * 3. Cài đặt (Settings)
 */
enum class BottomTab(val title: String, val icon: ImageVector) {
    DISCOVER("Khám phá", Icons.AutoMirrored.Filled.List),
    ALBUM("Album", Icons.Default.Favorite),
    SETTINGS("Cài đặt", Icons.Default.Settings)
}

@Composable
fun MainScreen(viewModel: DiscoverViewModel) {
    var selectedTab by remember { mutableStateOf(BottomTab.DISCOVER) }
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        bottomBar = {
            if (!uiState.isPendingDeletionsOpen) {
                CustomBottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (selectedTab) {
                BottomTab.DISCOVER -> DiscoverScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { selectedTab = it }
                )
                BottomTab.ALBUM -> AlbumScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { selectedTab = it }
                )
                BottomTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }

            // Màn hình Duyệt Xóa khi người dùng nhấn "Xem lại" từ hộp Ảnh Chờ Xóa
            AnimatedVisibility(
                visible = uiState.isPendingDeletionsOpen,
                enter = slideInHorizontally { width -> width } + fadeIn(),
                exit = slideOutHorizontally { width -> width } + fadeOut()
            ) {
                PendingReviewScreen(
                    viewModel = viewModel,
                    onNavigateBack = { viewModel.closePendingDeletions() }
                )
            }

            // Trình xem ảnh toàn màn hình dùng chung
            uiState.fullscreenPhoto?.let { photo ->
                FullscreenPhotoViewer(
                    photo = photo,
                    onClose = { viewModel.closeFullscreen() }
                )
            }
        }
    }
}

@Composable
fun CustomBottomNavigationBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        color = colorScheme.surface.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 10.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val tint = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant
                val scale = if (isSelected) 1.08f else 1.0f

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .scale(scale)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(tab) }
                        )
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.title,
                        color = tint,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
