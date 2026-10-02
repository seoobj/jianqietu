package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import com.example.model.ImageAnalysisResult
import com.example.model.ImageContentCategory
import com.example.model.LayoutType
import com.example.model.RecommendedLayout
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object SmartAnalyzer {

    /**
     * Analyze image composition, aspect ratio and visual features to recommend layouts
     */
    fun analyzeImage(bitmap: Bitmap): ImageAnalysisResult {
        val w = bitmap.width
        val h = bitmap.height
        val aspectRatio = w.toFloat() / h.toFloat()

        // Downsample to 48x48 for fast, memory-safe pixel analysis
        val thumb = try {
            Bitmap.createScaledBitmap(bitmap, 48, 48, false)
        } catch (e: Exception) {
            bitmap
        }

        var totalR = 0L
        var totalG = 0L
        var totalB = 0L
        var totalBrightness = 0.0

        var centerR = 0L
        var centerG = 0L
        var centerB = 0L
        var centerCount = 0

        val tw = thumb.width
        val th = thumb.height

        for (y in 0 until th) {
            for (x in 0 until tw) {
                val pixel = thumb.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                totalR += r
                totalG += g
                totalB += b
                totalBrightness += (0.299 * r + 0.587 * g + 0.114 * b)

                // Check center area (from 25% to 75%)
                if (x in (tw / 4)..(3 * tw / 4) && y in (th / 4)..(3 * th / 4)) {
                    centerR += r
                    centerG += g
                    centerB += b
                    centerCount++
                }
            }
        }

        val totalPixels = (tw * th).toDouble()
        val avgR = totalR / totalPixels
        val avgG = totalG / totalPixels
        val avgB = totalB / totalPixels
        val avgBrightness = totalBrightness / totalPixels

        val avgCenterR = if (centerCount > 0) centerR / centerCount.toDouble() else avgR
        val avgCenterG = if (centerCount > 0) centerG / centerCount.toDouble() else avgG
        val avgCenterB = if (centerCount > 0) centerB / centerCount.toDouble() else avgB

        // Center visual contrast with background
        val centerContrast = abs(avgCenterR - avgR) + abs(avgCenterG - avgG) + abs(avgCenterB - avgB)

        // Determine Category
        val category: ImageContentCategory
        val summary: String
        val details: String
        val recommendations = mutableListOf<RecommendedLayout>()

        when {
            aspectRatio >= 1.5f -> {
                // Wide Panoramic Landscape
                category = ImageContentCategory.PANORAMIC_LANDSCAPE
                summary = "宽屏全景画幅 (比例 ${String.format("%.2f", aspectRatio)}:1)"
                details = "检测到壮阔宽幅构图，视野开阔且横向延展度高，极其适合多段连贯全景展示。"

                recommendations.add(
                    RecommendedLayout(
                        layoutType = LayoutType.GRID_1x3,
                        title = "三联横切 (1×3) · 首选",
                        reason = "朋友圈发圈横向三联屏，展开如画卷般气势磅礴，保留完整地平线",
                        score = 98
                    )
                )
                recommendations.add(
                    RecommendedLayout(
                        layoutType = LayoutType.CREATIVE_TOP_HERO,
                        title = "全景画报 (1顶+3底)",
                        reason = "上方宽幅统揽全貌，下方三格展示水面、山峦或前景细节",
                        score = 94
                    )
                )
                recommendations.add(
                    RecommendedLayout(
                        layoutType = LayoutType.FREE_SPLIT_LINES,
                        title = "自由切割线 (自由比例)",
                        reason = "自如调整分割线位置，避开人物或重要景物边缘",
                        score = 88
                    )
                )
            }

            aspectRatio <= 0.7f -> {
                // Tall Portrait / Long Vertical
                category = ImageContentCategory.TALL_PORTRAIT
                summary = "垂直长图 / 全身人像 (比例 1:${String.format("%.2f", 1f / aspectRatio)})"
                details = "检测到纵向修长比例，适合展示垂直人像身姿、高挑建筑或长条幅视觉。"

                recommendations.add(
                    RecommendedLayout(
                        layoutType = LayoutType.GRID_3x1,
                        title = "三联竖切 (3×1) · 首选",
                        reason = "垂直分割三段，发圈后上下滑动连击呈现完整人物视觉纵深",
                        score = 97
                    )
                )
                recommendations.add(
                    RecommendedLayout(
                        layoutType = LayoutType.GRID_3x2,
                        title = "六宫格竖版 (3×2)",
                        reason = "三行两列节奏排布，兼顾细节特写与整体纵向美感",
                        score = 90
                    )
                )
                recommendations.add(
                    RecommendedLayout(
                        layoutType = LayoutType.GRID_3x4,
                        title = "十二格画册 (3×4)",
                        reason = "适合制作长微博画报或精细特写拼图",
                        score = 85
                    )
                )
            }

            centerContrast > 35.0 || (aspectRatio in 0.85f..1.35f && (avgCenterR > avgR * 1.15)) -> {
                // Prominent Subject / Close-up
                category = ImageContentCategory.FEATURE_SUBJECT
                summary = "主体突出 / 层次人物画报"
                details = "检测到中心/偏侧具有高对比度的主体焦点，非常适合主次分明的层次画报切图。"

                recommendations.add(
                    RecommendedLayout(
                        layoutType = LayoutType.CREATIVE_HERO_SPLIT,
                        title = "主角画报 (1大+2小) · 首选",
                        reason = "左侧大图展现主体人物与环境，右侧上下两格特写神态与局部",
                        score = 99
                    )
                )
                recommendations.add(
                    RecommendedLayout(
                        layoutType = LayoutType.GRID_3x3,
                        title = "经典九宫格 (中心C位)",
                        reason = "将主体人物安放于第5格正中心，四周八格烘托氛围",
                        score = 92
                    )
                )
                recommendations.add(
                    RecommendedLayout(
                        layoutType = LayoutType.CREATIVE_HEART_9,
                        title = "爱心九宫格 (浪漫拼图)",
                        reason = "精巧心形排列，为主角增添精致温馨的社交媒体仪式感",
                        score = 89
                    )
                )
            }

            aspectRatio in 0.9f..1.12f -> {
                // Square Lifestyle
                category = ImageContentCategory.SQUARE_LIFESTYLE
                summary = "方形打卡 / 社交生活"
                details = "检测到几乎完美的 1:1 正方形构图，与社交平台九宫格有天然的黄金契合度。"

                recommendations.add(
                    RecommendedLayout(
                        layoutType = LayoutType.GRID_3x3,
                        title = "经典九宫格 (3×3) · 绝配",
                        reason = "无缝贴合朋友圈与小红书，9张切片浑然一体，带来极致视觉张力",
                        score = 99
                    )
                )
                recommendations.add(
                    RecommendedLayout(
                        layoutType = LayoutType.GRID_2x2,
                        title = "四宫格 (2×2)",
                        reason = "四等分清新明快，简洁耐看，阅读负担极小",
                        score = 93
                    )
                )
                recommendations.add(
                    RecommendedLayout(
                        layoutType = LayoutType.CREATIVE_HEART_9,
                        title = "心形九宫格",
                        reason = "方正画幅中呈现爱心造型，发圈点赞拉满",
                        score = 91
                    )
                )
            }

            else -> {
                // Balanced Photography / Art
                category = ImageContentCategory.BALANCED_ART
                summary = "艺术摄影 / 丰富画面构图"
                details = "色彩饱满、元素丰富的摄影作品，每个分区都拥有独立耐看的细节。"

                recommendations.add(
                    RecommendedLayout(
                        layoutType = LayoutType.GRID_3x3,
                        title = "经典九宫格 (3×3) · 首选",
                        reason = "朋友圈经典排版，切分后每一张都是一张精彩的小明信片",
                        score = 96
                    )
                )
                recommendations.add(
                    RecommendedLayout(
                        layoutType = LayoutType.FREE_SPLIT_LINES,
                        title = "交互式自由切 (自定义分割)",
                        reason = "随心拖动水平与垂直切割线，按元素边界个性分割",
                        score = 91
                    )
                )
                recommendations.add(
                    RecommendedLayout(
                        layoutType = LayoutType.GRID_2x3,
                        title = "六宫格 (2×3)",
                        reason = "横向舒展两行排版，稳重大气",
                        score = 86
                    )
                )
            }
        }

        return ImageAnalysisResult(
            category = category,
            summary = summary,
            details = details,
            aspectRatio = aspectRatio,
            recommendations = recommendations
        )
    }
}
