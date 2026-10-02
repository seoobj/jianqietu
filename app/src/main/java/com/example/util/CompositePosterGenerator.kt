package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import com.example.model.LayoutType
import com.example.model.LineStyle
import com.example.model.MagazinePosition
import com.example.model.MagazineTypographyConfig
import com.example.model.PosterBackgroundConfig
import com.example.model.PosterBgStyle
import com.example.model.PosterTexture
import com.example.model.SlicePiece
import com.example.model.SplitLine
import com.example.model.TileShadowStyle
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

object CompositePosterGenerator {

    /**
     * Render the complete poster effect with line styles, 3D shadows, background, and magazine typography
     */
    fun generateMasterPoster(
        sourceCropped: Bitmap,
        pieces: List<SlicePiece>,
        layoutType: LayoutType,
        customRows: Int,
        customCols: Int,
        splitLines: List<SplitLine>,
        freeBoxes: List<RectF>,
        lineColorHex: Long,
        lineWidthDp: Float,
        lineStyle: LineStyle,
        shadowStyle: TileShadowStyle,
        magazineConfig: MagazineTypographyConfig,
        posterBgConfig: PosterBackgroundConfig,
        heroFocusIndex: Int? = null,
        showIndices: Boolean = false
    ): Bitmap {
        val srcW = sourceCropped.width
        val srcH = sourceCropped.height

        // Outer padding for framing (e.g. 5% if polaroid or poster background enabled)
        val hasBorderPadding = posterBgConfig.style != PosterBgStyle.SOLID || posterBgConfig.texture != PosterTexture.NONE || shadowStyle != TileShadowStyle.NONE || lineStyle == LineStyle.POLAROID
        val borderPadX = if (hasBorderPadding) (srcW * 0.05f).coerceAtLeast(30f) else 0f
        val borderPadY = if (hasBorderPadding) (srcH * 0.05f).coerceAtLeast(30f) else 0f
        val bottomExtraForMagazine = if (magazineConfig.isEnabled && (magazineConfig.position == MagazinePosition.BOTTOM_CENTER || magazineConfig.position == MagazinePosition.BOTTOM_RIGHT)) {
            (srcH * 0.12f).coerceAtLeast(80f)
        } else {
            0f
        }

        val outW = (srcW + borderPadX * 2).toInt()
        val outH = (srcH + borderPadY * 2 + bottomExtraForMagazine).toInt()

        val output = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 1. Draw Background
        drawBackground(canvas, outW, outH, sourceCropped, posterBgConfig)

        // 2. Draw 3D Tile Shadow or Image Content
        val targetRect = RectF(borderPadX, borderPadY, borderPadX + srcW, borderPadY + srcH)
        
        if (shadowStyle != TileShadowStyle.NONE && pieces.isNotEmpty()) {
            // Draw individual elevated tiles with shadow
            drawElevatedTiles(canvas, targetRect, pieces, sourceCropped, shadowStyle, heroFocusIndex)
        } else {
            // Draw direct source bitmap with spotlight if enabled
            if (heroFocusIndex != null && pieces.isNotEmpty()) {
                drawSpotlightBitmap(canvas, targetRect, pieces, sourceCropped, heroFocusIndex)
            } else {
                canvas.drawBitmap(sourceCropped, null, targetRect, null)
            }
        }

        // 3. Draw Division Lines based on LineStyle
        if (shadowStyle == TileShadowStyle.NONE || lineStyle == LineStyle.NEON_GLOW || lineStyle == LineStyle.POLAROID) {
            drawLineOverlay(
                canvas = canvas,
                rect = targetRect,
                layoutType = layoutType,
                customRows = customRows,
                customCols = customCols,
                splitLines = splitLines,
                freeBoxes = freeBoxes,
                lineColor = (lineColorHex and 0xFFFFFFL).toInt() or 0xFF000000.toInt(),
                lineWidthPx = (lineWidthDp * 3f).coerceAtLeast(2f),
                lineStyle = lineStyle
            )
        }

        // 4. Draw Index Badges if enabled
        if (showIndices && pieces.isNotEmpty()) {
            drawIndexBadges(canvas, targetRect, pieces, (lineColorHex and 0xFFFFFFL).toInt() or 0xFF000000.toInt())
        }

        // 5. Draw Magazine Typography Overlay if enabled
        if (magazineConfig.isEnabled) {
            drawMagazineTypography(canvas, outW, outH, targetRect, magazineConfig)
        }

        return output
    }

    private fun drawBackground(
        canvas: Canvas,
        w: Int,
        h: Int,
        source: Bitmap,
        config: PosterBackgroundConfig
    ) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (config.style) {
            PosterBgStyle.SOLID -> {
                bgPaint.color = (config.colorHex and 0xFFFFFFL).toInt() or 0xFF000000.toInt()
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)
            }
            PosterBgStyle.GAUSSIAN_BLUR -> {
                // Scaled down & blurred approximation
                try {
                    val small = Bitmap.createScaledBitmap(source, max(1, source.width / 16), max(1, source.height / 16), true)
                    val filterPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG).apply {
                        alpha = 180
                    }
                    canvas.drawBitmap(small, null, RectF(0f, 0f, w.toFloat(), h.toFloat()), filterPaint)
                    small.recycle()
                    // Dark overlay for readability
                    val dimPaint = Paint().apply { color = Color.argb(120, 15, 23, 42) }
                    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), dimPaint)
                } catch (_: Exception) {
                    bgPaint.color = Color.parseColor("#0F172A")
                    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)
                }
            }
            PosterBgStyle.TEXTURE_PAPER -> {
                bgPaint.color = Color.parseColor("#F8FAFC")
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)
                // Draw texture grid grain
                val grainPaint = Paint().apply {
                    color = Color.argb(18, 0, 0, 0)
                    strokeWidth = 1f
                }
                var x = 0f
                while (x < w) {
                    canvas.drawLine(x, 0f, x, h.toFloat(), grainPaint)
                    x += 24f
                }
            }
        }
    }

    private fun drawElevatedTiles(
        canvas: Canvas,
        destRect: RectF,
        pieces: List<SlicePiece>,
        source: Bitmap,
        shadowStyle: TileShadowStyle,
        heroFocusIndex: Int?
    ) {
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb((shadowStyle.shadowAlpha * 255).toInt(), 0, 0, 0)
            style = Paint.Style.FILL
        }
        val cardRadius = if (shadowStyle == TileShadowStyle.POLAROID_FLOAT) 8f else 14f

        for (piece in pieces) {
            if (piece.isDecorative) continue
            val r = piece.relativeRect
            val px = destRect.left + r.left * destRect.width()
            val py = destRect.top + r.top * destRect.height()
            val pw = r.width() * destRect.width()
            val ph = r.height() * destRect.height()

            val tileRect = RectF(px + 4f, py + 4f, px + pw - 4f, py + ph - 4f)
            val shadowRect = RectF(tileRect.left + 3f, tileRect.top + shadowStyle.elevationDp, tileRect.right + 3f, tileRect.bottom + shadowStyle.elevationDp)

            // Draw Drop Shadow
            canvas.drawRoundRect(shadowRect, cardRadius, cardRadius, shadowPaint)

            // Draw Tile Piece
            val pieceBmp = piece.bitmap ?: try {
                val srcR = Rect(
                    (r.left * source.width).toInt().coerceIn(0, source.width - 1),
                    (r.top * source.height).toInt().coerceIn(0, source.height - 1),
                    (r.right * source.width).toInt().coerceIn(1, source.width),
                    (r.bottom * source.height).toInt().coerceIn(1, source.height)
                )
                Bitmap.createBitmap(source, srcR.left, srcR.top, max(1, srcR.width()), max(1, srcR.height()))
            } catch (_: Exception) {
                null
            }

            if (pieceBmp != null) {
                val p = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                if (heroFocusIndex != null && piece.index != heroFocusIndex) {
                    p.alpha = 140
                }
                canvas.drawBitmap(pieceBmp, null, tileRect, p)
            }
        }
    }

    private fun drawSpotlightBitmap(
        canvas: Canvas,
        destRect: RectF,
        pieces: List<SlicePiece>,
        source: Bitmap,
        heroFocusIndex: Int
    ) {
        val dimPaint = Paint().apply {
            alpha = 120
        }
        canvas.drawBitmap(source, null, destRect, dimPaint)

        val focusedPiece = pieces.find { it.index == heroFocusIndex }
        if (focusedPiece != null) {
            val r = focusedPiece.relativeRect
            val srcR = Rect(
                (r.left * source.width).toInt().coerceIn(0, source.width - 1),
                (r.top * source.height).toInt().coerceIn(0, source.height - 1),
                (r.right * source.width).toInt().coerceIn(1, source.width),
                (r.bottom * source.height).toInt().coerceIn(1, source.height)
            )
            val tileDest = RectF(
                destRect.left + r.left * destRect.width(),
                destRect.top + r.top * destRect.height(),
                destRect.left + r.right * destRect.width(),
                destRect.top + r.bottom * destRect.height()
            )
            canvas.drawBitmap(source, srcR, tileDest, null)

            // Highlight border
            val focusBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#38BDF8")
                strokeWidth = 6f
                style = Paint.Style.STROKE
            }
            canvas.drawRect(tileDest, focusBorder)
        }
    }

    private fun drawLineOverlay(
        canvas: Canvas,
        rect: RectF,
        layoutType: LayoutType,
        customRows: Int,
        customCols: Int,
        splitLines: List<SplitLine>,
        freeBoxes: List<RectF>,
        lineColor: Int,
        lineWidthPx: Float,
        lineStyle: LineStyle
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lineColor
            strokeWidth = lineWidthPx
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }

        when (lineStyle) {
            LineStyle.SOLID -> { /* default */ }
            LineStyle.DASHED -> {
                paint.pathEffect = DashPathEffect(floatArrayOf(lineWidthPx * 3f, lineWidthPx * 2f), 0f)
            }
            LineStyle.DOTTED -> {
                paint.pathEffect = DashPathEffect(floatArrayOf(lineWidthPx, lineWidthPx * 2f), 0f)
            }
            LineStyle.POLAROID -> {
                paint.strokeWidth = (lineWidthPx * 3.5f).coerceAtLeast(16f)
                paint.color = Color.WHITE
            }
            LineStyle.NEON_GLOW -> {
                paint.setShadowLayer(lineWidthPx * 2f, 0f, 0f, lineColor)
            }
            LineStyle.FILM_SPROCKET -> {
                paint.pathEffect = DashPathEffect(floatArrayOf(lineWidthPx * 4f, lineWidthPx * 1.5f), 0f)
            }
        }

        val w = rect.width()
        val h = rect.height()
        val ox = rect.left
        val oy = rect.top

        when (layoutType) {
            LayoutType.CREATIVE_HERO_SPLIT -> {
                val sx = ox + w * 0.62f
                val sy = oy + h * 0.5f
                canvas.drawLine(sx, oy, sx, oy + h, paint)
                canvas.drawLine(sx, sy, ox + w, sy, paint)
            }
            LayoutType.CREATIVE_TOP_HERO -> {
                val sy = oy + h * 0.55f
                canvas.drawLine(ox, sy, ox + w, sy, paint)
                val cw = w / 3f
                canvas.drawLine(ox + cw, sy, ox + cw, oy + h, paint)
                canvas.drawLine(ox + cw * 2f, sy, ox + cw * 2f, oy + h, paint)
            }
            LayoutType.FREE_SPLIT_LINES -> {
                for (l in splitLines) {
                    if (l.isVertical) {
                        val lx = ox + l.positionFraction * w
                        canvas.drawLine(lx, oy, lx, oy + h, paint)
                    } else {
                        val ly = oy + l.positionFraction * h
                        canvas.drawLine(ox, ly, ox + w, ly, paint)
                    }
                }
            }
            LayoutType.FREE_MULTI_REGIONS -> {
                for (b in freeBoxes) {
                    val r = RectF(ox + b.left * w, oy + b.top * h, ox + b.right * w, oy + b.bottom * h)
                    canvas.drawRect(r, paint)
                }
            }
            LayoutType.GRID_CUSTOM -> {
                drawGrid(canvas, rect, customRows.coerceIn(1, 10), customCols.coerceIn(1, 10), paint)
            }
            else -> {
                drawGrid(canvas, rect, layoutType.defaultRows, layoutType.defaultCols, paint)
            }
        }
    }

    private fun drawGrid(canvas: Canvas, rect: RectF, rows: Int, cols: Int, paint: Paint) {
        if (cols > 1) {
            val cw = rect.width() / cols
            for (c in 1 until cols) {
                val x = rect.left + c * cw
                canvas.drawLine(x, rect.top, x, rect.bottom, paint)
            }
        }
        if (rows > 1) {
            val rh = rect.height() / rows
            for (r in 1 until rows) {
                val y = rect.top + r * rh
                canvas.drawLine(rect.left, y, rect.right, y, paint)
            }
        }
    }

    private fun drawIndexBadges(canvas: Canvas, rect: RectF, pieces: List<SlicePiece>, badgeColor: Int) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(190, 0, 0, 0)
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = badgeColor
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        for (p in pieces) {
            if (p.isDecorative) continue
            val r = p.relativeRect
            val cx = rect.left + r.centerX() * rect.width()
            val cy = rect.top + r.centerY() * rect.height()
            val radius = 24f

            canvas.drawCircle(cx, cy, radius, bgPaint)
            canvas.drawCircle(cx, cy, radius, borderPaint)
            val ty = cy - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText(p.index.toString(), cx, ty, textPaint)
        }
    }

    private fun drawMagazineTypography(
        canvas: Canvas,
        w: Int,
        h: Int,
        imageRect: RectF,
        config: MagazineTypographyConfig
    ) {
        val textColor = (config.textColorHex and 0xFFFFFFL).toInt() or ((config.opacity * 255).toInt() shl 24)
        
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = (w * 0.045f).coerceIn(24f, 54f)
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = when (config.position) {
                MagazinePosition.TOP_CENTER, MagazinePosition.BOTTOM_CENTER -> Paint.Align.CENTER
                MagazinePosition.BOTTOM_RIGHT -> Paint.Align.RIGHT
                MagazinePosition.BOTTOM_LEFT -> Paint.Align.LEFT
            }
        }

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = (w * 0.024f).coerceIn(14f, 26f)
            letterSpacing = 0.15f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = titlePaint.textAlign
        }

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = (w * 0.020f).coerceIn(12f, 22f)
            typeface = Typeface.MONOSPACE
            textAlign = titlePaint.textAlign
        }

        val (posX, posY) = when (config.position) {
            MagazinePosition.TOP_CENTER -> Pair(w / 2f, imageRect.top - 12f)
            MagazinePosition.BOTTOM_CENTER -> Pair(w / 2f, imageRect.bottom + 42f)
            MagazinePosition.BOTTOM_RIGHT -> Pair(w - 24f, imageRect.bottom + 42f)
            MagazinePosition.BOTTOM_LEFT -> Pair(24f, imageRect.bottom + 42f)
        }

        canvas.drawText(config.mainTitle, posX, posY, titlePaint)
        canvas.drawText("${config.subTitle}  ·  ${config.dateText}", posX, posY + subPaint.textSize * 1.35f, subPaint)
        if (config.locationText.isNotEmpty()) {
            canvas.drawText("📍 ${config.locationText}", posX, posY + subPaint.textSize * 2.5f, datePaint)
        }
    }
}
