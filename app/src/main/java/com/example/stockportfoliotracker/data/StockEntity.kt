package com.example.stockportfoliotracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stocks")
data class StockEntity(
    @PrimaryKey val ticker: String, // Primary key is required.
    val name: String,
    val price: Double,
    val change: String
)
