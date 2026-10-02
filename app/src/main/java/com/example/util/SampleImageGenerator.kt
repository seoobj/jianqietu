package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader

data class SamplePreset(
    val id: String,
    val name: String,
    val tag: String,
    val primaryColor: Int,
    val secondaryColor: Int
)

object SampleImageGenerator {

    val PRESETS = listOf(
        SamplePreset("sunset", "霞光海岸", "风景摄影", Color.parseColor("#FF5E62"), Color.parseColor("#FF9966")),
        SamplePreset("cyber", "未来霓虹", "科技潮流", Color.parseColor("#0F2027"), Color.parseColor("#2C5364")),
        SamplePreset("spring", "花漾春晨", "清新艺术", Color.parseColor("#11998E"), Color.parseColor("#38EF7D")),
        SamplePreset("abstract", "抽象色块", "现代极简", Color.parseColor("#8E2DE2"), Color.parseColor("#4A00E0"))
    )

    fun generatePresetBitmap(id: String, width: Int = 1200, height: Int = 1200): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (id) {
            "sunset" -> drawSunset(canvas, width, height, paint)
            "cyber" -> drawCyber(canvas, width, height, paint)
            "spring" -> drawSpring(canvas, width, height, paint)
            else -> drawAbstract(canvas, width, height, paint)
        }

        return bitmap
    }

    private fun drawSunset(canvas: Canvas, w: Int, h: Int, paint: Paint) {
        // Sky gradient
        val skyGradient = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(
                Color.parseColor("#2C3E50"),
                Color.parseColor("#FD746C"),
                Color.parseColor("#FF9068"),
                Color.parseColor("#FFD200")
            ),
            floatArrayOf(0f, 0.4f, 0.7f, 1.0f),
            Shader.TileMode.CLAMP
        )
        paint.shader = skyGradient
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Glowing Sun
        paint.color = Color.WHITE
        paint.shader = RadialGradient(
            w * 0.5f, h * 0.45f, w * 0.25f,
            intArrayOf(Color.parseColor("#FFFFEE"), Color.parseColor("#FFF3B0"), Color.TRANSPARENT),
            floatArrayOf(0f, 0.4f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(w * 0.5f, h * 0.45f, w * 0.25f, paint)
        paint.shader = null

        // Mountain layers
        paint.color = Color.parseColor("#5A2D82")
        val path1 = Path().apply {
            moveTo(0f, h * 0.65f)
            lineTo(w * 0.25f, h * 0.52f)
            lineTo(w * 0.55f, h * 0.62f)
            lineTo(w * 0.85f, h * 0.50f)
            lineTo(w.toFloat(), h * 0.68f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        canvas.drawPath(path1, paint)

        // Closer mountain
        paint.color = Color.parseColor("#2A164D")
        val path2 = Path().apply {
            moveTo(0f, h * 0.75f)
            lineTo(w * 0.4f, h * 0.65f)
            lineTo(w * 0.7f, h * 0.72f)
            lineTo(w.toFloat(), h * 0.62f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        canvas.drawPath(path2, paint)

        // Water
        val waterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, h * 0.75f, 0f, h.toFloat(),
                Color.parseColor("#1B1464"), Color.parseColor("#0C0824"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, h * 0.75f, w.toFloat(), h.toFloat(), waterPaint)

        // Water reflections
        paint.color = Color.parseColor("#66FFD200")
        for (i in 0..15) {
            val y = h * 0.77f + i * (h * 0.014f)
            val barW = (w * 0.35f) * (1f - (i / 20f))
            canvas.drawRoundRect(w * 0.5f - barW / 2, y, w * 0.5f + barW / 2, y + 4f, 4f, 4f, paint)
        }
    }

    private fun drawCyber(canvas: Canvas, w: Int, h: Int, paint: Paint) {
        val bgShader = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            intArrayOf(Color.parseColor("#090A0F"), Color.parseColor("#170F2C"), Color.parseColor("#0A192F")),
            null, Shader.TileMode.CLAMP
        )
        paint.shader = bgShader
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Grid lines
        paint.color = Color.parseColor("#3300F2FE")
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        val step = w / 12
        for (x in 0..w step step) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), h.toFloat(), paint)
        }
        for (y in 0..h step step) {
            canvas.drawLine(0f, y.toFloat(), w.toFloat(), y.toFloat(), paint)
        }

        paint.style = Paint.Style.FILL
        // Glowing Neon Circles
        paint.shader = RadialGradient(
            w * 0.7f, h * 0.35f, w * 0.35f,
            intArrayOf(Color.parseColor("#FFFF007F"), Color.parseColor("#887928CA"), Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP
        )
        canvas.drawCircle(w * 0.7f, h * 0.35f, w * 0.35f, paint)

        paint.shader = RadialGradient(
            w * 0.3f, h * 0.65f, w * 0.3f,
            intArrayOf(Color.parseColor("#FF00F2FE"), Color.parseColor("#664FACFE"), Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP
        )
        canvas.drawCircle(w * 0.3f, h * 0.65f, w * 0.3f, paint)
        paint.shader = null
    }

    private fun drawSpring(canvas: Canvas, w: Int, h: Int, paint: Paint) {
        val bg = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(Color.parseColor("#E0F2FE"), Color.parseColor("#BAE6FD"), Color.parseColor("#FEF08A")),
            null, Shader.TileMode.CLAMP
        )
        paint.shader = bg
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Soft pastel organic shapes
        paint.color = Color.parseColor("#86EFAC")
        canvas.drawCircle(w * 0.2f, h * 0.8f, w * 0.3f, paint)

        paint.color = Color.parseColor("#FCA5A5")
        canvas.drawCircle(w * 0.85f, h * 0.25f, w * 0.28f, paint)

        paint.color = Color.parseColor("#FDE047")
        canvas.drawCircle(w * 0.5f, h * 0.5f, w * 0.22f, paint)

        paint.color = Color.parseColor("#A78BFA")
        canvas.drawOval(w * 0.35f, h * 0.65f, w * 0.95f, h * 0.95f, paint)
    }

    private fun drawAbstract(canvas: Canvas, w: Int, h: Int, paint: Paint) {
        val bg = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            intArrayOf(Color.parseColor("#3B82F6"), Color.parseColor("#8B5CF6"), Color.parseColor("#EC4899")),
            null, Shader.TileMode.CLAMP
        )
        paint.shader = bg
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        paint.color = Color.WHITE
        paint.alpha = 220
        canvas.drawCircle(w * 0.5f, h * 0.5f, w * 0.32f, paint)

        paint.color = Color.parseColor("#F59E0B")
        paint.alpha = 240
        canvas.drawRoundRect(w * 0.15f, h * 0.15f, w * 0.45f, h * 0.45f, 40f, 40f, paint)

        paint.color = Color.parseColor("#10B981")
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.65f)
            lineTo(w * 0.85f, h * 0.9f)
            lineTo(w * 0.25f, h * 0.9f)
            close()
        }
        canvas.drawPath(path, paint)
    }
}
