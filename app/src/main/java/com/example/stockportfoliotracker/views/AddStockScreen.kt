package com.example.stockportfoliotracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stockportfoliotracker.viewmodel.StockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStockScreen(navController: NavController, stockViewModel: StockViewModel) {
    val ticker = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Stock") }
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
            TextField(
                value = ticker.value,
                onValueChange = { ticker.value = it },
                label = { Text("Stock Ticker") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    isLoading.value = true
                    errorMessage.value = null
                    stockViewModel.fetchStockQuote(ticker.value) { stock ->
                        isLoading.value = false
                        if (stock != null) {
                            stockViewModel.addStock(stock)
                            navController.navigateUp()
                        } else {
                            errorMessage.value = "Failed to fetch stock data. Please check the ticker."
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Add Stock")
            }
            if (isLoading.value) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
            errorMessage.value?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}