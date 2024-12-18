package com.example.stockportfoliotracker.network

import com.example.stockportfoliotracker.BuildConfig
import com.example.stockportfoliotracker.data.models.CompanyInfo
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FinancialModelingPrepApi {
    @GET("api/v3/quote/{symbol}")
    suspend fun getStockQuote(
        @Path("symbol") symbol: String // Use Path instead of Query
    ): List<StockQuoteResponse>

    @GET("api/v3/historical-price-full/{symbol}")
    suspend fun getHistoricalPrices(
        @Path("symbol") symbol: String
    ): HistoricalPricesResponse

    @GET("api/v3/profile/{symbol}")
    suspend fun getCompanyInfo(
        @Path("symbol") symbol: String
    ): List<CompanyInfo>

}

data class StockQuoteResponse(
    val symbol: String,
    val name: String,
    val price: Double,
    val change: Double,
    val changesPercentage: Double
)

data class HistoricalPricesResponse(
    val symbol: String,
    val historical: List<HistoricalPrice>
)

data class HistoricalPrice(
    val date: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

