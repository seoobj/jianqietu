package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class M3ThemePalette(
    val title: String,
    val subtitle: String,
    val primaryColorHex: Long
) {
    AURORA_INDIGO("极光靛蓝", "现代科技感 / 蓝紫渐变", 0xFF2563EB),
    MORANDI_SUNSET("落日晚霞", "温暖治愈 / 珊瑚橙与晨曦金", 0xFFE11D48),
    FUJI_EMERALD("富士胶片绿", "复古文艺 / 森林青与雅白", 0xFF059669),
    OBSIDIAN_GOLD("黑金轻奢", "低调奢华 / 曜黑与香槟金", 0xFFB45309),
    DYNAMIC_SYSTEM("Material You", "跟随系统壁纸智能动态取色", 0xFF6366F1)
}

fun getPaletteColorScheme(palette: M3ThemePalette, isDark: Boolean): ColorScheme {
    return when (palette) {
        M3ThemePalette.AURORA_INDIGO -> if (isDark) {
            darkColorScheme(
                primary = AuroraPrimaryDark,
                onPrimary = AuroraOnPrimaryDark,
                primaryContainer = AuroraPrimaryContainerDark,
                onPrimaryContainer = AuroraOnPrimaryContainerDark,
                secondary = AuroraSecondaryDark,
                onSecondary = AuroraOnSecondaryDark,
                secondaryContainer = AuroraSecondaryContainerDark,
                onSecondaryContainer = AuroraOnSecondaryContainerDark,
                tertiary = AuroraTertiaryDark,
                onTertiary = AuroraOnTertiaryDark,
                tertiaryContainer = AuroraTertiaryContainerDark,
                onTertiaryContainer = AuroraOnTertiaryContainerDark,
                background = NeutralBackgroundDark,
                onBackground = NeutralOnBackgroundDark,
                surface = NeutralSurfaceDark,
                onSurface = NeutralOnSurfaceDark,
                surfaceVariant = NeutralSurfaceVariantDark,
                onSurfaceVariant = NeutralOnSurfaceVariantDark,
                outline = NeutralOutlineDark
            )
        } else {
            lightColorScheme(
                primary = AuroraPrimaryLight,
                onPrimary = AuroraOnPrimaryLight,
                primaryContainer = AuroraPrimaryContainerLight,
                onPrimaryContainer = AuroraOnPrimaryContainerLight,
                secondary = AuroraSecondaryLight,
                onSecondary = AuroraOnSecondaryLight,
                secondaryContainer = AuroraSecondaryContainerLight,
                onSecondaryContainer = AuroraOnSecondaryContainerLight,
                tertiary = AuroraTertiaryLight,
                onTertiary = AuroraOnTertiaryLight,
                tertiaryContainer = AuroraTertiaryContainerLight,
                onTertiaryContainer = AuroraOnTertiaryContainerLight,
                background = NeutralBackgroundLight,
                onBackground = NeutralOnBackgroundLight,
                surface = NeutralSurfaceLight,
                onSurface = NeutralOnSurfaceLight,
                surfaceVariant = NeutralSurfaceVariantLight,
                onSurfaceVariant = NeutralOnSurfaceVariantLight,
                outline = NeutralOutlineLight
            )
        }

        M3ThemePalette.MORANDI_SUNSET -> if (isDark) {
            darkColorScheme(
                primary = SunsetPrimaryDark,
                onPrimary = SunsetOnPrimaryDark,
                primaryContainer = SunsetPrimaryContainerDark,
                onPrimaryContainer = SunsetOnPrimaryContainerDark,
                secondary = SunsetSecondaryDark,
                onSecondary = SunsetOnSecondaryDark,
                secondaryContainer = SunsetSecondaryContainerDark,
                onSecondaryContainer = SunsetOnSecondaryContainerDark,
                tertiary = SunsetTertiaryDark,
                onTertiary = SunsetOnTertiaryDark,
                tertiaryContainer = SunsetTertiaryContainerDark,
                onTertiaryContainer = SunsetOnTertiaryContainerDark,
                background = Color(0xFF14080B),
                onBackground = Color(0xFFFFF1F2),
                surface = Color(0xFF1F0D13),
                onSurface = Color(0xFFFFF1F2),
                surfaceVariant = Color(0xFF331720),
                onSurfaceVariant = Color(0xFFFDA4AF),
                outline = Color(0xFF4C1D2B)
            )
        } else {
            lightColorScheme(
                primary = SunsetPrimaryLight,
                onPrimary = SunsetOnPrimaryLight,
                primaryContainer = SunsetPrimaryContainerLight,
                onPrimaryContainer = SunsetOnPrimaryContainerLight,
                secondary = SunsetSecondaryLight,
                onSecondary = SunsetOnSecondaryLight,
                secondaryContainer = SunsetSecondaryContainerLight,
                onSecondaryContainer = SunsetOnSecondaryContainerLight,
                tertiary = SunsetTertiaryLight,
                onTertiary = SunsetOnTertiaryLight,
                tertiaryContainer = SunsetTertiaryContainerLight,
                onTertiaryContainer = SunsetOnTertiaryContainerLight,
                background = Color(0xFFFFF5F5),
                onBackground = Color(0xFF4C0519),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF4C0519),
                surfaceVariant = Color(0xFFFFE4E6),
                onSurfaceVariant = Color(0xFF9F1239),
                outline = Color(0xFFFECDD3)
            )
        }

        M3ThemePalette.FUJI_EMERALD -> if (isDark) {
            darkColorScheme(
                primary = FujiPrimaryDark,
                onPrimary = FujiOnPrimaryDark,
                primaryContainer = FujiPrimaryContainerDark,
                onPrimaryContainer = FujiOnPrimaryContainerDark,
                secondary = FujiSecondaryDark,
                onSecondary = FujiOnSecondaryDark,
                secondaryContainer = FujiSecondaryContainerDark,
                onSecondaryContainer = FujiOnSecondaryContainerDark,
                tertiary = FujiTertiaryDark,
                onTertiary = FujiOnTertiaryDark,
                tertiaryContainer = FujiTertiaryContainerDark,
                onTertiaryContainer = FujiOnTertiaryContainerDark,
                background = Color(0xFF06140F),
                onBackground = Color(0xFFF0FDF4),
                surface = Color(0xFF0C241B),
                onSurface = Color(0xFFF0FDF4),
                surfaceVariant = Color(0xFF13362A),
                onSurfaceVariant = Color(0xFF86EFAC),
                outline = Color(0xFF1B4E3C)
            )
        } else {
            lightColorScheme(
                primary = FujiPrimaryLight,
                onPrimary = FujiOnPrimaryLight,
                primaryContainer = FujiPrimaryContainerLight,
                onPrimaryContainer = FujiOnPrimaryContainerLight,
                secondary = FujiSecondaryLight,
                onSecondary = FujiOnSecondaryLight,
                secondaryContainer = FujiSecondaryContainerLight,
                onSecondaryContainer = FujiOnSecondaryContainerLight,
                tertiary = FujiTertiaryLight,
                onTertiary = FujiOnTertiaryLight,
                tertiaryContainer = FujiTertiaryContainerLight,
                onTertiaryContainer = FujiOnTertiaryContainerLight,
                background = Color(0xFFF6FBF7),
                onBackground = Color(0xFF022C22),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF022C22),
                surfaceVariant = Color(0xFFDCFCE7),
                onSurfaceVariant = Color(0xFF166534),
                outline = Color(0xFFBBF7D0)
            )
        }

        M3ThemePalette.OBSIDIAN_GOLD -> if (isDark) {
            darkColorScheme(
                primary = ObsidianPrimaryDark,
                onPrimary = ObsidianOnPrimaryDark,
                primaryContainer = ObsidianPrimaryContainerDark,
                onPrimaryContainer = ObsidianOnPrimaryContainerDark,
                secondary = ObsidianSecondaryDark,
                onSecondary = ObsidianOnSecondaryDark,
                secondaryContainer = ObsidianSecondaryContainerDark,
                onSecondaryContainer = ObsidianOnSecondaryContainerDark,
                tertiary = ObsidianTertiaryDark,
                onTertiary = ObsidianOnTertiaryDark,
                tertiaryContainer = ObsidianTertiaryContainerDark,
                onTertiaryContainer = ObsidianOnTertiaryContainerDark,
                background = Color(0xFF0D0B07),
                onBackground = Color(0xFFFEFCE8),
                surface = Color(0xFF1B1710),
                onSurface = Color(0xFFFEFCE8),
                surfaceVariant = Color(0xFF2E271C),
                onSurfaceVariant = Color(0xFFFDE047),
                outline = Color(0xFF453A2A)
            )
        } else {
            lightColorScheme(
                primary = ObsidianPrimaryLight,
                onPrimary = ObsidianOnPrimaryLight,
                primaryContainer = ObsidianPrimaryContainerLight,
                onPrimaryContainer = ObsidianOnPrimaryContainerLight,
                secondary = ObsidianSecondaryLight,
                onSecondary = ObsidianOnSecondaryLight,
                secondaryContainer = ObsidianSecondaryContainerLight,
                onSecondaryContainer = ObsidianOnSecondaryContainerLight,
                tertiary = ObsidianTertiaryLight,
                onTertiary = ObsidianOnTertiaryLight,
                tertiaryContainer = ObsidianTertiaryContainerLight,
                onTertiaryContainer = ObsidianOnTertiaryContainerLight,
                background = Color(0xFFFAF8F5),
                onBackground = Color(0xFF451A03),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF451A03),
                surfaceVariant = Color(0xFFFEF3C7),
                onSurfaceVariant = Color(0xFF854D0E),
                outline = Color(0xFFFDE68A)
            )
        }

        M3ThemePalette.DYNAMIC_SYSTEM -> if (isDark) {
            darkColorScheme(
                primary = AuroraPrimaryDark,
                onPrimary = AuroraOnPrimaryDark,
                primaryContainer = AuroraPrimaryContainerDark,
                onPrimaryContainer = AuroraOnPrimaryContainerDark,
                secondary = AuroraSecondaryDark,
                onSecondary = AuroraOnSecondaryDark,
                secondaryContainer = AuroraSecondaryContainerDark,
                onSecondaryContainer = AuroraOnSecondaryContainerDark,
                tertiary = AuroraTertiaryDark,
                onTertiary = AuroraOnTertiaryDark,
                tertiaryContainer = AuroraTertiaryContainerDark,
                onTertiaryContainer = AuroraOnTertiaryContainerDark,
                background = NeutralBackgroundDark,
                onBackground = NeutralOnBackgroundDark,
                surface = NeutralSurfaceDark,
                onSurface = NeutralOnSurfaceDark,
                surfaceVariant = NeutralSurfaceVariantDark,
                onSurfaceVariant = NeutralOnSurfaceVariantDark,
                outline = NeutralOutlineDark
            )
        } else {
            lightColorScheme(
                primary = AuroraPrimaryLight,
                onPrimary = AuroraOnPrimaryLight,
                primaryContainer = AuroraPrimaryContainerLight,
                onPrimaryContainer = AuroraOnPrimaryContainerLight,
                secondary = AuroraSecondaryLight,
                onSecondary = AuroraOnSecondaryLight,
                secondaryContainer = AuroraSecondaryContainerLight,
                onSecondaryContainer = AuroraOnSecondaryContainerLight,
                tertiary = AuroraTertiaryLight,
                onTertiary = AuroraOnTertiaryLight,
                tertiaryContainer = AuroraTertiaryContainerLight,
                onTertiaryContainer = AuroraOnTertiaryContainerLight,
                background = NeutralBackgroundLight,
                onBackground = NeutralOnBackgroundLight,
                surface = NeutralSurfaceLight,
                onSurface = NeutralOnSurfaceLight,
                surfaceVariant = NeutralSurfaceVariantLight,
                onSurfaceVariant = NeutralOnSurfaceVariantLight,
                outline = NeutralOutlineLight
            )
        }
    }
}

@Composable
fun MyApplicationTheme(
    palette: M3ThemePalette = M3ThemePalette.AURORA_INDIGO,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = if (palette == M3ThemePalette.DYNAMIC_SYSTEM && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        getPaletteColorScheme(palette, darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
