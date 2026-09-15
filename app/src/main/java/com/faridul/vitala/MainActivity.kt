package com.faridul.vitala

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
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
import com.faridul.vitala.ui.onboarding.DisclaimerScreen
import com.faridul.vitala.ui.theme.VitalaTheme
import com.faridul.vitala.ui.tips.TipsHistoryScreen
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val deepLinkRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLinkRoute.value = intent?.getStringExtra(EXTRA_DEEP_LINK_ROUTE)

        setContent {
            VitalaTheme {
                val app = applicationContext as VitalaApplication
                val hasAccepted by app.preferencesManager.hasAcceptedDisclaimer.collectAsState(initial = false)
                val scope = rememberCoroutineScope()

                if (hasAccepted) {
                    VitalaNavHost(pendingDeepLink = deepLinkRoute)
                } else {
                    DisclaimerScreen(
                        onAccept = {
                            scope.launch { app.preferencesManager.setAcceptedDisclaimer() }
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkRoute.value = intent.getStringExtra(EXTRA_DEEP_LINK_ROUTE)
    }

    companion object {
        const val EXTRA_DEEP_LINK_ROUTE = "deep_link_route"
    }
}

private data class BottomDestination(val route: String, val labelRes: Int, val icon: ImageVector)

private val bottomDestinations = listOf(
    BottomDestination("home", R.string.nav_home, Icons.Filled.Home),
    BottomDestination("news", R.string.nav_news, Icons.Filled.Article),
    BottomDestination("library", R.string.nav_library, Icons.Filled.MenuBook),
    BottomDestination("tips", R.string.nav_tips, Icons.Filled.Lightbulb)
)

@Composable
fun VitalaNavHost(pendingDeepLink: MutableState<String?> = mutableStateOf(null)) {
    val navController = rememberNavController()

    LaunchedEffect(pendingDeepLink.value) {
        val route = pendingDeepLink.value
        if (route != null) {
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            pendingDeepLink.value = null
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route

            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                bottomDestinations.forEach { destination ->
                    val selected = currentRoute != null && currentRoute.startsWith(destination.route)
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = stringResource(destination.labelRes)) },
                        label = { Text(stringResource(destination.labelRes)) },
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
