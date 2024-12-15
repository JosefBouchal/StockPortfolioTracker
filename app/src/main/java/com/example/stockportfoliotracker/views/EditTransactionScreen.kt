package com.example.stockportfoliotracker.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.*
import androidx.navigation.NavController
import com.example.stockportfoliotracker.viewmodel.TransactionViewModel
import androidx.compose.material3.CircularProgressIndicator
import kotlin.math.absoluteValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    transactionId: Int,
    transactionViewModel: TransactionViewModel,
    navController: NavController
) {
    val transactionState by transactionViewModel.getTransactionById(transactionId).collectAsState(initial = null)

    // Safely create a local variable for transaction
    val transaction = transactionState ?: run {
        // Show loading indicator while waiting for transaction data
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Create local variables for editing fields
    var quantity by remember { mutableStateOf(transaction.quantity.absoluteValue.toString()) }
    var purchasePrice by remember { mutableStateOf(transaction.purchasePrice.toString()) }
    var ticker by remember { mutableStateOf(transaction.ticker) }
    var transactionType by remember { mutableStateOf(if (transaction.quantity > 0) "Buy" else "Sell") }
    var localError by remember { mutableStateOf<String?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) } // Dropdown state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Transaction") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ticker Input
            OutlinedTextField(
                value = ticker,
                onValueChange = {
                    ticker = it
                    localError = null // Reset error on input change
                },
                label = { Text("Ticker") },
                isError = localError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Quantity Input
            OutlinedTextField(
                value = quantity,
                onValueChange = {
                    quantity = it
                    localError = null
                },
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                isError = localError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Purchase Price Input
            OutlinedTextField(
                value = purchasePrice,
                onValueChange = {
                    purchasePrice = it
                    localError = null
                },
                label = { Text("Purchase Price") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                isError = localError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Transaction Type Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Transaction Type: ", style = MaterialTheme.typography.bodyMedium)
                Box {
                    TextButton(onClick = { isDropdownExpanded = true }) {
                        Text(transactionType)
                    }
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            transactionType = "Buy"
                            isDropdownExpanded = false
                        }, text = { Text("Buy") })
                        DropdownMenuItem(onClick = {
                            transactionType = "Sell"
                            isDropdownExpanded = false
                        }, text = { Text("Sell") })
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    val quantityInt = quantity.toIntOrNull()
                    val priceDouble = purchasePrice.toDoubleOrNull()
                    val normalizedTicker = ticker.lowercase()

                    when {
                        ticker.isBlank() -> localError = "Ticker cannot be empty"
                        quantityInt == null || quantityInt <= 0 -> localError = "Quantity must be a positive number"
                        priceDouble == null || priceDouble <= 0 -> localError = "Price must be a positive number"
                        transactionType == "Sell" && quantityInt > transactionViewModel.getNetQuantityForTicker(normalizedTicker) ->
                            localError = "You cannot sell more than the total available quantity"
                        else -> {
                            val updatedTransaction = transaction.copy(
                                ticker = ticker.uppercase(), // Normalize to uppercase for consistency
                                quantity = if (transactionType == "Buy") quantityInt else -quantityInt,
                                purchasePrice = priceDouble
                            )
                            transactionViewModel.updateTransaction(updatedTransaction)
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Save Changes")
            }

            // Display error message if any
            localError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}
