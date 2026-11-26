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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.admin.data.AdminAppConfig
import com.example.admin.data.AdminStaffEntry
import com.example.admin.theme.AdminBorder
import com.example.admin.theme.AdminDanger
import com.example.admin.theme.AdminGreen
import com.example.admin.theme.AdminPrimary
import com.example.admin.theme.AdminTextSecondary

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
