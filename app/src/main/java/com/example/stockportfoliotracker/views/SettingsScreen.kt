package com.example.stockportfoliotracker.ui

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
import com.example.stockportfoliotracker.data.DataStoreManager
import com.example.stockportfoliotracker.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    navController: NavController,
    dataStoreManager: DataStoreManager
) {
    val coroutineScope = rememberCoroutineScope()
    val savedApiKey by dataStoreManager.apiKey.collectAsState(initial = "")

    var newApiKey by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }

    // Synchronize the `newApiKey` with the `savedApiKey`
    LaunchedEffect(savedApiKey) {
        newApiKey = savedApiKey ?: ""
        println("Loaded API Key: $savedApiKey")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = SnackbarHostState())
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dark mode toggle
            Text("App Theme", style = MaterialTheme.typography.headlineSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dark Mode")
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onToggleTheme() }
                )
            }

            // API Key Input
            Text("API Key", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = newApiKey,
                onValueChange = { newApiKey = it },
                label = { Text("Enter API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Save API Key Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        dataStoreManager.setApiKey(newApiKey)
                        RetrofitClient.reloadApiKey(newApiKey) // Dynamically reload API key
                        showSnackbar = true
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save API Key")
            }

            // Snackbar feedback
            if (showSnackbar) {
                Snackbar(
                    action = {
                        TextButton(onClick = { showSnackbar = false }) {
                            Text("Dismiss")
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("API Key Saved Successfully")
                }
            }
        }
    }
}