package com.mlbpark.bullpen.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.mlbpark.bullpen.data.BodyBlock
import com.mlbpark.bullpen.data.PostDetail
import com.mlbpark.bullpen.ui.CategoryChip
import com.mlbpark.bullpen.ui.DetailViewModel
import com.mlbpark.bullpen.ui.UiState
import com.mlbpark.bullpen.ui.theme.Accent
import com.mlbpark.bullpen.ui.theme.StadiumBg
import com.mlbpark.bullpen.ui.theme.StadiumDivider
import com.mlbpark.bullpen.ui.theme.StadiumDividerStrong
import com.mlbpark.bullpen.ui.theme.StadiumDividerSubtle
import com.mlbpark.bullpen.ui.theme.StadiumHeader
import com.mlbpark.bullpen.ui.theme.TextAccent
import com.mlbpark.bullpen.ui.theme.TextBody
import com.mlbpark.bullpen.ui.theme.TextPrimary
import com.mlbpark.bullpen.ui.theme.TextSecondary

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumBg),
    ) {
        DetailHeader(onBack = onBack)
        Box(modifier = Modifier.fillMaxSize()) {
            when (val s = state) {
                UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorView(message = s.message, onRetry = { viewModel.retry() })
                is UiState.Success -> DetailContent(detail = s.data)
            }
        }
    }
}

@Composable
private fun DetailHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StadiumHeader)
            .clickable { onBack() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "뒤로",
            tint = Accent,
        )
        Spacer(Modifier.width(12.dp))
        Text("목록으로", color = TextSecondary, fontSize = 13.sp)
    }
    HorizontalDivider(color = StadiumDividerStrong, thickness = 1.dp)
}

@Composable
private fun DetailContent(detail: PostDetail) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // 헤더 영역: 카테고리 → 제목 → 작성자/시간
        item {
            if (!detail.category.isNullOrBlank()) {
                CategoryChip(category = detail.category, modifier = Modifier.padding(vertical = 0.dp))
                Spacer(Modifier.height(12.dp))
            }
            Text(
                text = detail.title,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp,
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = detail.author,
                    color = TextAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.width(8.dp))
                Text("·", color = StadiumDividerSubtle, fontSize = 11.sp)
                Spacer(Modifier.width(8.dp))
                Text(detail.time, color = TextSecondary, fontSize = 11.sp)
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = StadiumDividerStrong, thickness = 1.dp)
            Spacer(Modifier.height(18.dp))
        }

        // 본문 영역
        items(detail.body) { block ->
            when (block) {
                is BodyBlock.Text -> {
                    Text(
                        text = block.text,
                        color = TextBody,
                        fontSize = 13.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                    )
                }
                is BodyBlock.Image -> {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(block.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .padding(vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Accent)
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "불러오지 못했어요",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(6.dp))
            Text(message, color = TextSecondary, fontSize = 11.sp)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    contentColor = StadiumBg,
                ),
            ) {
                Text("다시 시도", fontWeight = FontWeight.Medium)
            }
        }
    }
}
