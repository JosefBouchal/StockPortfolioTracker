package com.example.stockportfoliotracker.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
            // Ticker Input
            TextField(
                value = ticker.value,
                onValueChange = { ticker.value = it },
                label = { Text("Stock Ticker") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Quantity Input
            TextField(
                value = quantity.value,
                onValueChange = { quantity.value = it },
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Purchase Price Input
            TextField(
                value = purchasePrice.value,
                onValueChange = { purchasePrice.value = it },
                label = { Text("Purchase Price") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    transactionViewModel.addTransaction(
                        TransactionEntity(
                            ticker = ticker.value,
                            quantity = quantity.value.toIntOrNull() ?: 0,
                            purchasePrice = purchasePrice.value.toDoubleOrNull() ?: 0.0
                        )
                    )
                    navController.navigateUp()
                }
            ) {
                Text("Add Transaction")
            }
        }
    }
}
