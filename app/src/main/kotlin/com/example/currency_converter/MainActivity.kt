package com.example.currency_converter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import com.example.currency_converter.api.CurrencyRateService
import com.example.currency_converter.ui.theme.CurrencyConverterTheme
import com.example.currency_converter.ui.view.CurrencyConverterView
import com.example.currency_converter.ui.view.CurrencyConverterViewModel
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val client = HttpClient { install(JsonFeature) }
        val currencyRateService = CurrencyRateService(
            client = client,
            apiURL = getString(R.string.apiURL)
        )

        val viewModel = CurrencyConverterViewModel(service = currencyRateService)

        setContent {
            CurrencyConverterTheme {
                Surface(color = MaterialTheme.colors.background) {
                    CurrencyConverterView(model = viewModel)
                }
            }
        }
    }
}
