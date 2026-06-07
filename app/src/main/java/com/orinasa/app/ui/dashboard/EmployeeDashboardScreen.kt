package com.orinasa.app.ui.dashboard

import androidx.compose.foundation.background
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
import com.orinasa.app.model.Attendance
import com.orinasa.app.model.AttendanceStatus
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.utils.DateUtils
import com.orinasa.app.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDashboardScreen(
    vm: DashboardViewModel,
    userId: String,
    employeeName: String,
    companyName: String,
    leaveRemaining: Int,
    onGoClockIn: () -> Unit,
    onGoLeaves: () -> Unit,
    onGoPayslips: () -> Unit,
    onGoAnnouncements: () -> Unit
) {
    val myAttendance  by vm.myAttendanceToday.collectAsState()
    val announcements by vm.announcements.collectAsState()
    val myLeaves      by vm.pendingLeaves.collectAsState()

    val hasClockedIn  = myAttendance?.clockInTime?.isNotBlank() == true
    val hasClockedOut = myAttendance?.clockOutTime?.isNotBlank() == true

    Scaffold(
        containerColor = Surface0,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Bonjour, $employeeName 👋",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = TextPri)
                        Text(companyName,
                            style = MaterialTheme.typography.bodySmall, color = TextSec)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { pad ->
        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(pad),
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Carte pointage du jour ─────────────────────────────────────────
            item {
                ClockCard(
                    attendance    = myAttendance,
                    hasClockedIn  = hasClockedIn,
                    hasClockedOut = hasClockedOut,
                    onClockIn     = onGoClockIn,
                    onClockOut    = onGoClockIn
                )
            }

            // ── Stats rapides ─────────────────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title     = "Congés restants",
                        value     = "$leaveRemaining j",
                        subtitle  = "sur l'année",
                        icon      = Icons.Default.BeachAccess,
                        iconColor = StatusLeave,
                        modifier  = Modifier.weight(1f),
                        onClick   = onGoLeaves
                    )
                    StatCard(
                        title     = "Fiches de paie",
                        value     = "Voir",
                        icon      = Icons.Default.RequestPage,
                        iconColor = StatusPresent,
                        modifier  = Modifier.weight(1f),
                        onClick   = onGoPayslips
                    )
                }
            }

            // ── Raccourcis ────────────────────────────────────────────────────
            item {
                SectionHeader(title = "Actions", modifier = Modifier)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoCard(
                        icon     = Icons.Default.BeachAccess,
                        title    = "Demander un congé",
                        subtitle = "Solde restant : $leaveRemaining jour(s)",
                        iconBg   = StatusLeave.copy(alpha = 0.1f),
                        iconTint = StatusLeave,
                        onClick  = onGoLeaves
                    )
                    InfoCard(
                        icon     = Icons.Default.RequestPage,
                        title    = "Mes fiches de paie",
                        subtitle = "Consulter mes bulletins de salaire",
                        iconBg   = StatusPresent.copy(alpha = 0.1f),
                        iconTint = StatusPresent,
                        onClick  = onGoPayslips
                    )
                    InfoCard(
                        icon     = Icons.Default.Campaign,
                        title    = "Annonces de l'entreprise",
                        subtitle = "${announcements.size} annonce(s)",
                        iconBg   = PriorityImportant.copy(alpha = 0.1f),
                        iconTint = PriorityImportant,
                        onClick  = onGoAnnouncements
                    )
                }
            }

            // ── Annonces récentes ─────────────────────────────────────────────
            if (announcements.isNotEmpty()) {
                item {
                    SectionHeader(title = "Annonces", action = "Tout voir", onAction = onGoAnnouncements, modifier = Modifier)
                }
                items(announcements.take(2)) { ann ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        color    = White,
                        border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(ann.title, style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold, color = TextPri)
                            Text(ann.content, style = MaterialTheme.typography.bodySmall,
                                color = TextSec, maxLines = 2)
                            Text(DateUtils.formatTimestamp(ann.timestamp),
                                style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ── Carte pointage ────────────────────────────────────────────────────────────
@Composable
private fun ClockCard(
    attendance: Attendance?,
    hasClockedIn: Boolean,
    hasClockedOut: Boolean,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = Primary
    ) {
        Column(
            modifier            = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column {
                    Text(
                        DateUtils.formatDateLong(DateUtils.today()),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = White
                    )
                    Text(
                        "Pointage du jour",
                        style = MaterialTheme.typography.bodySmall,
                        color = White.copy(alpha = 0.7f)
                    )
                }
                attendance?.let {
                    AttendanceStatusChip(status = it.status)
                }
            }

            // Affichage heures
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimeBox(
                    label = "Arrivée",
                    time  = attendance?.clockInTime ?: "--:--",
                    icon  = Icons.Default.Login,
                    done  = hasClockedIn,
                    modifier = Modifier.weight(1f)
                )
                TimeBox(
                    label = "Départ",
                    time  = attendance?.clockOutTime ?: "--:--",
                    icon  = Icons.Default.Logout,
                    done  = hasClockedOut,
                    modifier = Modifier.weight(1f)
                )
            }

            // Durée si complet
            if (hasClockedOut && (attendance?.workDurationMinutes ?: 0) > 0) {
                val dur = attendance!!.workDurationMinutes
                Surface(color = White.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        "Durée travaillée : ${dur / 60}h${dur % 60}min",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style    = MaterialTheme.typography.labelMedium,
                        color    = White
                    )
                }
            }

            // Bouton action
            when {
                !hasClockedIn -> OrinasaButton(
                    text           = "Pointer mon arrivée",
                    onClick        = onClockIn,
                    modifier       = Modifier.fillMaxWidth(),
                    icon           = Icons.Default.Login,
                    containerColor = White,
                    contentColor   = Primary
                )
                !hasClockedOut -> OrinasaButton(
                    text           = "Pointer ma sortie",
                    onClick        = onClockOut,
                    modifier       = Modifier.fillMaxWidth(),
                    icon           = Icons.Default.Logout,
                    containerColor = White.copy(alpha = 0.15f),
                    contentColor   = White
                )
                else -> Surface(
                    color    = White.copy(alpha = 0.12f),
                    shape    = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier          = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null,
                            tint = White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Journée complétée !",
                            style = MaterialTheme.typography.labelMedium, color = White)
                    }
                }
            }

            if (hasClockedIn && attendance?.isLate == true) {
                Surface(
                    color = StatusLate.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "⏰ Retard de ${attendance.lateMinutes} min",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = StatusLate
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeBox(
    label: String,
    time: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    done: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color    = White.copy(alpha = if (done) 0.18f else 0.08f),
        shape    = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier            = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = White.copy(alpha = 0.7f))
            Text(
                time,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = White
            )
        }
    }
}