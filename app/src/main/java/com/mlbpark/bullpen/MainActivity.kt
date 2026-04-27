package com.mlbpark.bullpen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mlbpark.bullpen.data.BullpenRepository
import com.mlbpark.bullpen.ui.DetailViewModel
import com.mlbpark.bullpen.ui.ListViewModel
import com.mlbpark.bullpen.ui.screen.DetailScreen
import com.mlbpark.bullpen.ui.screen.ListScreen
import com.mlbpark.bullpen.ui.theme.MlbparkBullpenTheme
import com.mlbpark.bullpen.ui.theme.StadiumBg
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {

    // 앱 전역에서 공유하는 단일 Repository (DI 라이브러리 없이 가볍게).
    private val repository by lazy { BullpenRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MlbparkBullpenTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(
                            WindowInsets.systemBars.only(WindowInsetsSides.Vertical),
                        ),
                    color = StadiumBg,
                ) {
                    AppNavHost(repository = repository)
                }
            }
        }
    }
}

@Composable
private fun AppNavHost(repository: BullpenRepository) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "list",
    ) {
        composable("list") {
            val vm: ListViewModel = viewModel(factory = remember { ListViewModel.Factory(repository) })
            ListScreen(
                viewModel = vm,
                onItemClick = { post ->
                    val encoded = URLEncoder.encode(post.detailUrl, Charsets.UTF_8.name())
                    navController.navigate("detail/$encoded")
                },
            )
        }
        composable(
            route = "detail/{url}",
            arguments = listOf(navArgument("url") { type = NavType.StringType }),
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("url").orEmpty()
            val detailUrl = URLDecoder.decode(encoded, Charsets.UTF_8.name())
            val vm: DetailViewModel = viewModel(
                factory = remember(detailUrl) {
                    DetailViewModel.Factory(repository, detailUrl)
                },
            )
            DetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
