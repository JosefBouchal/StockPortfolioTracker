package com.example.stockportfoliotracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockportfoliotracker.data.StockDatabase
import com.example.stockportfoliotracker.data.models.TransactionEntity
import com.example.stockportfoliotracker.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val transactionDao = StockDatabase.getDatabase(application).transactionDao()

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            transactionDao.getAllTransactions()
                .catch { e -> e.printStackTrace() }
                .collect { transactionList ->
                    _transactions.value = transactionList
                }
        }
    }

    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionDao.insertTransaction(transaction)
            loadTransactions() // Refresh state
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionDao.deleteTransaction(transaction)
            loadTransactions() // Refresh state
        }
    }

    fun refreshPortfolioPrices() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // Fetch unique tickers from all transactions
                val transactions = transactionDao.getAllTransactions().first()
                val uniqueTickers = transactions.map { it.ticker }.distinct()

                // Fetch prices for unique tickers
                val tickerPriceMap = uniqueTickers.associateWith { ticker ->
                    try {
                        val response = RetrofitClient.api.getStockQuote(symbol = ticker)
                        response.firstOrNull()?.price ?: 0.0 // Default to 0.0 if no response
                    } catch (e: Exception) {
                        e.printStackTrace()
                        0.0 // Default to 0.0 in case of error
                    }
                }

                // Update all transactions with the fetched prices
                val updatedTransactions = transactions.map { transaction ->
                    transaction.copy(lastPrice = tickerPriceMap[transaction.ticker] ?: transaction.lastPrice)
                }

                // Update the database
                updatedTransactions.forEach { transaction ->
                    transactionDao.insertTransaction(transaction)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to refresh portfolio prices."
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun getTransactionById(id: Int): Flow<TransactionEntity?> {
        return transactionDao.getTransactionById(id)
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionDao.insertTransaction(transaction) // Replaces the transaction with the new one
            loadTransactions()
        }
    }

    fun getNetQuantityForTicker(ticker: String): Int {
        val normalizedTicker = ticker.lowercase() // Normalize user input
        return transactions.value
            .filter { it.ticker.lowercase() == normalizedTicker } // Normalize stored tickers
            .sumOf { it.quantity }
    }



}