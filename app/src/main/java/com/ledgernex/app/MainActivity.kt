package com.ledgernex.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.ledgernex.app.ui.navigation.AppNavigation
import com.ledgernex.app.ui.navigation.Screen
import com.ledgernex.app.ui.theme.LedgerNexTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var startDestination by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as LedgerNexApp

        lifecycleScope.launch {
            val done = app.settingsDataStore.isOnboardingDone()
            startDestination = if (done) Screen.Dashboard.route else Screen.Onboarding.route
        }

        setContent {
            LedgerNexTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    startDestination?.let { dest ->
                        AppNavigation(app, startDestination = dest)
                    }
                }
            }
        }
    }
}
