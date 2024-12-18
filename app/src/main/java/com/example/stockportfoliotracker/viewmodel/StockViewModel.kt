package com.example.stockportfoliotracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockportfoliotracker.data.StockDatabase
import com.example.stockportfoliotracker.data.models.CompanyInfo
import com.example.stockportfoliotracker.data.models.StockEntity
import com.example.stockportfoliotracker.network.HistoricalPrice
import com.example.stockportfoliotracker.network.RetrofitClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class StockViewModel(application: Application) : AndroidViewModel(application) {
    private val stockDao = StockDatabase.getDatabase(application).stockDao()

    // UI state management
    private val _stocks = MutableStateFlow<List<StockEntity>>(emptyList())
    val stocks: StateFlow<List<StockEntity>> = _stocks

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _stockDetails = MutableStateFlow<StockEntity?>(null)
    val stockDetails: StateFlow<StockEntity?> = _stockDetails

    private val _historicalPrices = MutableStateFlow<List<HistoricalPrice>?>(null)
    val historicalPrices: StateFlow<List<HistoricalPrice>?> = _historicalPrices

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isGraphLoading = MutableStateFlow(false)
    val isGraphLoading: StateFlow<Boolean> = _isGraphLoading

    private val _companyInfo = MutableStateFlow<CompanyInfo?>(null)
    val companyInfo: StateFlow<CompanyInfo?> = _companyInfo

    init {
        loadStocks()
    }

    /**
     * Load all stocks from the database into StateFlow.
     */
    private fun loadStocks() {
        viewModelScope.launch {
            stockDao.getAllStocks()
                .catch { e ->
                    e.printStackTrace()
                    _errorMessage.value = "Failed to load stocks."
                }
                .collect { stockList ->
                    _stocks.value = stockList
                }
        }
    }

    /**
     * Add a stock to the database.
     */
    fun addStock(stock: StockEntity) {
        viewModelScope.launch {
            stockDao.insertStock(stock)
            loadStocks() // Refresh state after adding
        }
    }

    /**
     * Delete a stock from the database.
     */
    fun deleteStock(stock: StockEntity) {
        viewModelScope.launch {
            stockDao.deleteStock(stock)
            loadStocks() // Refresh state after deletion
        }
    }

    /**
     * Fetch a single stock quote from the API and update state.
     */
    fun loadStockDetails(ticker: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _historicalPrices.value = null // Reset the historical prices
            _errorMessage.value = null
            try {
                val localStock = stockDao.getStockByTicker(ticker)
                if (localStock != null) {
                    _stockDetails.value = localStock
                    onResult(true) // Successfully loaded stock
                } else {
                    val response = RetrofitClient.api.getStockQuote(symbol = ticker)
                    if (response.isNotEmpty()) {
                        val stockQuote = response.first()
                        val stock = StockEntity(
                            ticker = stockQuote.symbol,
                            name = stockQuote.name,
                            price = stockQuote.price,
                            change = "${stockQuote.change} (${stockQuote.changesPercentage}%)"
                        )
                        _stockDetails.value = stock
                        addStock(stock) // Save to database
                        onResult(true)
                    } else {
                        _errorMessage.value = "Stock details not found."
                        onResult(false)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to load stock details."
                onResult(false)
            }
        }
    }


    /**
     * Refresh all stocks from the API and update the database.
     */
    fun refreshAllStocks() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val updatedStocks = _stocks.value.mapNotNull { stock ->
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
                loadStocks() // Refresh state after updating
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to refresh stocks."
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Fetch historical prices for a stock and update state.
     */
    fun loadHistoricalPrices(ticker: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isGraphLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.api.getHistoricalPrices(symbol = ticker)
                _historicalPrices.value = response.historical ?: emptyList() // Safeguard against null response
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to load historical prices."
            } finally {
                _isGraphLoading.value = false
                onComplete() // Call the callback to notify completion
            }
        }
    }


    fun addStockToWatchlist(ticker: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getStockQuote(ticker)
                if (response.isNotEmpty()) {
                    val stockQuote = response.first()
                    val stock = StockEntity(
                        ticker = stockQuote.symbol,
                        name = stockQuote.name,
                        price = stockQuote.price,
                        change = "${stockQuote.change} (${stockQuote.changesPercentage}%)"
                    )
                    stockDao.insertStock(stock)
                    _errorMessage.value = null
                    onResult(true) // Success
                } else {
                    _errorMessage.value = "Stock details not found."
                    onResult(false) // Failure
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to add stock to watchlist."
                onResult(false) // Failure
            }
        }
    }


    fun addStockToPortfolio(ticker: String, quantity: Int, purchasePrice: Double) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getStockQuote(symbol = ticker)
                if (response.isNotEmpty()) {
                    val stockQuote = response.first()
                    val stock = StockEntity(
                        ticker = stockQuote.symbol,
                        name = stockQuote.name,
                        price = stockQuote.price,
                        change = "${stockQuote.change} (${stockQuote.changesPercentage}%)",
                        quantity = quantity,
                        purchasePrice = purchasePrice
                    )
                    stockDao.insertStock(stock)
                    loadStocks()
                } else {
                    _errorMessage.value = "Stock details not found."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to add stock to portfolio."
            }
        }
    }

    fun loadCompanyInfo(ticker: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            try {
                val response = RetrofitClient.api.getCompanyInfo(symbol = ticker)
                if (response.isNotEmpty()) {
                    _companyInfo.value = response.first()
                } else {
                    _errorMessage.value = "Company info not found."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to load company info."
            }
        }
    }

}