package com.example.stockportfoliotracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stockportfoliotracker.viewmodel.StockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStockScreen(navController: NavController, stockViewModel: StockViewModel) {
    val ticker = remember { mutableStateOf("") }
    val isAddingStock by stockViewModel.isRefreshing.collectAsState(initial = false)
    val errorMessage by stockViewModel.errorMessage.collectAsState(initial = null)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Stock") },
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
            TextField(
                value = ticker.value,
                onValueChange = { ticker.value = it },
                label = { Text("Stock Ticker") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    stockViewModel.loadStockDetails(ticker.value) { success ->
                        if (success) {
                            navController.navigateUp() // Navigate back on success
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enabled = !isAddingStock
            ) {
                if (isAddingStock) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Add Stock")
                }
            }
            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}
