package com.faridul.vitala

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.faridul.vitala.ui.home.HomeScreen
import com.faridul.vitala.ui.library.DiseaseListScreen
import com.faridul.vitala.ui.news.NewsListScreen
import com.faridul.vitala.ui.theme.VitalaTheme
import com.faridul.vitala.ui.tips.TipsHistoryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VitalaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VitalaNavHost()
                }
            }
        }
    }
}

@Composable
fun VitalaNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("news") { NewsListScreen(navController) }
        composable("library") { DiseaseListScreen(navController) }
        composable("tips") { TipsHistoryScreen(navController) }
    }
}
