package com.ledgernex.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ledgernex.app.data.sync.CloudSyncManager
import com.ledgernex.app.ui.theme.BluePrimary
import com.ledgernex.app.ui.theme.RedError
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    syncManager: CloudSyncManager,
    onAuthSuccess: () -> Unit,
    onSkip: () -> Unit // Pour utiliser l'app sans cloud
) {
    val scope = rememberCoroutineScope()
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo/Titre
        Text(
            text = "LedgerNex",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = BluePrimary
        )
        
        Text(
            text = if (isLogin) "Connexion" else "Créer un compte",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        // Champ Email
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                errorMessage = null
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            isError = errorMessage != null
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Champ Mot de passe
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                errorMessage = null
            },
            label = { Text("Mot de passe") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = errorMessage != null,
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                val description = if (passwordVisible) "Masquer" else "Afficher"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(icon, contentDescription = description)
                }
            }
        )

        // Confirmation mot de passe (inscription uniquement)
        if (!isLogin) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    errorMessage = null
                },
                label = { Text("Confirmer le mot de passe") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = errorMessage != null,
                trailingIcon = {
                    val icon = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (confirmPasswordVisible) "Masquer" else "Afficher"
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(icon, contentDescription = description)
                    }
                }
            )
        }

        // Message d'erreur
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = RedError,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bouton principal
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Veuillez remplir tous les champs"
                    return@Button
                }
                
                if (!isLogin && password != confirmPassword) {
                    errorMessage = "Les mots de passe ne correspondent pas"
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    errorMessage = null
                    
                    val result = if (isLogin) {
                        syncManager.signIn(email, password)
                    } else {
                        syncManager.signUp(email, password)
                    }
                    
                    isLoading = false
                    
                    result.onSuccess {
                        onAuthSuccess()
                    }.onFailure { error ->
                        errorMessage = error.message ?: "Erreur"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White)
            } else {
                Text(if (isLogin) "Se connecter" else "S'inscrire")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Switch Login/Register
        TextButton(onClick = { 
            isLogin = !isLogin
            errorMessage = null
            confirmPassword = ""
        }) {
            Text(
                if (isLogin) "Pas de compte ? S'inscrire" 
                else "Déjà un compte ? Se connecter"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Option hors-ligne
        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Utiliser hors-ligne (sans cloud)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Explication
        Text(
            text = "Le mode cloud permet:\n" +
                  "• Sauvegarde automatique\n" +
                  "• Accès multi-appareils\n" +
                  "• Récupération si changement de téléphone",
            fontSize = 12.sp,
            color = androidx.compose.ui.graphics.Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
