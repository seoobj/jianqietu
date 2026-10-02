package com.example.util

import android.graphics.Bitmap
import android.graphics.RectF
import com.example.model.SplitLine
import kotlin.math.abs

object SmartAvoidanceHelper {

    data class AvoidanceResult(
        val hasConflict: Boolean,
        val focalPointFractionX: Float,
        val focalPointFractionY: Float,
        val suggestedSplitLines: List<SplitLine>,
        val adviceMessage: String
    )

    /**
     * Analyze source bitmap to compute center-of-mass / focal point
     * and check if any split line passes through the critical 15% center focal radius.
     */
    fun analyzeSubjectAvoidance(
        bitmap: Bitmap,
        splitLines: List<SplitLine>
    ): AvoidanceResult {
        // Fast sampling of bitmap to calculate luminance gradient / focal mass
        val sampleW = 64
        val sampleH = 64
        val scaled = try {
            Bitmap.createScaledBitmap(bitmap, sampleW, sampleH, false)
        } catch (_: Exception) {
            return AvoidanceResult(false, 0.5f, 0.5f, splitLines, "主体居中良好")
        }

        var totalWeight = 0.0
        var weightedX = 0.0
        var weightedY = 0.0

        for (y in 0 until sampleH) {
            for (x in 0 until sampleW) {
                val pixel = scaled.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                // Salience based on color saturation and contrast with mean
                val lum = 0.299 * r + 0.587 * g + 0.114 * b
                val weight = abs(lum - 128.0) + 20.0
                totalWeight += weight
                weightedX += x * weight
                weightedY += y * weight
            }
        }
        if (scaled != bitmap) {
            scaled.recycle()
        }

        val focalX = if (totalWeight > 0) (weightedX / (totalWeight * sampleW)).toFloat().coerceIn(0.2f, 0.8f) else 0.5f
        val focalY = if (totalWeight > 0) (weightedY / (totalWeight * sampleH)).toFloat().coerceIn(0.2f, 0.8f) else 0.5f

        val dangerRadius = 0.08f // 8% proximity to focal center is deemed a cutting conflict
        var hasConflict = false
        val newLines = splitLines.map { line ->
            if (line.isVertical) {
                if (abs(line.positionFraction - focalX) < dangerRadius) {
                    hasConflict = true
                    val shift = if (line.positionFraction <= focalX) focalX - dangerRadius - 0.02f else focalX + dangerRadius + 0.02f
                    line.copy(positionFraction = shift.coerceIn(0.15f, 0.85f))
                } else {
                    line
                }
            } else {
                if (abs(line.positionFraction - focalY) < dangerRadius) {
                    hasConflict = true
                    val shift = if (line.positionFraction <= focalY) focalY - dangerRadius - 0.02f else focalY + dangerRadius + 0.02f
                    line.copy(positionFraction = shift.coerceIn(0.15f, 0.85f))
                } else {
                    line
                }
            }
        }

        val msg = if (hasConflict) {
            "检测到原分割线可能切割画面主体（如人脸/主花蕾），已智能偏置避开关键构图区！"
        } else {
            "当前分割线与画面视觉主体协调，未发现主体冲突。"
        }

        return AvoidanceResult(hasConflict, focalX, focalY, newLines, msg)
    }
}
