package com.example.stockportfoliotracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stockportfoliotracker.data.StockEntity
import com.example.stockportfoliotracker.viewmodel.StockViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: NavController, stockViewModel: StockViewModel, ticker: String) {
    val stock = remember { mutableStateOf<StockEntity?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // Check database and fetch data only if necessary
    LaunchedEffect(ticker) {
        val localStock = stockViewModel.getStockFromDatabase(ticker)
        if (localStock != null) {
            stock.value = localStock
            isLoading.value = false
        } else {
            stockViewModel.fetchStockQuote(ticker) { result ->
                isLoading.value = false
                if (result != null) {
                    stock.value = result
                    stockViewModel.addStock(result) // Save to database
                } else {
                    errorMessage.value = "Failed to load stock details."
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Stock Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading.value -> CircularProgressIndicator()
                errorMessage.value != null -> Text(errorMessage.value!!, color = MaterialTheme.colorScheme.error)
                stock.value != null -> StockDetails(stock.value!!)
            }
        }
    }
}

@Composable
fun StockDetails(stock: StockEntity) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Name: ${stock.name}", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Ticker: ${stock.ticker}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Price: $${stock.price}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Change: ${stock.change}", style = MaterialTheme.typography.bodyLarge)
    }
}