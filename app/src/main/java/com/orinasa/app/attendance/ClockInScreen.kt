package com.orinasa.app.ui.attendance

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.utils.DateUtils
import com.orinasa.app.viewmodel.AttendanceViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockInScreen(
    vm: AttendanceViewModel,
    onBack: () -> Unit
) {
    val uiState      by vm.uiState.collectAsState()
    val todayRecord  by vm.todayAttendance.collectAsState()

    val hasClockedIn  = todayRecord?.clockInTime?.isNotBlank() == true
    val hasClockedOut = todayRecord?.clockOutTime?.isNotBlank() == true

    // Horloge temps réel
    var currentTime by remember { mutableStateOf(DateUtils.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTime = DateUtils.now()
        }
    }

    // Animation pulsation bouton
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Snackbar
    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val msg = uiState.successMessage ?: uiState.errorMessage ?: return@LaunchedEffect
        snackbarHost.showSnackbar(msg)
        vm.clearMessage()
    }

    Scaffold(
        topBar = {
            OrinasaTopBar(title = "Pointage", onBack = onBack)
        },
        snackbarHost = { OrinasaSnackbarHost(snackbarHost) },
        containerColor = Surface0
    ) { pad ->
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Date ──────────────────────────────────────────────────────────
            Text(
                DateUtils.formatDateLong(DateUtils.today()),
                style     = MaterialTheme.typography.bodyMedium,
                color     = TextSec,
                textAlign = TextAlign.Center
            )

            // ── Horloge ───────────────────────────────────────────────────────
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.radialGradient(
                            listOf(Primary.copy(0.08f), Surface0)
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    currentTime,
                    style      = MaterialTheme.typography.displayLarge.copy(
                        fontSize      = 60.sp,
                        fontWeight    = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = Primary
                )
            }

            // ── État du jour ──────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimeInfoCard(
                    label    = "Arrivée",
                    time     = todayRecord?.clockInTime ?: "--:--",
                    icon     = Icons.Default.Login,
                    isActive = hasClockedIn,
                    modifier = Modifier.weight(1f)
                )
                TimeInfoCard(
                    label    = "Départ",
                    time     = todayRecord?.clockOutTime ?: "--:--",
                    icon     = Icons.Default.Logout,
                    isActive = hasClockedOut,
                    modifier = Modifier.weight(1f)
                )
            }

            // Retard si applicable
            if (hasClockedIn && todayRecord?.isLate == true) {
                Surface(
                    color  = StatusLate.copy(alpha = 0.1f),
                    shape  = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, StatusLate.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier          = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Schedule, null,
                            tint = StatusLate, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Retard de ${todayRecord?.lateMinutes} minute(s)",
                            style  = MaterialTheme.typography.bodySmall,
                            color  = StatusLate,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Durée si journée complète
            if (hasClockedOut && (todayRecord?.workDurationMinutes ?: 0) > 0) {
                val dur = todayRecord!!.workDurationMinutes
                Surface(
                    color  = StatusPresent.copy(alpha = 0.08f),
                    shape  = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, StatusPresent.copy(alpha = 0.25f)
                    )
                ) {
                    Row(
                        modifier          = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Timer, null,
                            tint = StatusPresent, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Temps de travail : ${dur / 60}h ${dur % 60}min",
                            style  = MaterialTheme.typography.bodySmall,
                            color  = StatusPresent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Bouton principal ──────────────────────────────────────────────
            when {
                hasClockedOut -> {
                    // Journée terminée
                    Box(
                        modifier         = Modifier
                            .size(160.dp)
                            .background(StatusPresent.copy(alpha = 0.1f), CircleShape)
                            .border(3.dp, StatusPresent.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null,
                                tint = StatusPresent, modifier = Modifier.size(48.dp))
                            Text("Journée\nterminée",
                                style     = MaterialTheme.typography.labelMedium,
                                color     = StatusPresent,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                hasClockedIn -> {
                    // Bouton pointer sortie
                    ClockButton(
                        label     = "Pointer la\nsortie",
                        icon      = Icons.Default.Logout,
                        color     = Secondary,
                        scale     = if (!uiState.isLoading) scale else 1f,
                        isLoading = uiState.isLoading,
                        onClick   = { vm.clockOut() }
                    )
                }
                else -> {
                    // Bouton pointer arrivée
                    ClockButton(
                        label     = "Pointer\nl'arrivée",
                        icon      = Icons.Default.Login,
                        color     = Primary,
                        scale     = if (!uiState.isLoading) scale else 1f,
                        isLoading = uiState.isLoading,
                        onClick   = { vm.clockIn() }
                    )
                }
            }

            Text(
                when {
                    hasClockedOut -> "Bonne soirée !"
                    hasClockedIn  -> "Appuyez pour enregistrer votre départ"
                    else          -> "Appuyez pour enregistrer votre arrivée"
                },
                style     = MaterialTheme.typography.bodySmall,
                color     = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ClockButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    scale: Float,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier         = Modifier
            .size(160.dp)
            .scale(scale)
            .background(color, CircleShape)
            .border(4.dp, color.copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick   = onClick,
            modifier  = Modifier.size(160.dp),
            shape     = CircleShape,
            color     = androidx.compose.ui.graphics.Color.Transparent,
            enabled   = !isLoading
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color     = White,
                        modifier  = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(icon, null, tint = White, modifier = Modifier.size(40.dp))
                        Text(label,
                            style     = MaterialTheme.typography.labelLarge,
                            color     = White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeInfoCard(
    label: String,
    time: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(14.dp),
        color    = if (isActive) Primary.copy(alpha = 0.06f) else White,
        border   = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) Primary.copy(alpha = 0.25f) else Border
        )
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon, null,
                tint     = if (isActive) Primary else TextMuted,
                modifier = Modifier.size(20.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSec
            )
            Text(
                time,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = if (isActive) Primary else TextMuted
            )
        }
    }
}