package com.ledgernex.app.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    settingsDataStore: SettingsDataStore,
    onFinished: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var currencyInput by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("fr") }

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

        // --- Devise (saisie libre) ---
        Text(
            text = when (selectedLanguage) {
                "en" -> "Enter your currency"
                "ar" -> "أدخل عملتك"
                else -> "Saisissez votre devise"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = BluePrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when (selectedLanguage) {
                "en" -> "e.g. EUR, USD, TND, DA, CFA…"
                "ar" -> "مثال: EUR, USD, TND, DA, CFA…"
                else -> "ex : EUR, USD, TND, DA, CFA…"
            },
            fontSize = 13.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = currencyInput,
            onValueChange = { currencyInput = it.uppercase().take(10) },
            label = {
                Text(
                    when (selectedLanguage) {
                        "en" -> "Currency"
                        "ar" -> "العملة"
                        else -> "Devise"
                    }
                )
            },
            placeholder = { Text("EUR") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Bouton Commencer ---
        Button(
            onClick = {
                scope.launch {
                    val currency = currencyInput.ifBlank { "EUR" }
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
