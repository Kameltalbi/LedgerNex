package com.ledgernex.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
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
        private val CATEGORIES_KEY = stringSetPreferencesKey("categories")

        val DEFAULT_CATEGORIES = setOf(
            "Ventes", "Prestations", "Loyer", "Salaires", "Fournitures",
            "Télécom", "Transport", "Assurance", "Impôts", "Divers"
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

    // --- Reset complet ---
    suspend fun resetAll() {
        context.dataStore.edit { it.clear() }
    }
}
