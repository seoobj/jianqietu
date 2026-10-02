package com.example.util

import com.example.model.SocialDetectionResult
import com.example.model.SocialPlatform
import com.example.model.SocialRatioPreset
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object SocialPlatformDetector {

    /**
     * Detect best matching social media platform aspect ratio for given image dimensions
     */
    fun detectBestPlatformRatio(width: Int, height: Int): SocialDetectionResult {
        val safeW = width.coerceAtLeast(1)
        val safeH = height.coerceAtLeast(1)
        val nativeRatio = safeW.toFloat() / safeH.toFloat()

        val ratioLabel = String.format("%.2f:1", nativeRatio)

        // Evaluate all presets
        val scoredPresets = SocialRatioPreset.ALL_PRESETS.map { preset ->
            val targetRatio = preset.ratio
            val diff = abs(nativeRatio - targetRatio)

            // Crop loss calculation
            val cropLoss = if (nativeRatio > targetRatio) {
                (nativeRatio - targetRatio) / nativeRatio
            } else {
                (targetRatio - nativeRatio) / targetRatio
            }
            val cropLossPercent = (cropLoss * 100f).roundToInt().coerceIn(0, 80)

            // Score: 100 minus cropLoss penalty, with small bonus for golden standards
            var score = (100 - cropLossPercent * 1.8f).roundToInt().coerceIn(10, 100)
            if (preset.isGoldenStandard && cropLossPercent <= 12) {
                score = min(100, score + 4)
            }

            Triple(preset, score, cropLossPercent)
        }.sortedByDescending { it.second }

        val best = scoredPresets.first()
        val bestPreset = best.first
        val bestScore = best.second
        val bestCropLoss = best.third

        val highlightMessage = when (bestPreset.platform) {
            SocialPlatform.XIAOHONGSHU -> {
                if (bestPreset.ratioLabel == "3:4") {
                    "原图接近 3:4 竖图，与【小红书黄金封面】几乎无缝契合，信息流曝光面积最大，吸睛率最高！"
                } else {
                    "契合【小红书】常用比例，适合社交排版发布！"
                }
            }
            SocialPlatform.WECHAT -> {
                if (bestPreset.ratioLabel == "1:1") {
                    "原图为 1:1 正方比例，与【微信朋友圈九宫格】天然契合，9张切图整齐排列、严丝合缝！"
                } else {
                    "宽屏开阔构图，推荐【微信朋友圈横向三联】，展开如全景画卷！"
                }
            }
            SocialPlatform.INSTAGRAM -> {
                if (bestPreset.ratioLabel == "4:5") {
                    "原图接近 4:5 比例，最契合【Instagram 竖版满屏贴文】，可占据信息流最饱满视野！"
                } else {
                    "契合【Instagram】主页档案网格贴文！"
                }
            }
            SocialPlatform.WEIBO -> {
                "与【微博九宫格】比例高度契合，缩略图完整无多余黑边！"
            }
            else -> "画面比例均衡，适合各大社交平台通用发布！"
        }

        val alternatives = scoredPresets.drop(1).take(5).map { it.first }

        return SocialDetectionResult(
            nativeRatio = nativeRatio,
            nativeRatioLabel = ratioLabel,
            bestMatchPreset = bestPreset,
            matchScore = bestScore,
            cropLossPercent = bestCropLoss,
            platformHighlight = highlightMessage,
            allRecommendedPresets = alternatives
        )
    }
}
