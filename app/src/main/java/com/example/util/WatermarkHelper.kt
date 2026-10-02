package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.example.model.WatermarkConfig
import com.example.model.WatermarkPosition
import com.example.model.WatermarkScope
import com.example.model.WatermarkType
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object WatermarkHelper {

    /**
     * Generate a default emblem/logo bitmap when user wants an image watermark
     */
    fun createDefaultEmblemBitmap(title: String = "ORIGINAL", subtitle: String = "COPYRIGHT"): Bitmap {
        val size = 200
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Circle stamp border
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 10f, paint)

        // Inner dashed ring
        paint.strokeWidth = 2f
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 20f, paint)

        // Text in center
        paint.style = Paint.Style.FILL
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("★ ★ ★", size / 2f, size * 0.38f, paint)

        paint.textSize = 22f
        canvas.drawText(title, size / 2f, size * 0.52f, paint)

        paint.textSize = 15f
        paint.isFakeBoldText = false
        canvas.drawText(subtitle, size / 2f, size * 0.65f, paint)

        return bitmap
    }

    /**
     * Apply watermark to a single slice piece bitmap according to WatermarkConfig
     */
    fun applyWatermark(
        source: Bitmap,
        config: WatermarkConfig,
        pieceIndex: Int,
        totalPieces: Int
    ): Bitmap {
        if (!config.enabled || config.type == WatermarkType.NONE) {
            return source
        }

        // Check Scope applicability
        when (config.scope) {
            WatermarkScope.LAST_PIECE_ONLY -> {
                if (pieceIndex != totalPieces) return source
            }
            WatermarkScope.CENTER_PIECE_ONLY -> {
                val centerIdx = if (totalPieces == 9) 5 else (totalPieces / 2 + 1)
                if (pieceIndex != centerIdx) return source
            }
            WatermarkScope.ALL_PIECES -> {
                // Apply to every piece
            }
        }

        val result = source.copy(Bitmap.Config.ARGB_8888, true) ?: return source
        val canvas = Canvas(result)
        val w = result.width.toFloat()
        val h = result.height.toFloat()

        when (config.type) {
            WatermarkType.TEXT -> {
                drawTextWatermark(canvas, w, h, config)
            }
            WatermarkType.IMAGE -> {
                val emblem = config.imageBitmap ?: createDefaultEmblemBitmap("COPYRIGHT", "简切图")
                drawImageWatermark(canvas, w, h, emblem, config)
            }
            WatermarkType.NONE -> {}
        }

        return result
    }

    private fun drawTextWatermark(canvas: Canvas, w: Float, h: Float, config: WatermarkConfig) {
        val text = config.text.ifBlank { "© 原创切图" }
        val alphaInt = (config.opacity.coerceIn(0.1f, 1.0f) * 255).roundToInt()

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = alphaInt
            // Responsive text size relative to tile width
            textSize = (config.textSizeSp * (w / 360f)).coerceIn(12f, 72f)
            isFakeBoldText = true
            setShadowLayer(4f, 2f, 2f, Color.argb(alphaInt, 0, 0, 0))
        }

        if (config.position == WatermarkPosition.TILED) {
            // Diagonal repeated tiling across canvas
            canvas.save()
            canvas.rotate(-30f, w / 2f, h / 2f)
            textPaint.alpha = (alphaInt * 0.45f).roundToInt()

            val stepX = (textPaint.measureText(text) * 1.6f).coerceAtLeast(150f)
            val stepY = (textPaint.textSize * 4f).coerceAtLeast(80f)

            var y = -h
            while (y < h * 2) {
                var x = -w
                while (x < w * 2) {
                    canvas.drawText(text, x, y, textPaint)
                    x += stepX
                }
                y += stepY
            }
            canvas.restore()
            return
        }

        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val textW = textPaint.measureText(text)
        val textH = textBounds.height().toFloat()

        val padX = w * 0.05f
        val padY = h * 0.06f

        val (x, y) = when (config.position) {
            WatermarkPosition.BOTTOM_RIGHT -> Pair(w - padX - textW, h - padY)
            WatermarkPosition.BOTTOM_LEFT -> Pair(padX, h - padY)
            WatermarkPosition.TOP_RIGHT -> Pair(w - padX - textW, padY + textH)
            WatermarkPosition.TOP_LEFT -> Pair(padX, padY + textH)
            WatermarkPosition.CENTER -> Pair((w - textW) / 2f, (h + textH) / 2f)
            WatermarkPosition.TILED -> Pair(padX, padY)
        }

        // Draw translucent dark background capsule behind text for premium readability
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb((alphaInt * 0.4f).roundToInt(), 0, 0, 0)
        }
        val pillPadH = textH * 0.4f
        val pillPadV = textH * 0.25f
        val bgRect = RectF(
            x - pillPadH,
            y - textH - pillPadV,
            x + textW + pillPadH,
            y + pillPadV
        )
        canvas.drawRoundRect(bgRect, 8f, 8f, bgPaint)

        canvas.drawText(text, x, y, textPaint)
    }

    private fun drawImageWatermark(canvas: Canvas, w: Float, h: Float, emblem: Bitmap, config: WatermarkConfig) {
        val targetSize = (min(w, h) * 0.22f).coerceAtLeast(40f).roundToInt()
        val scaledEmblem = try {
            Bitmap.createScaledBitmap(emblem, targetSize, targetSize, true)
        } catch (e: Exception) {
            emblem
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            alpha = (config.opacity.coerceIn(0.1f, 1.0f) * 255).roundToInt()
        }

        val padX = w * 0.05f
        val padY = h * 0.05f

        val (x, y) = when (config.position) {
            WatermarkPosition.BOTTOM_RIGHT -> Pair(w - padX - targetSize, h - padY - targetSize)
            WatermarkPosition.BOTTOM_LEFT -> Pair(padX, h - padY - targetSize)
            WatermarkPosition.TOP_RIGHT -> Pair(w - padX - targetSize, padY)
            WatermarkPosition.TOP_LEFT -> Pair(padX, padY)
            WatermarkPosition.CENTER, WatermarkPosition.TILED -> Pair((w - targetSize) / 2f, (h - targetSize) / 2f)
        }

        canvas.drawBitmap(scaledEmblem, x, y, paint)

        if (scaledEmblem != emblem) {
            scaledEmblem.recycle()
        }
    }
}
