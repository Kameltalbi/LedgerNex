package com.ledgernex.app.ui.util

import com.ledgernex.app.data.datastore.SettingsDataStore
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Formate un montant avec le symbole de la devise choisie par l'utilisateur.
 */
fun formatCurrency(amount: Double, currencyCode: String): String {
    val symbol = getCurrencySymbol(currencyCode)
    val symbols = DecimalFormatSymbols(Locale.FRANCE)
    val df = DecimalFormat("#,##0.00", symbols)
    return "${df.format(amount)} $symbol"
}

fun getCurrencySymbol(currencyCode: String): String {
    return when (currencyCode.uppercase()) {
        "EUR" -> "€"
        "USD" -> "$"
        "GBP" -> "£"
        "JPY" -> "¥"
        "CHF" -> "CHF"
        "CAD" -> "CA$"
        "AUD" -> "A$"
        "CNY" -> "¥"
        "INR" -> "₹"
        "BRL" -> "R$"
        "RUB" -> "₽"
        "KRW" -> "₩"
        "TRY" -> "₺"
        "MXN" -> "MX$"
        "ZAR" -> "R"
        "SEK" -> "kr"
        "NOK" -> "kr"
        "DKK" -> "kr"
        "PLN" -> "zł"
        "CZK" -> "Kč"
        "HUF" -> "Ft"
        "THB" -> "฿"
        "SGD" -> "S$"
        "HKD" -> "HK$"
        "NZD" -> "NZ$"
        "ILS" -> "₪"
        "AED" -> "AED"
        "SAR" -> "SAR"
        "QAR" -> "QAR"
        "KWD" -> "KWD"
        "BHD" -> "BHD"
        "OMR" -> "OMR"
        "JOD" -> "JOD"
        "EGP" -> "E£"
        "MAD" -> "MAD"
        "TND" -> "DT"
        "DZD" -> "DA"
        "LYD" -> "LYD"
        "NGN" -> "₦"
        "GHS" -> "GH₵"
        "KES" -> "KSh"
        "XOF" -> "CFA"
        "XAF" -> "FCFA"
        "COP" -> "CO$"
        "ARS" -> "AR$"
        "CLP" -> "CL$"
        "PEN" -> "S/"
        "UYU" -> "\$U"
        "VES" -> "Bs"
        "PKR" -> "₨"
        "BDT" -> "৳"
        "LKR" -> "Rs"
        "MMK" -> "K"
        "VND" -> "₫"
        "IDR" -> "Rp"
        "MYR" -> "RM"
        "PHP" -> "₱"
        "TWD" -> "NT$"
        else -> currencyCode
    }
}
