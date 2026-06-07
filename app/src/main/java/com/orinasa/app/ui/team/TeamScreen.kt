package com.orinasa.app.ui.team

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.orinasa.app.model.User
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.viewmodel.TeamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
    vm: TeamViewModel,
    isAdmin: Boolean,
    onOpenEmployee: (User) -> Unit,
    onAddEmployee: () -> Unit,
    onBack: () -> Unit
) {
    val filteredUsers by vm.filteredUsers.collectAsState()
    val allUsers      by vm.users.collectAsState()
    val departments   by vm.departments.collectAsState()
    val searchQuery   by vm.searchQuery.collectAsState()
    val uiState       by vm.uiState.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val msg = uiState.successMessage ?: uiState.errorMessage ?: return@LaunchedEffect
        snackbar.showSnackbar(msg)
        vm.clearMessage()
    }

    Scaffold(
        topBar = {
            OrinasaTopBar(
                title    = "Équipe",
                subtitle = "${allUsers.size} employé(s) actif(s)",
                onBack   = onBack
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                ExtendedFloatingActionButton(
                    text           = { Text("Ajouter", fontWeight = FontWeight.SemiBold) },
                    icon           = { Icon(Icons.Default.PersonAdd, null) },
                    onClick        = onAddEmployee,
                    containerColor = Primary,
                    contentColor   = White,
                    shape          = RoundedCornerShape(14.dp)
                )
            }
        },
        snackbarHost   = { OrinasaSnackbarHost(snackbar) },
        containerColor = Surface0
    ) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad)) {
            // ── Recherche ─────────────────────────────────────────────────────
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { vm.setSearch(it) },
                placeholder   = { Text("Rechercher un employé…", color = TextMuted) },
                leadingIcon   = { Icon(Icons.Default.Search, null, tint = TextSec) },
                trailingIcon  = if (searchQuery.isNotBlank()) {
                    { IconButton(onClick = { vm.setSearch("") }) {
                        Icon(Icons.Default.Close, null, tint = TextSec) }
                    }
                } else null,
                singleLine    = true,
                shape         = RoundedCornerShape(0.dp),
                modifier      = Modifier.fillMaxWidth(),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Primary,
                    unfocusedBorderColor = Border,
                    unfocusedContainerColor = White,
                    focusedContainerColor   = White
                )
            )

            if (filteredUsers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon       = Icons.Default.People,
                        title      = if (searchQuery.isNotBlank()) "Aucun résultat" else "Aucun employé",
                        subtitle   = if (searchQuery.isNotBlank()) "Essayez un autre terme" else "Ajoutez votre premier employé",
                        actionText = if (isAdmin && searchQuery.isBlank()) "Ajouter un employé" else "",
                        onAction   = if (isAdmin && searchQuery.isBlank()) onAddEmployee else null
                    )
                }
            } else {
                LazyColumn(
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Groupe par département
                    val grouped = filteredUsers.groupBy { it.departmentName.ifBlank { "Sans département" } }
                    grouped.forEach { (dept, users) ->
                        item {
                            Text(
                                "$dept (${users.size})",
                                style    = MaterialTheme.typography.labelMedium,
                                color    = TextSec,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(users, key = { it.userId }) { user ->
                            EmployeeCard(
                                user     = user,
                                isAdmin  = isAdmin,
                                onClick  = { onOpenEmployee(user) }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun EmployeeCard(user: User, isAdmin: Boolean, onClick: () -> Unit) {
    Surface(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        color     = White,
        border    = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OrinasaAvatar(
                name     = user.fullName(),
                photoUrl = user.photoUrl,
                size     = 48.dp,
                isAdmin  = user.isAdmin
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        user.fullName(),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPri,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    if (user.isAdmin) {
                        Surface(
                            color  = Primary.copy(alpha = 0.08f),
                            shape  = RoundedCornerShape(4.dp)
                        ) {
                            Text("Admin", modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold, color = Primary)
                        }
                    }
                }
                Text(
                    user.position.ifBlank { "—" },
                    style    = MaterialTheme.typography.bodySmall,
                    color    = TextSec,
                    maxLines = 1
                )
                Text(
                    user.contractType,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(20.dp))
        }
    }
}