package com.example.stockportfoliotracker.network

import com.example.stockportfoliotracker.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FinancialModelingPrepApi {
    @GET("api/v3/quote/{symbol}")
    suspend fun getStockQuote(
        @Path("symbol") symbol: String, // Use Path instead of Query
        @Query("apikey") apiKey: String = BuildConfig.FINANCIAL_MODELING_PREP_API_KEY
    ): List<StockQuoteResponse>
}

data class StockQuoteResponse(
    val symbol: String,
    val name: String,
    val price: Double,
    val change: Double,
    val changesPercentage: Double
)
