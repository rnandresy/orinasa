package com.orinasa.app.ui.payslip

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
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
import com.orinasa.app.model.Payslip
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.utils.DateUtils
import com.orinasa.app.viewmodel.PayslipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayslipScreen(
    vm: PayslipViewModel,
    onBack: () -> Unit
) {
    val payslips by vm.myPayslips.collectAsState()

    var selectedPayslip by remember { mutableStateOf<Payslip?>(null) }

    Scaffold(
        topBar = {
            OrinasaTopBar(
                title    = "Mes fiches de paie",
                subtitle = "${payslips.size} bulletin(s)",
                onBack   = onBack
            )
        },
        containerColor = Surface0
    ) { pad ->
        if (payslips.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon     = Icons.Default.RequestPage,
                    title    = "Aucune fiche de paie",
                    subtitle = "Vos bulletins de salaire apparaîtront ici"
                )
            }
        } else {
            LazyColumn(
                modifier       = Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(payslips, key = { it.id }) { payslip ->
                    PayslipCard(
                        payslip = payslip,
                        onClick = { selectedPayslip = payslip }
                    )
                }
                item { Spacer(Modifier.height(60.dp)) }
            }
        }
    }

    selectedPayslip?.let { payslip ->
        PayslipDetailSheet(
            payslip  = payslip,
            onDismiss = { selectedPayslip = null }
        )
    }
}

@Composable
fun PayslipCard(payslip: Payslip, onClick: () -> Unit) {
    Surface(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        color     = White,
        border    = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Primary.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.RequestPage, null, tint = Primary, modifier = Modifier.size(26.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    payslip.period,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = TextPri
                )
                Text(
                    "Net : ${DateUtils.formatAriary(payslip.netSalary)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StatusPresent,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "${payslip.daysWorked} jour(s) travaillé(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSec
                    )
                    if (payslip.isPaid) {
                        Surface(
                            color  = StatusPresent.copy(alpha = 0.1f),
                            shape  = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Payé",
                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style      = MaterialTheme.typography.labelSmall,
                                color      = StatusPresent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayslipDetailSheet(payslip: Payslip, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = White,
        shape            = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Fiche de paie — ${payslip.period}",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = TextPri
                    )
                    Text(
                        payslip.userFullName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSec
                    )
                }
                if (payslip.isPaid) {
                    Surface(
                        color  = StatusPresent.copy(alpha = 0.1f),
                        shape  = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, StatusPresent.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            "✓ Payé",
                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = StatusPresent
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Border)
            Spacer(Modifier.height(14.dp))

            // ── Résumé ────────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PayslipStat("Jours travaillés", "${payslip.daysWorked}", Primary)
                VerticalDivider(modifier = Modifier.height(40.dp))
                PayslipStat("Jours absents", "${payslip.daysAbsent}", StatusAbsent)
                if (payslip.overtimeHours > 0) {
                    VerticalDivider(modifier = Modifier.height(40.dp))
                    PayslipStat("Heures sup.", "${payslip.overtimeHours}h", StatusLate)
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Border)
            Spacer(Modifier.height(14.dp))

            // ── Revenus ───────────────────────────────────────────────────────
            Text("Revenus", style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold, color = TextSec)
            Spacer(Modifier.height(8.dp))
            PayslipRow("Salaire de base",       payslip.baseSalary)
            if (payslip.overtimePay > 0)
                PayslipRow("Heures supplémentaires", payslip.overtimePay)
            if (payslip.bonuses > 0)
                PayslipRow("Primes",                payslip.bonuses)
            if (payslip.allowances > 0)
                PayslipRow("Indemnités",             payslip.allowances)
            Spacer(Modifier.height(6.dp))
            PayslipRow("Salaire brut", payslip.grossSalary, isTotal = true, color = TextPri)

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Border)
            Spacer(Modifier.height(14.dp))

            // ── Déductions ────────────────────────────────────────────────────
            Text("Déductions", style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold, color = TextSec)
            Spacer(Modifier.height(8.dp))
            PayslipRow("CNAPS (1%)",             payslip.cnapsEmployee, negative = true)
            PayslipRow("OSTIE (1%)",             payslip.ostieEmployee, negative = true)
            PayslipRow("IRSA",                   payslip.incomeTax,     negative = true)
            if (payslip.otherDeductions > 0)
                PayslipRow("Autres retenues",    payslip.otherDeductions, negative = true)
            Spacer(Modifier.height(6.dp))
            PayslipRow("Total déductions", payslip.totalDeductions,
                isTotal = true, negative = true, color = StatusAbsent)

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Border, thickness = 2.dp)
            Spacer(Modifier.height(14.dp))

            // ── Net ───────────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "SALAIRE NET",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color      = TextPri
                )
                Text(
                    DateUtils.formatAriary(payslip.netSalary),
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color      = StatusPresent
                )
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Border)
            Spacer(Modifier.height(10.dp))

            // ── Charges patronales ────────────────────────────────────────────
            Text("Charges patronales (information)",
                style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            PayslipRow("CNAPS employeur (13%)", payslip.cnapsEmployer, color = TextMuted)
            PayslipRow("OSTIE employeur (5%)",  payslip.ostieEmployer, color = TextMuted)

            Spacer(Modifier.height(8.dp))
            Text(
                "Généré le ${DateUtils.formatTimestamp(payslip.generatedAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun PayslipRow(
    label: String,
    amount: Double,
    isTotal: Boolean  = false,
    negative: Boolean = false,
    color: androidx.compose.ui.graphics.Color = TextPri
) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = if (isTotal) MaterialTheme.typography.labelLarge
            else MaterialTheme.typography.bodySmall,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color      = if (isTotal) color else TextSec
        )
        Text(
            "${if (negative) "- " else ""}${DateUtils.formatAriary(amount)}",
            style      = if (isTotal) MaterialTheme.typography.labelLarge
            else MaterialTheme.typography.bodySmall,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.SemiBold,
            color      = if (isTotal) color else TextPri
        )
    }
}

@Composable
private fun PayslipStat(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSec)
    }
}