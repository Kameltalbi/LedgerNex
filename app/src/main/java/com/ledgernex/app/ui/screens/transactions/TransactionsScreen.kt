package com.ledgernex.app.ui.screens.transactions

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.ledgernex.app.data.entity.Transaction
import com.ledgernex.app.data.entity.TransactionType
import com.ledgernex.app.ui.theme.BluePrimary
import com.ledgernex.app.ui.theme.GreenAccent
import com.ledgernex.app.ui.theme.RedError
import com.ledgernex.app.ui.viewmodel.TransactionsViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(app: LedgerNexApp) {
    val viewModel: TransactionsViewModel = viewModel(
        factory = TransactionsViewModel.Factory(app.transactionRepository)
    )
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val fmt = NumberFormat.getCurrencyInstance(Locale.FRANCE)
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
            Text(
                text = "Transactions",
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

            Spacer(modifier = Modifier.height(8.dp))

            // Recherche
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Rechercher") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = BluePrimary
                )
            } else if (state.transactions.isEmpty()) {
                Text(
                    text = "Aucune transaction pour cette période",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.transactions, key = { it.id }) { tx ->
                        TransactionCard(tx, fmt, dateFmt) {
                            viewModel.deleteTransaction(tx)
                        }
                    }
                }
            }
        }
    }

    // Dialogue ajout transaction
    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { type, date, libelle, objet, montant, categorie, accountId ->
                viewModel.addTransaction(type, date, libelle, objet, montant, categorie, accountId)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TransactionCard(
    tx: Transaction,
    fmt: NumberFormat,
    dateFmt: DateTimeFormatter,
    onDelete: () -> Unit
) {
    val isRecette = tx.type == TransactionType.RECETTE
    val color = if (isRecette) GreenAccent else RedError
    val sign = if (isRecette) "+" else "-"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.libelle,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${tx.categorie} • ${LocalDate.ofEpochDay(tx.dateEpoch).format(dateFmt)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (tx.objet.isNotBlank()) {
                    Text(text = tx.objet, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$sign${fmt.format(tx.montantTTC)}",
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 16.sp
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (TransactionType, LocalDate, String, String, Double, String, Long) -> Unit
) {
    var type by remember { mutableStateOf(TransactionType.DEPENSE) }
    var libelle by remember { mutableStateOf("") }
    var objet by remember { mutableStateOf("") }
    var montant by remember { mutableStateOf("") }
    var categorie by remember { mutableStateOf("") }
    var accountIdText by remember { mutableStateOf("1") }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Type selector
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
                        TransactionType.values().forEach { t ->
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
                    value = libelle,
                    onValueChange = { libelle = it },
                    label = { Text("Libellé") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = objet,
                    onValueChange = { objet = it },
                    label = { Text("Objet") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = montant,
                    onValueChange = { montant = it },
                    label = { Text("Montant TTC") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = categorie,
                    onValueChange = { categorie = it },
                    label = { Text("Catégorie") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = accountIdText,
                    onValueChange = { accountIdText = it },
                    label = { Text("ID Compte") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val m = montant.toDoubleOrNull() ?: return@TextButton
                val accId = accountIdText.toLongOrNull() ?: return@TextButton
                onConfirm(type, LocalDate.now(), libelle, objet, m, categorie, accId)
            }) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
