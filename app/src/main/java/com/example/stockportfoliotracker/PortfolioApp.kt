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
import com.example.stockportfoliotracker.viewmodel.TransactionViewModel
import com.example.stockportfoliotracker.views.AddTransactionScreen
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stockportfoliotracker.network.RetrofitClient
import com.example.stockportfoliotracker.ui.AddToWatchlistScreen
import com.example.stockportfoliotracker.views.EditTransactionScreen
import com.example.stockportfoliotracker.views.PortfolioScreen
import com.example.stockportfoliotracker.views.WatchlistScreen

@Composable
fun PortfolioApp(context: Context) {
    val dataStoreManager = remember { DataStoreManager(context) }
    val darkModeState = dataStoreManager.isDarkMode.collectAsState(initial = false)

    val navController = rememberNavController()
    val stockViewModel: StockViewModel = viewModel()
    val transactionViewModel: TransactionViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        RetrofitClient.setUserApiKey(context)
    }

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
        var selectedTab by remember { mutableStateOf("portfolio") }

        // Get the current destination
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    navController = navController
                )
            },
            floatingActionButton = {
                // Hide FAB for specific routes
                if (currentRoute !in listOf("detail/{ticker}", "settings", "addTransaction", "addToWatchlist")) {
                    when (selectedTab) {
                        "portfolio" -> FloatingActionButton(onClick = { navController.navigate("addTransaction") }) {
                            Text("+")
                        }
                        "watchlist" -> FloatingActionButton(onClick = { navController.navigate("addToWatchlist") }) {
                            Text("+")
                        }
                    }
                } else {
                    // Empty block to hide the FAB
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "portfolio",
                modifier = Modifier.padding(padding)
            ) {
                composable("portfolio") {
                    PortfolioScreen(
                        transactionViewModel = transactionViewModel,
                        navController = navController
                    )
                }
                composable("watchlist") {
                    WatchlistScreen(
                        stockViewModel = stockViewModel,
                        navController = navController
                    )
                }
                composable("addTransaction") {
                    AddTransactionScreen(
                        navController = navController,
                        transactionViewModel = transactionViewModel
                    )
                }
                composable("addToWatchlist") {
                    AddToWatchlistScreen(
                        navController = navController,
                        stockViewModel = stockViewModel
                    )
                }
                composable("detail/{ticker}") { backStackEntry ->
                    val ticker = backStackEntry.arguments?.getString("ticker") ?: ""
                    DetailScreen(
                        navController = navController,
                        stockViewModel = stockViewModel,
                        ticker = ticker
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        isDarkTheme = darkModeState.value,
                        onToggleTheme = toggleTheme,
                        navController = navController,
                        dataStoreManager = dataStoreManager
                    )
                }

                composable("editTransaction/{transactionId}") { backStackEntry ->
                    val transactionId = backStackEntry.arguments?.getString("transactionId")?.toIntOrNull()
                    if (transactionId != null) {
                        EditTransactionScreen(
                            transactionId = transactionId,
                            transactionViewModel = transactionViewModel,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    navController: NavController
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == "portfolio",
            onClick = {
                onTabSelected("portfolio")
                navController.navigate("portfolio") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Inventory, contentDescription = "Portfolio") },
            label = { Text("Portfolio") }
        )
        NavigationBarItem(
            selected = selectedTab == "watchlist",
            onClick = {
                onTabSelected("watchlist")
                navController.navigate("watchlist") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Visibility, contentDescription = "Watchlist") },
            label = { Text("Watchlist") }
        )
        NavigationBarItem(
            selected = selectedTab == "settings",
            onClick = {
                onTabSelected("settings")
                navController.navigate("settings") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}
