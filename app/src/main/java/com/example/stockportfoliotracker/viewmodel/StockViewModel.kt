package com.example.stockportfoliotracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockportfoliotracker.data.StockDatabase
import com.example.stockportfoliotracker.data.StockEntity
import com.example.stockportfoliotracker.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StockViewModel(application: Application) : AndroidViewModel(application) {
    private val stockDao = StockDatabase.getDatabase(application).stockDao()

    val allStocks: Flow<List<StockEntity>> = stockDao.getAllStocks()

    fun addStock(stock: StockEntity) {
        viewModelScope.launch {
            stockDao.insertStock(stock)
        }
    }

    fun deleteStock(stock: StockEntity) {
        viewModelScope.launch {
            stockDao.deleteStock(stock)
        }
    }

    /**
     * Fetches detailed stock information including price, name, and change.
     * @param ticker Stock ticker symbol
     * @param onResult Callback with fetched StockEntity or null in case of failure
     */
    fun fetchStockDetails(ticker: String, onResult: (StockEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                // Fetch the stock price, change, and change percent
                val globalQuoteResponse = RetrofitClient.api.getStockPrice(symbol = ticker)
                val globalQuote = globalQuoteResponse.globalQuote

                // Fetch the stock name
                val symbolSearchResponse = RetrofitClient.api.searchStock(keywords = ticker)
                val bestMatch = symbolSearchResponse.bestMatches.firstOrNull()

                if (globalQuote != null && bestMatch != null) {
                    val stock = StockEntity(
                        ticker = globalQuote.symbol,
                        name = bestMatch.name, // Retrieve stock name
                        price = globalQuote.price.toDoubleOrNull() ?: 0.0,
                        change = "${globalQuote.change} (${globalQuote.changePercent})" // Combine change and percent
                    )
                    onResult(stock)
                } else {
                    onResult(null) // Handle missing data gracefully
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }

    fun refreshAllStocks(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val updatedStocks = allStocks.first().mapNotNull { stock ->
                    try {
                        // Fetch updated stock details
                        val response = RetrofitClient.api.getStockPrice(symbol = stock.ticker)
                        val globalQuote = response.globalQuote

                        // Create an updated StockEntity
                        stock.copy(
                            price = globalQuote.price.toDoubleOrNull() ?: stock.price,
                            change = "${globalQuote.change} (${globalQuote.changePercent})"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace() // Handle individual stock fetch errors gracefully
                        null
                    }
                }
                // Update all stocks in the database
                stockDao.insertStockList(updatedStocks) // Assuming you add a batch insert method
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                onComplete() // Notify UI when refresh is done
            }
        }
    }

}

