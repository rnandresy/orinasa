package com.orinasa.app.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.viewmodel.AuthState
import com.orinasa.app.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    vm: AuthViewModel,
    onSuccess: () -> Unit,
    onRegisterCompany: () -> Unit,
    onJoinCompany: () -> Unit
) {
    val state by vm.state.collectAsState()

    var email       by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var showPwd     by remember { mutableStateOf(false) }
    var showReset   by remember { mutableStateOf(false) }
    var resetEmail  by remember { mutableStateOf("") }
    var resetMsg    by remember { mutableStateOf<String?>(null) }

    val isLoading = state is AuthState.Loading
    val error     = (state as? AuthState.Error)?.message

    LaunchedEffect(state) {
        if (state is AuthState.Success) onSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Primary.copy(alpha = 0.06f),
                        Surface0,
                        Surface0
                    )
                )
            )
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // ── Logo ──────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Primary, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Business, null,
                    tint     = White,
                    modifier = Modifier.size(38.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "ORINASA",
                style         = MaterialTheme.typography.displaySmall,
                fontWeight    = FontWeight.Black,
                color         = Primary,
                letterSpacing = 4.sp
            )
            Text(
                "Gestion RH & Pointage",
                style  = MaterialTheme.typography.bodySmall,
                color  = TextSec,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // ── Carte formulaire ──────────────────────────────────────────────
            Surface(
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(20.dp),
                color           = White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier            = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Connexion",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = TextPri
                    )

                    OrinasaTextField(
                        value           = email,
                        onValueChange   = { email = it; vm.resetState() },
                        label           = "Email professionnel",
                        leadingIcon     = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction    = ImeAction.Next
                        )
                    )

                    OrinasaTextField(
                        value            = password,
                        onValueChange    = { password = it; vm.resetState() },
                        label            = "Mot de passe",
                        leadingIcon      = Icons.Default.Lock,
                        isPassword       = true,
                        showPassword     = showPwd,
                        onTogglePassword = { showPwd = !showPwd },
                        keyboardOptions  = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done
                        )
                    )

                    // ── Erreur ────────────────────────────────────────────────
                    AnimatedVisibility(visible = error != null) {
                        Surface(
                            color  = MaterialTheme.colorScheme.errorContainer,
                            shape  = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline, null,
                                    tint     = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    error ?: "",
                                    style    = MaterialTheme.typography.bodySmall,
                                    color    = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Mot de passe oublié
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(
                            onClick        = { showReset = true; resetEmail = email },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                "Mot de passe oublié ?",
                                style = MaterialTheme.typography.labelMedium,
                                color = Primary
                            )
                        }
                    }

                    OrinasaButton(
                        text      = "Se connecter",
                        onClick   = { vm.login(email, password) },
                        modifier  = Modifier.fillMaxWidth(),
                        enabled   = email.isNotBlank() && password.isNotBlank(),
                        isLoading = isLoading,
                        icon      = Icons.Default.Login
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Divider ───────────────────────────────────────────────────────
            LabelDivider("ou")

            Spacer(Modifier.height(16.dp))

            // ── Créer / Rejoindre ──────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OrinasaOutlinedButton(
                    text      = "Créer une entreprise",
                    onClick   = onRegisterCompany,
                    modifier  = Modifier.weight(1f),
                    icon      = Icons.Default.AddBusiness,
                    color     = Primary
                )
                OrinasaOutlinedButton(
                    text     = "Rejoindre",
                    onClick  = onJoinCompany,
                    modifier = Modifier.weight(1f),
                    icon     = Icons.Default.GroupAdd,
                    color    = Secondary
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "Orinasa v1.0 — Solution RH pour Madagascar",
                style     = MaterialTheme.typography.labelSmall,
                color     = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Dialog réinitialisation ───────────────────────────────────────────────
    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false; resetMsg = null },
            shape            = RoundedCornerShape(16.dp),
            containerColor   = White,
            title            = {
                Text("Réinitialiser le mot de passe", fontWeight = FontWeight.Bold, color = TextPri)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Un lien de réinitialisation sera envoyé à votre email.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSec
                    )
                    OrinasaTextField(
                        value           = resetEmail,
                        onValueChange   = { resetEmail = it },
                        label           = "Email",
                        leadingIcon     = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    resetMsg?.let { msg ->
                        Surface(
                            color = if (msg.startsWith("✅"))
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                msg,
                                modifier = Modifier.padding(10.dp),
                                style    = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                OrinasaButton(
                    text    = "Envoyer",
                    onClick = {
                        vm.resetPassword(resetEmail) { ok, err ->
                            resetMsg = if (ok) "✅ Email envoyé !" else "❌ $err"
                        }
                    },
                    enabled = resetEmail.isNotBlank()
                )
            },
            dismissButton = {
                TextButton(onClick = { showReset = false; resetMsg = null }) {
                    Text("Fermer")
                }
            }
        )
    }
}