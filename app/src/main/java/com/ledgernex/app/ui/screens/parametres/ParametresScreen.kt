package com.ledgernex.app.ui.screens.parametres

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavController
import com.ledgernex.app.LedgerNexApp
import com.ledgernex.app.data.datastore.SettingsDataStore
import com.ledgernex.app.ui.theme.BluePrimary
import com.ledgernex.app.ui.theme.RedError
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ParametresScreen(app: LedgerNexApp, navController: NavController) {
    val scope = rememberCoroutineScope()
    val dataStore = app.settingsDataStore

    val equityAmount by dataStore.equityAmount.collectAsState(initial = 0.0)
    val currency by dataStore.currency.collectAsState(initial = "")
    val language by dataStore.language.collectAsState(initial = "fr")
    val categories by dataStore.categories.collectAsState(initial = emptySet())
    val onboardingDone by dataStore.onboardingDone.collectAsState(initial = false)

    var equityInput by remember { mutableStateOf("") }
    var newCategoryInput by remember { mutableStateOf("") }
    var languageExpanded by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<String?>(null) }
    var editCategoryInput by remember { mutableStateOf("") }
    var currencyExpanded by remember { mutableStateOf(false) }
    var customCurrencyInput by remember { mutableStateOf("") }
    var showCustomCurrency by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = BluePrimary)
            }
            Text(
                text = "Paramètres",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = BluePrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Capitaux propres ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Capitaux propres",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Valeur actuelle : ${String.format("%.2f", equityAmount)} $currency",
                    fontSize = 14.sp,
                    color = Color.Gray
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
                        label = { Text("Nouveau montant") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            val amount = equityInput.toDoubleOrNull()
                            if (amount != null) {
                                scope.launch { dataStore.setEquityAmount(amount) }
                                equityInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("OK")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Langue ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Langue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                val languageLabel = SettingsDataStore.SUPPORTED_LANGUAGES.firstOrNull { it.first == language }?.second ?: language
                ExposedDropdownMenuBox(
                    expanded = languageExpanded,
                    onExpandedChange = { languageExpanded = !languageExpanded }
                ) {
                    OutlinedTextField(
                        value = languageLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Langue") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = languageExpanded,
                        onDismissRequest = { languageExpanded = false }
                    ) {
                        SettingsDataStore.SUPPORTED_LANGUAGES.forEach { (code, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    scope.launch { dataStore.setLanguage(code) }
                                    languageExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Devise ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Devise",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                var showCustomCurrency by remember { mutableStateOf(false) }
                var customCurrencyInput by remember { mutableStateOf("") }
                var currencyExpanded by remember { mutableStateOf(false) }
                var searchQuery by remember { mutableStateOf("") }
                
                // Filtrer les devises par recherche
                val filteredCurrencies = remember(searchQuery) {
                    if (searchQuery.isBlank()) {
                        SettingsDataStore.SUPPORTED_CURRENCIES
                    } else {
                        SettingsDataStore.SUPPORTED_CURRENCIES.filter { curr ->
                            curr.code.contains(searchQuery, ignoreCase = true) ||
                            curr.name.contains(searchQuery, ignoreCase = true) ||
                            curr.countries.any { it.contains(searchQuery, ignoreCase = true) }
                        }
                    }
                }
                
                if (!showCustomCurrency) {
                    Column {
                        // Champ de sélection avec recherche intégrée
                        OutlinedTextField(
                            value = if (currencyExpanded) searchQuery else currency,
                            onValueChange = { 
                                searchQuery = it
                                if (!currencyExpanded) currencyExpanded = true
                            },
                            label = { Text("Devise") },
                            placeholder = { Text("Rechercher par pays ou devise...") },
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
                                    filteredCurrencies.forEach { curr ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    scope.launch { dataStore.setCurrency(curr.code) }
                                                    currencyExpanded = false
                                                    searchQuery = ""
                                                }
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${curr.code} - ${curr.name}",
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = curr.countries.take(3).joinToString(", "),
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
                                                showCustomCurrency = true
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customCurrencyInput,
                            onValueChange = { customCurrencyInput = it.uppercase().take(10) },
                            label = { Text("Devise personnalisée") },
                            placeholder = { Text("DA, CFA, XBT...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = {
                                if (customCurrencyInput.isNotBlank()) {
                                    scope.launch { dataStore.setCurrency(customCurrencyInput.trim()) }
                                    customCurrencyInput = ""
                                    showCustomCurrency = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("OK")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showCustomCurrency = false }) {
                        Text("← Retour à la liste")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Catégories ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Catégories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.sorted().forEach { cat ->
                        AssistChip(
                            onClick = {
                                editingCategory = cat
                                editCategoryInput = cat
                            },
                            label = { Text(cat, fontSize = 13.sp) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { scope.launch { dataStore.removeCategory(cat) } },
                                    modifier = Modifier.padding(0.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Supprimer",
                                        tint = Color.Gray,
                                        modifier = Modifier.padding(0.dp)
                                    )
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCategoryInput,
                        onValueChange = { newCategoryInput = it },
                        label = { Text("Nouvelle catégorie") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            val categoryToAdd = newCategoryInput.trim()
                            if (categoryToAdd.isNotBlank()) {
                                scope.launch {
                                    dataStore.addCategory(categoryToAdd)
                                }
                                newCategoryInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Ajouter")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajouter")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Reset ---
        Button(
            onClick = { showResetDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = RedError),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Réinitialiser tous les paramètres")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Dialog modifier catégorie
    if (editingCategory != null) {
        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text("Modifier catégorie") },
            text = {
                OutlinedTextField(
                    value = editCategoryInput,
                    onValueChange = { editCategoryInput = it },
                    label = { Text("Nouveau nom") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val newName = editCategoryInput.trim()
                    if (newName.isNotBlank() && newName != editingCategory) {
                        scope.launch {
                            dataStore.updateCategory(editingCategory!!, newName)
                        }
                    }
                    editingCategory = null
                }) {
                    Text("Enregistrer", color = BluePrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCategory = null }) { Text("Annuler") }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Réinitialiser ?") },
            text = { Text("Cette action va réinitialiser les capitaux propres, la devise et les catégories aux valeurs par défaut.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { dataStore.resetAll() }
                    showResetDialog = false
                }) {
                    Text("Confirmer", color = RedError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Annuler") }
            }
        )
    }
}
