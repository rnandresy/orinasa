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
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.utils.DateUtils
import com.orinasa.app.viewmodel.AttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHistoryScreen(
    vm: AttendanceViewModel,
    onBack: () -> Unit
) {
    val history      by vm.myHistory.collectAsState()
    val monthlyStats by vm.monthlyStats.collectAsState()
    val selMonth     by vm.selectedMonth.collectAsState()
    val selYear      by vm.selectedYear.collectAsState()

    var showMonthPicker by remember { mutableStateOf(false) }

    LaunchedEffect(selMonth, selYear) { vm.loadMonthlyStats() }

    Scaffold(
        topBar = {
            OrinasaTopBar(
                title    = "Mon historique",
                subtitle = "Présence et ponctualité",
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
        containerColor = Surface0
    ) { pad ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Résumé du mois ────────────────────────────────────────────────
            item {
                MonthSummaryCard(stats = monthlyStats)
            }

            // ── Historique ────────────────────────────────────────────────────
            if (history.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            icon     = Icons.Default.History,
                            title    = "Aucun enregistrement",
                            subtitle = "Votre historique de pointage apparaîtra ici"
                        )
                    }
                }
            } else {
                item {
                    SectionHeader(
                        title    = "${history.size} enregistrement(s)",
                        modifier = Modifier
                    )
                }
                items(history, key = { it.id }) { record ->
                    AttendanceRecordCard(record = record)
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // ── Picker de mois ────────────────────────────────────────────────────────
    if (showMonthPicker) {
        val months = (1..12).map { m -> DateUtils.monthName(m, selYear) }
        AlertDialog(
            onDismissRequest = { showMonthPicker = false },
            shape            = RoundedCornerShape(16.dp),
            containerColor   = White,
            title = {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = {
                        vm.setMonth(selMonth, selYear - 1)
                        showMonthPicker = false
                    }) { Icon(Icons.Default.ChevronLeft, null) }
                    Text("$selYear", fontWeight = FontWeight.Bold)
                    IconButton(onClick = {
                        vm.setMonth(selMonth, selYear + 1)
                        showMonthPicker = false
                    }) { Icon(Icons.Default.ChevronRight, null) }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    months.chunked(3).forEachIndexed { rowIdx, rowMonths ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowMonths.forEachIndexed { colIdx, month ->
                                val mNum = rowIdx * 3 + colIdx + 1
                                val isSelected = mNum == selMonth
                                Surface(
                                    onClick  = { vm.setMonth(mNum, selYear); showMonthPicker = false },
                                    modifier = Modifier.weight(1f),
                                    color    = if (isSelected) Primary else Surface1,
                                    shape    = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        month.take(3),
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
            confirmButton = {
                TextButton(onClick = { showMonthPicker = false }) { Text("Fermer") }
            }
        )
    }
}

@Composable
private fun MonthSummaryCard(stats: Map<String, Int>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = White,
        border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Résumé du mois",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = TextPri
            )
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem("Présences",   "${stats["present"] ?: 0}",  StatusPresent)
                VerticalDivider(modifier = Modifier.height(40.dp))
                SummaryItem("Absences",   "${stats["absent"] ?: 0}",   StatusAbsent)
                VerticalDivider(modifier = Modifier.height(40.dp))
                SummaryItem("Retards",    "${stats["late"] ?: 0}",     StatusLate)
                VerticalDivider(modifier = Modifier.height(40.dp))
                SummaryItem("Congés",     "${stats["onLeave"] ?: 0}",  StatusLeave)
            }
            val totalMin = stats["totalMinutes"] ?: 0
            if (totalMin > 0) {
                HorizontalDivider(color = Border)
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Timer, null, tint = TextSec, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Total : ${totalMin / 60}h ${totalMin % 60}min travaillées",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSec
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSec)
    }
}

@Composable
private fun AttendanceRecordCard(record: Attendance) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        color    = White,
        border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Date
            Surface(
                color  = Primary.copy(alpha = 0.06f),
                shape  = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier            = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val parts = record.date.split("-")
                    Text(
                        parts.getOrElse(2) { "" },
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color      = Primary
                    )
                    Text(
                        when (parts.getOrElse(1) { "01" }.toIntOrNull() ?: 1) {
                            1  -> "Jan"; 2  -> "Fév"; 3  -> "Mar"; 4  -> "Avr"
                            5  -> "Mai"; 6  -> "Juin"; 7 -> "Juil"; 8 -> "Aoû"
                            9  -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; else -> "Déc"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSec
                    )
                }
            }

            // Détails
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AttendanceStatusChip(status = record.status)
                    if (record.isLate) {
                        Surface(
                            color  = StatusLate.copy(alpha = 0.1f),
                            shape  = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "+${record.lateMinutes}min",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = StatusLate
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (record.clockInTime.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.Login, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                            Text(record.clockInTime, style = MaterialTheme.typography.bodySmall, color = TextSec)
                        }
                    }
                    if (record.clockOutTime.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.Logout, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                            Text(record.clockOutTime, style = MaterialTheme.typography.bodySmall, color = TextSec)
                        }
                    }
                }
            }

            // Durée
            if (record.workDurationMinutes > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${record.workDurationMinutes / 60}h${record.workDurationMinutes % 60}",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = TextPri
                    )
                    Text("travaillé", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }
        }
    }
}