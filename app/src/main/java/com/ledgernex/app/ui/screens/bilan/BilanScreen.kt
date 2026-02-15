package com.ledgernex.app.ui.screens.bilan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ledgernex.app.LedgerNexApp
import com.ledgernex.app.ui.theme.BluePrimary
import com.ledgernex.app.ui.theme.GreenAccent
import com.ledgernex.app.ui.theme.RedError
import com.ledgernex.app.ui.util.formatCurrency
import com.ledgernex.app.ui.viewmodel.BilanViewModel

@Composable
fun BilanScreen(app: LedgerNexApp) {
    val viewModel: BilanViewModel = viewModel(
        factory = BilanViewModel.Factory(
            app.accountRepository,
            app.transactionRepository,
            app.assetRepository,
            app.settingsDataStore
        )
    )
    val state by viewModel.state.collectAsState()
    val currency by app.settingsDataStore.currency.collectAsState(initial = "")
    fun fmt(amount: Double) = formatCurrency(amount, currency)

    var equityInput by remember { mutableStateOf("") }

    if (state.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = BluePrimary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Mini Bilan",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BluePrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Alerte déséquilibre
        if (!state.isBalanced) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RedError.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "⚠ Bilan déséquilibré : Total Actif ≠ Total Passif",
                    color = RedError,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ACTIF
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ACTIF",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                BilanRow("Trésorerie totale", fmt(state.tresorerieTotale))
                BilanRow("Valeur nette immobilisations", fmt(state.valeurNetteImmobilisations))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                BilanRow("Total Actif", fmt(state.totalActif), bold = true, color = BluePrimary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PASSIF
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PASSIF",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                BilanRow("Capitaux propres", fmt(state.capitauxPropres))
                BilanRow(
                    "Résultat de l'exercice",
                    fmt(state.resultatExercice),
                    color = if (state.resultatExercice >= 0) GreenAccent else RedError
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                BilanRow("Total Passif", fmt(state.totalPassif), bold = true, color = BluePrimary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Paramétrage capitaux propres
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Paramétrer les capitaux propres",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = equityInput,
                        onValueChange = { equityInput = it },
                        label = { Text("Montant") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val amount = equityInput.toDoubleOrNull()
                            if (amount != null) {
                                viewModel.setCapitauxPropres(amount)
                                equityInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("Valider")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Indicateur équilibre
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (state.isBalanced) GreenAccent.copy(alpha = 0.1f) else RedError.copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (state.isBalanced) "✓ Bilan équilibré" else "✗ Bilan déséquilibré",
                color = if (state.isBalanced) GreenAccent else RedError,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun BilanRow(
    label: String,
    value: String,
    bold: Boolean = false,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
        Text(
            text = value,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            color = color,
            fontSize = 14.sp
        )
    }
}
