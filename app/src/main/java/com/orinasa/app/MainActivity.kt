package com.orinasa.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.orinasa.app.model.User
import com.orinasa.app.ui.announcements.AnnouncementsScreen
import com.orinasa.app.ui.attendance.*
import com.orinasa.app.ui.auth.*
import com.orinasa.app.ui.dashboard.*
import com.orinasa.app.ui.leave.*
import com.orinasa.app.ui.payslip.*
import com.orinasa.app.ui.profile.*
import com.orinasa.app.ui.team.*
import com.orinasa.app.ui.theme.*
import com.orinasa.app.utils.NotificationHelper
import com.orinasa.app.viewmodel.*

class MainActivity : ComponentActivity() {

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

        setContent {
            OrinasaTheme { OrinasaApp() }
        }
    }
}

@Composable
fun OrinasaApp() {
    val navController = rememberNavController()
    val authVm        = viewModel<AuthViewModel>()

    val isLoggedIn    = authVm.isLoggedIn
    val companyId     by authVm.companyId.collectAsState()
    val userId        by authVm.userId.collectAsState()
    val isAdmin       by authVm.isAdmin.collectAsState()
    val currentUser   by authVm.currentUser.collectAsState()
    val company       by authVm.company.collectAsState()

    val startDest = if (isLoggedIn && companyId.isNotBlank()) "main" else "login"

    NavHost(navController = navController, startDestination = startDest) {

        // ── Auth ──────────────────────────────────────────────────────────────
        composable("login") {
            LoginScreen(
                vm                = authVm,
                onSuccess         = { navController.navigate("main") { popUpTo("login") { inclusive = true } } },
                onRegisterCompany = { navController.navigate("register_company") },
                onJoinCompany     = { navController.navigate("join_company") }
            )
        }
        composable("register_company") {
            RegisterCompanyScreen(
                vm        = authVm,
                onSuccess = { navController.navigate("main") { popUpTo(0) { inclusive = true } } },
                onBack    = { navController.popBackStack() }
            )
        }
        composable("join_company") {
            JoinCompanyScreen(
                vm        = authVm,
                onSuccess = { navController.navigate("main") { popUpTo(0) { inclusive = true } } },
                onBack    = { navController.popBackStack() }
            )
        }

        // ── Main ──────────────────────────────────────────────────────────────
        composable("main") {
            if (companyId.isBlank() || userId.isBlank()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@composable
            }

            OrinasaMainScreen(
                authVm    = authVm,
                companyId = companyId,
                userId    = userId,
                isAdmin   = isAdmin,
                currentUser = currentUser,
                company   = company,
                onLogout  = {
                    authVm.logout()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
    }
}

// ── Écran principal avec navigation ───────────────────────────────────────────
@Composable
fun OrinasaMainScreen(
    authVm: AuthViewModel,
    companyId: String,
    userId: String,
    isAdmin: Boolean,
    currentUser: User?,
    company: com.orinasa.app.model.Company?,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val innerNav = rememberNavController()
    val context  = androidx.compose.ui.platform.LocalContext.current
    val notifHelper = remember { NotificationHelper(context) }

    val dashVm = remember(companyId, userId) {
        DashboardViewModel(companyId, userId, isAdmin)
    }
    val attendVm = remember(companyId, currentUser) {
        if (currentUser != null)
            AttendanceViewModel(
                application = context.applicationContext as android.app.Application,
                companyId   = companyId,
                currentUser = currentUser
            )
        else null
    }
    val leaveVm = remember(companyId, currentUser) {
        if (currentUser != null)
            LeaveViewModel(companyId, currentUser, notifHelper)
        else null
    }
    val payslipVm = remember(companyId, currentUser) {
        if (currentUser != null)
            PayslipViewModel(
                application = context.applicationContext as android.app.Application,
                companyId   = companyId,
                currentUser = currentUser
            )
        else null
    }
    val teamVm = remember(companyId) { TeamViewModel(companyId) }
    val profileVm = remember(companyId, userId) {
        ProfileViewModel(
            application = context.applicationContext as android.app.Application,
            companyId   = companyId,
            userId      = userId
        )
    }

    val backStack by innerNav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val bottomRoutes = listOf("dashboard", "attendance", "leaves", "team", "profile")
    val showBottomBar = currentRoute in bottomRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = White) {
                    NavigationBarItem(
                        selected = currentRoute == "dashboard",
                        onClick  = { innerNav.navigate("dashboard") { launchSingleTop = true; restoreState = true } },
                        icon     = { Icon(Icons.Default.Dashboard, null) },
                        label    = { Text("Accueil") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "attendance",
                        onClick  = { innerNav.navigate("attendance") { launchSingleTop = true; restoreState = true } },
                        icon     = { Icon(Icons.Default.AccessTime, null) },
                        label    = { Text("Pointage") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "leaves",
                        onClick  = { innerNav.navigate("leaves") { launchSingleTop = true; restoreState = true } },
                        icon     = { Icon(Icons.Default.BeachAccess, null) },
                        label    = { Text("Congés") }
                    )
                    if (isAdmin) {
                        NavigationBarItem(
                            selected = currentRoute == "team",
                            onClick  = { innerNav.navigate("team") { launchSingleTop = true; restoreState = true } },
                            icon     = { Icon(Icons.Default.People, null) },
                            label    = { Text("Équipe") }
                        )
                    }
                    NavigationBarItem(
                        selected = currentRoute == "profile",
                        onClick  = { innerNav.navigate("profile") { launchSingleTop = true; restoreState = true } },
                        icon     = { Icon(Icons.Default.Person, null) },
                        label    = { Text("Profil") }
                    )
                }
            }
        },
        containerColor = Surface0
    ) { pad ->
        NavHost(
            navController    = innerNav,
            startDestination = "dashboard",
            modifier         = Modifier.padding(pad)
        ) {
            // ── Dashboard ─────────────────────────────────────────────────────
            composable("dashboard") {
                if (isAdmin) {
                    AdminDashboardScreen(
                        vm               = dashVm,
                        companyName      = company?.name ?: "",
                        adminName        = currentUser?.firstName ?: "",
                        onGoAttendance   = { innerNav.navigate("team_attendance") },
                        onGoLeaves       = { innerNav.navigate("leave_management") },
                        onGoPayslips     = { innerNav.navigate("payslip_generator") },
                        onGoTeam         = { innerNav.navigate("team") },
                        onGoAnnouncements = { innerNav.navigate("announcements") }
                    )
                } else {
                    EmployeeDashboardScreen(
                        vm              = dashVm,
                        userId          = userId,
                        employeeName    = currentUser?.firstName ?: "",
                        companyName     = company?.name ?: "",
                        leaveRemaining  = currentUser?.leaveRemaining() ?: 0,
                        onGoClockIn     = { innerNav.navigate("clockin") },
                        onGoLeaves      = { innerNav.navigate("leaves") },
                        onGoPayslips    = { innerNav.navigate("payslips") },
                        onGoAnnouncements = { innerNav.navigate("announcements") }
                    )
                }
            }

            // ── Pointage ──────────────────────────────────────────────────────
            composable("attendance") {
                if (attendVm != null) {
                    if (isAdmin) {
                        TeamAttendanceScreen(vm = attendVm, onBack = { innerNav.popBackStack() })
                    } else {
                        AttendanceHistoryScreen(vm = attendVm, onBack = { innerNav.popBackStack() })
                    }
                }
            }
            composable("clockin") {
                attendVm?.let {
                    ClockInScreen(vm = it, onBack = { innerNav.popBackStack() })
                }
            }
            composable("team_attendance") {
                attendVm?.let {
                    TeamAttendanceScreen(vm = it, onBack = { innerNav.popBackStack() })
                }
            }

            // ── Congés ────────────────────────────────────────────────────────
            composable("leaves") {
                leaveVm?.let { lvm ->
                    if (isAdmin) {
                        LeaveManagementScreen(vm = lvm, onBack = { innerNav.popBackStack() })
                    } else {
                        LeaveHistoryScreen(
                            vm             = lvm,
                            leaveRemaining = currentUser?.leaveRemaining() ?: 0,
                            onNewRequest   = { innerNav.navigate("leave_request") },
                            onBack         = { innerNav.popBackStack() }
                        )
                    }
                }
            }
            composable("leave_request") {
                leaveVm?.let { lvm ->
                    LeaveRequestScreen(
                        vm             = lvm,
                        leaveRemaining = currentUser?.leaveRemaining() ?: 0,
                        onBack         = { innerNav.popBackStack() },
                        onSuccess      = { innerNav.popBackStack() }
                    )
                }
            }
            composable("leave_management") {
                leaveVm?.let { LeaveManagementScreen(vm = it, onBack = { innerNav.popBackStack() }) }
            }

            // ── Fiches de paie ────────────────────────────────────────────────
            composable("payslips") {
                payslipVm?.let { PayslipScreen(vm = it, onBack = { innerNav.popBackStack() }) }
            }
            composable("payslip_generator") {
                payslipVm?.let { pvm ->
                    val allUsers by teamVm.users.collectAsState()
                    PayslipGeneratorScreen(
                        vm       = pvm,
                        allUsers = allUsers,
                        onBack   = { innerNav.popBackStack() }
                    )
                }
            }

            // ── Équipe ────────────────────────────────────────────────────────
            composable("team") {
                TeamScreen(
                    vm           = teamVm,
                    isAdmin      = isAdmin,
                    onOpenEmployee = { user ->
                        teamVm.selectUser(user)
                        innerNav.navigate("employee_detail")
                    },
                    onAddEmployee = { innerNav.navigate("add_employee") },
                    onBack        = { innerNav.popBackStack() }
                )
            }
            composable("employee_detail") {
                val selectedUser by teamVm.selectedUser.collectAsState()
                selectedUser?.let { user ->
                    EmployeeDetailScreen(
                        vm      = teamVm,
                        user    = user,
                        isAdmin = isAdmin,
                        onBack  = { innerNav.popBackStack() }
                    )
                }
            }
            composable("add_employee") {
                AddEmployeeScreen(
                    authVm      = authVm,
                    teamVm      = teamVm,
                    companyId   = companyId,
                    companyName = company?.name ?: "",
                    inviteCode  = company?.inviteCode ?: "",
                    onBack      = { innerNav.popBackStack() }
                )
            }

            // ── Annonces ──────────────────────────────────────────────────────
            composable("announcements") {
                AnnouncementsScreen(
                    vm              = dashVm,
                    companyId       = companyId,
                    currentUserId   = userId,
                    currentUserName = currentUser?.fullName() ?: "",
                    isAdmin         = isAdmin,
                    onBack          = { innerNav.popBackStack() }
                )
            }

            // ── Profil ────────────────────────────────────────────────────────
            composable("profile") {
                ProfileScreen(
                    vm          = profileVm,
                    onBack      = { innerNav.popBackStack() },
                    onGoSettings = { innerNav.navigate("settings") }
                )
            }
            composable("settings") {
                SettingsScreen(
                    authVm    = authVm,
                    profileVm = profileVm,
                    onLogout  = onLogout,
                    onBack    = { innerNav.popBackStack() }
                )
            }
        }
    }
}