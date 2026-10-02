package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Supported Social Media Platforms
 */
enum class SocialPlatform(val displayName: String, val shortName: String, val brandColorHex: Long) {
    ALL("全部平台", "全部", 0xFF2563EB),
    WECHAT("微信朋友圈", "朋友圈", 0xFF07C160),
    XIAOHONGSHU("小红书", "小红书", 0xFFFF2442),
    INSTAGRAM("Instagram", "IG", 0xFFE1306C),
    WEIBO("微博", "微博", 0xFFFF8200);

    val color: Color get() = Color(brandColorHex)
}

/**
 * Social Media Proportion Presets
 */
data class SocialRatioPreset(
    val id: String,
    val platform: SocialPlatform,
    val title: String,
    val ratio: Float,
    val ratioLabel: String,
    val description: String,
    val recommendedLayout: LayoutType,
    val isGoldenStandard: Boolean = false
) {
    companion object {
        val ALL_PRESETS = listOf(
            // 小红书 (RED)
            SocialRatioPreset(
                id = "red_3_4",
                platform = SocialPlatform.XIAOHONGSHU,
                title = "小红书黄金封面",
                ratio = 3f / 4f, // 0.75
                ratioLabel = "3:4",
                description = "小红书官方推荐黄金尺寸，信息流曝光面积最大，吸睛点击率最高",
                recommendedLayout = LayoutType.GRID_3x4,
                isGoldenStandard = true
            ),
            SocialRatioPreset(
                id = "red_1_1",
                platform = SocialPlatform.XIAOHONGSHU,
                title = "小红书正方切图",
                ratio = 1.0f,
                ratioLabel = "1:1",
                description = "正方形排版，适合笔记内页翻页滑动查看细节",
                recommendedLayout = LayoutType.GRID_3x3,
                isGoldenStandard = false
            ),
            SocialRatioPreset(
                id = "red_9_16",
                platform = SocialPlatform.XIAOHONGSHU,
                title = "小红书全屏长图",
                ratio = 9f / 16f, // 0.5625
                ratioLabel = "9:16",
                description = "全屏沉浸大图，适合长条展示或竖向海报",
                recommendedLayout = LayoutType.GRID_3x1,
                isGoldenStandard = false
            ),

            // 微信朋友圈 (WeChat Moments)
            SocialRatioPreset(
                id = "wechat_1_1",
                platform = SocialPlatform.WECHAT,
                title = "朋友圈九宫格",
                ratio = 1.0f,
                ratioLabel = "1:1",
                description = "微信朋友圈九宫格标准正方形，排列最整齐，防止缩略图裁边",
                recommendedLayout = LayoutType.GRID_3x3,
                isGoldenStandard = true
            ),
            SocialRatioPreset(
                id = "wechat_3_1",
                platform = SocialPlatform.WECHAT,
                title = "朋友圈全景三联",
                ratio = 3.0f, // 3:1
                ratioLabel = "3:1",
                description = "横向全景三联屏展开，朋友圈前三张无缝震撼画卷连击",
                recommendedLayout = LayoutType.GRID_1x3,
                isGoldenStandard = false
            ),
            SocialRatioPreset(
                id = "wechat_16_9",
                platform = SocialPlatform.WECHAT,
                title = "朋友圈宽屏横图",
                ratio = 16f / 9f,
                ratioLabel = "16:9",
                description = "单张大图自适应横向展示，适合宽屏风光摄影",
                recommendedLayout = LayoutType.GRID_1x3,
                isGoldenStandard = false
            ),

            // Instagram
            SocialRatioPreset(
                id = "ig_4_5",
                platform = SocialPlatform.INSTAGRAM,
                title = "IG 竖版满屏贴文",
                ratio = 4f / 5f, // 0.8
                ratioLabel = "4:5",
                description = "Instagram 贴文黄金尺寸 (1080×1350)，占据主页信息流最大屏幕面积",
                recommendedLayout = LayoutType.GRID_2x2,
                isGoldenStandard = true
            ),
            SocialRatioPreset(
                id = "ig_1_1",
                platform = SocialPlatform.INSTAGRAM,
                title = "IG 经典方格",
                ratio = 1.0f,
                ratioLabel = "1:1",
                description = "主页 Profile 档案网格点对点对应，排版干净高级",
                recommendedLayout = LayoutType.GRID_3x3,
                isGoldenStandard = false
            ),
            SocialRatioPreset(
                id = "ig_9_16",
                platform = SocialPlatform.INSTAGRAM,
                title = "IG 快拍 Stories",
                ratio = 9f / 16f,
                ratioLabel = "9:16",
                description = "Stories 快拍与 Reels 竖屏全屏比例",
                recommendedLayout = LayoutType.GRID_3x1,
                isGoldenStandard = false
            ),

            // 微博 (Weibo)
            SocialRatioPreset(
                id = "weibo_1_1",
                platform = SocialPlatform.WEIBO,
                title = "微博九宫格",
                ratio = 1.0f,
                ratioLabel = "1:1",
                description = "微博九宫格发图黄金格式，缩略图完整无黑边",
                recommendedLayout = LayoutType.GRID_3x3,
                isGoldenStandard = true
            ),
            SocialRatioPreset(
                id = "weibo_3_4",
                platform = SocialPlatform.WEIBO,
                title = "微博精修竖图",
                ratio = 3f / 4f,
                ratioLabel = "3:4",
                description = "长图画报与写真精修，信息流展开更充实",
                recommendedLayout = LayoutType.GRID_3x4,
                isGoldenStandard = false
            )
        )
    }
}

/**
 * Result of Social Media Aspect Ratio Detection
 */
data class SocialDetectionResult(
    val nativeRatio: Float,
    val nativeRatioLabel: String,
    val bestMatchPreset: SocialRatioPreset,
    val matchScore: Int, // 0..100
    val cropLossPercent: Int, // estimated percentage of image cropped (e.g. 2%)
    val platformHighlight: String,
    val allRecommendedPresets: List<SocialRatioPreset>
)
