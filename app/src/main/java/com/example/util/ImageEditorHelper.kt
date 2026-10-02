package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asAndroidColorFilter
import com.example.model.ImageFilterPreset

object ImageEditorHelper {

    /**
     * Compute unified Android ColorMatrix for brightness, contrast, saturation, and filter
     */
    fun createCombinedColorMatrix(
        brightness: Float, // -0.5f .. 0.5f
        contrast: Float,   // 0.5f .. 1.8f (1.0 = normal)
        saturation: Float, // 0.0f .. 2.0f (1.0 = normal)
        filter: ImageFilterPreset
    ): ColorMatrix {
        val resultMatrix = ColorMatrix()

        // 1. Base filter matrix
        val filterMatrix = filter.getColorMatrix()
        resultMatrix.postConcat(filterMatrix)

        // 2. Brightness (-0.5 .. 0.5 mapped to -100 .. 100)
        if (kotlin.math.abs(brightness) > 0.001f) {
            val b = brightness * 200f
            val brightMatrix = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, b,
                    0f, 1f, 0f, 0f, b,
                    0f, 0f, 1f, 0f, b,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            resultMatrix.postConcat(brightMatrix)
        }

        // 3. Contrast (0.5 .. 1.8)
        if (kotlin.math.abs(contrast - 1f) > 0.001f) {
            val scale = contrast
            val translate = (-0.5f * scale + 0.5f) * 255f
            val contrastMatrix = ColorMatrix(
                floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            resultMatrix.postConcat(contrastMatrix)
        }

        // 4. Saturation (0.0 .. 2.0)
        if (kotlin.math.abs(saturation - 1f) > 0.001f) {
            val satMatrix = ColorMatrix().apply {
                setSaturation(saturation)
            }
            resultMatrix.postConcat(satMatrix)
        }

        return resultMatrix
    }

    /**
     * Generate Compose ColorFilter for realtime display in UI Image composable
     */
    fun createComposeColorFilter(
        brightness: Float,
        contrast: Float,
        saturation: Float,
        filter: ImageFilterPreset
    ): ColorFilter {
        val androidMatrix = createCombinedColorMatrix(brightness, contrast, saturation, filter)
        // Convert Android ColorMatrix into Compose ColorFilter
        val m = androidx.compose.ui.graphics.ColorMatrix(androidMatrix.array)
        return ColorFilter.colorMatrix(m)
    }

    /**
     * Render the processed bitmap with rotation, horizontal/vertical flip, and color matrix
     */
    fun renderEditedBitmap(
        source: Bitmap,
        rotationDegrees: Float,
        flipH: Boolean,
        flipV: Boolean,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        filter: ImageFilterPreset
    ): Bitmap {
        // Step 1: Handle matrix transform (rotation and flip)
        val matrix = Matrix()
        if (rotationDegrees != 0f) {
            matrix.postRotate(rotationDegrees)
        }
        val scaleX = if (flipH) -1f else 1f
        val scaleY = if (flipV) -1f else 1f
        if (flipH || flipV) {
            matrix.postScale(scaleX, scaleY)
        }

        val rotated = try {
            Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        } catch (e: Exception) {
            source
        }

        // Step 2: Handle color filter (brightness, contrast, saturation, filter)
        val needsColorAdjust = (filter != ImageFilterPreset.ORIGINAL ||
                kotlin.math.abs(brightness) > 0.001f ||
                kotlin.math.abs(contrast - 1f) > 0.001f ||
                kotlin.math.abs(saturation - 1f) > 0.001f)

        if (!needsColorAdjust) {
            return rotated
        }

        val output = Bitmap.createBitmap(rotated.width, rotated.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            val cm = createCombinedColorMatrix(brightness, contrast, saturation, filter)
            colorFilter = ColorMatrixColorFilter(cm)
        }
        canvas.drawBitmap(rotated, 0f, 0f, paint)

        if (rotated != source && rotated != output) {
            rotated.recycle()
        }

        return output
    }

    /**
     * Extract pixel color from a bitmap at relative normalized coordinates (0..1),
     * taking into account current rotation and flips.
     */
    fun extractColorFromBitmap(
        source: Bitmap,
        fracX: Float,
        fracY: Float,
        rotationDegrees: Float = 0f,
        flipH: Boolean = false,
        flipV: Boolean = false
    ): Long {
        var fx = fracX.coerceIn(0f, 1f)
        var fy = fracY.coerceIn(0f, 1f)

        if (flipH) fx = 1f - fx
        if (flipV) fy = 1f - fy

        val normRot = ((rotationDegrees.toInt() % 360) + 360) % 360
        val (mappedX, mappedY) = when (normRot) {
            90 -> Pair(fy, 1f - fx)
            180 -> Pair(1f - fx, 1f - fy)
            270 -> Pair(1f - fy, fx)
            else -> Pair(fx, fy)
        }

        val px = (mappedX * (source.width - 1)).toInt().coerceIn(0, source.width - 1)
        val py = (mappedY * (source.height - 1)).toInt().coerceIn(0, source.height - 1)

        val pixel = source.getPixel(px, py)
        return pixel.toLong() and 0xFFFFFFFFL
    }
}
