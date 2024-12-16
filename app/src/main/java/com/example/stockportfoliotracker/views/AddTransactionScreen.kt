package com.example.stockportfoliotracker.views

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stockportfoliotracker.data.models.TransactionEntity
import com.example.stockportfoliotracker.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(navController: NavController, transactionViewModel: TransactionViewModel) {
    val ticker = remember { mutableStateOf("") }
    val quantity = remember { mutableStateOf("") }
    val purchasePrice = remember { mutableStateOf("") }
    val transactionType = remember { mutableStateOf("Buy") }
    val localError = remember { mutableStateOf<String?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) } // Dropdown state
    val netAvailableQuantity = remember { mutableStateOf(0) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Fetch net available quantity for the ticker when it changes
    LaunchedEffect(ticker.value) {
        if (ticker.value.isNotBlank()) {
            netAvailableQuantity.value =
                transactionViewModel.getNetQuantityForTicker(ticker.value)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Transaction") },
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
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                    keyboardController?.hide() // Dismiss keyboard on outside tap
                })
            },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ticker Input
            TextField(
                value = ticker.value,
                onValueChange = {
                    ticker.value = it
                    localError.value = null // Reset error on input change
                },
                label = { Text("Stock Ticker") },
                isError = localError.value != null,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Quantity Input
            TextField(
                value = quantity.value,
                onValueChange = {
                    quantity.value = it
                    localError.value = null
                },
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                isError = localError.value != null,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Purchase Price Input
            TextField(
                value = purchasePrice.value,
                onValueChange = {
                    purchasePrice.value = it
                    localError.value = null
                },
                label = { Text("Purchase Price") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                isError = localError.value != null,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Transaction Type Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Transaction Type: ", style = MaterialTheme.typography.bodyMedium)
                Box {
                    TextButton(onClick = { isDropdownExpanded = true }) {
                        Text(transactionType.value)
                    }
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            transactionType.value = "Buy"
                            isDropdownExpanded = false
                        }, text = { Text("Buy") })
                        DropdownMenuItem(onClick = {
                            transactionType.value = "Sell"
                            isDropdownExpanded = false
                        }, text = { Text("Sell") })
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Add Transaction Button
            Button(
                onClick = {
                    val quantityInt = quantity.value.toIntOrNull()
                    val priceDouble = purchasePrice.value.toDoubleOrNull()

                    when {
                        ticker.value.isBlank() -> localError.value = "Ticker cannot be empty"
                        quantityInt == null || quantityInt <= 0 -> localError.value = "Quantity must be a positive number"
                        priceDouble == null || priceDouble <= 0 -> localError.value = "Price must be a positive number"
                        transactionType.value == "Sell" && quantityInt > netAvailableQuantity.value -> {
                            localError.value =
                                "Cannot sell more than available quantity (${netAvailableQuantity.value})"
                        }
                        else -> {
                            transactionViewModel.addTransaction(
                                TransactionEntity(
                                    ticker = ticker.value,
                                    quantity = if (transactionType.value == "Buy") quantityInt else -quantityInt,
                                    purchasePrice = priceDouble,
                                    transactionType = transactionType.value
                                )
                            )
                            navController.navigateUp()
                        }
                    }
                }
            ) {
                Text("Add Transaction")
            }

            // Display error message if any
            localError.value?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}
