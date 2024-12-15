package com.example.stockportfoliotracker.data

import androidx.room.*
import com.example.stockportfoliotracker.data.models.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    // Insert a new transaction.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    // Fetch all transactions for a specific stock.
    @Query("SELECT * FROM transactions WHERE ticker = :ticker")
    fun getTransactionsForStock(ticker: String): Flow<List<TransactionEntity>>

    // Fetch all transactions (used for portfolio aggregation).
    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    // Delete a transaction by its ID.
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity): Int
}