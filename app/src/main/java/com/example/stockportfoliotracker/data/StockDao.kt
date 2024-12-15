package com.example.stockportfoliotracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stocks")
    fun getAllStocks(): Flow<List<StockEntity>> // Must use `StockEntity`.

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockEntity): Long // Returns row ID.

    @Delete
    suspend fun deleteStock(stock: StockEntity): Int // Returns number of rows deleted.

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockList(stocks: List<StockEntity>)

    @Query("SELECT * FROM stocks WHERE ticker = :ticker LIMIT 1")
    suspend fun getStockByTicker(ticker: String): StockEntity?

}
