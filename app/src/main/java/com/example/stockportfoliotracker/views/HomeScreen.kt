package com.example.stockportfoliotracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Welcome to Stock Portfolio Tracker!")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("details") }) {
            Text("Go to Details")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("settings") }) {
            Text("Go to Settings")
        }
    }
}
