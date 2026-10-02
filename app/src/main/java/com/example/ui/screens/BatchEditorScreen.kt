package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DimensionStepper
import com.example.model.AspectRatioOption
import com.example.model.FilterCategory
import com.example.model.ImageFilterPreset
import com.example.model.LayoutType
import com.example.ui.components.NotificationToast
import com.example.viewmodel.Screen
import com.example.viewmodel.SliceUiState
import com.example.viewmodel.SliceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchEditorScreen(
    viewModel: SliceViewModel,
    uiState: SliceUiState,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.Home)
    }

    val multiplePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addMoreToBatch(uris)
        }
    }

    val images = uiState.batchImages
    var batchFilterCategory by remember { androidx.compose.runtime.mutableStateOf(FilterCategory.ALL) }
    val piecesPerImage = when (uiState.batchLayout) {
        LayoutType.GRID_3x3 -> 9
        LayoutType.GRID_2x2 -> 4
        LayoutType.GRID_1x3, LayoutType.GRID_3x1 -> 3
        LayoutType.GRID_2x3, LayoutType.GRID_3x2 -> 6
        LayoutType.GRID_3x4 -> 12
        LayoutType.CREATIVE_HERO_SPLIT -> 3
        LayoutType.CREATIVE_TOP_HERO -> 4
        LayoutType.CREATIVE_HEART_9 -> 9
        LayoutType.GRID_CUSTOM -> uiState.batchCustomRows * uiState.batchCustomCols
        else -> 9
    }
    val totalEstimatedPieces = images.size * piecesPerImage

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "批量切图工坊",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "一次配置 · 统一批处理所有图片",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Home) },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回主页"
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (uiState.isProcessing) {
                        LinearProgressIndicator(
                            progress = { uiState.batchProcessingProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "正在处理: 第 ${uiState.batchCurrentProcessingIndex}/${images.size} 张图片...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Button(
                        onClick = { viewModel.performBatchSlice() },
                        enabled = images.isNotEmpty() && !uiState.isProcessing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_batch_slice_button")
                    ) {
                        if (uiState.isProcessing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("正在高速批量切片中...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.ContentCut, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "一键批量切图 (共 ${images.size} 张图 · 生成 $totalEstimatedPieces 个切片)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
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
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Batch Stats & Image Carousel
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "待切图片队列 (${images.size} 张)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = "单图 $piecesPerImage 格 · 共 $totalEstimatedPieces 格",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Horizontal Thumbnails List
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(images) { item ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.1f))
                            ) {
                                if (item.bitmap != null) {
                                    Image(
                                        bitmap = item.bitmap.asImageBitmap(),
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                // Delete badge
                                IconButton(
                                    onClick = { viewModel.removeBatchImage(item.id) },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(2.dp)
                                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "移除",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Add More Button
                        item {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .size(80.dp)
                                    .clickable {
                                        multiplePickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "加图",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "加图",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Unified Layout Configuration Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "统一切图版式 (应用于队列中所有图片)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Layout Presets for Batch
                val batchLayoutPresets = listOf(
                    LayoutType.GRID_3x3,
                    LayoutType.GRID_2x2,
                    LayoutType.GRID_1x3,
                    LayoutType.GRID_3x1,
                    LayoutType.GRID_2x3,
                    LayoutType.CREATIVE_HERO_SPLIT,
                    LayoutType.CREATIVE_TOP_HERO,
                    LayoutType.CREATIVE_HEART_9,
                    LayoutType.GRID_CUSTOM
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(batchLayoutPresets) { layout ->
                        FilterChip(
                            selected = uiState.batchLayout == layout,
                            onClick = { viewModel.setBatchLayout(layout) },
                            label = { Text(layout.title, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Custom Steppers if CUSTOM
                if (uiState.batchLayout == LayoutType.GRID_CUSTOM) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DimensionStepper(
                            label = "行数",
                            value = uiState.batchCustomRows,
                            onValueChange = { viewModel.setBatchCustomDimensions(it, uiState.batchCustomCols) }
                        )
                        DimensionStepper(
                            label = "列数",
                            value = uiState.batchCustomCols,
                            onValueChange = { viewModel.setBatchCustomDimensions(uiState.batchCustomRows, it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Unified Aspect Ratio
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AspectRatio,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "统一裁剪比例",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AspectRatioOption.values()) { opt ->
                        val isSelected = uiState.batchAspectRatio == opt
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { viewModel.setBatchAspectRatio(opt) }
                        ) {
                            Text(
                                text = opt.label,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Unified Filter Palette (Make all batch photos visually matching!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "统一摄影大师滤镜 (打造统一色系朋友圈)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = uiState.batchFilter.label,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Tabs for Batch Filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(FilterCategory.values()) { cat ->
                        val isCatSelected = batchFilterCategory == cat
                        FilterChip(
                            selected = isCatSelected,
                            onClick = { batchFilterCategory = cat },
                            label = {
                                Text(
                                    text = "${cat.iconEmoji} ${cat.label}",
                                    fontSize = 11.sp,
                                    fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val batchFilteredList = ImageFilterPreset.values().filter {
                    batchFilterCategory == FilterCategory.ALL || it.category == batchFilterCategory
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(batchFilteredList) { filter ->
                        val isSelected = uiState.batchFilter == filter
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(filter.colorTagHex).copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.clickable { viewModel.setBatchFilter(filter) }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(filter.colorTagHex))
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = filter.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = filter.description,
                                    fontSize = 9.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Unified Sliders: Gap and Corner Radius
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "留白间距: ${(uiState.batchGapFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Slider(
                            value = uiState.batchGapFraction,
                            onValueChange = { viewModel.setBatchGap(it) },
                            valueRange = 0f..0.12f,
                            modifier = Modifier.height(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "边框圆角: ${uiState.batchCornerRadiusDp.toInt()}dp",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Slider(
                            value = uiState.batchCornerRadiusDp,
                            onValueChange = { viewModel.setBatchCornerRadius(it) },
                            valueRange = 0f..24f,
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        NotificationToast(
            message = uiState.notificationMessage,
            onDismiss = { viewModel.clearNotification() }
        )
    }
}
