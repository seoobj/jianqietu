package com.example.model

import android.graphics.Bitmap
import android.graphics.RectF

/**
 * Aspect Ratio Options for Slicing
 */
enum class AspectRatioOption(val label: String, val ratio: Float?) {
    ORIGINAL("原图比例", null),
    SQUARE_1_1("1:1 正方形", 1.0f),
    RATIO_4_3("4:3 横图", 4.0f / 3.0f),
    RATIO_3_4("3:4 竖图", 3.0f / 4.0f),
    RATIO_16_9("16:9 宽屏", 16.0f / 9.0f),
    RATIO_9_16("9:16 手机", 9.0f / 16.0f),
}

/**
 * Layout Categories for tabs
 */
enum class LayoutCategory(val title: String) {
    GRID("经典网格"),
    CREATIVE("创意布局"),
    FREE("自由切割"),
}

/**
 * Layout Presets
 */
enum class LayoutType(
    val category: LayoutCategory,
    val title: String,
    val subtitle: String,
    val defaultRows: Int,
    val defaultCols: Int
) {
    GRID_3x3(LayoutCategory.GRID, "九宫格 (3×3)", "微信/小红书/微博经典发圈", 3, 3),
    GRID_2x2(LayoutCategory.GRID, "四宫格 (2×2)", "清新简约四连拼", 2, 2),
    GRID_2x3(LayoutCategory.GRID, "六宫格 (2×3)", "横版排布，两行三列", 2, 3),
    GRID_3x2(LayoutCategory.GRID, "六宫格 (3×2)", "竖版排布，三行两列", 3, 2),
    GRID_1x3(LayoutCategory.GRID, "三联横切 (1×3)", "横屏壁纸、三联连击", 1, 3),
    GRID_3x1(LayoutCategory.GRID, "三联竖切 (3×1)", "长图垂直分割三段", 3, 1),
    GRID_3x4(LayoutCategory.GRID, "十二格 (3×4)", "长微博/画册多格精剪", 3, 4),
    GRID_CUSTOM(LayoutCategory.GRID, "自定义网格", "自由设定任意行数与列数", 3, 3),

    CREATIVE_HERO_SPLIT(LayoutCategory.CREATIVE, "主角画报 (1大+2小)", "左侧大画幅，右侧两格特写", 2, 2),
    CREATIVE_TOP_HERO(LayoutCategory.CREATIVE, "全景画报 (1顶+3底)", "顶部宽幅全景，底部三格细节", 2, 3),
    CREATIVE_HEART_9(LayoutCategory.CREATIVE, "心形拼图 (爱心网格)", "精选爱心排布心型视觉", 3, 3),
    CREATIVE_CENTER_FOCUS(LayoutCategory.CREATIVE, "中心特写 (田字环绕)", "中心大图，四周八格环伺", 3, 3),

    FREE_CUSTOM_CROP(LayoutCategory.FREE, "自由矩形切片", "自由拉伸任意区域裁切", 1, 1),
    FREE_SPLIT_LINES(LayoutCategory.FREE, "交互式切割线", "拖动任意水平/垂直切割线", 2, 2),
    FREE_MULTI_REGIONS(LayoutCategory.FREE, "多局部特写提取", "在图上圈出多处局部自由导出", 1, 1)
}

/**
 * Per-slice customization config (Micro-Adjustments)
 */
data class SliceCustomConfig(
    val isBlank: Boolean = false,               // 留白/镂空 (Hollow out / Blank tile)
    val blankColorHex: Long = 0xFFFFFFFF,       // 留白背景色 (默认纯白，可选莫兰迪/深灰等)
    val panOffsetX: Float = 0f,                 // 微调平移 X (-0.4f .. +0.4f)
    val panOffsetY: Float = 0f,                 // 微调平移 Y (-0.4f .. +0.4f)
    val zoomScale: Float = 1.0f,                // 局部独立微调缩放 (0.8f .. 2.5f)
    val rotationAngle: Float = 0f,              // 0, 90, 180, 270 度
    val flipH: Boolean = false,                 // 单片水平镜像
    val filterOverride: ImageFilterPreset? = null, // 单片独立滤镜 (如单张黑白)
    val customText: String? = null              // 单片贴纸或表情文字
)

/**
 * Single sliced piece data
 */
data class SlicePiece(
    val id: Int,
    val index: Int,            // 1-based order for social posting
    val label: String,
    val relativeRect: RectF,   // 0.0 .. 1.0 fraction on the cropped bitmap
    val bitmap: Bitmap? = null,
    val isDecorative: Boolean = false, // if true, skipped or heart placeholder
    val isHidden: Boolean = false,
    val customConfig: SliceCustomConfig = SliceCustomConfig()
)

/**
 * Slicing result holding all cut pieces
 */
data class SliceResult(
    val id: String = System.currentTimeMillis().toString(),
    val layoutType: LayoutType,
    val originalWidth: Int,
    val originalHeight: Int,
    val pieces: List<SlicePiece>,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Draggable cut line for interactive split
 */
data class SplitLine(
    val id: String,
    val isVertical: Boolean,
    val positionFraction: Float // 0.0 to 1.0
)

/**
 * Free region selection
 */
data class FreeSelectionBox(
    val id: String,
    val name: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

/**
 * Snapshot of the editor state for Undo / Redo history
 */
data class EditorSnapshot(
    val cropRect: RectF,
    val aspectRatio: AspectRatioOption,
    val selectedLayout: LayoutType,
    val selectedCategory: LayoutCategory,
    val customRows: Int,
    val customCols: Int,
    val gapFraction: Float,
    val cornerRadiusDp: Float,
    val splitLines: List<SplitLine>,
    val freeBoxes: List<RectF>,
    val rotationAngle: Float,
    val flipHorizontal: Boolean,
    val flipVertical: Boolean,
    val brightness: Float,
    val contrast: Float,
    val saturation: Float,
    val selectedFilter: ImageFilterPreset,
    val lineColorHex: Long = 0xFFFFFFFF,
    val lineWidthDp: Float = 3.0f,
    val lineStyle: LineStyle = LineStyle.SOLID,
    val shadowStyle: TileShadowStyle = TileShadowStyle.NONE,
    val outerCornerRadiusDp: Float = 0f,
    val outerPaddingDp: Float = 0f,
    val outerBgColorHex: Long = 0x00000000L,
    val outerCardShadow: Boolean = false,
    val description: String = ""
)

/**
 * Line Styles for Grid Division
 */
enum class LineStyle(val label: String, val description: String) {
    SOLID("经典实线", "平整干净无缝切线"),
    POLAROID("拍立得留白", "加宽纯白边框，复古拍立得感"),
    DASHED("极简虚线", "间断点阵虚线，轻奢设计感"),
    DOTTED("复古圆点线", "可爱波点间断切割"),
    FILM_SPROCKET("胶卷齿孔", "胶片齿轮边缘质感"),
    NEON_GLOW("流光霓虹", "发光外晕电光线条")
}

/**
 * 3D Tile Drop Shadow Styles
 */
enum class TileShadowStyle(val label: String, val elevationDp: Float, val shadowAlpha: Float) {
    NONE("平面贴合", 0f, 0f),
    SOFT_SHADOW("立体微阴影", 4f, 0.25f),
    ELEVATED_CARD("3D悬浮实体卡", 10f, 0.45f),
    POLAROID_FLOAT("相纸浮起", 8f, 0.35f)
}

/**
 * Magazine Typography Templates
 */
enum class MagazineTemplate(
    val templateName: String,
    val defaultMain: String,
    val defaultSub: String,
    val defaultTag: String
) {
    FLORA_BOTANIC("花艺植物志", "FLORA & MEMORY", "BOTANICAL COLLECTION · VOL.04", "SPRING ATELIER"),
    AUTUMN_BLOOM("秋日盛放", "AUTUMN BLOOM", "WARM LIGHT & FLOWERS", "SEASONAL ESSAY"),
    MINIMAL_35MM("35mm 胶片志", "MINIMALIST 35MM", "ANALOG PHOTOGRAPHY ISSUE", "OCTOBER 2026"),
    PARIS_VOGUE("轻奢封面", "VOGUE & POETRY", "HAUTE COUTURE EDITORIAL", "PARIS · MILAN"),
    VINTAGE_JOURNAL("复古手记", "DAILY ESSENCE", "MEMORIES WORTH KEEPING", "JOURNAL NO.12"),
    CUSTOM("自定义排印", "PHOTO ESSAY", "VISUAL DIARY 2026", "STUDIO")
}

enum class MagazinePosition(val label: String) {
    TOP_CENTER("顶部居中"),
    BOTTOM_RIGHT("右下角标"),
    BOTTOM_CENTER("底部居中"),
    BOTTOM_LEFT("左下优雅")
}

data class MagazineTypographyConfig(
    val isEnabled: Boolean = false,
    val template: MagazineTemplate = MagazineTemplate.FLORA_BOTANIC,
    val mainTitle: String = "FLORA & MEMORY",
    val subTitle: String = "BOTANICAL COLLECTION · VOL.04",
    val dateText: String = "2026.10",
    val locationText: String = "STUDIO ATELIER",
    val position: MagazinePosition = MagazinePosition.BOTTOM_RIGHT,
    val textColorHex: Long = 0xFFFFFFFF,
    val opacity: Float = 0.9f
)

/**
 * Poster Background & Framing
 */
enum class PosterBgStyle(val label: String) {
    SOLID("纯色留白底板"),
    GAUSSIAN_BLUR("原图高斯模糊"),
    TEXTURE_PAPER("艺术纹理画板")
}

enum class PosterTexture(val label: String) {
    NONE("平滑"),
    LINEN_CANVAS("亚麻画布"),
    NEWSPAPER_GRAIN("复古颗粒"),
    MATTE_PLASTER("磨砂石膏")
}

data class PosterBackgroundConfig(
    val style: PosterBgStyle = PosterBgStyle.SOLID,
    val colorHex: Long = 0xFF0B1120, // dark studio default
    val paddingDp: Float = 16f,
    val texture: PosterTexture = PosterTexture.NONE
)

/**
 * Retro Film Timestamp / EXIF Stamp configuration
 */
data class FilmTimestampConfig(
    val isEnabled: Boolean = false,
    val dateText: String = "2026.10.01",
    val timeText: String = "15:59",
    val cameraModel: String = "LEICA M11 // 35mm",
    val colorHex: Long = 0xFFF59E0B, // Classic Warm Amber / Golden Orange LCD
    val onlyOnLastPiece: Boolean = true,
    val textSizeDp: Float = 12f
)

/**
 * Saved User Style Preset / Template
 */
data class StylePresetItem(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val description: String = "",
    val layoutType: LayoutType = LayoutType.GRID_3x3,
    val customRows: Int = 3,
    val customCols: Int = 3,
    val gapFraction: Float = 0.02f,
    val cornerRadiusDp: Float = 4f,
    val lineColorHex: Long = 0xFFFFFFFF,
    val lineWidthDp: Float = 3.0f,
    val lineStyle: LineStyle = LineStyle.SOLID,
    val shadowStyle: TileShadowStyle = TileShadowStyle.NONE,
    val outerCornerRadiusDp: Float = 0f,
    val outerPaddingDp: Float = 0f,
    val outerBgColorHex: Long = 0x00000000L,
    val outerCardShadow: Boolean = false,
    val filter: ImageFilterPreset = ImageFilterPreset.ORIGINAL,
    val isCenterBlank: Boolean = false,
    val magazineConfig: MagazineTypographyConfig = MagazineTypographyConfig(),
    val filmTimestampConfig: FilmTimestampConfig = FilmTimestampConfig(),
    val isOfficial: Boolean = false
)

/**
 * Social Feed Live Simulator Platforms
 */
enum class SocialSimulatorPlatform(val platformName: String, val subtitle: String) {
    WECHAT_MOMENTS("微信朋友圈实景", "模拟朋友圈 4格/9格真实缩略图裁切与间距"),
    RED_CAROUSEL("小红书左右滑动画廊", "模拟小红书 3:4 多图卡片无缝左右滑动画卷"),
    WEIBO_FEED("微博动态瀑布流", "模拟微博九宫格动态展示"),
    INSTAGRAM_GRID("Instagram 极简照片墙", "模拟 Instagram 1:1 纯净网格展示")
}

