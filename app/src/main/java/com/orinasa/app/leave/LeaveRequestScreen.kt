package com.orinasa.app.ui.leave

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
import com.orinasa.app.model.LeaveType
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.utils.DateUtils
import com.orinasa.app.viewmodel.LeaveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveRequestScreen(
    vm: LeaveViewModel,
    leaveRemaining: Int,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by vm.uiState.collectAsState()

    var selectedType  by remember { mutableStateOf(LeaveType.ANNUAL) }
    var startDate     by remember { mutableStateOf("") }
    var endDate       by remember { mutableStateOf("") }
    var reason        by remember { mutableStateOf("") }
    var showTypePicker by remember { mutableStateOf(false) }

    val daysCount = if (startDate.isNotBlank() && endDate.isNotBlank() && endDate >= startDate)
        DateUtils.daysBetween(startDate, endDate) else 0

    val isValid = startDate.isNotBlank() && endDate.isNotBlank() &&
            endDate >= startDate && reason.isNotBlank() &&
            (selectedType != LeaveType.ANNUAL || daysCount <= leaveRemaining)

    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
            onSuccess()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            OrinasaTopBar(
                title  = "Demande de congé",
                onBack = onBack
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
            // ── Solde disponible ──────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                color    = Primary.copy(alpha = 0.06f),
                border   = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier          = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.BeachAccess, null, tint = Primary, modifier = Modifier.size(20.dp))
                        Text("Solde disponible", style = MaterialTheme.typography.bodyMedium, color = TextSec)
                    }
                    Text(
                        "$leaveRemaining jour(s)",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Primary
                    )
                }
            }

            // ── Type de congé ──────────────────────────────────────────────────
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
                    Text("Type de congé", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = TextPri)

                    OutlinedCard(
                        onClick = { showTypePicker = true },
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
                                Icon(Icons.Default.EventNote, null, tint = TextSec, modifier = Modifier.size(18.dp))
                                Text(selectedType.label, style = MaterialTheme.typography.bodyMedium, color = TextPri)
                            }
                            Icon(Icons.Default.ArrowDropDown, null, tint = TextSec)
                        }
                    }

                    // ── Dates ──────────────────────────────────────────────────
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DatePickerField(
                            label    = "Date de début",
                            value    = startDate,
                            onPick   = { startDate = it },
                            modifier = Modifier.weight(1f)
                        )
                        DatePickerField(
                            label    = "Date de fin",
                            value    = endDate,
                            onPick   = { if (it >= startDate) endDate = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Nombre de jours
                    if (daysCount > 0) {
                        val isOver = selectedType == LeaveType.ANNUAL && daysCount > leaveRemaining
                        Surface(
                            color  = if (isOver) StatusAbsent.copy(0.08f) else StatusPresent.copy(0.08f),
                            shape  = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier          = Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    if (isOver) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    null,
                                    tint     = if (isOver) StatusAbsent else StatusPresent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    if (isOver) "$daysCount jour(s) demandés — Solde insuffisant !"
                                    else        "$daysCount jour(s) de congé",
                                    style  = MaterialTheme.typography.bodySmall,
                                    color  = if (isOver) StatusAbsent else StatusPresent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // ── Motif ─────────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                color    = White,
                border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Motif *", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = TextPri)
                    OrinasaTextField(
                        value         = reason,
                        onValueChange = { if (it.length <= 300) reason = it },
                        label         = "Expliquez brièvement le motif",
                        singleLine    = false,
                        maxLines      = 5
                    )
                    Text("${reason.length}/300",
                        style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }

            OrinasaButton(
                text      = "Soumettre la demande",
                onClick   = {
                    vm.submitRequest(selectedType, startDate, endDate, reason)
                },
                modifier  = Modifier.fillMaxWidth(),
                enabled   = isValid,
                isLoading = uiState.isLoading,
                icon      = Icons.Default.Send
            )

            Spacer(Modifier.height(20.dp))
        }
    }

    // ── Picker type de congé ──────────────────────────────────────────────────
    if (showTypePicker) {
        AlertDialog(
            onDismissRequest = { showTypePicker = false },
            shape            = RoundedCornerShape(16.dp),
            containerColor   = White,
            title            = { Text("Type de congé", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LeaveType.values().forEach { type ->
                        Surface(
                            onClick = { selectedType = type; showTypePicker = false },
                            color   = if (selectedType == type) Primary.copy(0.08f) else White,
                            shape   = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier          = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(type.label, style = MaterialTheme.typography.bodyMedium, color = TextPri)
                                if (selectedType == type)
                                    Icon(Icons.Default.Check, null, tint = Primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showTypePicker = false }) { Text("Fermer") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val state = rememberDatePickerState()

    OutlinedCard(
        onClick  = { showPicker = true },
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSec)
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.CalendarToday, null, tint = TextSec, modifier = Modifier.size(16.dp))
                Text(
                    value.ifBlank { "Choisir" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (value.isBlank()) TextMuted else TextPri,
                    fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.SemiBold
                )
            }
        }
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = ms }
                        onPick("%04d-%02d-%02d".format(
                            cal.get(java.util.Calendar.YEAR),
                            cal.get(java.util.Calendar.MONTH) + 1,
                            cal.get(java.util.Calendar.DAY_OF_MONTH)
                        ))
                    }
                    showPicker = false
                }) { Text("Confirmer") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Annuler") } }
        ) {
            DatePicker(state = state)
        }
    }
}