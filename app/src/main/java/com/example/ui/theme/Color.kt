package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Raw Color Definitions for Dark Theme
val DarkBackground = Color(0xFF0A0E17)
val DarkSurface = Color(0xFF141923)
val DarkSurfaceVariant = Color(0xFF1E2535)
val DarkBorder = Color(0xFF263045)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextTertiary = Color(0xFF64748B)
val DarkCyan = Color(0xFF00E5FF)
val DarkCyanDark = Color(0xFF00B0FF)
val DarkCoral = Color(0xFFFF3B30)
val DarkCardGlow = Color(0x3300E5FF)

// Raw Color Definitions for Light Theme
val LightBackground = Color(0xFFF4F6F9)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE9EFF7)
val LightBorder = Color(0xFFD3DDEB)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextTertiary = Color(0xFF64748B)
val LightCyan = Color(0xFF008BA3)
val LightCyanDark = Color(0xFF006D80)
val LightCoral = Color(0xFFE11D48)
val LightCardGlow = Color(0x22008BA3)

data class ShotblastColorPalette(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val cyan: Color,
    val cyanDark: Color,
    val coral: Color,
    val cardGlow: Color
)

val DarkPalette = ShotblastColorPalette(
    isDark = true,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    border = DarkBorder,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    cyan = DarkCyan,
    cyanDark = DarkCyanDark,
    coral = DarkCoral,
    cardGlow = DarkCardGlow
)

val LightPalette = ShotblastColorPalette(
    isDark = false,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    border = LightBorder,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    cyan = LightCyan,
    cyanDark = LightCyanDark,
    coral = LightCoral,
    cardGlow = LightCardGlow
)

val LocalShotblastColors = staticCompositionLocalOf { DarkPalette }

// Contextual dynamic colors that automatically resolve to the active theme
val ShotblastBackground: Color
    @Composable get() = LocalShotblastColors.current.background

val ShotblastSurface: Color
    @Composable get() = LocalShotblastColors.current.surface

val ShotblastSurfaceVariant: Color
    @Composable get() = LocalShotblastColors.current.surfaceVariant

val ShotblastBorder: Color
    @Composable get() = LocalShotblastColors.current.border

val ShotblastTextPrimary: Color
    @Composable get() = LocalShotblastColors.current.textPrimary

val ShotblastTextSecondary: Color
    @Composable get() = LocalShotblastColors.current.textSecondary

val ShotblastTextTertiary: Color
    @Composable get() = LocalShotblastColors.current.textTertiary

val ShotblastCyan: Color
    @Composable get() = LocalShotblastColors.current.cyan

val ShotblastCyanDark: Color
    @Composable get() = LocalShotblastColors.current.cyanDark

val ShotblastCoral: Color
    @Composable get() = LocalShotblastColors.current.coral

val ShotblastCardGlow: Color
    @Composable get() = LocalShotblastColors.current.cardGlow
