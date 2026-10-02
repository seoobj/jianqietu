package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AspectRatioOption
import com.example.model.BatchImageItem
import com.example.model.BatchItemStatus
import com.example.model.BatchProcessResult
import com.example.model.EditorSnapshot
import com.example.model.ExportConfig
import com.example.model.ExportFormat
import com.example.model.FilmTimestampConfig
import com.example.model.ImageAnalysisResult
import com.example.model.ImageFilterPreset
import com.example.model.LayoutCategory
import com.example.model.LayoutType
import com.example.model.LineStyle
import com.example.model.MagazineTypographyConfig
import com.example.model.RecommendedLayout
import com.example.model.SliceCustomConfig
import com.example.model.SlicePiece
import com.example.model.SliceResult
import com.example.model.SocialDetectionResult
import com.example.model.SocialPlatform
import com.example.model.SocialRatioPreset
import com.example.model.SplitLine
import com.example.model.StylePresetItem
import com.example.model.TileShadowStyle
import com.example.ui.theme.M3ThemePalette
import com.example.util.BitmapHelper
import com.example.util.CompositePosterGenerator
import com.example.util.DraftManager
import com.example.util.DraftProject
import com.example.util.ImageEditorHelper
import com.example.util.ImageSaver
import com.example.util.PresetManager
import com.example.util.SampleImageGenerator
import com.example.util.SmartAnalyzer
import com.example.util.SmartSubjectDetector
import com.example.util.SocialPlatformDetector
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class Screen {
    object Home : Screen()
    object Editor : Screen()
    object ResultPreview : Screen()
    object BatchEditor : Screen()
    object BatchResult : Screen()
}

data class SliceUiState(
    val currentScreen: Screen = Screen.Home,
    val sourceBitmap: Bitmap? = null,
    val sourceTitle: String = "未选择图片",
    val cropRect: RectF = RectF(0f, 0f, 1f, 1f),
    val aspectRatio: AspectRatioOption = AspectRatioOption.SQUARE_1_1,
    val selectedLayout: LayoutType = LayoutType.GRID_3x3,
    val selectedCategory: LayoutCategory = LayoutCategory.GRID,
    val customRows: Int = 3,
    val customCols: Int = 3,
    val gapFraction: Float = 0.02f,
    val cornerRadiusDp: Float = 4f,
    val showIndices: Boolean = true,
    val splitLines: List<SplitLine> = listOf(
        SplitLine("v1", isVertical = true, positionFraction = 0.5f),
        SplitLine("h1", isVertical = false, positionFraction = 0.5f)
    ),
    val freeBoxes: List<RectF> = listOf(
        RectF(0.05f, 0.05f, 0.45f, 0.45f),
        RectF(0.55f, 0.55f, 0.95f, 0.95f)
    ),

    // Image Editing & Tuning Properties
    val rotationAngle: Float = 0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val brightness: Float = 0f,
    val contrast: Float = 1.0f,
    val saturation: Float = 1.0f,
    val selectedFilter: ImageFilterPreset = ImageFilterPreset.ORIGINAL,
    val editorSubTab: Int = 0,

    // Smart Layout Recommendation
    val analysisResult: ImageAnalysisResult? = null,
    val showSmartDialog: Boolean = false,

    // Social Media Platform Detection & Recommendation
    val socialDetectionResult: SocialDetectionResult? = null,
    val selectedSocialPlatformFilter: SocialPlatform = SocialPlatform.ALL,
    val selectedSocialPreset: SocialRatioPreset? = null,
    val showSocialPlatformDialog: Boolean = false,

    // Export Options
    val exportConfig: ExportConfig = ExportConfig(),
    val showExportSettings: Boolean = false,

    // Batch Processing State
    val batchImages: List<BatchImageItem> = emptyList(),
    val batchLayout: LayoutType = LayoutType.GRID_3x3,
    val batchAspectRatio: AspectRatioOption = AspectRatioOption.SQUARE_1_1,
    val batchGapFraction: Float = 0.02f,
    val batchCornerRadiusDp: Float = 4f,
    val batchFilter: ImageFilterPreset = ImageFilterPreset.ORIGINAL,
    val batchCustomRows: Int = 3,
    val batchCustomCols: Int = 3,
    val batchProcessingProgress: Float = 0f, // 0.0 .. 1.0
    val batchCurrentProcessingIndex: Int = 0,
    val batchProcessResult: BatchProcessResult? = null,

    val isProcessing: Boolean = false,
    val sliceResult: SliceResult? = null,
    val recentResults: List<SliceResult> = emptyList(),
    val notificationMessage: String? = null,
    val inspectingPiece: SlicePiece? = null,
    val showPostingGuide: Boolean = false,

    // Undo / Redo History Status
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val undoCount: Int = 0,
    val redoCount: Int = 0,

    // Cutting Line Style (Width & Color & Eyedropper)
    val lineColorHex: Long = 0xFFFFFFFF, // Pure White default, clean grid lines matching sample
    val lineWidthDp: Float = 3.0f,
    val isEyedropperActive: Boolean = false,
    val eyedropperSampleColorHex: Long? = null,
    val showCustomColorDialog: Boolean = false,

    // Single Composite Grid Effect Image (带切割线单张效果大图 & 整体圆角底衬)
    val compositeEffectBitmap: Bitmap? = null,
    val outerCornerRadiusDp: Float = 0f,
    val outerPaddingDp: Float = 0f,
    val outerBgColorHex: Long = 0x00000000L,
    val outerCardShadow: Boolean = false,

    // Per-slice Micro-adjustment state
    val perSliceConfigs: Map<Int, SliceCustomConfig> = emptyMap(),
    val selectedPieceIndex: Int? = null,
    val showSliceDetailSheet: Boolean = false,
    val activeSplitLineId: String? = null,

    // Line & Shadow Styles
    val lineStyle: LineStyle = LineStyle.SOLID,
    val shadowStyle: TileShadowStyle = TileShadowStyle.NONE,

    // Magazine Typography & Film Timestamps
    val magazineConfig: MagazineTypographyConfig = MagazineTypographyConfig(),
    val filmTimestampConfig: FilmTimestampConfig = FilmTimestampConfig(),

    // Presets & Templates
    val allPresets: List<StylePresetItem> = emptyList(),
    val showPresetSheet: Boolean = false,

    // M3 Expressive Theme Customization
    val appThemePalette: M3ThemePalette = M3ThemePalette.AURORA_INDIGO,
    val isDarkMode: Boolean? = null, // null = follow system, true = force dark, false = force light
    val showThemePaletteSheet: Boolean = false,

    // Draft & Auto-Save Recovery
    val hasSavedDraft: Boolean = false,
    val draftTitle: String? = null,
    val draftTimeFormatted: String? = null,

    // Smart Subject & Face Avoidance
    val detectedFaceRect: RectF? = null,
    val hasSubjectCuttingCollision: Boolean = false,
    val subjectCollisionPrompt: String? = null,

    // Posting Sequence Card
    val showPostingSequenceDialog: Boolean = false
)

class SliceViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SliceUiState())
    val uiState: StateFlow<SliceUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<EditorSnapshot>()
    private val redoStack = mutableListOf<EditorSnapshot>()
    private val maxHistorySize = 30

    fun setAppThemePalette(palette: M3ThemePalette) {
        _uiState.update {
            it.copy(
                appThemePalette = palette,
                showThemePaletteSheet = false,
                notificationMessage = "已切换 M3 主题色彩：${palette.title}"
            )
        }
    }

    fun toggleDarkMode(isDark: Boolean?) {
        _uiState.update {
            val label = when (isDark) {
                true -> "深色模式"
                false -> "浅色模式"
                null -> "跟随系统"
            }
            it.copy(isDarkMode = isDark, notificationMessage = "已切换为：$label")
        }
    }

    fun toggleThemePaletteSheet(show: Boolean) {
        _uiState.update { it.copy(showThemePaletteSheet = show) }
    }

    init {
        loadPresets()
    }

    fun loadPresets() {
        val list = PresetManager.loadAllPresets(getApplication())
        _uiState.update { it.copy(allPresets = list) }
    }

    fun createCurrentSnapshot(desc: String = ""): EditorSnapshot {
        val s = _uiState.value
        return EditorSnapshot(
            cropRect = RectF(s.cropRect),
            aspectRatio = s.aspectRatio,
            selectedLayout = s.selectedLayout,
            selectedCategory = s.selectedCategory,
            customRows = s.customRows,
            customCols = s.customCols,
            gapFraction = s.gapFraction,
            cornerRadiusDp = s.cornerRadiusDp,
            splitLines = s.splitLines.toList(),
            freeBoxes = s.freeBoxes.map { RectF(it) },
            rotationAngle = s.rotationAngle,
            flipHorizontal = s.flipHorizontal,
            flipVertical = s.flipVertical,
            brightness = s.brightness,
            contrast = s.contrast,
            saturation = s.saturation,
            selectedFilter = s.selectedFilter,
            lineColorHex = s.lineColorHex,
            lineWidthDp = s.lineWidthDp,
            lineStyle = s.lineStyle,
            shadowStyle = s.shadowStyle,
            outerCornerRadiusDp = s.outerCornerRadiusDp,
            outerPaddingDp = s.outerPaddingDp,
            outerBgColorHex = s.outerBgColorHex,
            outerCardShadow = s.outerCardShadow,
            description = desc
        )
    }

    private fun isSameSnapshot(a: EditorSnapshot, b: EditorSnapshot): Boolean {
        return a.cropRect == b.cropRect &&
                a.aspectRatio == b.aspectRatio &&
                a.selectedLayout == b.selectedLayout &&
                a.selectedCategory == b.selectedCategory &&
                a.customRows == b.customRows &&
                a.customCols == b.customCols &&
                kotlin.math.abs(a.gapFraction - b.gapFraction) < 0.001f &&
                kotlin.math.abs(a.cornerRadiusDp - b.cornerRadiusDp) < 0.1f &&
                a.splitLines == b.splitLines &&
                a.freeBoxes == b.freeBoxes &&
                a.rotationAngle == b.rotationAngle &&
                a.flipHorizontal == b.flipHorizontal &&
                a.flipVertical == b.flipVertical &&
                kotlin.math.abs(a.brightness - b.brightness) < 0.01f &&
                kotlin.math.abs(a.contrast - b.contrast) < 0.01f &&
                kotlin.math.abs(a.saturation - b.saturation) < 0.01f &&
                a.selectedFilter == b.selectedFilter &&
                a.lineColorHex == b.lineColorHex &&
                kotlin.math.abs(a.lineWidthDp - b.lineWidthDp) < 0.05f &&
                kotlin.math.abs(a.outerCornerRadiusDp - b.outerCornerRadiusDp) < 0.1f &&
                kotlin.math.abs(a.outerPaddingDp - b.outerPaddingDp) < 0.1f &&
                a.outerBgColorHex == b.outerBgColorHex &&
                a.outerCardShadow == b.outerCardShadow
    }

    fun recordHistory(actionDescription: String = "") {
        val snapshot = createCurrentSnapshot(actionDescription)
        if (undoStack.isNotEmpty() && isSameSnapshot(undoStack.last(), snapshot)) {
            return
        }
        undoStack.add(snapshot)
        if (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
        _uiState.update {
            it.copy(
                canUndo = undoStack.isNotEmpty(),
                canRedo = false,
                undoCount = undoStack.size,
                redoCount = 0
            )
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val currentSnapshot = createCurrentSnapshot("当前状态")
        redoStack.add(currentSnapshot)
        val previousSnapshot = undoStack.removeAt(undoStack.lastIndex)
        applySnapshot(previousSnapshot)
        _uiState.update {
            it.copy(
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
                undoCount = undoStack.size,
                redoCount = redoStack.size,
                notificationMessage = if (previousSnapshot.description.isNotBlank()) "已撤销: ${previousSnapshot.description}" else "已撤销上一步操作"
            )
        }
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val currentSnapshot = createCurrentSnapshot("当前状态")
        undoStack.add(currentSnapshot)
        val nextSnapshot = redoStack.removeAt(redoStack.lastIndex)
        applySnapshot(nextSnapshot)
        _uiState.update {
            it.copy(
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
                undoCount = undoStack.size,
                redoCount = redoStack.size,
                notificationMessage = if (nextSnapshot.description.isNotBlank()) "已重做: ${nextSnapshot.description}" else "已恢复下一步操作"
            )
        }
    }

    private fun applySnapshot(snapshot: EditorSnapshot) {
        _uiState.update {
            it.copy(
                cropRect = RectF(snapshot.cropRect),
                aspectRatio = snapshot.aspectRatio,
                selectedLayout = snapshot.selectedLayout,
                selectedCategory = snapshot.selectedCategory,
                customRows = snapshot.customRows,
                customCols = snapshot.customCols,
                gapFraction = snapshot.gapFraction,
                cornerRadiusDp = snapshot.cornerRadiusDp,
                splitLines = snapshot.splitLines,
                freeBoxes = snapshot.freeBoxes.map { RectF(it) },
                rotationAngle = snapshot.rotationAngle,
                flipHorizontal = snapshot.flipHorizontal,
                flipVertical = snapshot.flipVertical,
                brightness = snapshot.brightness,
                contrast = snapshot.contrast,
                saturation = snapshot.saturation,
                selectedFilter = snapshot.selectedFilter,
                lineColorHex = snapshot.lineColorHex,
                lineWidthDp = snapshot.lineWidthDp,
                lineStyle = snapshot.lineStyle,
                shadowStyle = snapshot.shadowStyle,
                outerCornerRadiusDp = snapshot.outerCornerRadiusDp,
                outerPaddingDp = snapshot.outerPaddingDp,
                outerBgColorHex = snapshot.outerBgColorHex,
                outerCardShadow = snapshot.outerCardShadow
            )
        }
    }

    init {
        loadPresetSample("sunset")
        checkSavedDraft()
    }

    fun checkSavedDraft() {
        val hasDraft = DraftManager.hasDraft(getApplication())
        if (hasDraft) {
            val draftData = DraftManager.loadDraftProject(getApplication())
            if (draftData != null) {
                val (proj, _) = draftData
                val dateStr = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(proj.timestamp))
                _uiState.update {
                    it.copy(
                        hasSavedDraft = true,
                        draftTitle = proj.title,
                        draftTimeFormatted = dateStr
                    )
                }
            }
        } else {
            _uiState.update { it.copy(hasSavedDraft = false, draftTitle = null, draftTimeFormatted = null) }
        }
    }

    fun saveCurrentAsDraft() {
        val s = _uiState.value
        val bmp = s.sourceBitmap ?: return
        viewModelScope.launch(Dispatchers.IO) {
            DraftManager.saveDraft(
                context = getApplication(),
                bitmap = bmp,
                title = s.sourceTitle,
                layoutType = s.selectedLayout,
                category = s.selectedCategory,
                rows = s.customRows,
                cols = s.customCols,
                aspectRatio = s.aspectRatio,
                cropRect = s.cropRect,
                gapFraction = s.gapFraction,
                cornerRadiusDp = s.cornerRadiusDp,
                lineColorHex = s.lineColorHex,
                lineWidthDp = s.lineWidthDp,
                lineStyle = s.lineStyle,
                shadowStyle = s.shadowStyle,
                filter = s.selectedFilter,
                rotation = s.rotationAngle,
                flipH = s.flipHorizontal,
                flipV = s.flipVertical,
                brightness = s.brightness,
                contrast = s.contrast,
                saturation = s.saturation,
                splitLines = s.splitLines
            )
            checkSavedDraft()
            _uiState.update { it.copy(notificationMessage = "工程已自动保存在草稿箱") }
        }
    }

    fun restoreDraft() {
        viewModelScope.launch(Dispatchers.IO) {
            val draftData = DraftManager.loadDraftProject(getApplication())
            if (draftData != null) {
                val (proj, bmp) = draftData
                withContext(Dispatchers.Main) {
                    undoStack.clear()
                    redoStack.clear()
                    _uiState.update {
                        it.copy(
                            sourceBitmap = bmp,
                            sourceTitle = proj.title,
                            selectedLayout = proj.layoutType,
                            selectedCategory = proj.category,
                            customRows = proj.rows,
                            customCols = proj.cols,
                            aspectRatio = proj.aspectRatio,
                            cropRect = proj.cropRect,
                            gapFraction = proj.gapFraction,
                            cornerRadiusDp = proj.cornerRadiusDp,
                            lineColorHex = proj.lineColorHex,
                            lineWidthDp = proj.lineWidthDp,
                            lineStyle = proj.lineStyle,
                            shadowStyle = proj.shadowStyle,
                            selectedFilter = proj.filter,
                            rotationAngle = proj.rotation,
                            flipHorizontal = proj.flipH,
                            flipVertical = proj.flipV,
                            brightness = proj.brightness,
                            contrast = proj.contrast,
                            saturation = proj.saturation,
                            splitLines = proj.splitLines,
                            currentScreen = Screen.Editor,
                            notificationMessage = "已恢复未完成的草稿工程：${proj.title}"
                        )
                    }
                    detectSubjectCollision()
                }
            }
        }
    }

    fun discardDraft() {
        DraftManager.clearDraft(getApplication())
        _uiState.update {
            it.copy(
                hasSavedDraft = false,
                draftTitle = null,
                draftTimeFormatted = null,
                notificationMessage = "已清空草稿箱"
            )
        }
    }

    fun detectSubjectCollision() {
        val bmp = _uiState.value.sourceBitmap ?: return
        val crop = _uiState.value.cropRect
        viewModelScope.launch(Dispatchers.Default) {
            val subject = SmartSubjectDetector.detectSalientSubject(bmp, crop)
            val result = SmartSubjectDetector.analyzeCollision(
                subjectRect = subject,
                layoutType = _uiState.value.selectedLayout,
                rows = _uiState.value.customRows,
                cols = _uiState.value.customCols,
                splitLines = _uiState.value.splitLines
            )
            _uiState.update {
                it.copy(
                    detectedFaceRect = subject,
                    hasSubjectCuttingCollision = result.hasCuttingCollision,
                    subjectCollisionPrompt = if (result.hasCuttingCollision) "💡 发现切割线穿过人脸/主体中心 · 点击一键智能避让" else null
                )
            }
        }
    }

    fun autoAvoidSubjectCollision() {
        val subject = _uiState.value.detectedFaceRect ?: return
        val layout = _uiState.value.selectedLayout
        if (layout == LayoutType.FREE_SPLIT_LINES) {
            val adjusted = SmartSubjectDetector.avoidSplitLineCollision(_uiState.value.splitLines, subject)
            recordHistory("智能避让人脸切割线")
            _uiState.update {
                it.copy(
                    splitLines = adjusted,
                    hasSubjectCuttingCollision = false,
                    subjectCollisionPrompt = null,
                    notificationMessage = "✨ 已智能微调切割线，避开人物面部中心！"
                )
            }
        } else {
            val newCrop = SmartSubjectDetector.avoidGridCropCollision(_uiState.value.cropRect, 0.06f)
            recordHistory("智能避让人脸网格")
            _uiState.update {
                it.copy(
                    cropRect = newCrop,
                    hasSubjectCuttingCollision = false,
                    subjectCollisionPrompt = null,
                    notificationMessage = "✨ 已自动上移画幅，保留完整人脸！"
                )
            }
        }
    }

    fun savePostingSequenceGuideCard() {
        val pieces = _uiState.value.sliceResult?.pieces ?: return
        val layout = _uiState.value.selectedLayout
        val config = _uiState.value.exportConfig
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, notificationMessage = "正在生成并保存朋友圈防乱序发图卡...") }
            val res = ImageSaver.savePostingSequenceCard(getApplication(), pieces, layout, config)
            when (res) {
                is ImageSaver.SaveResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            notificationMessage = "🎉 防乱序发图指引卡已保存至相册！"
                        )
                    }
                }
                is ImageSaver.SaveResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            notificationMessage = res.message
                        )
                    }
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun loadFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, notificationMessage = "正在读取图片...") }
            val bitmap = withContext(Dispatchers.IO) {
                BitmapHelper.decodeSampledBitmapFromUri(getApplication(), uri)
            }
            if (bitmap != null) {
                val analysis = withContext(Dispatchers.Default) {
                    SmartAnalyzer.analyzeImage(bitmap)
                }
                val socialDetection = withContext(Dispatchers.Default) {
                    SocialPlatformDetector.detectBestPlatformRatio(bitmap.width, bitmap.height)
                }
                val initialCrop = BitmapHelper.computeCenterCropRect(
                    bitmap.width,
                    bitmap.height,
                    _uiState.value.aspectRatio.ratio
                )
                val firstRec = analysis.recommendations.firstOrNull()?.layoutType ?: LayoutType.GRID_3x3

                undoStack.clear()
                redoStack.clear()
                _uiState.update {
                    it.copy(
                        sourceBitmap = bitmap,
                        sourceTitle = "相册导入",
                        cropRect = initialCrop,
                        selectedLayout = firstRec,
                        selectedCategory = firstRec.category,
                        analysisResult = analysis,
                        socialDetectionResult = socialDetection,
                        selectedSocialPreset = socialDetection.bestMatchPreset,
                        rotationAngle = 0f,
                        flipHorizontal = false,
                        flipVertical = false,
                        brightness = 0f,
                        contrast = 1.0f,
                        saturation = 1.0f,
                        selectedFilter = ImageFilterPreset.ORIGINAL,
                        canUndo = false,
                        canRedo = false,
                        undoCount = 0,
                        redoCount = 0,
                        isProcessing = false,
                        currentScreen = Screen.Editor,
                        notificationMessage = "智能适配推荐：${socialDetection.bestMatchPreset.platform.displayName} · ${socialDetection.bestMatchPreset.ratioLabel}"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        notificationMessage = "图片加载失败，请重试"
                    )
                }
            }
        }
    }

    fun loadPresetSample(sampleId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            val bitmap = withContext(Dispatchers.Default) {
                SampleImageGenerator.generatePresetBitmap(sampleId)
            }
            val analysis = withContext(Dispatchers.Default) {
                SmartAnalyzer.analyzeImage(bitmap)
            }
            val socialDetection = withContext(Dispatchers.Default) {
                SocialPlatformDetector.detectBestPlatformRatio(bitmap.width, bitmap.height)
            }
            val preset = SampleImageGenerator.PRESETS.find { it.id == sampleId }
            val initialCrop = BitmapHelper.computeCenterCropRect(
                bitmap.width,
                bitmap.height,
                _uiState.value.aspectRatio.ratio
            )
            val firstRec = analysis.recommendations.firstOrNull()?.layoutType ?: LayoutType.GRID_3x3

            undoStack.clear()
            redoStack.clear()
            _uiState.update {
                it.copy(
                    sourceBitmap = bitmap,
                    sourceTitle = preset?.name ?: "示例图片",
                    cropRect = initialCrop,
                    selectedLayout = firstRec,
                    selectedCategory = firstRec.category,
                    analysisResult = analysis,
                    socialDetectionResult = socialDetection,
                    selectedSocialPreset = socialDetection.bestMatchPreset,
                    rotationAngle = 0f,
                    flipHorizontal = false,
                    flipVertical = false,
                    brightness = 0f,
                    contrast = 1.0f,
                    saturation = 1.0f,
                    selectedFilter = ImageFilterPreset.ORIGINAL,
                    canUndo = false,
                    canRedo = false,
                    undoCount = 0,
                    redoCount = 0,
                    isProcessing = false
                )
            }
        }
    }

    fun openPresetInEditor(sampleId: String) {
        loadPresetSample(sampleId)
        navigateTo(Screen.Editor)
    }

    fun startSliderEdit(description: String) {
        recordHistory(description)
    }

    fun setAspectRatio(option: AspectRatioOption) {
        recordHistory("切换比例: ${option.label}")
        val bitmap = _uiState.value.sourceBitmap
        val newCrop = if (bitmap != null) {
            BitmapHelper.computeCenterCropRect(bitmap.width, bitmap.height, option.ratio)
        } else {
            RectF(0f, 0f, 1f, 1f)
        }
        _uiState.update {
            it.copy(
                aspectRatio = option,
                cropRect = newCrop
            )
        }
    }

    fun setCropRect(rect: RectF) {
        _uiState.update { it.copy(cropRect = rect) }
    }

    fun setLayout(layoutType: LayoutType) {
        recordHistory("选择排版: ${layoutType.title}")
        _uiState.update {
            it.copy(
                selectedLayout = layoutType,
                selectedCategory = layoutType.category,
                customRows = if (layoutType.defaultRows > 0) layoutType.defaultRows else it.customRows,
                customCols = if (layoutType.defaultCols > 0) layoutType.defaultCols else it.customCols
            )
        }
    }

    fun setCategory(category: LayoutCategory) {
        recordHistory("切换分类: ${category.title}")
        val firstMatching = LayoutType.values().firstOrNull { it.category == category } ?: LayoutType.GRID_3x3
        _uiState.update {
            it.copy(
                selectedCategory = category,
                selectedLayout = firstMatching
            )
        }
    }

    fun setCustomDimensions(rows: Int, cols: Int) {
        recordHistory("设置网格: ${rows}×${cols}")
        _uiState.update {
            it.copy(
                customRows = rows.coerceIn(1, 10),
                customCols = cols.coerceIn(1, 10)
            )
        }
    }

    fun setGapFraction(gap: Float) {
        _uiState.update { it.copy(gapFraction = gap.coerceIn(0f, 0.15f)) }
    }

    fun setCornerRadiusDp(radius: Float) {
        _uiState.update { it.copy(cornerRadiusDp = radius.coerceIn(0f, 32f)) }
    }

    fun toggleShowIndices() {
        _uiState.update { it.copy(showIndices = !it.showIndices) }
    }

    // Cutting Line Width & Color Styling
    fun setLineColor(colorHex: Long) {
        val hexStr = String.format("#%06X", (colorHex and 0xFFFFFFL).toInt())
        recordHistory("线条颜色: $hexStr")
        _uiState.update { it.copy(lineColorHex = colorHex) }
    }

    fun setLineWidthDp(widthDp: Float) {
        _uiState.update { it.copy(lineWidthDp = widthDp.coerceIn(0.5f, 12f)) }
    }

    fun toggleEyedropper(active: Boolean) {
        _uiState.update {
            it.copy(
                isEyedropperActive = active,
                notificationMessage = if (active) "吸管已激活：点击画布任意位置吸取画面颜色" else null
            )
        }
    }

    fun pickColorFromCoordinates(fracX: Float, fracY: Float) {
        val bmp = _uiState.value.sourceBitmap ?: return
        val colorHex = ImageEditorHelper.extractColorFromBitmap(
            source = bmp,
            fracX = fracX,
            fracY = fracY,
            rotationDegrees = _uiState.value.rotationAngle,
            flipH = _uiState.value.flipHorizontal,
            flipV = _uiState.value.flipVertical
        )
        val hexStr = String.format("#%06X", (colorHex and 0xFFFFFFL).toInt())
        recordHistory("吸管吸取 $hexStr")
        _uiState.update {
            it.copy(
                lineColorHex = colorHex,
                eyedropperSampleColorHex = colorHex,
                notificationMessage = "已吸取画面颜色：$hexStr"
            )
        }
    }

    fun toggleCustomColorDialog(show: Boolean) {
        _uiState.update { it.copy(showCustomColorDialog = show) }
    }

    // Editing Controls
    fun rotateClockwise() {
        recordHistory("顺时针旋转90°")
        _uiState.update {
            it.copy(rotationAngle = (it.rotationAngle + 90f) % 360f)
        }
    }

    fun toggleFlipH() {
        recordHistory("水平镜像翻转")
        _uiState.update { it.copy(flipHorizontal = !it.flipHorizontal) }
    }

    fun toggleFlipV() {
        recordHistory("垂直翻转")
        _uiState.update { it.copy(flipVertical = !it.flipVertical) }
    }

    fun setBrightness(value: Float) {
        _uiState.update { it.copy(brightness = value.coerceIn(-0.5f, 0.5f)) }
    }

    fun setContrast(value: Float) {
        _uiState.update { it.copy(contrast = value.coerceIn(0.5f, 1.8f)) }
    }

    fun setSaturation(value: Float) {
        _uiState.update { it.copy(saturation = value.coerceIn(0.0f, 2.0f)) }
    }

    fun setFilter(filter: ImageFilterPreset) {
        recordHistory("应用滤镜: ${filter.label}")
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun resetEdits() {
        recordHistory("重置编辑参数")
        _uiState.update {
            it.copy(
                rotationAngle = 0f,
                flipHorizontal = false,
                flipVertical = false,
                brightness = 0f,
                contrast = 1.0f,
                saturation = 1.0f,
                selectedFilter = ImageFilterPreset.ORIGINAL
            )
        }
    }

    fun setEditorSubTab(tab: Int) {
        _uiState.update { it.copy(editorSubTab = tab) }
    }

    // Smart Layout Recommendation
    fun applyRecommendation(rec: RecommendedLayout) {
        recordHistory("应用推荐: ${rec.title}")
        setLayout(rec.layoutType)
        _uiState.update {
            it.copy(
                showSmartDialog = false,
                notificationMessage = "已应用智能推荐：${rec.title}"
            )
        }
    }

    fun toggleSmartDialog(show: Boolean) {
        _uiState.update { it.copy(showSmartDialog = show) }
    }

    // Social Platform Proportion Functions
    fun setSocialPlatformFilter(platform: SocialPlatform) {
        _uiState.update { it.copy(selectedSocialPlatformFilter = platform) }
    }

    fun applySocialPreset(preset: SocialRatioPreset) {
        val bitmap = _uiState.value.sourceBitmap ?: return
        recordHistory("适配社媒: ${preset.title}")
        val newCrop = BitmapHelper.computeCenterCropRect(bitmap.width, bitmap.height, preset.ratio)
        // Find matching AspectRatioOption if exists
        val matchingOpt = AspectRatioOption.values().find {
            it.ratio != null && kotlin.math.abs(it.ratio - preset.ratio) < 0.05f
        } ?: AspectRatioOption.ORIGINAL

        _uiState.update {
            it.copy(
                selectedSocialPreset = preset,
                aspectRatio = matchingOpt,
                cropRect = newCrop,
                selectedLayout = preset.recommendedLayout,
                selectedCategory = preset.recommendedLayout.category,
                customRows = if (preset.recommendedLayout.defaultRows > 0) preset.recommendedLayout.defaultRows else it.customRows,
                customCols = if (preset.recommendedLayout.defaultCols > 0) preset.recommendedLayout.defaultCols else it.customCols,
                showSocialPlatformDialog = false,
                notificationMessage = "已适配【${preset.platform.shortName} · ${preset.title} (${preset.ratioLabel})】"
            )
        }
    }

    fun toggleSocialPlatformDialog(show: Boolean) {
        _uiState.update { it.copy(showSocialPlatformDialog = show) }
    }

    // Export Options
    fun updateExportConfig(config: ExportConfig) {
        _uiState.update { it.copy(exportConfig = config) }
    }

    fun toggleExportSettings(show: Boolean) {
        _uiState.update { it.copy(showExportSettings = show) }
    }

    // Interactive Split Line controls
    fun updateSplitLine(lineId: String, newPosition: Float) {
        _uiState.update { state ->
            val updated = state.splitLines.map { line ->
                if (line.id == lineId) line.copy(positionFraction = newPosition.coerceIn(0.05f, 0.95f)) else line
            }
            state.copy(splitLines = updated)
        }
    }

    fun setActiveSplitLine(lineId: String?) {
        _uiState.update { it.copy(activeSplitLineId = lineId) }
    }

    fun addSplitLine(isVertical: Boolean) {
        val newId = "split_${System.currentTimeMillis()}"
        recordHistory(if (isVertical) "添加垂直分割线" else "添加水平分割线")
        val newLine = SplitLine(newId, isVertical, 0.5f)
        _uiState.update { it.copy(splitLines = it.splitLines + newLine, activeSplitLineId = newId) }
    }

    fun addSplitLineAtPosition(isVertical: Boolean, posFraction: Float) {
        val newId = "split_${System.currentTimeMillis()}"
        recordHistory(if (isVertical) "添加垂直分割线" else "添加水平分割线")
        val newLine = SplitLine(newId, isVertical, posFraction.coerceIn(0.05f, 0.95f))
        _uiState.update { it.copy(splitLines = it.splitLines + newLine, activeSplitLineId = newId) }
    }

    fun removeSplitLine(lineId: String) {
        recordHistory("删除分割线")
        _uiState.update { state ->
            state.copy(
                splitLines = state.splitLines.filterNot { it.id == lineId },
                activeSplitLineId = if (state.activeSplitLineId == lineId) null else state.activeSplitLineId
            )
        }
    }

    fun resetSplitLinesToPreset(rows: Int, cols: Int) {
        recordHistory("自由切割线重置为 ${rows}×${cols} 等分")
        val lines = mutableListOf<SplitLine>()
        for (c in 1 until cols) {
            lines.add(SplitLine("v_${System.currentTimeMillis()}_$c", isVertical = true, positionFraction = c.toFloat() / cols))
        }
        for (r in 1 until rows) {
            lines.add(SplitLine("h_${System.currentTimeMillis()}_$r", isVertical = false, positionFraction = r.toFloat() / rows))
        }
        _uiState.update { it.copy(splitLines = lines, activeSplitLineId = null) }
    }

    // -------------------------------------------------------------
    // Per-Slice Micro-Adjustment Controls
    // -------------------------------------------------------------
    fun selectPieceForMicroAdjust(index: Int?) {
        _uiState.update { it.copy(selectedPieceIndex = index, showSliceDetailSheet = index != null) }
    }

    fun dismissSliceDetailSheet() {
        _uiState.update { it.copy(showSliceDetailSheet = false, selectedPieceIndex = null) }
    }

    fun updateSliceConfig(index: Int, update: (SliceCustomConfig) -> SliceCustomConfig) {
        val current = _uiState.value.perSliceConfigs[index] ?: SliceCustomConfig()
        val updated = update(current)
        recordHistory("调整第 $index 格微调")
        _uiState.update { state ->
            state.copy(perSliceConfigs = state.perSliceConfigs + (index to updated))
        }
    }

    fun toggleSliceBlank(index: Int) {
        val current = _uiState.value.perSliceConfigs[index] ?: SliceCustomConfig()
        val isNowBlank = !current.isBlank
        recordHistory(if (isNowBlank) "将第 $index 格设为留白" else "恢复第 $index 格画面")
        updateSliceConfig(index) { it.copy(isBlank = isNowBlank) }
    }

    fun updateSlicePanZoom(index: Int, panX: Float, panY: Float, zoom: Float) {
        updateSliceConfig(index) {
            it.copy(
                panOffsetX = panX.coerceIn(-0.4f, 0.4f),
                panOffsetY = panY.coerceIn(-0.4f, 0.4f),
                zoomScale = zoom.coerceIn(0.7f, 2.5f)
            )
        }
    }

    fun rotateSlice(index: Int) {
        val current = _uiState.value.perSliceConfigs[index] ?: SliceCustomConfig()
        val newAngle = (current.rotationAngle + 90f) % 360f
        recordHistory("第 $index 格旋转90°")
        updateSliceConfig(index) { it.copy(rotationAngle = newAngle) }
    }

    fun flipSliceH(index: Int) {
        val current = _uiState.value.perSliceConfigs[index] ?: SliceCustomConfig()
        recordHistory("第 $index 格水平翻转")
        updateSliceConfig(index) { it.copy(flipH = !it.flipH) }
    }

    fun setSliceFilter(index: Int, filter: ImageFilterPreset?) {
        recordHistory("第 $index 格单独滤镜")
        updateSliceConfig(index) { it.copy(filterOverride = filter) }
    }

    fun setSliceCustomText(index: Int, text: String?) {
        recordHistory("第 $index 格贴纸文字")
        updateSliceConfig(index) { it.copy(customText = text) }
    }

    fun setSliceBlankColor(index: Int, colorHex: Long) {
        recordHistory("第 $index 格留白背景色")
        updateSliceConfig(index) { it.copy(blankColorHex = colorHex) }
    }

    fun resetSliceConfig(index: Int) {
        recordHistory("重置第 $index 格微调")
        _uiState.update { state ->
            state.copy(perSliceConfigs = state.perSliceConfigs - index)
        }
    }

    fun resetAllSliceConfigs() {
        recordHistory("重置所有单格微调")
        _uiState.update { state ->
            state.copy(perSliceConfigs = emptyMap(), selectedPieceIndex = null, showSliceDetailSheet = false)
        }
    }

    // Presets & Templates Methods
    fun togglePresetSheet(show: Boolean) {
        _uiState.update { it.copy(showPresetSheet = show) }
    }

    fun applyStylePreset(preset: StylePresetItem) {
        recordHistory("套用预设: ${preset.name}")
        val bitmap = _uiState.value.sourceBitmap
        val newCrop = if (bitmap != null) {
            val targetRatio = preset.layoutType.defaultCols.toFloat() / preset.layoutType.defaultRows.toFloat()
            BitmapHelper.computeCenterCropRect(bitmap.width, bitmap.height, targetRatio)
        } else _uiState.value.cropRect

        _uiState.update { state ->
            val updatedPerSlice = if (preset.isCenterBlank && preset.layoutType == LayoutType.GRID_3x3) {
                state.perSliceConfigs + (5 to SliceCustomConfig(isBlank = true))
            } else state.perSliceConfigs

            state.copy(
                selectedLayout = preset.layoutType,
                selectedCategory = preset.layoutType.category,
                customRows = preset.customRows,
                customCols = preset.customCols,
                gapFraction = preset.gapFraction,
                cornerRadiusDp = preset.cornerRadiusDp,
                lineColorHex = preset.lineColorHex,
                lineWidthDp = preset.lineWidthDp,
                lineStyle = preset.lineStyle,
                shadowStyle = preset.shadowStyle,
                selectedFilter = preset.filter,
                magazineConfig = preset.magazineConfig,
                filmTimestampConfig = preset.filmTimestampConfig,
                perSliceConfigs = updatedPerSlice,
                cropRect = newCrop,
                showPresetSheet = false,
                notificationMessage = "已套用预设：${preset.name}"
            )
        }
    }

    fun saveCurrentAsPreset(name: String, desc: String) {
        val s = _uiState.value
        val isCenterBlank = s.perSliceConfigs[5]?.isBlank == true
        val item = StylePresetItem(
            name = name,
            description = desc.ifBlank { "${s.selectedLayout.title} · ${s.selectedFilter.label} · ${s.lineStyle.label}" },
            layoutType = s.selectedLayout,
            customRows = s.customRows,
            customCols = s.customCols,
            gapFraction = s.gapFraction,
            cornerRadiusDp = s.cornerRadiusDp,
            lineColorHex = s.lineColorHex,
            lineWidthDp = s.lineWidthDp,
            lineStyle = s.lineStyle,
            shadowStyle = s.shadowStyle,
            filter = s.selectedFilter,
            isCenterBlank = isCenterBlank,
            magazineConfig = s.magazineConfig,
            filmTimestampConfig = s.filmTimestampConfig
        )
        PresetManager.saveCustomPreset(getApplication(), item)
        loadPresets()
        _uiState.update { it.copy(notificationMessage = "已成功保存为新模板预设：$name") }
    }

    fun deleteCustomPreset(id: String) {
        PresetManager.deleteCustomPreset(getApplication(), id)
        loadPresets()
        _uiState.update { it.copy(notificationMessage = "已删除自定义预设") }
    }

    fun setLineStyle(style: LineStyle) {
        recordHistory("切换线条样式: ${style.label}")
        _uiState.update { it.copy(lineStyle = style) }
    }

    fun setShadowStyle(style: TileShadowStyle) {
        recordHistory("切换阴影样式: ${style.label}")
        _uiState.update { it.copy(shadowStyle = style) }
    }

    fun setMagazineConfig(config: MagazineTypographyConfig) {
        recordHistory("更新杂志排印")
        _uiState.update { it.copy(magazineConfig = config) }
    }

    fun setFilmTimestampConfig(config: FilmTimestampConfig) {
        recordHistory("更新胶片时间戳")
        _uiState.update { it.copy(filmTimestampConfig = config) }
    }

    // Free boxes controls
    fun addFreeBox() {
        recordHistory("添加局部选框")
        val count = _uiState.value.freeBoxes.size
        val offset = (count * 0.1f) % 0.4f
        val newBox = RectF(0.1f + offset, 0.1f + offset, 0.5f + offset, 0.5f + offset)
        _uiState.update { it.copy(freeBoxes = it.freeBoxes + newBox) }
    }

    fun updateFreeBox(index: Int, rect: RectF) {
        _uiState.update { state ->
            if (index in state.freeBoxes.indices) {
                val list = state.freeBoxes.toMutableList()
                list[index] = rect
                state.copy(freeBoxes = list)
            } else state
        }
    }

    fun removeFreeBox(index: Int) {
        recordHistory("删除局部选框")
        _uiState.update { state ->
            if (index in state.freeBoxes.indices && state.freeBoxes.size > 1) {
                val list = state.freeBoxes.toMutableList()
                list.removeAt(index)
                state.copy(freeBoxes = list)
            } else state
        }
    }

    // -------------------------------------------------------------
    // Batch Processing Implementation
    // -------------------------------------------------------------

    fun loadMultipleFromUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, notificationMessage = "正在批量加载 ${uris.size} 张图片...") }
            val loadedItems = withContext(Dispatchers.IO) {
                uris.mapIndexedNotNull { index, uri ->
                    val bitmap = BitmapHelper.decodeSampledBitmapFromUri(getApplication(), uri, maxDimension = 1200)
                    if (bitmap != null) {
                        BatchImageItem(
                            id = "batch_${System.currentTimeMillis()}_$index",
                            uri = uri,
                            bitmap = bitmap,
                            title = "照片 ${index + 1}"
                        )
                    } else null
                }
            }

            if (loadedItems.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        batchImages = loadedItems,
                        isProcessing = false,
                        currentScreen = Screen.BatchEditor,
                        notificationMessage = "已导入 ${loadedItems.size} 张图片进行批量处理"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        notificationMessage = "批量选图加载失败，请重试"
                    )
                }
            }
        }
    }

    fun addMoreToBatch(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val startIdx = _uiState.value.batchImages.size
            val newItems = withContext(Dispatchers.IO) {
                uris.mapIndexedNotNull { index, uri ->
                    val bitmap = BitmapHelper.decodeSampledBitmapFromUri(getApplication(), uri, maxDimension = 1200)
                    if (bitmap != null) {
                        BatchImageItem(
                            id = "batch_${System.currentTimeMillis()}_${startIdx + index}",
                            uri = uri,
                            bitmap = bitmap,
                            title = "照片 ${startIdx + index + 1}"
                        )
                    } else null
                }
            }
            _uiState.update {
                it.copy(
                    batchImages = it.batchImages + newItems,
                    notificationMessage = "已追加 ${newItems.size} 张图片"
                )
            }
        }
    }

    fun removeBatchImage(id: String) {
        _uiState.update { state ->
            val filtered = state.batchImages.filterNot { it.id == id }
            state.copy(batchImages = filtered)
        }
    }

    fun loadPresetBatch() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            val items = withContext(Dispatchers.Default) {
                SampleImageGenerator.PRESETS.mapIndexed { idx, preset ->
                    val bmp = SampleImageGenerator.generatePresetBitmap(preset.id, 800, 800)
                    BatchImageItem(
                        id = "preset_batch_$idx",
                        bitmap = bmp,
                        title = preset.name
                    )
                }
            }
            _uiState.update {
                it.copy(
                    batchImages = items,
                    isProcessing = false,
                    currentScreen = Screen.BatchEditor,
                    notificationMessage = "已载入 ${items.size} 张预设美图进行批量切图"
                )
            }
        }
    }

    fun setBatchLayout(layout: LayoutType) {
        _uiState.update {
            it.copy(
                batchLayout = layout,
                batchCustomRows = if (layout.defaultRows > 0) layout.defaultRows else it.batchCustomRows,
                batchCustomCols = if (layout.defaultCols > 0) layout.defaultCols else it.batchCustomCols
            )
        }
    }

    fun setBatchAspectRatio(ratio: AspectRatioOption) {
        _uiState.update { it.copy(batchAspectRatio = ratio) }
    }

    fun setBatchGap(gap: Float) {
        _uiState.update { it.copy(batchGapFraction = gap.coerceIn(0f, 0.15f)) }
    }

    fun setBatchCornerRadius(radius: Float) {
        _uiState.update { it.copy(batchCornerRadiusDp = radius.coerceIn(0f, 32f)) }
    }

    fun setBatchFilter(filter: ImageFilterPreset) {
        _uiState.update { it.copy(batchFilter = filter) }
    }

    fun setBatchCustomDimensions(rows: Int, cols: Int) {
        _uiState.update {
            it.copy(
                batchCustomRows = rows.coerceIn(1, 10),
                batchCustomCols = cols.coerceIn(1, 10)
            )
        }
    }

    fun performBatchSlice() {
        val images = _uiState.value.batchImages
        if (images.isEmpty()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    batchProcessingProgress = 0f,
                    batchCurrentProcessingIndex = 0,
                    notificationMessage = "批量切图开始，共 ${images.size} 张图片..."
                )
            }

            val processedItems = mutableListOf<BatchImageItem>()
            val total = images.size

            withContext(Dispatchers.Default) {
                images.forEachIndexed { index, item ->
                    _uiState.update {
                        it.copy(
                            batchCurrentProcessingIndex = index + 1,
                            batchProcessingProgress = index.toFloat() / total.toFloat(),
                            notificationMessage = "正在切片 第 ${index + 1}/$total 张 [${item.title}]..."
                        )
                    }

                    val rawBitmap = item.bitmap
                    if (rawBitmap != null) {
                        // Apply unified filter if selected
                        val filteredBitmap = if (_uiState.value.batchFilter != ImageFilterPreset.ORIGINAL) {
                            ImageEditorHelper.renderEditedBitmap(
                                source = rawBitmap,
                                rotationDegrees = 0f,
                                flipH = false,
                                flipV = false,
                                brightness = 0f,
                                contrast = 1f,
                                saturation = 1f,
                                filter = _uiState.value.batchFilter
                            )
                        } else rawBitmap

                        // Calculate crop rect based on batch aspect ratio
                        val cropRect = BitmapHelper.computeCenterCropRect(
                            filteredBitmap.width,
                            filteredBitmap.height,
                            _uiState.value.batchAspectRatio.ratio
                        )

                        val pieces = BitmapHelper.sliceBitmap(
                            source = filteredBitmap,
                            cropRect = cropRect,
                            layoutType = _uiState.value.batchLayout,
                            customRows = _uiState.value.batchCustomRows,
                            customCols = _uiState.value.batchCustomCols,
                            gapFraction = _uiState.value.batchGapFraction,
                            cornerRadiusDp = _uiState.value.batchCornerRadiusDp
                        )

                        processedItems.add(
                            item.copy(
                                status = BatchItemStatus.COMPLETED,
                                slicePieces = pieces
                            )
                        )
                    } else {
                        processedItems.add(item.copy(status = BatchItemStatus.FAILED))
                    }
                }
            }

            val totalPieces = processedItems.sumOf { it.slicePieces.size }
            val batchResult = BatchProcessResult(
                totalImages = processedItems.size,
                totalPieces = totalPieces,
                items = processedItems
            )

            _uiState.update {
                it.copy(
                    isProcessing = false,
                    batchProcessingProgress = 1f,
                    batchImages = processedItems,
                    batchProcessResult = batchResult,
                    currentScreen = Screen.BatchResult,
                    notificationMessage = "批量切图完成！共生成 $totalPieces 张切片"
                )
            }
        }
    }

    fun saveAllBatchPieces() {
        val result = _uiState.value.batchProcessResult ?: return
        val allPieces = result.items.flatMap { it.slicePieces }
        if (allPieces.isEmpty()) return

        val config = _uiState.value.exportConfig
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, notificationMessage = "正在批量保存全部 ${allPieces.size} 张切片...") }
            val res = ImageSaver.savePiecesToGallery(getApplication(), allPieces, config, prefix = "batch")
            when (res) {
                is ImageSaver.SaveResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            notificationMessage = "已全部成功保存 ${res.savedCount} 张切图至相册 [${res.albumName}]"
                        )
                    }
                }
                is ImageSaver.SaveResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            notificationMessage = res.message
                        )
                    }
                }
            }
        }
    }

    fun shareAllBatchPieces() {
        val result = _uiState.value.batchProcessResult ?: return
        val allPieces = result.items.flatMap { it.slicePieces }
        if (allPieces.isEmpty()) return

        val config = _uiState.value.exportConfig
        viewModelScope.launch {
            ImageSaver.shareAllPieces(getApplication(), allPieces, config)
        }
    }

    // -------------------------------------------------------------
    // Single Slice Execution
    // -------------------------------------------------------------

    fun performSlice() {
        val rawBitmap = _uiState.value.sourceBitmap ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, notificationMessage = "正在渲染与精致切片中...") }

            val result = withContext(Dispatchers.Default) {
                val processedBitmap = ImageEditorHelper.renderEditedBitmap(
                    source = rawBitmap,
                    rotationDegrees = _uiState.value.rotationAngle,
                    flipH = _uiState.value.flipHorizontal,
                    flipV = _uiState.value.flipVertical,
                    brightness = _uiState.value.brightness,
                    contrast = _uiState.value.contrast,
                    saturation = _uiState.value.saturation,
                    filter = _uiState.value.selectedFilter
                )

                val pieces = BitmapHelper.sliceBitmap(
                    source = processedBitmap,
                    cropRect = _uiState.value.cropRect,
                    layoutType = _uiState.value.selectedLayout,
                    customRows = _uiState.value.customRows,
                    customCols = _uiState.value.customCols,
                    splitLines = _uiState.value.splitLines,
                    freeBoxes = _uiState.value.freeBoxes,
                    gapFraction = _uiState.value.gapFraction,
                    cornerRadiusDp = _uiState.value.cornerRadiusDp,
                    perSliceConfigs = _uiState.value.perSliceConfigs
                )

                val compositeBmp = BitmapHelper.renderCompositeGridBitmap(
                    source = processedBitmap,
                    cropRect = _uiState.value.cropRect,
                    layoutType = _uiState.value.selectedLayout,
                    customRows = _uiState.value.customRows,
                    customCols = _uiState.value.customCols,
                    splitLines = _uiState.value.splitLines,
                    freeBoxes = _uiState.value.freeBoxes,
                    lineColorHex = _uiState.value.lineColorHex,
                    lineWidthDp = _uiState.value.lineWidthDp,
                    showIndices = false,
                    outerCornerRadiusDp = _uiState.value.outerCornerRadiusDp,
                    outerPaddingDp = _uiState.value.outerPaddingDp,
                    outerBgColorHex = _uiState.value.outerBgColorHex,
                    outerCardShadow = _uiState.value.outerCardShadow
                )

                Pair(
                    SliceResult(
                        layoutType = _uiState.value.selectedLayout,
                        originalWidth = processedBitmap.width,
                        originalHeight = processedBitmap.height,
                        pieces = pieces
                    ),
                    compositeBmp
                )
            }

            _uiState.update {
                it.copy(
                    isProcessing = false,
                    sliceResult = result.first,
                    compositeEffectBitmap = result.second,
                    recentResults = listOf(result.first) + it.recentResults.take(4),
                    currentScreen = Screen.ResultPreview,
                    notificationMessage = null
                )
            }
        }
    }

    fun regenerateCompositeEffect(
        colorHex: Long = _uiState.value.lineColorHex,
        widthDp: Float = _uiState.value.lineWidthDp,
        outerCornerRadius: Float = _uiState.value.outerCornerRadiusDp,
        outerPadding: Float = _uiState.value.outerPaddingDp,
        outerBgColor: Long = _uiState.value.outerBgColorHex,
        outerShadow: Boolean = _uiState.value.outerCardShadow
    ) {
        val rawBitmap = _uiState.value.sourceBitmap ?: return
        viewModelScope.launch {
            val compBmp = withContext(Dispatchers.Default) {
                val processedBitmap = ImageEditorHelper.renderEditedBitmap(
                    source = rawBitmap,
                    rotationDegrees = _uiState.value.rotationAngle,
                    flipH = _uiState.value.flipHorizontal,
                    flipV = _uiState.value.flipVertical,
                    brightness = _uiState.value.brightness,
                    contrast = _uiState.value.contrast,
                    saturation = _uiState.value.saturation,
                    filter = _uiState.value.selectedFilter
                )
                BitmapHelper.renderCompositeGridBitmap(
                    source = processedBitmap,
                    cropRect = _uiState.value.cropRect,
                    layoutType = _uiState.value.selectedLayout,
                    customRows = _uiState.value.customRows,
                    customCols = _uiState.value.customCols,
                    splitLines = _uiState.value.splitLines,
                    freeBoxes = _uiState.value.freeBoxes,
                    lineColorHex = colorHex,
                    lineWidthDp = widthDp,
                    showIndices = false,
                    outerCornerRadiusDp = outerCornerRadius,
                    outerPaddingDp = outerPadding,
                    outerBgColorHex = outerBgColor,
                    outerCardShadow = outerShadow
                )
            }
            _uiState.update {
                it.copy(
                    lineColorHex = colorHex,
                    lineWidthDp = widthDp,
                    outerCornerRadiusDp = outerCornerRadius,
                    outerPaddingDp = outerPadding,
                    outerBgColorHex = outerBgColor,
                    outerCardShadow = outerShadow,
                    compositeEffectBitmap = compBmp
                )
            }
        }
    }

    fun setOuterCornerRadiusDp(radius: Float) {
        _uiState.update { it.copy(outerCornerRadiusDp = radius.coerceIn(0f, 64f)) }
        regenerateCompositeEffect(outerCornerRadius = radius.coerceIn(0f, 64f))
    }

    fun setOuterPaddingDp(padding: Float) {
        _uiState.update { it.copy(outerPaddingDp = padding.coerceIn(0f, 48f)) }
        regenerateCompositeEffect(outerPadding = padding.coerceIn(0f, 48f))
    }

    fun setOuterBgColorHex(colorHex: Long) {
        _uiState.update { it.copy(outerBgColorHex = colorHex) }
        regenerateCompositeEffect(outerBgColor = colorHex)
    }

    fun toggleOuterCardShadow(enabled: Boolean) {
        _uiState.update { it.copy(outerCardShadow = enabled) }
        regenerateCompositeEffect(outerShadow = enabled)
    }

    fun saveCompositeEffectImage() {
        val composite = _uiState.value.compositeEffectBitmap ?: return
        val config = _uiState.value.exportConfig
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, notificationMessage = "正在保存单张切图效果大图...") }
            val res = ImageSaver.saveCompositeEffectBitmap(getApplication(), composite, config)
            when (res) {
                is ImageSaver.SaveResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            notificationMessage = "🎉 单张切图效果大图已成功保存至相册 [${res.albumName}]！"
                        )
                    }
                }
                is ImageSaver.SaveResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            notificationMessage = res.message
                        )
                    }
                }
            }
        }
    }

    fun saveBothPiecesAndComposite() {
        val pieces = _uiState.value.sliceResult?.pieces ?: return
        val composite = _uiState.value.compositeEffectBitmap ?: return
        val config = _uiState.value.exportConfig
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, notificationMessage = "正在保存全部切片与效果大图...") }
            val res = ImageSaver.saveBothPiecesAndComposite(getApplication(), pieces, composite, config)
            when (res) {
                is ImageSaver.SaveResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            notificationMessage = "🎉 已全部成功保存 ${res.savedCount} 张图片（切片与效果大图）至相册！"
                        )
                    }
                }
                is ImageSaver.SaveResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            notificationMessage = res.message
                        )
                    }
                }
            }
        }
    }

    fun shareCompositeEffectImage() {
        val composite = _uiState.value.compositeEffectBitmap ?: return
        val config = _uiState.value.exportConfig
        viewModelScope.launch {
            ImageSaver.shareCompositeEffectBitmap(getApplication(), composite, config)
        }
    }

    fun quickExportCompositeEffectFromEditor() {
        val rawBitmap = _uiState.value.sourceBitmap ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, notificationMessage = "正在导出单张切图效果大图...") }
            val compBmp = withContext(Dispatchers.Default) {
                val processedBitmap = ImageEditorHelper.renderEditedBitmap(
                    source = rawBitmap,
                    rotationDegrees = _uiState.value.rotationAngle,
                    flipH = _uiState.value.flipHorizontal,
                    flipV = _uiState.value.flipVertical,
                    brightness = _uiState.value.brightness,
                    contrast = _uiState.value.contrast,
                    saturation = _uiState.value.saturation,
                    filter = _uiState.value.selectedFilter
                )
                BitmapHelper.renderCompositeGridBitmap(
                    source = processedBitmap,
                    cropRect = _uiState.value.cropRect,
                    layoutType = _uiState.value.selectedLayout,
                    customRows = _uiState.value.customRows,
                    customCols = _uiState.value.customCols,
                    splitLines = _uiState.value.splitLines,
                    freeBoxes = _uiState.value.freeBoxes,
                    lineColorHex = _uiState.value.lineColorHex,
                    lineWidthDp = _uiState.value.lineWidthDp,
                    showIndices = false
                )
            }
            val res = ImageSaver.saveCompositeEffectBitmap(getApplication(), compBmp, _uiState.value.exportConfig)
            when (res) {
                is ImageSaver.SaveResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            compositeEffectBitmap = compBmp,
                            notificationMessage = "🎉 已成功导出单张切图效果大图至手机相册！"
                        )
                    }
                }
                is ImageSaver.SaveResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            notificationMessage = res.message
                        )
                    }
                }
            }
        }
    }

    fun inspectPiece(piece: SlicePiece?) {
        _uiState.update { it.copy(inspectingPiece = piece) }
    }

    fun togglePostingGuide(show: Boolean) {
        _uiState.update { it.copy(showPostingGuide = show) }
    }

    fun clearNotification() {
        _uiState.update { it.copy(notificationMessage = null) }
    }

    fun saveAllPieces() {
        val pieces = _uiState.value.sliceResult?.pieces ?: return
        val config = _uiState.value.exportConfig
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, notificationMessage = "正在以 ${config.format.name} 格式保存至相册...") }
            val res = ImageSaver.savePiecesToGallery(getApplication(), pieces, config)
            when (res) {
                is ImageSaver.SaveResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            notificationMessage = "已成功保存 ${res.savedCount} 张 [${res.formatDesc}] 切图至相册"
                        )
                    }
                }
                is ImageSaver.SaveResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            notificationMessage = res.message
                        )
                    }
                }
            }
        }
    }

    fun saveSinglePiece(piece: SlicePiece) {
        val config = _uiState.value.exportConfig
        viewModelScope.launch {
            _uiState.update { it.copy(notificationMessage = "正在保存第 ${piece.index} 格 (${config.format.name})...") }
            val ok = ImageSaver.saveSinglePiece(getApplication(), piece, config)
            _uiState.update {
                it.copy(
                    notificationMessage = if (ok) "第 ${piece.index} 格已保存至相册" else "保存失败"
                )
            }
        }
    }

    fun shareAllPieces() {
        val pieces = _uiState.value.sliceResult?.pieces ?: return
        val config = _uiState.value.exportConfig
        viewModelScope.launch {
            ImageSaver.shareAllPieces(getApplication(), pieces, config)
        }
    }

    fun shareSinglePiece(piece: SlicePiece) {
        val config = _uiState.value.exportConfig
        viewModelScope.launch {
            ImageSaver.shareSinglePiece(getApplication(), piece, config)
        }
    }
}
