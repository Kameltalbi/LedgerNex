package com.ledgernex.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ledgernex_settings")

/**
 * DataStore pour les paramètres de l'application.
 * Stocke les capitaux propres (equity_amount) sans table Room.
 */
class SettingsDataStore(private val context: Context) {

    companion object {
        private val EQUITY_AMOUNT_KEY = doublePreferencesKey("equity_amount")
    }

    val equityAmount: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[EQUITY_AMOUNT_KEY] ?: 0.0
    }

    suspend fun setEquityAmount(amount: Double) {
        context.dataStore.edit { prefs ->
            prefs[EQUITY_AMOUNT_KEY] = amount
        }
    }
}
