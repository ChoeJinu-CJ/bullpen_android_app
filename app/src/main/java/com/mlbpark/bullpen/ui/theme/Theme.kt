package com.mlbpark.bullpen.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val MlbparkColorScheme = darkColorScheme(
    background = StadiumBg,
    surface = StadiumBg,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    primary = Accent,
    onPrimary = StadiumBg,
    secondary = TextAccent,
    onSecondary = StadiumBg,
    surfaceVariant = StadiumHeader,
    onSurfaceVariant = TextSecondary,
    outline = StadiumDividerStrong,
    error = Color(0xFFE24B4A),
)

private val MlbparkTypography = Typography(
    titleLarge = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimary,
    ),
    titleMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
        color = TextPrimary,
    ),
    bodyLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 24.sp,
        color = TextBody,
    ),
    bodyMedium = TextStyle(
        fontSize = 13.sp,
        lineHeight = 22.sp,
        color = TextBody,
    ),
    labelSmall = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
    ),
    labelMedium = TextStyle(
        fontSize = 11.sp,
        color = TextSecondary,
    ),
)

@Composable
fun MlbparkBullpenTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // 시안 A 는 항상 다크 테마.
    MaterialTheme(
        colorScheme = MlbparkColorScheme,
        typography = MlbparkTypography,
        content = content,
    )
}
