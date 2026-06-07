package com.orinasa.app.ui.profile

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.orinasa.app.ui.components.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.viewmodel.AuthViewModel
import com.orinasa.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authVm: AuthViewModel,
    profileVm: ProfileViewModel,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val notifyLeave by profileVm.notifyLeave.collectAsState()
    val notifyPay   by profileVm.notifyPay.collectAsState()
    val notifyAnn   by profileVm.notifyAnn.collectAsState()
    val loading     by authVm.state.collectAsState()

    var showChangePwd    by remember { mutableStateOf(false) }
    var showDeleteAcct   by remember { mutableStateOf(false) }
    var msg              by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(msg) {
        msg?.let { (_, text) -> snackbar.showSnackbar(text); msg = null }
    }

    Scaffold(
        topBar = {
            OrinasaTopBar(title = "Paramètres", onBack = onBack)
        },
        snackbarHost   = { OrinasaSnackbarHost(snackbar) },
        containerColor = Surface0
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Notifications ─────────────────────────────────────────────────
            SettingsSectionLabel("Notifications")
            SettingsCard {
                SettingSwitchRow(Icons.Default.BeachAccess, "Congés",     "Alertes validation/refus", notifyLeave) { profileVm.setNotifyLeave(it) }
                SettingsDivider()
                SettingSwitchRow(Icons.Default.RequestPage, "Fiches de paie", "Alertes nouvelles fiches", notifyPay) { profileVm.setNotifyPay(it) }
                SettingsDivider()
                SettingSwitchRow(Icons.Default.Campaign,    "Annonces",   "Alertes nouvelles annonces", notifyAnn)  { profileVm.setNotifyAnn(it) }
            }

            // ── Sécurité ──────────────────────────────────────────────────────
            SettingsSectionLabel("Sécurité")
            SettingsCard {
                SettingActionRow(Icons.Default.Lock, "Changer le mot de passe", "Modifier vos identifiants") { showChangePwd = true }
            }

            // ── App ───────────────────────────────────────────────────────────
            SettingsSectionLabel("À propos")
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Orinasa v1.0", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPri)
                    Text("Gestion RH & Pointage — Madagascar", style = MaterialTheme.typography.bodySmall, color = TextSec)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Déconnexion
            OrinasaButton(
                text           = "Se déconnecter",
                onClick        = onLogout,
                modifier       = Modifier.fillMaxWidth(),
                icon           = Icons.Default.Logout,
                containerColor = MaterialTheme.colorScheme.error,
                contentColor   = White
            )

            // Supprimer le compte
            OrinasaOutlinedButton(
                text     = "Supprimer mon compte",
                onClick  = { showDeleteAcct = true },
                modifier = Modifier.fillMaxWidth(),
                icon     = Icons.Default.DeleteForever,
                color    = StatusAbsent
            )

            Spacer(Modifier.height(20.dp))
        }
    }

    // ── Dialog MDP ────────────────────────────────────────────────────────────
    if (showChangePwd) {
        var cur by remember { mutableStateOf("") }
        var nw  by remember { mutableStateOf("") }
        var cf  by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChangePwd = false },
            shape = RoundedCornerShape(16.dp), containerColor = White,
            title = { Text("Changer le mot de passe", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OrinasaTextField(value = cur, onValueChange = { cur = it }, label = "Actuel", isPassword = true)
                    OrinasaTextField(value = nw,  onValueChange = { nw  = it }, label = "Nouveau (6 min)", isPassword = true)
                    OrinasaTextField(value = cf,  onValueChange = { cf  = it }, label = "Confirmer", isPassword = true,
                        isError = cf.isNotBlank() && cf != nw, errorMessage = if (cf.isNotBlank() && cf != nw) "Ne correspondent pas" else "")
                }
            },
            confirmButton = {
                OrinasaButton(
                    text    = "Confirmer",
                    onClick = {
                        authVm.updatePassword(cur, nw) { ok, err ->
                            showChangePwd = false
                            msg = ok to if (ok) "✅ Mot de passe mis à jour !" else "❌ $err"
                        }
                    },
                    enabled = cur.isNotBlank() && nw.length >= 6 && nw == cf
                )
            },
            dismissButton = { TextButton(onClick = { showChangePwd = false }) { Text("Annuler") } }
        )
    }

    // ── Dialog suppression ────────────────────────────────────────────────────
    if (showDeleteAcct) {
        var pwd     by remember { mutableStateOf("") }
        var confirm by remember { mutableStateOf("") }
        val WORD     = "SUPPRIMER"
        AlertDialog(
            onDismissRequest = { showDeleteAcct = false },
            shape = RoundedCornerShape(16.dp), containerColor = White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.DeleteForever, null, tint = StatusAbsent)
                    Text("Supprimer mon compte", color = StatusAbsent, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(10.dp)) {
                        Text("⚠️ Cette action est irréversible. Toutes vos données seront supprimées.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                    OrinasaTextField(value = confirm, onValueChange = { confirm = it.uppercase() },
                        label = "Tapez « $WORD »",
                        isError = confirm.isNotBlank() && confirm != WORD)
                    OrinasaTextField(value = pwd, onValueChange = { pwd = it },
                        label = "Mot de passe actuel", isPassword = true)
                }
            },
            confirmButton = {
                OrinasaButton(
                    text    = "Supprimer",
                    onClick = {
                        authVm.deleteAccount(pwd) { ok, err ->
                            if (ok) { showDeleteAcct = false; onLogout() }
                            else    { showDeleteAcct = false; msg = false to "❌ $err" }
                        }
                    },
                    enabled        = confirm == WORD && pwd.isNotBlank(),
                    containerColor = StatusAbsent
                )
            },
            dismissButton = { TextButton(onClick = { showDeleteAcct = false }) { Text("Annuler") } }
        )
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold, color = TextSec,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp))
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        color = White, border = androidx.compose.foundation.BorderStroke(0.5.dp, Border)) {
        Column(content = content)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = Border.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 52.dp), thickness = 0.5.dp)
}

@Composable
private fun SettingSwitchRow(icon: ImageVector, title: String, subtitle: String,
                             checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = TextPri)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSec)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SettingActionRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = TextPri)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSec)
        }
        IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}