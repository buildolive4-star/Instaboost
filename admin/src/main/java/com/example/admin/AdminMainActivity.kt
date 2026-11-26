package com.example.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.admin.data.AdminRepository
import com.example.admin.data.AdminSession
import com.example.admin.firebase.adminGoogleSignIn
import com.example.admin.firebase.adminSignOut
import com.example.admin.firebase.ensureAdminFirebaseInitialized
import com.example.admin.firebase.getAdminFirestore
import com.example.admin.theme.AdminBorder
import com.example.admin.theme.AdminPrimary
import com.example.admin.theme.AdminTextSecondary
import com.example.admin.theme.AdminTheme
import com.example.admin.ui.AdminCatalogTab
import com.example.admin.ui.AdminControlTab
import com.example.admin.ui.AdminLoginScreen
import com.example.admin.ui.AdminOverviewTab
import com.example.admin.ui.AdminTeamTab
import com.example.admin.ui.AdminUsersTab
import com.example.admin.viewmodel.AdminViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

private enum class AdminNavTab(val label: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Filled.Dashboard),
    USERS("Users", Icons.Filled.People),
    CATALOG("Catalog", Icons.Filled.Inventory2),
    CONTROL("Control", Icons.Filled.Tune),
    TEAM("Team", Icons.Filled.Groups)
}

class AdminMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureAdminFirebaseInitialized(this)
        enableEdgeToEdge()
        setContent {
            AdminTheme {
                AdminRoot()
            }
        }
    }
}

@Composable
private fun AdminRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val repository = remember(context) { AdminRepository(getAdminFirestore(context)) }

    var firebaseUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var session by remember { mutableStateOf<AdminSession?>(null) }
    var checkingSession by remember { mutableStateOf(firebaseUser != null) }
    var authBusy by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            firebaseUser = auth.currentUser
            if (auth.currentUser == null) {
                session = null
                checkingSession = false
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(firebaseUser?.uid) {
        if (firebaseUser != null) {
            checkingSession = true
            authError = null
            session = repository.resolveExistingSession()
            checkingSession = false
        } else {
            session = null
            checkingSession = false
        }
    }

    if (checkingSession) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFC)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AdminPrimary)
        }
        return
    }

    val activeSession = session
    if (firebaseUser == null || activeSession == null) {
        AdminLoginScreen(
            isGoogleSignedIn = firebaseUser != null,
            loading = authBusy,
            errorMessage = authError,
            onGoogleSignIn = {
                authBusy = true
                authError = null
                adminGoogleSignIn(
                    context = context,
                    credentialManager = credentialManager,
                    scope = scope,
                    onSuccess = {
                        authBusy = false
                        firebaseUser = Firebase.auth.currentUser
                    },
                    onError = { msg ->
                        authBusy = false
                        authError = msg
                    },
                    onCancelled = {
                        authBusy = false
                    }
                )
            },
            onCredentialLogin = { name, password ->
                scope.launch {
                    authBusy = true
                    authError = null
                    repository.signInWithNameAndPassword(name, password)
                        .onSuccess { resolved ->
                            session = resolved
                        }
                        .onFailure { err ->
                            authError = err.localizedMessage ?: "Invalid name or password."
                        }
                    authBusy = false
                }
            },
            onSwitchGoogleAccount = {
                authError = null
                adminSignOut(credentialManager, scope) {
                    firebaseUser = null
                    session = null
                }
            }
        )
    } else {
        key(activeSession.uid) {
            AdminAuthenticatedShell(
                repository = repository,
                session = activeSession,
                onSignOut = {
                    adminSignOut(credentialManager, scope) {
                        firebaseUser = null
                        session = null
                    }
                }
            )
        }
    }
}

@Composable
private fun AdminAuthenticatedShell(
    repository: AdminRepository,
    session: AdminSession,
    onSignOut: () -> Unit
) {
    val viewModel: AdminViewModel = viewModel(
        key = "admin_${session.uid}",
        factory = viewModelFactory {
            initializer { AdminViewModel(repository, session) }
        }
    )

    val users by viewModel.users.collectAsStateWithLifecycle()
    val services by viewModel.services.collectAsStateWithLifecycle()
    val packages by viewModel.coinPackages.collectAsStateWithLifecycle()
    val appConfig by viewModel.appConfig.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val adminStaff by viewModel.adminStaff.collectAsStateWithLifecycle()
    val selectedUserId by viewModel.selectedUserId.collectAsStateWithLifecycle()
    val selectedOrders by viewModel.selectedUserOrders.collectAsStateWithLifecycle()
    val selectedTickets by viewModel.selectedUserTickets.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val toast by viewModel.toast.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(toast) {
        toast?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    // Stealth design: Team tab is visible ONLY to Main Admin; other admins see a complete, seamless 4-tab Admin Panel
    val visibleTabs = remember(session.isMainAdmin) {
        if (session.isMainAdmin) {
            listOf(AdminNavTab.OVERVIEW, AdminNavTab.USERS, AdminNavTab.CATALOG, AdminNavTab.CONTROL, AdminNavTab.TEAM)
        } else {
            listOf(AdminNavTab.OVERVIEW, AdminNavTab.USERS, AdminNavTab.CATALOG, AdminNavTab.CONTROL)
        }
    }
    var currentTab by remember { mutableStateOf(AdminNavTab.OVERVIEW) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 78.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFC))
                .statusBarsPadding()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Unified Top Bar (identical title & branding for all admins)
                Surface(
                    color = Color.White,
                    border = BorderStroke(0.5.dp, AdminBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CreatorBoost Admin",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentTab.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = AdminPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = onSignOut,
                            modifier = Modifier.testTag("admin_logout_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign Out",
                                tint = AdminTextSecondary
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        AdminNavTab.OVERVIEW -> AdminOverviewTab(
                            adminName = session.displayName,
                            users = users,
                            services = services,
                            packages = packages,
                            appConfig = appConfig,
                            auditLogs = auditLogs,
                            onToggleMaintenance = { enabled ->
                                viewModel.saveConfig(appConfig.copy(maintenanceMode = enabled))
                            },
                            onOpenUsers = { currentTab = AdminNavTab.USERS },
                            onOpenControl = { currentTab = AdminNavTab.CONTROL }
                        )

                        AdminNavTab.USERS -> AdminUsersTab(
                            users = users,
                            selectedUserId = selectedUserId,
                            selectedUserOrders = selectedOrders,
                            selectedUserTickets = selectedTickets,
                            busy = busy,
                            onSelectUser = { viewModel.selectUser(it) },
                            onUpdateUser = { u, coins, pts, lvl, blocked, reason ->
                                viewModel.updateUser(u, coins, pts, lvl, blocked, reason)
                            },
                            onUpdateOrderStatus = { uid, oid, st ->
                                viewModel.updateOrderStatus(uid, oid, st)
                            },
                            onUpdateTicketStatus = { uid, tid, st ->
                                viewModel.updateTicketStatus(uid, tid, st)
                            },
                            onSendNotification = { uid, title, body, done ->
                                viewModel.sendUserNotification(uid, title, body, done)
                            }
                        )

                        AdminNavTab.CATALOG -> AdminCatalogTab(
                            services = services,
                            packages = packages,
                            busy = busy,
                            onSaveService = { id, catId, catName, name, type, price, trending, published, lines, done ->
                                viewModel.saveService(id, catId, catName, name, type, price, trending, published, lines, done)
                            },
                            onDeleteService = { viewModel.deleteService(it) },
                            onSavePackage = { id, name, coins, bonus, pts, inr, badge, order, active, done ->
                                viewModel.savePackage(id, name, coins, bonus, pts, inr, badge, order, active, done)
                            },
                            onDeletePackage = { viewModel.deletePackage(it) }
                        )

                        AdminNavTab.CONTROL -> AdminControlTab(
                            config = appConfig,
                            userCount = users.size,
                            busy = busy,
                            onSaveConfig = { viewModel.saveConfig(it) },
                            onBroadcast = { title, body, done ->
                                viewModel.broadcastNotification(title, body, done)
                            }
                        )

                        AdminNavTab.TEAM -> if (session.isMainAdmin) {
                            AdminTeamTab(
                                staffList = adminStaff,
                                busy = busy,
                                onCreateMember = { name, pass, done ->
                                    viewModel.createAdminMember(name, pass, done)
                                },
                                onToggleMember = { entry, active ->
                                    viewModel.toggleAdminMember(entry, active)
                                },
                                onDeleteMember = { viewModel.deleteAdminMember(it) }
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Bar
            Surface(
                color = Color.White,
                shadowElevation = 10.dp,
                border = BorderStroke(0.5.dp, AdminBorder),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 6.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    visibleTabs.forEach { tab ->
                        val selected = currentTab == tab
                        val tint = if (selected) AdminPrimary else AdminTextSecondary
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (tab != AdminNavTab.USERS) viewModel.selectUser(null)
                                    currentTab = tab
                                }
                                .padding(vertical = 4.dp)
                                .testTag("admin_tab_${tab.name.lowercase()}")
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = tint,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = tint
                            )
                        }
                    }
                }
            }
        }
    }
}
