package com.example.admin.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.admin.data.AdminCatalogCategory
import com.example.admin.data.AdminCoinPackage
import com.example.admin.data.AdminServiceFile
import com.example.admin.data.AdminServicePlan
import com.example.admin.data.formatRatePer1k
import com.example.admin.theme.AdminBlue
import com.example.admin.theme.AdminBorder
import com.example.admin.theme.AdminDanger
import com.example.admin.theme.AdminGreen
import com.example.admin.theme.AdminGreenBg
import com.example.admin.theme.AdminPrimary
import com.example.admin.theme.AdminTextSecondary
import kotlin.math.roundToLong

@Composable
fun AdminCatalogTab(
    categories: List<AdminCatalogCategory>,
    serviceFiles: List<AdminServiceFile>,
    servicePlans: List<AdminServicePlan>,
    packages: List<AdminCoinPackage>,
    busy: Boolean,
    onSeedSocialCatalog: () -> Unit,
    onSaveCategory: (String?, String, String, String, String, String, Long, Boolean, () -> Unit) -> Unit,
    onDeleteCategory: (AdminCatalogCategory) -> Unit,
    onSaveFile: (String?, String, String, String, String, String, String, Long, Boolean, () -> Unit) -> Unit,
    onDeleteFile: (AdminServiceFile) -> Unit,
    onSavePlan: (String?, String, String, String, String, String, String, String, Long, Long, Long, String, List<String>, Long, Boolean, () -> Unit) -> Unit,
    onDeletePlan: (AdminServicePlan) -> Unit,
    onSavePackage: (String?, String, Long, Long, Long, Long, String, Long, Boolean, () -> Unit) -> Unit,
    onDeletePackage: (AdminCoinPackage) -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) } // 0=Categories, 1=Files, 2=Plans, 3=Coins
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedFileFilter by remember { mutableStateOf("ALL") }

    var editingCategory by remember { mutableStateOf<AdminCatalogCategory?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    var editingFile by remember { mutableStateOf<AdminServiceFile?>(null) }
    var showFileDialog by remember { mutableStateOf(false) }

    var editingPlan by remember { mutableStateOf<AdminServicePlan?>(null) }
    var showPlanDialog by remember { mutableStateOf(false) }

    var editingPackage by remember { mutableStateOf<AdminCoinPackage?>(null) }
    var showPackageDialog by remember { mutableStateOf(false) }

    val filteredFiles = remember(serviceFiles, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") serviceFiles
        else serviceFiles.filter { it.categoryId == selectedCategoryFilter }
    }
    val filteredPlans = remember(servicePlans, selectedCategoryFilter, selectedFileFilter) {
        servicePlans.filter { plan ->
            (selectedCategoryFilter == "ALL" || plan.categoryId == selectedCategoryFilter) &&
                (selectedFileFilter == "ALL" || plan.fileId == selectedFileFilter)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 4 Sub-tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = subTab == 0,
                        onClick = { subTab = 0 },
                        label = { Text("Categories (${categories.size})", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("catalog_subtab_categories")
                    )
                }
                item {
                    FilterChip(
                        selected = subTab == 1,
                        onClick = { subTab = 1 },
                        label = { Text("Files (${serviceFiles.size})", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("catalog_subtab_files")
                    )
                }
                item {
                    FilterChip(
                        selected = subTab == 2,
                        onClick = { subTab = 2 },
                        label = { Text("Plans (${servicePlans.size})", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("catalog_subtab_plans")
                    )
                }
                item {
                    FilterChip(
                        selected = subTab == 3,
                        onClick = { subTab = 3 },
                        label = { Text("Coins (${packages.size})", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("catalog_subtab_coins")
                    )
                }
            }
        }

        when (subTab) {
            // --- 0: CATEGORIES (2-Column 1:1 Preview) ---
            0 -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                editingCategory = null
                                showCategoryDialog = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("admin_add_category_btn")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add New Category (1:1 ImgBB)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        if (categories.isEmpty() || serviceFiles.isEmpty()) {
                            OutlinedButton(
                                onClick = onSeedSocialCatalog,
                                enabled = !busy,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("admin_seed_social_btn")
                            ) {
                                Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = AdminPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Load Starter Social Categories & Files", color = AdminPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                if (categories.isEmpty()) {
                    item {
                        EmptyCatalogNotice(
                            "No categories created yet. Add Instagram, Facebook, Telegram, YouTube, or any custom category with a 1:1 ImgBB image — they appear in a 2-column 1:1 grid on the User Panel."
                        )
                    }
                } else {
                    items(categories.chunked(2), key = { row -> row.joinToString("_") { it.id } }) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { cat ->
                                val fileCount = serviceFiles.count { it.categoryId == cat.id }
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, AdminBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            AdminPhotoPreview(
                                                photoUrl = cat.photoUrl,
                                                fallbackEmoji = cat.emoji.ifBlank { "✨" },
                                                size = null,
                                                cornerRadius = 14.dp,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(1f)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (cat.active) AdminGreenBg else Color(0xFFF3F4F6),
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                            ) {
                                                Text(
                                                    text = if (cat.active) "LIVE" else "OFF",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (cat.active) AdminGreen else AdminTextSecondary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Text(
                                            text = "$fileCount files • #${cat.sortOrder}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AdminTextSecondary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            IconButton(onClick = {
                                                editingCategory = cat
                                                showCategoryDialog = true
                                            }) {
                                                Icon(Icons.Filled.Edit, contentDescription = "Edit category", tint = AdminBlue)
                                            }
                                            IconButton(onClick = { onDeleteCategory(cat) }) {
                                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete category", tint = AdminDanger)
                                            }
                                        }
                                    }
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // --- 1: CUSTOM SERVICE FILES INSIDE EACH CATEGORY ---
            1 -> {
                item {
                    Button(
                        onClick = {
                            editingFile = null
                            showFileDialog = true
                        },
                        enabled = categories.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_add_file_btn")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add New Service File", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                if (categories.isNotEmpty()) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedCategoryFilter == "ALL",
                                    onClick = { selectedCategoryFilter = "ALL" },
                                    label = { Text("All Categories") }
                                )
                            }
                            items(categories, key = { it.id }) { cat ->
                                FilterChip(
                                    selected = selectedCategoryFilter == cat.id,
                                    onClick = { selectedCategoryFilter = cat.id },
                                    label = { Text(cat.name) }
                                )
                            }
                        }
                    }
                }

                if (filteredFiles.isEmpty()) {
                    item {
                        EmptyCatalogNotice(
                            if (categories.isEmpty()) "Create a Category first (e.g. Instagram), then add custom files inside it like Followers, Reach, Reel Views, or Likes."
                            else "No service files in this category yet. Tap 'Add New Service File' to create custom items."
                        )
                    }
                } else {
                    items(filteredFiles, key = { it.id }) { file ->
                        val planCount = servicePlans.count { it.fileId == file.id }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, AdminBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AdminPhotoPreview(
                                    photoUrl = file.photoUrl,
                                    fallbackEmoji = file.emoji.ifBlank { "⚡" },
                                    size = 52.dp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${file.categoryName} • $planCount plans • ${if (file.active) "Live" else "Hidden"}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AdminPrimary
                                    )
                                    if (file.subtitle.isNotBlank()) {
                                        Text(
                                            text = file.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AdminTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Row {
                                    IconButton(onClick = {
                                        editingFile = file
                                        showFileDialog = true
                                    }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit file", tint = AdminBlue)
                                    }
                                    IconButton(onClick = { onDeleteFile(file) }) {
                                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete file", tint = AdminDanger)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 2: SERVICE PLANS (PRICING PER 1K & QUANTITY TIERS) ---
            2 -> {
                item {
                    Button(
                        onClick = {
                            editingPlan = null
                            showPlanDialog = true
                        },
                        enabled = serviceFiles.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_add_plan_btn")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add New Service Plan (₹/k)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                if (serviceFiles.isNotEmpty()) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedFileFilter == "ALL",
                                    onClick = { selectedFileFilter = "ALL" },
                                    label = { Text("All Files") }
                                )
                            }
                            items(serviceFiles, key = { it.id }) { f ->
                                FilterChip(
                                    selected = selectedFileFilter == f.id,
                                    onClick = { selectedFileFilter = f.id },
                                    label = { Text(f.name) }
                                )
                            }
                        }
                    }
                }

                if (filteredPlans.isEmpty()) {
                    item {
                        EmptyCatalogNotice(
                            if (serviceFiles.isEmpty()) "Create at least one Service File first, then add priced Service Plans inside it."
                            else "No service plans yet. Add a plan with ₹/1k price, min/max quantity, and discount tiers."
                        )
                    }
                } else {
                    items(filteredPlans, key = { it.id }) { plan ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, AdminBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AdminPhotoPreview(
                                    photoUrl = plan.photoUrl,
                                    fallbackEmoji = "⚡",
                                    size = 50.dp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = plan.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${plan.categoryName} › ${plan.fileName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AdminTextSecondary
                                    )
                                    Text(
                                        text = "${formatRatePer1k(plan.pricePer1kPaise)} • Qty ${plan.minQty}–${plan.maxQty} • ${if (plan.active) "Live" else "Hidden"}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AdminPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row {
                                    IconButton(onClick = {
                                        editingPlan = plan
                                        showPlanDialog = true
                                    }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit plan", tint = AdminBlue)
                                    }
                                    IconButton(onClick = { onDeletePlan(plan) }) {
                                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete plan", tint = AdminDanger)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 3: COIN PACKAGES ---
            else -> {
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
                                    text = "${pkg.coins} (+${pkg.bonusCoins} bonus) • ₹${pkg.priceInr} • ${if (pkg.active) "Active" else "Paused"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AdminTextSecondary
                                )
                            }
                            Row {
                                IconButton(onClick = {
                                    editingPackage = pkg
                                    showPackageDialog = true
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit package", tint = AdminBlue)
                                }
                                IconButton(onClick = { onDeletePackage(pkg) }) {
                                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete package", tint = AdminDanger)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCategoryDialog) {
        CategoryEditorDialog(
            initial = editingCategory,
            busy = busy,
            onDismiss = { showCategoryDialog = false },
            onSave = { name, subtitle, desc, emoji, photo, order, active ->
                onSaveCategory(editingCategory?.id, name, subtitle, desc, emoji, photo, order, active) {
                    showCategoryDialog = false
                }
            }
        )
    }

    if (showFileDialog && categories.isNotEmpty()) {
        ServiceFileEditorDialog(
            initial = editingFile,
            categories = categories,
            defaultCategoryId = selectedCategoryFilter.takeIf { it != "ALL" },
            busy = busy,
            onDismiss = { showFileDialog = false },
            onSave = { catId, catName, name, subtitle, emoji, photo, order, active ->
                onSaveFile(editingFile?.id, catId, catName, name, subtitle, emoji, photo, order, active) {
                    showFileDialog = false
                }
            }
        )
    }

    if (showPlanDialog && serviceFiles.isNotEmpty()) {
        ServicePlanEditorDialog(
            initial = editingPlan,
            files = serviceFiles,
            defaultFileId = selectedFileFilter.takeIf { it != "ALL" },
            busy = busy,
            onDismiss = { showPlanDialog = false },
            onSave = { catId, catName, fileId, fileName, name, desc, photo, pricePaise, minQ, maxQ, hint, tiers, order, active ->
                onSavePlan(
                    editingPlan?.id, catId, catName, fileId, fileName, name, desc, photo,
                    pricePaise, minQ, maxQ, hint, tiers, order, active
                ) { showPlanDialog = false }
            }
        )
    }

    if (showPackageDialog) {
        PackageEditorDialog(
            initial = editingPackage,
            busy = busy,
            onDismiss = { showPackageDialog = false },
            onSave = { name, coins, bonus, pts, inr, badge, order, active ->
                onSavePackage(editingPackage?.id, name, coins, bonus, pts, inr, badge, order, active) {
                    showPackageDialog = false
                }
            }
        )
    }
}

@Composable
private fun EmptyCatalogNotice(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, AdminBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = AdminTextSecondary,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun CategoryEditorDialog(
    initial: AdminCatalogCategory?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, Long, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var subtitle by remember { mutableStateOf(initial?.subtitle ?: "Instant Services") }
    var description by remember { mutableStateOf(initial?.description.orEmpty()) }
    var emoji by remember { mutableStateOf(initial?.emoji ?: "📸") }
    var photoUrl by remember { mutableStateOf(initial?.photoUrl.orEmpty()) }
    var sortOrder by remember { mutableStateOf((initial?.sortOrder ?: 10L).toString()) }
    var active by remember { mutableStateOf(initial?.active ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New Category" else "Edit Category", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ImgBbPhotoPickerField(
                    label = "Category 1:1 Image (ImgBB)",
                    photoUrl = photoUrl,
                    fallbackEmoji = emoji.ifBlank { "📸" },
                    onPhotoChange = { photoUrl = it }
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name (e.g. Instagram)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Short Tagline") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it.take(6) },
                        label = { Text("Fallback Emoji") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sortOrder,
                        onValueChange = { sortOrder = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Sort Order") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Visible in User Panel")
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, subtitle, description, emoji, photoUrl, sortOrder.toLongOrNull() ?: 10L, active)
                },
                enabled = !busy && name.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary)
            ) { Text("Save", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ServiceFileEditorDialog(
    initial: AdminServiceFile?,
    categories: List<AdminCatalogCategory>,
    defaultCategoryId: String?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, Long, Boolean) -> Unit
) {
    val startCat = categories.firstOrNull { it.id == (initial?.categoryId ?: defaultCategoryId) } ?: categories.first()
    var selectedCatId by remember { mutableStateOf(startCat.id) }
    var selectedCatName by remember { mutableStateOf(startCat.name) }
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var subtitle by remember { mutableStateOf(initial?.subtitle.orEmpty()) }
    var emoji by remember { mutableStateOf(initial?.emoji ?: "🚀") }
    var photoUrl by remember { mutableStateOf(initial?.photoUrl.orEmpty()) }
    var sortOrder by remember { mutableStateOf((initial?.sortOrder ?: 10L).toString()) }
    var active by remember { mutableStateOf(initial?.active ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New Service File" else "Edit Service File", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select Category", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories, key = { it.id }) { cat ->
                        FilterChip(
                            selected = selectedCatId == cat.id,
                            onClick = {
                                selectedCatId = cat.id
                                selectedCatName = cat.name
                            },
                            label = { Text(cat.name) }
                        )
                    }
                }
                ImgBbPhotoPickerField(
                    label = "File Icon / Photo (ImgBB)",
                    photoUrl = photoUrl,
                    fallbackEmoji = emoji.ifBlank { "🚀" },
                    onPhotoChange = { photoUrl = it }
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("File Name (e.g. Instagram Reach, Followers)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Subtitle (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it.take(6) },
                        label = { Text("Emoji") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sortOrder,
                        onValueChange = { sortOrder = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Order") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Visible to Users")
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(selectedCatId, selectedCatName, name, subtitle, emoji, photoUrl, sortOrder.toLongOrNull() ?: 10L, active)
                },
                enabled = !busy && name.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary)
            ) { Text("Save", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ServicePlanEditorDialog(
    initial: AdminServicePlan?,
    files: List<AdminServiceFile>,
    defaultFileId: String?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String, Long, Long, Long, String, List<String>, Long, Boolean) -> Unit
) {
    val startFile = files.firstOrNull { it.id == (initial?.fileId ?: defaultFileId) } ?: files.first()
    var selectedFile by remember { mutableStateOf(startFile) }
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var description by remember { mutableStateOf(initial?.description.orEmpty()) }
    var photoUrl by remember { mutableStateOf(initial?.photoUrl.orEmpty()) }
    var priceInrText by remember {
        mutableStateOf(
            if (initial != null) String.format(java.util.Locale.US, "%.2f", initial.pricePer1kPaise / 100.0)
            else "1.00"
        )
    }
    var minQty by remember { mutableStateOf((initial?.minQty ?: 100L).toString()) }
    var maxQty by remember { mutableStateOf((initial?.maxQty ?: 100_000L).toString()) }
    var inputHint by remember { mutableStateOf(initial?.inputHint ?: "Paste your Instagram post/reel or profile link") }
    var tiersText by remember {
        mutableStateOf(
            (initial?.presetTiers?.takeIf { it.isNotEmpty() }
                ?: listOf("100:0", "500:0", "1000:0", "2000:5", "5000:6", "10000:9"))
                .joinToString(", ")
        )
    }
    var sortOrder by remember { mutableStateOf((initial?.sortOrder ?: 10L).toString()) }
    var active by remember { mutableStateOf(initial?.active ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New Service Plan" else "Edit Service Plan", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select Service File", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(files, key = { it.id }) { f ->
                        FilterChip(
                            selected = selectedFile.id == f.id,
                            onClick = { selectedFile = f },
                            label = { Text("${f.categoryName}: ${f.name}") }
                        )
                    }
                }
                ImgBbPhotoPickerField(
                    label = "Plan Icon (ImgBB, optional)",
                    photoUrl = photoUrl,
                    fallbackEmoji = selectedFile.emoji.ifBlank { "⚡" },
                    onPhotoChange = { photoUrl = it },
                    previewSize = 56.dp
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Plan Name (e.g. IG Reel Views - Superfast ⚡)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceInrText,
                        onValueChange = { priceInrText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Price ₹ / 1k") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sortOrder,
                        onValueChange = { sortOrder = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Order") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.7f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minQty,
                        onValueChange = { minQty = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Min Qty") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxQty,
                        onValueChange = { maxQty = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Max Qty") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = inputHint,
                    onValueChange = { inputHint = it },
                    label = { Text("User Link Input Hint") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tiersText,
                    onValueChange = { tiersText = it },
                    label = { Text("Preset Tiers (qty:discount%)") },
                    supportingText = { Text("Example: 100:0, 500:0, 1000:0, 2000:5, 5000:6, 10000:9") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Visible to Users")
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            val parsedPaise = ((priceInrText.toDoubleOrNull() ?: 0.0) * 100.0).roundToLong()
            Button(
                onClick = {
                    val tiers = tiersText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    onSave(
                        selectedFile.categoryId,
                        selectedFile.categoryName,
                        selectedFile.id,
                        selectedFile.name,
                        name,
                        description,
                        photoUrl,
                        parsedPaise.coerceAtLeast(1L),
                        minQty.toLongOrNull() ?: 50L,
                        maxQty.toLongOrNull() ?: 100_000L,
                        inputHint,
                        tiers,
                        sortOrder.toLongOrNull() ?: 10L,
                        active
                    )
                },
                enabled = !busy && name.trim().length >= 2 && parsedPaise >= 1L,
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary)
            ) { Text("Publish", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
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
    var coins by remember { mutableStateOf((initial?.coins ?: 500L).toString()) }
    var bonus by remember { mutableStateOf((initial?.bonusCoins ?: 50L).toString()) }
    var points by remember { mutableStateOf((initial?.points ?: 2L).toString()) }
    var priceInr by remember { mutableStateOf((initial?.priceInr ?: 249L).toString()) }
    var badge by remember { mutableStateOf(initial?.badge.orEmpty()) }
    var sortOrder by remember { mutableStateOf((initial?.sortOrder ?: 1L).toString()) }
    var active by remember { mutableStateOf(initial?.active ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New Coin Package" else "Edit Coin Package", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Package Name") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = coins,
                        onValueChange = { coins = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Coins") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = bonus,
                        onValueChange = { bonus = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Bonus") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceInr,
                        onValueChange = { priceInr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Price (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = points,
                        onValueChange = { points = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Points") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = badge,
                        onValueChange = { badge = it },
                        label = { Text("Badge") },
                        singleLine = true,
                        modifier = Modifier.weight(1.4f)
                    )
                    OutlinedTextField(
                        value = sortOrder,
                        onValueChange = { sortOrder = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Order") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.8f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active")
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
                        points.toLongOrNull() ?: 1L,
                        priceInr.toLongOrNull() ?: 99L,
                        badge,
                        sortOrder.toLongOrNull() ?: 1L,
                        active
                    )
                },
                enabled = !busy && name.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary)
            ) { Text("Save", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
