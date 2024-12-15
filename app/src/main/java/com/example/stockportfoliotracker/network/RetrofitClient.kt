package com.example.stockportfoliotracker.network

import android.content.Context
import com.example.stockportfoliotracker.BuildConfig
import com.example.stockportfoliotracker.data.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://financialmodelingprep.com/"
    private var userApiKey: String? = null

    fun setUserApiKey(context: Context) {
        val dataStoreManager = DataStoreManager(context)
        runBlocking {
            userApiKey = dataStoreManager.apiKey.first()
            println("Loaded API Key: $userApiKey")
        }
    }

    // New function to dynamically reload the API key
    fun reloadApiKey(newKey: String) {
        userApiKey = newKey
        println("Reloaded API Key: $userApiKey")
    }

    private val httpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val apiKey = if (userApiKey.isNullOrEmpty()) {
                    BuildConfig.FINANCIAL_MODELING_PREP_API_KEY
                } else {
                    userApiKey
                }
                val originalRequest = chain.request()
                val urlWithApiKey = originalRequest.url.newBuilder()
                    .addQueryParameter("apikey", apiKey)
                    .build()
                val requestWithApiKey = originalRequest.newBuilder()
                    .url(urlWithApiKey)
                    .build()
                chain.proceed(requestWithApiKey)
            }
            .build()
    }

    val api: FinancialModelingPrepApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(FinancialModelingPrepApi::class.java)
    }
}