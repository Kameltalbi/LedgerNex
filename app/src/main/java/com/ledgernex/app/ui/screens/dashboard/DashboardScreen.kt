package com.ledgernex.app.ui.screens.dashboard

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ledgernex.app.LedgerNexApp
import com.ledgernex.app.ui.theme.BluePrimary
import com.ledgernex.app.ui.theme.GreenAccent
import com.ledgernex.app.ui.theme.RedError
import com.ledgernex.app.ui.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(app: LedgerNexApp) {
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(
            app.accountRepository,
            app.transactionRepository,
            app.assetRepository,
            app.recurrenceManager
        )
    )
    val state by viewModel.state.collectAsState()

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

    val fmt = NumberFormat.getCurrencyInstance(Locale.FRANCE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BluePrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Solde total
        DashboardCard(
            title = "Solde total entreprise",
            value = fmt.format(state.soldeTotalEntreprise),
            valueColor = if (state.soldeTotalEntreprise >= 0) GreenAccent else RedError
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Résultat mois / année
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardCard(
                modifier = Modifier.weight(1f),
                title = "Résultat du mois",
                value = fmt.format(state.resultatMois),
                valueColor = if (state.resultatMois >= 0) GreenAccent else RedError
            )
            DashboardCard(
                modifier = Modifier.weight(1f),
                title = "Résultat annuel",
                value = fmt.format(state.resultatAnnuel),
                valueColor = if (state.resultatAnnuel >= 0) GreenAccent else RedError
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Immobilisations
        DashboardCard(
            title = "Valeur nette immobilisations",
            value = fmt.format(state.valeurImmobilisations),
            valueColor = BluePrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Résumé bilan
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Résumé Bilan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Actif", color = Color.Gray)
                    Text(fmt.format(state.totalActif), fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Graphique résultat mensuel
        if (state.monthlyResults.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Résultat mensuel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SimpleLineChart(
                        data = state.monthlyResults,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DashboardCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun SimpleLineChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxVal = data.maxOf { it.second }.coerceAtLeast(1.0)
    val minVal = data.minOf { it.second }.coerceAtMost(0.0)
    val range = (maxVal - minVal).coerceAtLeast(1.0)

    Canvas(modifier = modifier) {
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val path = Path()

        data.forEachIndexed { index, (_, value) ->
            val x = index * stepX
            val y = size.height - ((value - minVal) / range * size.height).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        // Ligne zéro
        val zeroY = size.height - ((0.0 - minVal) / range * size.height).toFloat()
        drawLine(
            color = Color.LightGray,
            start = Offset(0f, zeroY),
            end = Offset(size.width, zeroY),
            strokeWidth = 1f
        )

        drawPath(
            path = path,
            color = BluePrimary,
            style = Stroke(width = 3f)
        )

        // Points
        data.forEachIndexed { index, (_, value) ->
            val x = index * stepX
            val y = size.height - ((value - minVal) / range * size.height).toFloat()
            drawCircle(
                color = if (value >= 0) GreenAccent else RedError,
                radius = 5f,
                center = Offset(x, y)
            )
        }
    }
}
