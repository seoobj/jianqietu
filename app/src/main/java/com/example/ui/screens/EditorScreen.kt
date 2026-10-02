package com.example.ui.screens

import android.graphics.RectF
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LineWeight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
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
import java.util.Locale
import kotlin.math.abs
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioOption
import com.example.model.FilterCategory
import com.example.model.ImageFilterPreset
import com.example.model.LayoutCategory
import com.example.model.LayoutType
import com.example.model.RecommendedLayout
import com.example.model.SliceCustomConfig
import com.example.model.SocialDetectionResult
import com.example.model.SocialPlatform
import com.example.model.SocialRatioPreset
import com.example.ui.components.DimensionStepper
import com.example.ui.components.M3ThemePaletteBottomSheet
import com.example.ui.components.NotificationToast
import com.example.ui.components.SliceCanvasOverlay
import com.example.ui.components.SliceMicroAdjustDialog
import com.example.util.ImageEditorHelper
import com.example.viewmodel.Screen
import com.example.viewmodel.SliceUiState
import com.example.viewmodel.SliceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: SliceViewModel,
    uiState: SliceUiState,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.saveCurrentAsDraft()
        viewModel.navigateTo(Screen.Home)
    }

    var isPanelExpanded by remember { mutableStateOf(true) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    var isDraggingGap by remember { mutableStateOf(false) }
    var isDraggingRadius by remember { mutableStateOf(false) }
    var isDraggingLineWidth by remember { mutableStateOf(false) }
    var isDraggingBrightness by remember { mutableStateOf(false) }
    var isDraggingContrast by remember { mutableStateOf(false) }
    var isDraggingSaturation by remember { mutableStateOf(false) }
    var isDraggingOuterRadius by remember { mutableStateOf(false) }
    var isDraggingOuterPadding by remember { mutableStateOf(false) }
    var selectedFilterCategory by remember { mutableStateOf(FilterCategory.ALL) }

    val changePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.loadFromUri(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.selectedLayout.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = uiState.selectedLayout.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.saveCurrentAsDraft()
                            viewModel.navigateTo(Screen.Home)
                        },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回主页"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleThemePaletteSheet(true) },
                        modifier = Modifier.testTag("theme_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "M3 主题色彩",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = uiState.canUndo,
                        modifier = Modifier.testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "撤销",
                            tint = if (uiState.canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = uiState.canRedo,
                        modifier = Modifier.testTag("redo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "重做",
                            tint = if (uiState.canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        )
                    }
                    Button(
                        onClick = { viewModel.performSlice() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("perform_slice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "切图预览",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            changePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "换图", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("换图", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    // Undo and Redo in bottom dock
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = uiState.canUndo,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "撤销",
                            tint = if (uiState.canUndo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = uiState.canRedo,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "重做",
                            tint = if (uiState.canRedo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Main Slice & Export Button
                    Button(
                        onClick = { viewModel.performSlice() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("perform_slice_bottom_bar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "切图预览与导出",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
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
            // 1. Interactive Canvas Workspace (With Pinch-to-Zoom, Pan, and Fullscreen Space)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isPanelExpanded) 1.2f else 1f)
                    .background(Color(0xFF0B1120)), // studio dark canvas
                contentAlignment = Alignment.Center
            ) {
                val bitmap = uiState.sourceBitmap
                if (bitmap != null) {
                    val isRotated90or270 = (uiState.rotationAngle == 90f || uiState.rotationAngle == 270f)
                    val effectiveW = if (isRotated90or270) bitmap.height else bitmap.width
                    val effectiveH = if (isRotated90or270) bitmap.width else bitmap.height
                    val imgRatio = (effectiveW.toFloat() / effectiveH.toFloat()).coerceIn(0.2f, 5f)

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val maxW = maxWidth
                        val maxH = maxHeight

                        val (viewW, viewH) = if (maxW / maxH > imgRatio) {
                            Pair(maxH * imgRatio, maxH)
                        } else {
                            Pair(maxW, maxW / imgRatio)
                        }

                        Box(
                            modifier = Modifier
                                .size(viewW, viewH)
                                .graphicsLayer {
                                    scaleX = zoomScale
                                    scaleY = zoomScale
                                    translationX = panOffsetX
                                    translationY = panOffsetY
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = { tapOffset ->
                                            if (zoomScale > 1.15f) {
                                                zoomScale = 1.0f
                                                panOffsetX = 0f
                                                panOffsetY = 0f
                                            } else {
                                                zoomScale = 2.0f
                                                val maxPanX = (viewW.toPx() * (2.0f - 1f)) / 2f
                                                val maxPanY = (viewH.toPx() * (2.0f - 1f)) / 2f
                                                panOffsetX = (viewW.toPx() / 2f - tapOffset.x).coerceIn(-maxPanX, maxPanX)
                                                panOffsetY = (viewH.toPx() / 2f - tapOffset.y).coerceIn(-maxPanY, maxPanY)
                                            }
                                        }
                                    )
                                }
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        val newScale = (zoomScale * zoom).coerceIn(1.0f, 5.0f)
                                        zoomScale = newScale
                                        if (newScale > 1.0f) {
                                            val maxPanX = (viewW.toPx() * (newScale - 1f)) / 2f + 30f
                                            val maxPanY = (viewH.toPx() * (newScale - 1f)) / 2f + 30f
                                            panOffsetX = (panOffsetX + pan.x).coerceIn(-maxPanX, maxPanX)
                                            panOffsetY = (panOffsetY + pan.y).coerceIn(-maxPanY, maxPanY)
                                        } else {
                                            panOffsetX = 0f
                                            panOffsetY = 0f
                                        }
                                    }
                                }
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            // Underlying Bitmap with Rotation, Flip, and Real-time ColorFilter
                            val composeColorFilter = ImageEditorHelper.createComposeColorFilter(
                                brightness = uiState.brightness,
                                contrast = uiState.contrast,
                                saturation = uiState.saturation,
                                filter = uiState.selectedFilter
                            )

                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "待切图片",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        rotationZ = uiState.rotationAngle
                                        scaleX = if (uiState.flipHorizontal) -1f else 1f
                                        scaleY = if (uiState.flipVertical) -1f else 1f
                                    },
                                contentScale = ContentScale.FillBounds,
                                colorFilter = composeColorFilter
                            )

                            // Interactive Slice Overlay
                            SliceCanvasOverlay(
                                cropRect = uiState.cropRect,
                                layoutType = uiState.selectedLayout,
                                rows = uiState.customRows,
                                cols = uiState.customCols,
                                showIndices = uiState.showIndices,
                                splitLines = uiState.splitLines,
                                freeBoxes = uiState.freeBoxes,
                                gapFraction = uiState.gapFraction,
                                gridLineColor = Color(uiState.lineColorHex),
                                gridLineWidthDp = uiState.lineWidthDp,
                                lineStyle = uiState.lineStyle,
                                shadowStyle = uiState.shadowStyle,
                                outerCornerRadiusDp = uiState.outerCornerRadiusDp,
                                isEyedropperActive = uiState.isEyedropperActive,
                                perSliceConfigs = uiState.perSliceConfigs,
                                selectedPieceIndex = uiState.selectedPieceIndex,
                                activeSplitLineId = uiState.activeSplitLineId,
                                onCropRectChange = { viewModel.setCropRect(it) },
                                onSplitLineMove = { id, pos -> viewModel.updateSplitLine(id, pos) },
                                onSplitLineClick = { id -> viewModel.setActiveSplitLine(id) },
                                onSplitLineDelete = { id -> viewModel.removeSplitLine(id) },
                                onSliceClick = { index -> viewModel.selectPieceForMicroAdjust(index) },
                                onEyedropperPick = { fx, fy -> viewModel.pickColorFromCoordinates(fx, fy) },
                                onDragStart = { viewModel.startSliderEdit("调整裁剪框") },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                } else {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                // Top Smart Recommendation Pill (Social Platform & Composition)
                val socialRec = uiState.socialDetectionResult
                if (socialRec != null) {
                    val bestPreset = socialRec.bestMatchPreset
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                        shape = RoundedCornerShape(20.dp),
                        tonalElevation = 6.dp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .clickable { viewModel.toggleSocialPlatformDialog(true) }
                            .border(
                                1.dp,
                                bestPreset.platform.color.copy(alpha = 0.6f),
                                RoundedCornerShape(20.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(bestPreset.platform.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "智能推荐：${bestPreset.platform.displayName} · ${bestPreset.ratioLabel}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${socialRec.matchScore}%契合) >",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = bestPreset.platform.color,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                } else if (bitmap != null) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = uiState.selectedLayout.title,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.5f)))
                            Text(
                                text = "${uiState.aspectRatio.label} · ${bitmap.width}×${bitmap.height}",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                            Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.5f)))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(uiState.lineColorHex))
                                        .border(0.5.dp, Color.White, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", uiState.lineWidthDp)}dp",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Face / Salient Subject Cutting Collision Avoidance Chip
                if (uiState.hasSubjectCuttingCollision) {
                    Surface(
                        color = Color(0xFFEA580C),
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 46.dp)
                            .clickable { viewModel.autoAvoidSubjectCollision() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "切割线划过人物面部 · 点击一键智能避让",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Interactive Bottom Canvas Bar: Eyedropper Status Banner or Quick Styling Bar
                if (uiState.isEyedropperActive) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(20.dp),
                        tonalElevation = 6.dp,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(uiState.lineColorHex))
                                    .border(1.5.dp, Color.White, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "吸管取色中: #${String.format(Locale.US, "%06X", (uiState.lineColorHex and 0xFFFFFFL).toInt())}",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = { viewModel.toggleEyedropper(false) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("完成", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Left Bottom Quick Tool Bar
                    Surface(
                        color = Color.Black.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 12.dp, bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Color circle quick swatch
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(uiState.lineColorHex))
                                    .border(1.dp, Color.White, CircleShape)
                                    .clickable { viewModel.toggleCustomColorDialog(true) }
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.1f", uiState.lineWidthDp)}dp",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Quick Eyedropper chip
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.clickable { viewModel.toggleEyedropper(true) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Colorize,
                                        contentDescription = "吸管取色",
                                        tint = Color(0xFF67E8F9),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("吸管", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                // Right Bottom Floating Zoom & Fullscreen Control Deck
                Surface(
                    color = Color.Black.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        IconButton(
                            onClick = {
                                zoomScale = (zoomScale - 0.25f).coerceAtLeast(1.0f)
                                if (zoomScale == 1.0f) {
                                    panOffsetX = 0f
                                    panOffsetY = 0f
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, "缩小", tint = Color.White, modifier = Modifier.size(15.dp))
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (zoomScale > 1f) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.clickable {
                                zoomScale = when {
                                    zoomScale < 1.4f -> 1.5f
                                    zoomScale < 1.9f -> 2.0f
                                    zoomScale < 2.9f -> 3.0f
                                    else -> 1.0f
                                }
                                if (zoomScale == 1.0f) {
                                    panOffsetX = 0f
                                    panOffsetY = 0f
                                }
                            }
                        ) {
                            Text(
                                text = "${(zoomScale * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                zoomScale = (zoomScale + 0.25f).coerceAtMost(4.0f)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, "放大", tint = Color.White, modifier = Modifier.size(15.dp))
                        }

                        if (zoomScale > 1.0f || panOffsetX != 0f || panOffsetY != 0f) {
                            IconButton(
                                onClick = {
                                    zoomScale = 1.0f
                                    panOffsetX = 0f
                                    panOffsetY = 0f
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.RestartAlt, "复位", tint = Color(0xFF67E8F9), modifier = Modifier.size(16.dp))
                            }
                        }

                        IconButton(
                            onClick = { isPanelExpanded = !isPanelExpanded },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isPanelExpanded) Icons.Default.Fullscreen else Icons.Default.FullscreenExit,
                                contentDescription = if (isPanelExpanded) "大图全屏预览" else "退出全屏",
                                tint = if (!isPanelExpanded) Color(0xFF38BDF8) else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 2. Bottom Function Controls (Expandable / Collapsible with Pull-Down Handle)
            if (isPanelExpanded) {
                Surface(
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 16.dp)
                    ) {
                        // Top Drag Handle & Pull-down Bar to Collapse and give large preview
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isPanelExpanded = false }
                                .pointerInput(Unit) {
                                    detectVerticalDragGestures { _, dragAmount ->
                                        if (dragAmount > 12) isPanelExpanded = false
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f))
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "下拉收起面板",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "下拉收起操作面板 · 放大图片编辑预览",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // 3-Category Dock: ✂️ 排版与比例 | 📏 线条与吸管 | 🎨 调色与滤镜
                        TabRow(
                            selectedTabIndex = uiState.editorSubTab.coerceIn(0, 2),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Tab(
                                selected = uiState.editorSubTab == 0,
                                onClick = { viewModel.setEditorSubTab(0) },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ContentCut, null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("排版比例", fontSize = 13.sp, fontWeight = if (uiState.editorSubTab == 0) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            )
                            Tab(
                                selected = uiState.editorSubTab == 1,
                                onClick = { viewModel.setEditorSubTab(1) },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LineWeight, null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("线条吸管", fontSize = 13.sp, fontWeight = if (uiState.editorSubTab == 1) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            )
                            Tab(
                                selected = uiState.editorSubTab == 2,
                                onClick = { viewModel.setEditorSubTab(2) },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ColorLens, null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("调色滤镜", fontSize = 13.sp, fontWeight = if (uiState.editorSubTab == 2) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            )
                        }

                    // Undo / Redo Quick History Strip
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
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
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.canUndo) "历史步数: ${uiState.undoCount} 步" else "历史记录: 最新",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.undo() },
                                    enabled = uiState.canUndo,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("bottom_undo_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Undo,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("撤销", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.redo() },
                                    enabled = uiState.canRedo,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("bottom_redo_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Redo,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("重做", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (uiState.editorSubTab == 0) {
                        // === TAB 0: 切图排版 ===

                        // Section A: 📱 社交媒体黄金比例推荐
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PhoneAndroid,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "社交平台常用比例",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Text(
                                    text = "查看平台分析 >",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { viewModel.toggleSocialPlatformDialog(true) }
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Platform Filter Chips
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(SocialPlatform.values()) { platform ->
                                    val isSelected = uiState.selectedSocialPlatformFilter == platform
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setSocialPlatformFilter(platform) },
                                        label = { Text(platform.shortName, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = platform.color.copy(alpha = 0.2f),
                                            selectedLabelColor = platform.color
                                        ),
                                        modifier = Modifier.height(30.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Social Ratio Presets Cards
                            val filteredPresets = if (uiState.selectedSocialPlatformFilter == SocialPlatform.ALL) {
                                SocialRatioPreset.ALL_PRESETS
                            } else {
                                SocialRatioPreset.ALL_PRESETS.filter { it.platform == uiState.selectedSocialPlatformFilter }
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(filteredPresets) { preset ->
                                    val isBestMatch = uiState.socialDetectionResult?.bestMatchPreset?.id == preset.id
                                    val isCurrentlySelected = uiState.selectedSocialPreset?.id == preset.id
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isCurrentlySelected) preset.platform.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = if (isCurrentlySelected) {
                                            androidx.compose.foundation.BorderStroke(1.5.dp, preset.platform.color)
                                        } else if (isBestMatch) {
                                            androidx.compose.foundation.BorderStroke(1.dp, preset.platform.color.copy(alpha = 0.5f))
                                        } else null,
                                        modifier = Modifier
                                            .width(138.dp)
                                            .clickable { viewModel.applySocialPreset(preset) }
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    color = preset.platform.color,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = preset.platform.shortName,
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                                Text(
                                                    text = preset.ratioLabel,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = preset.title,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1
                                            )
                                            Text(
                                                text = if (isBestMatch) "⭐ 智能最契合" else preset.description.split("，")[0],
                                                fontSize = 10.sp,
                                                color = if (isBestMatch) preset.platform.color else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = if (isBestMatch) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Section B: 布局版式分类与选择
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = "切图网格版式",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        TabRow(
                            selectedTabIndex = uiState.selectedCategory.ordinal,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            LayoutCategory.values().forEach { cat ->
                                Tab(
                                    selected = uiState.selectedCategory == cat,
                                    onClick = { viewModel.setCategory(cat) },
                                    text = {
                                        Text(
                                            text = cat.title,
                                            fontSize = 13.sp,
                                            fontWeight = if (uiState.selectedCategory == cat) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sub-presets Chip Row
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val matching = LayoutType.values().filter { it.category == uiState.selectedCategory }
                            items(matching) { type ->
                                FilterChip(
                                    selected = uiState.selectedLayout == type,
                                    onClick = { viewModel.setLayout(type) },
                                    label = { Text(type.title, fontSize = 13.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Section C: 参数微调 (比例、留白间距、圆角与序号)
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            // Custom Grid Stepper
                            if (uiState.selectedLayout == LayoutType.GRID_CUSTOM) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DimensionStepper(
                                        label = "行数 (Row)",
                                        value = uiState.customRows,
                                        onValueChange = { viewModel.setCustomDimensions(it, uiState.customCols) }
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    DimensionStepper(
                                        label = "列数 (Col)",
                                        value = uiState.customCols,
                                        onValueChange = { viewModel.setCustomDimensions(uiState.customRows, it) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // Free Split Lines Add/Remove/Snap controls
                            if (uiState.selectedLayout == LayoutType.FREE_SPLIT_LINES) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                                                    imageVector = Icons.Default.Straighten,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "自由切割线工具箱 (已设 ${uiState.splitLines.size} 条)",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                TextButton(
                                                    onClick = { viewModel.resetSplitLinesToPreset(2, 2) },
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("2×2", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                TextButton(
                                                    onClick = { viewModel.resetSplitLinesToPreset(3, 3) },
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("3×3", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { viewModel.addSplitLine(isVertical = true) },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier.weight(1f).height(36.dp)
                                            ) {
                                                Icon(Icons.Default.Add, null, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("+ 垂直线", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { viewModel.addSplitLine(isVertical = false) },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier.weight(1f).height(36.dp)
                                            ) {
                                                Icon(Icons.Default.Add, null, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("+ 水平线", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Selected Line Inspector
                                        val activeLine = uiState.splitLines.find { it.id == uiState.activeSplitLineId }
                                        if (activeLine != null) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF0284C7).copy(alpha = 0.12f),
                                                border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = if (activeLine.isVertical) "选中垂直线 · ${(activeLine.positionFraction * 100).toInt()}%" else "选中水平线 · ${(activeLine.positionFraction * 100).toInt()}%",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                        TextButton(
                                                            onClick = { viewModel.removeSplitLine(activeLine.id) },
                                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                                        ) {
                                                            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                                            Spacer(modifier = Modifier.width(2.dp))
                                                            Text("删除此线", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                                        }
                                                    }

                                                    // Precision Slider
                                                    Slider(
                                                        value = activeLine.positionFraction,
                                                        onValueChange = { viewModel.updateSplitLine(activeLine.id, it) },
                                                        valueRange = 0.05f..0.95f,
                                                        modifier = Modifier.height(28.dp)
                                                    )

                                                    // Magnetic Snap Pills
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        listOf(0.25f to "1/4", 0.333f to "1/3", 0.50f to "中心", 0.667f to "2/3", 0.75f to "3/4").forEach { (pos, label) ->
                                                            Surface(
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = if (abs(activeLine.positionFraction - pos) < 0.02f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                                modifier = Modifier
                                                                    .clickable { viewModel.updateSplitLine(activeLine.id, pos) }
                                                            ) {
                                                                Text(
                                                                    text = label,
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (abs(activeLine.positionFraction - pos) < 0.02f) Color.White else MaterialTheme.colorScheme.onSurface,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // Free Multi-Regions controls
                            if (uiState.selectedLayout == LayoutType.FREE_MULTI_REGIONS) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "已选 ${uiState.freeBoxes.size} 个局部特写",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Button(
                                        onClick = { viewModel.addFreeBox() },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("添加特写框", fontSize = 12.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // Per-Slice Micro-Adjustment & Hollow Out Dock
                            if (uiState.selectedLayout != LayoutType.FREE_SPLIT_LINES && uiState.selectedLayout != LayoutType.FREE_MULTI_REGIONS) {
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Tune,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "单格微调与留白（点击单格调整）",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }

                                            if (uiState.selectedLayout == LayoutType.GRID_3x3) {
                                                val isCenterBlank = uiState.perSliceConfigs[5]?.isBlank == true
                                                FilterChip(
                                                    selected = isCenterBlank,
                                                    onClick = { viewModel.toggleSliceBlank(5) },
                                                    label = { Text(if (isCenterBlank) "中心已留白" else "九宫格中心留白", fontSize = 10.sp) },
                                                    modifier = Modifier.height(28.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Slices Mini Chips Row
                                        val totalPiecesCount = when (uiState.selectedLayout) {
                                            LayoutType.GRID_3x3 -> 9
                                            LayoutType.GRID_2x2 -> 4
                                            LayoutType.GRID_2x3 -> 6
                                            LayoutType.GRID_3x2 -> 6
                                            LayoutType.GRID_1x3 -> 3
                                            LayoutType.GRID_3x1 -> 3
                                            LayoutType.GRID_3x4 -> 12
                                            LayoutType.GRID_CUSTOM -> (uiState.customRows * uiState.customCols).coerceIn(1, 16)
                                            LayoutType.CREATIVE_HERO_SPLIT -> 3
                                            LayoutType.CREATIVE_TOP_HERO -> 4
                                            LayoutType.CREATIVE_HEART_9 -> 9
                                            else -> 9
                                        }

                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(totalPiecesCount) { pIdx ->
                                                val pieceNumber = pIdx + 1
                                                val cfg = uiState.perSliceConfigs[pieceNumber]
                                                val isBlank = cfg?.isBlank == true
                                                val isCustomized = cfg != null && (cfg.zoomScale != 1.0f || cfg.panOffsetX != 0f || cfg.panOffsetY != 0f || cfg.rotationAngle != 0f || cfg.flipH || cfg.filterOverride != null || cfg.customText != null)
                                                val isSelected = uiState.selectedPieceIndex == pieceNumber

                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else if (isBlank) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface,
                                                    border = BorderStroke(
                                                        if (isSelected) 1.5.dp else 1.dp,
                                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                                    ),
                                                    modifier = Modifier
                                                        .clickable { viewModel.selectPieceForMicroAdjust(pieceNumber) }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "第 $pieceNumber 格",
                                                            fontSize = 11.sp,
                                                            fontWeight = if (isSelected || isCustomized) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                        )
                                                        if (isBlank) {
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text("(留白)", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                        } else if (isCustomized) {
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text("★", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // Gap and Corner Radius Sliders
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "切片留白间距: ${(uiState.gapFraction * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Slider(
                                        value = uiState.gapFraction,
                                        onValueChange = {
                                            if (!isDraggingGap) {
                                                isDraggingGap = true
                                                viewModel.startSliderEdit("调整切片留白")
                                            }
                                            viewModel.setGapFraction(it)
                                        },
                                        onValueChangeFinished = { isDraggingGap = false },
                                        valueRange = 0f..0.12f,
                                        modifier = Modifier.height(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "切片圆角: ${uiState.cornerRadiusDp.toInt()}dp",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Slider(
                                        value = uiState.cornerRadiusDp,
                                        onValueChange = {
                                            if (!isDraggingRadius) {
                                                isDraggingRadius = true
                                                viewModel.startSliderEdit("调整切片圆角")
                                            }
                                            viewModel.setCornerRadiusDp(it)
                                        },
                                        onValueChangeFinished = { isDraggingRadius = false },
                                        valueRange = 0f..24f,
                                        modifier = Modifier.height(28.dp)
                                    )
                                }
                            }
                        }
                    } else if (uiState.editorSubTab == 1) {
                        // === TAB 1: 📏 线条与吸管定制 ===
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                            // Show index numbers toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "显示切片发圈序号标记 (1, 2, 3...)",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Switch(
                                    checked = uiState.showIndices,
                                    onCheckedChange = { viewModel.toggleShowIndices() }
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Section D: 切图线条样式设置 (线条宽度条、颜色选择器、吸管)
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                ),
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
                                                imageVector = Icons.Default.LineWeight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "切图分割线条样式",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }

                                        // Eyedropper Tool button
                                        Button(
                                            onClick = { viewModel.toggleEyedropper(!uiState.isEyedropperActive) },
                                            colors = if (uiState.isEyedropperActive) {
                                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            } else {
                                                ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Colorize,
                                                contentDescription = "吸管取色",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (uiState.isEyedropperActive) "吸管激活中" else "吸管取色",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // 1. Line Width Slider (线条宽度条)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "线条宽度",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", uiState.lineWidthDp)} dp",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Slider(
                                        value = uiState.lineWidthDp,
                                        onValueChange = {
                                            if (!isDraggingLineWidth) {
                                                isDraggingLineWidth = true
                                                viewModel.startSliderEdit("调整线条宽度")
                                            }
                                            viewModel.setLineWidthDp(it)
                                        },
                                        onValueChangeFinished = { isDraggingLineWidth = false },
                                        valueRange = 0.5f..8.0f,
                                        modifier = Modifier.height(28.dp)
                                    )

                                    // Quick Width Preset Chips
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val widthPresets = listOf(
                                            1.0f to "极细 1dp",
                                            2.0f to "标准 2dp",
                                            3.5f to "清晰 3.5dp",
                                            5.0f to "加粗 5dp"
                                        )
                                        widthPresets.forEach { (w, label) ->
                                            val isSelected = kotlin.math.abs(uiState.lineWidthDp - w) < 0.2f
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    viewModel.startSliderEdit("设线条为 $label")
                                                    viewModel.setLineWidthDp(w)
                                                },
                                                label = { Text(label, fontSize = 10.sp) },
                                                modifier = Modifier.weight(1f),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // 2. Color Picker (颜色选择器)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "线条颜色选择",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { viewModel.toggleCustomColorDialog(true) }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(uiState.lineColorHex))
                                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "#${String.format(Locale.US, "%06X", (uiState.lineColorHex and 0xFFFFFFL).toInt())} 自定义 >",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Preset Color Swatches
                                    val presetColors = listOf(
                                        0xFF67E8F9L to "青空蓝",
                                        0xFF22C55EL to "荧光绿",
                                        0xFFFACC15L to "明黄",
                                        0xFFF43F5EL to "胭脂红",
                                        0xFFFB923CL to "日落橙",
                                        0xFFFFFFFFL to "纯白",
                                        0xFF18181BL to "曜黑",
                                        0xFFA855F7L to "电光紫",
                                        0xFF3B82F6L to "宝石蓝",
                                        0xFFEC4899L to "樱花粉"
                                    )

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        items(presetColors) { (colorHex, _) ->
                                            val isSelected = (uiState.lineColorHex and 0xFFFFFFL) == (colorHex and 0xFFFFFFL)
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(colorHex))
                                                    .border(
                                                        width = if (isSelected) 2.5.dp else 1.dp,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                                                        shape = CircleShape
                                                    )
                                                    .clickable { viewModel.setLineColor(colorHex) }
                                            ) {
                                                if (isSelected) {
                                                    val isLight = (Color(colorHex).red * 0.299 + Color(colorHex).green * 0.587 + Color(colorHex).blue * 0.114) > 0.5
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "已选",
                                                        tint = if (isLight) Color.Black else Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }

                                        item {
                                            // Custom Color Palette Button
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.surface,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clickable { viewModel.toggleCustomColorDialog(true) }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Palette,
                                                        contentDescription = "自定义颜色",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Section E: 整体效果图外框与圆角底衬 (Overall Composite Poster Corner Radius & Card Canvas)
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                ),
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
                                                imageVector = Icons.Default.Crop,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "切图大图整体圆角与底衬",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                        Text(
                                            text = "${uiState.outerCornerRadiusDp.toInt()}dp",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // 1. Overall Outer Corner Radius Slider
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "海报外框大圆角",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = if (uiState.outerCornerRadiusDp < 1f) "直角" else "圆角 ${uiState.outerCornerRadiusDp.toInt()}dp",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Slider(
                                        value = uiState.outerCornerRadiusDp,
                                        onValueChange = {
                                            if (!isDraggingOuterRadius) {
                                                isDraggingOuterRadius = true
                                                viewModel.startSliderEdit("调整整体外框圆角")
                                            }
                                            viewModel.setOuterCornerRadiusDp(it)
                                        },
                                        onValueChangeFinished = { isDraggingOuterRadius = false },
                                        valueRange = 0f..48f,
                                        modifier = Modifier.height(28.dp)
                                    )

                                    // Preset Corner Radius Quick Chips
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(
                                            0f to "直角 0dp",
                                            12f to "微圆角 12dp",
                                            24f to "标准卡片 24dp",
                                            40f to "胶囊圆 40dp"
                                        ).forEach { (r, label) ->
                                            val isSelected = kotlin.math.abs(uiState.outerCornerRadiusDp - r) < 2f
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    viewModel.startSliderEdit("设置整体圆角 $label")
                                                    viewModel.setOuterCornerRadiusDp(r)
                                                },
                                                label = { Text(label, fontSize = 10.sp) },
                                                modifier = Modifier.weight(1f),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // 2. Outer Padding Margin Slider
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "海报留白衬底外边距",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${uiState.outerPaddingDp.toInt()}dp",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Slider(
                                        value = uiState.outerPaddingDp,
                                        onValueChange = {
                                            if (!isDraggingOuterPadding) {
                                                isDraggingOuterPadding = true
                                                viewModel.startSliderEdit("调整海报留白边距")
                                            }
                                            viewModel.setOuterPaddingDp(it)
                                        },
                                        onValueChangeFinished = { isDraggingOuterPadding = false },
                                        valueRange = 0f..32f,
                                        modifier = Modifier.height(28.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 3. Canvas Background Swatches (底衬颜色)
                                    Text(
                                        text = "海报底衬背景",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val bgCanvasPresets = listOf(
                                        0x00000000L to "透明PNG",
                                        0xFFFFFFFFL to "纯白卡片",
                                        0xFF18181BL to "极夜曜黑",
                                        0xFFF5F5F7L to "莫兰迪灰",
                                        0xFFFDF2F8L to "浪漫浅粉",
                                        0xFFF0FDF4L to "薄荷浅绿"
                                    )
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        items(bgCanvasPresets) { (hex, label) ->
                                            val isSelected = uiState.outerBgColorHex == hex
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                                ),
                                                modifier = Modifier.clickable {
                                                    viewModel.setOuterBgColorHex(hex)
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .clip(CircleShape)
                                                            .background(if (hex == 0x00000000L) Color.LightGray else Color(hex))
                                                            .border(0.5.dp, Color.Gray, CircleShape)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = label,
                                                        fontSize = 10.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 4. Floating Shadow Toggle
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "开启立体悬浮卡片投影",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Switch(
                                            checked = uiState.outerCardShadow,
                                            onCheckedChange = { viewModel.toggleOuterCardShadow(it) }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // === TAB 2: 🎨 画面美化与滤镜 ===
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            // 1. Quick Transform Actions (Rotate, Flip, Reset)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.rotateClockwise() },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.RotateRight, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("旋转90°", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.toggleFlipH() },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Flip, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (uiState.flipHorizontal) "已水平镜像" else "水平镜像", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.resetEdits() },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.RestartAlt, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("重置", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 2. Preset Filters (Categorized & Top-tier Photography Tones)
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
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "摄影大师影调滤镜",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Text(
                                    text = "当前: ${uiState.selectedFilter.label}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Category Tabs Carousel
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(FilterCategory.values()) { cat ->
                                    val isCatSelected = selectedFilterCategory == cat
                                    FilterChip(
                                        selected = isCatSelected,
                                        onClick = { selectedFilterCategory = cat },
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

                            Spacer(modifier = Modifier.height(10.dp))

                            // Filters List
                            val filteredList = ImageFilterPreset.values().filter {
                                selectedFilterCategory == FilterCategory.ALL || it.category == selectedFilterCategory
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(filteredList) { filter ->
                                    val isSelected = uiState.selectedFilter == filter
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color(filter.colorTagHex).copy(alpha = 0.35f)
                                        ),
                                        modifier = Modifier.clickable { viewModel.setFilter(filter) }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(filter.colorTagHex))
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = filter.label,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = filter.description,
                                                fontSize = 10.sp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 3. Brightness Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Brightness6, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("亮度调整", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                                }
                                Text(
                                    text = "${((uiState.brightness) * 200).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Slider(
                                value = uiState.brightness,
                                onValueChange = {
                                    if (!isDraggingBrightness) {
                                        isDraggingBrightness = true
                                        viewModel.startSliderEdit("调整画面亮度")
                                    }
                                    viewModel.setBrightness(it)
                                },
                                onValueChangeFinished = { isDraggingBrightness = false },
                                valueRange = -0.4f..0.4f,
                                modifier = Modifier.height(28.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 4. Contrast Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Contrast, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("对比度", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                                }
                                Text(
                                    text = "${(uiState.contrast * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Slider(
                                value = uiState.contrast,
                                onValueChange = {
                                    if (!isDraggingContrast) {
                                        isDraggingContrast = true
                                        viewModel.startSliderEdit("调整画面对比度")
                                    }
                                    viewModel.setContrast(it)
                                },
                                onValueChangeFinished = { isDraggingContrast = false },
                                valueRange = 0.6f..1.6f,
                                modifier = Modifier.height(28.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 5. Saturation Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ColorLens, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.tertiary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("饱和度", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                                }
                                Text(
                                    text = "${(uiState.saturation * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Slider(
                                value = uiState.saturation,
                                onValueChange = {
                                    if (!isDraggingSaturation) {
                                        isDraggingSaturation = true
                                        viewModel.startSliderEdit("调整画面饱和度")
                                    }
                                    viewModel.setSaturation(it)
                                },
                                onValueChangeFinished = { isDraggingSaturation = false },
                                valueRange = 0.0f..2.0f,
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }
            }
        } else {
                // Collapsed State: Sleek Quick Dock Bar
                Surface(
                    tonalElevation = 6.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount < -10) isPanelExpanded = true
                            }
                        }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Drag bar handle to expand
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isPanelExpanded = true }
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f))
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = uiState.editorSubTab == 0,
                                    onClick = {
                                        viewModel.setEditorSubTab(0)
                                        isPanelExpanded = true
                                    },
                                    label = { Text("✂️ 排版比例", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                FilterChip(
                                    selected = uiState.editorSubTab == 1,
                                    onClick = {
                                        viewModel.setEditorSubTab(1)
                                        isPanelExpanded = true
                                    },
                                    label = { Text("✏️ 线条吸管", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                FilterChip(
                                    selected = uiState.editorSubTab == 2,
                                    onClick = {
                                        viewModel.setEditorSubTab(2)
                                        isPanelExpanded = true
                                    },
                                    label = { Text("🎨 调色滤镜", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            OutlinedButton(
                                onClick = { isPanelExpanded = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("展开面板", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Per-Slice Micro-Adjustment Dialog
        if (uiState.showSliceDetailSheet && uiState.selectedPieceIndex != null) {
            val pIdx = uiState.selectedPieceIndex!!
            val cfg = uiState.perSliceConfigs[pIdx] ?: SliceCustomConfig()
            SliceMicroAdjustDialog(
                pieceIndex = pIdx,
                config = cfg,
                onConfigChange = { updated ->
                    viewModel.updateSliceConfig(pIdx) { updated }
                },
                onReset = { viewModel.resetSliceConfig(pIdx) },
                onDismiss = { viewModel.dismissSliceDetailSheet() }
            )
        }

        // Social Platform Media Optimization Dialog
        if (uiState.showSocialPlatformDialog && uiState.socialDetectionResult != null) {
            SocialPlatformDetailDialog(
                result = uiState.socialDetectionResult,
                onDismiss = { viewModel.toggleSocialPlatformDialog(false) },
                onSelectPreset = { preset -> viewModel.applySocialPreset(preset) }
            )
        }

        // Custom Color Picker Dialog
        if (uiState.showCustomColorDialog) {
            CustomColorPickerDialog(
                currentColorHex = uiState.lineColorHex,
                onColorSelected = { viewModel.setLineColor(it) },
                onActivateEyedropper = { viewModel.toggleEyedropper(true) },
                onDismiss = { viewModel.toggleCustomColorDialog(false) }
            )
        }

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

        NotificationToast(
            message = uiState.notificationMessage,
            onDismiss = { viewModel.clearNotification() }
        )
    }
}

@Composable
fun SocialPlatformDetailDialog(
    result: SocialDetectionResult,
    onDismiss: () -> Unit,
    onSelectPreset: (SocialRatioPreset) -> Unit
) {
    val best = result.bestMatchPreset

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = best.platform.color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("社交媒体平台智能适配", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Best Platform Feature Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = best.platform.color.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, best.platform.color),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "首选推荐：${best.platform.displayName}",
                                fontWeight = FontWeight.Bold,
                                color = best.platform.color,
                                fontSize = 14.sp
                            )
                            Surface(
                                color = best.platform.color,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${result.matchScore}% 契合",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = result.platformHighlight,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "原图比例 ${result.nativeRatioLabel} · 裁剪边缘损耗仅约 ${result.cropLossPercent}%",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { onSelectPreset(best) },
                            colors = ButtonDefaults.buttonColors(containerColor = best.platform.color),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("立即适配此比例", fontSize = 12.sp)
                        }
                    }
                }

                Text(
                    text = "其他主流社媒常用尺寸：",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )

                // Alternative platform presets
                result.allRecommendedPresets.forEach { preset ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPreset(preset) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = preset.platform.color,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = preset.platform.shortName,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${preset.title} (${preset.ratioLabel})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = preset.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { onSelectPreset(preset) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text("选择", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
fun CustomColorPickerDialog(
    currentColorHex: Long,
    onColorSelected: (Long) -> Unit,
    onActivateEyedropper: () -> Unit,
    onDismiss: () -> Unit
) {
    val initialR = ((currentColorHex shr 16) and 0xFF).toFloat()
    val initialG = ((currentColorHex shr 8) and 0xFF).toFloat()
    val initialB = (currentColorHex and 0xFF).toFloat()

    var r by remember { mutableStateOf(initialR) }
    var g by remember { mutableStateOf(initialG) }
    var b by remember { mutableStateOf(initialB) }

    val currentColorLong = (0xFF000000L or ((r.toInt() and 0xFF).toLong() shl 16) or ((g.toInt() and 0xFF).toLong() shl 8) or (b.toInt() and 0xFF).toLong())
    val hexString = String.format(Locale.US, "#%06X", (currentColorLong and 0xFFFFFFL).toInt())
    var hexInputText by remember { mutableStateOf(hexString) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("自定义切图线条颜色", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Color preview circle and hex tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(currentColorLong))
                                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("当前选色", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(hexString, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Eyedropper shortcut button
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onActivateEyedropper()
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Colorize, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("吸管取色", fontSize = 12.sp)
                    }
                }

                // RGB sliders
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("红 (R): ${r.toInt()}", fontSize = 12.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = r,
                        onValueChange = {
                            r = it
                            val newLong = (0xFF000000L or ((r.toInt() and 0xFF).toLong() shl 16) or ((g.toInt() and 0xFF).toLong() shl 8) or (b.toInt() and 0xFF).toLong())
                            hexInputText = String.format(Locale.US, "#%06X", (newLong and 0xFFFFFFL).toInt())
                        },
                        valueRange = 0f..255f,
                        modifier = Modifier.height(28.dp)
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("绿 (G): ${g.toInt()}", fontSize = 12.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = g,
                        onValueChange = {
                            g = it
                            val newLong = (0xFF000000L or ((r.toInt() and 0xFF).toLong() shl 16) or ((g.toInt() and 0xFF).toLong() shl 8) or (b.toInt() and 0xFF).toLong())
                            hexInputText = String.format(Locale.US, "#%06X", (newLong and 0xFFFFFFL).toInt())
                        },
                        valueRange = 0f..255f,
                        modifier = Modifier.height(28.dp)
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("蓝 (B): ${b.toInt()}", fontSize = 12.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = b,
                        onValueChange = {
                            b = it
                            val newLong = (0xFF000000L or ((r.toInt() and 0xFF).toLong() shl 16) or ((g.toInt() and 0xFF).toLong() shl 8) or (b.toInt() and 0xFF).toLong())
                            hexInputText = String.format(Locale.US, "#%06X", (newLong and 0xFFFFFFL).toInt())
                        },
                        valueRange = 0f..255f,
                        modifier = Modifier.height(28.dp)
                    )
                }

                // Hex input field
                OutlinedTextField(
                    value = hexInputText,
                    onValueChange = { input ->
                        hexInputText = input
                        val clean = input.trim().removePrefix("#")
                        if (clean.length == 6) {
                            try {
                                val parsed = clean.toLong(16)
                                r = ((parsed shr 16) and 0xFF).toFloat()
                                g = ((parsed shr 8) and 0xFF).toFloat()
                                b = (parsed and 0xFF).toFloat()
                            } catch (_: Exception) {}
                        }
                    },
                    label = { Text("十六进制颜色代码 (Hex)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected(currentColorLong)
                    onDismiss()
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
