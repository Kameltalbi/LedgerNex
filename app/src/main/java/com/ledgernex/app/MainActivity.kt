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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ledgernex.app.ui.navigation.AppNavigation
import com.ledgernex.app.ui.navigation.Screen
import com.ledgernex.app.ui.theme.BluePrimary
import com.ledgernex.app.ui.theme.LedgerNexTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as LedgerNexApp

        setContent {
            LedgerNexTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(Unit) {
                        val done = app.settingsDataStore.isOnboardingDone()
                        startDestination = if (done) Screen.Dashboard.route else Screen.Onboarding.route
                    }

                    if (startDestination != null) {
                        AppNavigation(app, startDestination = startDestination!!)
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BluePrimary)
                        }
                    }
                }
            }
        }
    }
}
