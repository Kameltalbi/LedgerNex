package com.ledgernex.app.ui.screens.assets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.navigation.NavController
import com.ledgernex.app.LedgerNexApp
import com.ledgernex.app.ui.theme.BluePrimary
import com.ledgernex.app.ui.theme.GreenAccent
import com.ledgernex.app.ui.theme.OnSurfaceSecondary
import com.ledgernex.app.ui.theme.RedError
import com.ledgernex.app.ui.viewmodel.AssetDetail
import com.ledgernex.app.ui.util.formatCurrency
import com.ledgernex.app.ui.viewmodel.AssetsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AssetsScreen(app: LedgerNexApp, navController: NavController) {
    val viewModel: AssetsViewModel = viewModel(
        factory = AssetsViewModel.Factory(app.assetRepository)
    )
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val currency by app.settingsDataStore.currency.collectAsState(initial = "")
    val dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BluePrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = BluePrimary)
                }
                Text(
                    text = "Immobilisations",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = BluePrimary
                )
            } else {
                // Total valeur nette
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BluePrimary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total valeur nette", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Text(
                            text = formatCurrency(state.totalValeurNette, currency),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (state.assets.isEmpty()) {
                    Text(
                        text = "Aucune immobilisation",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.assets, key = { it.asset.id }) { detail ->
                            AssetCard(detail, currency, dateFmt) {
                                viewModel.deleteAsset(detail.asset)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAssetDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { nom, dateAchat, montant, duree ->
                viewModel.addAsset(nom, dateAchat, montant, duree)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AssetCard(
    detail: AssetDetail,
    currency: String,
    dateFmt: DateTimeFormatter,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = detail.asset.nom,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Gray)
                }
            }

            Text(
                text = "Achat : ${LocalDate.ofEpochDay(detail.asset.dateAchatEpoch).format(dateFmt)} • ${detail.asset.dureeAmortissement} ans",
                fontSize = 12.sp,
                color = OnSurfaceSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Montant", fontSize = 11.sp, color = OnSurfaceSecondary)
                    Text(formatCurrency(detail.asset.montantTTC, currency), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Amort. annuel", fontSize = 11.sp, color = OnSurfaceSecondary)
                    Text(formatCurrency(detail.amortissementAnnuel, currency), fontWeight = FontWeight.Medium, fontSize = 14.sp, color = RedError)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("VNC", fontSize = 11.sp, color = OnSurfaceSecondary)
                    Text(
                        formatCurrency(detail.valeurNetteComptable, currency),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GreenAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Barre de progression amortissement
            val progress = if (detail.asset.montantTTC > 0) {
                (detail.amortissementCumule / detail.asset.montantTTC).toFloat().coerceIn(0f, 1f)
            } else 0f

            androidx.compose.material3.LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = BluePrimary,
                trackColor = Color.LightGray,
            )
            Text(
                text = "Amorti : ${String.format(Locale.FRANCE, "%.0f", progress * 100)} %",
                fontSize = 11.sp,
                color = OnSurfaceSecondary
            )
        }
    }
}

@Composable
private fun AddAssetDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, LocalDate, Double, Int) -> Unit
) {
    var nom by remember { mutableStateOf("") }
    var montant by remember { mutableStateOf("") }
    var duree by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle immobilisation") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = montant,
                    onValueChange = { montant = it },
                    label = { Text("Montant TTC") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = duree,
                    onValueChange = { duree = it },
                    label = { Text("Durée amortissement (années)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val m = montant.toDoubleOrNull() ?: return@TextButton
                val d = duree.toIntOrNull() ?: return@TextButton
                if (nom.isBlank() || d <= 0) return@TextButton
                onConfirm(nom, LocalDate.now(), m, d)
            }) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
