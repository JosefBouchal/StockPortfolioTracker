package com.example.stockportfoliotracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockportfoliotracker.data.StockDatabase
import com.example.stockportfoliotracker.data.models.TransactionEntity
import com.example.stockportfoliotracker.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val transactionDao = StockDatabase.getDatabase(application).transactionDao()

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    // Calculated fields
    val totalSpent: StateFlow<Double>
        get() = _transactions.map { calculateTotalSpent(it) }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val totalSells: StateFlow<Double>
        get() = _transactions.map { calculateTotalSells(it) }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val currentValue: StateFlow<Double>
        get() = _transactions.map { calculateCurrentValue(it) }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val realizedProfitLoss: StateFlow<Double>
        get() = _transactions.map { calculateRealizedProfitLoss(it) }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val unrealizedProfitLoss: StateFlow<Double>
        get() = _transactions.map { calculateUnrealizedProfitLoss(it) }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

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

    private fun calculateTotalSpent(transactions: List<TransactionEntity>): Double {
        return transactions.filter { it.quantity > 0 }.sumOf { it.quantity * it.purchasePrice }
    }

    private fun calculateTotalSells(transactions: List<TransactionEntity>): Double {
        return transactions.filter { it.quantity < 0 }.sumOf { -it.quantity * it.purchasePrice }
    }

    private fun calculateCurrentValue(transactions: List<TransactionEntity>): Double {
        return transactions.groupBy { it.ticker.uppercase() }.entries.sumOf { (ticker, tickerTransactions) ->
            val totalShares = tickerTransactions.sumOf { it.quantity }
            val lastPrice = tickerTransactions.lastOrNull()?.lastPrice ?: 0.0
            totalShares * lastPrice
        }
    }

    private fun calculateRealizedProfitLoss(transactions: List<TransactionEntity>): Double {
        var realizedProfitLoss = 0.0
        transactions.groupBy { it.ticker.uppercase() }.forEach { (_, tickerTransactions) ->
            var totalShares = 0
            var totalCost = 0.0
            tickerTransactions.forEach { transaction ->
                if (transaction.quantity > 0) {
                    totalShares += transaction.quantity
                    totalCost += transaction.quantity * transaction.purchasePrice
                } else {
                    val soldShares = -transaction.quantity
                    if (soldShares <= totalShares) {
                        val averageCostPerShare = totalCost / totalShares
                        realizedProfitLoss += soldShares * (transaction.purchasePrice - averageCostPerShare)
                        totalShares -= soldShares
                        totalCost -= soldShares * averageCostPerShare
                    }
                }
            }
        }
        return realizedProfitLoss
    }

    private fun calculateUnrealizedProfitLoss(transactions: List<TransactionEntity>): Double {
        var unrealizedProfitLoss = 0.0

        transactions.groupBy { it.ticker.uppercase() }.forEach { (_, tickerTransactions) ->
            var totalShares = 0
            var totalCost = 0.0

            tickerTransactions.forEach { transaction ->
                if (transaction.quantity > 0) { // Buy transaction
                    totalShares += transaction.quantity
                    totalCost += transaction.quantity * transaction.purchasePrice
                } else { // Sell transaction
                    val soldShares = -transaction.quantity
                    if (soldShares <= totalShares) {
                        val averageCostPerShare = totalCost / totalShares
                        totalShares -= soldShares
                        totalCost -= soldShares * averageCostPerShare
                    }
                }
            }

            // Calculate unrealized P/L for remaining shares
            if (totalShares > 0) {
                val lastPrice = tickerTransactions.lastOrNull()?.lastPrice ?: 0.0
                unrealizedProfitLoss += totalShares * (lastPrice - (totalCost / totalShares))
            }
        }

        return unrealizedProfitLoss
    }


}