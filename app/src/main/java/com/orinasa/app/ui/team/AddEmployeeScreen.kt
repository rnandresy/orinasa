package com.orinasa.app.ui.team

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.orinasa.app.model.WorkSchedule
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.utils.CONTRACT_TYPES
import com.orinasa.app.viewmodel.AuthViewModel
import com.orinasa.app.viewmodel.TeamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmployeeScreen(
    authVm: AuthViewModel,
    teamVm: TeamViewModel,
    companyId: String,
    companyName: String,
    inviteCode: String,
    onBack: () -> Unit
) {
    val departments by teamVm.departments.collectAsState()
    val uiState     by teamVm.uiState.collectAsState()

    var firstName    by remember { mutableStateOf("") }
    var lastName     by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var showPwd      by remember { mutableStateOf(false) }
    var position     by remember { mutableStateOf("") }
    var baseSalary   by remember { mutableStateOf("") }
    var contractType by remember { mutableStateOf("CDI") }
    var selectedDept by remember { mutableStateOf("" to "") }

    var showContractPicker by remember { mutableStateOf(false) }
    var showDeptPicker     by remember { mutableStateOf(false) }

    val canSubmit = firstName.isNotBlank() && lastName.isNotBlank() &&
            email.isNotBlank() && password.length >= 6

    val snackbar = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            OrinasaTopBar(
                title    = "Ajouter un employé",
                subtitle = companyName,
                onBack   = onBack
            )
        },
        snackbarHost   = { OrinasaSnackbarHost(snackbar) },
        containerColor = Surface0
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Code invitation ───────────────────────────────────────────────
            Surface(
                color  = Primary.copy(alpha = 0.06f),
                shape  = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier          = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Code d'invitation à partager",
                            style = MaterialTheme.typography.bodySmall, color = TextSec)
                        Text(inviteCode, style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black, color = Primary,
                            letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified)
                    }
                    Icon(Icons.Default.Share, null, tint = Primary)
                }
            }

            LabelDivider("ou créer directement")

            // ── Compte ────────────────────────────────────────────────────────
            EmployeeFormSection("Compte") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OrinasaTextField(value = firstName, onValueChange = { firstName = it },
                        label = "Prénom *", modifier = Modifier.weight(1f))
                    OrinasaTextField(value = lastName, onValueChange = { lastName = it },
                        label = "Nom *", modifier = Modifier.weight(1f))
                }
                OrinasaTextField(value = email, onValueChange = { email = it }, label = "Email *",
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email))
                OrinasaTextField(value = password, onValueChange = { password = it },
                    label = "Mot de passe (6 min) *", isPassword = true,
                    showPassword = showPwd, onTogglePassword = { showPwd = !showPwd },
                    leadingIcon = Icons.Default.Lock)
            }

            // ── Profil pro ────────────────────────────────────────────────────
            EmployeeFormSection("Profil professionnel") {
                OrinasaTextField(value = position, onValueChange = { position = it }, label = "Poste")

                OutlinedCard(onClick = { showDeptPicker = true }, shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedDept.second.ifBlank { "Département (optionnel)" },
                            color = if (selectedDept.second.isBlank()) TextMuted else TextPri,
                            style = MaterialTheme.typography.bodyMedium)
                        Icon(Icons.Default.ArrowDropDown, null, tint = TextSec)
                    }
                }

                OutlinedCard(onClick = { showContractPicker = true }, shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Contrat : $contractType", style = MaterialTheme.typography.bodyMedium, color = TextPri)
                        Icon(Icons.Default.ArrowDropDown, null, tint = TextSec)
                    }
                }

                OrinasaTextField(value = baseSalary, onValueChange = { baseSalary = it },
                    label = "Salaire de base (Ar)",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
            }

            OrinasaButton(
                text      = "Créer l'employé",
                onClick   = {
                    authVm.registerEmployeeByAdmin(
                        email          = email,
                        password       = password,
                        firstName      = firstName,
                        lastName       = lastName,
                        position       = position,
                        departmentId   = selectedDept.first,
                        departmentName = selectedDept.second,
                        baseSalary     = baseSalary.toDoubleOrNull() ?: 0.0,
                        contractType   = contractType,
                        companyId      = companyId,
                        onDone         = { ok, err ->
                            if (ok) onBack()
                        }
                    )
                },
                modifier  = Modifier.fillMaxWidth(),
                enabled   = canSubmit,
                icon      = Icons.Default.PersonAdd
            )

            Spacer(Modifier.height(20.dp))
        }
    }

    if (showContractPicker) {
        AlertDialog(
            onDismissRequest = { showContractPicker = false },
            shape = RoundedCornerShape(16.dp), containerColor = White,
            title = { Text("Type de contrat", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    CONTRACT_TYPES.forEach { ct ->
                        Surface(onClick = { contractType = ct; showContractPicker = false },
                            color = if (contractType == ct) Primary.copy(0.08f) else White,
                            shape = RoundedCornerShape(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(ct, style = MaterialTheme.typography.bodyMedium, color = TextPri)
                                if (contractType == ct) Icon(Icons.Default.Check, null, tint = Primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showContractPicker = false }) { Text("Fermer") } }
        )
    }

    if (showDeptPicker) {
        AlertDialog(
            onDismissRequest = { showDeptPicker = false },
            shape = RoundedCornerShape(16.dp), containerColor = White,
            title = { Text("Département", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    departments.forEach { dept ->
                        Surface(onClick = { selectedDept = dept.id to dept.name; showDeptPicker = false },
                            color = if (selectedDept.first == dept.id) Primary.copy(0.08f) else White,
                            shape = RoundedCornerShape(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(dept.name, style = MaterialTheme.typography.bodyMedium, color = TextPri)
                                if (selectedDept.first == dept.id) Icon(Icons.Default.Check, null, tint = Primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showDeptPicker = false }) { Text("Fermer") } }
        )
    }
}

@Composable
private fun EmployeeFormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = White,
        border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold, color = TextPri)
            HorizontalDivider(color = Border)
            content()
        }
    }
}