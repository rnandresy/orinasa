package com.orinasa.app.ui.leave

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.orinasa.app.model.LeaveRequest
import com.orinasa.app.model.LeaveStatus
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.viewmodel.LeaveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveManagementScreen(
    vm: LeaveViewModel,
    onBack: () -> Unit
) {
    val pending  by vm.pendingLeaves.collectAsState()
    val all      by vm.allLeaves.collectAsState()
    val uiState  by vm.uiState.collectAsState()
    val snackbar  = remember { SnackbarHostState() }

    var selectedTab      by remember { mutableIntStateOf(0) }
    var reviewingLeave   by remember { mutableStateOf<LeaveRequest?>(null) }
    var reviewNote       by remember { mutableStateOf("") }
    var isApproving      by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val msg = uiState.successMessage ?: uiState.errorMessage ?: return@LaunchedEffect
        snackbar.showSnackbar(msg)
        vm.clearMessage()
        reviewingLeave = null
        reviewNote     = ""
    }

    Scaffold(
        topBar = {
            OrinasaTopBar(
                title    = "Gestion des congés",
                subtitle = "${pending.size} demande(s) en attente",
                onBack   = onBack
            )
        },
        snackbarHost   = { OrinasaSnackbarHost(snackbar) },
        containerColor = Surface0
    ) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad)) {
            TabRow(selectedTabIndex = selectedTab, containerColor = White) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = { Text("En attente (${pending.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    text = { Text("Toutes (${all.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) })
            }

            val displayList = if (selectedTab == 0) pending else all

            if (displayList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon     = Icons.Default.BeachAccess,
                        title    = if (selectedTab == 0) "Aucune demande en attente" else "Aucune demande",
                        subtitle = if (selectedTab == 0) "Toutes les demandes ont été traitées" else ""
                    )
                }
            } else {
                LazyColumn(
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayList, key = { it.id }) { leave ->
                        LeaveManagementCard(
                            leave       = leave,
                            onApprove   = if (leave.status == LeaveStatus.PENDING) {{
                                reviewingLeave = leave; isApproving = true; reviewNote = ""
                            }} else null,
                            onReject    = if (leave.status == LeaveStatus.PENDING) {{
                                reviewingLeave = leave; isApproving = false; reviewNote = ""
                            }} else null
                        )
                    }
                    item { Spacer(Modifier.height(60.dp)) }
                }
            }
        }
    }

    // ── Dialog validation/refus ───────────────────────────────────────────────
    reviewingLeave?.let { leave ->
        AlertDialog(
            onDismissRequest = { reviewingLeave = null; reviewNote = "" },
            shape            = RoundedCornerShape(16.dp),
            containerColor   = White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        if (isApproving) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        null,
                        tint = if (isApproving) StatusPresent else StatusAbsent
                    )
                    Text(
                        if (isApproving) "Approuver la demande" else "Refuser la demande",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(color = Surface1, shape = RoundedCornerShape(10.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(leave.userFullName, fontWeight = FontWeight.SemiBold, color = TextPri)
                            Text("${leave.type.label} · ${leave.daysCount} jour(s)", color = TextSec,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    OrinasaTextField(
                        value         = reviewNote,
                        onValueChange = { reviewNote = it },
                        label         = if (isApproving) "Note (optionnel)" else "Motif du refus *",
                        singleLine    = false,
                        maxLines      = 3
                    )
                }
            },
            confirmButton = {
                OrinasaButton(
                    text           = if (isApproving) "Approuver" else "Refuser",
                    onClick        = {
                        if (isApproving) vm.approveLeave(leave.id, reviewNote)
                        else vm.rejectLeave(leave.id, reviewNote)
                    },
                    enabled        = isApproving || reviewNote.isNotBlank(),
                    isLoading      = uiState.isLoading,
                    containerColor = if (isApproving) StatusPresent else StatusAbsent
                )
            },
            dismissButton = {
                TextButton(onClick = { reviewingLeave = null }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun LeaveManagementCard(
    leave: LeaveRequest,
    onApprove: (() -> Unit)?,
    onReject: (() -> Unit)?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        color    = White,
        border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OrinasaAvatar(name = leave.userFullName, photoUrl = leave.userPhotoUrl, size = 40.dp)
                    Column {
                        Text(leave.userFullName, style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold, color = TextPri)
                        Text(leave.departmentName, style = MaterialTheme.typography.bodySmall, color = TextSec)
                    }
                }
                LeaveStatusChip(status = leave.status)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LeaveTypeChip(type = leave.type)
                Surface(color = Surface1, shape = RoundedCornerShape(6.dp)) {
                    Text("${leave.daysCount} jour(s)",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style    = MaterialTheme.typography.labelSmall, color = TextSec)
                }
            }

            Text(
                "${com.orinasa.app.utils.DateUtils.formatDate(leave.startDate)} → ${com.orinasa.app.utils.DateUtils.formatDate(leave.endDate)}",
                style = MaterialTheme.typography.bodyMedium, color = TextPri, fontWeight = FontWeight.SemiBold
            )

            if (leave.reason.isNotBlank()) {
                Text(leave.reason, style = MaterialTheme.typography.bodySmall, color = TextSec, maxLines = 2)
            }

            if (onApprove != null && onReject != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick  = onReject,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(10.dp),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, StatusAbsent.copy(0.4f)),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = StatusAbsent)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Refuser", style = MaterialTheme.typography.labelMedium)
                    }
                    Button(
                        onClick  = onApprove,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = StatusPresent),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Approuver", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}