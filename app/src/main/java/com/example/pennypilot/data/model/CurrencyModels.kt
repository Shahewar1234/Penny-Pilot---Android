package com.example.pennypilot.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRatesResponse(
    val result: String,
    val documentation: String,
    val terms_of_use: String,
    val time_last_update_unix: Long,
    val time_last_update_utc: String,
    val time_next_update_unix: Long,
    val time_next_update_utc: String,
    val base_code: String,
    val conversion_rates: Map<String, Double>
)

data class CountryCurrency(
    val name: String,
    val currencyCode: String,
    val currencySymbol: String,
    val flag: String
)

object CurrencyData {
    // This list is now private to enforce repository usage.
    internal val countries = listOf(
        CountryCurrency("United States", "USD", "$", "🇺🇸"),
        CountryCurrency("India", "INR", "₹", "🇮🇳"),
        CountryCurrency("Japan", "JPY", "¥", "🇯🇵"),
        CountryCurrency("United Kingdom", "GBP", "£", "🇬🇧"),
        CountryCurrency("Canada", "CAD", "$", "🇨🇦"),
        CountryCurrency("Australia", "AUD", "$", "🇦🇺"),
        CountryCurrency("Switzerland", "CHF", "CHF", "🇨🇭"),
        CountryCurrency("China", "CNY", "¥", "🇨🇳"),
        CountryCurrency("Sweden", "SEK", "kr", "🇸🇪"),
        CountryCurrency("New Zealand", "NZD", "$", "🇳🇿"),
        CountryCurrency("Mexico", "MXN", "$", "🇲🇽"),
        CountryCurrency("Singapore", "SGD", "$", "🇸🇬"),
        CountryCurrency("Hong Kong", "HKD", "$", "🇭🇰"),
        CountryCurrency("Norway", "NOK", "kr", "🇳🇴"),
        CountryCurrency("South Korea", "KRW", "₩", "🇰🇷"),
        CountryCurrency("Turkey", "TRY", "₺", "🇹🇷"),
        CountryCurrency("Russia", "RUB", "₽", "🇷🇺"),
        CountryCurrency("Brazil", "BRL", "R$", "🇧🇷"),
        CountryCurrency("South Africa", "ZAR", "R", "🇿🇦")
    )

    fun getCountryByCode(code: String): CountryCurrency? {
        return countries.find { it.currencyCode.equals(code, ignoreCase = true) }
    }
}
