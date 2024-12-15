package com.example.stockportfoliotracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticker: String,
    val quantity: Int,
    val purchasePrice: Double,
    val lastPrice: Double = 0.0
)