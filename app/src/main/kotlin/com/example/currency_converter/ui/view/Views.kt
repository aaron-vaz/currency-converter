package com.example.currency_converter.ui.view

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.currency_converter.R
import com.example.currency_converter.api.CurrencyRateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CurrencyConverterViewModel(private val service: CurrencyRateService) : ViewModel() {
    private val _rates: Channel<List<String>> = Channel(Channel.BUFFERED)
    val rates = _rates.receiveAsFlow()

    private val _conversion: Channel<Float> = Channel(Channel.BUFFERED)
    val conversion = _conversion.receiveAsFlow()

    private val scope = MainScope()

    init {
        scope.launch(Dispatchers.IO) {
            _rates.send(service.rates().rates.map { it.key })
        }
    }

    fun convert(
        fromValue: String,
        fromCurrency: String,
        toCurrency: String
    ) {
        scope.launch(Dispatchers.IO) {
            val value = fromValue.toFloat()
            val conversionRate = service.rates(fromCurrency).rates[toCurrency]
            if (conversionRate != null) {
                _conversion.send(value * conversionRate)
            }
        }
    }
}

@Composable
fun CurrencyDropdown(
    currencies: List<String>,
    selected: MutableState<String>
) {

    val expanded = remember { mutableStateOf(false) }
    val icon = if (expanded.value) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

    Column {
        TextField(
            value = selected.value,
            onValueChange = { selected.value = it },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = "DropDown",
                    modifier = Modifier.clickable { expanded.value = !expanded.value }
                )
            }
        )

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            for (currency in currencies) {
                DropdownMenuItem(
                    onClick = {
                        selected.value = currency
                        expanded.value = false
                    }
                ) {
                    Text(text = currency)
                }
            }
        }
    }
}

@Composable
fun CurrencyView(
    currencies: State<List<String>>,
    fromValue: MutableState<TextFieldValue>,
    fromCurrency: MutableState<String>,
    toValue: State<TextFieldValue>,
    toCurrency: MutableState<String>,

) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            label = { Text(text = "From") },
            value = fromValue.value,
            singleLine = true,
            onValueChange = { fromValue.value = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(Modifier.width(5.dp))

        CurrencyDropdown(
            currencies = currencies.value,
            selected = fromCurrency
        )
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            label = { Text(text = "To") },
            value = toValue.value,
            singleLine = true,
            onValueChange = {},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            readOnly = true
        )

        Spacer(Modifier.width(5.dp))

        CurrencyDropdown(
            currencies = currencies.value,
            selected = toCurrency
        )
    }
}

@Composable
fun CurrencyConverterView(model: CurrencyConverterViewModel) {
    val context = LocalContext.current

    val fromValue = remember { mutableStateOf(TextFieldValue()) }
    val fromCurrency = remember { mutableStateOf("") }
    val toCurrency = remember { mutableStateOf("") }

    val currencies = model.rates.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Image(
            painter = painterResource(R.drawable.banner),
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(),
            contentDescription = null
        )

        Text(text = "Specify the amount you want to convert")

        CurrencyView(
            currencies = currencies,
            fromValue = fromValue,
            fromCurrency = fromCurrency,
            toValue = model
                .conversion
                .map { TextFieldValue(text = it.toString()) }
                .collectAsState(initial = TextFieldValue()),
            toCurrency = toCurrency
        )

        Button(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(),
            onClick = {
                if (fromValue.value.text.isBlank()) {
                    Toast
                        .makeText(
                            context,
                            "Please supply a value to convert",
                            Toast.LENGTH_SHORT
                        )
                        .show()

                    return@Button
                }

                model.convert(
                    fromValue = fromValue.value.text,
                    fromCurrency = fromCurrency.value,
                    toCurrency = toCurrency.value
                )
            }
        ) {
            Text(text = "Convert")
        }
    }
}
