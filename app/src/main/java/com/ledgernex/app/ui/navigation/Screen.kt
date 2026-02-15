package com.ledgernex.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Transactions : Screen("transactions", "Transactions", Icons.Default.Receipt)
    object Resultat : Screen("resultat", "Résultat", Icons.Default.Assessment)
    object Bilan : Screen("bilan", "Bilan", Icons.Default.AccountBalance)
    object Comptes : Screen("comptes", "Comptes", Icons.Default.Wallet)
    object Immobilisations : Screen("immobilisations", "Immobilisations", Icons.Default.Business)
    object Parametres : Screen("parametres", "Paramètres", Icons.Default.Settings)
    object Onboarding : Screen("onboarding", "Bienvenue", Icons.Default.Settings)

    companion object {
        val bottomNavItems: List<Screen> by lazy {
            listOf(Dashboard, Transactions, Resultat, Bilan, Comptes)
        }
    }
}
