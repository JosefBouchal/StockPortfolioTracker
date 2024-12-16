package com.example.stockportfoliotracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stockportfoliotracker.data.models.CompanyInfo
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
    // Store delegated properties in local variables
    val companyInfoState = stockViewModel.companyInfo.collectAsState(initial = null)
    val historicalPricesState = stockViewModel.historicalPrices.collectAsState(initial = null) // Nullable state
    val errorMessageState = stockViewModel.errorMessage.collectAsState(initial = null)
    val isGraphLoadingState = stockViewModel.isGraphLoading.collectAsState()

    val companyInfo = companyInfoState.value
    val historicalPrices = historicalPricesState.value ?: emptyList() // Ensure non-null list
    val errorMessage = errorMessageState.value
    val isGraphLoading = isGraphLoadingState.value

    // Track whether the price table should be visible
    var showPriceTable by remember { mutableStateOf(false) }

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
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                companyInfo == null && errorMessage == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                companyInfo != null -> {
                    CompanyDetails(companyInfo)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        /*Button(
                            onClick = {
                                if (!showPriceTable) {
                                    stockViewModel.loadHistoricalPrices(ticker) {
                                        showPriceTable = true
                                    }
                                } else {
                                    showPriceTable = false
                                }
                            }
                        ) {
                            Text(if (showPriceTable) "Hide Price Table" else "Show Price Table")
                        }*/
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isGraphLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (showPriceTable) {
                if (historicalPrices.isEmpty()) {
                    Text(
                        text = "No historical data available.",
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    StockPriceTable(prices = historicalPrices)
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
        AsyncImage(
            model = company.image,
            contentDescription = "Company Logo",
            modifier = Modifier
                .size(100.dp)
                .padding(8.dp)
        )
        Text(text = company.companyName, style = MaterialTheme.typography.headlineSmall)
        Text(text = "CEO: ${company.ceo}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Industry: ${company.industry}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Website: ${company.website}", style = MaterialTheme.typography.bodyLarge)
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
fun StockPriceTable(prices: List<HistoricalPrice>) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.YEAR, -1)
    val lastYear = calendar.time

    val monthlyPrices = prices.filter {
        val priceDate = dateFormat.parse(it.date)
        priceDate != null && priceDate.after(lastYear)
    }.groupBy { it.date.substring(0, 7) }

    val sortedMonths = monthlyPrices.keys.sortedDescending()

    val tableData = sortedMonths.map { month ->
        val currentPrice = monthlyPrices[month]?.map { it.close }?.average() ?: 0.0
        val changePercentage = "N/A"
        Triple(month, currentPrice, changePercentage)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(month, modifier = Modifier.weight(1f))
                Text("$%.2f".format(price), modifier = Modifier.weight(1f))
                Text(change, modifier = Modifier.weight(1f))
            }
        }
    }
}