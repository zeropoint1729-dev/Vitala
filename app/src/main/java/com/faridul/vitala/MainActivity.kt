package com.faridul.vitala

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.faridul.vitala.ui.home.HomeScreen
import com.faridul.vitala.ui.library.DiseaseDetailScreen
import com.faridul.vitala.ui.library.DiseaseListScreen
import com.faridul.vitala.ui.news.ArticleDetailScreen
import com.faridul.vitala.ui.news.NewsListScreen
import com.faridul.vitala.ui.theme.VitalaTheme
import com.faridul.vitala.ui.tips.TipsHistoryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VitalaTheme {
                VitalaNavHost()
            }
        }
    }
}

private data class BottomDestination(val route: String, val label: String, val icon: ImageVector)

private val bottomDestinations = listOf(
    BottomDestination("home", "Home", Icons.Filled.Home),
    BottomDestination("news", "News", Icons.Filled.Article),
    BottomDestination("library", "Library", Icons.Filled.MenuBook),
    BottomDestination("tips", "Tips", Icons.Filled.Lightbulb)
)

@Composable
fun VitalaNavHost() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route

            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                bottomDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(navController) }
            composable("news") { NewsListScreen(navController) }
            composable(
                route = "news/{articleId}",
                arguments = listOf(navArgument("articleId") { type = NavType.StringType })
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getString("articleId")
                if (articleId != null) {
                    ArticleDetailScreen(navController, articleId)
                }
            }
            composable("library") { DiseaseListScreen(navController) }
            composable(
                route = "library/{diseaseId}",
                arguments = listOf(navArgument("diseaseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val diseaseId = backStackEntry.arguments?.getString("diseaseId")
                if (diseaseId != null) {
                    DiseaseDetailScreen(navController, diseaseId)
                }
            }
            composable("tips") { TipsHistoryScreen(navController) }
        }
    }
}
