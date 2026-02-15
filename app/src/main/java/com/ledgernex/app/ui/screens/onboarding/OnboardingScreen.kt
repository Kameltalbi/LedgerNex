package com.ledgernex.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ledgernex.app.data.datastore.SettingsDataStore
import com.ledgernex.app.ui.theme.BluePrimary
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    settingsDataStore: SettingsDataStore,
    onFinished: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedCurrency by remember { mutableStateOf("EUR") }
    var customCurrency by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("fr") }
    
    // Filtrer les devises par recherche (code, nom ou pays)
    val filteredCurrencies = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            SettingsDataStore.SUPPORTED_CURRENCIES
        } else {
            SettingsDataStore.SUPPORTED_CURRENCIES.filter { currency ->
                currency.code.contains(searchQuery, ignoreCase = true) ||
                currency.name.contains(searchQuery, ignoreCase = true) ||
                currency.countries.any { it.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "LedgerNex",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = BluePrimary
        )
        Text(
            text = "Comptabilité simplifiée",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- Langue ---
        Text(
            text = "Choisissez votre langue",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = BluePrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            SettingsDataStore.SUPPORTED_LANGUAGES.forEach { (code, label) ->
                val selected = selectedLanguage == code
                FilterChip(
                    selected = selected,
                    onClick = { selectedLanguage = code },
                    label = {
                        Text(
                            text = label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 15.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BluePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Devise ---
        Text(
            text = when (selectedLanguage) {
                "en" -> "Choose your currency"
                "ar" -> "اختر عملتك"
                else -> "Choisissez votre devise"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = BluePrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (!showCustomInput) {
            Column {
                // Champ de sélection avec recherche intégrée
                OutlinedTextField(
                    value = if (currencyExpanded) searchQuery else selectedCurrency,
                    onValueChange = { 
                        searchQuery = it
                        if (!currencyExpanded) currencyExpanded = true
                    },
                    label = { Text(when (selectedLanguage) {
                        "en" -> "Currency"
                        "ar" -> "العملة"
                        else -> "Devise"
                    }) },
                    placeholder = { Text(when (selectedLanguage) {
                        "en" -> "Search by country or currency..."
                        "ar" -> "ابحث عن بلد أو عملة..."
                        else -> "Rechercher par pays ou devise..."
                    }) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { currencyExpanded = !currencyExpanded }) {
                            Icon(
                                imageVector = if (currencyExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }
                    }
                )
                
                // Liste déroulante
                if (currencyExpanded && filteredCurrencies.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            filteredCurrencies.forEach { currency ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCurrency = currency.code
                                            currencyExpanded = false
                                            searchQuery = ""
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${currency.code} - ${currency.name}",
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = currency.countries.take(3).joinToString(", "),
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                            
                            // Option devise personnalisée
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showCustomInput = true
                                        currencyExpanded = false
                                        searchQuery = ""
                                    }
                                    .padding(16.dp)
                                    .background(Color.LightGray.copy(alpha = 0.1f)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✏️ Autre devise...", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = customCurrency,
                onValueChange = { customCurrency = it.uppercase().take(10) },
                label = { Text("Devise personnalisée") },
                placeholder = { Text("ex: DA, CFA, XBT...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { showCustomInput = false }) {
                Text("← Retour à la liste")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Bouton Commencer ---
        Button(
            onClick = {
                scope.launch {
                    val currency = if (showCustomInput) {
                        customCurrency.ifBlank { "EUR" }
                    } else {
                        selectedCurrency
                    }
                    settingsDataStore.setCurrency(currency)
                    settingsDataStore.setLanguage(selectedLanguage)
                    settingsDataStore.setOnboardingDone()
                    onFinished()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Text(
                text = when (selectedLanguage) {
                    "en" -> "Get Started"
                    "ar" -> "ابدأ"
                    else -> "Commencer"
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
