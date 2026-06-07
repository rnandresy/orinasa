package com.orinasa.app.ui.payslip

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.orinasa.app.model.Payslip
import com.orinasa.app.model.User
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.utils.DateUtils
import com.orinasa.app.viewmodel.PayslipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayslipGeneratorScreen(
    vm: PayslipViewModel,
    allUsers: List<User>,
    onBack: () -> Unit
) {
    val uiState       by vm.uiState.collectAsState()
    val monthPayslips by vm.monthPayslips.collectAsState()
    val selMonth      by vm.selectedMonth.collectAsState()
    val selYear       by vm.selectedYear.collectAsState()

    var selectedUser      by remember { mutableStateOf<User?>(null) }
    var showUserPicker    by remember { mutableStateOf(false) }
    var showMonthPicker   by remember { mutableStateOf(false) }

    // Champs personnalisés
    var bonuses        by remember { mutableStateOf("") }
    var allowances     by remember { mutableStateOf("") }
    var deductions     by remember { mutableStateOf("") }
    var overtimeHours  by remember { mutableStateOf("") }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val msg = uiState.successMessage ?: uiState.errorMessage ?: return@LaunchedEffect
        snackbar.showSnackbar(msg)
        vm.clearMessage()
        if (uiState.successMessage != null) selectedUser = null
    }

    // IDs déjà générés ce mois
    val generatedUserIds = monthPayslips.map { it.userId }.toSet()

    Scaffold(
        topBar = {
            OrinasaTopBar(
                title    = "Génération des fiches",
                subtitle = DateUtils.monthName(selMonth, selYear),
                onBack   = onBack,
                actions  = {
                    TextButton(onClick = { showMonthPicker = true }) {
                        Text(
                            DateUtils.monthName(selMonth, selYear),
                            style = MaterialTheme.typography.labelMedium,
                            color = Primary
                        )
                        Icon(Icons.Default.ArrowDropDown, null, tint = Primary)
                    }
                }
            )
        },
        snackbarHost   = { OrinasaSnackbarHost(snackbar) },
        containerColor = Surface0
    ) { pad ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Résumé du mois ────────────────────────────────────────────────
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                    color    = White,
                    border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Row(
                        modifier          = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Fiches générées",
                                style = MaterialTheme.typography.bodySmall, color = TextSec)
                            Text("${generatedUserIds.size} / ${allUsers.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold, color = TextPri)
                        }
                        OrinasaButton(
                            text      = "Tout générer",
                            onClick   = {
                                val pending = allUsers.filter { it.userId !in generatedUserIds }
                                vm.generateAllPayslips(pending)
                            },
                            enabled   = allUsers.any { it.userId !in generatedUserIds },
                            isLoading = uiState.isLoading,
                            icon      = Icons.Default.AutoAwesome
                        )
                    }
                }
            }

            // ── Génération individuelle ───────────────────────────────────────
            item {
                SectionHeader("Génération individuelle", modifier = Modifier)
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    color    = White,
                    border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Column(
                        modifier            = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Sélecteur employé
                        OutlinedCard(
                            onClick = { showUserPicker = true },
                            shape   = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier          = Modifier.fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    if (selectedUser != null) {
                                        OrinasaAvatar(
                                            name     = selectedUser!!.fullName(),
                                            photoUrl = selectedUser!!.photoUrl,
                                            size     = 32.dp
                                        )
                                    } else {
                                        Icon(Icons.Default.Person, null, tint = TextSec)
                                    }
                                    Text(
                                        selectedUser?.fullName() ?: "Sélectionner un employé",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (selectedUser == null) TextMuted else TextPri
                                    )
                                }
                                Icon(Icons.Default.ArrowDropDown, null, tint = TextSec)
                            }
                        }

                        if (selectedUser != null) {
                            val alreadyGenerated = selectedUser!!.userId in generatedUserIds
                            if (alreadyGenerated) {
                                Surface(
                                    color  = StatusLate.copy(alpha = 0.08f),
                                    shape  = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier          = Modifier.fillMaxWidth().padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Info, null,
                                            tint = StatusLate, modifier = Modifier.size(16.dp))
                                        Text(
                                            "Une fiche existe déjà pour ce mois",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = StatusLate
                                        )
                                    }
                                }
                            } else {
                                // Infos employé
                                Surface(color = Surface1, shape = RoundedCornerShape(10.dp)) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(selectedUser!!.position.ifBlank { "—" },
                                            style = MaterialTheme.typography.bodySmall, color = TextSec)
                                        Text("Salaire de base : ${DateUtils.formatAriary(selectedUser!!.baseSalary)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold, color = TextPri)
                                    }
                                }

                                // Champs bonus / indemnités
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OrinasaTextField(
                                        value           = bonuses,
                                        onValueChange   = { bonuses = it },
                                        label           = "Primes (Ar)",
                                        modifier        = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OrinasaTextField(
                                        value           = allowances,
                                        onValueChange   = { allowances = it },
                                        label           = "Indemnités (Ar)",
                                        modifier        = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OrinasaTextField(
                                        value           = overtimeHours,
                                        onValueChange   = { overtimeHours = it },
                                        label           = "Heures sup.",
                                        modifier        = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OrinasaTextField(
                                        value           = deductions,
                                        onValueChange   = { deductions = it },
                                        label           = "Retenues (Ar)",
                                        modifier        = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                                OrinasaButton(
                                    text      = "Générer la fiche",
                                    onClick   = {
                                        selectedUser?.let { user ->
                                            vm.generatePayslip(
                                                user            = user,
                                                bonuses         = bonuses.toDoubleOrNull() ?: 0.0,
                                                allowances      = allowances.toDoubleOrNull() ?: 0.0,
                                                otherDeductions = deductions.toDoubleOrNull() ?: 0.0,
                                                overtimeHours   = overtimeHours.toDoubleOrNull() ?: 0.0
                                            )
                                        }
                                    },
                                    modifier  = Modifier.fillMaxWidth(),
                                    enabled   = !alreadyGenerated,
                                    isLoading = uiState.isLoading,
                                    icon      = Icons.Default.RequestPage
                                )
                            }
                        }
                    }
                }
            }

            // ── Fiches déjà générées ──────────────────────────────────────────
            if (monthPayslips.isNotEmpty()) {
                item {
                    SectionHeader(
                        title    = "Fiches du mois (${monthPayslips.size})",
                        modifier = Modifier
                    )
                }
                items(monthPayslips, key = { it.id }) { payslip ->
                    GeneratedPayslipRow(payslip = payslip, onMarkPaid = { vm.markAsPaid(payslip.id) })
                }
            }

            item { Spacer(Modifier.height(60.dp)) }
        }
    }

    // ── Picker employé ────────────────────────────────────────────────────────
    if (showUserPicker) {
        AlertDialog(
            onDismissRequest = { showUserPicker = false },
            shape            = RoundedCornerShape(16.dp),
            containerColor   = White,
            title            = { Text("Sélectionner un employé", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    allUsers.forEach { user ->
                        val isGenerated = user.userId in generatedUserIds
                        Surface(
                            onClick = { selectedUser = user; showUserPicker = false },
                            color   = if (selectedUser?.userId == user.userId) Primary.copy(0.06f) else White,
                            shape   = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier          = Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OrinasaAvatar(name = user.fullName(), photoUrl = user.photoUrl, size = 36.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.fullName(), style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium, color = TextPri)
                                    Text(user.position.ifBlank { user.departmentName },
                                        style = MaterialTheme.typography.bodySmall, color = TextSec)
                                }
                                if (isGenerated) {
                                    Icon(Icons.Default.CheckCircle, null,
                                        tint = StatusPresent, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showUserPicker = false }) { Text("Fermer") } }
        )
    }

    // ── Picker mois ───────────────────────────────────────────────────────────
    if (showMonthPicker) {
        AlertDialog(
            onDismissRequest = { showMonthPicker = false },
            shape            = RoundedCornerShape(16.dp),
            containerColor   = White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { vm.setMonth(selMonth, selYear - 1); showMonthPicker = false }) {
                        Icon(Icons.Default.ChevronLeft, null)
                    }
                    Text("$selYear", fontWeight = FontWeight.Bold)
                    IconButton(onClick = { vm.setMonth(selMonth, selYear + 1); showMonthPicker = false }) {
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..12).chunked(3).forEachIndexed { rowIdx, row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { mNum ->
                                val isSelected = mNum == selMonth
                                Surface(
                                    onClick  = { vm.setMonth(mNum, selYear); showMonthPicker = false },
                                    modifier = Modifier.weight(1f),
                                    color    = if (isSelected) Primary else Surface1,
                                    shape    = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        DateUtils.monthName(mNum, selYear).take(3),
                                        modifier  = Modifier.padding(vertical = 10.dp),
                                        style     = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color     = if (isSelected) White else TextSec,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showMonthPicker = false }) { Text("Fermer") } }
        )
    }
}

@Composable
private fun GeneratedPayslipRow(payslip: Payslip, onMarkPaid: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        color    = White,
        border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OrinasaAvatar(name = payslip.userFullName, size = 36.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(payslip.userFullName, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold, color = TextPri)
                Text("Net : ${DateUtils.formatAriary(payslip.netSalary)}",
                    style = MaterialTheme.typography.bodySmall, color = StatusPresent,
                    fontWeight = FontWeight.SemiBold)
            }
            if (!payslip.isPaid) {
                OutlinedButton(
                    onClick        = onMarkPaid,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape          = RoundedCornerShape(8.dp),
                    border         = androidx.compose.foundation.BorderStroke(1.dp, StatusPresent.copy(0.4f)),
                    colors         = ButtonDefaults.outlinedButtonColors(contentColor = StatusPresent)
                ) {
                    Text("Marquer payé", style = MaterialTheme.typography.labelSmall)
                }
            } else {
                Icon(Icons.Default.CheckCircle, null, tint = StatusPresent, modifier = Modifier.size(22.dp))
            }
        }
    }
}
