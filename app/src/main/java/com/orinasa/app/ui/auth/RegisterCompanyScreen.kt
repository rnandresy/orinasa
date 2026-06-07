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
import androidx.compose.ui.unit.dp
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.utils.COMPANY_SECTORS
import com.orinasa.app.viewmodel.AuthState
import com.orinasa.app.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterCompanyScreen(
    vm: AuthViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()

    // Entreprise
    var companyName   by remember { mutableStateOf("") }
    var sector        by remember { mutableStateOf("") }
    var address       by remember { mutableStateOf("") }
    var companyPhone  by remember { mutableStateOf("") }
    var companyEmail  by remember { mutableStateOf("") }

    // Admin
    var firstName  by remember { mutableStateOf("") }
    var lastName   by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }
    var adminPwd   by remember { mutableStateOf("") }
    var showPwd    by remember { mutableStateOf(false) }

    var currentStep      by remember { mutableIntStateOf(0) } // 0 = entreprise, 1 = admin
    var showSectorPicker by remember { mutableStateOf(false) }

    val isLoading = state is AuthState.Loading
    val error     = (state as? AuthState.Error)?.message

    val step0Valid = companyName.isNotBlank() && sector.isNotBlank()
    val step1Valid = firstName.isNotBlank() && lastName.isNotBlank() &&
            adminEmail.isNotBlank() && adminPwd.length >= 6

    LaunchedEffect(state) {
        if (state is AuthState.Success) onSuccess()
    }

    Scaffold(
        topBar = {
            OrinasaTopBar(
                title    = "Créer mon entreprise",
                subtitle = "Étape ${currentStep + 1} sur 2",
                onBack   = if (currentStep == 0) onBack else ({ currentStep = 0 })
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
            // ── Indicateur étape ──────────────────────────────────────────────
            StepIndicator(currentStep = currentStep, totalSteps = 2)

            Spacer(Modifier.height(4.dp))

            // ── Erreur ────────────────────────────────────────────────────────
            AnimatedVisibility(visible = error != null) {
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(10.dp)) {
                    Row(
                        modifier          = Modifier.fillMaxWidth().padding(12.dp),
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

            if (currentStep == 0) {
                // ── Étape 1 : Informations entreprise ─────────────────────────
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
                        SectionHeader("Informations de l'entreprise", modifier = Modifier)

                        OrinasaTextField(
                            value         = companyName,
                            onValueChange = { companyName = it },
                            label         = "Nom de l'entreprise *",
                            leadingIcon   = Icons.Default.Business,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        // Secteur
                        OutlinedCard(
                            onClick = { showSectorPicker = true },
                            shape   = RoundedCornerShape(12.dp),
                            border  = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier          = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.Category, null,
                                        tint = TextSec, modifier = Modifier.size(20.dp))
                                    Text(
                                        sector.ifBlank { "Secteur d'activité *" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (sector.isBlank()) TextMuted else TextPri
                                    )
                                }
                                Icon(Icons.Default.ArrowDropDown, null, tint = TextSec)
                            }
                        }

                        OrinasaTextField(
                            value         = address,
                            onValueChange = { address = it },
                            label         = "Adresse",
                            leadingIcon   = Icons.Default.LocationOn,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        OrinasaTextField(
                            value           = companyPhone,
                            onValueChange   = { companyPhone = it },
                            label           = "Téléphone",
                            leadingIcon     = Icons.Default.Phone,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction    = ImeAction.Next
                            )
                        )

                        OrinasaTextField(
                            value           = companyEmail,
                            onValueChange   = { companyEmail = it },
                            label           = "Email de l'entreprise",
                            leadingIcon     = Icons.Default.Email,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction    = ImeAction.Done
                            )
                        )
                    }
                }

                OrinasaButton(
                    text     = "Continuer →",
                    onClick  = { currentStep = 1; vm.resetState() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled  = step0Valid,
                    icon     = Icons.Default.ArrowForward
                )

            } else {
                // ── Étape 2 : Compte administrateur ───────────────────────────
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
                        SectionHeader("Votre compte administrateur", modifier = Modifier)

                        Surface(
                            color  = Primary.copy(alpha = 0.06f),
                            shape  = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier          = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, null,
                                    tint = Primary, modifier = Modifier.size(18.dp))
                                Text(
                                    "Vous serez l'administrateur de ${companyName}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Primary
                                )
                            }
                        }

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
                            value           = adminEmail,
                            onValueChange   = { adminEmail = it },
                            label           = "Email *",
                            leadingIcon     = Icons.Default.Email,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction    = ImeAction.Next
                            )
                        )

                        OrinasaTextField(
                            value            = adminPwd,
                            onValueChange    = { adminPwd = it },
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
                    }
                }

                OrinasaButton(
                    text      = "Créer mon entreprise",
                    onClick   = {
                        vm.registerCompany(
                            companyName    = companyName,
                            sector         = sector,
                            address        = address,
                            companyPhone   = companyPhone,
                            companyEmail   = companyEmail,
                            adminFirstName = firstName,
                            adminLastName  = lastName,
                            adminEmail     = adminEmail,
                            adminPassword  = adminPwd
                        )
                    },
                    modifier  = Modifier.fillMaxWidth(),
                    enabled   = step1Valid,
                    isLoading = isLoading,
                    icon      = Icons.Default.CheckCircle
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    // ── Picker secteur ────────────────────────────────────────────────────────
    if (showSectorPicker) {
        AlertDialog(
            onDismissRequest = { showSectorPicker = false },
            shape            = RoundedCornerShape(16.dp),
            containerColor   = White,
            title            = { Text("Secteur d'activité", fontWeight = FontWeight.Bold) },
            text             = {
                Column(
                    modifier            = Modifier.heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    COMPANY_SECTORS.forEach { s ->
                        Surface(
                            onClick = { sector = s; showSectorPicker = false },
                            color   = if (sector == s) Primary.copy(0.08f) else White,
                            shape   = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier          = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(s, style = MaterialTheme.typography.bodyMedium, color = TextPri)
                                if (sector == s)
                                    Icon(Icons.Default.Check, null, tint = Primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSectorPicker = false }) { Text("Fermer") }
            }
        )
    }
}

// ── Indicateur d'étapes ───────────────────────────────────────────────────────
@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { i ->
            val done    = i < currentStep
            val current = i == currentStep
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .weight(1f)
                    .background(
                        when { done || current -> Primary; else -> Surface2 },
                        RoundedCornerShape(2.dp)
                    )
            )
            if (i < totalSteps - 1) Spacer(Modifier.width(6.dp))
        }
    }
}