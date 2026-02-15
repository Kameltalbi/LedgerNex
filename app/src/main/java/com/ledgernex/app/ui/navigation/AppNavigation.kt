package com.ledgernex.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ledgernex.app.LedgerNexApp
import com.ledgernex.app.ui.screens.assets.AssetsScreen
import com.ledgernex.app.ui.screens.bilan.BilanScreen
import com.ledgernex.app.ui.screens.comptes.ComptesScreen
import com.ledgernex.app.ui.screens.dashboard.DashboardScreen
import com.ledgernex.app.ui.screens.onboarding.OnboardingScreen
import com.ledgernex.app.ui.screens.parametres.ParametresScreen
import com.ledgernex.app.ui.screens.resultat.ResultatScreen
import com.ledgernex.app.ui.screens.transactions.TransactionsScreen
import com.ledgernex.app.ui.theme.BackgroundLight
import com.ledgernex.app.ui.theme.BluePrimary
import com.ledgernex.app.ui.theme.OnSurfaceSecondary
import com.ledgernex.app.ui.theme.SurfaceWhite

@Composable
fun AppNavigation(app: LedgerNexApp, startDestination: String = Screen.Dashboard.route) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != Screen.Onboarding.route

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = SurfaceWhite) {
                    val currentDestination = navBackStackEntry?.destination

                    Screen.bottomNavItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BluePrimary,
                            selectedTextColor = BluePrimary,
                            unselectedIconColor = OnSurfaceSecondary,
                            unselectedTextColor = OnSurfaceSecondary
                        )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(app, navController) }
            composable(Screen.Transactions.route) { TransactionsScreen(app) }
            composable(Screen.Resultat.route) { ResultatScreen(app) }
            composable(Screen.Bilan.route) { BilanScreen(app) }
            composable(Screen.Comptes.route) { ComptesScreen(app) }
            composable(Screen.Immobilisations.route) { AssetsScreen(app, navController) }
            composable(Screen.Parametres.route) { ParametresScreen(app, navController) }
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    settingsDataStore = app.settingsDataStore,
                    onFinished = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
