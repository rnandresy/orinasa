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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.orinasa.app.model.User
import com.orinasa.app.model.WorkSchedule
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.utils.CONTRACT_TYPES
import com.orinasa.app.utils.DateUtils
import com.orinasa.app.viewmodel.TeamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDetailScreen(
    vm: TeamViewModel,
    user: User,
    isAdmin: Boolean,
    onBack: () -> Unit
) {
    val uiState     by vm.uiState.collectAsState()
    val departments by vm.departments.collectAsState()

    var isEditing     by remember { mutableStateOf(false) }
    var showDeactivate by remember { mutableStateOf(false) }

    var firstName    by remember { mutableStateOf(user.firstName) }
    var lastName     by remember { mutableStateOf(user.lastName) }
    var position     by remember { mutableStateOf(user.position) }
    var phone        by remember { mutableStateOf(user.phone) }
    var baseSalary   by remember { mutableStateOf(user.baseSalary.toString()) }
    var contractType by remember { mutableStateOf(user.contractType) }
    var startDate    by remember { mutableStateOf(user.startDate) }
    var selectedDept by remember { mutableStateOf(user.departmentId to user.departmentName) }
    var startHour    by remember { mutableStateOf(user.workSchedule.startTime) }
    var endHour      by remember { mutableStateOf(user.workSchedule.endTime) }
    var tolerance    by remember { mutableStateOf(user.workSchedule.lateToleranceMinutes.toString()) }

    var showContractPicker by remember { mutableStateOf(false) }
    var showDeptPicker     by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val msg = uiState.successMessage ?: uiState.errorMessage ?: return@LaunchedEffect
        snackbar.showSnackbar(msg)
        vm.clearMessage()
        if (uiState.successMessage != null) isEditing = false
    }

    Scaffold(
        topBar = {
            OrinasaTopBar(
                title  = user.fullName(),
                subtitle = user.position.ifBlank { "Employé" },
                onBack = onBack,
                actions = {
                    if (isAdmin) {
                        if (!isEditing) {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(Icons.Default.Edit, null, tint = Primary)
                            }
                        } else {
                            TextButton(onClick = { isEditing = false }) { Text("Annuler") }
                            TextButton(onClick = {
                                vm.updateUser(
                                    userId         = user.userId,
                                    firstName      = firstName,
                                    lastName       = lastName,
                                    position       = position,
                                    departmentId   = selectedDept.first,
                                    departmentName = selectedDept.second,
                                    baseSalary     = baseSalary.toDoubleOrNull() ?: user.baseSalary,
                                    contractType   = contractType,
                                    workSchedule   = WorkSchedule(
                                        startTime             = startHour,
                                        endTime               = endHour,
                                        lateToleranceMinutes  = tolerance.toIntOrNull() ?: 15
                                    ),
                                    phone     = phone,
                                    startDate = startDate
                                )
                            }) {
                                Text("Sauvegarder", fontWeight = FontWeight.Bold, color = StatusPresent)
                            }
                        }
                    }
                }
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
            // ── Avatar + infos ────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                color    = White,
                border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OrinasaAvatar(name = user.fullName(), photoUrl = user.photoUrl, size = 72.dp, isAdmin = user.isAdmin)
                    Text(user.email, style = MaterialTheme.typography.bodySmall, color = TextSec)
                    Text("Depuis ${DateUtils.formatDate(user.startDate).ifBlank { "—" }}",
                        style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }

            // ── Informations personnelles ──────────────────────────────────────
            EmployeeSection(title = "Informations personnelles") {
                if (isEditing) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OrinasaTextField(value = firstName, onValueChange = { firstName = it },
                            label = "Prénom", modifier = Modifier.weight(1f))
                        OrinasaTextField(value = lastName, onValueChange = { lastName = it },
                            label = "Nom", modifier = Modifier.weight(1f))
                    }
                    OrinasaTextField(value = phone, onValueChange = { phone = it }, label = "Téléphone")
                } else {
                    InfoRow(Icons.Default.Person, "Nom complet", user.fullName())
                    InfoRow(Icons.Default.Phone, "Téléphone", user.phone.ifBlank { "—" })
                }
            }

            // ── Informations professionnelles ─────────────────────────────────
            EmployeeSection(title = "Informations professionnelles") {
                if (isEditing) {
                    OrinasaTextField(value = position, onValueChange = { position = it }, label = "Poste")

                    // Département
                    OutlinedCard(onClick = { showDeptPicker = true }, shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedDept.second.ifBlank { "Département" },
                                style = MaterialTheme.typography.bodyMedium, color = TextPri)
                            Icon(Icons.Default.ArrowDropDown, null, tint = TextSec)
                        }
                    }

                    // Contrat
                    OutlinedCard(onClick = { showContractPicker = true }, shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("Contrat : $contractType",
                                style = MaterialTheme.typography.bodyMedium, color = TextPri)
                            Icon(Icons.Default.ArrowDropDown, null, tint = TextSec)
                        }
                    }

                    OrinasaTextField(value = baseSalary, onValueChange = { baseSalary = it },
                        label = "Salaire de base (Ar)",
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                } else {
                    InfoRow(Icons.Default.Work,          "Poste",       user.position.ifBlank { "—" })
                    InfoRow(Icons.Default.AccountBalance, "Département", user.departmentName.ifBlank { "—" })
                    InfoRow(Icons.Default.Description,   "Contrat",     user.contractType)
                    InfoRow(Icons.Default.Payments,      "Salaire base",DateUtils.formatAriary(user.baseSalary))
                }
            }

            // ── Horaires ──────────────────────────────────────────────────────
            EmployeeSection(title = "Horaires de travail") {
                if (isEditing) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OrinasaTextField(value = startHour, onValueChange = { startHour = it },
                            label = "Arrivée", modifier = Modifier.weight(1f))
                        OrinasaTextField(value = endHour, onValueChange = { endHour = it },
                            label = "Départ", modifier = Modifier.weight(1f))
                    }
                    OrinasaTextField(value = tolerance, onValueChange = { tolerance = it },
                        label = "Tolérance retard (min)",
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                } else {
                    InfoRow(Icons.Default.Schedule, "Horaire",
                        "${user.workSchedule.startTime} — ${user.workSchedule.endTime}")
                    InfoRow(Icons.Default.Timer, "Tolérance retard",
                        "${user.workSchedule.lateToleranceMinutes} minutes")
                }
            }

            // ── Congés ────────────────────────────────────────────────────────
            EmployeeSection(title = "Congés") {
                InfoRow(Icons.Default.BeachAccess, "Solde restant",  "${user.leaveRemaining()} jour(s)")
                InfoRow(Icons.Default.EventBusy,   "Jours utilisés", "${user.leaveUsed} jour(s)")
                InfoRow(Icons.Default.CalendarMonth, "Solde annuel", "${user.leaveBalance} jours")
            }

            // ── Désactiver l'employé ──────────────────────────────────────────
            if (isAdmin && !isEditing) {
                OrinasaOutlinedButton(
                    text     = "Désactiver cet employé",
                    onClick  = { showDeactivate = true },
                    modifier = Modifier.fillMaxWidth(),
                    icon     = Icons.Default.PersonOff,
                    color    = StatusAbsent
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    // ── Dialog désactivation ──────────────────────────────────────────────────
    if (showDeactivate) {
        AlertDialog(
            onDismissRequest = { showDeactivate = false },
            shape            = RoundedCornerShape(16.dp),
            containerColor   = White,
            title = { Text("Désactiver ${user.fullName()} ?", fontWeight = FontWeight.Bold) },
            text = {
                Text("L'employé n'aura plus accès à l'application. Vous pourrez le réactiver plus tard.",
                    style = MaterialTheme.typography.bodySmall, color = TextSec)
            },
            confirmButton = {
                Button(onClick = { vm.deactivateUser(user.userId); showDeactivate = false; onBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusAbsent),
                    shape  = RoundedCornerShape(10.dp)) { Text("Désactiver") }
            },
            dismissButton = { TextButton(onClick = { showDeactivate = false }) { Text("Annuler") } }
        )
    }

    // Pickers
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
private fun EmployeeSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        color    = White,
        border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold, color = TextSec)
            HorizontalDivider(color = Border)
            content()
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSec, modifier = Modifier.width(120.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPri)
    }
}