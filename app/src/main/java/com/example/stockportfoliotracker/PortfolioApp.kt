package com.example.stockportfoliotracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stockportfoliotracker.ui.DetailScreen
import com.example.stockportfoliotracker.ui.HomeScreen
import com.example.stockportfoliotracker.ui.SettingsScreen
import com.example.stockportfoliotracker.ui.AddStockScreen
import com.example.stockportfoliotracker.data.StockEntity
import com.example.stockportfoliotracker.viewmodel.StockViewModel

@Composable
fun PortfolioApp() {
    val navController = rememberNavController()
    val stockViewModel: StockViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController, stockViewModel)
        }
        composable("addStock") {
            AddStockScreen(navController, stockViewModel)
        }
        composable("detail/{ticker}") { backStackEntry ->
            val ticker = backStackEntry.arguments?.getString("ticker") ?: ""
            DetailScreen(navController, stockViewModel, ticker)
        }
    }
}
