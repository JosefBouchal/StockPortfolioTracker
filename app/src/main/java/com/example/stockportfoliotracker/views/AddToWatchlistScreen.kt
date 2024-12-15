package com.example.stockportfoliotracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stockportfoliotracker.viewmodel.StockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToWatchlistScreen(navController: NavController, stockViewModel: StockViewModel) {
    val ticker = remember { mutableStateOf("") }
    val isAddingStock by stockViewModel.isRefreshing.collectAsState(initial = false)
    val errorMessage by stockViewModel.errorMessage.collectAsState(initial = null)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add to Watchlist") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ticker Input
            TextField(
                value = ticker.value,
                onValueChange = { ticker.value = it },
                label = { Text("Stock Ticker") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    stockViewModel.addStockToWatchlist(ticker.value)
                    navController.navigateUp()
                },
                enabled = !isAddingStock
            ) {
                if (isAddingStock) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Add to Watchlist")
                }
            }
            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}

