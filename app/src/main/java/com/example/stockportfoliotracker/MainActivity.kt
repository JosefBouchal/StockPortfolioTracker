package com.example.stockportfoliotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Surface
import androidx.compose.material.MaterialTheme
import com.example.stockportfoliotracker.ui.theme.StockPortfolioTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StockPortfolioTrackerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    PortfolioApp()
                }
            }
        }
    }
}