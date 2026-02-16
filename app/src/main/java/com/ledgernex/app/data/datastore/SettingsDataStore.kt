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
        private val RECETTES_CATEGORIES_KEY = stringPreferencesKey("recettes_categories_csv")
        private val DEPENSES_CATEGORIES_KEY = stringPreferencesKey("depenses_categories_csv")

        private const val DEFAULT_RECETTES_CSV = "Ventes,Prestations,Services,Consulting,Autres recettes"
        private const val DEFAULT_DEPENSES_CSV = "Loyer,Salaires,Fournitures,Télécom,Transport,Assurance,Impôts,Marketing,Maintenance,Divers"

        val DEFAULT_RECETTES_CATEGORIES = DEFAULT_RECETTES_CSV.split(",").map { it.trim() }.toSet()
        val DEFAULT_DEPENSES_CATEGORIES = DEFAULT_DEPENSES_CSV.split(",").map { it.trim() }.toSet()

        val SUPPORTED_LANGUAGES = listOf(
            "fr" to "Français",
            "en" to "English",
            "ar" to "العربية"
        )

        data class CurrencyInfo(
            val code: String,
            val name: String,
            val countries: List<String>
        )

        val SUPPORTED_CURRENCIES = listOf(
            CurrencyInfo("EUR", "Euro (€)", listOf("France", "Allemagne", "Italie", "Espagne", "Portugal", "Belgique", "Pays-Bas", "Autriche", "Grèce", "Irlande")),
            CurrencyInfo("USD", "Dollar américain ($)", listOf("États-Unis", "USA", "Amérique")),
            CurrencyInfo("GBP", "Livre sterling (£)", listOf("Royaume-Uni", "Angleterre", "UK")),
            CurrencyInfo("JPY", "Yen japonais (¥)", listOf("Japon")),
            CurrencyInfo("CHF", "Franc suisse (CHF)", listOf("Suisse")),
            CurrencyInfo("CAD", "Dollar canadien (CAD)", listOf("Canada")),
            CurrencyInfo("AUD", "Dollar australien (AUD)", listOf("Australie")),
            CurrencyInfo("CNY", "Yuan chinois (¥)", listOf("Chine")),
            CurrencyInfo("INR", "Roupie indienne (₹)", listOf("Inde")),
            CurrencyInfo("BRL", "Real brésilien (R$)", listOf("Brésil")),
            CurrencyInfo("RUB", "Rouble russe (₽)", listOf("Russie")),
            CurrencyInfo("KRW", "Won sud-coréen (₩)", listOf("Corée du Sud", "Corée")),
            CurrencyInfo("MXN", "Peso mexicain (MXN)", listOf("Mexique")),
            CurrencyInfo("ZAR", "Rand sud-africain (ZAR)", listOf("Afrique du Sud")),
            CurrencyInfo("SEK", "Couronne suédoise (SEK)", listOf("Suède")),
            CurrencyInfo("NOK", "Couronne norvégienne (NOK)", listOf("Norvège")),
            CurrencyInfo("DKK", "Couronne danoise (DKK)", listOf("Danemark")),
            CurrencyInfo("PLN", "Zloty polonais (PLN)", listOf("Pologne")),
            CurrencyInfo("TRY", "Livre turque (₺)", listOf("Turquie")),
            CurrencyInfo("THB", "Baht thaïlandais (฿)", listOf("Thaïlande")),
            CurrencyInfo("IDR", "Roupie indonésienne (Rp)", listOf("Indonésie")),
            CurrencyInfo("MYR", "Ringgit malaisien (RM)", listOf("Malaisie")),
            CurrencyInfo("PHP", "Peso philippin (₱)", listOf("Philippines")),
            CurrencyInfo("SGD", "Dollar singapourien (SGD)", listOf("Singapour")),
            CurrencyInfo("HKD", "Dollar de Hong Kong (HKD)", listOf("Hong Kong")),
            CurrencyInfo("NZD", "Dollar néo-zélandais (NZD)", listOf("Nouvelle-Zélande")),
            CurrencyInfo("AED", "Dirham des EAU (AED)", listOf("Émirats arabes unis", "Dubai", "Abu Dhabi")),
            CurrencyInfo("SAR", "Riyal saoudien (SAR)", listOf("Arabie saoudite")),
            CurrencyInfo("QAR", "Riyal qatari (QAR)", listOf("Qatar")),
            CurrencyInfo("KWD", "Dinar koweïtien (KWD)", listOf("Koweït")),
            CurrencyInfo("BHD", "Dinar bahreïni (BHD)", listOf("Bahreïn")),
            CurrencyInfo("OMR", "Rial omanais (OMR)", listOf("Oman")),
            CurrencyInfo("JOD", "Dinar jordanien (JOD)", listOf("Jordanie")),
            CurrencyInfo("ILS", "Shekel israélien (₪)", listOf("Israël")),
            CurrencyInfo("EGP", "Livre égyptienne (EGP)", listOf("Égypte")),
            CurrencyInfo("MAD", "Dirham marocain (MAD)", listOf("Maroc")),
            CurrencyInfo("TND", "Dinar tunisien (TND)", listOf("Tunisie")),
            CurrencyInfo("DZD", "Dinar algérien (DZD)", listOf("Algérie")),
            CurrencyInfo("LYD", "Dinar libyen (LYD)", listOf("Libye")),
            CurrencyInfo("NGN", "Naira nigérian (₦)", listOf("Nigeria")),
            CurrencyInfo("KES", "Shilling kenyan (KES)", listOf("Kenya")),
            CurrencyInfo("GHS", "Cedi ghanéen (GHS)", listOf("Ghana")),
            CurrencyInfo("XOF", "Franc CFA (BCEAO)", listOf("Sénégal", "Côte d'Ivoire", "Bénin", "Burkina Faso", "Mali", "Niger", "Togo")),
            CurrencyInfo("XAF", "Franc CFA (BEAC)", listOf("Cameroun", "Gabon", "Congo", "Tchad", "Centrafrique", "Guinée équatoriale")),
            CurrencyInfo("ARS", "Peso argentin (ARS)", listOf("Argentine")),
            CurrencyInfo("CLP", "Peso chilien (CLP)", listOf("Chili")),
            CurrencyInfo("COP", "Peso colombien (COP)", listOf("Colombie")),
            CurrencyInfo("PEN", "Sol péruvien (PEN)", listOf("Pérou")),
            CurrencyInfo("VND", "Dong vietnamien (₫)", listOf("Vietnam")),
            CurrencyInfo("PKR", "Roupie pakistanaise (PKR)", listOf("Pakistan")),
            CurrencyInfo("BDT", "Taka bangladais (৳)", listOf("Bangladesh")),
            CurrencyInfo("LKR", "Roupie sri-lankaise (LKR)", listOf("Sri Lanka")),
            CurrencyInfo("CZK", "Couronne tchèque (CZK)", listOf("République tchèque", "Tchéquie")),
            CurrencyInfo("HUF", "Forint hongrois (Ft)", listOf("Hongrie")),
            CurrencyInfo("RON", "Leu roumain (RON)", listOf("Roumanie")),
            CurrencyInfo("BGN", "Lev bulgare (BGN)", listOf("Bulgarie")),
            CurrencyInfo("HRK", "Kuna croate (HRK)", listOf("Croatie")),
            CurrencyInfo("UAH", "Hryvnia ukrainienne (₴)", listOf("Ukraine")),
            CurrencyInfo("IQD", "Dinar irakien (IQD)", listOf("Irak")),
            CurrencyInfo("IRR", "Rial iranien (IRR)", listOf("Iran")),
            CurrencyInfo("AFN", "Afghani (AFN)", listOf("Afghanistan")),
            CurrencyInfo("MMK", "Kyat birman (MMK)", listOf("Birmanie", "Myanmar")),
            CurrencyInfo("NPR", "Roupie népalaise (NPR)", listOf("Népal"))
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
        val savedCurrency = prefs[CURRENCY_KEY] ?: "EUR"
        // Normaliser les alias courants
        when (savedCurrency.uppercase()) {
            "DT" -> "TND"  // Dinar Tunisien
            "DA" -> "DZD"  // Dinar Algérien
            else -> savedCurrency
        }
    }

    suspend fun setCurrency(currency: String) {
        context.dataStore.edit { prefs ->
            // Normaliser avant de sauvegarder
            val normalized = when (currency.uppercase()) {
                "DT" -> "TND"
                "DA" -> "DZD"
                else -> currency
            }
            prefs[CURRENCY_KEY] = normalized
        }
    }

    // --- Catégories Recettes ---
    val recettesCategories: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        val csv = prefs[RECETTES_CATEGORIES_KEY] ?: DEFAULT_RECETTES_CSV
        csv.split(",").filter { it.isNotBlank() }.map { it.trim() }.toSet()
    }

    suspend fun setRecettesCategories(categories: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[RECETTES_CATEGORIES_KEY] = categories.filter { it.isNotBlank() }.joinToString(",")
        }
    }

    suspend fun addRecettesCategory(category: String) {
        val trimmed = category.trim()
        if (trimmed.isBlank()) return
        context.dataStore.edit { prefs ->
            val csv = prefs[RECETTES_CATEGORIES_KEY] ?: DEFAULT_RECETTES_CSV
            val current = csv.split(",").filter { it.isNotBlank() }.map { it.trim() }.toMutableList()
            if (!current.contains(trimmed)) {
                current.add(trimmed)
            }
            prefs[RECETTES_CATEGORIES_KEY] = current.joinToString(",")
        }
    }

    suspend fun removeRecettesCategory(category: String) {
        context.dataStore.edit { prefs ->
            val csv = prefs[RECETTES_CATEGORIES_KEY] ?: DEFAULT_RECETTES_CSV
            val current = csv.split(",").filter { it.isNotBlank() && it.trim() != category }.map { it.trim() }
            prefs[RECETTES_CATEGORIES_KEY] = current.joinToString(",")
        }
    }

    suspend fun updateRecettesCategory(oldName: String, newName: String) {
        val trimmedNew = newName.trim()
        if (trimmedNew.isBlank()) return
        context.dataStore.edit { prefs ->
            val csv = prefs[RECETTES_CATEGORIES_KEY] ?: DEFAULT_RECETTES_CSV
            val updated = csv.split(",")
                .map { it.trim() }
                .map { if (it == oldName) trimmedNew else it }
                .filter { it.isNotBlank() }
                .distinct()
            prefs[RECETTES_CATEGORIES_KEY] = updated.joinToString(",")
        }
    }

    // --- Catégories Dépenses ---
    val depensesCategories: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        val csv = prefs[DEPENSES_CATEGORIES_KEY] ?: DEFAULT_DEPENSES_CSV
        csv.split(",").filter { it.isNotBlank() }.map { it.trim() }.toSet()
    }

    suspend fun setDepensesCategories(categories: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[DEPENSES_CATEGORIES_KEY] = categories.filter { it.isNotBlank() }.joinToString(",")
        }
    }

    suspend fun addDepensesCategory(category: String) {
        val trimmed = category.trim()
        if (trimmed.isBlank()) return
        context.dataStore.edit { prefs ->
            val csv = prefs[DEPENSES_CATEGORIES_KEY] ?: DEFAULT_DEPENSES_CSV
            val current = csv.split(",").filter { it.isNotBlank() }.map { it.trim() }.toMutableList()
            if (!current.contains(trimmed)) {
                current.add(trimmed)
            }
            prefs[DEPENSES_CATEGORIES_KEY] = current.joinToString(",")
        }
    }

    suspend fun removeDepensesCategory(category: String) {
        context.dataStore.edit { prefs ->
            val csv = prefs[DEPENSES_CATEGORIES_KEY] ?: DEFAULT_DEPENSES_CSV
            val current = csv.split(",").filter { it.isNotBlank() && it.trim() != category }.map { it.trim() }
            prefs[DEPENSES_CATEGORIES_KEY] = current.joinToString(",")
        }
    }

    suspend fun updateDepensesCategory(oldName: String, newName: String) {
        val trimmedNew = newName.trim()
        if (trimmedNew.isBlank()) return
        context.dataStore.edit { prefs ->
            val csv = prefs[DEPENSES_CATEGORIES_KEY] ?: DEFAULT_DEPENSES_CSV
            val updated = csv.split(",")
                .map { it.trim() }
                .map { if (it == oldName) trimmedNew else it }
                .filter { it.isNotBlank() }
                .distinct()
            prefs[DEPENSES_CATEGORIES_KEY] = updated.joinToString(",")
        }
    }

    // --- Ancienne méthode (pour compatibilité) ---
    @Deprecated("Utiliser recettesCategories ou depensesCategories")
    val categories: Flow<Set<String>> = recettesCategories

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
