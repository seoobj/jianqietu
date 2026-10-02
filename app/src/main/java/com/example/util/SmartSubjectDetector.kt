package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import com.example.model.LayoutType
import com.example.model.SplitLine
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object SmartSubjectDetector {

    data class FocalDetectionResult(
        val subjectRect: RectF, // 0f..1f normalized relative to cropped space
        val isFaceOrPerson: Boolean,
        val confidence: Float,
        val hasCuttingCollision: Boolean,
        val collisionLineIds: List<String>,
        val suggestedShift: Float // negative: shift up/left, positive: shift down/right
    )

    /**
     * Detect prominent face / salient subject region using high-frequency skin/edge luminance heuristic
     */
    fun detectSalientSubject(bitmap: Bitmap, cropRect: RectF): RectF {
        val w = bitmap.width
        val h = bitmap.height

        val cropX1 = (cropRect.left * w).toInt().coerceIn(0, w - 1)
        val cropY1 = (cropRect.top * h).toInt().coerceIn(0, h - 1)
        val cropX2 = (cropRect.right * w).toInt().coerceIn(cropX1 + 1, w)
        val cropY2 = (cropRect.bottom * h).toInt().coerceIn(cropY1 + 1, h)

        val sampleStep = max(1, min(cropX2 - cropX1, cropY2 - cropY1) / 32)
        var totalWeight = 0f
        var weightedSumX = 0f
        var weightedSumY = 0f

        var skinPixelCount = 0
        var totalSampled = 0

        for (y in cropY1 until cropY2 step sampleStep) {
            for (x in cropX1 until cropX2 step sampleStep) {
                totalSampled++
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                // Heuristic skin-tone and high-contrast facial feature detector
                val isSkinTone = (r > 95 && g > 40 && b > 20 && (r - g) > 15 && r > b && (max(r, max(g, b)) - min(r, min(g, b))) > 15)
                val lum = 0.299f * r + 0.587f * g + 0.114f * b

                // Center proximity weight
                val relX = (x - cropX1).toFloat() / (cropX2 - cropX1).toFloat()
                val relY = (y - cropY1).toFloat() / (cropY2 - cropY1).toFloat()
                val centerDist = abs(relX - 0.5f) + abs(relY - 0.42f)
                val centerWeight = (1.5f - centerDist).coerceAtLeast(0.2f)

                val weight = if (isSkinTone) {
                    skinPixelCount++
                    3.5f * centerWeight
                } else {
                    val contrastWeight = abs(lum - 128f) / 128f
                    (1.0f + contrastWeight) * centerWeight
                }

                weightedSumX += relX * weight
                weightedSumY += relY * weight
                totalWeight += weight
            }
        }

        val centerX = if (totalWeight > 0) (weightedSumX / totalWeight).coerceIn(0.2f, 0.8f) else 0.5f
        val centerY = if (totalWeight > 0) (weightedSumY / totalWeight).coerceIn(0.2f, 0.8f) else 0.45f

        val boxWidth = if (skinPixelCount > totalSampled * 0.08f) 0.32f else 0.28f
        val boxHeight = if (skinPixelCount > totalSampled * 0.08f) 0.35f else 0.30f

        val left = (centerX - boxWidth / 2f).coerceIn(0.05f, 0.95f - boxWidth)
        val top = (centerY - boxHeight / 2f).coerceIn(0.05f, 0.95f - boxHeight)

        return RectF(left, top, left + boxWidth, top + boxHeight)
    }

    /**
     * Check if current layout split lines or 3x3 grid lines cut through the subject face center
     */
    fun analyzeCollision(
        subjectRect: RectF,
        layoutType: LayoutType,
        rows: Int,
        cols: Int,
        splitLines: List<SplitLine>
    ): FocalDetectionResult {
        val faceMargin = 0.04f
        val safeLeft = subjectRect.left + faceMargin
        val safeRight = subjectRect.right - faceMargin
        val safeTop = subjectRect.top + faceMargin
        val safeBottom = subjectRect.bottom - faceMargin

        val collisions = mutableListOf<String>()
        var shiftProposal = 0f

        when (layoutType) {
            LayoutType.FREE_SPLIT_LINES -> {
                for (line in splitLines) {
                    if (line.isVertical) {
                        if (line.positionFraction in safeLeft..safeRight) {
                            collisions.add(line.id)
                            // Propose shifting to the nearest side
                            val dLeft = abs(line.positionFraction - safeLeft)
                            val dRight = abs(line.positionFraction - safeRight)
                            shiftProposal = if (dLeft < dRight) -(dLeft + 0.05f) else (dRight + 0.05f)
                        }
                    } else {
                        if (line.positionFraction in safeTop..safeBottom) {
                            collisions.add(line.id)
                            val dTop = abs(line.positionFraction - safeTop)
                            val dBottom = abs(line.positionFraction - safeBottom)
                            shiftProposal = if (dTop < dBottom) -(dTop + 0.05f) else (dBottom + 0.05f)
                        }
                    }
                }
            }
            LayoutType.GRID_3x3, LayoutType.GRID_2x2, LayoutType.GRID_3x2, LayoutType.GRID_2x3, LayoutType.GRID_CUSTOM -> {
                val actualRows = if (layoutType == LayoutType.GRID_CUSTOM) rows else layoutType.defaultRows
                val actualCols = if (layoutType == LayoutType.GRID_CUSTOM) cols else layoutType.defaultCols

                for (r in 1 until actualRows) {
                    val lineY = r / actualRows.toFloat()
                    if (lineY in safeTop..safeBottom) {
                        collisions.add("row_$r")
                        val dTop = abs(lineY - safeTop)
                        val dBottom = abs(lineY - safeBottom)
                        shiftProposal = if (dTop < dBottom) -(dTop + 0.06f) else (dBottom + 0.06f)
                    }
                }

                for (c in 1 until actualCols) {
                    val lineX = c / actualCols.toFloat()
                    if (lineX in safeLeft..safeRight) {
                        collisions.add("col_$c")
                    }
                }
            }
            else -> {}
        }

        return FocalDetectionResult(
            subjectRect = subjectRect,
            isFaceOrPerson = true,
            confidence = 0.88f,
            hasCuttingCollision = collisions.isNotEmpty(),
            collisionLineIds = collisions,
            suggestedShift = shiftProposal
        )
    }

    /**
     * Compute adjusted split lines to avoid cutting the subject
     */
    fun avoidSplitLineCollision(
        splitLines: List<SplitLine>,
        subjectRect: RectF
    ): List<SplitLine> {
        return splitLines.map { line ->
            if (line.isVertical) {
                if (line.positionFraction in subjectRect.left..subjectRect.right) {
                    val mid = (subjectRect.left + subjectRect.right) / 2f
                    val newPos = if (line.positionFraction < mid) {
                        (subjectRect.left - 0.04f).coerceAtLeast(0.08f)
                    } else {
                        (subjectRect.right + 0.04f).coerceAtMost(0.92f)
                    }
                    line.copy(positionFraction = newPos)
                } else line
            } else {
                if (line.positionFraction in subjectRect.top..subjectRect.bottom) {
                    val mid = (subjectRect.top + subjectRect.bottom) / 2f
                    val newPos = if (line.positionFraction < mid) {
                        (subjectRect.top - 0.04f).coerceAtLeast(0.08f)
                    } else {
                        (subjectRect.bottom + 0.04f).coerceAtMost(0.92f)
                    }
                    line.copy(positionFraction = newPos)
                } else line
            }
        }
    }

    /**
     * Compute adjusted crop rect to avoid cutting the subject in a standard grid
     */
    fun avoidGridCropCollision(
        currentCrop: RectF,
        shiftYFraction: Float
    ): RectF {
        val newTop = (currentCrop.top + shiftYFraction * currentCrop.height()).coerceIn(0f, 1f - currentCrop.height())
        return RectF(currentCrop.left, newTop, currentCrop.right, newTop + currentCrop.height())
    }
}
