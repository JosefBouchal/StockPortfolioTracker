package com.example.stockportfoliotracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stockportfoliotracker.data.models.StockEntity
import com.example.stockportfoliotracker.network.HistoricalPrice
import com.example.stockportfoliotracker.viewmodel.StockViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: NavController, stockViewModel: StockViewModel, ticker: String) {
    val stock by stockViewModel.stockDetails.collectAsState(initial = null)
    val historicalPrices by stockViewModel.historicalPrices.collectAsState(initial = null)
    val errorMessage by stockViewModel.errorMessage.collectAsState(initial = null)
    val isGraphLoading by stockViewModel.isGraphLoading.collectAsState(initial = false)

    LaunchedEffect(ticker) {
        stockViewModel.loadStockDetails(ticker)
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
                stock == null && errorMessage == null -> {
                    CircularProgressIndicator()
                }
                !errorMessage.isNullOrBlank() -> { // Use safe call and explicit null check
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error) // Error message is non-null here
                }
                stock != null -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StockDetails(stock!!)
                        Button(
                            onClick = { stockViewModel.loadHistoricalPrices(ticker) },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            enabled = !isGraphLoading
                        ) {
                            if (isGraphLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Text("Load Table")
                            }
                        }
                        historicalPrices?.let {
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
    }.groupBy { it.date.substring(0, 7) }

    // Prepare the list of months sorted in descending order
    val sortedMonths = monthlyPrices.keys.sortedDescending()

    // Calculate the table data: Month, Average Price, Change %
    val tableData = mutableListOf<Triple<String, Double, String>>()
    for (i in sortedMonths.indices) {
        val currentMonth = sortedMonths[i]
        val currentPrice = monthlyPrices[currentMonth]?.map { it.close }?.average() ?: 0.0

        val changePercentage = if (i + 1 < sortedMonths.size) {
            val previousMonth = sortedMonths[i + 1]
            val previousPrice = monthlyPrices[previousMonth]?.map { it.close }?.average() ?: 0.0

            if (previousPrice != 0.0) {
                val change = ((currentPrice - previousPrice) / previousPrice) * 100
                "%.2f%%".format(change) // Calculate change %
            } else {
                "N/A"
            }
        } else {
            "N/A" // No previous month to compare for the last month
        }

        tableData.add(Triple(currentMonth, currentPrice, changePercentage))
    }

    // Display the table
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Monthly Price Table", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Table header
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Month", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text("Price", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text("Change %", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                }
            }

            // Table rows
            items(tableData) { (month, price, change) ->
                val changeColor = when {
                    change.startsWith("-") -> Color.Red
                    change != "N/A" -> Color.Green
                    else -> Color.Unspecified
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(month, modifier = Modifier.weight(1f))
                    Text("$%.2f".format(price), modifier = Modifier.weight(1f))
                    Text(change, color = changeColor, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
