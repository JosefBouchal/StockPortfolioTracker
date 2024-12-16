package com.example.stockportfoliotracker.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.stockportfoliotracker.viewmodel.StockViewModel
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stockportfoliotracker.data.models.StockEntity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(stockViewModel: StockViewModel, navController: NavController) {
    val stocks by stockViewModel.stocks.collectAsState(initial = emptyList())
    val isRefreshing by stockViewModel.isRefreshing.collectAsState(initial = false)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Watchlist") },
                actions = {
                    IconButton(onClick = { stockViewModel.refreshAllStocks() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Stocks",
                            tint = if (isRefreshing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(stocks) { stock ->
                StockItem(
                    stock = stock,
                    navController = navController,
                    onDeleteStock = { stockViewModel.deleteStock(stock) }
                )
            }
        }
    }
}


@Composable
fun StockItem(
    stock: StockEntity,
    navController: NavController, // Add navController parameter
    onDeleteStock: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { navController.navigate("detail/${stock.ticker}") }, // Navigate to detail screen
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stock.name, style = MaterialTheme.typography.titleMedium)
                Text(text = stock.ticker, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "$%.2f".format(stock.price), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stock.change,
                    color = if (stock.change.startsWith("+")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            IconButton(
                onClick = { onDeleteStock() }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Stock")
            }
        }
    }
}