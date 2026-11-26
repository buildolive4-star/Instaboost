package com.example.admin.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.admin.data.AdminAppConfig
import com.example.admin.data.AdminCatalogService
import com.example.admin.data.AdminCoinPackage
import com.example.admin.data.AdminStaffEntry
import com.example.admin.theme.AdminBlue
import com.example.admin.theme.AdminBorder
import com.example.admin.theme.AdminDanger
import com.example.admin.theme.AdminGreen
import com.example.admin.theme.AdminPrimary
import com.example.admin.theme.AdminPrimaryLight
import com.example.admin.theme.AdminTextSecondary

@Composable
fun AdminCatalogTab(
    services: List<AdminCatalogService>,
    packages: List<AdminCoinPackage>,
    busy: Boolean,
    onSaveService: (String?, String, String, String, String, Long, Boolean, Boolean, List<String>, () -> Unit) -> Unit,
    onDeleteService: (AdminCatalogService) -> Unit,
    onSavePackage: (String?, String, Long, Long, Long, Long, String, Long, Boolean, () -> Unit) -> Unit,
    onDeletePackage: (AdminCoinPackage) -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) } // 0 = Services, 1 = Coin Packages
    var editingService by remember { mutableStateOf<AdminCatalogService?>(null) }
    var showServiceDialog by remember { mutableStateOf(false) }
    var editingPackage by remember { mutableStateOf<AdminCoinPackage?>(null) }
    var showPackageDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = subTab == 0,
                    onClick = { subTab = 0 },
                    label = { Text("Services (${services.size})", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = subTab == 1,
                    onClick = { subTab = 1 },
                    label = { Text("Coin Packs (${packages.size})", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (subTab == 0) {
            item {
                Button(
                    onClick = {
                        editingService = null
                        showServiceDialog = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_add_service_btn")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add New Service Pack", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            items(services, key = { it.id }) { svc ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, AdminBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(svc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${svc.categoryName} • ${svc.packType} • ${svc.coinPrice} coins • ${if (svc.published) "Live" else "Hidden"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AdminTextSecondary
                            )
                        }
                        Row {
                            IconButton(onClick = {
                                editingService = svc
                                showServiceDialog = true
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = AdminBlue)
                            }
                            IconButton(onClick = { onDeleteService(svc) }) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = AdminDanger)
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Button(
                    onClick = {
                        editingPackage = null
                        showPackageDialog = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_add_package_btn")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Coin Package", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            items(packages, key = { it.id }) { pkg ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, AdminBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pkg.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${pkg.coins} (+${pkg.bonusCoins} bonus) • ₹${pkg.priceInr} • +${pkg.points} pts • ${if (pkg.active) "Active" else "Paused"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AdminTextSecondary
                            )
                        }
                        Row {
                            IconButton(onClick = {
                                editingPackage = pkg
                                showPackageDialog = true
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = AdminBlue)
                            }
                            IconButton(onClick = { onDeletePackage(pkg) }) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = AdminDanger)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showServiceDialog) {
        ServiceEditorDialog(
            initial = editingService,
            busy = busy,
            onDismiss = { showServiceDialog = false },
            onSave = { catId, catName, name, type, price, trending, published, lines ->
                onSaveService(
                    editingService?.id, catId, catName, name, type, price, trending, published, lines
                ) { showServiceDialog = false }
            }
        )
    }

    if (showPackageDialog) {
        PackageEditorDialog(
            initial = editingPackage,
            busy = busy,
            onDismiss = { showPackageDialog = false },
            onSave = { name, coins, bonus, pts, inr, badge, order, active ->
                onSavePackage(
                    editingPackage?.id, name, coins, bonus, pts, inr, badge, order, active
                ) { showPackageDialog = false }
            }
        )
    }
}

@Composable
private fun ServiceEditorDialog(
    initial: AdminCatalogService?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Long, Boolean, Boolean, List<String>) -> Unit
) {
    val categories = listOf(
        "food" to "Food",
        "christmas" to "Christmas",
        "fashion" to "Fashion",
        "travel" to "Travel",
        "fitness" to "Fitness",
        "photography" to "Photography",
        "captions" to "Captions",
        "bios" to "Creator Bios"
    )
    var selectedCatIndex by remember {
        mutableIntStateOf(categories.indexOfFirst { it.first == initial?.categoryId }.coerceAtLeast(0))
    }
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var packType by remember { mutableStateOf(initial?.packType ?: "HASHTAGS") }
    var coinPrice by remember { mutableStateOf((initial?.coinPrice ?: 5L).toString()) }
    var trending by remember { mutableStateOf(initial?.trending ?: true) }
    var published by remember { mutableStateOf(initial?.published ?: true) }
    var contentText by remember { mutableStateOf(initial?.contentLines?.joinToString("\n").orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New Service Pack" else "Edit Service Pack", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Pack Name (e.g. 30 Viral Reels)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.take(4).forEachIndexed { idx, (_, label) ->
                        FilterChip(
                            selected = selectedCatIndex == idx,
                            onClick = { selectedCatIndex = idx },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("HASHTAGS", "CAPTIONS", "BIOS").forEach { t ->
                        FilterChip(
                            selected = packType == t,
                            onClick = { packType = t },
                            label = { Text(t, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = coinPrice,
                    onValueChange = { coinPrice = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Coin Price") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    label = { Text("Lines (one per line)") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Published Live")
                    Switch(checked = published, onCheckedChange = { published = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show in Trending")
                    Switch(checked = trending, onCheckedChange = { trending = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val (catId, catName) = categories[selectedCatIndex]
                    val lines = contentText.lines().map { it.trim() }.filter { it.isNotBlank() }
                    onSave(catId, catName, name, packType, coinPrice.toLongOrNull() ?: 5L, trending, published, lines)
                },
                enabled = !busy && name.isNotBlank() && contentText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun PackageEditorDialog(
    initial: AdminCoinPackage?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Long, Long, Long, Long, String, Long, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var coins by remember { mutableStateOf((initial?.coins ?: 1000L).toString()) }
    var bonus by remember { mutableStateOf((initial?.bonusCoins ?: 250L).toString()) }
    var points by remember { mutableStateOf((initial?.points ?: 5L).toString()) }
    var priceInr by remember { mutableStateOf((initial?.priceInr ?: 399L).toString()) }
    var badge by remember { mutableStateOf(initial?.badge.orEmpty()) }
    var active by remember { mutableStateOf(initial?.active ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New Coin Package" else "Edit Coin Package", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Package Title") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = coins,
                        onValueChange = { coins = it.filter { c -> c.isDigit() } },
                        label = { Text("Coins") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = bonus,
                        onValueChange = { bonus = it.filter { c -> c.isDigit() } },
                        label = { Text("Bonus") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceInr,
                        onValueChange = { priceInr = it.filter { c -> c.isDigit() } },
                        label = { Text("Price ₹") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = points,
                        onValueChange = { points = it.filter { c -> c.isDigit() } },
                        label = { Text("Points") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(value = badge, onValueChange = { badge = it }, label = { Text("Badge (e.g. Best Value)") }, singleLine = true)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active in Wallet")
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        coins.toLongOrNull() ?: 500L,
                        bonus.toLongOrNull() ?: 0L,
                        points.toLongOrNull() ?: 2L,
                        priceInr.toLongOrNull() ?: 249L,
                        badge,
                        initial?.sortOrder ?: 10L,
                        active
                    )
                },
                enabled = !busy && name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminControlTab(
    config: AdminAppConfig,
    userCount: Int,
    busy: Boolean,
    onSaveConfig: (AdminAppConfig) -> Unit,
    onBroadcast: (String, String, () -> Unit) -> Unit
) {
    var maintenanceMode by remember(config) { mutableStateOf(config.maintenanceMode) }
    var maintenanceMessage by remember(config) { mutableStateOf(config.maintenanceMessage) }
    var announcementTitle by remember(config) { mutableStateOf(config.announcementTitle) }
    var announcementBody by remember(config) { mutableStateOf(config.announcementBody) }
    var welcomeCoins by remember(config) { mutableStateOf(config.welcomeCoins.toString()) }
    var referralBonusCoins by remember(config) { mutableStateOf(config.referralBonusCoins.toString()) }
    var spinEnabled by remember(config) { mutableStateOf(config.spinEnabled) }
    var dailyRewardsEnabled by remember(config) { mutableStateOf(config.dailyRewardsEnabled) }

    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastBody by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Live Feature Switches & App Settings
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, AdminBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Live User Panel Controls", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    ControlSwitchRow("Maintenance Mode", "Lock user panel with custom message", maintenanceMode) {
                        maintenanceMode = it
                    }
                    if (maintenanceMode) {
                        OutlinedTextField(
                            value = maintenanceMessage,
                            onValueChange = { maintenanceMessage = it },
                            label = { Text("Maintenance Message") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    ControlSwitchRow("Daily Rewards", "Enable daily check-in bonuses", dailyRewardsEnabled) {
                        dailyRewardsEnabled = it
                    }
                    ControlSwitchRow("Spin & Win Wheel", "Enable lucky spin wheel", spinEnabled) {
                        spinEnabled = it
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = welcomeCoins,
                            onValueChange = { welcomeCoins = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Welcome Coins") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = referralBonusCoins,
                            onValueChange = { referralBonusCoins = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Referral Bonus") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = announcementTitle,
                        onValueChange = { announcementTitle = it },
                        label = { Text("Home Banner Title (leave blank to hide)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = announcementBody,
                        onValueChange = { announcementBody = it },
                        label = { Text("Home Banner Message") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onSaveConfig(
                                AdminAppConfig(
                                    maintenanceMode = maintenanceMode,
                                    maintenanceMessage = maintenanceMessage,
                                    announcementTitle = announcementTitle,
                                    announcementBody = announcementBody,
                                    welcomeCoins = welcomeCoins.toLongOrNull() ?: 15L,
                                    referralBonusCoins = referralBonusCoins.toLongOrNull() ?: 25L,
                                    spinEnabled = spinEnabled,
                                    dailyRewardsEnabled = dailyRewardsEnabled
                                )
                            )
                        },
                        enabled = !busy,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_save_config_btn")
                    ) {
                        Text("Publish Live Settings", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Broadcast Notification to All Users
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, AdminBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Campaign, contentDescription = null, tint = AdminPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Broadcast to All Users ($userCount)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    OutlinedTextField(
                        value = broadcastTitle,
                        onValueChange = { broadcastTitle = it },
                        label = { Text("Notification Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = broadcastBody,
                        onValueChange = { broadcastBody = it },
                        label = { Text("Message to all users") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = {
                            onBroadcast(broadcastTitle, broadcastBody) {
                                broadcastTitle = ""
                                broadcastBody = ""
                            }
                        },
                        enabled = !busy && broadcastTitle.isNotBlank() && broadcastBody.isNotBlank() && userCount > 0,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_broadcast_btn")
                    ) {
                        Text("Send Broadcast Now", color = AdminPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AdminTextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = AdminPrimary)
        )
    }
}

// Visible EXCLUSIVELY to Main Admin (buildolive4@gmail.com)
@Composable
fun AdminTeamTab(
    staffList: List<AdminStaffEntry>,
    busy: Boolean,
    onCreateMember: (name: String, password: String, onDone: () -> Unit) -> Unit,
    onToggleMember: (AdminStaffEntry, Boolean) -> Unit,
    onDeleteMember: (AdminStaffEntry) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, AdminBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = AdminPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Admin Access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Enter only Name and Password. They will log in seamlessly with the full Admin experience.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AdminTextSecondary
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("team_name_input")
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showPassword) "Hide" else "Show"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("team_password_input")
                    )
                    Button(
                        onClick = {
                            onCreateMember(name, password) {
                                name = ""
                                password = ""
                            }
                        },
                        enabled = !busy && name.trim().length >= 2 && password.trim().length >= 4,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("team_create_btn")
                    ) {
                        Text("Create Admin", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "Active Admin Accounts (${staffList.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (staffList.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, AdminBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No additional admins created yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AdminTextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(staffList, key = { it.id }) { member ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, AdminBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.staffName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (member.active) "Active access" else "Paused",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (member.active) AdminGreen else AdminDanger
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = member.active,
                                onCheckedChange = { onToggleMember(member, it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = AdminPrimary)
                            )
                            IconButton(onClick = { onDeleteMember(member) }) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Remove", tint = AdminDanger)
                            }
                        }
                    }
                }
            }
        }
    }
}
