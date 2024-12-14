package com.example.stockportfoliotracker.network

import retrofit2.http.GET
import retrofit2.http.Query
import com.example.stockportfoliotracker.BuildConfig
import com.google.gson.annotations.SerializedName

interface AlphaVantageApi {
    @GET("query")
    suspend fun getStockPrice(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String = BuildConfig.ALPHA_VANTAGE_API_KEY
    ): StockResponse

    @GET("query")
    suspend fun searchStock(
        @Query("function") function: String = "SYMBOL_SEARCH",
        @Query("keywords") keywords: String,
        @Query("apikey") apiKey: String = BuildConfig.ALPHA_VANTAGE_API_KEY
    ): SymbolSearchResponse
}

data class StockResponse(
    @SerializedName("Global Quote") val globalQuote: GlobalQuote
)

data class GlobalQuote(
    @SerializedName("01. symbol") val symbol: String,
    @SerializedName("05. price") val price: String,
    @SerializedName("09. change") val change: String,
    @SerializedName("10. change percent") val changePercent: String
)

data class SymbolSearchResponse(
    @SerializedName("bestMatches") val bestMatches: List<BestMatch>
)

data class BestMatch(
    @SerializedName("1. symbol") val symbol: String,
    @SerializedName("2. name") val name: String
)

