package com.example.stockportfoliotracker.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://www.alphavantage.co/"

    // Create an instance of OkHttpClient with a logging interceptor
    private val httpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Log request and response bodies
        }
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // Add logging interceptor
            .build()
    }

    // Create an instance of Retrofit
    val api: AlphaVantageApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Convert JSON to Kotlin objects
            .client(httpClient) // Add the OkHttp client
            .build()
            .create(AlphaVantageApi::class.java) // Create the API interface
    }
}
