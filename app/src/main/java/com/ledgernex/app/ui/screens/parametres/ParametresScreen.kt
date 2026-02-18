package com.ledgernex.app.ui.screens.parametres

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Delete
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
import com.ledgernex.app.ui.theme.GreenAccent
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
    val recettesCategories by dataStore.recettesCategories.collectAsState(initial = emptySet())
    val depensesCategories by dataStore.depensesCategories.collectAsState(initial = emptySet())
    val onboardingDone by dataStore.onboardingDone.collectAsState(initial = false)

    var equityInput by remember { mutableStateOf("") }
    var languageExpanded by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<String?>(null) }
    var editingCategoryType by remember { mutableStateOf<com.ledgernex.app.data.entity.TransactionType?>(null) }
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

        // --- Catégories avec Onglets ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Onglets Recettes / Dépenses
                var selectedTab by remember { mutableStateOf(0) }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Onglet Recettes
                    val isRecettesTab = selectedTab == 0
                    val recettesCount = recettesCategories.size
                    
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = if (isRecettesTab) GreenAccent else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedTab = 0 },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💰",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Recettes",
                            color = if (isRecettesTab) Color.White else Color.Gray,
                            fontWeight = if (isRecettesTab) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                        // Badge compteur
                        if (recettesCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isRecettesTab) Color.White.copy(alpha = 0.3f) else GreenAccent.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = recettesCount.toString(),
                                    color = if (isRecettesTab) Color.White else GreenAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    // Onglet Dépenses
                    val isDepensesTab = selectedTab == 1
                    val depensesCount = depensesCategories.size
                    
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = if (isDepensesTab) RedError else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedTab = 1 },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💸",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Dépenses",
                            color = if (isDepensesTab) Color.White else Color.Gray,
                            fontWeight = if (isDepensesTab) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                        // Badge compteur
                        if (depensesCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isDepensesTab) Color.White.copy(alpha = 0.3f) else RedError.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = depensesCount.toString(),
                                    color = if (isDepensesTab) Color.White else RedError,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Contenu selon l'onglet sélectionné
                val currentCategories = if (selectedTab == 0) recettesCategories.sorted() else depensesCategories.sorted()
                val currentColor = if (selectedTab == 0) GreenAccent else RedError
                val isRecettes = selectedTab == 0
                
                if (currentCategories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucune catégorie",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Column {
                        currentCategories.forEach { cat ->
                            var offsetX by remember { mutableStateOf(0f) }
                            val dismissThreshold = -200f
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(vertical = 4.dp)
                            ) {
                                // Background (visible lors du swipe)
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            RedError.copy(alpha = 0.8f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Supprimer",
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Supprimer",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // Card frontale
                                Card(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .offset(x = offsetX.dp)
                                        .clickable {
                                            editingCategory = cat
                                            editingCategoryType = if (isRecettes) 
                                                com.ledgernex.app.data.entity.TransactionType.RECETTE 
                                            else 
                                                com.ledgernex.app.data.entity.TransactionType.DEPENSE
                                            editCategoryInput = cat
                                        }
                                        .pointerInput(Unit) {
                                            detectHorizontalDragGestures(
                                                onDragEnd = {
                                                    if (offsetX < -150) {
                                                        // Supprimer
                                                        scope.launch {
                                                            if (isRecettes) {
                                                                dataStore.removeRecettesCategory(cat)
                                                            } else {
                                                                dataStore.removeDepensesCategory(cat)
                                                            }
                                                        }
                                                    }
                                                    offsetX = 0f
                                                },
                                                onHorizontalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    val newOffset = offsetX + dragAmount
                                                    offsetX = newOffset.coerceIn(-250f, 0f)
                                                }
                                            )
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isRecettes) "💰" else "💸",
                                                fontSize = 20.sp
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = cat,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    editingCategory = cat
                                                    editingCategoryType = if (isRecettes) 
                                                        com.ledgernex.app.data.entity.TransactionType.RECETTE 
                                                    else 
                                                        com.ledgernex.app.data.entity.TransactionType.DEPENSE
                                                    editCategoryInput = cat
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Modifier",
                                                    tint = currentColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Champ d'ajout
                var newCategoryInput by remember { mutableStateOf("") }
                
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
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Text(
                                text = if (isRecettes) "💰" else "💸",
                                fontSize = 18.sp
                            )
                        }
                    )
                    Button(
                        onClick = {
                            val categoryToAdd = newCategoryInput.trim()
                            if (categoryToAdd.isNotBlank()) {
                                scope.launch {
                                    if (isRecettes) {
                                        dataStore.addRecettesCategory(categoryToAdd)
                                    } else {
                                        dataStore.addDepensesCategory(categoryToAdd)
                                    }
                                }
                                newCategoryInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Ajouter")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Déconnexion Cloud ---
        val isLoggedIn = app.cloudSyncManager.isLoggedIn()
        if (isLoggedIn) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Compte Cloud",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Connecté : ${app.cloudSyncManager.currentUser.value?.email ?: ""}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            app.cloudSyncManager.signOut()
                            // Redémarrer l'app pour retourner à l'écran d'auth
                            navController.navigate("dashboard") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedError),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Se déconnecter")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

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
                    val category = editingCategory
                    if (newName.isNotBlank() && category != null && newName != category) {
                        scope.launch {
                            when (editingCategoryType) {
                                com.ledgernex.app.data.entity.TransactionType.RECETTE ->
                                    dataStore.updateRecettesCategory(category, newName)
                                com.ledgernex.app.data.entity.TransactionType.DEPENSE ->
                                    dataStore.updateDepensesCategory(category, newName)
                                else -> {}
                            }
                        }
                    }
                    editingCategory = null
                    editingCategoryType = null
                }) {
                    Text("Enregistrer", color = BluePrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    editingCategory = null 
                    editingCategoryType = null
                }) { Text("Annuler") }
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
