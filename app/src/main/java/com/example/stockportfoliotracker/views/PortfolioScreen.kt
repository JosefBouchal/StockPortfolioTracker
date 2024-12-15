package com.example.stockportfoliotracker.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stockportfoliotracker.data.models.TransactionEntity
import com.example.stockportfoliotracker.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(transactionViewModel: TransactionViewModel, navController: NavController) {
    val transactions by transactionViewModel.transactions.collectAsState(initial = emptyList())
    val totalSpent = transactions.sumOf { it.quantity * it.purchasePrice }
    val currentValue = transactions.sumOf { it.quantity * it.lastPrice }
    val profitLoss = currentValue - totalSpent
    var isRefreshing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Portfolio") },
                actions = {
                    IconButton(onClick = {
                        isRefreshing = true
                        transactionViewModel.refreshPortfolioPrices()
                        isRefreshing = false
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Portfolio",
                            tint = if (isRefreshing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addTransaction") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Portfolio Summary", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total Spent: $%.2f".format(totalSpent))
            Text("Current Value: $%.2f".format(currentValue))
            Text(
                "Profit/Loss: $%.2f".format(profitLoss),
                color = if (profitLoss >= 0) Color.Green else Color.Red
            )

            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(transactions) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
}



@Composable
fun TransactionItem(transaction: TransactionEntity) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(transaction.ticker)
        Text("Qty: ${transaction.quantity}")
        Text("Bought: $%.2f".format(transaction.purchasePrice))
        Text("Current: $%.2f".format(transaction.lastPrice))
        Text(
            "P/L: $%.2f".format((transaction.lastPrice - transaction.purchasePrice) * transaction.quantity),
            color = if ((transaction.lastPrice - transaction.purchasePrice) >= 0) Color.Green else Color.Red
        )
    }
}
