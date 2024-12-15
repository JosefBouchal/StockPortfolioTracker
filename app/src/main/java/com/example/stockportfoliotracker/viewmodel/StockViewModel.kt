package com.example.stockportfoliotracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockportfoliotracker.data.StockDatabase
import com.example.stockportfoliotracker.data.StockEntity
import com.example.stockportfoliotracker.network.HistoricalPrice
import com.example.stockportfoliotracker.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

    fun fetchStockQuote(ticker: String, onResult: (StockEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getStockQuote(symbol = ticker)
                if (response.isNotEmpty()) {
                    val stockQuote = response.first()
                    val stock = StockEntity(
                        ticker = stockQuote.symbol,
                        name = stockQuote.name,
                        price = stockQuote.price,
                        change = "${stockQuote.change} (${stockQuote.changesPercentage}%)"
                    )
                    onResult(stock)
                } else {
                    onResult(null)
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
                        val response = RetrofitClient.api.getStockQuote(symbol = stock.ticker)
                        if (response.isNotEmpty()) {
                            val stockQuote = response.first()
                            stock.copy(
                                price = stockQuote.price,
                                change = "${stockQuote.change} (${stockQuote.changesPercentage}%)"
                            )
                        } else null
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                stockDao.insertStockList(updatedStocks)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                onComplete()
            }
        }
    }

    fun getStockFromDatabase(ticker: String): StockEntity? {
        return runBlocking {
            stockDao.getStockByTicker(ticker)
        }
    }

    fun fetchHistoricalPrices(ticker: String, onResult: (List<HistoricalPrice>?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getHistoricalPrices(symbol = ticker)
                onResult(response.historical)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }


}