package com.fintrack.app

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class FTTheme(
    val bg: Color, val surface: Color, val surface2: Color, val surface3: Color,
    val text: Color, val text2: Color, val text3: Color, val divider: Color,
    val accent: Color, val accentBg: Color,
    val green: Color, val greenBg: Color,
    val red: Color,   val redBg: Color,
)

val DarkFT = FTTheme(
    bg       = Color(0xFF0A0A0A), surface  = Color(0xFF1A1A1A),
    surface2 = Color(0xFF252525), surface3 = Color(0xFF323232),
    text     = Color(0xFFF5F5F7), text2    = Color(0xFF8E8E93),
    text3    = Color(0xFF48484A), divider  = Color(0x12FFFFFF),
    accent   = Color(0xFF0A84FF), accentBg = Color(0x260A84FF),
    green    = Color(0xFF30D158), greenBg  = Color(0x2630D158),
    red      = Color(0xFFFF453A), redBg    = Color(0x26FF453A),
)

val LightFT = FTTheme(
    bg       = Color(0xFFF2F2F7), surface  = Color(0xFFFFFFFF),
    surface2 = Color(0xFFF2F2F7), surface3 = Color(0xFFE5E5EA),
    text     = Color(0xFF1C1C1E), text2    = Color(0xFF8E8E93),
    text3    = Color(0xFFC7C7CC), divider  = Color(0x12000000),
    accent   = Color(0xFF007AFF), accentBg = Color(0x1F007AFF),
    green    = Color(0xFF34C759), greenBg  = Color(0x1F34C759),
    red      = Color(0xFFFF3B30), redBg    = Color(0x1FFF3B30),
)

val LocalFTTheme = staticCompositionLocalOf { LightFT }
