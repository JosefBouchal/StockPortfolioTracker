package com.example.stockportfoliotracker

import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.statusBarColor=this.resources.getColor(R.color.black)
        setContent {
            PortfolioApp(context = this) // Pass context to PortfolioApp
        }
    }
}