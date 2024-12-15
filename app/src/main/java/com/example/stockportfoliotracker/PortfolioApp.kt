package com.example.stockportfoliotracker

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stockportfoliotracker.data.DataStoreManager
import com.example.stockportfoliotracker.ui.*
import com.example.stockportfoliotracker.viewmodel.StockViewModel
import kotlinx.coroutines.launch

@Composable
fun PortfolioApp(context: Context) {
    val dataStoreManager = remember { DataStoreManager(context) }
    val darkModeState = dataStoreManager.isDarkMode.collectAsState(initial = false)

    val navController = rememberNavController()
    val stockViewModel: StockViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    // Toggle dark mode and save to DataStore
    val toggleTheme: () -> Unit = {
        coroutineScope.launch {
            dataStoreManager.setDarkMode(!darkModeState.value)
        }
    }

    val colorScheme = if (darkModeState.value) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme
    ) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    navController = navController,
                    stockViewModel = stockViewModel
                )
            }
            composable("addStock") {
                AddStockScreen(navController, stockViewModel)
            }
            composable("detail/{ticker}") { backStackEntry ->
                val ticker = backStackEntry.arguments?.getString("ticker") ?: ""
                DetailScreen(navController, stockViewModel, ticker)
            }
            composable("settings") {
                SettingsScreen(
                    isDarkTheme = darkModeState.value,
                    onToggleTheme = toggleTheme,
                    navController
                )
            }
        }
    }
}