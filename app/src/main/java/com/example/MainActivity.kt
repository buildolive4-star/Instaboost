package com.example

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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.AiContentRepository
import com.example.data.CreatorBoostRepository
import com.example.data.UiState
import com.example.data.UserProfile
import com.example.firebase.configureAppCheck
import com.example.firebase.signOutUser
import com.example.ui.components.FloatingMascotCompanion
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AddFundsScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AiStudioScreen
import com.example.ui.screens.AuthOnboardingScreen
import com.example.ui.screens.CategoryDetailsScreen
import com.example.ui.screens.CategoryFilesScreen
import com.example.ui.screens.CreateOrderScreen
import com.example.ui.screens.DailyRewardsScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.FilePlansScreen
import com.example.ui.screens.HelpSupportScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MyOrdersScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ReferEarnScreen
import com.example.ui.screens.SelectQuantityOrderScreen
import com.example.ui.screens.ServiceOrdersScreen
import com.example.ui.screens.ServicesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SpinWheelScreen
import com.example.ui.screens.WalletHistoryScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.CreatorBoostTheme
import com.example.ui.theme.RaspberryPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CreatorBoostViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

private object Routes {
    const val SERVICES = "services"
    const val WALLET = "wallet"
    const val HOME = "home"
    const val ACCOUNT = "account"
    const val CATEGORY_DETAILS = "category_details/{categoryId}"
    const val CREATE_ORDER = "create_order/{serviceId}"
    const val CATEGORY_FILES = "category_files/{categoryId}"
    const val FILE_PLANS = "file_plans/{fileId}"
    const val PLAN_CHECKOUT = "plan_checkout/{planId}"
    const val SERVICE_ORDERS = "service_orders"
    const val ADD_FUNDS = "add_funds"
    const val WALLET_HISTORY = "wallet_history"
    const val ORDERS = "orders"
    const val FAVORITES = "favorites"
    const val DAILY_REWARDS = "daily_rewards"
    const val SPIN_WHEEL = "spin_wheel"
    const val REFER_EARN = "refer_earn"
    const val AI_STUDIO = "ai_studio"
    const val NOTIFICATIONS = "notifications"
    const val SETTINGS = "settings"
    const val HELP_SUPPORT = "help_support"
    const val ADMIN_PANEL = "admin_panel"

    fun categoryDetails(id: String) = "category_details/$id"
    fun createOrder(id: String) = "create_order/$id"
    fun categoryFiles(id: String) = "category_files/$id"
    fun filePlans(id: String) = "file_plans/$id"
    fun planCheckout(id: String) = "plan_checkout/$id"
}

private data class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

// Clean 4-tab bottom navigation (Leaderboard intentionally removed per user request)
private val bottomTabs = listOf(
    BottomTab(Routes.SERVICES, "Services", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    BottomTab(Routes.WALLET, "Wallet", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    BottomTab(Routes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomTab(Routes.ACCOUNT, "Account", Icons.Filled.Person, Icons.Outlined.Person)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent BEFORE Compose UI or ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            CreatorBoostRoot()
        }
    }
}

@Composable
fun CreatorBoostRoot() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    val user = currentUser
    if (user == null) {
        CreatorBoostTheme(darkTheme = false) {
            AuthOnboardingScreen(
                onAuthSuccess = { currentUser = Firebase.auth.currentUser }
            )
        }
    } else {
        val uid = user.uid
        key(uid) {
            AuthenticatedAppShell(
                currentUserId = uid,
                onSignedOut = { currentUser = null }
            )
        }
    }
}

@Composable
private fun AuthenticatedAppShell(
    currentUserId: String,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: CreatorBoostViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                CreatorBoostViewModel(
                    repository = CreatorBoostRepository(db),
                    aiRepository = AiContentRepository(),
                    currentUserId = currentUserId
                )
            }
        }
    )

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val supportTickets by viewModel.supportTickets.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val serviceFiles by viewModel.serviceFiles.collectAsStateWithLifecycle()
    val servicePlans by viewModel.servicePlans.collectAsStateWithLifecycle()
    val myServiceOrders by viewModel.myServiceOrders.collectAsStateWithLifecycle()
    val myFundRequests by viewModel.myFundRequests.collectAsStateWithLifecycle()
    val services by viewModel.catalogServices.collectAsStateWithLifecycle()
    val coinPackages by viewModel.coinPackages.collectAsStateWithLifecycle()
    val appConfig by viewModel.appConfig.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val mascotMessage by viewModel.mascotMessage.collectAsStateWithLifecycle()
    val busy by viewModel.busyAction.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()

    val profile = (profileState as? UiState.Success)?.data ?: UserProfile(
        userId = currentUserId,
        displayName = Firebase.auth.currentUser?.displayName ?: "Creator",
        email = Firebase.auth.currentUser?.email.orEmpty()
    )

    val unreadNotifs = notifications.count { !it.read }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    CreatorBoostTheme(darkTheme = profile.darkMode) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val isPrimaryTab = currentRoute in setOf(Routes.SERVICES, Routes.WALLET, Routes.HOME, Routes.ACCOUNT)

        fun navigateToTab(route: String) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = if (isPrimaryTab) 76.dp else 12.dp)
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(innerPadding)
            ) {
                if (profileState is UiState.Loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = RaspberryPrimary)
                    }
                } else if (profile.blocked && !viewModel.isAdmin) {
                    StatusGateOverlay(
                        icon = Icons.Filled.Block,
                        title = "Account Suspended",
                        message = "Your account has been temporarily restricted by the administrator. Contact support if you believe this is a mistake.",
                        onSignOut = { signOutUser(credentialManager, scope, onSignOutComplete = onSignedOut) }
                    )
                } else if (appConfig.maintenanceMode && !viewModel.isAdmin) {
                    StatusGateOverlay(
                        icon = Icons.Filled.Build,
                        title = "Maintenance in Progress",
                        message = appConfig.maintenanceMessage.ifBlank {
                            "Fame Maker is currently undergoing scheduled maintenance. Please check back shortly!"
                        },
                        onSignOut = { signOutUser(credentialManager, scope, onSignOutComplete = onSignedOut) }
                    )
                } else {
                    NavHost(
                        navController = navController,
                        startDestination = Routes.SERVICES,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(Routes.SERVICES) {
                            ServicesScreen(
                                profile = profile,
                                categories = categories,
                                serviceFiles = serviceFiles,
                                serviceOrdersCount = myServiceOrders.size,
                                appConfig = appConfig,
                                onCategorySelected = { catId ->
                                    navController.navigate(Routes.categoryFiles(catId))
                                },
                                onOpenServiceOrders = { navController.navigate(Routes.SERVICE_ORDERS) },
                                onOpenAddFunds = { navController.navigate(Routes.ADD_FUNDS) },
                                onOpenAiStudio = { navController.navigate(Routes.AI_STUDIO) },
                                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                                onShowMessage = { viewModel.showToast(it) }
                            )
                        }

                        composable(Routes.WALLET) {
                            WalletScreen(
                                profile = profile,
                                packages = coinPackages,
                                busy = busy,
                                onPurchasePackage = { pkg, onSuccess ->
                                    viewModel.purchasePackage(pkg, onSuccess)
                                },
                                onOpenAddFunds = { navController.navigate(Routes.ADD_FUNDS) },
                                onOpenHistory = { navController.navigate(Routes.WALLET_HISTORY) },
                                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
                            )
                        }

                        composable(Routes.HOME) {
                            HomeScreen(
                                profile = profile,
                                categories = categories,
                                services = services,
                                appConfig = appConfig,
                                unreadNotifs = unreadNotifs,
                                onOpenCategory = { catId ->
                                    navController.navigate(Routes.categoryFiles(catId))
                                },
                                onOpenService = { svc ->
                                    navController.navigate(Routes.createOrder(svc.id))
                                },
                                onOpenServicesTab = { navigateToTab(Routes.SERVICES) },
                                onOpenWalletTab = { navigateToTab(Routes.WALLET) },
                                onOpenDailyRewards = { navController.navigate(Routes.DAILY_REWARDS) },
                                onOpenSpinWheel = { navController.navigate(Routes.SPIN_WHEEL) },
                                onOpenAiStudio = { navController.navigate(Routes.AI_STUDIO) },
                                onOpenOrders = { navController.navigate(Routes.SERVICE_ORDERS) },
                                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                                onClaimDailyQuick = { viewModel.claimDailyReward() }
                            )
                        }

                        composable(Routes.ACCOUNT) {
                            AccountScreen(
                                profile = profile,
                                isAdmin = viewModel.isAdmin,
                                unreadNotifs = unreadNotifs,
                                onOpenOrders = { navController.navigate(Routes.SERVICE_ORDERS) },
                                onOpenPayments = { navController.navigate(Routes.ADD_FUNDS) },
                                onOpenFavorites = { navController.navigate(Routes.FAVORITES) },
                                onOpenDailyRewards = { navController.navigate(Routes.DAILY_REWARDS) },
                                onOpenSpinWheel = { navController.navigate(Routes.SPIN_WHEEL) },
                                onOpenReferEarn = { navController.navigate(Routes.REFER_EARN) },
                                onOpenAiStudio = { navController.navigate(Routes.AI_STUDIO) },
                                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                                onOpenHelpSupport = { navController.navigate(Routes.HELP_SUPPORT) },
                                onOpenAdminPanel = { navController.navigate(Routes.ADMIN_PANEL) },
                                onLogout = {
                                    signOutUser(credentialManager, scope, onSignOutComplete = onSignedOut)
                                }
                            )
                        }

                        composable(Routes.CATEGORY_FILES) { backStack ->
                            val categoryId = backStack.arguments?.getString("categoryId").orEmpty()
                            CategoryFilesScreen(
                                categoryId = categoryId,
                                profile = profile,
                                categories = categories,
                                serviceFiles = serviceFiles,
                                servicePlans = servicePlans,
                                onBack = { navController.popBackStack() },
                                onFileSelected = { fileId ->
                                    navController.navigate(Routes.filePlans(fileId))
                                },
                                onOpenAddFunds = { navController.navigate(Routes.ADD_FUNDS) }
                            )
                        }

                        composable(Routes.FILE_PLANS) { backStack ->
                            val fileId = backStack.arguments?.getString("fileId").orEmpty()
                            FilePlansScreen(
                                fileId = fileId,
                                profile = profile,
                                serviceFiles = serviceFiles,
                                servicePlans = servicePlans,
                                onBack = { navController.popBackStack() },
                                onPlanSelected = { planId ->
                                    navController.navigate(Routes.planCheckout(planId))
                                },
                                onOpenAddFunds = { navController.navigate(Routes.ADD_FUNDS) }
                            )
                        }

                        composable(Routes.PLAN_CHECKOUT) { backStack ->
                            val planId = backStack.arguments?.getString("planId").orEmpty()
                            val selectedPlan = servicePlans.firstOrNull { it.id == planId }
                            if (selectedPlan != null) {
                                SelectQuantityOrderScreen(
                                    plan = selectedPlan,
                                    profile = profile,
                                    appConfig = appConfig,
                                    busy = busy,
                                    onBack = { navController.popBackStack() },
                                    onOpenAddFunds = { navController.navigate(Routes.ADD_FUNDS) },
                                    onOrderPlaced = {
                                        navController.navigate(Routes.SERVICE_ORDERS) {
                                            popUpTo(Routes.SERVICES) { inclusive = false }
                                        }
                                    },
                                    onShowMessage = { viewModel.showToast(it) },
                                    onPlaceOrder = { p, link, qty, totalPaise, method, utr, proof, onLowBal, onOk ->
                                        viewModel.placeServiceOrder(
                                            plan = p,
                                            targetLink = link,
                                            quantity = qty,
                                            totalPricePaise = totalPaise,
                                            paymentMethod = method,
                                            utrNumber = utr,
                                            paymentProofUrl = proof,
                                            onInsufficientBalance = onLowBal,
                                            onSuccess = onOk
                                        )
                                    }
                                )
                            } else {
                                LaunchedEffect(Unit) { navController.popBackStack() }
                            }
                        }

                        composable(Routes.SERVICE_ORDERS) {
                            ServiceOrdersScreen(
                                profile = profile,
                                orders = myServiceOrders,
                                onBack = { navController.popBackStack() },
                                onOpenAddFunds = { navController.navigate(Routes.ADD_FUNDS) },
                                onExploreServices = {
                                    navController.popBackStack(Routes.SERVICES, inclusive = false)
                                    navigateToTab(Routes.SERVICES)
                                },
                                onShowMessage = { viewModel.showToast(it) }
                            )
                        }

                        composable(Routes.ADD_FUNDS) {
                            AddFundsScreen(
                                profile = profile,
                                appConfig = appConfig,
                                fundRequests = myFundRequests,
                                busy = busy,
                                onBack = { navController.popBackStack() },
                                onSubmitFundRequest = { amtPaise, utr, proof, onOk ->
                                    viewModel.submitFundRequest(amtPaise, utr, proof, onOk)
                                },
                                onShowMessage = { viewModel.showToast(it) }
                            )
                        }

                        composable(Routes.CATEGORY_DETAILS) { backStack ->
                            val categoryId = backStack.arguments?.getString("categoryId").orEmpty()
                            CategoryDetailsScreen(
                                categoryId = categoryId,
                                categories = categories,
                                services = services,
                                orders = orders,
                                onBack = { navController.popBackStack() },
                                onServiceSelected = { svc ->
                                    navController.navigate(Routes.createOrder(svc.id))
                                }
                            )
                        }

                        composable(Routes.CREATE_ORDER) { backStack ->
                            val serviceId = backStack.arguments?.getString("serviceId").orEmpty()
                            val service = services.firstOrNull { it.id == serviceId } ?: services.first()
                            val existingOrder = orders.firstOrNull { it.serviceId == service.id }
                            CreateOrderScreen(
                                service = service,
                                categories = categories,
                                profile = profile,
                                existingOrder = existingOrder,
                                favorites = favorites,
                                busy = busy,
                                onBack = { navController.popBackStack() },
                                onUnlock = { lines, onInsufficient, onUnlocked ->
                                    viewModel.unlockService(service, lines, onInsufficient, onUnlocked)
                                },
                                onRegenerateAi = { orderId, onResult ->
                                    viewModel.regenerateServiceLines(service, orderId, onResult)
                                },
                                onToggleFavorite = { text ->
                                    viewModel.toggleFavorite(service.id, service.name, service.categoryName, text)
                                },
                                onGoToWallet = {
                                    navController.popBackStack(Routes.SERVICES, inclusive = false)
                                    navigateToTab(Routes.WALLET)
                                },
                                onShowMessage = { viewModel.showToast(it) }
                            )
                        }

                        composable(Routes.WALLET_HISTORY) {
                            WalletHistoryScreen(
                                transactions = transactions,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.ORDERS) {
                            MyOrdersScreen(
                                orders = orders,
                                onBack = { navController.popBackStack() },
                                onShowMessage = { viewModel.showToast(it) }
                            )
                        }

                        composable(Routes.FAVORITES) {
                            FavoritesScreen(
                                favorites = favorites,
                                onBack = { navController.popBackStack() },
                                onRemove = { viewModel.removeFavoriteById(it) },
                                onShowMessage = { viewModel.showToast(it) }
                            )
                        }

                        composable(Routes.DAILY_REWARDS) {
                            DailyRewardsScreen(
                                profile = profile,
                                busy = busy,
                                onBack = { navController.popBackStack() },
                                onClaim = { viewModel.claimDailyReward() }
                            )
                        }

                        composable(Routes.SPIN_WHEEL) {
                            SpinWheelScreen(
                                profile = profile,
                                onBack = { navController.popBackStack() },
                                onSpinComplete = { seg -> viewModel.recordSpin(seg) }
                            )
                        }

                        composable(Routes.REFER_EARN) {
                            ReferEarnScreen(
                                profile = profile,
                                busy = busy,
                                onBack = { navController.popBackStack() },
                                onRedeemCode = { code, done -> viewModel.redeemReferral(code, done) },
                                onShowMessage = { viewModel.showToast(it) }
                            )
                        }

                        composable(Routes.AI_STUDIO) {
                            AiStudioScreen(
                                aiState = aiState,
                                onBack = { navController.popBackStack() },
                                onGenerate = { tool, topic, tone, count ->
                                    viewModel.generateAiContent(tool, topic, tone, count)
                                },
                                onSaveFavorite = { title, cat, text ->
                                    viewModel.toggleFavorite("ai_${title.hashCode()}", title, cat, text)
                                },
                                onShowMessage = { viewModel.showToast(it) }
                            )
                        }

                        composable(Routes.NOTIFICATIONS) {
                            NotificationsScreen(
                                notifications = notifications,
                                onBack = { navController.popBackStack() },
                                onMarkRead = { viewModel.markNotificationRead(it) },
                                onMarkAllRead = { viewModel.markAllNotificationsRead() },
                                onDelete = { viewModel.deleteNotification(it) }
                            )
                        }

                        composable(Routes.SETTINGS) {
                            SettingsScreen(
                                profile = profile,
                                onBack = { navController.popBackStack() },
                                onSavePreferences = { name, country, mascot, dark ->
                                    viewModel.updatePreferences(name, country, mascot, dark)
                                }
                            )
                        }

                        composable(Routes.HELP_SUPPORT) {
                            HelpSupportScreen(
                                tickets = supportTickets,
                                busy = busy,
                                onBack = { navController.popBackStack() },
                                onSubmitTicket = { subj, cat, desc, done ->
                                    viewModel.submitTicket(subj, cat, desc, done)
                                }
                            )
                        }

                        composable(Routes.ADMIN_PANEL) {
                            AdminPanelScreen(
                                busy = busy,
                                onBack = { navController.popBackStack() },
                                onAddService = { catId, catName, name, type, price, trending, lines, done ->
                                    viewModel.adminAddService(catId, catName, name, type, price, trending, lines, done)
                                },
                                onAddCoinPackage = { name, coins, bonus, pts, inr, badge, done ->
                                    viewModel.adminAddCoinPackage(name, coins, bonus, pts, inr, badge, done)
                                }
                            )
                        }
                    }
                }

                val showChrome = isPrimaryTab && profileState is UiState.Success &&
                    !(profile.blocked && !viewModel.isAdmin) &&
                    !(appConfig.maintenanceMode && !viewModel.isAdmin)

                // Cute Floating Mascot Companion (Exact signature vibe of the reference screenshots!)
                if (showChrome && currentRoute != Routes.WALLET) {
                    FloatingMascotCompanion(
                        visible = profile.mascotEnabled,
                        message = mascotMessage,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .navigationBarsPadding()
                            .padding(end = 18.dp, bottom = 86.dp)
                    )
                }

                // Clean Fixed 4-Tab Bottom Navigation Bar (Services, Wallet, Home, Account — NO Leaderboard)
                if (showChrome) {
                    CreatorBoostBottomBar(
                        currentRoute = currentRoute,
                        onSelectTab = ::navigateToTab,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreatorBoostBottomBar(
    currentRoute: String?,
    onSelectTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomTabs.forEach { tab ->
                val selected = currentRoute == tab.route
                val tint = if (selected) RaspberryPrimary else MaterialTheme.colorScheme.onSurface
                val labelColor = if (selected) RaspberryPrimary else TextSecondary

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectTab(tab.route) }
                        .padding(vertical = 4.dp)
                        .testTag("nav_tab_${tab.route}")
                ) {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                        tint = tint,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = labelColor
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusGateOverlay(
    icon: ImageVector,
    title: String,
    message: String,
    onSignOut: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = RaspberryPrimary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary),
                modifier = Modifier.testTag("gate_sign_out_btn")
            ) {
                Text("Sign Out")
            }
        }
    }
}
