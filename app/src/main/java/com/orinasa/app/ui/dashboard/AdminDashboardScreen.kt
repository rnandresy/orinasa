package com.orinasa.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.orinasa.app.model.Announcement
import com.orinasa.app.model.AnnouncementPriority
import com.orinasa.app.model.LeaveRequest
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.utils.DateUtils
import com.orinasa.app.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    vm: DashboardViewModel,
    companyName: String,
    adminName: String,
    onGoAttendance: () -> Unit,
    onGoLeaves: () -> Unit,
    onGoPayslips: () -> Unit,
    onGoTeam: () -> Unit,
    onGoAnnouncements: () -> Unit
) {
    val stats         by vm.stats.collectAsState()
    val pendingLeaves by vm.pendingLeaves.collectAsState()
    val announcements by vm.announcements.collectAsState()

    Scaffold(
        containerColor = Surface0,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Bonjour, $adminName 👋",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = TextPri
                        )
                        Text(
                            companyName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSec
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onGoAnnouncements) {
                        Icon(Icons.Default.Campaign, null, tint = TextSec)
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
            // ── Date du jour ──────────────────────────────────────────────────
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    color    = Primary
                ) {
                    Row(
                        modifier          = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                DateUtils.formatDateLong(DateUtils.today()),
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color      = White
                            )
                            Text(
                                "Rapport journalier",
                                style = MaterialTheme.typography.bodySmall,
                                color = White.copy(alpha = 0.7f)
                            )
                        }
                        Icon(Icons.Default.CalendarToday, null, tint = White.copy(alpha = 0.8f))
                    }
                }
            }

            // ── Stats du jour ─────────────────────────────────────────────────
            item {
                SectionHeader(title = "Présence aujourd'hui", modifier = Modifier)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title     = "Présents",
                        value     = "${stats.presentToday}",
                        subtitle  = "sur ${stats.totalEmployees}",
                        icon      = Icons.Default.CheckCircle,
                        iconColor = StatusPresent,
                        modifier  = Modifier.weight(1f),
                        onClick   = onGoAttendance
                    )
                    StatCard(
                        title     = "Absents",
                        value     = "${stats.absentToday}",
                        icon      = Icons.Default.Cancel,
                        iconColor = StatusAbsent,
                        modifier  = Modifier.weight(1f),
                        onClick   = onGoAttendance
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title     = "En retard",
                        value     = "${stats.lateToday}",
                        icon      = Icons.Default.Schedule,
                        iconColor = StatusLate,
                        modifier  = Modifier.weight(1f),
                        onClick   = onGoAttendance
                    )
                    StatCard(
                        title     = "En congé",
                        value     = "${stats.onLeaveToday}",
                        icon      = Icons.Default.BeachAccess,
                        iconColor = StatusLeave,
                        modifier  = Modifier.weight(1f),
                        onClick   = onGoLeaves
                    )
                }
            }

            // ── Congés en attente ─────────────────────────────────────────────
            if (pendingLeaves.isNotEmpty()) {
                item {
                    SectionHeader(
                        title    = "Congés en attente",
                        action   = "Tout voir",
                        onAction = onGoLeaves,
                        modifier = Modifier
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        color    = StatusLate.copy(alpha = 0.06f),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, StatusLate.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(4.dp)) {
                            pendingLeaves.take(3).forEach { leave ->
                                PendingLeaveRow(leave = leave, onClick = onGoLeaves)
                                if (leave != pendingLeaves.take(3).last()) {
                                    HorizontalDivider(
                                        color     = StatusLate.copy(alpha = 0.15f),
                                        modifier  = Modifier.padding(horizontal = 12.dp),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                            if (pendingLeaves.size > 3) {
                                TextButton(
                                    onClick  = onGoLeaves,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "${pendingLeaves.size - 3} autres demandes",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = StatusLate
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Raccourcis ────────────────────────────────────────────────────
            item {
                SectionHeader(title = "Actions rapides", modifier = Modifier)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            icon    = Icons.Default.People,
                            label   = "Équipe",
                            color   = Secondary,
                            onClick = onGoTeam,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            icon    = Icons.Default.AccessTime,
                            label   = "Pointage",
                            color   = Accent,
                            onClick = onGoAttendance,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            icon    = Icons.Default.RequestPage,
                            label   = "Fiches de paie",
                            color   = StatusPresent,
                            onClick = onGoPayslips,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            icon    = Icons.Default.Campaign,
                            label   = "Annonces",
                            color   = PriorityImportant,
                            onClick = onGoAnnouncements,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Annonces récentes ─────────────────────────────────────────────
            if (announcements.isNotEmpty()) {
                item {
                    SectionHeader(
                        title    = "Annonces récentes",
                        action   = "Voir tout",
                        onAction = onGoAnnouncements,
                        modifier = Modifier
                    )
                }
                items(announcements.take(2)) { ann ->
                    AnnouncementRow(ann = ann)
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun PendingLeaveRow(leave: LeaveRequest, onClick: () -> Unit) {
    Row(
        modifier          = Modifier.fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OrinasaAvatar(name = leave.userFullName, size = 36.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                leave.userFullName,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = TextPri
            )
            Text(
                "${leave.type.label} · ${leave.daysCount} jour(s)",
                style = MaterialTheme.typography.bodySmall,
                color = TextSec
            )
        }
        LeaveStatusChip(status = leave.status)
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick   = onClick,
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        color     = White,
        border    = androidx.compose.foundation.BorderStroke(1.dp, Border),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Text(label, style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold, color = TextPri)
        }
    }
}

@Composable
private fun AnnouncementRow(ann: Announcement) {
    val priorityColor = when (ann.priority) {
        AnnouncementPriority.URGENT    -> PriorityUrgent
        AnnouncementPriority.IMPORTANT -> PriorityImportant
        AnnouncementPriority.NORMAL    -> PriorityNormal
    }
    Surface(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        color     = White,
        border    = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(y = 6.dp)
                    .background(priorityColor, RoundedCornerShape(4.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(ann.title, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold, color = TextPri, maxLines = 1)
                Text(ann.content, style = MaterialTheme.typography.bodySmall,
                    color = TextSec, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Text(DateUtils.formatTimestamp(ann.timestamp),
                    style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}