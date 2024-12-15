package com.example.stockportfoliotracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.stockportfoliotracker.data.models.StockEntity
import com.example.stockportfoliotracker.viewmodel.StockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    stockViewModel: StockViewModel
) {
    // Observe state from ViewModel
    val stocks by stockViewModel.stocks.collectAsStateWithLifecycle(initialValue = emptyList())
    val isRefreshing by stockViewModel.isRefreshing.collectAsState(initial = false)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Stocks") },
                actions = {
                    // Refresh button
                    IconButton(onClick = { stockViewModel.refreshAllStocks() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Stocks",
                            tint = if (isRefreshing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    // Navigate to settings
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addStock") }) {
                Text("+")
            }
        }
    ) { padding ->
        StockList(
            stocks = stocks,
            navController = navController,
            onDeleteStock = { stockViewModel.deleteStock(it) },
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun StockList(
    stocks: List<StockEntity>,
    navController: NavController,
    onDeleteStock: (StockEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(stocks) { stock ->
            StockItem(stock, navController, onDeleteStock)
        }
    }
}

@Composable
fun StockItem(stock: StockEntity, navController: NavController, onDeleteStock: (StockEntity) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { navController.navigate("detail/${stock.ticker}") },
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stock.name, style = MaterialTheme.typography.headlineSmall)
                Text(text = stock.ticker, style = MaterialTheme.typography.bodySmall)
            }
            Column(
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(text = "$${stock.price}", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = stock.change,
                    color = if (stock.change.startsWith("+")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            IconButton(
                onClick = { onDeleteStock(stock) }, // Call deleteStock from ViewModel
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete, // Add delete icon
                    contentDescription = "Delete Stock",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
