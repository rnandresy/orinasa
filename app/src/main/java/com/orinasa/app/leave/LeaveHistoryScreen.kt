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
import com.orinasa.app.utils.DateUtils
import com.orinasa.app.viewmodel.LeaveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveHistoryScreen(
    vm: LeaveViewModel,
    leaveRemaining: Int,
    onNewRequest: () -> Unit,
    onBack: () -> Unit
) {
    val leaves   by vm.myLeaves.collectAsState()
    val uiState  by vm.uiState.collectAsState()
    val snackbar  = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val msg = uiState.successMessage ?: uiState.errorMessage ?: return@LaunchedEffect
        snackbar.showSnackbar(msg)
        vm.clearMessage()
    }

    Scaffold(
        topBar = {
            OrinasaTopBar(
                title    = "Mes congés",
                subtitle = "Solde restant : $leaveRemaining jour(s)",
                onBack   = onBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text             = { Text("Nouveau congé", fontWeight = FontWeight.SemiBold) },
                icon             = { Icon(Icons.Default.Add, null) },
                onClick          = onNewRequest,
                containerColor   = Primary,
                contentColor     = White,
                shape            = RoundedCornerShape(14.dp)
            )
        },
        snackbarHost   = { OrinasaSnackbarHost(snackbar) },
        containerColor = Surface0
    ) { pad ->
        if (leaves.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon       = Icons.Default.BeachAccess,
                    title      = "Aucune demande de congé",
                    subtitle   = "Vos demandes apparaîtront ici",
                    actionText = "Faire une demande",
                    onAction   = onNewRequest
                )
            }
        } else {
            LazyColumn(
                modifier       = Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("${leaves.size} demande(s)",
                        style = MaterialTheme.typography.bodySmall, color = TextSec)
                }
                items(leaves, key = { it.id }) { leave ->
                    LeaveCard(
                        leave    = leave,
                        onCancel = {
                            if (leave.status == LeaveStatus.PENDING) vm.cancelRequest(leave.id)
                        }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun LeaveCard(leave: LeaveRequest, onCancel: (() -> Unit)? = null, showEmployee: Boolean = false) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        color    = White,
        border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (showEmployee) {
                        Text(leave.userFullName, style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold, color = TextPri)
                    }
                    LeaveTypeChip(type = leave.type)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${DateUtils.formatDate(leave.startDate)} → ${DateUtils.formatDate(leave.endDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPri
                    )
                    Text(
                        "${leave.daysCount} jour(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSec
                    )
                }
                LeaveStatusChip(status = leave.status)
            }

            if (leave.reason.isNotBlank()) {
                Text(
                    leave.reason,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = TextSec,
                    maxLines = 2
                )
            }

            // Note du validateur
            if (leave.reviewNote.isNotBlank()) {
                Surface(
                    color  = when (leave.status) {
                        LeaveStatus.APPROVED -> StatusPresent.copy(0.08f)
                        LeaveStatus.REJECTED -> StatusAbsent.copy(0.08f)
                        else -> Surface1
                    },
                    shape  = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier          = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Comment, null,
                            tint = TextSec, modifier = Modifier.size(14.dp))
                        Text(leave.reviewNote, style = MaterialTheme.typography.bodySmall, color = TextSec)
                    }
                }
            }

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "Demandé le ${DateUtils.formatTimestamp(leave.requestedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                if (onCancel != null && leave.status == LeaveStatus.PENDING) {
                    TextButton(
                        onClick        = onCancel,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Annuler", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}