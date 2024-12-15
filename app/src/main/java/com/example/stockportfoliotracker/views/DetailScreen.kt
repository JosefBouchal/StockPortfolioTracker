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
import com.example.stockportfoliotracker.data.models.CompanyInfo
import com.example.stockportfoliotracker.data.models.StockEntity
import com.example.stockportfoliotracker.network.HistoricalPrice
import com.example.stockportfoliotracker.viewmodel.StockViewModel
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    stockViewModel: StockViewModel,
    ticker: String
) {
    val companyInfo by stockViewModel.companyInfo.collectAsState(initial = null)
    val errorMessage by stockViewModel.errorMessage.collectAsState(initial = null)

    // Load company info when the screen is displayed
    LaunchedEffect(ticker) {
        stockViewModel.loadCompanyInfo(ticker)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Company Details") },
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
                companyInfo == null && errorMessage == null -> {
                    CircularProgressIndicator()
                }
                !errorMessage.isNullOrBlank() -> {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
                companyInfo != null -> {
                    CompanyDetails(companyInfo!!)
                }
            }
        }
    }
}

@Composable
fun CompanyDetails(company: CompanyInfo) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Company Logo
        AsyncImage(
            model = company.image,
            contentDescription = "Company Logo",
            modifier = Modifier
                .size(100.dp)
                .padding(8.dp)
        )

        // General Information
        Text(text = company.companyName, style = MaterialTheme.typography.headlineSmall)
        Text(text = "CEO: ${company.ceo}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Industry: ${company.industry}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Website: ${company.website}", style = MaterialTheme.typography.bodyLarge)
        //Text(
        //    text = company.description,
        //    style = MaterialTheme.typography.bodyMedium,
        //    modifier = Modifier.padding(8.dp)
        //)

        // Statistics
        Divider()
        Text("Statistics", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Market Cap: ${company.mktCap} USD", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Fulltime Employees: ${company.fullTimeEmployees}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Beta: ${company.beta}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Volume Avg: ${company.volAvg}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Last Dividend: ${company.lastDiv} USD", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Range: ${company.range}", style = MaterialTheme.typography.bodyLarge)
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
