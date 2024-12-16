package com.example.stockportfoliotracker.ui

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
import com.example.stockportfoliotracker.viewmodel.StockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToWatchlistScreen(navController: NavController, stockViewModel: StockViewModel) {
    val ticker = remember { mutableStateOf("") }
    val isAddingStock by stockViewModel.isRefreshing.collectAsState(initial = false)
    val errorMessage by stockViewModel.errorMessage.collectAsState(initial = null)
    val addSuccess = remember { mutableStateOf(false) }
    val localError = remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(addSuccess.value) {
        if (addSuccess.value) {
            navController.navigateUp() // Navigate back only on success
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add to Watchlist") },
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
                    localError.value = null // Reset local error on input change
                },
                label = { Text("Stock Ticker") },
                modifier = Modifier.fillMaxWidth(),
                isError = localError.value != null,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
            )
            localError.value?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (ticker.value.isBlank()) {
                        localError.value = "Field cannot be empty"
                    } else {
                        stockViewModel.addStockToWatchlist(ticker.value) { success ->
                            addSuccess.value = success
                        }
                    }
                },
                enabled = !isAddingStock
            ) {
                if (isAddingStock) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Add to Watchlist")
                }
            }

            // Display error messages from the ViewModel
            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}