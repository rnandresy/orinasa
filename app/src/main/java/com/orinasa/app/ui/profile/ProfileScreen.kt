package com.orinasa.app.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.utils.DateUtils
import com.orinasa.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    vm: ProfileViewModel,
    onBack: () -> Unit,
    onGoSettings: () -> Unit
) {
    val profile   by vm.profile.collectAsState()
    val uiState   by vm.uiState.collectAsState()
    val snackbar   = remember { SnackbarHostState() }

    var firstName  by remember { mutableStateOf(profile?.firstName ?: "") }
    var lastName   by remember { mutableStateOf(profile?.lastName ?: "") }
    var phone      by remember { mutableStateOf(profile?.phone ?: "") }
    var isEditing  by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        profile?.let {
            firstName = it.firstName
            lastName  = it.lastName
            phone     = it.phone
        }
    }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val msg = uiState.successMessage ?: uiState.errorMessage ?: return@LaunchedEffect
        snackbar.showSnackbar(msg)
        vm.clearMessage()
        if (uiState.successMessage != null) isEditing = false
    }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { vm.uploadPhoto(it) }
    }

    Scaffold(
        topBar = {
            OrinasaTopBar(
                title  = "Mon profil",
                onBack = onBack,
                actions = {
                    IconButton(onClick = onGoSettings) {
                        Icon(Icons.Default.Settings, null, tint = TextSec)
                    }
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, null, tint = Primary)
                        }
                    } else {
                        TextButton(onClick = { isEditing = false }) { Text("Annuler") }
                        TextButton(onClick = {
                            vm.updateProfile(firstName, lastName, phone)
                        }) {
                            Text("Sauvegarder", fontWeight = FontWeight.Bold, color = StatusPresent)
                        }
                    }
                }
            )
        },
        snackbarHost   = { OrinasaSnackbarHost(snackbar) },
        containerColor = Surface0
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Avatar ────────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                color    = White,
                border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
            ) {
                Column(
                    modifier            = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        OrinasaAvatar(
                            name     = profile?.fullName() ?: "",
                            photoUrl = profile?.photoUrl ?: "",
                            size     = 80.dp,
                            isAdmin  = profile?.isAdmin == true
                        )
                        if (uiState.isLoading && uiState.uploadProgress > 0) {
                            CircularProgressIndicator(
                                progress = { uiState.uploadProgress / 100f },
                                modifier = Modifier.size(80.dp),
                                color    = Primary,
                                strokeWidth = 3.dp
                            )
                        }
                        Surface(
                            onClick = {
                                avatarPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            color    = Primary,
                            shape    = RoundedCornerShape(50),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CameraAlt, null, tint = White,
                                    modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Text(profile?.fullName() ?: "—", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = TextPri)
                    Text(profile?.email ?: "—", style = MaterialTheme.typography.bodySmall, color = TextSec)
                    if (profile?.isAdmin == true) {
                        Surface(color = Primary.copy(0.08f), shape = RoundedCornerShape(6.dp)) {
                            Text("Administrateur", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold, color = Primary)
                        }
                    }
                    if (profile?.photoUrl?.isNotBlank() == true) {
                        TextButton(
                            onClick        = { vm.deletePhoto() },
                            colors         = ButtonDefaults.textButtonColors(contentColor = StatusAbsent),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Supprimer la photo", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // ── Informations ──────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                color    = White,
                border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Informations personnelles", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = TextPri)
                    HorizontalDivider(color = Border)

                    if (isEditing) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OrinasaTextField(value = firstName, onValueChange = { firstName = it },
                                label = "Prénom", modifier = Modifier.weight(1f))
                            OrinasaTextField(value = lastName, onValueChange = { lastName = it },
                                label = "Nom", modifier = Modifier.weight(1f))
                        }
                        OrinasaTextField(value = phone, onValueChange = { phone = it }, label = "Téléphone",
                            leadingIcon = Icons.Default.Phone,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone))
                    } else {
                        ProfileInfoRow(Icons.Default.Person, "Prénom",       profile?.firstName ?: "—")
                        ProfileInfoRow(Icons.Default.Person, "Nom",          profile?.lastName ?: "—")
                        ProfileInfoRow(Icons.Default.Phone,  "Téléphone",    profile?.phone?.ifBlank { "—" } ?: "—")
                        ProfileInfoRow(Icons.Default.Work,   "Poste",        profile?.position?.ifBlank { "—" } ?: "—")
                        ProfileInfoRow(Icons.Default.Business, "Département", profile?.departmentName?.ifBlank { "—" } ?: "—")
                    }
                }
            }

            // ── Contrat & Congés ──────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                color    = White,
                border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Contrat & Congés", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = TextPri)
                    HorizontalDivider(color = Border)
                    ProfileInfoRow(Icons.Default.Description,    "Contrat",         profile?.contractType ?: "—")
                    ProfileInfoRow(Icons.Default.CalendarToday,  "Depuis",          DateUtils.formatDate(profile?.startDate ?: ""))
                    ProfileInfoRow(Icons.Default.BeachAccess,    "Congés restants", "${profile?.leaveRemaining() ?: 0} jour(s)")
                    ProfileInfoRow(Icons.Default.EventBusy,      "Congés utilisés", "${profile?.leaveUsed ?: 0} jour(s)")
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSec, modifier = Modifier.width(100.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPri)
    }
}