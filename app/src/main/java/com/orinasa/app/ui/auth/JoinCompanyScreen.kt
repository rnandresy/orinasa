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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orinasa.app.model.Company
import com.orinasa.app.repository.CompanyRepository
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.viewmodel.AuthState
import com.orinasa.app.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun JoinCompanyScreen(
    vm: AuthViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val state  by vm.state.collectAsState()
    val scope   = rememberCoroutineScope()
    val compRepo = remember { CompanyRepository() }

    var inviteCode   by remember { mutableStateOf("") }
    var foundCompany by remember { mutableStateOf<Company?>(null) }
    var codeError    by remember { mutableStateOf<String?>(null) }
    var isVerifying  by remember { mutableStateOf(false) }

    var firstName by remember { mutableStateOf("") }
    var lastName  by remember { mutableStateOf("") }
    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var showPwd   by remember { mutableStateOf(false) }

    val isLoading = state is AuthState.Loading
    val error     = (state as? AuthState.Error)?.message

    LaunchedEffect(state) {
        if (state is AuthState.Success) onSuccess()
    }

    Scaffold(
        topBar = {
            OrinasaTopBar(
                title    = "Rejoindre une entreprise",
                subtitle = "Entrez votre code d'invitation",
                onBack   = onBack
            )
        },
        containerColor = Surface0
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Erreur globale ────────────────────────────────────────────────
            AnimatedVisibility(visible = error != null) {
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, null,
                            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Text(error ?: "", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                    }
                }
            }

            // ── Code d'invitation ─────────────────────────────────────────────
            Surface(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                color     = White,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier            = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Key, null, tint = Primary, modifier = Modifier.size(20.dp))
                        Text(
                            "Code d'invitation",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color      = TextPri
                        )
                    }

                    OutlinedTextField(
                        value         = inviteCode,
                        onValueChange = {
                            inviteCode   = it.uppercase().take(6)
                            foundCompany = null
                            codeError    = null
                            vm.resetState()
                        },
                        label       = { Text("Code à 6 chiffres") },
                        placeholder = { Text("Ex : 482917", color = TextMuted) },
                        singleLine  = true,
                        shape       = RoundedCornerShape(12.dp),
                        modifier    = Modifier.fillMaxWidth(),
                        textStyle   = MaterialTheme.typography.titleMedium.copy(
                            letterSpacing = 6.sp,
                            textAlign     = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction    = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Primary,
                            unfocusedBorderColor = Border
                        )
                    )

                    codeError?.let { err ->
                        Text(err, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error)
                    }

                    // Entreprise trouvée
                    AnimatedVisibility(visible = foundCompany != null) {
                        foundCompany?.let { company ->
                            Surface(
                                color  = StatusPresent.copy(alpha = 0.08f),
                                shape  = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, StatusPresent.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier          = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, null,
                                        tint = StatusPresent, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text(company.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold, color = TextPri)
                                        Text(company.sector,
                                            style = MaterialTheme.typography.bodySmall, color = TextSec)
                                    }
                                }
                            }
                        }
                    }

                    OrinasaButton(
                        text      = if (foundCompany == null) "Vérifier le code" else "Code vérifié ✓",
                        onClick   = {
                            scope.launch {
                                isVerifying  = true
                                codeError    = null
                                foundCompany = null
                                val company  = compRepo.findCompanyByInviteCode(inviteCode)
                                isVerifying  = false
                                if (company != null) foundCompany = company
                                else codeError = "Code invalide ou expiré. Demandez à votre administrateur."
                            }
                        },
                        modifier  = Modifier.fillMaxWidth(),
                        enabled   = inviteCode.length == 6 && foundCompany == null,
                        isLoading = isVerifying,
                        icon      = Icons.Default.Search,
                        containerColor = if (foundCompany != null) StatusPresent else Primary
                    )
                }
            }

            // ── Formulaire inscription ────────────────────────────────────────
            AnimatedVisibility(visible = foundCompany != null) {
                Surface(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    color     = White,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier            = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SectionHeader("Votre compte", modifier = Modifier)

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OrinasaTextField(
                                value         = firstName,
                                onValueChange = { firstName = it },
                                label         = "Prénom *",
                                modifier      = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )
                            OrinasaTextField(
                                value         = lastName,
                                onValueChange = { lastName = it },
                                label         = "Nom *",
                                modifier      = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )
                        }

                        OrinasaTextField(
                            value           = email,
                            onValueChange   = { email = it },
                            label           = "Email *",
                            leadingIcon     = Icons.Default.Email,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction    = ImeAction.Next
                            )
                        )

                        OrinasaTextField(
                            value            = password,
                            onValueChange    = { password = it },
                            label            = "Mot de passe (6 min) *",
                            leadingIcon      = Icons.Default.Lock,
                            isPassword       = true,
                            showPassword     = showPwd,
                            onTogglePassword = { showPwd = !showPwd },
                            keyboardOptions  = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction    = ImeAction.Done
                            )
                        )

                        OrinasaButton(
                            text      = "Rejoindre ${foundCompany?.name ?: ""}",
                            onClick   = {
                                vm.joinCompany(
                                    inviteCode = inviteCode,
                                    firstName  = firstName,
                                    lastName   = lastName,
                                    email      = email,
                                    password   = password
                                )
                            },
                            modifier  = Modifier.fillMaxWidth(),
                            enabled   = firstName.isNotBlank() && lastName.isNotBlank() &&
                                    email.isNotBlank() && password.length >= 6,
                            isLoading = isLoading,
                            icon      = Icons.Default.GroupAdd
                        )
                    }
                }
            }

            // ── Illustration si rien trouvé ───────────────────────────────────
            AnimatedVisibility(visible = foundCompany == null && !isVerifying) {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.QrCode2, null,
                        tint     = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "Demandez le code à 6 chiffres\nà votre responsable RH",
                        style     = MaterialTheme.typography.bodySmall,
                        color     = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}