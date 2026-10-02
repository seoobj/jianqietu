package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.model.FilmTimestampConfig
import com.example.model.ImageFilterPreset
import com.example.model.LayoutType
import com.example.model.LineStyle
import com.example.model.MagazinePosition
import com.example.model.MagazineTemplate
import com.example.model.MagazineTypographyConfig
import com.example.model.StylePresetItem
import com.example.model.TileShadowStyle
import org.json.JSONArray
import org.json.JSONObject

object PresetManager {

    private const val PREFS_NAME = "slice_app_presets"
    private const val KEY_CUSTOM_PRESETS = "custom_presets_json"

    val OFFICIAL_PRESETS: List<StylePresetItem> = listOf(
        StylePresetItem(
            id = "official_xhs_warm_9",
            name = "小红书爆款暖调九宫格",
            description = "纯白细线 + 暖阳金辉滤镜 + 4dp微圆角",
            layoutType = LayoutType.GRID_3x3,
            gapFraction = 0.015f,
            cornerRadiusDp = 4f,
            lineColorHex = 0xFFFFFFFF,
            lineWidthDp = 3.0f,
            lineStyle = LineStyle.SOLID,
            filter = ImageFilterPreset.WARM,
            isOfficial = true
        ),
        StylePresetItem(
            id = "official_polaroid_4",
            name = "复古拍立得白边 (2×2)",
            description = "宽幅纯白边框 + 8dp悬浮浮雕 + 胶片色调",
            layoutType = LayoutType.GRID_2x2,
            gapFraction = 0.04f,
            cornerRadiusDp = 6f,
            lineColorHex = 0xFFFFFFFF,
            lineWidthDp = 8.0f,
            lineStyle = LineStyle.POLAROID,
            shadowStyle = TileShadowStyle.POLAROID_FLOAT,
            filter = ImageFilterPreset.FILM,
            filmTimestampConfig = FilmTimestampConfig(
                isEnabled = true,
                dateText = "2026.10.01",
                timeText = "15:59",
                cameraModel = "POLAROID SX-70"
            ),
            isOfficial = true
        ),
        StylePresetItem(
            id = "official_film_3strip",
            name = "35mm 胶片全景三联 (1×3)",
            description = "宽屏连贯画卷 + 胶片齿孔 + 液晶时间戳",
            layoutType = LayoutType.GRID_1x3,
            gapFraction = 0.02f,
            cornerRadiusDp = 2f,
            lineColorHex = 0xFF1E293B,
            lineWidthDp = 4.0f,
            lineStyle = LineStyle.FILM_SPROCKET,
            filter = ImageFilterPreset.FILM,
            filmTimestampConfig = FilmTimestampConfig(
                isEnabled = true,
                dateText = "2026.10.01",
                timeText = "16:20",
                cameraModel = "LEICA M11 // 35mm f/1.4"
            ),
            isOfficial = true
        ),
        StylePresetItem(
            id = "official_vogue_magazine",
            name = "高级轻奢杂志跨屏九宫格",
            description = "带 VOGUE 跨格杂志排印 + 质感黑白影调",
            layoutType = LayoutType.GRID_3x3,
            gapFraction = 0.01f,
            cornerRadiusDp = 0f,
            lineColorHex = 0xFFFFFFFF,
            lineWidthDp = 2.0f,
            lineStyle = LineStyle.SOLID,
            filter = ImageFilterPreset.BW,
            magazineConfig = MagazineTypographyConfig(
                isEnabled = true,
                template = MagazineTemplate.PARIS_VOGUE,
                mainTitle = "VOGUE & POETRY",
                subTitle = "AUTUMN ATELIER ISSUE",
                dateText = "OCT 2026",
                locationText = "PARIS",
                position = MagazinePosition.BOTTOM_RIGHT
            ),
            isOfficial = true
        ),
        StylePresetItem(
            id = "official_cyber_neon",
            name = "赛博霓虹发光四格 (2×2)",
            description = "赛博朋克调色 + 荧光青蓝线条 + 3D悬浮卡片",
            layoutType = LayoutType.GRID_2x2,
            gapFraction = 0.035f,
            cornerRadiusDp = 8f,
            lineColorHex = 0xFF00F0FF,
            lineWidthDp = 3.5f,
            lineStyle = LineStyle.NEON_GLOW,
            shadowStyle = TileShadowStyle.ELEVATED_CARD,
            filter = ImageFilterPreset.CYBERPUNK,
            isOfficial = true
        ),
        StylePresetItem(
            id = "official_heart_9",
            name = "爱心九宫格浪漫排版",
            description = "心型爱心排布 + 中心留白 + 日系温柔",
            layoutType = LayoutType.CREATIVE_HEART_9,
            gapFraction = 0.02f,
            cornerRadiusDp = 6f,
            lineColorHex = 0xFFFFFFFF,
            lineWidthDp = 3.0f,
            filter = ImageFilterPreset.JAPANESE,
            isCenterBlank = true,
            isOfficial = true
        ),
        StylePresetItem(
            id = "official_fuji_nc_9",
            name = "富士 NC 经典负片九宫格",
            description = "富士经典负片青红影调 + 纯白极细分割线",
            layoutType = LayoutType.GRID_3x3,
            gapFraction = 0.015f,
            cornerRadiusDp = 3f,
            lineColorHex = 0xFFFFFFFF,
            lineWidthDp = 2.5f,
            lineStyle = LineStyle.SOLID,
            filter = ImageFilterPreset.FUJI_NC,
            isOfficial = true
        ),
        StylePresetItem(
            id = "official_canon_portrait_4",
            name = "佳能 白皙人像四宫格",
            description = "通透红润白皙肤色 + 柔和圆角",
            layoutType = LayoutType.GRID_2x2,
            gapFraction = 0.025f,
            cornerRadiusDp = 8f,
            lineColorHex = 0xFFFFFFFF,
            lineWidthDp = 3.0f,
            filter = ImageFilterPreset.CANON_PORTRAIT,
            isOfficial = true
        ),
        StylePresetItem(
            id = "official_cinema_teal_orange",
            name = "好莱坞青橙电影三联 (1×3)",
            description = "电影级青橙冷暖反差 + 宽幅全景连卷",
            layoutType = LayoutType.GRID_1x3,
            gapFraction = 0.02f,
            cornerRadiusDp = 4f,
            lineColorHex = 0xFF0F172A,
            lineWidthDp = 3.0f,
            lineStyle = LineStyle.SOLID,
            filter = ImageFilterPreset.CINEMATIC_TEAL_ORANGE,
            isOfficial = true
        ),
        StylePresetItem(
            id = "official_monet_floral_9",
            name = "莫奈花园浪漫花艺九宫格",
            description = "柔粉淡紫油画花卉 + 典雅留白间距",
            layoutType = LayoutType.GRID_3x3,
            gapFraction = 0.025f,
            cornerRadiusDp = 6f,
            lineColorHex = 0xFFFFFFFF,
            lineWidthDp = 3.0f,
            filter = ImageFilterPreset.FLORAL_MONET,
            isOfficial = true
        ),
        StylePresetItem(
            id = "official_bridal_romance",
            name = "梦幻纯白法式婚礼 (2×2)",
            description = "纯白通透神圣婚纱光晕 + 法式典雅",
            layoutType = LayoutType.GRID_2x2,
            gapFraction = 0.03f,
            cornerRadiusDp = 10f,
            lineColorHex = 0xFFFFFFFF,
            lineWidthDp = 4.0f,
            filter = ImageFilterPreset.BRIDAL_PURE_WHITE,
            isOfficial = true
        )
    )

    fun loadAllPresets(context: Context): List<StylePresetItem> {
        val customPresets = loadCustomPresets(context)
        return OFFICIAL_PRESETS + customPresets
    }

    fun saveCustomPreset(context: Context, preset: StylePresetItem): Boolean {
        return try {
            val existing = loadCustomPresets(context).toMutableList()
            // Replace if same id exists or append
            val index = existing.indexOfFirst { it.id == preset.id }
            if (index >= 0) {
                existing[index] = preset
            } else {
                existing.add(0, preset)
            }
            saveCustomPresetsList(context, existing)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteCustomPreset(context: Context, presetId: String): Boolean {
        return try {
            val existing = loadCustomPresets(context).filterNot { it.id == presetId }
            saveCustomPresetsList(context, existing)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun loadCustomPresets(context: Context): List<StylePresetItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_CUSTOM_PRESETS, null) ?: return emptyList()
        val list = mutableListOf<StylePresetItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(deserializePreset(obj))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun saveCustomPresetsList(context: Context, list: List<StylePresetItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        for (item in list) {
            array.put(serializePreset(item))
        }
        prefs.edit().putString(KEY_CUSTOM_PRESETS, array.toString()).apply()
    }

    private fun serializePreset(p: StylePresetItem): JSONObject {
        return JSONObject().apply {
            put("id", p.id)
            put("name", p.name)
            put("description", p.description)
            put("layoutType", p.layoutType.name)
            put("customRows", p.customRows)
            put("customCols", p.customCols)
            put("gapFraction", p.gapFraction.toDouble())
            put("cornerRadiusDp", p.cornerRadiusDp.toDouble())
            put("lineColorHex", p.lineColorHex)
            put("lineWidthDp", p.lineWidthDp.toDouble())
            put("lineStyle", p.lineStyle.name)
            put("shadowStyle", p.shadowStyle.name)
            put("filter", p.filter.name)
            put("isCenterBlank", p.isCenterBlank)
            put("isOfficial", false)
        }
    }

    private fun deserializePreset(obj: JSONObject): StylePresetItem {
        val layoutName = obj.optString("layoutType", LayoutType.GRID_3x3.name)
        val layout = try { LayoutType.valueOf(layoutName) } catch (_: Exception) { LayoutType.GRID_3x3 }
        val lineStyleName = obj.optString("lineStyle", LineStyle.SOLID.name)
        val lineStyle = try { LineStyle.valueOf(lineStyleName) } catch (_: Exception) { LineStyle.SOLID }
        val shadowName = obj.optString("shadowStyle", TileShadowStyle.NONE.name)
        val shadow = try { TileShadowStyle.valueOf(shadowName) } catch (_: Exception) { TileShadowStyle.NONE }
        val filterName = obj.optString("filter", ImageFilterPreset.ORIGINAL.name)
        val filter = try { ImageFilterPreset.valueOf(filterName) } catch (_: Exception) { ImageFilterPreset.ORIGINAL }

        return StylePresetItem(
            id = obj.optString("id", System.currentTimeMillis().toString()),
            name = obj.optString("name", "未命名预设"),
            description = obj.optString("description", "自定义切图模板"),
            layoutType = layout,
            customRows = obj.optInt("customRows", 3),
            customCols = obj.optInt("customCols", 3),
            gapFraction = obj.optDouble("gapFraction", 0.02).toFloat(),
            cornerRadiusDp = obj.optDouble("cornerRadiusDp", 4.0).toFloat(),
            lineColorHex = obj.optLong("lineColorHex", 0xFFFFFFFF),
            lineWidthDp = obj.optDouble("lineWidthDp", 3.0).toFloat(),
            lineStyle = lineStyle,
            shadowStyle = shadow,
            filter = filter,
            isCenterBlank = obj.optBoolean("isCenterBlank", false),
            isOfficial = false
        )
    }
}
