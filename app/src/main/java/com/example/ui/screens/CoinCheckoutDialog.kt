package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.AppConfig
import com.example.data.ImgBbUploader
import com.example.data.UserProfile
import com.example.data.formatInrFromPaise
import com.example.ui.components.DynamicPhotoImage
import com.example.ui.theme.GoldCoinDeep
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RaspberryPrimary
import com.example.ui.theme.SoftCardBg
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class CoinCheckoutSelection(
    val packageId: String,
    val packageName: String,
    val coins: Long,
    val bonusCoins: Long,
    val points: Long,
    val priceInr: Long
) {
    val totalCoins: Long get() = coins + bonusCoins
    val pricePaise: Long get() = priceInr * 100L
}

@Composable
fun CoinCheckoutDialog(
    selection: CoinCheckoutSelection,
    profile: UserProfile,
    appConfig: AppConfig,
    busy: Boolean,
    onDismiss: () -> Unit,
    onPayWithInrWallet: (CoinCheckoutSelection) -> Unit,
    onSubmitUpiPayment: (selection: CoinCheckoutSelection, utrNumber: String, proofUrl: String) -> Unit,
    onOpenAddFunds: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val formatter = remember { NumberFormat.getIntegerInstance(Locale.US) }

    val canUseWallet = profile.walletBalancePaise >= selection.pricePaise
    var paymentTab by remember(selection) {
        mutableStateOf(if (canUseWallet) "WALLET" else "UPI")
    }
    var utrText by remember(selection) { mutableStateOf("") }
    var proofUrl by remember(selection) { mutableStateOf("") }
    var uploadingProof by remember { mutableStateOf(false) }

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
                        onShowMessage(err.localizedMessage ?: "Screenshot upload failed")
                    }
                uploadingProof = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!busy && !uploadingProof) onDismiss() },
        title = {
            Column {
                Text("Pay & Get Coins", fontWeight = FontWeight.ExtraBold)
                Text(
                    text = "${formatter.format(selection.totalCoins)} Coins • ₹${formatter.format(selection.priceInr)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = RaspberryPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Summary Strip
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SoftCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Coins", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                "${formatter.format(selection.coins)}${if (selection.bonusCoins > 0) " + ${formatter.format(selection.bonusCoins)} bonus" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = GoldCoinDeep
                            )
                        }
                        if (selection.points > 0L) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Reward Points", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("+${selection.points} pts", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amount to Pay", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "₹${formatter.format(selection.priceInr)}.00",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = RaspberryPrimary
                            )
                        }
                    }
                }

                // Payment Method Selector (UPI QR vs Instant INR Wallet)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = paymentTab == "UPI",
                        onClick = { paymentTab = "UPI" },
                        leadingIcon = {
                            Icon(Icons.Filled.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text("UPI Pay", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RaspberryPrimary,
                            selectedLabelColor = PureWhite,
                            selectedLeadingIconColor = PureWhite
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("coin_pay_tab_upi")
                    )
                    FilterChip(
                        selected = paymentTab == "WALLET",
                        onClick = { paymentTab = "WALLET" },
                        leadingIcon = {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text("INR Wallet", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RaspberryPrimary,
                            selectedLabelColor = PureWhite,
                            selectedLeadingIconColor = PureWhite
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("coin_pay_tab_wallet")
                    )
                }

                if (paymentTab == "WALLET") {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (canUseWallet) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, if (canUseWallet) Color(0xFFA7F3D0) else Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "INR Service Wallet: ${formatInrFromPaise(profile.walletBalancePaise)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (canUseWallet) Color(0xFF065F46) else Color(0xFF991B1B)
                            )
                            if (canUseWallet) {
                                val remaining = profile.walletBalancePaise - selection.pricePaise
                                Text(
                                    text = "Coins will be credited INSTANTLY! Remaining INR balance: ${formatInrFromPaise(remaining)}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF047857)
                                )
                            } else {
                                val shortPaise = selection.pricePaise - profile.walletBalancePaise
                                Text(
                                    text = "You need ${formatInrFromPaise(shortPaise)} more in your INR Wallet, OR switch to 'UPI Pay' tab above to pay directly via QR.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFB91C1C)
                                )
                                OutlinedButton(
                                    onClick = {
                                        onDismiss()
                                        onOpenAddFunds()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Add Funds to INR Wallet", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // UPI QR & UTR Payment Flow
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (appConfig.paymentQrUrl.isNotBlank()) {
                            DynamicPhotoImage(
                                photoUrl = appConfig.paymentQrUrl,
                                contentDescription = "UPI QR Code for Coins",
                                modifier = Modifier
                                    .size(180.dp)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SoftCardBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Admin hasn't uploaded a QR code yet. Pay via UPI ID below or ask Admin to set UPI in Payment Setup.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        if (appConfig.upiId.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SoftCardBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    TextButton(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(appConfig.upiId))
                                            onShowMessage("UPI ID copied!")
                                        }
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy")
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    val note = "Buy ${selection.totalCoins} Coins"
                                    val uri = Uri.parse(
                                        "upi://pay?pa=${Uri.encode(appConfig.upiId)}" +
                                            "&pn=${Uri.encode(appConfig.upiMerchantName.ifBlank { "Fame Maker" })}" +
                                            "&am=${selection.priceInr}.00&cu=INR&tn=${Uri.encode(note)}"
                                    )
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    } catch (_: Exception) {
                                        onShowMessage("No UPI app detected. Scan QR or copy UPI ID.")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("coin_checkout_open_upi_btn")
                            ) {
                                Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pay ₹${formatter.format(selection.priceInr)} via UPI App", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedTextField(
                            value = utrText,
                            onValueChange = { utrText = it.take(40) },
                            label = { Text("12-Digit UTR / Transaction ID *") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("coin_checkout_utr_input")
                        )

                        OutlinedButton(
                            onClick = {
                                proofPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !uploadingProof,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("coin_checkout_proof_btn")
                        ) {
                            if (uploadingProof) {
                                CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Uploading to ImgBB...")
                            } else {
                                Icon(
                                    imageVector = if (proofUrl.isBlank()) Icons.Filled.AddPhotoAlternate else Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = if (proofUrl.isBlank()) RaspberryPrimary else SuccessGreen,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (proofUrl.isBlank()) "Upload Payment Screenshot (Optional)" else "Screenshot Uploaded ✓",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Text(
                            text = "After payment, enter your UTR above. Admin verifies and credits ${formatter.format(selection.totalCoins)} coins directly to your wallet.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (paymentTab == "WALLET") {
                Button(
                    onClick = { onPayWithInrWallet(selection) },
                    enabled = !busy && canUseWallet,
                    colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary),
                    modifier = Modifier.testTag("confirm_coin_wallet_pay_btn")
                ) {
                    if (busy) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PureWhite, strokeWidth = 2.dp)
                    } else {
                        Text("Pay ₹${formatter.format(selection.priceInr)} Instantly", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = { onSubmitUpiPayment(selection, utrText.trim(), proofUrl) },
                    enabled = !busy && !uploadingProof && utrText.trim().length >= 6,
                    colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary),
                    modifier = Modifier.testTag("confirm_coin_upi_submit_btn")
                ) {
                    if (busy) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PureWhite, strokeWidth = 2.dp)
                    } else {
                        Text("Submit UTR & Get Coins", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !busy && !uploadingProof
            ) {
                Text("Cancel")
            }
        }
    )
}
