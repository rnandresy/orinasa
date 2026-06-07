package com.orinasa.app.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.SubcomposeAsyncImage
import com.orinasa.app.model.AttendanceStatus
import com.orinasa.app.model.LeaveStatus
import com.orinasa.app.model.LeaveType
import com.orinasa.app.ui.theme.*

// ── Avatar utilisateur ────────────────────────────────────────────────────────
@Composable
fun OrinasaAvatar(
    name: String,
    photoUrl: String   = "",
    size: Dp           = 44.dp,
    isAdmin: Boolean   = false,
    onClick: (() -> Unit)? = null
) {
    val bg = if (isAdmin) Secondary else Primary
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl.isNotBlank()) {
            SubcomposeAsyncImage(
                model              = photoUrl,
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
                loading            = { AvatarPlaceholder(name, size, bg) }
            )
        } else {
            AvatarPlaceholder(name, size, bg)
        }
        if (isAdmin) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.3f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .border(1.5.dp, White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Star, null,
                    tint     = White,
                    modifier = Modifier.size(size * 0.17f)
                )
            }
        }
    }
}

@Composable
private fun AvatarPlaceholder(name: String, size: Dp, bg: Color) {
    val initials = name.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")
        .ifBlank { "?" }
    Box(
        modifier         = Modifier.fillMaxSize().background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initials,
            style    = MaterialTheme.typography.labelLarge.copy(
                fontSize   = (size.value / 2.8).sp,
                fontWeight = FontWeight.Bold
            ),
            color = White
        )
    }
}

// ── Chip statut présence ──────────────────────────────────────────────────────
@Composable
fun AttendanceStatusChip(status: AttendanceStatus, modifier: Modifier = Modifier) {
    val (label, color) = when (status) {
        AttendanceStatus.PRESENT  -> "Présent"   to StatusPresent
        AttendanceStatus.ABSENT   -> "Absent"    to StatusAbsent
        AttendanceStatus.LATE     -> "Retard"    to StatusLate
        AttendanceStatus.HALF_DAY -> "Mi-temps"  to StatusHalfDay
        AttendanceStatus.ON_LEAVE -> "Congé"     to StatusLeave
        AttendanceStatus.HOLIDAY  -> "Férié"     to TextMuted
    }
    Surface(
        modifier = modifier,
        color    = color.copy(alpha = 0.12f),
        shape    = RoundedCornerShape(6.dp),
        border   = BorderStroke(0.5.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            label,
            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color      = color
        )
    }
}

// ── Chip statut congé ─────────────────────────────────────────────────────────
@Composable
fun LeaveStatusChip(status: LeaveStatus, modifier: Modifier = Modifier) {
    val (label, color) = when (status) {
        LeaveStatus.PENDING   -> "En attente"  to StatusLate
        LeaveStatus.APPROVED  -> "Approuvé"    to StatusPresent
        LeaveStatus.REJECTED  -> "Refusé"      to StatusAbsent
        LeaveStatus.CANCELLED -> "Annulé"      to TextMuted
    }
    Surface(
        modifier = modifier,
        color    = color.copy(alpha = 0.12f),
        shape    = RoundedCornerShape(6.dp),
        border   = BorderStroke(0.5.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            label,
            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color      = color
        )
    }
}

// ── Type de congé chip ────────────────────────────────────────────────────────
@Composable
fun LeaveTypeChip(type: LeaveType, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color    = MaterialTheme.colorScheme.secondaryContainer,
        shape    = RoundedCornerShape(6.dp)
    ) {
        Text(
            type.label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

// ── Carte stats dashboard ─────────────────────────────────────────────────────
@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String   = "",
    icon: ImageVector,
    iconColor: Color   = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier  = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape     = RoundedCornerShape(14.dp),
        color     = White,
        border    = BorderStroke(1.dp, Border),
        shadowElevation = 0.dp,
        tonalElevation  = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSec
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
            }
            Text(
                value,
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = TextPri
            )
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}

// ── Bouton principal ──────────────────────────────────────────────────────────
@Composable
fun OrinasaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier  = Modifier,
    enabled: Boolean    = true,
    isLoading: Boolean  = false,
    icon: ImageVector?  = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color   = White
) {
    Button(
        onClick   = onClick,
        modifier  = modifier.height(48.dp),
        enabled   = enabled && !isLoading,
        shape     = RoundedCornerShape(12.dp),
        colors    = ButtonDefaults.buttonColors(
            containerColor         = containerColor,
            contentColor           = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor   = contentColor.copy(alpha = 0.6f)
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(18.dp),
                color       = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Bouton secondaire ─────────────────────────────────────────────────────────
@Composable
fun OrinasaOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean   = true,
    icon: ImageVector? = null,
    color: Color       = MaterialTheme.colorScheme.primary
) {
    OutlinedButton(
        onClick  = onClick,
        modifier = modifier.height(48.dp),
        enabled  = enabled,
        shape    = RoundedCornerShape(12.dp),
        colors   = ButtonDefaults.outlinedButtonColors(contentColor = color),
        border   = BorderStroke(1.dp, color.copy(if (enabled) 1f else 0.4f))
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

// ── Champ texte ───────────────────────────────────────────────────────────────
@Composable
fun OrinasaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier     = Modifier,
    placeholder: String    = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean    = true,
    maxLines: Int          = 1,
    enabled: Boolean       = true,
    readOnly: Boolean      = false,
    isError: Boolean       = false,
    errorMessage: String   = "",
    isPassword: Boolean    = false,
    showPassword: Boolean  = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions =
        androidx.compose.foundation.text.KeyboardOptions.Default
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value           = value,
            onValueChange   = onValueChange,
            label           = { Text(label) },
            placeholder     = if (placeholder.isNotBlank()) {
                { Text(placeholder, color = TextMuted) }
            } else null,
            leadingIcon     = if (leadingIcon != null) {
                { Icon(leadingIcon, null, tint = TextSec, modifier = Modifier.size(20.dp)) }
            } else null,
            trailingIcon    = when {
                isPassword && onTogglePassword != null -> {
                    {
                        IconButton(onClick = onTogglePassword) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null, tint = TextSec
                            )
                        }
                    }
                }
                trailingIcon != null -> trailingIcon
                else -> null
            },
            visualTransformation = if (isPassword && !showPassword)
                androidx.compose.ui.text.input.PasswordVisualTransformation()
            else androidx.compose.ui.text.input.VisualTransformation.None,
            singleLine      = singleLine,
            maxLines        = maxLines,
            enabled         = enabled,
            readOnly        = readOnly,
            isError         = isError,
            keyboardOptions = keyboardOptions,
            modifier        = Modifier.fillMaxWidth(),
            shape           = RoundedCornerShape(12.dp),
            colors          = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Border,
                errorBorderColor     = MaterialTheme.colorScheme.error,
                disabledBorderColor  = Border.copy(alpha = 0.5f)
            )
        )
        if (isError && errorMessage.isNotBlank()) {
            Text(
                errorMessage,
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp, top = 3.dp)
            )
        }
    }
}

// ── TopBar avec retour ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrinasaTopBar(
    title: String,
    subtitle: String   = "",
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSec)
                }
            }
        },
        navigationIcon = if (onBack != null) {
            {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null)
                }
            }
        } else ({}),
        actions = actions,
        colors  = TopAppBarDefaults.topAppBarColors(
            containerColor    = White,
            titleContentColor = TextPri
        )
    )
}

// ── État vide ─────────────────────────────────────────────────────────────────
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String   = "",
    actionText: String = "",
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Surface1),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = TextMuted, modifier = Modifier.size(36.dp))
        }
        Text(
            title,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color      = TextPri,
            textAlign  = TextAlign.Center
        )
        if (subtitle.isNotBlank()) {
            Text(
                subtitle,
                style     = MaterialTheme.typography.bodySmall,
                color     = TextSec,
                textAlign = TextAlign.Center
            )
        }
        if (actionText.isNotBlank() && onAction != null) {
            Spacer(Modifier.height(4.dp))
            OrinasaButton(text = actionText, onClick = onAction)
        }
    }
}

// ── Séparateur avec label ─────────────────────────────────────────────────────
@Composable
fun LabelDivider(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Border)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        HorizontalDivider(modifier = Modifier.weight(1f), color = Border)
    }
}

// ── Section header ────────────────────────────────────────────────────────────
@Composable
fun SectionHeader(
    title: String,
    action: String     = "",
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            title,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color      = TextPri
        )
        if (action.isNotBlank() && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(4.dp)) {
                Text(
                    action,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ── Carte info ────────────────────────────────────────────────────────────────
@Composable
fun InfoCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBg: Color      = MaterialTheme.colorScheme.primaryContainer,
    iconTint: Color    = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier  = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape     = RoundedCornerShape(12.dp),
        color     = White,
        border    = BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier         = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPri)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSec, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (onClick != null) {
                Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ── Loading overlay ───────────────────────────────────────────────────────────
@Composable
fun LoadingOverlay(message: String = "Chargement…") {
    Box(
        modifier         = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(shape = RoundedCornerShape(16.dp), color = White) {
            Column(
                modifier            = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = Primary)
                Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSec)
            }
        }
    }
}

// ── Snackbar host ─────────────────────────────────────────────────────────────
@Composable
fun OrinasaSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState) { data ->
        Snackbar(
            snackbarData    = data,
            shape           = RoundedCornerShape(12.dp),
            containerColor  = Primary,
            contentColor    = White,
            actionColor     = Accent
        )
    }
}

// ── Badge compte ──────────────────────────────────────────────────────────────
@Composable
fun CountBadge(count: Int, modifier: Modifier = Modifier) {
    if (count <= 0) return
    Box(
        modifier         = modifier
            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.error)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (count > 99) "99+" else "$count",
            style    = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color    = White,
            fontWeight = FontWeight.Bold
        )
    }
}