package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewCarousel
import com.example.ui.components.MagazineTypographyDialog
import com.example.ui.components.PresetManagerBottomSheet
import com.example.ui.components.SocialFeedSimulatorDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.model.WatermarkConfig
import com.example.model.WatermarkPosition
import com.example.model.WatermarkScope
import com.example.model.WatermarkType
import com.example.util.WatermarkHelper
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ExportConfig
import com.example.model.ExportFormat
import com.example.model.SlicePiece
import com.example.ui.components.NotificationToast
import com.example.ui.components.OrderBadge
import com.example.viewmodel.Screen
import com.example.viewmodel.SliceUiState
import com.example.viewmodel.SliceViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportPreviewScreen(
    viewModel: SliceViewModel,
    uiState: SliceUiState,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.Editor)
    }

    val result = uiState.sliceResult
    val pieces = result?.pieces ?: emptyList()
    var previewTab by remember { mutableIntStateOf(0) } // 0: Sequential Grid, 1: Slices, 2: Collage Mode
    val config = uiState.exportConfig
    var showSocialSimulator by remember { androidx.compose.runtime.mutableStateOf(false) }
    var showMagazineDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var showPresetSheet by remember { androidx.compose.runtime.mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "切图完成 (共 ${pieces.size} 张)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Editor) },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回编辑"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSocialSimulator = true }) {
                        Icon(
                            imageVector = Icons.Default.ViewCarousel,
                            contentDescription = "社媒发圈模拟",
                            tint = Color(0xFF07C160)
                        )
                    }
                    IconButton(onClick = { showMagazineDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "排版与胶片时间戳",
                            tint = Color(0xFFF59E0B)
                        )
                    }
                    IconButton(onClick = { showPresetSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = "模版预设",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { viewModel.toggleExportSettings(true) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "导出选项",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { viewModel.togglePostingGuide(true) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "发圈攻略",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Export Settings Summary Bar
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleExportSettings(true) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "导出参数：${config.format.name} · 分辨率 ${(config.scaleFactor * 100).toInt()}%" +
                                            if (config.format == ExportFormat.JPG) " · 质量 ${config.quality}%" else " · 无损",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "修改设置 >",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Main Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (previewTab == 0) {
                                    viewModel.shareCompositeEffectImage()
                                } else {
                                    viewModel.shareAllPieces()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("share_button")
                        ) {
                            Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (previewTab == 0) "分享大图" else "批量分享", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (previewTab == 0) {
                                    viewModel.saveCompositeEffectImage()
                                } else {
                                    viewModel.saveAllPieces()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .testTag("save_primary_button")
                        ) {
                            if (uiState.isProcessing) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (previewTab == 0) "保存效果大图" else "保存全部切片",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Save Both (Pieces + Composite Image)
                        Button(
                            onClick = { viewModel.saveBothPiecesAndComposite() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(46.dp)
                                .testTag("save_both_button")
                        ) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("一键全存", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // View Mode Tab
            TabRow(
                selectedTabIndex = previewTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = previewTab == 0,
                    onClick = { previewTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Image, null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "切图效果大图",
                                fontWeight = if (previewTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                )
                Tab(
                    selected = previewTab == 1,
                    onClick = { previewTab = 1 },
                    text = {
                        Text(
                            "发圈切片 (${pieces.size})",
                            fontWeight = if (previewTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = previewTab == 2,
                    onClick = { previewTab = 2 },
                    text = {
                        Text(
                            "空间拼贴",
                            fontWeight = if (previewTab == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                if (previewTab == 0) {
                    // TAB 0: Single Composite Grid Effect Image (带切割线单张效果大图, 匹配用户样图需求)
                    val compBmp = uiState.compositeEffectBitmap
                    if (compBmp != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Large high-res preview of the composite photo with cut lines
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val aspect = (compBmp.width.toFloat() / compBmp.height.toFloat()).coerceIn(0.3f, 3.0f)
                                    Image(
                                        bitmap = compBmp.asImageBitmap(),
                                        contentDescription = "带切割线切图效果大图",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(aspect)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${uiState.selectedLayout.title} · ${compBmp.width}×${compBmp.height}px · 线条宽度 ${String.format(Locale.US, "%.1f", uiState.lineWidthDp)}dp",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Quick Line Color & Width Tweak Card
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "效果图线条快速定制",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(uiState.lineColorHex))
                                                    .border(0.5.dp, Color.Gray, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "#${String.format(Locale.US, "%06X", (uiState.lineColorHex and 0xFFFFFFL).toInt())}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Quick Color Swatches
                                    val linePresetColors = listOf(
                                        0xFFFFFFFFL to "纯白",
                                        0xFF67E8F9L to "青空蓝",
                                        0xFF22C55EL to "荧光绿",
                                        0xFFFACC15L to "明黄",
                                        0xFFF43F5EL to "胭脂红",
                                        0xFF18181BL to "深曜黑"
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        linePresetColors.forEach { (colorHex, name) ->
                                            val isSelected = (uiState.lineColorHex and 0xFFFFFFL) == (colorHex and 0xFFFFFFL)
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { viewModel.regenerateCompositeEffect(colorHex = colorHex) },
                                                label = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(9.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(colorHex))
                                                                .border(0.5.dp, Color.Gray, CircleShape)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(name, fontSize = 10.sp)
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Quick Width Chips
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val widthPresets = listOf(1.5f to "细线 1.5dp", 3.0f to "标准 3.0dp", 5.0f to "加粗 5.0dp")
                                        widthPresets.forEach { (w, name) ->
                                            val isSelected = kotlin.math.abs(uiState.lineWidthDp - w) < 0.2f
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { viewModel.regenerateCompositeEffect(widthDp = w) },
                                                label = { Text(name, fontSize = 10.sp) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            // Dedicated Quick Export Actions for Composite Image
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.shareCompositeEffectImage() },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("分享效果图", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { viewModel.saveCompositeEffectImage() },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1.3f)
                                ) {
                                    Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("保存单张效果大图", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else if (previewTab == 1) {
                    // TAB 1: Sequential Grid (Pieces list 1, 2, 3...)
                    if (pieces.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("暂无切图碎片，请返回重新切片")
                        }
                    } else {
                        val cols = when {
                            pieces.size <= 4 -> 2
                            pieces.size <= 9 -> 3
                            else -> 3
                        }
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "已按 1 → ${pieces.size} 自动编号排版",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                OutlinedButton(
                                    onClick = { viewModel.savePostingSequenceGuideCard() },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Download, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("保存防乱序指引卡", fontSize = 11.sp)
                                }
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(cols),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(pieces) { piece ->
                                    SlicePieceCard(
                                        piece = piece,
                                        onClick = { viewModel.inspectPiece(piece) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // TAB 2: Spatial Collage Mode
                    CollagePreviewLayout(
                        pieces = pieces,
                        onPieceClick = { viewModel.inspectPiece(it) }
                    )
                }
            }
        }

        // Export Settings Dialog
        if (uiState.showExportSettings) {
            ExportSettingsDialog(
                config = uiState.exportConfig,
                samplePiece = pieces.firstOrNull(),
                onDismiss = { viewModel.toggleExportSettings(false) },
                onConfirm = { updated ->
                    viewModel.updateExportConfig(updated)
                    viewModel.toggleExportSettings(false)
                }
            )
        }

        // Single Piece Detail Inspector Dialog
        if (uiState.inspectingPiece != null) {
            val piece = uiState.inspectingPiece
            SinglePieceDialog(
                piece = piece,
                exportConfig = uiState.exportConfig,
                onDismiss = { viewModel.inspectPiece(null) },
                onSave = { viewModel.saveSinglePiece(piece) },
                onShare = { viewModel.shareSinglePiece(piece) }
            )
        }

        // Social Posting Guide Dialog
        if (uiState.showPostingGuide) {
            PostingGuideDialog(
                onDismiss = { viewModel.togglePostingGuide(false) }
            )
        }

        // 1. Social Feed Real-time Simulator (WeChat Moments / Red Carousel / Weibo)
        if (showSocialSimulator) {
            SocialFeedSimulatorDialog(
                pieces = pieces,
                onDismiss = { showSocialSimulator = false }
            )
        }

        // 2. Magazine Editorial Typography & Film Timestamps Dialog
        if (showMagazineDialog) {
            MagazineTypographyDialog(
                currentConfig = uiState.magazineConfig,
                currentLineStyle = uiState.lineStyle,
                currentShadowStyle = uiState.shadowStyle,
                currentPosterBg = com.example.model.PosterBackgroundConfig(),
                onSaveConfig = { magCfg, lineSt, shadowSt, _ ->
                    viewModel.setMagazineConfig(magCfg)
                    viewModel.setLineStyle(lineSt)
                    viewModel.setShadowStyle(shadowSt)
                    viewModel.regenerateCompositeEffect()
                    showMagazineDialog = false
                },
                onDismiss = { showMagazineDialog = false }
            )
        }

        // 3. Preset Templates Manager Sheet
        if (showPresetSheet) {
            PresetManagerBottomSheet(
                allPresets = uiState.allPresets,
                onApplyPreset = { preset ->
                    viewModel.applyStylePreset(preset)
                    viewModel.performSlice()
                    showPresetSheet = false
                },
                onSaveCurrentAsPreset = { name, desc ->
                    viewModel.saveCurrentAsPreset(name, desc)
                },
                onDeleteCustomPreset = { id ->
                    viewModel.deleteCustomPreset(id)
                },
                onDismiss = { showPresetSheet = false }
            )
        }

        NotificationToast(
            message = uiState.notificationMessage,
            onDismiss = { viewModel.clearNotification() }
        )
    }
}

@Composable
fun ExportSettingsDialog(
    config: ExportConfig,
    samplePiece: SlicePiece?,
    onDismiss: () -> Unit,
    onConfirm: (ExportConfig) -> Unit
) {
    var selectedFormat by remember { androidx.compose.runtime.mutableStateOf(config.format) }
    var selectedScale by remember { androidx.compose.runtime.mutableFloatStateOf(config.scaleFactor) }
    var quality by remember { androidx.compose.runtime.mutableIntStateOf(config.quality) }

    // Watermark State
    var watermarkEnabled by remember { androidx.compose.runtime.mutableStateOf(config.watermark.enabled) }
    var watermarkType by remember { androidx.compose.runtime.mutableStateOf(config.watermark.type) }
    var watermarkText by remember { androidx.compose.runtime.mutableStateOf(config.watermark.text) }
    var watermarkPosition by remember { androidx.compose.runtime.mutableStateOf(config.watermark.position) }
    var watermarkScope by remember { androidx.compose.runtime.mutableStateOf(config.watermark.scope) }
    var watermarkOpacity by remember { androidx.compose.runtime.mutableFloatStateOf(config.watermark.opacity) }
    var selectedEmblemTitle by remember { androidx.compose.runtime.mutableStateOf("COPYRIGHT") }

    val rawW = samplePiece?.bitmap?.width ?: 1080
    val rawH = samplePiece?.bitmap?.height ?: 1080
    val targetW = (rawW * selectedScale).roundToInt()
    val targetH = (rawH * selectedScale).roundToInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("自定义图片导出设置", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Export Format
                Text("导出图片格式", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportFormat.values().forEach { fmt ->
                        val isSelected = selectedFormat == fmt
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedFormat = fmt }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = fmt.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (fmt == ExportFormat.PNG) "无损高清 / 透明支持" else "体积小巧 / 适合社交",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 2. Resolution Scale Factor
                Text("输出分辨率规格", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                val scaleOptions = listOf(
                    Pair(1.0f, "100% 原始画质"),
                    Pair(1.5f, "150% 超清放大"),
                    Pair(0.75f, "75% 社交推荐"),
                    Pair(0.5f, "50% 紧凑省流")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    scaleOptions.forEach { (scale, label) ->
                        val isSelected = (selectedScale == scale)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedScale = scale }
                        ) {
                            Text(
                                text = label.split(" ")[0],
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // Realtime dimension preview
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "单张切片预计输出尺寸: 约 ${targetW} × ${targetH} 像素",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // 3. JPG Quality Slider (only visible when JPG is selected)
                if (selectedFormat == ExportFormat.JPG) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("JPG 压缩质量", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("$quality% (${if (quality >= 90) "高质量无损感" else if (quality >= 75) "标准平衡" else "高压缩"})", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = quality.toFloat(),
                            onValueChange = { quality = it.toInt() },
                            valueRange = 50f..100f,
                            steps = 10,
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                // 4. 🛡️ 版权水印设置 (Watermark Protection)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (watermarkEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "添加版权水印保护",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "防止图片被非法盗用与搬运",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = watermarkEnabled,
                                onCheckedChange = { watermarkEnabled = it }
                            )
                        }

                        if (watermarkEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))

                            // Watermark Type: Text vs Image
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val isText = (watermarkType == WatermarkType.TEXT)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isText) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { watermarkType = WatermarkType.TEXT }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.TextFields, null, tint = if (isText) Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("自定义文字", color = if (isText) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                val isImage = (watermarkType == WatermarkType.IMAGE)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isImage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { watermarkType = WatermarkType.IMAGE }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Image, null, tint = if (isImage) Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("图片印章/Logo", color = if (isImage) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (watermarkType == WatermarkType.TEXT) {
                                OutlinedTextField(
                                    value = watermarkText,
                                    onValueChange = { watermarkText = it },
                                    label = { Text("水印签名文字", fontSize = 12.sp) },
                                    placeholder = { Text("例如：© 摄影师小林 或 简切图") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text("选择印章徽标样式", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val emblemOptions = listOf(
                                    Pair("COPYRIGHT", "© 原创版权印"),
                                    Pair("ORIGINAL", "📸 摄影专属印"),
                                    Pair("VERIFIED", "⭐ 独家首发徽章")
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    emblemOptions.forEach { (title, label) ->
                                        val isSel = selectedEmblemTitle == title
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedEmblemTitle = title }
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Position Options
                            Text("水印摆放位置", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(WatermarkPosition.values()) { pos ->
                                    val isSel = watermarkPosition == pos
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.clickable { watermarkPosition = pos }
                                    ) {
                                        Text(
                                            text = pos.label,
                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Scope Options
                            Text("水印应用范围", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                WatermarkScope.values().forEach { scope ->
                                    val isSel = watermarkScope == scope
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { watermarkScope = scope }
                                    ) {
                                        Text(
                                            text = scope.label,
                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Opacity Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("水印不透明度", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${(watermarkOpacity * 100).toInt()}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = watermarkOpacity,
                                onValueChange = { watermarkOpacity = it },
                                valueRange = 0.2f..1.0f,
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalEmblem = if (watermarkType == WatermarkType.IMAGE) {
                        WatermarkHelper.createDefaultEmblemBitmap(selectedEmblemTitle, "简切图")
                    } else null

                    onConfirm(
                        ExportConfig(
                            format = selectedFormat,
                            scaleFactor = selectedScale,
                            quality = quality,
                            watermark = WatermarkConfig(
                                enabled = watermarkEnabled,
                                type = watermarkType,
                                text = watermarkText,
                                imageBitmap = finalEmblem,
                                position = watermarkPosition,
                                scope = watermarkScope,
                                opacity = watermarkOpacity
                            )
                        )
                    )
                }
            ) {
                Text("确定应用")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun SlicePieceCard(
    piece: SlicePiece,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (piece.bitmap != null) {
                Image(
                    bitmap = piece.bitmap.asImageBitmap(),
                    contentDescription = piece.label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )
            }

            // Top-left Order Badge
            OrderBadge(
                index = piece.index,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
            )

            // Bottom Label Tag
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(topStart = 8.dp),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Text(
                    text = piece.label,
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun CollagePreviewLayout(
    pieces: List<SlicePiece>,
    onPieceClick: (SlicePiece) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "全景拼贴连贯效果 (点击单张查看细节)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val cols = 3
                val rows = (pieces.size + cols - 1) / cols

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (r in 0 until rows) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (c in 0 until cols) {
                                val idx = r * cols + c
                                if (idx < pieces.size) {
                                    val piece = pieces[idx]
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { onPieceClick(piece) }
                                    ) {
                                        if (piece.bitmap != null) {
                                            Image(
                                                bitmap = piece.bitmap.asImageBitmap(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        OrderBadge(
                                            index = piece.index,
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(4.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SinglePieceDialog(
    piece: SlicePiece,
    exportConfig: ExportConfig,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OrderBadge(index = piece.index)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = piece.label,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${exportConfig.format.name} · ${(exportConfig.scaleFactor * 100).toInt()}% 规格",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (piece.bitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = piece.bitmap.asImageBitmap(),
                            contentDescription = piece.label,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onShare()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("分享单张", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            onSave()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("保存到相册", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PostingGuideDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("社交媒体发圈顺序指引", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "微信朋友圈、微博、小红书均采用自左至右、自上而下的九宫格排列：",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (r in 0 until 3) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                for (c in 0 until 3) {
                                    val idx = r * 3 + c + 1
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = idx.toString(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "💡 操作贴士：",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1. 保存到相册后，发朋友圈时按数字 1 到 9 的顺序依次勾选图片即可。\n2. 可在右上角「设置」中切换导出为 PNG 高清无损或 JPG 紧凑格式。\n3. 点击单张可单独保存或分享任意细节。",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("我知道了", fontWeight = FontWeight.Bold)
            }
        }
    )
}
