package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.model.ExportConfig
import com.example.model.ExportFormat
import com.example.model.LayoutType
import com.example.model.SlicePiece
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.roundToInt
import kotlin.math.min

object ImageSaver {

    sealed class SaveResult {
        data class Success(val savedCount: Int, val albumName: String, val formatDesc: String) : SaveResult()
        data class Error(val message: String) : SaveResult()
    }

    /**
     * Scale and apply watermark to bitmap according to ExportConfig
     */
    private fun prepareExportBitmap(
        bitmap: Bitmap,
        exportConfig: ExportConfig,
        pieceIndex: Int,
        totalPieces: Int
    ): Bitmap {
        val scaled = if (kotlin.math.abs(exportConfig.scaleFactor - 1.0f) >= 0.01f) {
            val targetW = (bitmap.width * exportConfig.scaleFactor).roundToInt().coerceAtLeast(1)
            val targetH = (bitmap.height * exportConfig.scaleFactor).roundToInt().coerceAtLeast(1)
            try {
                Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            } catch (e: Exception) {
                bitmap
            }
        } else bitmap

        return WatermarkHelper.applyWatermark(scaled, exportConfig.watermark, pieceIndex, totalPieces)
    }

    /**
     * Save all sliced pieces into the system gallery under "Pictures/简切图"
     * respecting ExportConfig (PNG/JPG, Resolution Scale, Quality, Watermark)
     */
    suspend fun savePiecesToGallery(
        context: Context,
        pieces: List<SlicePiece>,
        exportConfig: ExportConfig = ExportConfig(),
        prefix: String = "slice"
    ): SaveResult = withContext(Dispatchers.IO) {
        val validPieces = pieces.filter { it.bitmap != null }
        if (validPieces.isEmpty()) {
            return@withContext SaveResult.Error("没有可保存的切片图片")
        }

        var savedCount = 0
        val timestamp = System.currentTimeMillis()
        val albumName = "简切图"
        val isPng = exportConfig.format == ExportFormat.PNG
        val compressFormat = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val quality = if (isPng) 100 else exportConfig.quality.coerceIn(50, 100)
        val ext = exportConfig.format.extension
        val mimeType = exportConfig.format.mimeType

        try {
            validPieces.forEachIndexed { _, piece ->
                val rawBitmap = piece.bitmap ?: return@forEachIndexed
                val exportBitmap = prepareExportBitmap(rawBitmap, exportConfig, piece.index, validPieces.size)
                val orderStr = String.format(java.util.Locale.US, "%02d", piece.index)
                val filename = "${prefix}_${timestamp}_${orderStr}$ext"

                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$albumName")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

                val uri: Uri? = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                if (uri != null) {
                    var outputStream: OutputStream? = null
                    try {
                        outputStream = context.contentResolver.openOutputStream(uri)
                        if (outputStream != null) {
                            exportBitmap.compress(compressFormat, quality, outputStream)
                            savedCount++
                        }
                    } finally {
                        outputStream?.close()
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                        context.contentResolver.update(uri, contentValues, null, null)
                    }
                }

                if (exportBitmap != rawBitmap) {
                    exportBitmap.recycle()
                }
            }

            if (savedCount > 0) {
                val formatDesc = "${exportConfig.format.name} ${(exportConfig.scaleFactor * 100).toInt()}%"
                SaveResult.Success(savedCount, albumName, formatDesc)
            } else {
                SaveResult.Error("保存失败，未能写入相册")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            SaveResult.Error("保存出错: ${e.localizedMessage}")
        }
    }

    /**
     * Save a single piece to gallery with export config
     */
    suspend fun saveSinglePiece(
        context: Context,
        piece: SlicePiece,
        exportConfig: ExportConfig = ExportConfig()
    ): Boolean = withContext(Dispatchers.IO) {
        val rawBitmap = piece.bitmap ?: return@withContext false
        val exportBitmap = prepareExportBitmap(rawBitmap, exportConfig, piece.index, 1)
        val isPng = exportConfig.format == ExportFormat.PNG
        val compressFormat = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val quality = if (isPng) 100 else exportConfig.quality.coerceIn(50, 100)
        val ext = exportConfig.format.extension
        val mimeType = exportConfig.format.mimeType

        try {
            val filename = "slice_${System.currentTimeMillis()}_${piece.index}$ext"
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/简切图")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext false

            context.contentResolver.openOutputStream(uri)?.use { os ->
                exportBitmap.compress(compressFormat, quality, os)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }

            if (exportBitmap != rawBitmap) {
                exportBitmap.recycle()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Share multiple sliced pieces with export config
     */
    suspend fun shareAllPieces(
        context: Context,
        pieces: List<SlicePiece>,
        exportConfig: ExportConfig = ExportConfig()
    ) = withContext(Dispatchers.IO) {
        val validPieces = pieces.filter { it.bitmap != null }
        if (validPieces.isEmpty()) return@withContext

        val cacheDir = File(context.cacheDir, "slices").apply { mkdirs() }
        val uris = ArrayList<Uri>()
        val isPng = exportConfig.format == ExportFormat.PNG
        val compressFormat = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val quality = if (isPng) 100 else exportConfig.quality.coerceIn(50, 100)
        val ext = exportConfig.format.extension
        val mimeType = exportConfig.format.mimeType

        validPieces.forEach { piece ->
            val rawBitmap = piece.bitmap ?: return@forEach
            val exportBitmap = prepareExportBitmap(rawBitmap, exportConfig, piece.index, validPieces.size)
            val file = File(cacheDir, "share_slice_${piece.index}$ext")
            FileOutputStream(file).use { out ->
                exportBitmap.compress(compressFormat, quality, out)
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            uris.add(uri)
            if (exportBitmap != rawBitmap) {
                exportBitmap.recycle()
            }
        }

        if (uris.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = mimeType
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "分享切图 (${exportConfig.format.name}，共 ${uris.size} 张)").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }

    /**
     * Share a single slice piece
     */
    suspend fun shareSinglePiece(
        context: Context,
        piece: SlicePiece,
        exportConfig: ExportConfig = ExportConfig()
    ) = withContext(Dispatchers.IO) {
        val rawBitmap = piece.bitmap ?: return@withContext
        val exportBitmap = prepareExportBitmap(rawBitmap, exportConfig, piece.index, 1)
        val cacheDir = File(context.cacheDir, "slices").apply { mkdirs() }
        val isPng = exportConfig.format == ExportFormat.PNG
        val compressFormat = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val quality = if (isPng) 100 else exportConfig.quality.coerceIn(50, 100)
        val ext = exportConfig.format.extension
        val mimeType = exportConfig.format.mimeType

        val file = File(cacheDir, "share_single_${piece.index}$ext")
        FileOutputStream(file).use { out ->
            exportBitmap.compress(compressFormat, quality, out)
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "分享第 ${piece.index} 格切片").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)

        if (exportBitmap != rawBitmap) {
            exportBitmap.recycle()
        }
    }

    /**
     * Save the single composite effect image with slice lines directly to gallery
     */
    suspend fun saveCompositeEffectBitmap(
        context: Context,
        compositeBitmap: Bitmap,
        exportConfig: ExportConfig = ExportConfig(),
        customFilename: String? = null
    ): SaveResult = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val albumName = "简切图"
        val isPng = exportConfig.format == ExportFormat.PNG
        val compressFormat = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val quality = if (isPng) 100 else exportConfig.quality.coerceIn(50, 100)
        val ext = exportConfig.format.extension
        val mimeType = exportConfig.format.mimeType

        val exportBitmap = prepareExportBitmap(compositeBitmap, exportConfig, 1, 1)
        val filename = customFilename ?: "grid_effect_${timestamp}$ext"

        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$albumName")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    exportBitmap.compress(compressFormat, quality, os)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                }

                if (exportBitmap != compositeBitmap) {
                    exportBitmap.recycle()
                }

                val formatDesc = "${exportConfig.format.name} ${(exportConfig.scaleFactor * 100).toInt()}%"
                SaveResult.Success(1, albumName, formatDesc)
            } else {
                SaveResult.Error("保存切图效果大图失败，未能写入相册")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            SaveResult.Error("保存出错: ${e.localizedMessage}")
        }
    }

    /**
     * Share single composite effect image
     */
    suspend fun shareCompositeEffectBitmap(
        context: Context,
        compositeBitmap: Bitmap,
        exportConfig: ExportConfig = ExportConfig()
    ) = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "shares").apply { mkdirs() }
        val isPng = exportConfig.format == ExportFormat.PNG
        val compressFormat = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val quality = if (isPng) 100 else exportConfig.quality.coerceIn(50, 100)
        val ext = exportConfig.format.extension
        val file = File(cacheDir, "grid_effect_${System.currentTimeMillis()}$ext")
        val exportBitmap = prepareExportBitmap(compositeBitmap, exportConfig, 1, 1)

        FileOutputStream(file).use { fos ->
            exportBitmap.compress(compressFormat, quality, fos)
        }
        if (exportBitmap != compositeBitmap) {
            exportBitmap.recycle()
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = exportConfig.format.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, "分享切图效果大图").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Save both individual sliced pieces AND the single composite effect image
     */
    suspend fun saveBothPiecesAndComposite(
        context: Context,
        pieces: List<SlicePiece>,
        compositeBitmap: Bitmap,
        exportConfig: ExportConfig = ExportConfig()
    ): SaveResult = withContext(Dispatchers.IO) {
        // 1. Save pieces
        val piecesResult = savePiecesToGallery(context, pieces, exportConfig)
        if (piecesResult is SaveResult.Error) {
            return@withContext piecesResult
        }
        val piecesCount = (piecesResult as SaveResult.Success).savedCount

        // 2. Save composite effect image
        val compResult = saveCompositeEffectBitmap(context, compositeBitmap, exportConfig)
        if (compResult is SaveResult.Success) {
            SaveResult.Success(piecesCount + 1, piecesResult.albumName, piecesResult.formatDesc)
        } else {
            piecesResult
        }
    }

    /**
     * Generate an anti-disorder posting guide card bitmap with numbered slots and thumbnails
     */
    fun generatePostingSequenceCard(
        pieces: List<SlicePiece>,
        layoutType: LayoutType
    ): Bitmap {
        val cardW = 1080
        val cardH = 1350
        val output = Bitmap.createBitmap(cardW, cardH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Background dark gradient
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#0F172A")
        }
        canvas.drawRect(0f, 0f, cardW.toFloat(), cardH.toFloat(), bgPaint)

        // Header Title
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 52f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("朋友圈 / 社媒防乱序发图指引", cardW / 2f, 100f, titlePaint)

        val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#94A3B8")
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("按编号 1 → ${pieces.size} 依次在微信/小红书勾选上传即可完美呈现", cardW / 2f, 150f, subTitlePaint)

        // Draw Nine Grid Thumbnail Area
        val gridMargin = 80f
        val gridTop = 220f
        val gridW = cardW - gridMargin * 2
        val rows = 3
        val cols = 3
        val cellW = (gridW - 24f * (cols - 1)) / cols
        val cellH = cellW

        val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 34f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#6366F1")
        }

        pieces.take(9).forEachIndexed { idx, piece ->
            val r = idx / cols
            val c = idx % cols
            val x = gridMargin + c * (cellW + 24f)
            val y = gridTop + r * (cellH + 24f)
            val rectF = RectF(x, y, x + cellW, y + cellH)

            // Draw thumbnail
            val bmp = piece.bitmap
            if (bmp != null) {
                canvas.drawBitmap(bmp, null, rectF, cellPaint)
            } else {
                val placeholderPaint = Paint().apply { color = android.graphics.Color.DKGRAY }
                canvas.drawRect(rectF, placeholderPaint)
            }

            // Draw Number Badge
            val badgeRadius = 32f
            val bx = x + badgeRadius + 14f
            val by = y + badgeRadius + 14f
            canvas.drawCircle(bx, by, badgeRadius, badgeBgPaint)
            val badgeTextY = by - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText("${piece.index}", bx, badgeTextY, textPaint)
        }

        // Tips at bottom
        val tipCardY = gridTop + 3 * cellH + 60f
        val tipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#1E293B")
        }
        val tipRect = RectF(gridMargin, tipCardY, cardW - gridMargin, tipCardY + 220f)
        canvas.drawRoundRect(tipRect, 24f, 24f, tipPaint)

        val tipTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#38BDF8")
            textSize = 32f
            isFakeBoldText = true
        }
        canvas.drawText("💡 发朋友圈小贴士：", gridMargin + 30f, tipCardY + 55f, tipTitlePaint)

        val tipContentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#E2E8F0")
            textSize = 26f
        }
        canvas.drawText("1. 微信朋友圈选择照片时，严格按照第 1 张至第 9 张的顺序点击", gridMargin + 30f, tipCardY + 110f, tipContentPaint)
        canvas.drawText("2. 发出后九宫格无缝拼合，呈现震撼视觉效果！", gridMargin + 30f, tipCardY + 160f, tipContentPaint)

        return output
    }

    /**
     * Save posting sequence guide card to gallery
     */
    suspend fun savePostingSequenceCard(
        context: Context,
        pieces: List<SlicePiece>,
        layoutType: LayoutType,
        exportConfig: ExportConfig = ExportConfig()
    ): SaveResult = withContext(Dispatchers.IO) {
        val guideBitmap = generatePostingSequenceCard(pieces, layoutType)
        val albumName = "简切图"
        val timestamp = System.currentTimeMillis()
        val filename = "posting_guide_${timestamp}.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$albumName")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                guideBitmap.compress(Bitmap.CompressFormat.JPEG, 95, os)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }
            guideBitmap.recycle()
            SaveResult.Success(1, albumName, "JPG 发图指引卡")
        } else {
            guideBitmap.recycle()
            SaveResult.Error("保存发图指引卡失败")
        }
    }
}
