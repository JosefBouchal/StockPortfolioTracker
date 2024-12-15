package com.example.stockportfoliotracker.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    val totalSpent = transactions.filter { it.quantity > 0 }.sumOf { it.quantity * it.purchasePrice }
    val currentValue = transactions.sumOf { it.quantity * it.lastPrice }
    val profitLoss = currentValue - totalSpent
    val isRefreshing by transactionViewModel.isRefreshing.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Portfolio") },
                actions = {
                    IconButton(onClick = { transactionViewModel.refreshPortfolioPrices() }) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Portfolio",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Portfolio Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Portfolio Summary", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Spent: $%.2f".format(totalSpent))
                    Text("Current Value: $%.2f".format(currentValue))
                    Text(
                        "Profit/Loss: $%.2f".format(profitLoss),
                        color = if (profitLoss >= 0) Color.Green else Color.Red,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            // Transactions List
            LazyColumn {
                items(transactions) { transaction ->
                    TransactionCard(
                        transaction = transaction,
                        onEdit = { navController.navigate("editTransaction/${transaction.id}") },
                        onDelete = { transactionViewModel.deleteTransaction(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionCard(
    transaction: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.elevatedCardElevation(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = transaction.ticker,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Quantity: ${transaction.quantity}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Bought: $%.2f".format(transaction.purchasePrice),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Current: $%.2f".format(transaction.lastPrice),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "P/L: $%.2f".format((transaction.lastPrice - transaction.purchasePrice) * transaction.quantity),
                        color = if ((transaction.lastPrice - transaction.purchasePrice) >= 0) Color.Green else Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Transaction",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Transaction",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
