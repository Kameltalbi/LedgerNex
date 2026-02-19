package com.ledgernex.app.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.UploadFile
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ledgernex.app.LedgerNexApp
import com.ledgernex.app.data.datastore.SettingsDataStore
import com.ledgernex.app.data.entity.CompanyAccount
import com.ledgernex.app.data.entity.Transaction
import com.ledgernex.app.data.entity.TransactionType
import com.ledgernex.app.ui.theme.BluePrimary
import com.ledgernex.app.ui.theme.GreenAccent
import com.ledgernex.app.ui.theme.RedError
import com.ledgernex.app.ui.util.CsvTransactionImporter
import com.ledgernex.app.ui.util.formatCurrency
import com.ledgernex.app.ui.viewmodel.TransactionsViewModel
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class ImportResult(val successCount: Int, val errorCount: Int, val errors: List<String>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(app: LedgerNexApp) {
    val viewModel: TransactionsViewModel = viewModel(
        factory = TransactionsViewModel.Factory(app.transactionRepository, app.accountRepository)
    )
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showImportResult by remember { mutableStateOf<ImportResult?>(null) }
    val scope = rememberCoroutineScope()
    val currency by app.settingsDataStore.currency.collectAsState(initial = "")

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val context = app.applicationContext
            val (transactions, errors) = CsvTransactionImporter.importFromUri(context, it)
            scope.launch {
                if (transactions.isNotEmpty()) {
                    val (successCount, importErrors) = viewModel.importTransactions(transactions)
                    showImportResult = ImportResult(
                        successCount = successCount,
                        errorCount = errors.size + importErrors.size,
                        errors = errors + importErrors
                    )
                } else {
                    showImportResult = ImportResult(
                        successCount = 0,
                        errorCount = errors.size,
                        errors = errors
                    )
                }
            }
        }
    }

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

            // Bouton Import CSV
            OutlinedButton(
                onClick = { csvLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*")) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = "Import CSV")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Importer CSV")
            }
            Text(
                text = "Astuce: si « Récents » est vide, appuyez sur le menu (≡) en haut à gauche du sélecteur puis choisissez « Téléchargements » pour voir le fichier.",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

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
                        TransactionCard(
                            tx = tx,
                            currency = currency,
                            dateFmt = dateFmt,
                            onEdit = { editingTransaction = tx },
                            onDelete = { viewModel.deleteTransaction(tx) }
                        )
                    }
                }
            }
        }
    }

    // Dialogue ajout transaction
    if (showAddDialog) {
        val categories by app.settingsDataStore.categories.collectAsState(initial = emptySet())
        val accounts by app.accountRepository.getAll().collectAsState(initial = emptyList())

        AddTransactionDialog(
            categories = categories,
            accounts = accounts,
            onDismiss = { showAddDialog = false },
            onConfirm = { type, date, libelle, objet, montant, categorie, accountId ->
                viewModel.addTransaction(type, date, libelle, objet, montant, categorie, accountId)
                showAddDialog = false
            }
        )
    }

    // Dialogue modification transaction (libellé, catégorie, etc.)
    editingTransaction?.let { tx ->
        val categories by app.settingsDataStore.categories.collectAsState(initial = emptySet())
        val accounts by app.accountRepository.getAll().collectAsState(initial = emptyList())
        EditTransactionDialog(
            transaction = tx,
            categories = categories,
            accounts = accounts,
            onDismiss = { editingTransaction = null },
            onConfirm = { updated ->
                viewModel.updateTransaction(updated)
                editingTransaction = null
            }
        )
    }

    // Dialogue résultat import CSV
    showImportResult?.let { result ->
        AlertDialog(
            onDismissRequest = { showImportResult = null },
            title = { Text("Résultat de l'import") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "${result.successCount} transaction(s) importée(s) avec succès",
                        color = GreenAccent,
                        fontWeight = FontWeight.Bold
                    )
                    if (result.errorCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${result.errorCount} ligne(s) ignorée(s) ou en erreur",
                            color = RedError,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Détail (exemples) :", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        for (i in 0 until minOf(result.errors.size, 8)) {
                            Text("• ${result.errors[i]}", fontSize = 11.sp, color = Color.Gray)
                        }
                        if (result.errors.size > 8) {
                            Text("... et ${result.errors.size - 8} autres", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImportResult = null }) {
                    Text("OK", color = BluePrimary)
                }
            }
        )
    }
}

@Composable
private fun TransactionCard(
    tx: Transaction,
    currency: String,
    dateFmt: DateTimeFormatter,
    onEdit: () -> Unit,
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
                    text = "$sign${formatCurrency(tx.montantTTC, currency)}",
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 16.sp
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = BluePrimary)
                    }
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
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddTransactionDialog(
    categories: Set<String>,
    accounts: List<CompanyAccount>,
    onDismiss: () -> Unit,
    onConfirm: (TransactionType, LocalDate, String, String, Double, String, Long) -> Unit
) {
    var type by remember { mutableStateOf(TransactionType.DEPENSE) }
    var libelle by remember { mutableStateOf("") }
    var objet by remember { mutableStateOf("") }
    var montant by remember { mutableStateOf("") }
    var categorie by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var accountExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Nouvelle transaction",
                fontWeight = FontWeight.Bold,
                color = BluePrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- Toggle Recette / Dépense ---
                Text("Type", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bouton Dépense
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (type == TransactionType.DEPENSE) RedError else Color.Transparent
                            )
                            .border(
                                width = 1.dp,
                                color = if (type == TransactionType.DEPENSE) RedError else Color.Gray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { type = TransactionType.DEPENSE },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▼ Dépense",
                            color = if (type == TransactionType.DEPENSE) Color.White else Color.Gray,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                    // Bouton Recette
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (type == TransactionType.RECETTE) GreenAccent else Color.Transparent
                            )
                            .border(
                                width = 1.dp,
                                color = if (type == TransactionType.RECETTE) GreenAccent else Color.Gray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { type = TransactionType.RECETTE },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▲ Recette",
                            color = if (type == TransactionType.RECETTE) Color.White else Color.Gray,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                // --- Libellé ---
                OutlinedTextField(
                    value = libelle,
                    onValueChange = { libelle = it },
                    label = { Text("Libellé") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // --- Objet ---
                OutlinedTextField(
                    value = objet,
                    onValueChange = { objet = it },
                    label = { Text("Objet / Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // --- Montant ---
                OutlinedTextField(
                    value = montant,
                    onValueChange = { montant = it },
                    label = { Text("Montant TTC") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // --- Catégorie (chips) ---
                Text("Catégorie", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                if (categories.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.sorted().forEach { cat ->
                            FilterChip(
                                selected = categorie == cat,
                                onClick = { categorie = cat },
                                label = { Text(cat, fontSize = 12.sp) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BluePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                } else {
                    Text("Aucune catégorie définie", fontSize = 12.sp, color = Color.Gray)
                }
                OutlinedTextField(
                    value = categorie,
                    onValueChange = { categorie = it },
                    label = { Text("Ou saisir une catégorie") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // --- Compte ---
                if (accounts.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = accountExpanded,
                        onExpandedChange = { accountExpanded = !accountExpanded }
                    ) {
                        OutlinedTextField(
                            value = accounts.firstOrNull { it.id == selectedAccountId }?.nom ?: "Sélectionner un compte",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Compte") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = accountExpanded,
                            onDismissRequest = { accountExpanded = false }
                        ) {
                            accounts.forEach { account ->
                                DropdownMenuItem(
                                    text = { Text(account.nom) },
                                    onClick = {
                                        selectedAccountId = account.id
                                        accountExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text("Aucun compte créé. Créez un compte d'abord.", fontSize = 12.sp, color = RedError)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = montant.toDoubleOrNull() ?: return@Button
                    val accId = selectedAccountId ?: accounts.firstOrNull()?.id ?: return@Button
                    if (libelle.isBlank() || categorie.isBlank()) return@Button
                    onConfirm(type, LocalDate.now(), libelle, objet, m, categorie, accId)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == TransactionType.RECETTE) GreenAccent else RedError
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EditTransactionDialog(
    transaction: Transaction,
    categories: Set<String>,
    accounts: List<CompanyAccount>,
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit
) {
    var type by remember(transaction.id) { mutableStateOf(transaction.type) }
    var libelle by remember(transaction.id) { mutableStateOf(transaction.libelle) }
    var objet by remember(transaction.id) { mutableStateOf(transaction.objet) }
    var montant by remember(transaction.id) { mutableStateOf(transaction.montantTTC.toString()) }
    var categorie by remember(transaction.id) { mutableStateOf(transaction.categorie) }
    var selectedAccountId by remember(transaction.id) { mutableStateOf<Long?>(transaction.accountId) }
    var accountExpanded by remember(transaction.id) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Modifier la transaction", fontWeight = FontWeight.Bold, color = BluePrimary)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Type", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (type == TransactionType.DEPENSE) RedError else Color.Transparent)
                            .border(1.dp, if (type == TransactionType.DEPENSE) RedError else Color.Gray, RoundedCornerShape(12.dp))
                            .clickable { type = TransactionType.DEPENSE },
                        contentAlignment = Alignment.Center
                    ) { Text("▼ Dépense", color = if (type == TransactionType.DEPENSE) Color.White else Color.Gray, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (type == TransactionType.RECETTE) GreenAccent else Color.Transparent)
                            .border(1.dp, if (type == TransactionType.RECETTE) GreenAccent else Color.Gray, RoundedCornerShape(12.dp))
                            .clickable { type = TransactionType.RECETTE },
                        contentAlignment = Alignment.Center
                    ) { Text("▲ Recette", color = if (type == TransactionType.RECETTE) Color.White else Color.Gray, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) }
                }
                OutlinedTextField(value = libelle, onValueChange = { libelle = it }, label = { Text("Libellé") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = objet, onValueChange = { objet = it }, label = { Text("Objet / Description") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = montant, onValueChange = { montant = it }, label = { Text("Montant TTC") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                Text("Catégorie", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                if (categories.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        categories.sorted().forEach { cat ->
                            FilterChip(
                                selected = categorie == cat,
                                onClick = { categorie = cat },
                                label = { Text(cat, fontSize = 12.sp) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BluePrimary, selectedLabelColor = Color.White)
                            )
                        }
                    }
                }
                OutlinedTextField(value = categorie, onValueChange = { categorie = it }, label = { Text("Ou saisir une catégorie") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                if (accounts.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = accountExpanded, onExpandedChange = { accountExpanded = !accountExpanded }) {
                        OutlinedTextField(
                            value = accounts.firstOrNull { it.id == selectedAccountId }?.nom ?: "Sélectionner un compte",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Compte") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = accountExpanded, onDismissRequest = { accountExpanded = false }) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text(acc.nom) },
                                    onClick = { selectedAccountId = acc.id; accountExpanded = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = montant.toDoubleOrNull() ?: return@Button
                    val accId = selectedAccountId ?: transaction.accountId
                    if (libelle.isBlank() || categorie.isBlank()) return@Button
                    onConfirm(
                        transaction.copy(
                            type = type,
                            libelle = libelle.trim(),
                            objet = objet.trim(),
                            montantTTC = m,
                            categorie = categorie.trim(),
                            accountId = accId,
                            isModified = true
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
