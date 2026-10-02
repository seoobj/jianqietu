package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.media.ExifInterface
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Typeface
import com.example.model.LayoutType
import com.example.model.SliceCustomConfig
import com.example.model.SlicePiece
import com.example.model.SplitLine
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object BitmapHelper {

    /**
     * Decode a bitmap from URI with memory-safe downsampling (max ~2048px on longest side)
     */
    fun decodeSampledBitmapFromUri(context: Context, uri: Uri, maxDimension: Int = 2048): Bitmap? {
        return try {
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            val srcWidth = options.outWidth
            val srcHeight = options.outHeight
            if (srcWidth <= 0 || srcHeight <= 0) return null

            var inSampleSize = 1
            if (srcWidth > maxDimension || srcHeight > maxDimension) {
                val halfWidth = srcWidth / 2
                val halfHeight = srcHeight / 2
                while ((halfWidth / inSampleSize) >= maxDimension && (halfHeight / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            inputStream = context.contentResolver.openInputStream(uri)
            val decoded = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (decoded == null) return null

            // Handle EXIF orientation
            val orientation = getExifOrientation(context, uri)
            rotateBitmap(decoded, orientation)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getExifOrientation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return try {
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) {
                bitmap.recycle()
            }
            rotated
        } catch (e: OutOfMemoryError) {
            bitmap
        }
    }

    /**
     * Compute default center-crop rectangle in normalized (0.0 .. 1.0) coordinates
     * based on target aspect ratio (e.g. 1:1, 4:3, etc.)
     */
    fun computeCenterCropRect(srcWidth: Int, srcHeight: Int, targetRatio: Float?): RectF {
        if (targetRatio == null || targetRatio <= 0f) {
            return RectF(0f, 0f, 1f, 1f)
        }
        val imgRatio = srcWidth.toFloat() / srcHeight.toFloat()
        return if (imgRatio > targetRatio) {
            // Image is wider than target ratio: crop left and right
            val targetW = srcHeight * targetRatio
            val marginX = (srcWidth - targetW) / 2f
            RectF(marginX / srcWidth, 0f, (srcWidth - marginX) / srcWidth, 1f)
        } else {
            // Image is taller than target ratio: crop top and bottom
            val targetH = srcWidth / targetRatio
            val marginY = (srcHeight - targetH) / 2f
            RectF(0f, marginY / srcHeight, 1f, (srcHeight - marginY) / srcHeight)
        }
    }

    /**
     * Perform the actual slicing on a source Bitmap based on layout configuration
     */
    fun sliceBitmap(
        source: Bitmap,
        cropRect: RectF, // 0f..1f relative to source
        layoutType: LayoutType,
        customRows: Int = 3,
        customCols: Int = 3,
        splitLines: List<SplitLine> = emptyList(),
        freeBoxes: List<RectF> = emptyList(),
        gapFraction: Float = 0f,
        cornerRadiusDp: Float = 0f,
        perSliceConfigs: Map<Int, SliceCustomConfig> = emptyMap()
    ): List<SlicePiece> {
        val srcW = source.width
        val srcH = source.height

        // Calculate absolute crop rect on source
        val cropLeft = (cropRect.left * srcW).coerceIn(0f, srcW.toFloat())
        val cropTop = (cropRect.top * srcH).coerceIn(0f, srcH.toFloat())
        val cropRight = (cropRect.right * srcW).coerceIn(cropLeft + 1f, srcW.toFloat())
        val cropBottom = (cropRect.bottom * srcH).coerceIn(cropTop + 1f, srcH.toFloat())

        val cropW = cropRight - cropLeft
        val cropH = cropBottom - cropTop

        val baseCropped = try {
            Bitmap.createBitmap(
                source,
                cropLeft.roundToInt(),
                cropTop.roundToInt(),
                cropW.roundToInt().coerceAtLeast(1),
                cropH.roundToInt().coerceAtLeast(1)
            )
        } catch (e: Exception) {
            source
        }

        val pieces = mutableListOf<SlicePiece>()

        when (layoutType) {
            LayoutType.CREATIVE_HERO_SPLIT -> {
                // Left 2/3 is large hero (2x2 area), Right 1/3 has 2 stacked vertical tiles
                val leftRatio = 0.62f
                // Piece 1: Left big hero
                pieces.add(
                    createSubSlice(
                        baseCropped, 1, "主图",
                        RectF(0f, 0f, leftRatio, 1f),
                        gapFraction, cornerRadiusDp
                    )
                )
                // Piece 2: Right Top
                pieces.add(
                    createSubSlice(
                        baseCropped, 2, "特写 1",
                        RectF(leftRatio, 0f, 1f, 0.5f),
                        gapFraction, cornerRadiusDp
                    )
                )
                // Piece 3: Right Bottom
                pieces.add(
                    createSubSlice(
                        baseCropped, 3, "特写 2",
                        RectF(leftRatio, 0.5f, 1f, 1f),
                        gapFraction, cornerRadiusDp
                    )
                )
            }

            LayoutType.CREATIVE_TOP_HERO -> {
                // Top half is wide panoramic banner (Piece 1)
                // Bottom half is 3 equal columns (Pieces 2, 3, 4)
                val topRatio = 0.55f
                pieces.add(
                    createSubSlice(
                        baseCropped, 1, "全景横幅",
                        RectF(0f, 0f, 1f, topRatio),
                        gapFraction, cornerRadiusDp
                    )
                )
                for (col in 0 until 3) {
                    val x1 = col / 3f
                    val x2 = (col + 1) / 3f
                    pieces.add(
                        createSubSlice(
                            baseCropped, col + 2, "细节 ${col + 1}",
                            RectF(x1, topRatio, x2, 1f),
                            gapFraction, cornerRadiusDp
                        )
                    )
                }
            }

            LayoutType.CREATIVE_HEART_9 -> {
                // Standard 3x3 layout, but highlighted with heart formation:
                // Grid:
                // 1(x) 2(♥) 3(x)  -> or WeChat Heart 9-grid format
                // In 3x3, heart shape covers tiles:
                // Row 0: [0,0]=skip or heart top-left, [0,1]=heart top, [0,2]=skip or heart top-right
                // Let's create all 9 tiles with WeChat/Weibo heart post order (1 to 9),
                // marking the non-heart corner tiles as decorative or subtle.
                val rows = 3
                val cols = 3
                var index = 1
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val isCorner = (r == 0 && (c == 0 || c == 2))
                        val x1 = c / cols.toFloat()
                        val y1 = r / rows.toFloat()
                        val x2 = (c + 1) / cols.toFloat()
                        val y2 = (r + 1) / rows.toFloat()
                        pieces.add(
                            createSubSlice(
                                baseCropped, index, "格 $index",
                                RectF(x1, y1, x2, y2),
                                gapFraction, cornerRadiusDp,
                                isDecorative = isCorner
                            )
                        )
                        index++
                    }
                }
            }

            LayoutType.FREE_SPLIT_LINES -> {
                // Custom split lines partition the crop area
                val vSplits = splitLines.filter { it.isVertical }.map { it.positionFraction }.sorted()
                val hSplits = splitLines.filter { !it.isVertical }.map { it.positionFraction }.sorted()

                val xBounds = listOf(0f) + vSplits + listOf(1f)
                val yBounds = listOf(0f) + hSplits + listOf(1f)

                var index = 1
                for (r in 0 until (yBounds.size - 1)) {
                    for (c in 0 until (xBounds.size - 1)) {
                        val x1 = xBounds[c]
                        val x2 = xBounds[c + 1]
                        val y1 = yBounds[r]
                        val y2 = yBounds[r + 1]
                        pieces.add(
                            createSubSlice(
                                baseCropped, index, "切片 $index",
                                RectF(x1, y1, x2, y2),
                                gapFraction, cornerRadiusDp
                            )
                        )
                        index++
                    }
                }
            }

            LayoutType.FREE_MULTI_REGIONS -> {
                if (freeBoxes.isNotEmpty()) {
                    freeBoxes.forEachIndexed { i, box ->
                        pieces.add(
                            createSubSlice(
                                baseCropped, i + 1, "特写 ${i + 1}",
                                box, gapFraction, cornerRadiusDp
                            )
                        )
                    }
                } else {
                    // Default single free box
                    pieces.add(
                        createSubSlice(
                            baseCropped, 1, "自由切片",
                            RectF(0f, 0f, 1f, 1f),
                            gapFraction, cornerRadiusDp
                        )
                    )
                }
            }

            else -> {
                // Uniform grid slicing
                val rows = when (layoutType) {
                    LayoutType.GRID_3x3 -> 3
                    LayoutType.GRID_2x2 -> 2
                    LayoutType.GRID_2x3 -> 2
                    LayoutType.GRID_3x2 -> 3
                    LayoutType.GRID_1x3 -> 1
                    LayoutType.GRID_3x1 -> 3
                    LayoutType.GRID_3x4 -> 3
                    LayoutType.GRID_CUSTOM -> customRows.coerceIn(1, 10)
                    LayoutType.FREE_CUSTOM_CROP -> 1
                    else -> 3
                }
                val cols = when (layoutType) {
                    LayoutType.GRID_3x3 -> 3
                    LayoutType.GRID_2x2 -> 2
                    LayoutType.GRID_2x3 -> 3
                    LayoutType.GRID_3x2 -> 2
                    LayoutType.GRID_1x3 -> 3
                    LayoutType.GRID_3x1 -> 1
                    LayoutType.GRID_3x4 -> 4
                    LayoutType.GRID_CUSTOM -> customCols.coerceIn(1, 10)
                    LayoutType.FREE_CUSTOM_CROP -> 1
                    else -> 3
                }

                var index = 1
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val x1 = c / cols.toFloat()
                        val y1 = r / rows.toFloat()
                        val x2 = (c + 1) / cols.toFloat()
                        val y2 = (r + 1) / rows.toFloat()

                        val label = if (rows == 3 && cols == 3) {
                            when (index) {
                                1 -> "1·左上"
                                2 -> "2·正上"
                                3 -> "3·右上"
                                4 -> "4·正左"
                                5 -> "5·中心"
                                6 -> "6·正右"
                                7 -> "7·左下"
                                8 -> "8·正下"
                                9 -> "9·右下"
                                else -> "$index"
                            }
                        } else {
                            "第 $index 格"
                        }

                        pieces.add(
                            createSubSlice(
                                baseCropped, index, label,
                                RectF(x1, y1, x2, y2),
                                gapFraction, cornerRadiusDp
                            )
                        )
                        index++
                    }
                }
            }
        }

        return pieces
    }

    private fun createSubSlice(
        src: Bitmap,
        index: Int,
        label: String,
        relRect: RectF,
        gapFraction: Float,
        cornerRadiusDp: Float,
        isDecorative: Boolean = false,
        perSliceConfigs: Map<Int, SliceCustomConfig> = emptyMap()
    ): SlicePiece {
        val totalW = src.width.toFloat()
        val totalH = src.height.toFloat()
        val config = perSliceConfigs[index] ?: SliceCustomConfig()

        var left = (relRect.left * totalW)
        var top = (relRect.top * totalH)
        var right = (relRect.right * totalW)
        var bottom = (relRect.bottom * totalH)

        // Apply gap shrink if specified
        if (gapFraction > 0.001f) {
            val insetX = (right - left) * (gapFraction * 0.5f)
            val insetY = (bottom - top) * (gapFraction * 0.5f)
            left += insetX
            top += insetY
            right -= insetX
            bottom -= insetY
        }

        val pieceW = (right - left).roundToInt().coerceIn(1, src.width)
        val pieceH = (bottom - top).roundToInt().coerceIn(1, src.height)

        if (config.isBlank) {
            val blankBmp = Bitmap.createBitmap(pieceW, pieceH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(blankBmp)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = (config.blankColorHex and 0xFFFFFFL).toInt() or 0xFF000000.toInt()
            }
            if (cornerRadiusDp > 0.5f) {
                canvas.drawRoundRect(RectF(0f, 0f, pieceW.toFloat(), pieceH.toFloat()), cornerRadiusDp * 2f, cornerRadiusDp * 2f, paint)
            } else {
                canvas.drawRect(0f, 0f, pieceW.toFloat(), pieceH.toFloat(), paint)
            }
            if (!config.customText.isNullOrBlank()) {
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (config.blankColorHex == 0xFFFFFFFF) Color.BLACK else Color.WHITE
                    textSize = (pieceW * 0.12f).coerceIn(16f, 48f)
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                canvas.drawText(config.customText, pieceW / 2f, pieceH / 2f + textPaint.textSize * 0.35f, textPaint)
            }
            return SlicePiece(
                id = index,
                index = index,
                label = if (!config.customText.isNullOrBlank()) config.customText else "留白格 $index",
                relativeRect = relRect,
                bitmap = blankBmp,
                isDecorative = false,
                isHidden = true,
                customConfig = config
            )
        }

        // Apply micro pan and zoom offset if configured
        val zoom = config.zoomScale.coerceIn(0.6f, 3.0f)
        val targetCropW = (pieceW / zoom).roundToInt().coerceIn(1, src.width)
        val targetCropH = (pieceH / zoom).roundToInt().coerceIn(1, src.height)
        val centerCenterX = left + pieceW / 2f + (config.panOffsetX * pieceW)
        val centerCenterY = top + pieceH / 2f + (config.panOffsetY * pieceH)

        val cropX = (centerCenterX - targetCropW / 2f).roundToInt().coerceIn(0, src.width - targetCropW)
        val cropY = (centerCenterY - targetCropH / 2f).roundToInt().coerceIn(0, src.height - targetCropH)

        val subBitmap = try {
            val cropped = Bitmap.createBitmap(
                src,
                cropX,
                cropY,
                targetCropW,
                targetCropH
            )
            val scaled = if (zoom != 1.0f) {
                Bitmap.createScaledBitmap(cropped, pieceW, pieceH, true)
            } else {
                cropped
            }

            // Apply rotation and flip if present
            val matrix = Matrix()
            var hasTransform = false
            if (config.rotationAngle != 0f) {
                matrix.postRotate(config.rotationAngle)
                hasTransform = true
            }
            if (config.flipH) {
                matrix.postScale(-1f, 1f)
                hasTransform = true
            }
            val transformed = if (hasTransform) {
                Bitmap.createBitmap(scaled, 0, 0, scaled.width, scaled.height, matrix, true)
            } else {
                scaled
            }

            // Apply filter override if present
            val filtered = if (config.filterOverride != null && config.filterOverride != com.example.model.ImageFilterPreset.ORIGINAL) {
                val filterBmp = Bitmap.createBitmap(transformed.width, transformed.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(filterBmp)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(config.filterOverride.getColorMatrix())
                }
                canvas.drawBitmap(transformed, 0f, 0f, paint)
                filterBmp
            } else {
                transformed
            }

            if (cornerRadiusDp > 0.5f) {
                applyRoundedCorners(filtered, cornerRadiusDp * (pieceW / 200f).coerceAtLeast(1f))
            } else {
                filtered
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        return SlicePiece(
            id = index,
            index = index,
            label = label,
            relativeRect = relRect,
            bitmap = subBitmap,
            isDecorative = isDecorative,
            isHidden = false,
            customConfig = config
        )
    }

    private fun applyRoundedCorners(src: Bitmap, radius: Float): Bitmap {
        return try {
            val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val rect = Rect(0, 0, src.width, src.height)
            val rectF = RectF(rect)
            canvas.drawRoundRect(rectF, radius, radius, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(src, rect, rect, paint)
            output
        } catch (e: Exception) {
            src
        }
    }

    /**
     * Render a single composite image with slicing grid lines and optional overall outer rounded corners,
     * margins and background canvas (for designer poster & card exports).
     */
    fun renderCompositeGridBitmap(
        source: Bitmap,
        cropRect: RectF,
        layoutType: LayoutType,
        customRows: Int = 3,
        customCols: Int = 3,
        splitLines: List<SplitLine> = emptyList(),
        freeBoxes: List<RectF> = emptyList(),
        lineColorHex: Long = 0xFFFFFFFFL,
        lineWidthDp: Float = 3.0f,
        showIndices: Boolean = false,
        outerCornerRadiusDp: Float = 0f,
        outerPaddingDp: Float = 0f,
        outerBgColorHex: Long = 0x00000000L,
        outerCardShadow: Boolean = false
    ): Bitmap {
        val srcW = source.width
        val srcH = source.height

        val cropLeft = (cropRect.left * srcW).coerceIn(0f, srcW.toFloat())
        val cropTop = (cropRect.top * srcH).coerceIn(0f, srcH.toFloat())
        val cropRight = (cropRect.right * srcW).coerceIn(cropLeft + 1f, srcW.toFloat())
        val cropBottom = (cropRect.bottom * srcH).coerceIn(cropTop + 1f, srcH.toFloat())

        val cropW = (cropRight - cropLeft).roundToInt().coerceAtLeast(1)
        val cropH = (cropBottom - cropTop).roundToInt().coerceAtLeast(1)

        val baseCropped = try {
            Bitmap.createBitmap(
                source,
                cropLeft.roundToInt(),
                cropTop.roundToInt(),
                cropW,
                cropH
            )
        } catch (e: Exception) {
            source
        }

        val rawGridBmp = Bitmap.createBitmap(baseCropped.width, baseCropped.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(rawGridBmp)

        val w = baseCropped.width.toFloat()
        val h = baseCropped.height.toFloat()

        // 1. Draw base cropped image
        canvas.drawBitmap(baseCropped, 0f, 0f, null)

        // 2. Stroke paint scaled to bitmap resolution
        val scaleFactor = (max(w, h) / 480f).coerceAtLeast(1f)
        val strokePx = (lineWidthDp * scaleFactor).coerceIn(2f, 48f)
        val paintColor = (lineColorHex and 0xFFFFFFFFL).toInt()

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = paintColor
            strokeWidth = strokePx
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.SQUARE
        }

        // 3. Draw dividing lines based on layoutType
        when (layoutType) {
            LayoutType.CREATIVE_HERO_SPLIT -> {
                val splitX = w * 0.62f
                val splitY = h * 0.5f
                canvas.drawLine(splitX, 0f, splitX, h, linePaint)
                canvas.drawLine(splitX, splitY, w, splitY, linePaint)
            }
            LayoutType.CREATIVE_TOP_HERO -> {
                val splitY = h * 0.55f
                canvas.drawLine(0f, splitY, w, splitY, linePaint)
                val colW = w / 3f
                canvas.drawLine(colW, splitY, colW, h, linePaint)
                canvas.drawLine(colW * 2f, splitY, colW * 2f, h, linePaint)
            }
            LayoutType.FREE_SPLIT_LINES -> {
                for (line in splitLines) {
                    if (line.isVertical) {
                        val lx = line.positionFraction * w
                        canvas.drawLine(lx, 0f, lx, h, linePaint)
                    } else {
                        val ly = line.positionFraction * h
                        canvas.drawLine(0f, ly, w, ly, linePaint)
                    }
                }
            }
            LayoutType.FREE_MULTI_REGIONS -> {
                for (box in freeBoxes) {
                    val rect = RectF(box.left * w, box.top * h, box.right * w, box.bottom * h)
                    canvas.drawRect(rect, linePaint)
                }
            }
            LayoutType.GRID_CUSTOM -> {
                drawStandardGridLines(canvas, w, h, customRows.coerceIn(1, 10), customCols.coerceIn(1, 10), linePaint)
            }
            else -> {
                drawStandardGridLines(canvas, w, h, layoutType.defaultRows, layoutType.defaultCols, linePaint)
            }
        }

        if (showIndices) {
            drawIndexBadgesOnCanvas(canvas, w, h, layoutType, customRows, customCols, paintColor)
        }

        if (baseCropped != source && baseCropped != rawGridBmp) {
            baseCropped.recycle()
        }

        // 4. Apply overall rounded corners if configured
        val cornerRadiusPx = (outerCornerRadiusDp * scaleFactor).coerceAtLeast(0f)
        val roundedGridBmp = if (cornerRadiusPx > 0.5f) {
            applyRoundedCorners(rawGridBmp, cornerRadiusPx)
        } else {
            rawGridBmp
        }

        // 5. Handle outer padding, canvas background, and floating card shadow
        val paddingPx = (outerPaddingDp * scaleFactor).coerceAtLeast(0f)
        val hasPadding = paddingPx > 0.5f
        val hasBg = outerBgColorHex != 0x00000000L
        val hasShadow = outerCardShadow

        if (!hasPadding && !hasBg && !hasShadow) {
            return roundedGridBmp
        }

        val extraPad = if (hasShadow) (16f * scaleFactor) else 0f
        val totalPad = paddingPx + extraPad
        val finalW = (roundedGridBmp.width + totalPad * 2).toInt()
        val finalH = (roundedGridBmp.height + totalPad * 2).toInt()

        val finalOutput = Bitmap.createBitmap(finalW, finalH, Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalOutput)

        // Draw Canvas Background
        if (hasBg) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = (outerBgColorHex and 0xFFFFFFFFL).toInt()
            }
            finalCanvas.drawRect(0f, 0f, finalW.toFloat(), finalH.toFloat(), bgPaint)
        }

        // Draw Soft Drop Shadow if enabled
        if (hasShadow) {
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(50, 0, 0, 0)
                maskFilter = android.graphics.BlurMaskFilter(14f * scaleFactor, android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
            val shadowRect = RectF(
                totalPad - 2f * scaleFactor,
                totalPad + 4f * scaleFactor,
                totalPad + roundedGridBmp.width + 2f * scaleFactor,
                totalPad + roundedGridBmp.height + 8f * scaleFactor
            )
            val rad = if (cornerRadiusPx > 0.5f) cornerRadiusPx else 4f * scaleFactor
            finalCanvas.drawRoundRect(shadowRect, rad, rad, shadowPaint)
        }

        // Draw the rounded grid bitmap
        finalCanvas.drawBitmap(roundedGridBmp, totalPad, totalPad, null)

        if (roundedGridBmp != rawGridBmp) {
            roundedGridBmp.recycle()
        }
        if (rawGridBmp != finalOutput) {
            rawGridBmp.recycle()
        }

        return finalOutput
    }

    private fun drawStandardGridLines(
        canvas: Canvas,
        w: Float,
        h: Float,
        rows: Int,
        cols: Int,
        paint: Paint
    ) {
        if (cols > 1) {
            val colW = w / cols
            for (c in 1 until cols) {
                val x = c * colW
                canvas.drawLine(x, 0f, x, h, paint)
            }
        }
        if (rows > 1) {
            val rowH = h / rows
            for (r in 1 until rows) {
                val y = r * rowH
                canvas.drawLine(0f, y, w, y, paint)
            }
        }
    }

    private fun drawIndexBadgesOnCanvas(
        canvas: Canvas,
        w: Float,
        h: Float,
        layoutType: LayoutType,
        customRows: Int,
        customCols: Int,
        badgeColor: Int
    ) {
        val rows = if (layoutType == LayoutType.GRID_CUSTOM) customRows else layoutType.defaultRows
        val cols = if (layoutType == LayoutType.GRID_CUSTOM) customCols else layoutType.defaultCols
        if (rows <= 0 || cols <= 0) return

        val colW = w / cols
        val rowH = h / rows
        val badgeRadius = (min(colW, rowH) * 0.16f).coerceIn(16f, 50f)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(190, 0, 0, 0)
            style = Paint.Style.FILL
        }
        val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = badgeColor
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = badgeRadius * 1.1f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        var idx = 1
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cx = c * colW + colW / 2f
                val cy = r * rowH + rowH / 2f
                canvas.drawCircle(cx, cy, badgeRadius, bgPaint)
                canvas.drawCircle(cx, cy, badgeRadius, rimPaint)
                val textY = cy - ((textPaint.descent() + textPaint.ascent()) / 2f)
                canvas.drawText(idx.toString(), cx, textY, textPaint)
                idx++
            }
        }
    }
}
