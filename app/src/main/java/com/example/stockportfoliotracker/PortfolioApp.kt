package com.example.stockportfoliotracker

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stockportfoliotracker.ui.DetailScreen
import com.example.stockportfoliotracker.ui.HomeScreen
import com.example.stockportfoliotracker.ui.SettingsScreen

@Composable
fun PortfolioApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("details") { DetailScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
    }
}