package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppConfig
import com.example.data.ImgBbUploader
import com.example.data.ServiceCategoryUi
import com.example.data.ServiceFileItem
import com.example.data.ServicePlanItem
import com.example.data.UserProfile
import com.example.data.calculateOrderPricePaise
import com.example.data.formatInrFromPaise
import com.example.data.formatRatePer1k
import com.example.data.formatShortQty
import com.example.data.parsePresetTiers
import com.example.ui.components.DynamicPhotoImage
import com.example.ui.components.HeaderIconButton
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RaspberryPrimary
import com.example.ui.theme.SoftCardBg
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private val GoldenPillBg = Color(0xFFFACC15)
private val GoldenPillText = Color(0xFF111827)
private val BrandBlue = Color(0xFF1D63ED)
private val DiscountRed = Color(0xFFEF4444)

@Composable
fun InrWalletPill(
    balancePaise: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = GoldenPillBg,
        shadowElevation = 3.dp,
        modifier = modifier.testTag("inr_wallet_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AccountBalanceWallet,
                contentDescription = "INR Wallet Balance",
                tint = GoldenPillText,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = formatInrFromPaise(balancePaise),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = GoldenPillText
            )
        }
    }
}

@Composable
fun ServiceFlowTopBar(
    title: String,
    balancePaise: Long,
    onBack: () -> Unit,
    onOpenAddFunds: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            HeaderIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                onClick = onBack,
                testTag = "service_flow_back_btn"
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        InrWalletPill(balancePaise = balancePaise, onClick = onOpenAddFunds)
    }
}

@Composable
fun CategoryFilesScreen(
    categoryId: String,
    profile: UserProfile,
    categories: List<ServiceCategoryUi>,
    serviceFiles: List<ServiceFileItem>,
    servicePlans: List<ServicePlanItem>,
    onBack: () -> Unit,
    onFileSelected: (String) -> Unit,
    onOpenAddFunds: () -> Unit
) {
    val category = categories.firstOrNull { it.id == categoryId }
    val matchingFiles = remember(serviceFiles, categoryId) {
        serviceFiles.filter { it.categoryId == categoryId }.sortedBy { it.sortOrder }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ServiceFlowTopBar(
                title = category?.name ?: "Services",
                balancePaise = profile.walletBalancePaise,
                onBack = onBack,
                onOpenAddFunds = onOpenAddFunds
            )
        }

        // Category Banner Card
        if (category != null) {
            item {
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
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp)),
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
                                                Brush.linearGradient(
                                                    listOf(category.gradientStart, category.gradientEnd)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = category.emoji, fontSize = 28.sp)
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = category.subtitle.ifBlank { "Choose a service below" },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        if (matchingFiles.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SoftCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No service files added yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Admin can add custom files (like Followers, Reel Views, Reach) to this category anytime.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(matchingFiles, key = { it.id }) { file ->
                val planCount = servicePlans.count { it.fileId == file.id }
                Surface(
                    onClick = { onFileSelected(file.id) },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("service_file_${file.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SoftCardBg),
                            contentAlignment = Alignment.Center
                        ) {
                            DynamicPhotoImage(
                                photoUrl = file.photoUrl,
                                contentDescription = file.name,
                                modifier = Modifier.fillMaxSize(),
                                fallbackContent = {
                                    Text(text = file.emoji.ifBlank { "⚡" }, fontSize = 24.sp)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (file.subtitle.isNotBlank()) {
                                Text(
                                    text = file.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (planCount > 0) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "$planCount ${if (planCount == 1) "plan" else "plans"} available",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open ${file.name}",
                            tint = BrandBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilePlansScreen(
    fileId: String,
    profile: UserProfile,
    serviceFiles: List<ServiceFileItem>,
    servicePlans: List<ServicePlanItem>,
    onBack: () -> Unit,
    onPlanSelected: (String) -> Unit,
    onOpenAddFunds: () -> Unit
) {
    val file = serviceFiles.firstOrNull { it.id == fileId }
    val plans = remember(servicePlans, fileId) {
        servicePlans.filter { it.fileId == fileId }.sortedBy { it.sortOrder }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ServiceFlowTopBar(
                title = file?.name ?: "Service Plans",
                balancePaise = profile.walletBalancePaise,
                onBack = onBack,
                onOpenAddFunds = onOpenAddFunds
            )
        }

        if (plans.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SoftCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No plans added in this service file yet. Check back soon!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(plans, key = { it.id }) { plan ->
                val iconUrl = plan.photoUrl.ifBlank { file?.photoUrl.orEmpty() }
                Surface(
                    onClick = { onPlanSelected(plan.id) },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("service_plan_${plan.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFEDE5)),
                            contentAlignment = Alignment.Center
                        ) {
                            DynamicPhotoImage(
                                photoUrl = iconUrl,
                                contentDescription = plan.name,
                                modifier = Modifier.fillMaxSize(),
                                fallbackContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFFF97316)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            tint = PureWhite,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = plan.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (plan.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = plan.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Min ${formatShortQty(plan.minQty)} • Max ${formatShortQty(plan.maxQty)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatRatePer1k(plan.pricePer1kPaise),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Select ${plan.name}",
                            tint = BrandBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectQuantityOrderScreen(
    plan: ServicePlanItem,
    profile: UserProfile,
    appConfig: AppConfig,
    busy: Boolean,
    onBack: () -> Unit,
    onOpenAddFunds: () -> Unit,
    onOrderPlaced: () -> Unit,
    onShowMessage: (String) -> Unit,
    onPlaceOrder: (
        plan: ServicePlanItem,
        targetLink: String,
        quantity: Long,
        totalPricePaise: Long,
        paymentMethod: String,
        utrNumber: String,
        paymentProofUrl: String,
        onInsufficient: () -> Unit,
        onSuccess: (String) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val tiers = remember(plan.id, plan.presetTiers, plan.minQty, plan.maxQty) {
        parsePresetTiers(plan.presetTiers, plan.minQty, plan.maxQty)
    }

    var selectedTierIndex by remember(plan.id) { mutableStateOf<Int?>(0) }
    var customQtyText by remember(plan.id) { mutableStateOf("") }
    var targetLink by remember(plan.id) { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("WALLET") } // WALLET or UPI_DIRECT
    var utrNumber by remember { mutableStateOf("") }
    var proofUrl by remember { mutableStateOf("") }
    var uploadingProof by remember { mutableStateOf(false) }
    var showLowBalanceDialog by remember { mutableStateOf(false) }
    var placedOrderId by remember { mutableStateOf<String?>(null) }

    val proofPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                uploadingProof = true
                ImgBbUploader.uploadImageUri(context, uri)
                    .onSuccess { url ->
                        proofUrl = url
                        onShowMessage("Payment screenshot uploaded!")
                    }
                    .onFailure { err ->
                        onShowMessage(err.localizedMessage ?: "ImgBB upload failed")
                    }
                uploadingProof = false
            }
        }
    }

    val activeQuantity: Long = if (selectedTierIndex != null && selectedTierIndex!! < tiers.size) {
        tiers[selectedTierIndex!!].quantity
    } else {
        customQtyText.filter { it.isDigit() }.toLongOrNull() ?: 0L
    }
    val activeDiscount: Int = if (selectedTierIndex != null && selectedTierIndex!! < tiers.size) {
        tiers[selectedTierIndex!!].discountPct
    } else 0

    val totalPricePaise = if (activeQuantity > 0L) {
        calculateOrderPricePaise(plan.pricePer1kPaise, activeQuantity, activeDiscount)
    } else 0L

    val isQtyValid = activeQuantity in plan.minQty..plan.maxQty
    val hasEnoughWallet = profile.walletBalancePaise >= totalPricePaise

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ServiceFlowTopBar(
            title = "Select Quantity",
            balancePaise = profile.walletBalancePaise,
            onBack = onBack,
            onOpenAddFunds = onOpenAddFunds
        )

        // Selected Plan Header Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = BrandBlue.copy(alpha = 0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = BrandBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${plan.fileName} • ${formatRatePer1k(plan.pricePer1kPaise)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Preset Quantity Cards (2-column grid with red -X% discount pills)
        val tierRows = tiers.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            tierRows.forEachIndexed { rowIdx, rowTiers ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowTiers.forEachIndexed { colIdx, tier ->
                        val index = rowIdx * 2 + colIdx
                        val selected = selectedTierIndex == index
                        val tierPrice = calculateOrderPricePaise(
                            plan.pricePer1kPaise,
                            tier.quantity,
                            tier.discountPct
                        )
                        Surface(
                            onClick = {
                                selectedTierIndex = index
                                customQtyText = ""
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) BrandBlue.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) BrandBlue else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("qty_tier_${tier.quantity}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formatShortQty(tier.quantity),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (selected) BrandBlue else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (tier.discountPct > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = DiscountRed
                                        ) {
                                            Text(
                                                text = "-${tier.discountPct}%",
                                                color = PureWhite,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = formatInrFromPaise(tierPrice),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) BrandBlue else TextSecondary
                                )
                            }
                        }
                    }
                    if (rowTiers.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Custom Quantity Input
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SoftCardBg,
            border = BorderStroke(
                width = if (selectedTierIndex == null) 2.dp else 1.dp,
                color = if (selectedTierIndex == null) BrandBlue else MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Custom quantity (${plan.minQty} – ${plan.maxQty})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = customQtyText,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(9)
                        customQtyText = digits
                        if (digits.isNotEmpty()) selectedTierIndex = null
                    },
                    placeholder = { Text("Enter custom quantity e.g. 1500", color = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_qty_input")
                )
            }
        }

        // Target Link / Username Field
        OutlinedTextField(
            value = targetLink,
            onValueChange = { targetLink = it },
            label = { Text(plan.inputHint.ifBlank { "Profile / Post Link or Username" }) },
            placeholder = { Text("https://instagram.com/...", color = TextSecondary) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("order_target_link_input")
        )

        // Payment Method Selector
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val isWallet = paymentMethod == "WALLET"
                Surface(
                    onClick = { paymentMethod = "WALLET" },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isWallet) BrandBlue.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(if (isWallet) 2.dp else 1.dp, if (isWallet) BrandBlue else MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pay_method_wallet")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Wallet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatInrFromPaise(profile.walletBalancePaise),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (hasEnoughWallet) SuccessGreen else DiscountRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val isUpi = paymentMethod == "UPI_DIRECT"
                Surface(
                    onClick = { paymentMethod = "UPI_DIRECT" },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isUpi) BrandBlue.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(if (isUpi) 2.dp else 1.dp, if (isUpi) BrandBlue else MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pay_method_upi")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.QrCode2, contentDescription = null, tint = RaspberryPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Direct UPI", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scan QR & UTR",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            if (paymentMethod == "WALLET" && !hasEnoughWallet && totalPricePaise > 0L) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, DiscountRed.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Low wallet balance. Need ${formatInrFromPaise(totalPricePaise)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DiscountRed,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onOpenAddFunds) {
                            Text("Add Funds", fontWeight = FontWeight.Bold, color = BrandBlue)
                        }
                    }
                }
            }

            if (paymentMethod == "UPI_DIRECT") {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (appConfig.paymentQrUrl.isNotBlank()) {
                            DynamicPhotoImage(
                                photoUrl = appConfig.paymentQrUrl,
                                contentDescription = "UPI Payment QR Code",
                                modifier = Modifier
                                    .size(180.dp)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                        }
                        if (appConfig.upiId.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = appConfig.upiMerchantName.ifBlank { "Fame Maker" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = appConfig.upiId,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(appConfig.upiId))
                                        onShowMessage("UPI ID copied!")
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy")
                                }
                            }
                            if (totalPricePaise > 0L) {
                                val amtStr = String.format(java.util.Locale.US, "%.2f", totalPricePaise / 100.0)
                                OutlinedButton(
                                    onClick = {
                                        val uri = Uri.parse(
                                            "upi://pay?pa=${Uri.encode(appConfig.upiId)}" +
                                                "&pn=${Uri.encode(appConfig.upiMerchantName.ifBlank { "Fame Maker" })}" +
                                                "&am=$amtStr&cu=INR"
                                        )
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                        } catch (_: Exception) {
                                            onShowMessage("No UPI app detected. Scan QR or copy UPI ID instead.")
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open UPI App (${formatInrFromPaise(totalPricePaise)})", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Text(
                                text = "Admin has not configured UPI ID yet. Please check back or pay via Wallet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        OutlinedTextField(
                            value = utrNumber,
                            onValueChange = { utrNumber = it.take(40) },
                            label = { Text("12-Digit UTR / Transaction ID *") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("direct_upi_utr_input")
                        )

                        OutlinedButton(
                            onClick = {
                                proofPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !uploadingProof,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (uploadingProof) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Uploading screenshot...")
                            } else {
                                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (proofUrl.isBlank()) "Upload Payment Screenshot (Optional)" else "Screenshot Uploaded ✓",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Order Total & Primary Place Order Button
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Payable", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Text(
                            text = if (activeQuantity > 0L) "${formatShortQty(activeQuantity)} • ${plan.fileName}" else "Select quantity",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = formatInrFromPaise(totalPricePaise),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandBlue
                    )
                }

                Button(
                    onClick = {
                        onPlaceOrder(
                            plan,
                            targetLink,
                            activeQuantity,
                            totalPricePaise,
                            paymentMethod,
                            utrNumber,
                            proofUrl,
                            { showLowBalanceDialog = true },
                            { orderId -> placedOrderId = orderId }
                        )
                    },
                    enabled = !busy && !uploadingProof && isQtyValid && targetLink.trim().length >= 2 &&
                        (paymentMethod == "WALLET" || utrNumber.trim().length >= 6),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue, contentColor = PureWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("place_service_order_btn")
                ) {
                    if (busy) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = PureWhite, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Place Order • ${formatInrFromPaise(totalPricePaise)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showLowBalanceDialog) {
        AlertDialog(
            onDismissRequest = { showLowBalanceDialog = false },
            title = { Text("Insufficient Wallet Balance", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Your INR wallet balance is ${formatInrFromPaise(profile.walletBalancePaise)}, " +
                        "but this order costs ${formatInrFromPaise(totalPricePaise)}. Add funds via UPI or switch to Direct UPI payment."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLowBalanceDialog = false
                        onOpenAddFunds()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Add Funds", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLowBalanceDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (placedOrderId != null) {
        AlertDialog(
            onDismissRequest = {
                placedOrderId = null
                onOrderPlaced()
            },
            icon = {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(46.dp))
            },
            title = { Text("Order Placed!", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Your order for ${formatShortQty(activeQuantity)} of ${plan.name} has been submitted. Track live progress in My Orders."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        placedOrderId = null
                        onOrderPlaced()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("View My Orders", color = PureWhite)
                }
            }
        )
    }
}
