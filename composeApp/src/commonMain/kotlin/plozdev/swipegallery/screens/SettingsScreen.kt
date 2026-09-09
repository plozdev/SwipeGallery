package plozdev.swipegallery.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import plozdev.swipegallery.screens.viewModels.DiscoverViewModel

/**
 * Immutable UI State cho màn hình Cài đặt.
 */
data class SettingsUiState(
    val streakDays: Int = 0,
    val totalCleanedBytes: Long = 0L,
    val triagedCount: Int = 0,
    val keptRatio: Int = 0,
    val safeStagingEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val autoAdvanceEnabled: Boolean = true,
    val burstGroupingEnabled: Boolean = true
)

/**
 * Màn hình Cài đặt (100% tiếng Việt thống nhất theo yêu cầu).
 */
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSafeStagingToggled: (Boolean) -> Unit,
    onHapticsToggled: (Boolean) -> Unit,
    onAutoAdvanceToggled: (Boolean) -> Unit,
    onBurstGroupingToggled: (Boolean) -> Unit,
    onClearHistoryClick: () -> Unit,
    onClearCacheClick: () -> Unit,
    onExportReportClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // Tiêu đề Cài Đặt
        Text(
            text = "Cài Đặt",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
        )

        // --- 1. Thẻ Thống kê Chuỗi dọn dẹp (Declutter Streak) ---
        DeclutterStreakCard(uiState = uiState)

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. Nhóm: HÀNH VI PHÂN LOẠI ---
        SettingsSectionHeader(title = "HÀNH VI PHÂN LOẠI")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colorScheme.outlineVariant, MaterialTheme.shapes.extraLarge),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingSwitchRow(
                    title = "Hàng đợi xóa an toàn",
                    subtitle = "Đưa vào hàng đợi xem lại thay vì xóa vĩnh viễn ngay",
                    checked = uiState.safeStagingEnabled,
                    onCheckedChange = onSafeStagingToggled
                )
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingSwitchRow(
                    title = "Rung phản hồi",
                    subtitle = "Rung nhẹ khi ngón tay vượt qua ngưỡng vuốt thẻ",
                    checked = uiState.hapticsEnabled,
                    onCheckedChange = onHapticsToggled
                )
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingSwitchRow(
                    title = "Tự động chuyển thẻ",
                    subtitle = "Tự động chuyển sang ảnh tiếp theo sau khi vuốt",
                    checked = uiState.autoAdvanceEnabled,
                    onCheckedChange = onAutoAdvanceToggled
                )
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingSwitchRow(
                    title = "Gom cụm ảnh chụp liên tiếp",
                    subtitle = "Tự động nhận diện cụm ảnh chụp liên tiếp nhanh",
                    checked = uiState.burstGroupingEnabled,
                    onCheckedChange = onBurstGroupingToggled
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 3. Nhóm: DỮ LIỆU & LỊCH SỬ ---
        SettingsSectionHeader(title = "DỮ LIỆU & LỊCH SỬ")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colorScheme.outlineVariant, MaterialTheme.shapes.extraLarge),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingActionRow(
                    icon = Icons.Default.Refresh,
                    title = "Xóa lịch sử vuốt",
                    subtitle = "Đặt lại các thẻ đã duyệt để xem lại từ đầu",
                    onClick = onClearHistoryClick
                )
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingActionRow(
                    icon = Icons.Default.Share,
                    title = "Xuất báo cáo dọn dẹp",
                    subtitle = "Chia sẻ thống kê dung lượng đã giải phóng",
                    onClick = onExportReportClick
                )
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingActionRow(
                    icon = Icons.Default.Delete,
                    title = "Xóa bộ nhớ đệm ảnh",
                    subtitle = "Dọn sạch bộ nhớ cache thumbnail tạm thời",
                    onClick = onClearCacheClick
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. Nhóm: THÔNG TIN ỨNG DỤNG ---
        SettingsSectionHeader(title = "THÔNG TIN ỨNG DỤNG")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colorScheme.outlineVariant, MaterialTheme.shapes.extraLarge),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                InfoSpecRow(label = "Phiên bản", value = "1.0.0 (Compose Multiplatform)")
                InfoSpecRow(label = "Nền tảng", value = "Kotlin Multiplatform • MD3")
                InfoSpecRow(label = "Kiến trúc", value = "Clean Architecture + MVVM")
                InfoSpecRow(label = "Quyền truy cập", value = "Bộ nhớ hình ảnh thiết bị")
            }
        }
    }
}

/**
 * Thẻ thống kê Chuỗi ngày dọn dẹp.
 */
@Composable
private fun DeclutterStreakCard(uiState: SettingsUiState) {
    val colorScheme = MaterialTheme.colorScheme
    val cleanedText = formatSettingsFileSize(uiState.totalCleanedBytes)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.primary.copy(alpha = 0.3f), MaterialTheme.shapes.extraLarge),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔥", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${uiState.streakDays} Ngày Dọn Dẹp Liên Tiếp",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "Duy trì thói quen để giữ thư viện luôn ngăn nắp",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dòng 3 chỉ số phụ
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorScheme.surface)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(label = "ĐÃ DỌN DẸP", value = cleanedText)
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(colorScheme.outlineVariant)
                )
                MetricItem(label = "ĐÃ DUYỆT", value = "${uiState.triagedCount}")
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(colorScheme.outlineVariant)
                )
                MetricItem(label = "TỶ LỆ GIỮ", value = "${uiState.keptRatio}%")
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    val colorScheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
    )
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorScheme.onPrimary,
                checkedTrackColor = colorScheme.primary,
                uncheckedThumbColor = colorScheme.onSurfaceVariant,
                uncheckedTrackColor = colorScheme.surface
            )
        )
    }
}

@Composable
private fun SettingActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun InfoSpecRow(label: String, value: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurface
        )
    }
}

private fun formatSettingsFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
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
// Overload tích hợp cùng DiscoverViewModel
// --------------------------------------------------------------------------------------

@Composable
fun SettingsScreen(
    viewModel: DiscoverViewModel,
    modifier: Modifier = Modifier
) {
    val settingsState by viewModel.settingsState.collectAsState()
    var showReportDialog by remember { mutableStateOf(false) }
    var showCacheClearedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshSettingsStats()
    }

    SettingsScreen(
        uiState = settingsState,
        onSafeStagingToggled = { viewModel.setSafeStagingEnabled(it) },
        onHapticsToggled = { viewModel.setHapticsEnabled(it) },
        onAutoAdvanceToggled = { viewModel.setAutoAdvanceEnabled(it) },
        onBurstGroupingToggled = { viewModel.setBurstGroupingEnabled(it) },
        onClearHistoryClick = { viewModel.clearSwipeHistory() },
        onClearCacheClick = {
            viewModel.clearCache()
            showCacheClearedDialog = true
        },
        onExportReportClick = {
            showReportDialog = true
        },
        modifier = modifier
    )

    if (showReportDialog) {
        val cleanedText = formatSettingsFileSize(settingsState.totalCleanedBytes)
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = {
                Text(
                    text = "Báo Cáo Dọn Dẹp",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🔥 Chuỗi dọn dẹp: ${settingsState.streakDays} ngày liên tiếp")
                    Text("🗑️ Đã giải phóng: $cleanedText")
                    Text("📸 Đã phân loại: ${settingsState.triagedCount} ảnh")
                    Text("⭐ Tỷ lệ giữ lại: ${settingsState.keptRatio}%")
                }
            },
            confirmButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    if (showCacheClearedDialog) {
        AlertDialog(
            onDismissRequest = { showCacheClearedDialog = false },
            title = { Text("Bộ Nhớ Đệm", fontWeight = FontWeight.Bold) },
            text = { Text("Đã dọn sạch bộ nhớ cache thumbnail và dữ liệu tạm thời thành công.") },
            confirmButton = {
                TextButton(onClick = { showCacheClearedDialog = false }) {
                    Text("Xong")
                }
            }
        )
    }
}
