package com.example.admin.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.admin.data.AdminAppConfig
import com.example.admin.data.AdminFundRequest
import com.example.admin.data.AdminServiceOrder
import com.example.admin.data.formatInrFromPaise
import com.example.admin.theme.AdminBlue
import com.example.admin.theme.AdminBlueBg
import com.example.admin.theme.AdminBorder
import com.example.admin.theme.AdminDanger
import com.example.admin.theme.AdminDangerBg
import com.example.admin.theme.AdminGoldBg
import com.example.admin.theme.AdminGreen
import com.example.admin.theme.AdminGreenBg
import com.example.admin.theme.AdminPrimary
import com.example.admin.theme.AdminTextSecondary

@Composable
fun AdminOrdersPaymentsTab(
    orders: List<AdminServiceOrder>,
    fundRequests: List<AdminFundRequest>,
    appConfig: AdminAppConfig,
    busy: Boolean,
    onUpdateOrder: (AdminServiceOrder, String, String, Boolean) -> Unit,
    onApproveDeposit: (AdminFundRequest, String) -> Unit,
    onRejectDeposit: (AdminFundRequest, String) -> Unit,
    onSavePaymentConfig: (AdminAppConfig) -> Unit,
    onCopyToast: (String) -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) } // 0 = Orders, 1 = Deposits, 2 = Payment Setup
    var orderStatusFilter by remember { mutableStateOf("ALL") }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }
    var editingOrder by remember { mutableStateOf<AdminServiceOrder?>(null) }

    val pendingOrdersCount = orders.count { it.status == "PENDING" }
    val pendingDepositsCount = fundRequests.count { it.status == "PENDING" }

    val filteredOrders = remember(orders, orderStatusFilter) {
        if (orderStatusFilter == "ALL") orders
        else orders.filter { it.status == orderStatusFilter }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = subTab == 0,
                        onClick = { subTab = 0 },
                        label = {
                            Text(
                                text = if (pendingOrdersCount > 0) "Orders ($pendingOrdersCount new)" else "Orders (${orders.size})",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        modifier = Modifier.testTag("orders_subtab_orders")
                    )
                }
                item {
                    FilterChip(
                        selected = subTab == 1,
                        onClick = { subTab = 1 },
                        label = {
                            Text(
                                text = if (pendingDepositsCount > 0) "Deposits ($pendingDepositsCount new)" else "Deposits (${fundRequests.size})",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        modifier = Modifier.testTag("orders_subtab_deposits")
                    )
                }
                item {
                    FilterChip(
                        selected = subTab == 2,
                        onClick = { subTab = 2 },
                        label = { Text("Payment Setup", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("orders_subtab_payment_setup")
                    )
                }
            }
        }

        when (subTab) {
            // --- 0: SERVICE ORDERS ---
            0 -> {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ALL" to "All", "PENDING" to "Pending", "IN_PROGRESS" to "In Progress", "COMPLETED" to "Completed", "CANCELLED" to "Cancelled")
                            .forEach { (key, label) ->
                                item {
                                    FilterChip(
                                        selected = orderStatusFilter == key,
                                        onClick = { orderStatusFilter = key },
                                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                    }
                }

                if (filteredOrders.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, AdminBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No service orders in this view yet. Orders placed by users appear here live.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AdminTextSecondary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(filteredOrders, key = { it.id }) { order ->
                        AdminServiceOrderCard(
                            order = order,
                            busy = busy,
                            onCopy = onCopyToast,
                            onPreviewProof = { previewImageUrl = it },
                            onManage = { editingOrder = order }
                        )
                    }
                }
            }

            // --- 1: FUND / DEPOSIT REQUESTS ---
            1 -> {
                if (fundRequests.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, AdminBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No wallet deposit requests yet. When users submit a UPI UTR to add funds, approve or reject it here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AdminTextSecondary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(fundRequests, key = { it.id }) { req ->
                        AdminFundRequestCard(
                            request = req,
                            busy = busy,
                            onCopy = onCopyToast,
                            onPreviewProof = { previewImageUrl = it },
                            onApprove = { onApproveDeposit(req, "") },
                            onReject = { onRejectDeposit(req, "Payment not received / invalid UTR") }
                        )
                    }
                }
            }

            // --- 2: PAYMENT SETUP (UPI ID + IMGBB QR CODE + SUPPORT) ---
            else -> {
                item {
                    PaymentSetupEditorCard(
                        appConfig = appConfig,
                        busy = busy,
                        onSave = onSavePaymentConfig
                    )
                }
            }
        }
    }

    editingOrder?.let { order ->
        ManageServiceOrderDialog(
            order = order,
            busy = busy,
            onDismiss = { editingOrder = null },
            onSave = { status, note, refund ->
                onUpdateOrder(order, status, note, refund)
                editingOrder = null
            }
        )
    }

    previewImageUrl?.let { url ->
        AlertDialog(
            onDismissRequest = { previewImageUrl = null },
            title = { Text("Payment Screenshot", fontWeight = FontWeight.Bold) },
            text = {
                AsyncImage(
                    model = url,
                    contentDescription = "Payment proof screenshot",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.85f)
                )
            },
            confirmButton = {
                TextButton(onClick = { previewImageUrl = null }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun AdminServiceOrderCard(
    order: AdminServiceOrder,
    busy: Boolean,
    onCopy: (String) -> Unit,
    onPreviewProof: (String) -> Unit,
    onManage: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val (badgeBg, badgeFg) = when (order.status) {
        "COMPLETED" -> AdminGreenBg to AdminGreen
        "IN_PROGRESS" -> AdminBlueBg to AdminBlue
        "CANCELLED" -> AdminDangerBg to AdminDanger
        else -> AdminGoldBg to Color(0xFFB45309)
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, AdminBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.planName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${order.categoryName} › ${order.fileName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AdminTextSecondary
                    )
                }
                Surface(shape = RoundedCornerShape(8.dp), color = badgeBg) {
                    Text(
                        text = order.status.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeFg,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Qty: ${order.quantity} • ${formatInrFromPaise(order.totalPricePaise)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = AdminPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (order.paymentMethod == "WALLET") "Paid via Wallet" else "Direct UPI",
                    style = MaterialTheme.typography.labelMedium,
                    color = AdminBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "User: ${order.userName} (${order.userEmail.ifBlank { order.userId.take(8) }})",
                style = MaterialTheme.typography.bodySmall,
                color = AdminTextSecondary
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, AdminBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.targetLink,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(order.targetLink))
                            onCopy("Target link copied!")
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy target link", tint = AdminPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (order.utrNumber.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "UTR: ${order.utrNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AdminGreen
                    )
                    if (order.paymentProofUrl.isNotBlank()) {
                        TextButton(onClick = { onPreviewProof(order.paymentProofUrl) }) {
                            Text("View Proof", color = AdminBlue)
                        }
                    }
                }
            }

            if (order.adminNote.isNotBlank()) {
                Text(
                    text = "Note: ${order.adminNote}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AdminTextSecondary
                )
            }

            Button(
                onClick = onManage,
                enabled = !busy,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Update Order Status", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ManageServiceOrderDialog(
    order: AdminServiceOrder,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(order.status) }
    var note by remember { mutableStateOf(order.adminNote) }
    var refundToWallet by remember { mutableStateOf(order.paymentMethod == "WALLET") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Order", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${order.planName} • Qty ${order.quantity} (${formatInrFromPaise(order.totalPricePaise)})", style = MaterialTheme.typography.bodySmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED").forEach { st ->
                        item {
                            FilterChip(
                                selected = selectedStatus == st,
                                onClick = { selectedStatus = st },
                                label = { Text(st.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
                if (selectedStatus == "CANCELLED" && order.paymentMethod == "WALLET" && order.status != "CANCELLED") {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AdminGreenBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { refundToWallet = !refundToWallet }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Refund ${formatInrFromPaise(order.totalPricePaise)} to user's wallet",
                                style = MaterialTheme.typography.labelMedium,
                                color = AdminGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(if (refundToWallet) "YES" else "NO", fontWeight = FontWeight.Bold, color = AdminGreen)
                        }
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Admin Note (visible to user)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedStatus, note, refundToWallet) },
                enabled = !busy,
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary)
            ) { Text("Update", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AdminFundRequestCard(
    request: AdminFundRequest,
    busy: Boolean,
    onCopy: (String) -> Unit,
    onPreviewProof: (String) -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val (bg, fg) = when (request.status) {
        "APPROVED" -> AdminGreenBg to AdminGreen
        "REJECTED" -> AdminDangerBg to AdminDanger
        else -> AdminGoldBg to Color(0xFFB45309)
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, AdminBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatInrFromPaise(request.amountPaise),
                        style = MaterialTheme.typography.headlineSmall,
                        color = AdminPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${request.userName} • ${request.userEmail.ifBlank { request.userId.take(8) }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AdminTextSecondary
                    )
                }
                Surface(shape = RoundedCornerShape(8.dp), color = bg) {
                    Text(
                        text = request.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = fg,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("UTR: ${request.utrNumber}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(request.utrNumber))
                            onCopy("UTR copied!")
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy UTR", tint = AdminPrimary, modifier = Modifier.size(16.dp))
                    }
                }
                if (request.proofImageUrl.isNotBlank()) {
                    OutlinedButton(
                        onClick = { onPreviewProof(request.proofImageUrl) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("View Proof", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            if (request.status == "PENDING") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onReject,
                        enabled = !busy,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = AdminDanger, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", color = AdminDanger, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onApprove,
                        enabled = !busy,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminGreen),
                        modifier = Modifier.weight(1.4f)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve & Credit", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (request.adminNote.isNotBlank()) {
                Text("Note: ${request.adminNote}", style = MaterialTheme.typography.bodySmall, color = AdminTextSecondary)
            }
        }
    }
}

@Composable
private fun PaymentSetupEditorCard(
    appConfig: AdminAppConfig,
    busy: Boolean,
    onSave: (AdminAppConfig) -> Unit
) {
    var upiId by remember(appConfig) { mutableStateOf(appConfig.upiId) }
    var merchantName by remember(appConfig) { mutableStateOf(appConfig.upiMerchantName.ifBlank { "Fame Maker" }) }
    var paymentQrUrl by remember(appConfig) { mutableStateOf(appConfig.paymentQrUrl) }
    var minDeposit by remember(appConfig) { mutableStateOf(appConfig.minDepositInr.toString()) }
    var instructions by remember(appConfig) { mutableStateOf(appConfig.paymentInstructions) }
    var supportLink by remember(appConfig) { mutableStateOf(appConfig.supportLink) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, AdminBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.QrCode2, contentDescription = null, tint = AdminPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("UPI & QR Payment Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "Upload your UPI QR Code via ImgBB and set your UPI ID. Both Main Admin and Co-Admins can update this live for all users.",
                style = MaterialTheme.typography.bodySmall,
                color = AdminTextSecondary
            )

            ImgBbPhotoPickerField(
                label = "UPI Payment QR Code (ImgBB)",
                photoUrl = paymentQrUrl,
                fallbackEmoji = "📱",
                onPhotoChange = { paymentQrUrl = it },
                previewSize = 84.dp,
                previewContentScale = ContentScale.Fit
            )

            OutlinedTextField(
                value = upiId,
                onValueChange = { upiId = it },
                label = { Text("UPI ID (e.g. yourname@okaxis)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_upi_id_input")
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = merchantName,
                    onValueChange = { merchantName = it },
                    label = { Text("Merchant Name") },
                    singleLine = true,
                    modifier = Modifier.weight(1.3f)
                )
                OutlinedTextField(
                    value = minDeposit,
                    onValueChange = { minDeposit = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Min ₹") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.7f)
                )
            }
            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Payment Instructions for Users") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = supportLink,
                onValueChange = { supportLink = it },
                label = { Text("Support Link (WhatsApp / Telegram URL)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    onSave(
                        appConfig.copy(
                            upiId = upiId.trim(),
                            upiMerchantName = merchantName.trim().ifBlank { "Fame Maker" },
                            paymentQrUrl = paymentQrUrl.trim(),
                            minDepositInr = (minDeposit.toLongOrNull() ?: 10L).coerceAtLeast(1L),
                            paymentInstructions = instructions.trim(),
                            supportLink = supportLink.trim()
                        )
                    )
                },
                enabled = !busy,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("admin_save_payment_setup_btn")
            ) {
                Text("Save Payment Setup Live", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
