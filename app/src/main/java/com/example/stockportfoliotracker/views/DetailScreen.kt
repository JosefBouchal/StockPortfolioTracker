package com.example.stockportfoliotracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import com.example.stockportfoliotracker.network.HistoricalPrice
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: NavController, stockViewModel: StockViewModel, ticker: String) {
    val stock = remember { mutableStateOf<StockEntity?>(null) }
    val historicalPrices = remember { mutableStateOf<List<HistoricalPrice>?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val isGraphLoading = remember { mutableStateOf(false) }

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
                    stockViewModel.addStock(result)
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
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                isLoading.value -> CircularProgressIndicator()
                errorMessage.value != null -> Text(errorMessage.value!!, color = MaterialTheme.colorScheme.error)
                stock.value != null -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StockDetails(stock.value!!)
                        Button(
                            onClick = {
                                isGraphLoading.value = true
                                stockViewModel.fetchHistoricalPrices(ticker) { prices ->
                                    isGraphLoading.value = false
                                    if (prices != null) {
                                        historicalPrices.value = prices
                                    } else {
                                        errorMessage.value = "Failed to load historical data."
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Load Table")
                        }
                        if (isGraphLoading.value) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                        historicalPrices.value?.let {
                            StockPriceTable(it)
                        }
                    }
                }
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

@Composable
fun StockPriceTable(prices: List<HistoricalPrice>) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.YEAR, -1) // Go back 1 year
    val lastYear = calendar.time

    // Filter data for the last year and group by month
    val monthlyPrices = prices.filter {
        val priceDate = dateFormat.parse(it.date)
        priceDate != null && priceDate.after(lastYear)
    }.groupBy { it.date.substring(0, 7) } // Group by Year-Month (e.g., 2024-06)

    // Calculate differences and percentage changes
    val tableData = mutableListOf<Triple<String, Double, String>>()
    var previousPrice: Double? = null

    for ((month, pricesInMonth) in monthlyPrices) {
        val avgPrice = pricesInMonth.map { it.close }.average() // Average price in the month
        val percentageChange = if (previousPrice != null) {
            val change = ((avgPrice - previousPrice) / previousPrice) * 100
            "%.2f%%".format(change)
        } else {
            "N/A"
        }
        tableData.add(Triple(month, avgPrice, percentageChange))
        previousPrice = avgPrice
    }

    // Display the table in a LazyColumn
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Monthly Price Table", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Month", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text("Price", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text("Change %", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                }
            }
            items(tableData) { (month, price, change) ->
                val changeColor = when {
                    change.contains("-") -> Color.Red     // Negative change
                    change != "N/A" -> Color.Green       // Positive change
                    else -> Color.Unspecified            // Default color for "N/A"
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(month, modifier = Modifier.weight(1f))
                    Text("$%.2f".format(price), modifier = Modifier.weight(1f))
                    Text(change, color = changeColor, modifier = Modifier.weight(1f)) // Apply color
                }
            }
        }
    }
}