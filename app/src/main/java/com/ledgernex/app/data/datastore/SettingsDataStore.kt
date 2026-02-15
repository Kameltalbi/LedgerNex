package com.ledgernex.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ledgernex_settings")

/**
 * DataStore pour les paramètres de l'application.
 * Stocke : capitaux propres, catégories, devise.
 */
class SettingsDataStore(private val context: Context) {

    companion object {
        private val EQUITY_AMOUNT_KEY = doublePreferencesKey("equity_amount")
        private val CURRENCY_KEY = stringPreferencesKey("currency")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val ONBOARDING_DONE_KEY = booleanPreferencesKey("onboarding_done")
        private val CATEGORIES_KEY = stringSetPreferencesKey("categories")

        val DEFAULT_CATEGORIES = setOf(
            "Ventes", "Prestations", "Loyer", "Salaires", "Fournitures",
            "Télécom", "Transport", "Assurance", "Impôts", "Divers"
        )

        val SUPPORTED_LANGUAGES = listOf(
            "fr" to "Français",
            "en" to "English",
            "ar" to "العربية"
        )

        val SUPPORTED_CURRENCIES = listOf(
            "EUR" to "Euro (€)",
            "USD" to "Dollar américain ($)",
            "GBP" to "Livre sterling (£)",
            "JPY" to "Yen japonais (¥)",
            "CHF" to "Franc suisse (CHF)",
            "CAD" to "Dollar canadien (CAD)",
            "AUD" to "Dollar australien (AUD)",
            "CNY" to "Yuan chinois (¥)",
            "INR" to "Roupie indienne (₹)",
            "BRL" to "Real brésilien (R$)",
            "RUB" to "Rouble russe (₽)",
            "KRW" to "Won sud-coréen (₩)",
            "MXN" to "Peso mexicain (MXN)",
            "ZAR" to "Rand sud-africain (ZAR)",
            "SEK" to "Couronne suédoise (SEK)",
            "NOK" to "Couronne norvégienne (NOK)",
            "DKK" to "Couronne danoise (DKK)",
            "PLN" to "Zloty polonais (PLN)",
            "TRY" to "Livre turque (₺)",
            "THB" to "Baht thaïlandais (฿)",
            "IDR" to "Roupie indonésienne (Rp)",
            "MYR" to "Ringgit malaisien (RM)",
            "PHP" to "Peso philippin (₱)",
            "SGD" to "Dollar singapourien (SGD)",
            "HKD" to "Dollar de Hong Kong (HKD)",
            "NZD" to "Dollar néo-zélandais (NZD)",
            "AED" to "Dirham des EAU (AED)",
            "SAR" to "Riyal saoudien (SAR)",
            "QAR" to "Riyal qatari (QAR)",
            "KWD" to "Dinar koweïtien (KWD)",
            "BHD" to "Dinar bahreïni (BHD)",
            "OMR" to "Rial omanais (OMR)",
            "JOD" to "Dinar jordanien (JOD)",
            "ILS" to "Shekel israélien (₪)",
            "EGP" to "Livre égyptienne (EGP)",
            "MAD" to "Dirham marocain (MAD)",
            "TND" to "Dinar tunisien (TND)",
            "DZD" to "Dinar algérien (DZD)",
            "LYD" to "Dinar libyen (LYD)",
            "NGN" to "Naira nigérian (₦)",
            "KES" to "Shilling kenyan (KES)",
            "GHS" to "Cedi ghanéen (GHS)",
            "XOF" to "Franc CFA (BCEAO)",
            "XAF" to "Franc CFA (BEAC)",
            "ARS" to "Peso argentin (ARS)",
            "CLP" to "Peso chilien (CLP)",
            "COP" to "Peso colombien (COP)",
            "PEN" to "Sol péruvien (PEN)",
            "VND" to "Dong vietnamien (₫)",
            "PKR" to "Roupie pakistanaise (PKR)",
            "BDT" to "Taka bangladais (৳)",
            "LKR" to "Roupie sri-lankaise (LKR)",
            "CZK" to "Couronne tchèque (CZK)",
            "HUF" to "Forint hongrois (Ft)",
            "RON" to "Leu roumain (RON)",
            "BGN" to "Lev bulgare (BGN)",
            "HRK" to "Kuna croate (HRK)",
            "UAH" to "Hryvnia ukrainienne (₴)",
            "IQD" to "Dinar irakien (IQD)",
            "IRR" to "Rial iranien (IRR)",
            "AFN" to "Afghani (AFN)",
            "MMK" to "Kyat birman (MMK)",
            "NPR" to "Roupie népalaise (NPR)"
        )
    }

    // --- Capitaux propres ---
    val equityAmount: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[EQUITY_AMOUNT_KEY] ?: 0.0
    }

    suspend fun setEquityAmount(amount: Double) {
        context.dataStore.edit { prefs ->
            prefs[EQUITY_AMOUNT_KEY] = amount
        }
    }

    // --- Devise ---
    val currency: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CURRENCY_KEY] ?: "EUR"
    }

    suspend fun setCurrency(currency: String) {
        context.dataStore.edit { prefs ->
            prefs[CURRENCY_KEY] = currency
        }
    }

    // --- Catégories ---
    val categories: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[CATEGORIES_KEY] ?: DEFAULT_CATEGORIES
    }

    suspend fun setCategories(categories: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[CATEGORIES_KEY] = categories
        }
    }

    suspend fun addCategory(category: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[CATEGORIES_KEY] ?: DEFAULT_CATEGORIES
            prefs[CATEGORIES_KEY] = current + category
        }
    }

    suspend fun removeCategory(category: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[CATEGORIES_KEY] ?: DEFAULT_CATEGORIES
            prefs[CATEGORIES_KEY] = current - category
        }
    }

    // --- Langue ---
    val language: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LANGUAGE_KEY] ?: "fr"
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = language
        }
    }

    // --- Onboarding ---
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_DONE_KEY] ?: false
    }

    suspend fun setOnboardingDone() {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_DONE_KEY] = true
        }
    }

    suspend fun isOnboardingDone(): Boolean {
        return context.dataStore.data.first()[ONBOARDING_DONE_KEY] ?: false
    }

    // --- Reset complet ---
    suspend fun resetAll() {
        context.dataStore.edit { it.clear() }
    }
}
