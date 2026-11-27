package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AppConfig
import com.example.data.FundRequestRecord
import com.example.data.ImgBbUploader
import com.example.data.ServiceOrderRecord
import com.example.data.UserProfile
import com.example.data.formatInrFromPaise
import com.example.data.formatShortQty
import com.example.ui.components.DynamicPhotoImage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftCardBg
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

private val OrdersBlue = Color(0xFF1D63ED)
private val OrdersDarkChip = Color(0xFF111827)
private val OrdersGoldAccent = Color(0xFFFACC15)

@Composable
fun ServiceOrdersScreen(
    profile: UserProfile,
    orders: List<ServiceOrderRecord>,
    onBack: () -> Unit,
    onOpenAddFunds: () -> Unit,
    onExploreServices: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }
    var searchInput by remember { mutableStateOf("") }
    var activeQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") }

    val pendingCount = orders.count { it.status == "PENDING" }
    val inProgressCount = orders.count { it.status == "IN_PROGRESS" }
    val completedCount = orders.count { it.status == "COMPLETED" }
    val cancelledCount = orders.count { it.status == "CANCELLED" }

    val filterTabs = listOf(
        "ALL" to "All (${orders.size})",
        "PENDING" to "Pending ($pendingCount)",
        "IN_PROGRESS" to "In progress ($inProgressCount)",
        "COMPLETED" to "Completed ($completedCount)",
        "CANCELLED" to "Cancelled ($cancelledCount)"
    )

    val filtered = remember(orders, activeQuery, statusFilter) {
        orders.filter { ord ->
            val matchesStatus = statusFilter == "ALL" || ord.status == statusFilter
            val q = activeQuery.trim()
            val matchesQuery = q.isBlank() ||
                ord.planName.contains(q, ignoreCase = true) ||
                ord.fileName.contains(q, ignoreCase = true) ||
                ord.categoryName.contains(q, ignoreCase = true) ||
                ord.targetLink.contains(q, ignoreCase = true) ||
                ord.id.contains(q, ignoreCase = true)
            matchesStatus && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ServiceFlowTopBar(
                title = "My Orders",
                balancePaise = profile.walletBalancePaise,
                onBack = onBack,
                onOpenAddFunds = onOpenAddFunds
            )
        }

        // Pill Search Bar + Blue Search Button (Matches Screenshot 2!)
        item {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = {
                            searchInput = it
                            activeQuery = it
                        },
                        placeholder = { Text("Search orders...", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("service_orders_search_input")
                    )
                    Button(
                        onClick = { activeQuery = searchInput },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = OrdersBlue, contentColor = PureWhite),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("service_orders_search_btn")
                    ) {
                        Text("Search", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Status Filter Pills
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filterTabs) { (key, label) ->
                    val selected = statusFilter == key
                    Surface(
                        onClick = { statusFilter = key },
                        shape = RoundedCornerShape(50),
                        color = if (selected) OrdersDarkChip else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (selected) OrdersDarkChip else MaterialTheme.colorScheme.outline),
                        modifier = Modifier.testTag("orders_filter_$key")
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) OrdersGoldAccent else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Inventory2,
                        contentDescription = null,
                        tint = OrdersDarkChip.copy(alpha = 0.8f),
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "No Orders Found",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (orders.isEmpty()) "You haven't placed any orders yet." else "No orders match this filter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    if (orders.isEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onExploreServices,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = OrdersBlue)
                        ) {
                            Text("Explore Services", color = PureWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { order ->
                val (badgeBg, badgeFg, badgeLabel) = when (order.status) {
                    "COMPLETED" -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), "Completed")
                    "IN_PROGRESS" -> Triple(Color(0xFFDBEAFE), Color(0xFF1D4ED8), "In Progress")
                    "CANCELLED" -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), "Cancelled")
                    else -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), "Pending")
                }
                val dateLabel = order.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "Just now"

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("service_order_card_${order.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${order.id.take(8).uppercase(Locale.US)} • $dateLabel",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Surface(shape = RoundedCornerShape(10.dp), color = badgeBg) {
                                Text(
                                    text = badgeLabel,
                                    color = badgeFg,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = order.planName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${order.categoryName} › ${order.fileName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftCardBg)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = order.targetLink,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(order.targetLink))
                                    onShowMessage("Link copied!")
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.ContentCopy,
                                    contentDescription = "Copy link",
                                    tint = OrdersBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Qty: ${formatShortQty(order.quantity)} • ${if (order.paymentMethod == "UPI_DIRECT") "UPI" else "Wallet"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = formatInrFromPaise(order.totalPricePaise),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = OrdersBlue
                            )
                        }

                        if (order.utrNumber.isNotBlank()) {
                            Text(
                                text = "UTR: ${order.utrNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }

                        if (order.adminNote.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Admin Note: ${order.adminNote}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF1E40AF),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddFundsScreen(
    profile: UserProfile,
    appConfig: AppConfig,
    fundRequests: List<FundRequestRecord>,
    busy: Boolean,
    onBack: () -> Unit,
    onSubmitFundRequest: (amountPaise: Long, utrNumber: String, proofUrl: String, onSuccess: () -> Unit) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }

    var amountInrText by remember { mutableStateOf("100") }
    var utrText by remember { mutableStateOf("") }
    var proofUrl by remember { mutableStateOf("") }
    var uploadingProof by remember { mutableStateOf(false) }

    val quickAmounts = listOf(50L, 100L, 200L, 500L, 1000L)
    val parsedAmountInr = amountInrText.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val amountPaise = parsedAmountInr * 100L
    val minInr = appConfig.minDepositInr.coerceAtLeast(1L)

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ServiceFlowTopBar(
                title = "Add Funds (UPI)",
                balancePaise = profile.walletBalancePaise,
                onBack = onBack,
                onOpenAddFunds = {}
            )
        }

        // Balance Summary Card
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = OrdersDarkChip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Current Service Wallet",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF9CA3AF)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatInrFromPaise(profile.walletBalancePaise),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = OrdersGoldAccent
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = OrdersGoldAccent.copy(alpha = 0.18f),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.AccountBalanceWallet,
                                contentDescription = null,
                                tint = OrdersGoldAccent,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }

        // Step 1: Scan UPI QR or Copy UPI ID
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.QrCode2, contentDescription = null, tint = OrdersBlue, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Step 1: Pay via UPI QR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (appConfig.paymentQrUrl.isNotBlank()) {
                        DynamicPhotoImage(
                            photoUrl = appConfig.paymentQrUrl,
                            contentDescription = "UPI QR Code",
                            modifier = Modifier
                                .size(210.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SoftCardBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Admin hasn't uploaded a QR code yet. Check UPI ID below or contact Admin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    if (appConfig.upiId.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SoftCardBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("copy_upi_id_btn")
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy")
                                }
                            }
                        }
                    }

                    if (appConfig.paymentInstructions.isNotBlank()) {
                        Text(
                            text = appConfig.paymentInstructions,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Quick Amount Pills
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickAmounts) { amt ->
                            val selected = parsedAmountInr == amt
                            Surface(
                                onClick = { amountInrText = amt.toString() },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) OrdersBlue else SoftCardBg,
                                border = BorderStroke(1.dp, if (selected) OrdersBlue else MaterialTheme.colorScheme.outline)
                            ) {
                                Text(
                                    text = "₹$amt",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) PureWhite else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amountInrText,
                        onValueChange = { amountInrText = it.filter { ch -> ch.isDigit() }.take(6) },
                        label = { Text("Deposit Amount (₹) • Min ₹$minInr") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_amount_input")
                    )

                    if (appConfig.upiId.isNotBlank() && parsedAmountInr >= minInr) {
                        OutlinedButton(
                            onClick = {
                                val uri = Uri.parse(
                                    "upi://pay?pa=${Uri.encode(appConfig.upiId)}" +
                                        "&pn=${Uri.encode(appConfig.upiMerchantName.ifBlank { "Fame Maker" })}" +
                                        "&am=$parsedAmountInr.00&cu=INR"
                                )
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                } catch (_: Exception) {
                                    onShowMessage("No UPI app detected. Scan QR or copy UPI ID instead.")
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_upi_app_btn")
                        ) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pay ₹$parsedAmountInr via UPI App", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Step 2: Submit UTR & Optional ImgBB Screenshot
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Step 2: Submit UTR & Proof",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = utrText,
                        onValueChange = { utrText = it.take(40) },
                        label = { Text("12-Digit UTR / Transaction Ref No. *") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_utr_input")
                    )

                    OutlinedButton(
                        onClick = {
                            proofPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !uploadingProof,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_upload_proof_btn")
                    ) {
                        if (uploadingProof) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Uploading to ImgBB...")
                        } else {
                            Icon(
                                imageVector = if (proofUrl.isBlank()) Icons.Filled.AddPhotoAlternate else Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = if (proofUrl.isBlank()) OrdersBlue else SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (proofUrl.isBlank()) "Upload Payment Screenshot (ImgBB)" else "Payment Proof Uploaded ✓",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onSubmitFundRequest(amountPaise, utrText, proofUrl) {
                                utrText = ""
                                proofUrl = ""
                            }
                        },
                        enabled = !busy && !uploadingProof && parsedAmountInr >= minInr && utrText.trim().length >= 6,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrdersBlue, contentColor = PureWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_deposit_btn")
                    ) {
                        if (busy) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PureWhite, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "Submit ₹$parsedAmountInr Deposit Request",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Recent Deposit Requests History
        if (fundRequests.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Deposit Requests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(fundRequests, key = { it.id }) { req ->
                val (bg, fg) = when (req.status) {
                    "APPROVED" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
                    "REJECTED" -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
                    else -> Color(0xFFFEF3C7) to Color(0xFFB45309)
                }
                val dateLabel = req.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "Just now"
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (req.requestType == "COIN_PACK" || req.coinsToCredit > 0L) {
                                    "${formatInrFromPaise(req.amountPaise)} → ${req.coinsToCredit} Coins"
                                } else {
                                    formatInrFromPaise(req.amountPaise)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "UTR: ${req.utrNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            if (req.adminNote.isNotBlank()) {
                                Text(
                                    text = "Note: ${req.adminNote}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OrdersBlue
                                )
                            }
                        }
                        Surface(shape = RoundedCornerShape(10.dp), color = bg) {
                            Text(
                                text = req.status,
                                color = fg,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
