package com.ledgernex.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ledgernex.app.ui.navigation.AppNavigation
import com.ledgernex.app.ui.navigation.Screen
import com.ledgernex.app.ui.screens.auth.AuthScreen
import com.ledgernex.app.ui.theme.BluePrimary
import com.ledgernex.app.ui.theme.LedgerNexTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as LedgerNexApp

        setContent {
            LedgerNexTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    var startDestination by remember { mutableStateOf<String?>(null) }
                    var showAuth by remember { mutableStateOf(false) }
                    
                    // Monitor auth state
                    val currentUser by app.cloudSyncManager.currentUser.collectAsState()

                    LaunchedEffect(Unit) {
                        val onboardingDone = app.settingsDataStore.isOnboardingDone()
                        val isLoggedIn = app.cloudSyncManager.isLoggedIn()
                        
                        if (!isLoggedIn) {
                            // Pas connecté → toujours montrer auth
                            showAuth = true
                        }
                        
                        startDestination = if (onboardingDone) Screen.Dashboard.route else Screen.Onboarding.route
                    }
                    
                    // Detect logout and return to auth screen
                    LaunchedEffect(currentUser) {
                        if (currentUser == null && !showAuth) {
                            // User logged out, force show auth
                            showAuth = true
                        }
                    }

                    val destination = startDestination
                    when {
                        destination == null -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = BluePrimary)
                            }
                        }
                        showAuth -> {
                            AuthScreen(
                                syncManager = app.cloudSyncManager,
                                onAuthSuccess = { 
                                    showAuth = false
                                    // Lancer sync après connexion
                                },
                                onSkip = {
                                    showAuth = false
                                }
                            )
                        }
                        else -> {
                            AppNavigation(app, startDestination = destination)
                        }
                    }
                }
            }
        }
    }
}
