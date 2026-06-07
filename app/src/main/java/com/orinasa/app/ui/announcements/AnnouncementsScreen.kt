package com.orinasa.app.ui.announcements

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
import com.orinasa.app.model.Announcement
import com.orinasa.app.model.AnnouncementPriority
import com.orinasa.app.repository.AnnouncementRepository
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.utils.DateUtils
import com.orinasa.app.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsScreen(
    vm: DashboardViewModel,
    companyId: String,
    currentUserId: String,
    currentUserName: String,
    isAdmin: Boolean,
    onBack: () -> Unit
) {
    val announcements by vm.announcements.collectAsState()
    val scope          = rememberCoroutineScope()
    val repo           = remember { AnnouncementRepository(companyId) }
    val snackbar       = remember { SnackbarHostState() }

    var showCreate  by remember { mutableStateOf(false) }
    var newTitle    by remember { mutableStateOf("") }
    var newContent  by remember { mutableStateOf("") }
    var newPriority by remember { mutableStateOf(AnnouncementPriority.NORMAL) }
    var isPosting   by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            OrinasaTopBar(
                title  = "Annonces",
                onBack = onBack
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                ExtendedFloatingActionButton(
                    text           = { Text("Publier", fontWeight = FontWeight.SemiBold) },
                    icon           = { Icon(Icons.Default.Campaign, null) },
                    onClick        = { showCreate = true },
                    containerColor = Primary,
                    contentColor   = White,
                    shape          = RoundedCornerShape(14.dp)
                )
            }
        },
        snackbarHost   = { OrinasaSnackbarHost(snackbar) },
        containerColor = Surface0
    ) { pad ->
        if (announcements.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon     = Icons.Default.Campaign,
                    title    = "Aucune annonce",
                    subtitle = if (isAdmin) "Publiez votre première annonce" else "Aucune annonce pour le moment"
                )
            }
        } else {
            LazyColumn(
                modifier       = Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(announcements, key = { it.id }) { ann ->
                    AnnouncementCard(
                        ann          = ann,
                        currentUserId = currentUserId,
                        isAdmin      = isAdmin,
                        onRead       = { vm.markAnnouncementRead(ann.id) },
                        onDelete     = if (isAdmin) {{
                            scope.launch {
                                repo.deleteAnnouncement(ann.id)
                                snackbar.showSnackbar("Annonce supprimée")
                            }
                        }} else null
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // ── Dialog création ───────────────────────────────────────────────────────
    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            shape            = RoundedCornerShape(16.dp),
            containerColor   = White,
            title            = { Text("Nouvelle annonce", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OrinasaTextField(
                        value         = newTitle,
                        onValueChange = { newTitle = it },
                        label         = "Titre *"
                    )
                    OrinasaTextField(
                        value         = newContent,
                        onValueChange = { if (it.length <= 500) newContent = it },
                        label         = "Contenu *",
                        singleLine    = false,
                        maxLines      = 5
                    )
                    Text("${newContent.length}/500",
                        style = MaterialTheme.typography.labelSmall, color = TextMuted)

                    // Priorité
                    Text("Priorité", style = MaterialTheme.typography.labelMedium, color = TextSec)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AnnouncementPriority.values().forEach { p ->
                            val (label, color) = when (p) {
                                AnnouncementPriority.NORMAL    -> "Normal"    to PriorityNormal
                                AnnouncementPriority.IMPORTANT -> "Important" to PriorityImportant
                                AnnouncementPriority.URGENT    -> "Urgent"    to PriorityUrgent
                            }
                            FilterChip(
                                selected = newPriority == p,
                                onClick  = { newPriority = p },
                                label    = {
                                    Text(label, style = MaterialTheme.typography.labelSmall,
                                        color = if (newPriority == p) White else color)
                                },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = color
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                OrinasaButton(
                    text      = "Publier",
                    onClick   = {
                        scope.launch {
                            isPosting = true
                            repo.publishAnnouncement(
                                title      = newTitle,
                                content    = newContent,
                                authorId   = currentUserId,
                                authorName = currentUserName,
                                priority   = newPriority
                            )
                            isPosting = false
                            showCreate = false
                            newTitle   = ""; newContent = ""
                            newPriority = AnnouncementPriority.NORMAL
                            snackbar.showSnackbar("Annonce publiée")
                        }
                    },
                    enabled   = newTitle.isNotBlank() && newContent.isNotBlank(),
                    isLoading = isPosting,
                    icon      = Icons.Default.Send
                )
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Annuler") } }
        )
    }
}

@Composable
private fun AnnouncementCard(
    ann: Announcement,
    currentUserId: String,
    isAdmin: Boolean,
    onRead: () -> Unit,
    onDelete: (() -> Unit)?
) {
    val isRead = ann.isReadBy(currentUserId)
    val priorityColor = when (ann.priority) {
        AnnouncementPriority.URGENT    -> PriorityUrgent
        AnnouncementPriority.IMPORTANT -> PriorityImportant
        AnnouncementPriority.NORMAL    -> PriorityNormal
    }

    LaunchedEffect(ann.id) { if (!isRead) onRead() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        color    = White,
        border   = androidx.compose.foundation.BorderStroke(
            if (!isRead) 1.5.dp else 1.dp,
            if (!isRead) priorityColor.copy(0.6f) else Border
        )
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(10.dp)
                        .background(priorityColor, RoundedCornerShape(5.dp)))
                    Column {
                        Text(ann.title, style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (!isRead) FontWeight.Bold else FontWeight.SemiBold,
                            color = TextPri)
                        Text("Par ${ann.authorName}",
                            style = MaterialTheme.typography.labelSmall, color = TextSec)
                    }
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, null, tint = StatusAbsent, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Text(ann.content, style = MaterialTheme.typography.bodyMedium, color = TextSec)

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(DateUtils.formatTimestamp(ann.timestamp),
                    style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text("${ann.readBy.size} lecture(s)",
                    style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}