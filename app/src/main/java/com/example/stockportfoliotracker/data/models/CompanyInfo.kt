package com.example.stockportfoliotracker.data.models

data class CompanyInfo(
    val symbol: String,
    val price: Double,
    val beta: Double,
    val volAvg: Long,
    val mktCap: Long,
    val lastDiv: Double,
    val range: String,
    val changes: Double,
    val companyName: String,
    val currency: String,
    val industry: String,
    val website: String,
    val description: String,
    val ceo: String,
    val sector: String,
    val country: String,
    val fullTimeEmployees: String,
    val image: String
)
