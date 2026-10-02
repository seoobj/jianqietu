package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LayoutType
import com.example.ui.components.M3ThemePaletteBottomSheet
import com.example.ui.components.NotificationToast
import com.example.ui.theme.M3ThemePalette
import com.example.util.SampleImageGenerator
import com.example.viewmodel.Screen
import com.example.viewmodel.SliceUiState
import com.example.viewmodel.SliceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SliceViewModel,
    uiState: SliceUiState,
    modifier: Modifier = Modifier
) {
    // Single Photo Picker contract
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.loadFromUri(uri)
        }
    }

    // Multiple Photos Picker contract
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.loadMultipleFromUris(uris)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "简切图",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            )
                            Text(
                                text = "简洁高效的多功能图片切割工坊",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleThemePaletteSheet(true) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "M3 主题色彩定制",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // 1. Hero Main Slice Showcase: Top Preview, Bottom Operations
                item {
                    HeroSliceShowcaseCard(
                        onPickPhoto = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onPickMultiplePhotos = {
                            multiplePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onOpenPreset = { sampleId ->
                            viewModel.openPresetInEditor(sampleId)
                        },
                        onLoadPresetBatch = {
                            viewModel.loadPresetBatch()
                        }
                    )
                }

                // 1.5 M3 Expressive Theme Selector Bar
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "M3 灵动调色板",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "· ${uiState.appThemePalette.title}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "全部主题 >",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { viewModel.toggleThemePaletteSheet(true) }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(M3ThemePalette.values()) { palette ->
                                    val isSelected = uiState.appThemePalette == palette
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setAppThemePalette(palette) },
                                        label = { Text(palette.title, fontSize = 11.sp) },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(palette.primaryColorHex))
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 1.5. Draft Recovery Card (if any saved project draft exists)
                if (uiState.hasSavedDraft) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.tertiary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onTertiary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "发现未完成的切图草稿",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            text = "${uiState.draftTitle ?: "草稿工程"} · ${uiState.draftTimeFormatted ?: "近期"}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    TextButton(
                                        onClick = { viewModel.discardDraft() },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("丢弃", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.restoreDraft() },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("继续编辑", fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Preset Samples Carousel
                item {
                    SectionHeader(
                        title = "即刻体验预设美图",
                        subtitle = "免找图，点击立即切图测试",
                        icon = Icons.Default.AutoAwesome
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(SampleImageGenerator.PRESETS) { preset ->
                            SamplePresetCard(
                                name = preset.name,
                                tag = preset.tag,
                                primaryColor = Color(preset.primaryColor),
                                secondaryColor = Color(preset.secondaryColor),
                                onClick = {
                                    viewModel.openPresetInEditor(preset.id)
                                }
                            )
                        }
                    }
                }

                // 3. Common Cut Modes Quick Launch
                item {
                    SectionHeader(
                        title = "核心切图版式",
                        subtitle = "支持多种比例、网格、创意与自由随心切",
                        icon = Icons.Default.GridView
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickModeButton(
                            title = "经典九宫格",
                            tag = "3×3 朋友圈爆款",
                            icon = Icons.Default.GridOn,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.setLayout(LayoutType.GRID_3x3)
                                viewModel.navigateTo(Screen.Editor)
                            }
                        )
                        QuickModeButton(
                            title = "四宫格画报",
                            tag = "2×2 极简清新",
                            icon = Icons.Default.GridView,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.setLayout(LayoutType.GRID_2x2)
                                viewModel.navigateTo(Screen.Editor)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickModeButton(
                            title = "三联全景切",
                            tag = "1×3 横屏长卷",
                            icon = Icons.Default.ViewCarousel,
                            color = Color(0xFF0D9488),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.setLayout(LayoutType.GRID_1x3)
                                viewModel.navigateTo(Screen.Editor)
                            }
                        )
                        QuickModeButton(
                            title = "交互式自由切",
                            tag = "随心拖拽切割线",
                            icon = Icons.Default.Crop,
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.setLayout(LayoutType.FREE_SPLIT_LINES)
                                viewModel.navigateTo(Screen.Editor)
                            }
                        )
                    }
                }

                // 4. Creative Shapes & Poster Highlight
                item {
                    CreativeHighlightCard(
                        onOpenHeart = {
                            viewModel.setLayout(LayoutType.CREATIVE_HEART_9)
                            viewModel.navigateTo(Screen.Editor)
                        },
                        onOpenHero = {
                            viewModel.setLayout(LayoutType.CREATIVE_HERO_SPLIT)
                            viewModel.navigateTo(Screen.Editor)
                        }
                    )
                }

                // 5. Recent Results History (if any)
                if (uiState.recentResults.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "最近切图记录",
                            subtitle = "一键再次查看切片或重新导出",
                            icon = Icons.Default.History
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        RecentHistorySection(
                            results = uiState.recentResults,
                            onSelect = { res ->
                                viewModel.navigateTo(Screen.ResultPreview)
                            }
                        )
                    }
                }

                // 6. Practical Tips Footer
                item {
                    PostingGuideFooterCard(
                        onClickGuide = {
                            viewModel.togglePostingGuide(true)
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Notification popup toast
            NotificationToast(
                message = uiState.notificationMessage,
                onDismiss = { viewModel.clearNotification() },
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // M3 Theme Palette BottomSheet
            if (uiState.showThemePaletteSheet) {
                M3ThemePaletteBottomSheet(
                    currentPalette = uiState.appThemePalette,
                    isDarkMode = uiState.isDarkMode,
                    onSelectPalette = { viewModel.setAppThemePalette(it) },
                    onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                    onDismiss = { viewModel.toggleThemePaletteSheet(false) }
                )
            }
        }
    }
}

@Composable
fun HeroSliceShowcaseCard(
    onPickPhoto: () -> Unit,
    onPickMultiplePhotos: () -> Unit,
    onOpenPreset: (String) -> Unit,
    onLoadPresetBatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showcaseMode by remember { mutableIntStateOf(0) } // 0: 3x3, 1: 2x2, 2: 1x3, 3: 创意心形

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section: Live Slicing Effect Preview
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.22f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    // Sliced Mock Canvas Content
                    when (showcaseMode) {
                        0 -> {
                            // 3x3 Nine-Grid Preview
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF6366F1), Color(0xFFEC4899), Color(0xFFF59E0B))
                                        )
                                    )
                                    .padding(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (row in 0 until 3) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        for (col in 0 until 3) {
                                            val index = row * 3 + col + 1
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                                    .background(Color.White.copy(alpha = 0.08f)),
                                                contentAlignment = Alignment.TopStart
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color.Black.copy(alpha = 0.65f),
                                                    modifier = Modifier.padding(4.dp)
                                                ) {
                                                    Text(
                                                        text = "$index",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // 2x2 Four-Grid Preview
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF06B6D4), Color(0xFF3B82F6), Color(0xFF8B5CF6))
                                        )
                                    )
                                    .padding(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (row in 0 until 2) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        for (col in 0 until 2) {
                                            val index = row * 2 + col + 1
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.2.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                                                    .background(Color.White.copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.TopStart
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color.Black.copy(alpha = 0.65f),
                                                    modifier = Modifier.padding(6.dp)
                                                ) {
                                                    Text(
                                                        text = "$index",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // 1x3 Panoramic Strip Preview
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF10B981), Color(0xFF0EA5E9), Color(0xFF6366F1))
                                        )
                                    )
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (col in 0 until 3) {
                                    val index = col + 1
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.2.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.08f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color.Black.copy(alpha = 0.65f)
                                            ) {
                                                Text(
                                                    text = "$index",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = when (index) {
                                                    1 -> "左屏"
                                                    2 -> "中屏"
                                                    else -> "右屏"
                                                },
                                                fontSize = 10.sp,
                                                color = Color.White.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        else -> {
                            // Creative Heart / Split Layout Preview
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFF43F5E), Color(0xFF8B5CF6), Color(0xFF3B82F6))
                                        )
                                    )
                                    .padding(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Big Left Hero (62%)
                                    Box(
                                        modifier = Modifier
                                            .weight(0.62f)
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.2.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.08f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("主视角海报 (大图)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    // Right 2 stacked tiles
                                    Column(
                                        modifier = Modifier
                                            .weight(0.38f)
                                            .fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.2.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("细节 1", color = Color.White, fontSize = 10.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.2.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("细节 2", color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Floating Mode Switch Pills across Top of Preview
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val modes = listOf("3×3九宫格", "2×2四宫格", "1×3全景", "创意切图")
                            modes.forEachIndexed { i, title ->
                                val isSelected = showcaseMode == i
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    modifier = Modifier.clickable { showcaseMode = i }
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Hint in Preview
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 4.dp, end = 4.dp)
                    ) {
                        Text(
                            text = "✨ 实时切图效果预览",
                            fontSize = 10.sp,
                            color = Color(0xFF67E8F9),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Text Header
            Text(
                text = "简切图 · 创意工坊",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "上面即时预览切图效果，下面一键开始操作",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Corresponding Primary Operations (下方相应操作)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onPickPhoto,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("pick_single_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "单张切图",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = onPickMultiplePhotos,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("pick_multiple_photos_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "多图批量切",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onOpenPreset("sample_sunset") },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "体验预设示例",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = onLoadPresetBatch,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewCarousel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "批量预设演练",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SamplePresetCard(
    name: String,
    tag: String,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .width(135.dp)
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Brush.linearGradient(listOf(primaryColor, secondaryColor))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(32.dp)
                )
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
fun QuickModeButton(
    title: String,
    tag: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
            .clickable { onClick() }
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
fun CreativeHighlightCard(
    onOpenHeart: () -> Unit,
    onOpenHero: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFF43F5E),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "创意心型与画报排版",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenHeart,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF43F5E)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("心形九宫格", fontSize = 13.sp)
                }
                Button(
                    onClick = onOpenHero,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("主角大图拼", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun RecentHistorySection(
    results: List<com.example.model.SliceResult>,
    onSelect: (com.example.model.SliceResult) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(results) { res ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp,
                modifier = Modifier
                    .width(160.dp)
                    .clickable { onSelect(res) }
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = res.layoutType.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "共 ${res.pieces.size} 个切片",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PostingGuideFooterCard(
    onClickGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClickGuide() }
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "9",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "微信/小红书 发圈不乱序小妙招",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "了解切图序号 1~9 在朋友圈中的排列对应关系",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }
    }
}
