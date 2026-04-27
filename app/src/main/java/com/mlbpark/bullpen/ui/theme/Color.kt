package com.mlbpark.bullpen.ui.theme

import androidx.compose.ui.graphics.Color

// ── 시안 A: Stadium / Dark ──────────────────────────────────────────────
// 배경
val StadiumBg = Color(0xFF0F1A14)
val StadiumHeader = Color(0xFF0A130F)
val StadiumDivider = Color(0xFF1A2620)
val StadiumDividerStrong = Color(0xFF1F3328)
val StadiumDividerSubtle = Color(0xFF2F4538)

// 텍스트
val TextPrimary = Color(0xFFF1EFE8)
val TextSecondary = Color(0xFF6B8278)
val TextBody = Color(0xFFC7D4CB)
val TextAccent = Color(0xFF9FE1CB)

// 카테고리 칩 — 시안 그대로
val ChipMlbBg = Color(0xFF14241C)
val ChipMlbFg = Color(0xFF5DCAA5)

val ChipKboBg = Color(0xFF2A1810)
val ChipKboFg = Color(0xFFF0997B)

val ChipTalkBg = Color(0xFF0F1F2E)
val ChipTalkFg = Color(0xFFB5D4F4)

val ChipDefaultBg = Color(0xFF1F2A24)
val ChipDefaultFg = Color(0xFFB4B2A9)

val Accent = Color(0xFF5DCAA5)

/**
 * 카테고리 텍스트로 칩 색상을 결정.
 * - MLB 계열, KBO 계열, 잡담 계열, 그 외 default.
 */
data class ChipColors(val bg: Color, val fg: Color)

fun chipColorsFor(category: String?): ChipColors {
    val c = category?.trim().orEmpty()
    return when {
        c.isEmpty() -> ChipColors(ChipDefaultBg, ChipDefaultFg)
        c.contains("MLB", ignoreCase = true) -> ChipColors(ChipMlbBg, ChipMlbFg)
        c.contains("KBO", ignoreCase = true) -> ChipColors(ChipKboBg, ChipKboFg)
        c.contains("잡담") || c.contains("자유") -> ChipColors(ChipTalkBg, ChipTalkFg)
        else -> ChipColors(ChipDefaultBg, ChipDefaultFg)
    }
}
