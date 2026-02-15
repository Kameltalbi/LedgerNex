package com.ledgernex.app.ui.screens.comptes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.ledgernex.app.data.entity.AccountType
import com.ledgernex.app.ui.theme.BluePrimary
import com.ledgernex.app.ui.theme.GreenAccent
import com.ledgernex.app.ui.theme.OnSurfaceSecondary
import com.ledgernex.app.ui.theme.RedError
import com.ledgernex.app.ui.viewmodel.AccountWithBalance
import com.ledgernex.app.ui.viewmodel.ComptesViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComptesScreen(app: LedgerNexApp) {
    val viewModel: ComptesViewModel = viewModel(
        factory = ComptesViewModel.Factory(app.accountRepository, app.transactionRepository)
    )
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val fmt = NumberFormat.getCurrencyInstance(Locale.FRANCE)

    // Afficher erreur via snackbar
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            Text(
                text = "Comptes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = BluePrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = BluePrimary
                )
            } else if (state.accounts.isEmpty()) {
                Text(
                    text = "Aucun compte créé",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                // Solde total
                val soldeTotal = state.accounts.sumOf { it.solde }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BluePrimary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Solde total", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Text(
                            text = fmt.format(soldeTotal),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.accounts, key = { it.account.id }) { awb ->
                        AccountCard(awb, fmt) { viewModel.deleteAccount(awb.account) }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAccountDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { nom, type, soldeInitial ->
                viewModel.addAccount(nom, type, soldeInitial)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AccountCard(
    awb: AccountWithBalance,
    fmt: NumberFormat,
    onDelete: () -> Unit
) {
    val dateFmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM")

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = awb.account.nom,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${awb.account.type.name} • ${if (awb.account.actif) "Actif" else "Inactif"}",
                        fontSize = 12.sp,
                        color = OnSurfaceSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = fmt.format(awb.solde),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (awb.solde >= 0) GreenAccent else RedError
                    )
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Gray)
                    }
                }
            }

            // Total recettes / dépenses
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Recettes", fontSize = 11.sp, color = OnSurfaceSecondary)
                    Text(fmt.format(awb.totalRecettes), fontSize = 13.sp, color = GreenAccent, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Dépenses", fontSize = 11.sp, color = OnSurfaceSecondary)
                    Text(fmt.format(awb.totalDepenses), fontSize = 13.sp, color = RedError, fontWeight = FontWeight.Medium)
                }
            }

            // Dernières transactions
            if (awb.recentTransactions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Derniers mouvements", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceSecondary)
                awb.recentTransactions.forEach { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${java.time.LocalDate.ofEpochDay(tx.dateEpoch).format(dateFmt)} ${tx.libelle}",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        val isRecette = tx.type == com.ledgernex.app.data.entity.TransactionType.RECETTE
                        Text(
                            text = "${if (isRecette) "+" else "-"}${fmt.format(tx.montantTTC)}",
                            fontSize = 12.sp,
                            color = if (isRecette) GreenAccent else RedError,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, AccountType, Double) -> Unit
) {
    var nom by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AccountType.BANK) }
    var soldeInitial by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau compte") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom du compte") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = type.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        AccountType.values().forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.name) },
                                onClick = {
                                    type = t
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = soldeInitial,
                    onValueChange = { soldeInitial = it },
                    label = { Text("Solde initial") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val solde = soldeInitial.toDoubleOrNull() ?: return@TextButton
                if (nom.isBlank()) return@TextButton
                onConfirm(nom, type, solde)
            }) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
