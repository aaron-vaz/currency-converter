package com.example.currency_converter.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

@Serializable
data class Rates(
    var base: String,
    var date: String,
    var rates: Map<String, Float>
)

class CurrencyRateService(
    private val client: HttpClient,
    private val apiURL: String
) {
    suspend fun rates(base: String = "") =
        client.get<Rates>("$apiURL/rates?base=$base")
}
