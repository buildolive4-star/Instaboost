package com.example.admin.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.admin.data.AdminCatalogCategory
import com.example.admin.data.AdminCatalogService
import com.example.admin.data.AdminCoinPackage
import com.example.admin.theme.AdminBlue
import com.example.admin.theme.AdminBorder
import com.example.admin.theme.AdminDanger
import com.example.admin.theme.AdminGreen
import com.example.admin.theme.AdminGreenBg
import com.example.admin.theme.AdminPrimary
import com.example.admin.theme.AdminPrimaryLight
import com.example.admin.theme.AdminTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Locale

@Composable
fun AdminCatalogTab(
    categories: List<AdminCatalogCategory>,
    services: List<AdminCatalogService>,
    packages: List<AdminCoinPackage>,
    busy: Boolean,
    onSaveCategory: (String?, String, String, String, String, String, Long, Boolean, () -> Unit) -> Unit,
    onDeleteCategory: (AdminCatalogCategory) -> Unit,
    onSeedDefaultCategories: () -> Unit,
    onSeedDefaultServices: () -> Unit,
    onSaveService: (String?, String, String, String, String, Long, String, String, Boolean, Boolean, List<String>, () -> Unit) -> Unit,
    onDeleteService: (AdminCatalogService) -> Unit,
    onSavePackage: (String?, String, Long, Long, Long, Long, String, Long, Boolean, () -> Unit) -> Unit,
    onDeletePackage: (AdminCoinPackage) -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) } // 0 = Categories, 1 = Services, 2 = Coin Packs
    var editingCategory by remember { mutableStateOf<AdminCatalogCategory?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = subTab == 0,
                    onClick = { subTab = 0 },
                    label = {
                        Text(
                            "Categories (${categories.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("catalog_subtab_categories")
                )
                FilterChip(
                    selected = subTab == 1,
                    onClick = { subTab = 1 },
                    label = {
                        Text(
                            "Services (${services.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("catalog_subtab_services")
                )
                FilterChip(
                    selected = subTab == 2,
                    onClick = { subTab = 2 },
                    label = {
                        Text(
                            "Coins (${packages.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("catalog_subtab_coins")
                )
            }
        }

        when (subTab) {
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
                            Text("Add New Category", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        if (categories.size < 12) {
                            OutlinedButton(
                                onClick = onSeedDefaultCategories,
                                enabled = !busy,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("admin_seed_categories_btn")
                            ) {
                                Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = AdminPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Load Default Categories to Edit", color = AdminPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                if (categories.isEmpty()) {
                    item {
                        EmptyCatalogNotice(
                            text = "No custom categories yet. Tap 'Load Default Categories to Edit' to customize built-in categories, or add a brand-new category."
                        )
                    }
                } else {
                    items(categories, key = { it.id }) { cat ->
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
                                    photoUrl = cat.photoUrl,
                                    fallbackEmoji = cat.emoji.ifBlank { "✨" },
                                    size = 52.dp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (cat.active) AdminGreenBg else Color(0xFFF3F4F6)
                                        ) {
                                            Text(
                                                text = if (cat.active) "LIVE" else "HIDDEN",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (cat.active) AdminGreen else AdminTextSecondary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${cat.subtitle.ifBlank { "Trending packs" }} • Order #${cat.sortOrder}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AdminTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row {
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
                }
            }

            1 -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            Text("Add New Service", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        if (services.size < 4) {
                            OutlinedButton(
                                onClick = onSeedDefaultServices,
                                enabled = !busy,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("admin_seed_services_btn")
                            ) {
                                Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = AdminPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Load Starter Services to Edit", color = AdminPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                if (services.isEmpty()) {
                    item {
                        EmptyCatalogNotice(
                            text = "No services in database yet. Tap 'Add New Service' or 'Load Starter Services to Edit' to manage Service Name, Category, Price, Provider, and Photo live."
                        )
                    }
                } else {
                    items(services, key = { it.id }) { svc ->
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
                                    photoUrl = svc.photoUrl,
                                    fallbackEmoji = when (svc.packType) {
                                        "CAPTIONS" -> "✍️"
                                        "BIOS" -> "🪪"
                                        "IDEAS" -> "💡"
                                        else -> "#️⃣"
                                    },
                                    size = 54.dp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = svc.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${svc.categoryName} • by ${svc.providerName.ifBlank { "Fame Maker" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AdminPrimary,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${svc.coinPrice} coins • ${svc.packType} • ${if (svc.published) "Live" else "Hidden"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AdminTextSecondary
                                    )
                                }
                                Row {
                                    IconButton(onClick = {
                                        editingService = svc
                                        showServiceDialog = true
                                    }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit service", tint = AdminBlue)
                                    }
                                    IconButton(onClick = { onDeleteService(svc) }) {
                                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete service", tint = AdminDanger)
                                    }
                                }
                            }
                        }
                    }
                }
            }

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
                onSaveCategory(
                    editingCategory?.id, name, subtitle, desc, emoji, photo, order, active
                ) { showCategoryDialog = false }
            }
        )
    }

    if (showServiceDialog) {
        ServiceEditorDialog(
            initial = editingService,
            availableCategories = categories,
            busy = busy,
            onDismiss = { showServiceDialog = false },
            onSave = { catId, catName, name, type, price, provider, photo, trending, published, lines ->
                onSaveService(
                    editingService?.id, catId, catName, name, type, price, provider, photo, trending, published, lines
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
private fun AdminPhotoPreview(
    photoUrl: String,
    fallbackEmoji: String,
    size: Dp
) {
    val cleanUrl = photoUrl.trim()
    val decodedBitmap = remember(cleanUrl) {
        if (cleanUrl.startsWith("data:image", ignoreCase = true) && cleanUrl.contains(",")) {
            runCatching {
                val base64Part = cleanUrl.substringAfter(",")
                val bytes = Base64.decode(base64Part, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }.getOrNull()
        } else {
            null
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(AdminPrimaryLight),
        contentAlignment = Alignment.Center
    ) {
        when {
            decodedBitmap != null -> {
                Image(
                    bitmap = decodedBitmap,
                    contentDescription = "Selected photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            cleanUrl.startsWith("http://", ignoreCase = true) ||
                cleanUrl.startsWith("https://", ignoreCase = true) -> {
                AsyncImage(
                    model = cleanUrl,
                    contentDescription = "Remote photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            fallbackEmoji.isNotBlank() -> {
                Text(text = fallbackEmoji, style = MaterialTheme.typography.titleLarge)
            }
            else -> {
                Icon(Icons.Filled.Image, contentDescription = null, tint = AdminPrimary)
            }
        }
    }
}

@Composable
private fun PhotoPickerField(
    label: String,
    photoUrl: String,
    fallbackEmoji: String,
    onPhotoChange: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var encoding by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                encoding = true
                val dataUri = encodePickedPhotoToDataUri(context, uri)
                if (!dataUri.isNullOrBlank()) {
                    onPhotoChange(dataUri)
                }
                encoding = false
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AdminPhotoPreview(
                photoUrl = photoUrl,
                fallbackEmoji = fallbackEmoji,
                size = 60.dp
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            picker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !encoding,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddPhotoAlternate,
                            contentDescription = null,
                            tint = AdminPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (encoding) "Processing..." else "Pick Gallery Photo",
                            style = MaterialTheme.typography.labelMedium,
                            color = AdminPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (photoUrl.isNotBlank()) {
                        IconButton(
                            onClick = { onPhotoChange("") },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove photo", tint = AdminDanger)
                        }
                    }
                }
                Text(
                    text = if (photoUrl.startsWith("data:image")) "Gallery photo ready (synced live)" else "Or paste an https:// image link below",
                    style = MaterialTheme.typography.labelSmall,
                    color = AdminTextSecondary
                )
            }
        }
        OutlinedTextField(
            value = if (photoUrl.startsWith("data:image")) "" else photoUrl,
            onValueChange = { onPhotoChange(it) },
            label = { Text("Image URL (https://...)") },
            placeholder = { Text(if (photoUrl.startsWith("data:image")) "Gallery photo selected" else "Optional image link") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private suspend fun encodePickedPhotoToDataUri(context: Context, uri: Uri): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

            var sampleSize = 1
            val maxDim = maxOf(bounds.outWidth, bounds.outHeight)
            while (maxDim / sampleSize > 760) {
                sampleSize *= 2
            }

            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val raw = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOpts)
            } ?: return@runCatching null

            val targetMax = 380f
            val scale = minOf(1f, targetMax / maxOf(raw.width, raw.height).toFloat())
            val scaled = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    raw,
                    (raw.width * scale).toInt().coerceAtLeast(1),
                    (raw.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                raw
            }

            var quality = 74
            var bytes: ByteArray
            do {
                val out = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
                bytes = out.toByteArray()
                quality -= 14
            } while (bytes.size > 160_000 && quality >= 30)

            if (scaled !== raw) scaled.recycle()
            raw.recycle()

            val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:image/jpeg;base64,$encoded"
        }.getOrNull()
    }

@Composable
private fun CategoryEditorDialog(
    initial: AdminCatalogCategory?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, Long, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var subtitle by remember { mutableStateOf(initial?.subtitle ?: "Trending packs") }
    var description by remember { mutableStateOf(initial?.description.orEmpty()) }
    var emoji by remember { mutableStateOf(initial?.emoji ?: "✨") }
    var photoUrl by remember { mutableStateOf(initial?.photoUrl.orEmpty()) }
    var sortOrder by remember { mutableStateOf((initial?.sortOrder ?: 10L).toString()) }
    var active by remember { mutableStateOf(initial?.active ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initial == null) "New Category" else "Edit Category",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PhotoPickerField(
                    label = "Category Photo",
                    photoUrl = photoUrl,
                    fallbackEmoji = emoji.ifBlank { "✨" },
                    onPhotoChange = { photoUrl = it }
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Subtitle (e.g. Trending hashtags)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 2,
                    maxLines = 3,
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
                    Text("Visible in User Panel")
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        subtitle,
                        description,
                        emoji,
                        photoUrl,
                        sortOrder.toLongOrNull() ?: 10L,
                        active
                    )
                },
                enabled = !busy && name.trim().length >= 2,
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
private fun ServiceEditorDialog(
    initial: AdminCatalogService?,
    availableCategories: List<AdminCatalogCategory>,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Long, String, String, Boolean, Boolean, List<String>) -> Unit
) {
    val fallbackCategories = listOf(
        "food" to "Food",
        "christmas" to "Christmas",
        "fashion" to "Fashion",
        "photography" to "Photography",
        "funny" to "Funny",
        "travel" to "Travel",
        "fitness" to "Fitness",
        "beauty" to "Beauty",
        "gaming" to "Gaming",
        "technology" to "Technology",
        "motivation" to "Motivation",
        "lifestyle" to "Lifestyle"
    )
    val categoryChoices = remember(availableCategories) {
        val merged = linkedMapOf<String, String>()
        availableCategories.forEach { merged[it.id] = it.name }
        fallbackCategories.forEach { (id, label) -> merged.putIfAbsent(id, label) }
        merged.entries.map { it.key to it.value }
    }

    var selectedCatId by remember {
        mutableStateOf(initial?.categoryId?.ifBlank { null } ?: categoryChoices.first().first)
    }
    var categoryName by remember {
        mutableStateOf(
            initial?.categoryName?.ifBlank { null }
                ?: categoryChoices.firstOrNull { it.first == selectedCatId }?.second
                ?: "Food"
        )
    }
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var packType by remember { mutableStateOf(initial?.packType ?: "HASHTAGS") }
    var coinPrice by remember { mutableStateOf((initial?.coinPrice ?: 5L).toString()) }
    var providerName by remember { mutableStateOf(initial?.providerName?.ifBlank { "Fame Maker" } ?: "Fame Maker") }
    var photoUrl by remember { mutableStateOf(initial?.photoUrl.orEmpty()) }
    var trending by remember { mutableStateOf(initial?.trending ?: true) }
    var published by remember { mutableStateOf(initial?.published ?: true) }
    var contentText by remember { mutableStateOf(initial?.contentLines?.joinToString("\n").orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New Service" else "Edit Service", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PhotoPickerField(
                    label = "Service Photo",
                    photoUrl = photoUrl,
                    fallbackEmoji = "👑",
                    onPhotoChange = { photoUrl = it }
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categoryChoices, key = { it.first }) { (catId, label) ->
                        FilterChip(
                            selected = selectedCatId == catId,
                            onClick = {
                                selectedCatId = catId
                                categoryName = label
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = {
                        categoryName = it
                        val matched = categoryChoices.firstOrNull { c -> c.second.equals(it.trim(), ignoreCase = true) }
                        selectedCatId = matched?.first ?: it.trim().lowercase(Locale.US)
                            .replace(Regex("[^a-z0-9]+"), "_")
                            .trim('_')
                            .ifBlank { "general" }
                    },
                    label = { Text("Category Name (Editable)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Service Type",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("HASHTAGS", "CAPTIONS", "BIOS", "IDEAS")) { t ->
                        FilterChip(
                            selected = packType == t,
                            onClick = { packType = t },
                            label = { Text(t, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = coinPrice,
                        onValueChange = { coinPrice = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Price (Coins)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = providerName,
                        onValueChange = { providerName = it },
                        label = { Text("Provider") },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )
                }

                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    label = { Text("Service Content (one item per line)") },
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
                    val lines = contentText.lines().map { it.trim() }.filter { it.isNotBlank() }
                    onSave(
                        selectedCatId,
                        categoryName.trim().ifBlank { "General" },
                        name,
                        packType,
                        coinPrice.toLongOrNull() ?: 5L,
                        providerName.trim().ifBlank { "Fame Maker" },
                        photoUrl,
                        trending,
                        published,
                        lines
                    )
                },
                enabled = !busy && name.trim().length >= 2 && contentText.isNotBlank(),
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
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
