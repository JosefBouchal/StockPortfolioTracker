package com.example.stockportfoliotracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stocks")
data class StockEntity(
    @PrimaryKey val ticker: String, // Primary key is required.
    val name: String,
    val price: Double,
    val change: String,
    val quantity: Int = 0, // Default to 0 for Watchlist items
    val purchasePrice: Double = 0.0 // Default to 0.0 for Watchlist items
)
