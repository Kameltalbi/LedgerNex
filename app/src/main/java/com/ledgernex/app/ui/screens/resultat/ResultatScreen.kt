package com.ledgernex.app.ui.screens.resultat

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.ledgernex.app.ui.viewmodel.ResultatViewModel
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale

@Composable
fun ResultatScreen(app: LedgerNexApp) {
    val viewModel: ResultatViewModel = viewModel(
        factory = ResultatViewModel.Factory(app.transactionRepository)
    )
    val state by viewModel.state.collectAsState()
    val fmt = NumberFormat.getCurrencyInstance(Locale.FRANCE)

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
            text = "Compte de Résultat",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BluePrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Sélecteur mois
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val ym = YearMonth.of(state.selectedYear, state.selectedMonth).minusMonths(1)
                viewModel.setMonth(ym.year, ym.monthValue)
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mois précédent")
            }
            Text(
                text = "${YearMonth.of(state.selectedYear, state.selectedMonth).month.name} ${state.selectedYear}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = {
                val ym = YearMonth.of(state.selectedYear, state.selectedMonth).plusMonths(1)
                viewModel.setMonth(ym.year, ym.monthValue)
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Mois suivant")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vue mensuelle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Vue Mensuelle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BluePrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                ResultRow("Total Produits", fmt.format(state.totalProduitsMois), GreenAccent)
                ResultRow("Total Charges", fmt.format(state.totalChargesMois), RedError)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ResultRow(
                    "Résultat Net",
                    fmt.format(state.resultatMois),
                    if (state.resultatMois >= 0) GreenAccent else RedError,
                    bold = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Comparaison mois précédent
                ResultRow(
                    "Mois précédent",
                    fmt.format(state.resultatMoisPrecedent),
                    Color.Gray
                )
                val variationText = if (state.variationMois >= 0) "+${String.format(Locale.FRANCE, "%.1f", state.variationMois)} %"
                    else "${String.format(Locale.FRANCE, "%.1f", state.variationMois)} %"
                ResultRow(
                    "Variation",
                    variationText,
                    if (state.variationMois >= 0) GreenAccent else RedError
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vue annuelle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Vue Annuelle ${state.selectedYear}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BluePrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                ResultRow("Total Produits", fmt.format(state.totalProduitsAnnuel), GreenAccent)
                ResultRow("Total Charges", fmt.format(state.totalChargesAnnuel), RedError)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ResultRow(
                    "Résultat Annuel",
                    fmt.format(state.resultatAnnuel),
                    if (state.resultatAnnuel >= 0) GreenAccent else RedError,
                    bold = true
                )
                Spacer(modifier = Modifier.height(4.dp))
                ResultRow(
                    "Marge",
                    String.format(Locale.FRANCE, "%.1f %%", state.margePercent),
                    BluePrimary
                )
            }
        }
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
    valueColor: Color,
    bold: Boolean = false
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
            fontSize = 15.sp
        )
        Text(
            text = value,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor,
            fontSize = 15.sp
        )
    }
}
