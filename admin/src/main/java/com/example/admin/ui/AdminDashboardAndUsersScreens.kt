package com.example.admin.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.admin.data.AdminAppConfig
import com.example.admin.data.AdminAuditLog
import com.example.admin.data.AdminCatalogService
import com.example.admin.data.AdminCoinPackage
import com.example.admin.data.AdminFundRequest
import com.example.admin.data.AdminOrderRecord
import com.example.admin.data.AdminServiceOrder
import com.example.admin.data.AdminServicePlan
import com.example.admin.data.AdminSupportTicket
import com.example.admin.data.AdminUserProfile
import com.example.admin.data.formatInrFromPaise
import com.example.admin.theme.AdminBlue
import com.example.admin.theme.AdminBlueBg
import com.example.admin.theme.AdminBorder
import com.example.admin.theme.AdminDanger
import com.example.admin.theme.AdminDangerBg
import com.example.admin.theme.AdminGold
import com.example.admin.theme.AdminGoldBg
import com.example.admin.theme.AdminGreen
import com.example.admin.theme.AdminGreenBg
import com.example.admin.theme.AdminPrimary
import com.example.admin.theme.AdminPrimaryLight
import com.example.admin.theme.AdminTextSecondary
import com.example.admin.theme.AdminWarmCard
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

@Composable
fun AdminOverviewTab(
    adminName: String,
    users: List<AdminUserProfile>,
    serviceOrders: List<AdminServiceOrder>,
    fundRequests: List<AdminFundRequest>,
    servicePlans: List<AdminServicePlan>,
    appConfig: AdminAppConfig,
    auditLogs: List<AdminAuditLog>,
    onToggleMaintenance: (Boolean) -> Unit,
    onOpenOrders: () -> Unit,
    onOpenUsers: () -> Unit,
    onOpenCatalog: () -> Unit,
    onOpenControl: () -> Unit
) {
    val formatter = NumberFormat.getIntegerInstance(Locale.US)
    val totalWalletPaise = users.sumOf { it.walletBalancePaise }
    val pendingOrders = serviceOrders.count { it.status == "PENDING" }
    val pendingDeposits = fundRequests.count { it.status == "PENDING" }
    val blockedCount = users.count { it.blocked }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = AdminPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Welcome, $adminName",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Full live control over categories, service files, plans, orders & payments",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }
        }

        // KPI 2x2 Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiMetricCard(
                    title = "Service Orders",
                    value = serviceOrders.size.toString(),
                    subtitle = if (pendingOrders > 0) "$pendingOrders pending" else "All processed",
                    icon = Icons.Filled.Inventory2,
                    iconTint = AdminPrimary,
                    iconBg = AdminPrimaryLight,
                    onClick = onOpenOrders,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = "UPI Deposits",
                    value = pendingDeposits.toString(),
                    subtitle = "${fundRequests.size} total requests",
                    icon = Icons.Filled.AccountBalanceWallet,
                    iconTint = AdminGreen,
                    iconBg = AdminGreenBg,
                    onClick = onOpenOrders,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiMetricCard(
                    title = "Total Users",
                    value = formatter.format(users.size),
                    subtitle = if (blockedCount > 0) "$blockedCount blocked" else "All active",
                    icon = Icons.Filled.People,
                    iconTint = AdminBlue,
                    iconBg = AdminBlueBg,
                    onClick = onOpenUsers,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = "User Wallets",
                    value = formatInrFromPaise(totalWalletPaise),
                    subtitle = "${servicePlans.count { it.active }} active plans",
                    icon = Icons.Filled.MonetizationOn,
                    iconTint = Color(0xFFD97706),
                    iconBg = AdminGoldBg,
                    onClick = onOpenCatalog,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Maintenance Switch
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (appConfig.maintenanceMode) AdminDangerBg else AdminWarmCard,
                border = BorderStroke(1.dp, if (appConfig.maintenanceMode) AdminDanger.copy(alpha = 0.4f) else AdminBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Build,
                            contentDescription = null,
                            tint = if (appConfig.maintenanceMode) AdminDanger else AdminPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Maintenance Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (appConfig.maintenanceMode) "User Panel is currently paused" else "User Panel is live for everyone",
                                style = MaterialTheme.typography.bodySmall,
                                color = AdminTextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = appConfig.maintenanceMode,
                        onCheckedChange = onToggleMaintenance,
                        colors = SwitchDefaults.colors(checkedTrackColor = AdminDanger),
                        modifier = Modifier.testTag("overview_maintenance_switch")
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onOpenControl,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = AdminPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Banners, Rewards & Broadcasts", color = AdminPrimary, fontWeight = FontWeight.SemiBold)
            }
        }

        // Recent Activity
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.History, contentDescription = null, tint = AdminTextSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Recent Admin Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (auditLogs.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, AdminBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No actions logged yet. Updates to users, catalog, or settings appear here in real time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AdminTextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(auditLogs.take(10), key = { it.id }) { log ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, AdminBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = log.action.replace("_", " "),
                                style = MaterialTheme.typography.labelLarge,
                                color = AdminPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = log.actorName,
                                style = MaterialTheme.typography.labelSmall,
                                color = AdminTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = log.summary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, AdminBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = AdminTextSecondary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = iconTint
            )
        }
    }
}

@Composable
fun AdminUsersTab(
    users: List<AdminUserProfile>,
    selectedUserId: String?,
    selectedUserOrders: List<AdminOrderRecord>,
    selectedUserTickets: List<AdminSupportTicket>,
    busy: Boolean,
    onSelectUser: (String?) -> Unit,
    onUpdateUser: (AdminUserProfile, Long, Long, Long, Long, Boolean, String) -> Unit,
    onUpdateOrderStatus: (String, String, String) -> Unit,
    onUpdateTicketStatus: (String, String, String) -> Unit,
    onSendNotification: (String, String, String, () -> Unit) -> Unit
) {
    val selectedUser = users.firstOrNull { it.userId == selectedUserId }
    if (selectedUser != null) {
        BackHandler { onSelectUser(null) }
        AdminUserDetailView(
            user = selectedUser,
            orders = selectedUserOrders,
            tickets = selectedUserTickets,
            busy = busy,
            onBack = { onSelectUser(null) },
            onSave = onUpdateUser,
            onUpdateOrderStatus = onUpdateOrderStatus,
            onUpdateTicketStatus = onUpdateTicketStatus,
            onSendNotification = onSendNotification
        )
        return
    }

    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(users, searchQuery) {
        val q = searchQuery.trim().lowercase(Locale.US)
        if (q.isBlank()) users
        else users.filter {
            it.displayName.lowercase(Locale.US).contains(q) ||
                it.email.lowercase(Locale.US).contains(q) ||
                it.referralCode.lowercase(Locale.US).contains(q)
        }
    }
    val formatter = NumberFormat.getIntegerInstance(Locale.US)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search name, email, or referral code...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_user_search")
            )
        }

        if (filtered.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, AdminBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No users found yet. Users appear automatically once they sign in to Fame Maker.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AdminTextSecondary,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        } else {
            items(filtered, key = { it.userId }) { user ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, if (user.blocked) AdminDanger.copy(alpha = 0.5f) else AdminBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectUser(user.userId) }
                        .testTag("admin_user_row_${user.userId}")
                ) {
                    Row(
                        modifier = Modifier.padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (user.blocked) AdminDangerBg else AdminPrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.displayName.take(1).uppercase(Locale.US),
                                    fontWeight = FontWeight.Bold,
                                    color = if (user.blocked) AdminDanger else AdminPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (user.blocked) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = AdminDangerBg
                                        ) {
                                            Text(
                                                text = "BLOCKED",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AdminDanger,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = user.email.ifBlank { user.referralCode },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AdminTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${formatInrFromPaise(user.walletBalancePaise)} • ${formatter.format(user.coins)} coins • Lvl ${user.level}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AdminPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Manage user",
                            tint = AdminTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminUserDetailView(
    user: AdminUserProfile,
    orders: List<AdminOrderRecord>,
    tickets: List<AdminSupportTicket>,
    busy: Boolean,
    onBack: () -> Unit,
    onSave: (AdminUserProfile, Long, Long, Long, Long, Boolean, String) -> Unit,
    onUpdateOrderStatus: (String, String, String) -> Unit,
    onUpdateTicketStatus: (String, String, String) -> Unit,
    onSendNotification: (String, String, String, () -> Unit) -> Unit
) {
    var walletInrText by remember(user.userId, user.walletBalancePaise) {
        mutableStateOf(String.format(Locale.US, "%.2f", user.walletBalancePaise / 100.0))
    }
    var coinsText by remember(user.userId, user.coins) { mutableStateOf(user.coins.toString()) }
    var pointsText by remember(user.userId, user.points) { mutableStateOf(user.points.toString()) }
    var levelText by remember(user.userId, user.level) { mutableStateOf(user.level.toString()) }
    var blocked by remember(user.userId, user.blocked) { mutableStateOf(user.blocked) }
    var reason by remember { mutableStateOf("") }

    var notifTitle by remember { mutableStateOf("") }
    var notifBody by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("user_detail_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column {
                    Text(text = user.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "${user.email} • Code ${user.referralCode}", style = MaterialTheme.typography.bodySmall, color = AdminTextSecondary)
                }
            }
        }

        // Wallet & Status Editor Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, AdminBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("INR Wallet & Account Controls", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(50.0 to "+₹50", 100.0 to "+₹100", 500.0 to "+₹500", -50.0 to "-₹50").forEach { (delta, label) ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val cur = walletInrText.toDoubleOrNull() ?: (user.walletBalancePaise / 100.0)
                                    val next = (cur + delta).coerceAtLeast(0.0)
                                    walletInrText = String.format(Locale.US, "%.2f", next)
                                },
                                label = { Text(label, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = walletInrText,
                        onValueChange = { walletInrText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("INR Wallet Balance (₹)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_wallet_inr_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = coinsText,
                            onValueChange = { coinsText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Coins") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pointsText,
                            onValueChange = { pointsText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Points") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = levelText,
                            onValueChange = { levelText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Lvl 1-10") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Adjustment note (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Block / Suspend User", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("Instantly blocks access to User Panel", style = MaterialTheme.typography.bodySmall, color = AdminTextSecondary)
                        }
                        Switch(
                            checked = blocked,
                            onCheckedChange = { blocked = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = AdminDanger),
                            modifier = Modifier.testTag("user_block_switch")
                        )
                    }

                    Button(
                        onClick = {
                            val paise = ((walletInrText.toDoubleOrNull() ?: (user.walletBalancePaise / 100.0)) * 100.0)
                                .roundToLong()
                                .coerceAtLeast(0L)
                            onSave(
                                user,
                                coinsText.toLongOrNull() ?: user.coins,
                                paise,
                                pointsText.toLongOrNull() ?: user.points,
                                (levelText.toLongOrNull() ?: user.level).coerceIn(1L, 10L),
                                blocked,
                                reason
                            )
                        },
                        enabled = !busy,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("user_save_changes_btn")
                    ) {
                        Text("Save User Changes", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Send Direct Notification
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, AdminBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Send Direct Notification", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = notifTitle,
                        onValueChange = { notifTitle = it },
                        label = { Text("Notification Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notifBody,
                        onValueChange = { notifBody = it },
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            onSendNotification(user.userId, notifTitle, notifBody) {
                                notifTitle = ""
                                notifBody = ""
                            }
                        },
                        enabled = !busy && notifTitle.isNotBlank() && notifBody.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send to ${user.displayName}", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // User Orders
        item {
            Text("User Orders (${orders.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (orders.isEmpty()) {
            item {
                Text("No orders placed by this user yet.", style = MaterialTheme.typography.bodySmall, color = AdminTextSecondary)
            }
        } else {
            items(orders, key = { it.id }) { order ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, AdminBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(order.serviceName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${order.coinPrice} coins", style = MaterialTheme.typography.labelMedium, color = AdminGold, fontWeight = FontWeight.Bold)
                        }
                        Text("${order.categoryName} • ${order.itemCount} items", style = MaterialTheme.typography.bodySmall, color = AdminTextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("COMPLETED", "PENDING", "CANCELLED").forEach { st ->
                                FilterChip(
                                    selected = order.status == st,
                                    onClick = { onUpdateOrderStatus(user.userId, order.id, st) },
                                    label = { Text(st, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Support Tickets
        item {
            Text("Support Tickets (${tickets.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (tickets.isEmpty()) {
            item {
                Text("No support tickets from this user.", style = MaterialTheme.typography.bodySmall, color = AdminTextSecondary)
            }
        } else {
            items(tickets, key = { it.id }) { ticket ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, AdminBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(ticket.subject, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(ticket.description, style = MaterialTheme.typography.bodySmall, color = AdminTextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("OPEN", "IN_PROGRESS", "RESOLVED").forEach { st ->
                                FilterChip(
                                    selected = ticket.status == st,
                                    onClick = { onUpdateTicketStatus(user.userId, ticket.id, st) },
                                    label = { Text(st.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
