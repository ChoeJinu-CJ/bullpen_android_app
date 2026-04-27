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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mlbpark.bullpen.data.PostSummary
import com.mlbpark.bullpen.ui.CategoryChip
import com.mlbpark.bullpen.ui.ListViewModel
import com.mlbpark.bullpen.ui.UiState
import com.mlbpark.bullpen.ui.theme.Accent
import com.mlbpark.bullpen.ui.theme.StadiumBg
import com.mlbpark.bullpen.ui.theme.StadiumDivider
import com.mlbpark.bullpen.ui.theme.StadiumDividerStrong
import com.mlbpark.bullpen.ui.theme.StadiumDividerSubtle
import com.mlbpark.bullpen.ui.theme.StadiumHeader
import com.mlbpark.bullpen.ui.theme.TextPrimary
import com.mlbpark.bullpen.ui.theme.TextSecondary

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListScreen(
    viewModel: ListViewModel,
    onItemClick: (PostSummary) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val pullState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumBg),
    ) {
        ListHeader()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullState),
        ) {
            when (val s = state) {
                UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorView(message = s.message, onRetry = { viewModel.retry() })
                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        items(s.data, key = { it.id }) { post ->
                            PostRow(post = post, onClick = { onItemClick(post) })
                            HorizontalDivider(color = StadiumDivider, thickness = 1.dp)
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = StadiumHeader,
                contentColor = Accent,
            )
        }
    }
}

@Composable
private fun ListHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StadiumHeader)
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MLBPARK",
                    color = Accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Bullpen",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            // 시안 A의 우측 상단 원형 인디케이터 (장식)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(StadiumDividerStrong),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(2.dp)
                        .background(Accent),
                )
            }
        }
    }
    HorizontalDivider(color = StadiumDividerStrong, thickness = 1.dp)
}

@Composable
private fun PostRow(post: PostSummary, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        if (!post.category.isNullOrBlank()) {
            CategoryChip(category = post.category)
            Spacer(Modifier.height(6.dp))
        }
        Text(
            text = post.title,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp,
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(post.author, color = TextSecondary, fontSize = 11.sp)
            Spacer(Modifier.width(8.dp))
            Text("·", color = StadiumDividerSubtle, fontSize = 11.sp)
            Spacer(Modifier.width(8.dp))
            Text(post.time, color = TextSecondary, fontSize = 11.sp)
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
            Text(
                text = message,
                color = TextSecondary,
                fontSize = 11.sp,
            )
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
