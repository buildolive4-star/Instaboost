package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CatalogService
import com.example.data.DefaultCatalog
import com.example.data.FavoritePack
import com.example.data.OrderRecord
import com.example.data.ServiceCategoryUi
import com.example.data.UserProfile
import com.example.ui.components.CategoryTileCard
import com.example.ui.components.DynamicPhotoImage
import com.example.ui.components.GoldFlameCoin
import com.example.ui.components.HeaderIconButton
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RaspberryLight
import com.example.ui.theme.RaspberryPrimary
import com.example.ui.theme.SoftCardBg
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSecondary

@Composable
fun ServicesScreen(
    profile: UserProfile,
    categories: List<ServiceCategoryUi>,
    services: List<CatalogService>,
    onCategorySelected: (String) -> Unit,
    onOpenAiStudio: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWallet: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAllCategories by remember { mutableStateOf(false) }

    val filteredCategories = remember(categories, searchQuery, showAllCategories) {
        val base = if (searchQuery.isBlank()) {
            categories
        } else {
            categories.filter {
                it.name.contains(searchQuery.trim(), ignoreCase = true) ||
                    it.description.contains(searchQuery.trim(), ignoreCase = true)
            }
        }
        if (showAllCategories || searchQuery.isNotBlank()) base else base.take(6)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Branded Fame Maker Services Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_fame_maker_logo),
                        contentDescription = "Fame Maker",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Fame Maker",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Services & Packs",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        onClick = onOpenWallet,
                        shape = RoundedCornerShape(20.dp),
                        color = SoftCardBg,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.testTag("services_coin_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            GoldFlameCoin(size = 20.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = profile.coins.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    HeaderIconButton(
                        icon = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        onClick = onOpenSettings,
                        testTag = "services_settings_btn"
                    )
                }
            }
        }

        // Clean search bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search categories (Food, Christmas, Fashion...)", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RaspberryPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = SoftCardBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("services_search_input")
            )
        }

        // Main Categories Card (Exact faithful layout of uploaded Services screenshot!)
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE7F7FC),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Tag,
                                    contentDescription = null,
                                    tint = Color(0xFF4CC3E0),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Categories",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Hashtags, captions & creator packs",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    if (filteredCategories.isEmpty()) {
                        Text(
                            text = "No matching categories found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    } else {
                        val rows = filteredCategories.chunked(3)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            rows.forEach { rowCats ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowCats.forEach { cat ->
                                        val packCount = services.count { it.categoryId == cat.id }
                                        CategoryTileCard(
                                            category = cat,
                                            packCount = packCount,
                                            onClick = { onCategorySelected(cat.id) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    repeat(3 - rowCats.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    if (searchQuery.isBlank() && categories.size > 6) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { showAllCategories = !showAllCategories },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("view_all_categories_btn")
                        ) {
                            Text(
                                text = if (showAllCategories) "Show Less" else "View All Categories (${categories.size})",
                                color = RaspberryPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // AI Creator Studio Card
        item {
            Surface(
                onClick = onOpenAiStudio,
                shape = RoundedCornerShape(22.dp),
                color = RaspberryLight,
                border = BorderStroke(1.dp, RaspberryPrimary.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("services_ai_studio_card")
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = RaspberryPrimary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Bio & Caption Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RaspberryPrimary
                        )
                        Text(
                            text = "Generate custom hashtags, captions & bios instantly",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5A2135)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDetailsScreen(
    categoryId: String,
    categories: List<ServiceCategoryUi>,
    services: List<CatalogService>,
    orders: List<OrderRecord>,
    onBack: () -> Unit,
    onServiceSelected: (CatalogService) -> Unit
) {
    val category = categories.firstOrNull { it.id == categoryId }
        ?: categories.firstOrNull()
        ?: DefaultCatalog.categories.first()
    var sortOption by remember { mutableStateOf("Popular") }
    val sortOptions = listOf("Popular", "Lowest Price", "Highest Price")

    val categoryServices = remember(services, categoryId, sortOption) {
        val list = services.filter { it.categoryId == categoryId }
        when (sortOption) {
            "Lowest Price" -> list.sortedBy { it.coinPrice }
            "Highest Price" -> list.sortedByDescending { it.coinPrice }
            else -> list.sortedByDescending { it.trending }
        }
    }

    val unlockedServiceIds = remember(orders) { orders.map { it.serviceId }.toSet() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Button Header (Matches uploaded Category Details screenshot!)
        item {
            HeaderIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                onClick = onBack,
                testTag = "category_back_btn"
            )
        }

        // Pastel Hero Banner with Decorative Dashed White Arc (Matches Christmas screenshot!)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(category.bannerTint)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 16f), 0f)
                    val topArc = Path().apply {
                        moveTo(size.width * 0.42f, 0f)
                        quadraticTo(size.width * 0.65f, size.height * 0.34f, size.width * 0.85f, size.height * 0.16f)
                    }
                    drawPath(
                        path = topArc,
                        color = Color.White.copy(alpha = 0.72f),
                        style = Stroke(width = 6f, pathEffect = dashEffect)
                    )
                    val bottomArc = Path().apply {
                        moveTo(size.width * 0.25f, size.height)
                        quadraticTo(size.width * 0.46f, size.height * 0.82f, size.width * 0.64f, size.height)
                    }
                    drawPath(
                        path = bottomArc,
                        color = Color.White.copy(alpha = 0.72f),
                        style = Stroke(width = 6f, pathEffect = dashEffect)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryThumbnail(category = category, size = 86.dp)
                    Spacer(modifier = Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF171717)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = category.subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }

        // Sort Chips
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                sortOptions.forEach { option ->
                    FilterChip(
                        selected = sortOption == option,
                        onClick = { sortOption = option },
                        label = { Text(option, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RaspberryPrimary,
                            selectedLabelColor = PureWhite
                        )
                    )
                }
            }
        }

        // Pack Cards (Matches `30 Christmas   🪙 3` screenshot!)
        if (categoryServices.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = SoftCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No packs in this category yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(categoryServices, key = { it.id }) { service ->
                val isUnlocked = service.id in unlockedServiceIds
                Surface(
                    onClick = { onServiceSelected(service) },
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("service_pack_${service.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (service.photoUrl.isNotBlank()) {
                                DynamicPhotoImage(
                                    photoUrl = service.photoUrl,
                                    contentDescription = service.name,
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = service.itemCount.toString(),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isUnlocked) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFE3F7EA)
                                        ) {
                                            Text(
                                                text = "Unlocked",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SuccessGreen,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = service.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (service.providerName.isNotBlank()) {
                                    Text(
                                        text = "Provider: ${service.providerName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GoldFlameCoin(size = 24.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = service.coinPrice.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateOrderScreen(
    service: CatalogService,
    categories: List<ServiceCategoryUi>,
    profile: UserProfile,
    existingOrder: OrderRecord?,
    favorites: List<FavoritePack>,
    busy: Boolean,
    onBack: () -> Unit,
    onUnlock: (List<String>, () -> Unit, (OrderRecord) -> Unit) -> Unit,
    onRegenerateAi: (String?, (List<String>) -> Unit) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onGoToWallet: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val category = categories.firstOrNull { it.id == service.categoryId }
        ?: DefaultCatalog.categories.firstOrNull { it.id == service.categoryId }
        ?: DefaultCatalog.categories.first()

    var currentLines by remember(service.id, existingOrder?.id) {
        mutableStateOf(existingOrder?.contentLines?.takeIf { it.isNotEmpty() } ?: service.contentLines)
    }
    var unlockedLocally by remember(service.id, existingOrder?.id) {
        mutableStateOf(existingOrder != null)
    }
    var showInsufficientDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val formattedText = remember(currentLines, service.packType) {
        if (service.packType == "HASHTAGS") currentLines.joinToString(", ")
        else currentLines.joinToString("\n\n")
    }
    val isFavorited = favorites.any { it.serviceId == service.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header: Back + Centered "Create Order" + Favorite Heart
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                onClick = onBack,
                testTag = "create_order_back_btn"
            )
            Text(
                text = "Create Order",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            HeaderIconButton(
                icon = if (isFavorited) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Save to Favorites",
                onClick = { onToggleFavorite(formattedText) },
                testTag = "create_order_fav_btn"
            )
        }

        // Service Header Card (Matches `30 Christmas` card in screenshot!)
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (service.photoUrl.isNotBlank()) {
                    DynamicPhotoImage(
                        photoUrl = service.photoUrl,
                        contentDescription = service.name,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                } else {
                    CategoryThumbnail(category = category, size = 76.dp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val providerTag = if (service.providerName.isNotBlank()) " • by ${service.providerName}" else ""
                    Text(
                        text = "${currentLines.size} items • ${service.categoryName}$providerTag",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    if (unlockedLocally) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Unlocked in your library",
                                style = MaterialTheme.typography.labelSmall,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Content Preview Container (Matches grey rounded hashtag box in screenshot!)
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = SoftCardBg,
            border = BorderStroke(1.dp, SubtleBorder),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 210.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = formattedText,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Quick actions row inside card
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = {
                            if (unlockedLocally) {
                                clipboard.setText(AnnotatedString(formattedText))
                                onShowMessage("Copied all ${currentLines.size} items!")
                            } else {
                                onShowMessage("Unlock this pack first to copy all items.")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Copy All", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Surface(
                        onClick = {
                            if (unlockedLocally) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, formattedText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share ${service.name}"))
                            } else {
                                onShowMessage("Unlock this pack first to share.")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Share", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Surface(
                        onClick = {
                            if (!busy) {
                                onRegenerateAi(existingOrder?.id) { freshLines ->
                                    currentLines = freshLines
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = RaspberryLight,
                        modifier = Modifier.testTag("regenerate_ai_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = null,
                                tint = RaspberryPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                "AI Refresh",
                                style = MaterialTheme.typography.labelMedium,
                                color = RaspberryPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Bottom Price & Action Card (Matches screenshot Price + big Raspberry Pink button!)
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Price",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Your balance: ${profile.coins} coins",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GoldFlameCoin(size = 24.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (unlockedLocally) "Paid" else service.coinPrice.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (unlockedLocally) {
                            clipboard.setText(AnnotatedString(formattedText))
                            onShowMessage("Copied to clipboard!")
                        } else {
                            onUnlock(
                                currentLines,
                                { showInsufficientDialog = true },
                                {
                                    unlockedLocally = true
                                    clipboard.setText(AnnotatedString(formattedText))
                                    showSuccessDialog = true
                                }
                            )
                        }
                    },
                    enabled = !busy,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RaspberryPrimary,
                        contentColor = PureWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("order_primary_cta")
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = PureWhite,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (unlockedLocally) Icons.Filled.ContentCopy else Icons.Filled.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (unlockedLocally) "Copy" else "Unlock & Copy (${service.coinPrice} Coins)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showInsufficientDialog) {
        AlertDialog(
            onDismissRequest = { showInsufficientDialog = false },
            title = { Text("Insufficient Coins", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "You need ${service.coinPrice} coins to unlock ${service.name}, " +
                        "and your current balance is ${profile.coins} coins. Top up in Wallet or claim Daily Rewards!"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInsufficientDialog = false
                        onGoToWallet()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary)
                ) {
                    Text("Buy Coins", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInsufficientDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(44.dp)
                )
            },
            title = { Text("Pack Unlocked!", fontWeight = FontWeight.Bold) },
            text = {
                Text("${service.name} has been saved to My Orders and copied to your clipboard. +1 Reward Point earned!")
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary)
                ) {
                    Text("Awesome!", color = PureWhite)
                }
            }
        )
    }
}

@Composable
private fun CategoryThumbnail(category: ServiceCategoryUi, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        DynamicPhotoImage(
            photoUrl = category.photoUrl,
            contentDescription = category.name,
            fallbackRes = category.imageRes,
            modifier = Modifier.fillMaxSize(),
            fallbackContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(listOf(category.gradientStart, category.gradientEnd))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = category.emoji, fontSize = 30.sp)
                }
            }
        )
    }
}
