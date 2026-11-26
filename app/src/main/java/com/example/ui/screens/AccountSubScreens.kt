package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AppNotification
import com.example.data.DefaultCatalog
import com.example.data.FavoritePack
import com.example.data.OrderRecord
import com.example.data.SupportTicket
import com.example.data.UserProfile
import com.example.ui.components.BlueStarCoin
import com.example.ui.components.GoldFlameCoin
import com.example.ui.components.HeaderIconButton
import com.example.ui.components.LevelGreenBadge
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RaspberryLight
import com.example.ui.theme.RaspberryPrimary
import com.example.ui.theme.SoftCardBg
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AccountScreen(
    profile: UserProfile,
    isAdmin: Boolean,
    unreadNotifs: Int,
    onOpenOrders: () -> Unit,
    onOpenPayments: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenDailyRewards: () -> Unit,
    onOpenSpinWheel: () -> Unit,
    onOpenReferEarn: () -> Unit,
    onOpenAiStudio: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHelpSupport: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onLogout: () -> Unit
) {
    val formatter = remember { NumberFormat.getIntegerInstance(Locale.US) }
    var showLevelDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header ("Account" + Notifications icon)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                HeaderIconButton(
                    icon = Icons.Filled.NotificationsNone,
                    contentDescription = "Notifications",
                    badgeCount = unreadNotifs,
                    onClick = onOpenNotifications,
                    testTag = "account_notifications_btn"
                )
            }
        }

        // Profile Row (Exact match to Account screenshot!)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onOpenSettings)
                    .padding(vertical = 6.dp)
                    .testTag("account_profile_row"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = SoftCardBg,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.PersonOutline,
                            contentDescription = "Profile Avatar",
                            tint = TextSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (profile.email.isNotBlank()) {
                        Text(
                            text = profile.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Edit Profile",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 3 Big Centered Stat Items (Exact match to Account screenshot!)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    GoldFlameCoin(size = 56.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = formatter.format(profile.coins),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your Coins",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showLevelDialog = true }
                        .padding(vertical = 4.dp)
                ) {
                    LevelGreenBadge(size = 56.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = profile.level.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your Level",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    BlueStarCoin(size = 56.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = formatter.format(profile.points),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your Points",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Clean Menu List (NO Leaderboard!)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                AccountMenuRow(Icons.Filled.ShoppingCart, "Orders", onOpenOrders, "menu_orders")
                AccountMenuRow(Icons.Filled.History, "Payments & Wallet History", onOpenPayments, "menu_payments")
                AccountMenuRow(Icons.Filled.FavoriteBorder, "My Favorites", onOpenFavorites, "menu_favorites")
                AccountMenuRow(Icons.Filled.CardGiftcard, "Daily Rewards", onOpenDailyRewards, "menu_daily")
                AccountMenuRow(Icons.Filled.Casino, "Spin & Win", onOpenSpinWheel, "menu_spin")
                AccountMenuRow(Icons.Filled.Share, "Refer & Earn", onOpenReferEarn, "menu_refer")
                AccountMenuRow(Icons.Filled.AutoAwesome, "AI Creator Studio", onOpenAiStudio, "menu_ai_studio")
                AccountMenuRow(Icons.Filled.Settings, "App Settings", onOpenSettings, "menu_settings")
                AccountMenuRow(Icons.AutoMirrored.Filled.HelpOutline, "Help & Support", onOpenHelpSupport, "menu_help")
                if (isAdmin) {
                    AccountMenuRow(Icons.Filled.AdminPanelSettings, "Admin Console", onOpenAdminPanel, "menu_admin")
                }

                HorizontalDivider(
                    color = SubtleBorder,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                AccountMenuRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    label = "Logout",
                    onClick = { showLogoutDialog = true },
                    testTag = "menu_logout",
                    tint = RaspberryPrimary
                )
            }
        }
    }

    if (showLevelDialog) {
        AlertDialog(
            onDismissRequest = { showLevelDialog = false },
            title = { Text("Creator Levels", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    DefaultCatalog.levelTiers.forEach { tier ->
                        val reached = profile.points >= tier.requiredPoints
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Lv ${tier.level} • ${tier.title}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (reached) RaspberryPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = tier.rewardNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "${tier.requiredPoints} pts",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (reached) SuccessGreen else TextSecondary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLevelDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary)
                ) {
                    Text("Close", color = PureWhite)
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out?", fontWeight = FontWeight.Bold) },
            text = { Text("Your coins, orders, and favorites stay safely synced to your Google account.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary),
                    modifier = Modifier.testTag("confirm_logout_btn")
                ) {
                    Text("Logout", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AccountMenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = tint
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary
        )
    }
}

@Composable
fun MyOrdersScreen(
    orders: List<OrderRecord>,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    var selectedOrder by remember { mutableStateOf<OrderRecord?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeaderIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    onClick = onBack,
                    testTag = "orders_back_btn"
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "My Orders",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (orders.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SoftCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No unlocked packs yet. Visit Services to unlock your first hashtag or caption pack!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(orders, key = { it.id }) { order ->
                val dateLabel = order.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "Today"
                Surface(
                    onClick = { selectedOrder = order },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = order.serviceName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${order.categoryName} • ${order.itemCount} items • $dateLabel",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE3F7EA)
                            ) {
                                Text(
                                    text = order.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GoldFlameCoin(size = 20.dp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = order.coinPrice.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    selectedOrder?.let { order ->
        val fullText = order.contentLines.joinToString(", ")
        AlertDialog(
            onDismissRequest = { selectedOrder = null },
            title = { Text(order.serviceName, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(text = fullText, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(fullText))
                        onShowMessage("Copied ${order.serviceName}!")
                        selectedOrder = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = PureWhite)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy All", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedOrder = null }) { Text("Close") }
            }
        )
    }
}

@Composable
fun FavoritesScreen(
    favorites: List<FavoritePack>,
    onBack: () -> Unit,
    onRemove: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeaderIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    onClick = onBack,
                    testTag = "favorites_back_btn"
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text("My Favorites", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }

        if (favorites.isEmpty()) {
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = SoftCardBg, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No favorites saved yet. Tap the heart icon on any pack or AI Studio output to bookmark it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(favorites, key = { it.id }) { fav ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(fav.serviceName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(fav.categoryName, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Row {
                                IconButton(onClick = {
                                    clipboard.setText(AnnotatedString(fav.contentText))
                                    onShowMessage("Copied to clipboard!")
                                }) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = RaspberryPrimary)
                                }
                                IconButton(onClick = { onRemove(fav.id) }) {
                                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Remove", tint = TextSecondary)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = fav.contentText,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(
    notifications: List<AppNotification>,
    onBack: () -> Unit,
    onMarkRead: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HeaderIconButton(
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        onClick = onBack,
                        testTag = "notifs_back_btn"
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text("Notifications", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                if (notifications.any { !it.read }) {
                    HeaderIconButton(
                        icon = Icons.Filled.DoneAll,
                        contentDescription = "Mark All Read",
                        onClick = onMarkAllRead,
                        testTag = "mark_all_read_btn"
                    )
                }
            }
        }

        if (notifications.isEmpty()) {
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = SoftCardBg, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "You're all caught up!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(notifications, key = { it.id }) { notif ->
                Surface(
                    onClick = { if (!notif.read) onMarkRead(notif.id) },
                    shape = RoundedCornerShape(18.dp),
                    color = if (notif.read) MaterialTheme.colorScheme.surface else RaspberryLight.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(notif.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(notif.body, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        IconButton(onClick = { onDelete(notif.id) }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    profile: UserProfile,
    onBack: () -> Unit,
    onSavePreferences: (displayName: String?, country: String?, mascotEnabled: Boolean?, darkMode: Boolean?) -> Unit
) {
    var name by remember(profile.displayName) { mutableStateOf(profile.displayName) }
    var country by remember(profile.country) { mutableStateOf(profile.country) }
    val countries = listOf("India", "United States", "United Kingdom", "Canada", "UAE", "Australia")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HeaderIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                onClick = onBack,
                testTag = "settings_back_btn"
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Profile Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("settings_name_input")
                )
                Text("Country", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    countries.take(3).forEach { c ->
                        FilterChip(
                            selected = country == c,
                            onClick = { country = c },
                            label = { Text(c) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RaspberryPrimary,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }
                Button(
                    onClick = { onSavePreferences(name, country, null, null) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("settings_save_btn")
                ) {
                    Text("Save Profile", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("App Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Floating Flame Mascot", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Show helpful mascot companion across tabs", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Switch(
                        checked = profile.mascotEnabled,
                        onCheckedChange = { onSavePreferences(null, null, it, null) },
                        colors = SwitchDefaults.colors(checkedTrackColor = RaspberryPrimary),
                        modifier = Modifier.testTag("settings_mascot_switch")
                    )
                }
                HorizontalDivider(color = SubtleBorder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dark Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Switch between crisp light and dark theme", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Switch(
                        checked = profile.darkMode,
                        onCheckedChange = { onSavePreferences(null, null, null, it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = RaspberryPrimary),
                        modifier = Modifier.testTag("settings_dark_mode_switch")
                    )
                }
            }
        }
    }
}

@Composable
fun HelpSupportScreen(
    tickets: List<SupportTicket>,
    busy: Boolean,
    onBack: () -> Unit,
    onSubmitTicket: (subject: String, category: String, description: String, onDone: () -> Unit) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Order Issue") }
    var description by remember { mutableStateOf("") }
    val categories = listOf("Order Issue", "Coin Wallet", "Account", "Feedback")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeaderIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    onClick = onBack,
                    testTag = "help_back_btn"
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text("Help & Support", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Submit a Support Ticket", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.take(3).forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RaspberryPrimary,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("ticket_subject_input")
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Describe your issue") },
                        minLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("ticket_desc_input")
                    )
                    Button(
                        onClick = {
                            onSubmitTicket(subject, category, description) {
                                subject = ""
                                description = ""
                            }
                        },
                        enabled = !busy && subject.trim().length >= 3 && description.trim().length >= 5,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_ticket_btn")
                    ) {
                        Text("Submit Ticket", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (tickets.isNotEmpty()) {
            item {
                Text("Your Tickets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(tickets, key = { it.id }) { ticket ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(ticket.subject, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(ticket.status, style = MaterialTheme.typography.labelSmall, color = RaspberryPrimary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(ticket.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPanelScreen(
    busy: Boolean,
    onBack: () -> Unit,
    onAddService: (String, String, String, String, Long, Boolean, List<String>, () -> Unit) -> Unit,
    onAddCoinPackage: (String, Long, Long, Long, Long, String, () -> Unit) -> Unit
) {
    var svcCategory by remember { mutableStateOf(DefaultCatalog.categories.first()) }
    var svcName by remember { mutableStateOf("") }
    var svcPrice by remember { mutableStateOf("5") }
    var svcContent by remember { mutableStateOf("") }

    var pkgName by remember { mutableStateOf("") }
    var pkgCoins by remember { mutableStateOf("2500") }
    var pkgBonus by remember { mutableStateOf("500") }
    var pkgPoints by remember { mutableStateOf("8") }
    var pkgPriceInr by remember { mutableStateOf("899") }
    var pkgBadge by remember { mutableStateOf("Limited Offer") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HeaderIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                onClick = onBack,
                testTag = "admin_back_btn"
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text("Admin Console", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        // Publish New Service Pack
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Publish New Content Pack", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DefaultCatalog.categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = svcCategory.id == cat.id,
                            onClick = { svcCategory = cat },
                            label = { Text(cat.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RaspberryPrimary,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }
                OutlinedTextField(
                    value = svcName,
                    onValueChange = { svcName = it },
                    label = { Text("Pack Name (e.g. 25 Viral Food Tags)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = svcPrice,
                    onValueChange = { svcPrice = it.filter(Char::isDigit) },
                    label = { Text("Coin Price") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = svcContent,
                    onValueChange = { svcContent = it },
                    label = { Text("Hashtags or Captions (comma or newline separated)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val lines = svcContent.split(",", "\n").map { it.trim() }.filter { it.isNotBlank() }
                        onAddService(
                            svcCategory.id,
                            svcCategory.name,
                            svcName,
                            "HASHTAGS",
                            svcPrice.toLongOrNull() ?: 3L,
                            true,
                            lines
                        ) {
                            svcName = ""
                            svcContent = ""
                        }
                    },
                    enabled = !busy && svcName.isNotBlank() && svcContent.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Publish Pack to App", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Add Coin Package
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Add Custom Coin Package", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = pkgName, onValueChange = { pkgName = it }, label = { Text("Package Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = pkgCoins, onValueChange = { pkgCoins = it.filter(Char::isDigit) }, label = { Text("Coins") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = pkgBonus, onValueChange = { pkgBonus = it.filter(Char::isDigit) }, label = { Text("Bonus") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = pkgPoints, onValueChange = { pkgPoints = it.filter(Char::isDigit) }, label = { Text("Points") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = pkgPriceInr, onValueChange = { pkgPriceInr = it.filter(Char::isDigit) }, label = { Text("Price (₹)") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = pkgBadge, onValueChange = { pkgBadge = it }, label = { Text("Badge Label") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(
                    onClick = {
                        onAddCoinPackage(
                            pkgName,
                            pkgCoins.toLongOrNull() ?: 500L,
                            pkgBonus.toLongOrNull() ?: 0L,
                            pkgPoints.toLongOrNull() ?: 2L,
                            pkgPriceInr.toLongOrNull() ?: 249L,
                            pkgBadge
                        ) { pkgName = "" }
                    },
                    enabled = !busy && pkgName.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Save Coin Package", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
