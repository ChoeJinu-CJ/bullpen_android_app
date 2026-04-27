package com.mlbpark.bullpen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mlbpark.bullpen.ui.theme.chipColorsFor

/**
 * 시안 A의 카테고리 칩.
 *
 *  - 모서리는 거의 직각에 가까운 3dp (전광판 라벨 느낌).
 *  - 글자 크기는 10sp 라벨, 살짝 트래킹.
 */
@Composable
fun CategoryChip(
    category: String?,
    modifier: Modifier = Modifier,
) {
    val label = category?.takeIf { it.isNotBlank() } ?: return
    val colors = chipColorsFor(label)
    Text(
        text = label,
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(colors.bg)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        color = colors.fg,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
    )
}
