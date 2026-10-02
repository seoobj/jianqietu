package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import androidx.test.core.app.ApplicationProvider
import com.example.model.AspectRatioOption
import com.example.model.BatchImageItem
import com.example.model.BatchItemStatus
import com.example.model.BatchProcessResult
import com.example.model.ExportConfig
import com.example.model.ExportFormat
import com.example.model.ImageFilterPreset
import com.example.model.ImageContentCategory
import com.example.model.LayoutType
import com.example.model.SocialPlatform
import com.example.model.SocialRatioPreset
import com.example.model.WatermarkConfig
import com.example.model.WatermarkPosition
import com.example.model.WatermarkScope
import com.example.model.WatermarkType
import com.example.util.BitmapHelper
import com.example.util.ImageEditorHelper
import com.example.util.SampleImageGenerator
import com.example.util.SmartAnalyzer
import com.example.util.SocialPlatformDetector
import com.example.util.WatermarkHelper
import com.example.viewmodel.SliceViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource matches 简切图`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("简切图", appName)
    }

    @Test
    fun `sample image generator generates valid bitmap`() {
        val bitmap = SampleImageGenerator.generatePresetBitmap("sunset", 300, 300)
        assertNotNull(bitmap)
        assertEquals(300, bitmap.width)
        assertEquals(300, bitmap.height)
    }

    @Test
    fun `3x3 grid slicing produces exactly 9 pieces`() {
        val source = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
        val pieces = BitmapHelper.sliceBitmap(
            source = source,
            cropRect = RectF(0f, 0f, 1f, 1f),
            layoutType = LayoutType.GRID_3x3,
            customRows = 3,
            customCols = 3
        )
        assertEquals(9, pieces.size)
        assertEquals(1, pieces.first().index)
        assertEquals(9, pieces.last().index)
        pieces.forEach { piece ->
            assertNotNull(piece.bitmap)
        }
    }

    @Test
    fun `center crop calculation preserves aspect ratio correctly`() {
        val cropRect = BitmapHelper.computeCenterCropRect(1000, 500, 1.0f)
        assertEquals(0.25f, cropRect.left, 0.01f)
        assertEquals(0.75f, cropRect.right, 0.01f)
        assertEquals(0f, cropRect.top, 0.01f)
        assertEquals(1f, cropRect.bottom, 0.01f)
    }

    @Test
    fun `image editing renders rotation and color adjustment correctly`() {
        val src = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888)
        val edited = ImageEditorHelper.renderEditedBitmap(
            source = src,
            rotationDegrees = 90f,
            flipH = false,
            flipV = false,
            brightness = 0.1f,
            contrast = 1.1f,
            saturation = 1.2f,
            filter = ImageFilterPreset.WARM
        )
        assertNotNull(edited)
        assertEquals(200, edited.width)
        assertEquals(100, edited.height)
    }

    @Test
    fun `smart analyzer detects panoramic landscape and recommends 1x3`() {
        val wideBitmap = Bitmap.createBitmap(1600, 900, Bitmap.Config.ARGB_8888)
        val analysis = SmartAnalyzer.analyzeImage(wideBitmap)
        assertEquals(ImageContentCategory.PANORAMIC_LANDSCAPE, analysis.category)
        assertTrue(analysis.recommendations.isNotEmpty())
        assertEquals(LayoutType.GRID_1x3, analysis.recommendations.first().layoutType)
    }

    @Test
    fun `smart analyzer detects tall portrait and recommends 3x1`() {
        val tallBitmap = Bitmap.createBitmap(900, 1600, Bitmap.Config.ARGB_8888)
        val analysis = SmartAnalyzer.analyzeImage(tallBitmap)
        assertEquals(ImageContentCategory.TALL_PORTRAIT, analysis.category)
        assertTrue(analysis.recommendations.isNotEmpty())
        assertEquals(LayoutType.GRID_3x1, analysis.recommendations.first().layoutType)
    }

    @Test
    fun `export configuration handles formats and scale factors`() {
        val config = ExportConfig(
            format = ExportFormat.JPG,
            scaleFactor = 1.5f,
            quality = 95
        )
        assertEquals(".jpg", config.format.extension)
        assertEquals("image/jpeg", config.format.mimeType)
        assertEquals(1.5f, config.scaleFactor, 0.001f)
        assertEquals(95, config.quality)
    }

    @Test
    fun `batch processing cuts multiple images with unified layout`() {
        val bmp1 = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
        val bmp2 = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)

        val batchItems = listOf(
            BatchImageItem(id = "1", bitmap = bmp1, title = "图1"),
            BatchImageItem(id = "2", bitmap = bmp2, title = "图2")
        )

        val unifiedLayout = LayoutType.GRID_2x2
        val processed = batchItems.map { item ->
            val pieces = BitmapHelper.sliceBitmap(
                source = item.bitmap!!,
                cropRect = RectF(0f, 0f, 1f, 1f),
                layoutType = unifiedLayout,
                customRows = 2,
                customCols = 2
            )
            item.copy(status = BatchItemStatus.COMPLETED, slicePieces = pieces)
        }

        val totalPieces = processed.sumOf { it.slicePieces.size }
        assertEquals(2, processed.size)
        assertEquals(8, totalPieces)
        assertEquals(4, processed[0].slicePieces.size)
        assertEquals(4, processed[1].slicePieces.size)
    }

    @Test
    fun `social platform detector identifies Xiaohongshu 3to4 ratio`() {
        val detection = SocialPlatformDetector.detectBestPlatformRatio(900, 1200)
        assertEquals(SocialPlatform.XIAOHONGSHU, detection.bestMatchPreset.platform)
        assertEquals("3:4", detection.bestMatchPreset.ratioLabel)
        assertEquals(0, detection.cropLossPercent)
        assertTrue(detection.matchScore >= 95)
    }

    @Test
    fun `social platform detector identifies WeChat Moments 1to1 square`() {
        val detection = SocialPlatformDetector.detectBestPlatformRatio(1080, 1080)
        assertEquals("1:1", detection.bestMatchPreset.ratioLabel)
        assertEquals(0, detection.cropLossPercent)
    }

    @Test
    fun `social platform detector identifies Instagram 4to5 portrait`() {
        val detection = SocialPlatformDetector.detectBestPlatformRatio(1080, 1350)
        assertEquals(SocialPlatform.INSTAGRAM, detection.bestMatchPreset.platform)
        assertEquals("4:5", detection.bestMatchPreset.ratioLabel)
        assertEquals(0, detection.cropLossPercent)
    }

    @Test
    fun `watermark helper renders text watermark correctly`() {
        val src = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
        val config = WatermarkConfig(
            enabled = true,
            type = WatermarkType.TEXT,
            text = "© 测试水印",
            position = WatermarkPosition.BOTTOM_RIGHT,
            scope = WatermarkScope.ALL_PIECES,
            opacity = 0.8f
        )
        val watermarked = WatermarkHelper.applyWatermark(src, config, 1, 9)
        assertNotNull(watermarked)
        assertEquals(300, watermarked.width)
        assertEquals(300, watermarked.height)
    }

    @Test
    fun `watermark scope last piece only applies only to final slice`() {
        val src = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val config = WatermarkConfig(
            enabled = true,
            type = WatermarkType.TEXT,
            text = "© 简切图",
            scope = WatermarkScope.LAST_PIECE_ONLY
        )
        // Piece 1 of 9 should NOT get watermark (returns original bitmap reference)
        val piece1 = WatermarkHelper.applyWatermark(src, config, 1, 9)
        assertEquals(src, piece1)

        // Piece 9 of 9 SHOULD get watermark
        val piece9 = WatermarkHelper.applyWatermark(src, config, 9, 9)
        assertNotNull(piece9)
    }

    @Test
    fun `editor undo and redo restores rotation correctly`() {
        val vm = SliceViewModel(ApplicationProvider.getApplicationContext())
        assertEquals(0f, vm.uiState.value.rotationAngle, 0.001f)
        assertEquals(false, vm.uiState.value.canUndo)

        // Perform rotation action
        vm.rotateClockwise()
        assertEquals(90f, vm.uiState.value.rotationAngle, 0.001f)
        assertEquals(true, vm.uiState.value.canUndo)
        assertEquals(false, vm.uiState.value.canRedo)

        // Undo rotation
        vm.undo()
        assertEquals(0f, vm.uiState.value.rotationAngle, 0.001f)
        assertEquals(false, vm.uiState.value.canUndo)
        assertEquals(true, vm.uiState.value.canRedo)

        // Redo rotation
        vm.redo()
        assertEquals(90f, vm.uiState.value.rotationAngle, 0.001f)
        assertEquals(true, vm.uiState.value.canUndo)
        assertEquals(false, vm.uiState.value.canRedo)
    }

    @Test
    fun `editor undo and redo restores filter and layout selections`() {
        val vm = SliceViewModel(ApplicationProvider.getApplicationContext())
        assertEquals(ImageFilterPreset.ORIGINAL, vm.uiState.value.selectedFilter)

        // Change filter
        vm.setFilter(ImageFilterPreset.FILM)
        assertEquals(ImageFilterPreset.FILM, vm.uiState.value.selectedFilter)

        // Change layout
        vm.setLayout(LayoutType.GRID_1x3)
        assertEquals(LayoutType.GRID_1x3, vm.uiState.value.selectedLayout)

        // Undo layout change
        vm.undo()
        assertEquals(LayoutType.GRID_3x3, vm.uiState.value.selectedLayout)
        assertEquals(ImageFilterPreset.FILM, vm.uiState.value.selectedFilter)

        // Undo filter change
        vm.undo()
        assertEquals(ImageFilterPreset.ORIGINAL, vm.uiState.value.selectedFilter)

        // Redo filter change
        vm.redo()
        assertEquals(ImageFilterPreset.FILM, vm.uiState.value.selectedFilter)

        // Redo layout change
        vm.redo()
        assertEquals(LayoutType.GRID_1x3, vm.uiState.value.selectedLayout)
    }

    @Test
    fun `editor undo and redo restores aspect ratio and crop rect`() {
        val vm = SliceViewModel(ApplicationProvider.getApplicationContext())
        assertEquals(AspectRatioOption.SQUARE_1_1, vm.uiState.value.aspectRatio)

        vm.setAspectRatio(AspectRatioOption.RATIO_16_9)
        assertEquals(AspectRatioOption.RATIO_16_9, vm.uiState.value.aspectRatio)

        vm.undo()
        assertEquals(AspectRatioOption.SQUARE_1_1, vm.uiState.value.aspectRatio)

        vm.redo()
        assertEquals(AspectRatioOption.RATIO_16_9, vm.uiState.value.aspectRatio)
    }

    @Test
    fun `editor line width and line color can be configured and restored via undo and redo`() {
        val vm = SliceViewModel(ApplicationProvider.getApplicationContext())
        assertEquals(2.0f, vm.uiState.value.lineWidthDp, 0.01f)
        val originalColor = vm.uiState.value.lineColorHex

        // Change line width
        vm.startSliderEdit("调整线条宽度")
        vm.setLineWidthDp(4.5f)
        assertEquals(4.5f, vm.uiState.value.lineWidthDp, 0.01f)

        // Change line color
        vm.setLineColor(0xFF22C55EL)
        assertEquals(0xFF22C55EL, vm.uiState.value.lineColorHex)

        // Undo color change
        vm.undo()
        assertEquals(originalColor, vm.uiState.value.lineColorHex)
        assertEquals(4.5f, vm.uiState.value.lineWidthDp, 0.01f)

        // Undo line width change
        vm.undo()
        assertEquals(2.0f, vm.uiState.value.lineWidthDp, 0.01f)

        // Redo line width
        vm.redo()
        assertEquals(4.5f, vm.uiState.value.lineWidthDp, 0.01f)

        // Redo line color
        vm.redo()
        assertEquals(0xFF22C55EL, vm.uiState.value.lineColorHex)
    }

    @Test
    fun `eyedropper tool accurately samples color from bitmap`() {
        val vm = SliceViewModel(ApplicationProvider.getApplicationContext())
        // Create known bitmap with distinctive color in center
        val testBmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        testBmp.eraseColor(android.graphics.Color.RED)

        val extractedColor = ImageEditorHelper.extractColorFromBitmap(
            source = testBmp,
            fracX = 0.5f,
            fracY = 0.5f
        )
        // Red color in ARGB (0xFFFF0000)
        assertEquals(0xFFFF0000L, extractedColor and 0xFFFFFFFFL)

        // Test eyedropper state toggling
        vm.toggleEyedropper(true)
        assertTrue(vm.uiState.value.isEyedropperActive)

        vm.toggleEyedropper(false)
        assertFalse(vm.uiState.value.isEyedropperActive)
    }
}
