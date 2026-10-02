package com.example.model

import android.graphics.ColorMatrix

/**
 * Filter Categories for intuitive organization
 */
enum class FilterCategory(val label: String, val iconEmoji: String) {
    ALL("全部", "✨"),
    FUJI("富士胶片", "📷"),
    CANON("佳能人像", "🌸"),
    CINEMATIC("电影大师", "🎬"),
    FRESH("小清新", "🌿"),
    FLORAL("浪漫花束", "💐"),
    BRIDAL("梦幻新娘", "👰"),
    MAGAZINE("杂志硬照", "📰"),
    CLASSIC("经典基础", "🎨")
}

/**
 * Filter Presets - Comprehensive Top-tier Photography Color Science
 */
enum class ImageFilterPreset(
    val label: String,
    val description: String,
    val category: FilterCategory = FilterCategory.CLASSIC,
    val colorTagHex: Long = 0xFF6366F1
) {
    ORIGINAL("原图", "保持原本画面色彩", FilterCategory.CLASSIC, 0xFF94A3B8),

    // === 富士胶片系列 (FUJIFILM Color Science) ===
    FUJI_NC("富士 NC负片", "经典负片青红反差，复古胶片颗粒感", FilterCategory.FUJI, 0xFF0D9488),
    FUJI_CC("富士 CC胶片", "Classic Chrome 经典正片，低饱和纪实电影感", FilterCategory.FUJI, 0xFF0284C7),
    FUJI_VELVIA("富士 Velvia鲜艳", "反转片超高反差与浓郁风光色彩", FilterCategory.FUJI, 0xFF059669),
    FUJI_PROVIA("富士 Provia自然", "清透原色纪实还原，纯净自然光影", FilterCategory.FUJI, 0xFF14B8A6),

    // === 佳能人像系列 (Canon Portrait Warmth) ===
    CANON_PORTRAIT("佳能 人像白皙", "通透红润白皙肤色，柔和高光过渡", FilterCategory.CANON, 0xFFEC4899),
    CANON_WARM("佳能 暖阳微风", "暖金色柔焦微风，治愈下午茶人像", FilterCategory.CANON, 0xFFF59E0B),

    // === 电影大师系列 (Cinematic Masters & Kodak) ===
    CINEMATIC_TEAL_ORANGE("电影 青橙大片", "好莱坞青蓝暗部与暖橙高光强烈冲撞", FilterCategory.CINEMATIC, 0xFF0EA5E9),
    CINEMATIC_VISION3("柯达 500T胶卷", "Kodak Vision3 经典电影工业胶片质感", FilterCategory.CINEMATIC, 0xFFD97706),
    CINEMATIC_NOSTALGIA("王家卫 复古绿金", "浓郁油画绿金复古电影暗涌质感", FilterCategory.CINEMATIC, 0xFF65A30D),

    // === 小清新系列 (Fresh, Soft & Airy) ===
    FRESH_AIRY("日系 空气感", "高调通透微过曝，温柔轻盈日系小清新", FilterCategory.FRESH, 0xFF38BDF8),
    FRESH_SUMMER("浅葱 初夏微风", "浅绿青蓝与通透阳光，夏日柠檬汽水感", FilterCategory.FRESH, 0xFF10B981),
    JAPANESE("日系 轻柔", "通透高调温柔清新", FilterCategory.FRESH, 0xFF818CF8),

    // === 浪漫花束系列 (Romantic Floral & Botanical) ===
    FLORAL_MONET("浪漫 莫奈花园", "柔粉与淡紫高光，梦幻花艺与油画质感", FilterCategory.FLORAL, 0xFFA855F7),
    FLORAL_EMERALD("绿野 仙踪植被", "纯净翠绿与森林深浅层次，高级植物生机", FilterCategory.FLORAL, 0xFF16A34A),

    // === 梦幻新娘系列 (Bridal & Wedding Glow) ===
    BRIDAL_PURE_WHITE("梦幻 纯白婚纱", "婚纱高光通透纯净，柔焦漫射神圣光晕", FilterCategory.BRIDAL, 0xFFE2E8F0),
    BRIDAL_FRENCH("法式 复古婚礼", "香槟金与奶油白典雅浪漫，法式庄园仪式", FilterCategory.BRIDAL, 0xFFFDE68A),

    // === 杂志硬照系列 (Editorial & Vogue High Fashion) ===
    MAGAZINE_VOGUE("杂志 Vogue冷淡", "高冷低饱和硬朗质感，时尚大片高级灰调", FilterCategory.MAGAZINE, 0xFF64748B),
    MAGAZINE_BAZAAR("芭莎 摩登暖咖", "摩登暖咖复古棕调，时尚杂志封面质感", FilterCategory.MAGAZINE, 0xFF78350F),

    // === 经典基础系列 ===
    FILM("复古胶片", "胶片暖调与柔和质感", FilterCategory.CLASSIC, 0xFFCA8A04),
    WARM("暖阳金辉", "温暖阳光与惬意午后", FilterCategory.CLASSIC, 0xFFEA580C),
    BW("质感黑白", "高反差黑白影调", FilterCategory.CLASSIC, 0xFF334155),
    VIBRANT("鲜亮明艳", "高饱和度生动色彩", FilterCategory.CLASSIC, 0xFFDC2626),
    CYBERPUNK("赛博霓虹", "冷暖冲撞潮流夜景", FilterCategory.CLASSIC, 0xFF9333EA),
    MOODY("冷萃森系", "幽静青蓝冷冽质感", FilterCategory.CLASSIC, 0xFF0F766E);

    fun getColorMatrix(): ColorMatrix {
        val cm = ColorMatrix()
        when (this) {
            ORIGINAL -> cm.reset()

            // 1. 富士 NC 经典负片 (Fujifilm Classic Neg)
            FUJI_NC -> {
                cm.set(
                    floatArrayOf(
                        1.08f, -0.02f, 0.04f, 0f, 6f,
                        -0.03f, 1.05f, 0.02f, 0f, 4f,
                        0.02f, 0.04f, 0.92f, 0f, -8f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                // Contrast boost + gentle saturation curve
                val contrast = 1.15f
                val translate = (-0.5f * contrast + 0.5f) * 255f
                val contrastMatrix = ColorMatrix(
                    floatArrayOf(
                        contrast, 0f, 0f, 0f, translate,
                        0f, contrast, 0f, 0f, translate,
                        0f, 0f, contrast, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(contrastMatrix)
            }

            // 2. 富士 CC 经典正片 (Classic Chrome)
            FUJI_CC -> {
                cm.set(
                    floatArrayOf(
                        0.98f, 0.04f, 0.02f, 0f, 4f,
                        0.02f, 0.96f, 0.04f, 0f, 2f,
                        0.02f, 0.06f, 0.90f, 0f, -6f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val sat = ColorMatrix().apply { setSaturation(0.82f) }
                cm.postConcat(sat)
            }

            // 3. 富士 Velvia 鲜艳反转片
            FUJI_VELVIA -> {
                cm.set(
                    floatArrayOf(
                        1.18f, 0f, 0.05f, 0f, -6f,
                        0f, 1.14f, 0f, 0f, -4f,
                        0.04f, 0f, 1.22f, 0f, 2f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val sat = ColorMatrix().apply { setSaturation(1.38f) }
                cm.postConcat(sat)
            }

            // 4. 富士 Provia 自然还原
            FUJI_PROVIA -> {
                cm.set(
                    floatArrayOf(
                        1.04f, 0f, 0.01f, 0f, 2f,
                        0f, 1.03f, 0.01f, 0f, 2f,
                        0.01f, 0f, 1.05f, 0f, 4f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val sat = ColorMatrix().apply { setSaturation(1.08f) }
                cm.postConcat(sat)
            }

            // 5. 佳能 人像白皙 (Canon Rosy Portrait)
            CANON_PORTRAIT -> {
                // Lifted highlights, rosy skin tones, gentle softness
                cm.set(
                    floatArrayOf(
                        1.06f, 0.02f, 0.01f, 0f, 18f,
                        0.01f, 1.02f, 0.02f, 0f, 16f,
                        0f, 0.02f, 0.98f, 0f, 12f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val sat = ColorMatrix().apply { setSaturation(1.05f) }
                cm.postConcat(sat)
            }

            // 6. 佳能 暖阳微风 (Canon Warm Golden Glow)
            CANON_WARM -> {
                cm.set(
                    floatArrayOf(
                        1.12f, 0.04f, 0f, 0f, 16f,
                        0.02f, 1.06f, 0f, 0f, 10f,
                        0f, 0.02f, 0.90f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val sat = ColorMatrix().apply { setSaturation(1.10f) }
                cm.postConcat(sat)
            }

            // 7. 电影 青橙大片 (Teal & Orange Hollywood Cinema)
            CINEMATIC_TEAL_ORANGE -> {
                cm.set(
                    floatArrayOf(
                        1.22f, -0.04f, -0.06f, 0f, 10f,
                        -0.02f, 1.02f, 0.04f, 0f, -4f,
                        -0.08f, 0.12f, 1.28f, 0f, -8f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val contrast = 1.20f
                val translate = (-0.5f * contrast + 0.5f) * 255f
                val contrastMatrix = ColorMatrix(
                    floatArrayOf(
                        contrast, 0f, 0f, 0f, translate,
                        0f, contrast, 0f, 0f, translate,
                        0f, 0f, contrast, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(contrastMatrix)
            }

            // 8. 柯达 500T 电影胶卷 (Kodak Vision3 500T)
            CINEMATIC_VISION3 -> {
                cm.set(
                    floatArrayOf(
                        1.08f, 0.04f, -0.02f, 0f, 14f,
                        0.02f, 1.02f, 0.03f, 0f, 8f,
                        -0.04f, 0.08f, 1.10f, 0f, 4f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val sat = ColorMatrix().apply { setSaturation(0.95f) }
                cm.postConcat(sat)
            }

            // 9. 王家卫 复古绿金 (Wong Kar-wai Nostalgia)
            CINEMATIC_NOSTALGIA -> {
                cm.set(
                    floatArrayOf(
                        1.06f, 0.08f, -0.04f, 0f, 12f,
                        0.04f, 1.12f, -0.02f, 0f, 10f,
                        -0.06f, 0.06f, 0.82f, 0f, -14f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val contrast = 1.18f
                val translate = (-0.5f * contrast + 0.5f) * 255f
                val contrastMatrix = ColorMatrix(
                    floatArrayOf(
                        contrast, 0f, 0f, 0f, translate,
                        0f, contrast, 0f, 0f, translate,
                        0f, 0f, contrast, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(contrastMatrix)
            }

            // 10. 日系 空气感 (Fresh & Airy Pastel)
            FRESH_AIRY -> {
                cm.set(
                    floatArrayOf(
                        1.04f, 0f, 0.02f, 0f, 26f,
                        0f, 1.04f, 0.02f, 0f, 26f,
                        0.01f, 0.02f, 1.10f, 0f, 30f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val sat = ColorMatrix().apply { setSaturation(0.88f) }
                cm.postConcat(sat)
            }

            // 11. 浅葱 初夏微风 (Fresh Summer Breeze)
            FRESH_SUMMER -> {
                cm.set(
                    floatArrayOf(
                        0.98f, 0.02f, 0.02f, 0f, 16f,
                        0.02f, 1.08f, 0.04f, 0f, 20f,
                        0.02f, 0.04f, 1.14f, 0f, 22f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val sat = ColorMatrix().apply { setSaturation(1.12f) }
                cm.postConcat(sat)
            }

            // 12. 浪漫 莫奈花园 (Romantic Bouquet / Monet Floral)
            FLORAL_MONET -> {
                cm.set(
                    floatArrayOf(
                        1.14f, 0.02f, 0.06f, 0f, 14f,
                        0.02f, 1.02f, 0.04f, 0f, 6f,
                        0.08f, 0.02f, 1.18f, 0f, 16f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val sat = ColorMatrix().apply { setSaturation(1.22f) }
                cm.postConcat(sat)
            }

            // 13. 绿野 仙踪植被 (Emerald Botanical)
            FLORAL_EMERALD -> {
                cm.set(
                    floatArrayOf(
                        0.96f, 0.04f, 0f, 0f, 2f,
                        0.04f, 1.18f, 0.02f, 0f, 12f,
                        0f, 0.06f, 1.05f, 0f, 6f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val sat = ColorMatrix().apply { setSaturation(1.25f) }
                cm.postConcat(sat)
            }

            // 14. 梦幻 纯白婚纱 (Bridal Pure White)
            BRIDAL_PURE_WHITE -> {
                cm.set(
                    floatArrayOf(
                        1.05f, 0.01f, 0.01f, 0f, 28f,
                        0.01f, 1.04f, 0.01f, 0f, 28f,
                        0.01f, 0.01f, 1.06f, 0f, 32f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val sat = ColorMatrix().apply { setSaturation(0.92f) }
                cm.postConcat(sat)
            }

            // 15. 法式 复古婚礼 (French Wedding Warmth)
            BRIDAL_FRENCH -> {
                cm.set(
                    floatArrayOf(
                        1.10f, 0.03f, 0f, 0f, 18f,
                        0.02f, 1.05f, 0.01f, 0f, 14f,
                        0f, 0.02f, 0.94f, 0f, 6f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val sat = ColorMatrix().apply { setSaturation(1.02f) }
                cm.postConcat(sat)
            }

            // 16. 杂志 Vogue 冷淡 (Magazine Vogue Minimalist)
            MAGAZINE_VOGUE -> {
                cm.set(
                    floatArrayOf(
                        0.98f, 0.02f, 0.02f, 0f, 2f,
                        0.02f, 0.98f, 0.02f, 0f, 2f,
                        0.02f, 0.02f, 1.02f, 0f, 6f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val sat = ColorMatrix().apply { setSaturation(0.72f) }
                cm.postConcat(sat)
                val contrast = 1.24f
                val translate = (-0.5f * contrast + 0.5f) * 255f
                val contrastMatrix = ColorMatrix(
                    floatArrayOf(
                        contrast, 0f, 0f, 0f, translate,
                        0f, contrast, 0f, 0f, translate,
                        0f, 0f, contrast, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(contrastMatrix)
            }

            // 17. 芭莎 摩登暖咖 (Bazaar Warm Latte)
            MAGAZINE_BAZAAR -> {
                cm.set(
                    floatArrayOf(
                        1.08f, 0.04f, -0.02f, 0f, 14f,
                        0.02f, 1.02f, -0.02f, 0f, 8f,
                        -0.02f, 0.02f, 0.88f, 0f, -12f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val sat = ColorMatrix().apply { setSaturation(0.92f) }
                cm.postConcat(sat)
            }

            // 18. 经典 基础
            FILM -> {
                cm.set(
                    floatArrayOf(
                        1.05f, 0.05f, 0.0f, 0f, 15f,
                        0.0f, 1.0f, 0.05f, 0f, 10f,
                        0.0f, 0.05f, 0.9f, 0f, -5f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            WARM -> {
                cm.set(
                    floatArrayOf(
                        1.15f, 0f, 0f, 0f, 20f,
                        0f, 1.05f, 0f, 0f, 10f,
                        0f, 0f, 0.88f, 0f, -15f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            BW -> {
                cm.setSaturation(0f)
                val contrast = 1.25f
                val translate = (-0.5f * contrast + 0.5f) * 255f
                val contrastMatrix = ColorMatrix(
                    floatArrayOf(
                        contrast, 0f, 0f, 0f, translate,
                        0f, contrast, 0f, 0f, translate,
                        0f, 0f, contrast, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(contrastMatrix)
            }
            VIBRANT -> {
                cm.setSaturation(1.45f)
                val contrastMatrix = ColorMatrix(
                    floatArrayOf(
                        1.08f, 0f, 0f, 0f, -5f,
                        0f, 1.08f, 0f, 0f, -5f,
                        0f, 0f, 1.08f, 0f, -5f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(contrastMatrix)
            }
            CYBERPUNK -> {
                cm.set(
                    floatArrayOf(
                        1.2f, 0f, 0.2f, 0f, 10f,
                        0f, 0.95f, 0.1f, 0f, 0f,
                        0.1f, 0f, 1.35f, 0f, 25f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            JAPANESE -> {
                cm.set(
                    floatArrayOf(
                        1.02f, 0f, 0f, 0f, 22f,
                        0f, 1.02f, 0f, 0f, 22f,
                        0f, 0f, 1.08f, 0f, 26f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val sat = ColorMatrix().apply { setSaturation(0.9f) }
                cm.postConcat(sat)
            }
            MOODY -> {
                cm.set(
                    floatArrayOf(
                        0.88f, 0.05f, 0.05f, 0f, -8f,
                        0.05f, 0.98f, 0.05f, 0f, 5f,
                        0.05f, 0.1f, 1.15f, 0f, 15f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val sat = ColorMatrix().apply { setSaturation(0.85f) }
                cm.postConcat(sat)
            }
        }
        return cm
    }
}

/**
 * Export Format Options
 */
enum class ExportFormat(val extension: String, val mimeType: String, val label: String) {
    PNG(".png", "image/png", "PNG (无损高清)"),
    JPG(".jpg", "image/jpeg", "JPG (高效节省空间)")
}

/**
 * Watermark Configuration Models
 */
enum class WatermarkType(val label: String) {
    NONE("无水印"),
    TEXT("文字水印"),
    IMAGE("图片印章/Logo")
}

enum class WatermarkPosition(val label: String) {
    BOTTOM_RIGHT("右下角"),
    BOTTOM_LEFT("左下角"),
    TOP_RIGHT("右上角"),
    TOP_LEFT("左上角"),
    CENTER("正中央"),
    TILED("平铺斜纹防盗")
}

enum class WatermarkScope(val label: String, val description: String) {
    ALL_PIECES("所有切片", "每个切片独立打水印，防止单张被盗用"),
    LAST_PIECE_ONLY("仅最后一张", "朋友圈整屏排版时仅右下角签名印章"),
    CENTER_PIECE_ONLY("仅中心切片", "九宫格中心C位签名")
}

data class WatermarkConfig(
    val enabled: Boolean = false,
    val type: WatermarkType = WatermarkType.TEXT,
    val text: String = "© 原创切图",
    val imageBitmap: android.graphics.Bitmap? = null,
    val position: WatermarkPosition = WatermarkPosition.BOTTOM_RIGHT,
    val scope: WatermarkScope = WatermarkScope.ALL_PIECES,
    val opacity: Float = 0.75f, // 0.2 .. 1.0
    val textSizeSp: Float = 16f,
    val textColorHex: Long = 0xFFFFFFFF
)

/**
 * Export Configuration Model
 */
data class ExportConfig(
    val format: ExportFormat = ExportFormat.PNG,
    val scaleFactor: Float = 1.0f, // 1.0: 100%, 1.5: 150%, 0.75: 75%, 0.5: 50%
    val quality: Int = 92, // 60..100 (for JPG)
    val watermark: WatermarkConfig = WatermarkConfig()
)

/**
 * Single Recommendation Item
 */
data class RecommendedLayout(
    val layoutType: LayoutType,
    val title: String,
    val reason: String,
    val score: Int // 0..100
)

/**
 * Image Content Analysis Category
 */
enum class ImageContentCategory(val title: String, val iconName: String) {
    PANORAMIC_LANDSCAPE("全景风光 / 宽幅摄影", "landscape"),
    TALL_PORTRAIT("垂直人像 / 长图展示", "portrait"),
    SQUARE_LIFESTYLE("社交生活 / 静物特写", "square"),
    FEATURE_SUBJECT("主体突出 / 层次画报", "subject"),
    BALANCED_ART("艺术作品 / 丰富构图", "art")
}

/**
 * Result of Smart Layout Analysis
 */
data class ImageAnalysisResult(
    val category: ImageContentCategory,
    val summary: String,
    val details: String,
    val aspectRatio: Float,
    val recommendations: List<RecommendedLayout>
)
