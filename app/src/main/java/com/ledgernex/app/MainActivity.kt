package com.ledgernex.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.ledgernex.app.ui.navigation.AppNavigation
import com.ledgernex.app.ui.theme.LedgerNexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as LedgerNexApp

        setContent {
            LedgerNexTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation(app)
                }
            }
        }
    }
}
