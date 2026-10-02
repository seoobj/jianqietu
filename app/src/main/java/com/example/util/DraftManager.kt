package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import com.example.model.AspectRatioOption
import com.example.model.ImageFilterPreset
import com.example.model.LayoutCategory
import com.example.model.LayoutType
import com.example.model.LineStyle
import com.example.model.SplitLine
import com.example.model.TileShadowStyle
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

data class DraftProject(
    val title: String,
    val timestamp: Long,
    val layoutType: LayoutType,
    val category: LayoutCategory,
    val rows: Int,
    val cols: Int,
    val aspectRatio: AspectRatioOption,
    val cropRect: RectF,
    val gapFraction: Float,
    val cornerRadiusDp: Float,
    val lineColorHex: Long,
    val lineWidthDp: Float,
    val lineStyle: LineStyle,
    val shadowStyle: TileShadowStyle,
    val filter: ImageFilterPreset,
    val rotation: Float,
    val flipH: Boolean,
    val flipV: Boolean,
    val brightness: Float,
    val contrast: Float,
    val saturation: Float,
    val splitLines: List<SplitLine>
)

object DraftManager {

    private const val PREFS_NAME = "slice_draft_prefs"
    private const val KEY_HAS_DRAFT = "has_draft"
    private const val KEY_DRAFT_JSON = "draft_json"
    private const val DRAFT_IMAGE_FILENAME = "draft_source_image.png"

    fun saveDraft(
        context: Context,
        bitmap: Bitmap?,
        title: String,
        layoutType: LayoutType,
        category: LayoutCategory,
        rows: Int,
        cols: Int,
        aspectRatio: AspectRatioOption,
        cropRect: RectF,
        gapFraction: Float,
        cornerRadiusDp: Float,
        lineColorHex: Long,
        lineWidthDp: Float,
        lineStyle: LineStyle,
        shadowStyle: TileShadowStyle,
        filter: ImageFilterPreset,
        rotation: Float,
        flipH: Boolean,
        flipV: Boolean,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        splitLines: List<SplitLine>
    ) {
        if (bitmap == null) return
        try {
            // 1. Save bitmap to private cache file
            val file = File(context.filesDir, DRAFT_IMAGE_FILENAME)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }

            // 2. Build JSON
            val json = JSONObject().apply {
                put("title", title)
                put("timestamp", System.currentTimeMillis())
                put("layoutType", layoutType.name)
                put("category", category.name)
                put("rows", rows)
                put("cols", cols)
                put("aspectRatio", aspectRatio.name)
                put("cropLeft", cropRect.left.toDouble())
                put("cropTop", cropRect.top.toDouble())
                put("cropRight", cropRect.right.toDouble())
                put("cropBottom", cropRect.bottom.toDouble())
                put("gapFraction", gapFraction.toDouble())
                put("cornerRadiusDp", cornerRadiusDp.toDouble())
                put("lineColorHex", lineColorHex)
                put("lineWidthDp", lineWidthDp.toDouble())
                put("lineStyle", lineStyle.name)
                put("shadowStyle", shadowStyle.name)
                put("filter", filter.name)
                put("rotation", rotation.toDouble())
                put("flipH", flipH)
                put("flipV", flipV)
                put("brightness", brightness.toDouble())
                put("contrast", contrast.toDouble())
                put("saturation", saturation.toDouble())

                val linesArr = JSONArray()
                splitLines.forEach { line ->
                    val lineObj = JSONObject().apply {
                        put("id", line.id)
                        put("isVertical", line.isVertical)
                        put("pos", line.positionFraction.toDouble())
                    }
                    linesArr.put(lineObj)
                }
                put("splitLines", linesArr)
            }

            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_HAS_DRAFT, true)
                .putString(KEY_DRAFT_JSON, json.toString())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hasDraft(context: Context): Boolean {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val file = File(context.filesDir, DRAFT_IMAGE_FILENAME)
        return sp.getBoolean(KEY_HAS_DRAFT, false) && file.exists() && file.length() > 0
    }

    fun loadDraftProject(context: Context): Pair<DraftProject, Bitmap>? {
        return try {
            val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = sp.getString(KEY_DRAFT_JSON, null) ?: return null
            val file = File(context.filesDir, DRAFT_IMAGE_FILENAME)
            if (!file.exists()) return null

            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null
            val obj = JSONObject(jsonStr)

            val layoutType = try { LayoutType.valueOf(obj.getString("layoutType")) } catch (_: Exception) { LayoutType.GRID_3x3 }
            val category = try { LayoutCategory.valueOf(obj.getString("category")) } catch (_: Exception) { LayoutCategory.GRID }
            val aspectRatio = try { AspectRatioOption.valueOf(obj.getString("aspectRatio")) } catch (_: Exception) { AspectRatioOption.SQUARE_1_1 }
            val lineStyle = try { LineStyle.valueOf(obj.getString("lineStyle")) } catch (_: Exception) { LineStyle.SOLID }
            val shadowStyle = try { TileShadowStyle.valueOf(obj.getString("shadowStyle")) } catch (_: Exception) { TileShadowStyle.NONE }
            val filter = try { ImageFilterPreset.valueOf(obj.getString("filter")) } catch (_: Exception) { ImageFilterPreset.ORIGINAL }

            val cropRect = RectF(
                obj.optDouble("cropLeft", 0.0).toFloat(),
                obj.optDouble("cropTop", 0.0).toFloat(),
                obj.optDouble("cropRight", 1.0).toFloat(),
                obj.optDouble("cropBottom", 1.0).toFloat()
            )

            val splitLines = mutableListOf<SplitLine>()
            val linesArr = obj.optJSONArray("splitLines")
            if (linesArr != null) {
                for (i in 0 until linesArr.length()) {
                    val lineObj = linesArr.getJSONObject(i)
                    splitLines.add(
                        SplitLine(
                            id = lineObj.optString("id", "l_$i"),
                            isVertical = lineObj.optBoolean("isVertical", true),
                            positionFraction = lineObj.optDouble("pos", 0.5).toFloat()
                        )
                    )
                }
            }

            val project = DraftProject(
                title = obj.optString("title", "未命名草稿"),
                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                layoutType = layoutType,
                category = category,
                rows = obj.optInt("rows", 3),
                cols = obj.optInt("cols", 3),
                aspectRatio = aspectRatio,
                cropRect = cropRect,
                gapFraction = obj.optDouble("gapFraction", 0.02).toFloat(),
                cornerRadiusDp = obj.optDouble("cornerRadiusDp", 4.0).toFloat(),
                lineColorHex = obj.optLong("lineColorHex", 0xFFFFFFFFL),
                lineWidthDp = obj.optDouble("lineWidthDp", 3.0).toFloat(),
                lineStyle = lineStyle,
                shadowStyle = shadowStyle,
                filter = filter,
                rotation = obj.optDouble("rotation", 0.0).toFloat(),
                flipH = obj.optBoolean("flipH", false),
                flipV = obj.optBoolean("flipV", false),
                brightness = obj.optDouble("brightness", 0.0).toFloat(),
                contrast = obj.optDouble("contrast", 1.0).toFloat(),
                saturation = obj.optDouble("saturation", 1.0).toFloat(),
                splitLines = splitLines
            )

            Pair(project, bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearDraft(context: Context) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_HAS_DRAFT)
                .remove(KEY_DRAFT_JSON)
                .apply()
            val file = File(context.filesDir, DRAFT_IMAGE_FILENAME)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
