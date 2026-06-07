package com.orinasa.app.ui.attendance

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
import com.orinasa.app.model.User
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.utils.DateUtils
import com.orinasa.app.viewmodel.AttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamAttendanceScreen(
    vm: AttendanceViewModel,
    onBack: () -> Unit
) {
    val teamToday by vm.teamToday.collectAsState()
    val allUsers  by vm.allUsers.collectAsState()
    val uiState   by vm.uiState.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val msg = uiState.successMessage ?: uiState.errorMessage ?: return@LaunchedEffect
        snackbar.showSnackbar(msg)
        vm.clearMessage()
    }

    // Calcule les présents / absents
    val presentIds = teamToday.map { it.userId }.toSet()
    val absentUsers = allUsers.filter { it.userId !in presentIds }

    // Onglets
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Présents (${teamToday.count { it.status != AttendanceStatus.ABSENT }})",
        "Absents (${absentUsers.size})"
    )

    Scaffold(
        topBar = {
            OrinasaTopBar(
                title    = "Présence équipe",
                subtitle = DateUtils.formatDateLong(DateUtils.today()),
                onBack   = onBack
            )
        },
        snackbarHost   = { OrinasaSnackbarHost(snackbar) },
        containerColor = Surface0
    ) { pad ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pad)
        ) {
            // ── Tabs ──────────────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = White
            ) {
                tabs.forEachIndexed { i, tab ->
                    Tab(
                        selected = selectedTab == i,
                        onClick  = { selectedTab = i },
                        text     = {
                            Text(
                                tab,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> PresentList(records = teamToday)
                1 -> AbsentList(
                    users    = absentUsers,
                    onMarkAbsent = { user ->
                        vm.markAbsent(user.userId, user.fullName())
                    }
                )
            }
        }
    }
}

@Composable
private fun PresentList(records: List<Attendance>) {
    if (records.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                icon     = Icons.Default.People,
                title    = "Aucun pointage enregistré",
                subtitle = "Les arrivées apparaîtront ici"
            )
        }
        return
    }

    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "${records.filter { it.status != AttendanceStatus.ABSENT }.size} présent(s) aujourd'hui",
                style = MaterialTheme.typography.bodySmall,
                color = TextSec,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(
            records.filter { it.status != AttendanceStatus.ABSENT }
                .sortedBy { it.clockInTimestamp },
            key = { it.id }
        ) { record ->
            TeamAttendanceRow(record = record)
        }
        item { Spacer(Modifier.height(60.dp)) }
    }
}

@Composable
private fun AbsentList(users: List<User>, onMarkAbsent: (User) -> Unit) {
    if (users.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                icon     = Icons.Default.CheckCircle,
                title    = "Tout le monde est présent !",
                subtitle = "Bonne journée pour l'équipe 🎉"
            )
        }
        return
    }

    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "${users.size} employé(s) sans pointage",
                style = MaterialTheme.typography.bodySmall,
                color = TextSec,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(users, key = { it.userId }) { user ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                color    = White,
                border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
            ) {
                Row(
                    modifier          = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OrinasaAvatar(
                        name     = user.fullName(),
                        photoUrl = user.photoUrl,
                        size     = 42.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.fullName(), style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold, color = TextPri)
                        Text(user.position.ifBlank { user.departmentName },
                            style = MaterialTheme.typography.bodySmall, color = TextSec)
                    }
                    OutlinedButton(
                        onClick        = { onMarkAbsent(user) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape          = RoundedCornerShape(8.dp),
                        border         = androidx.compose.foundation.BorderStroke(1.dp, StatusAbsent.copy(0.5f)),
                        colors         = ButtonDefaults.outlinedButtonColors(contentColor = StatusAbsent)
                    ) {
                        Text("Absent", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(60.dp)) }
    }
}

@Composable
private fun TeamAttendanceRow(record: Attendance) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        color    = White,
        border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OrinasaAvatar(
                name     = record.userFullName,
                photoUrl = record.userPhotoUrl,
                size     = 42.dp
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(record.userFullName, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold, color = TextPri)
                if (record.departmentName.isNotBlank()) {
                    Text(record.departmentName, style = MaterialTheme.typography.bodySmall, color = TextSec)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (record.clockInTime.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.Login, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                            Text(record.clockInTime, style = MaterialTheme.typography.labelSmall, color = TextSec)
                        }
                    }
                    if (record.clockOutTime.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.Logout, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                            Text(record.clockOutTime, style = MaterialTheme.typography.labelSmall, color = TextSec)
                        }
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AttendanceStatusChip(status = record.status)
                if (record.isLate) {
                    Text("+${record.lateMinutes}min", style = MaterialTheme.typography.labelSmall, color = StatusLate)
                }
            }
        }
    }
}