package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import com.example.data.AppConfig
import com.example.data.CoinPackageItem
import com.example.data.DefaultCatalog
import com.example.data.FundRequestRecord
import com.example.data.UserProfile
import com.example.data.WalletTransaction
import com.example.data.formatInrFromPaise
import com.example.ui.components.BlueStarCoin
import com.example.ui.components.BonusCoinChip
import com.example.ui.components.CoinStackTile
import com.example.ui.components.GoldFlameCoin
import com.example.ui.components.HeaderIconButton
import com.example.ui.components.LevelCrownPill
import com.example.ui.components.PointsStarChip
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.GoldCoinDeep
import com.example.ui.theme.GoldCoinLight
import com.example.ui.theme.LevelGreen
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RaspberryLight
import com.example.ui.theme.RaspberryPrimary
import com.example.ui.theme.SoftCardBg
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmBalanceCardBg
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WalletScreen(
    profile: UserProfile,
    packages: List<CoinPackageItem>,
    appConfig: AppConfig,
    fundRequests: List<FundRequestRecord>,
    busy: Boolean,
    onBuyCoinsWithWallet: (CoinCheckoutSelection, () -> Unit, (Long) -> Unit) -> Unit,
    onSubmitCoinUpi: (CoinCheckoutSelection, String, String, () -> Unit) -> Unit,
    onOpenAddFunds: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val formatter = remember { NumberFormat.getIntegerInstance(Locale.US) }
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }
    var selectedPkg by remember { mutableStateOf<CoinPackageItem?>(null) }
    var checkoutSelection by remember { mutableStateOf<CoinCheckoutSelection?>(null) }
    var lastAddedCoins by remember { mutableStateOf<Long?>(null) }
    var lastSubmittedCoinUpi by remember { mutableStateOf<CoinCheckoutSelection?>(null) }

    // Custom INR -> Coins calculator
    var customInrText by remember { mutableStateOf("50") }
    val coinRate = appConfig.coinRatePerInr.coerceAtLeast(1L)
    val customInr = customInrText.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val customCoins = (customInr * coinRate).coerceAtMost(10_000_000L)
    val customPoints = (customInr / 50L).coerceIn(1L, 500L)
    val coinRequests = remember(fundRequests) {
        fundRequests.filter { it.requestType == "COIN_PACK" || it.coinsToCredit > 0L }
    }

    val (nextLevel, ptsRemaining) = DefaultCatalog.pointsToNextLevel(profile.points)
    val progress = DefaultCatalog.levelProgressFraction(profile.points)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 16.dp,
                bottom = if (selectedPkg != null) 186.dp else 104.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header ("Wallet" + History + Settings)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wallet",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        HeaderIconButton(
                            icon = Icons.Filled.History,
                            contentDescription = "Wallet History",
                            onClick = onOpenHistory,
                            testTag = "wallet_history_btn"
                        )
                        HeaderIconButton(
                            icon = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            onClick = onOpenSettings,
                            testTag = "wallet_settings_btn"
                        )
                    }
                }
            }

            // INR Service Wallet Card (for Service Orders & Instant Coin Top-Up)
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFF111827),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "INR Service Wallet",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF9CA3AF)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatInrFromPaise(profile.walletBalancePaise),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFACC15)
                            )
                            Text(
                                text = "Use for service orders or instant coins",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFD1D5DB)
                            )
                        }
                        Button(
                            onClick = onOpenAddFunds,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFACC15),
                                contentColor = Color(0xFF111827)
                            ),
                            modifier = Modifier.testTag("wallet_add_funds_btn")
                        ) {
                            Text("Add Funds", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            // Warm Cream Balance Card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = WarmBalanceCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CoinStackTile(stackCount = 1, tileSize = 58.dp)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Your balance",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = formatter.format(profile.coins),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF171717)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "coins",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary,
                                            modifier = Modifier.padding(bottom = 3.dp)
                                        )
                                    }
                                }
                            }
                            LevelCrownPill(level = profile.level)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (ptsRemaining > 0) "$ptsRemaining points to reach Level $nextLevel"
                                else "Maximum Creator Level Reached!",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "${profile.points} pts",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = RaspberryPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            color = RaspberryPrimary,
                            trackColor = Color(0xFFE7DFCF),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }

            // Custom INR -> Coins Payment Card ("Paisa pay kre to coin mile")
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Custom Coin Recharge",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Pay any ₹ via UPI or INR Wallet → Get Coins",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = RaspberryLight
                            ) {
                                Text(
                                    text = "₹1 = $coinRate coins",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = RaspberryPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf(10L, 50L, 100L, 200L, 500L)) { amt ->
                                val active = customInr == amt
                                FilterChip(
                                    selected = active,
                                    onClick = { customInrText = amt.toString() },
                                    label = { Text("₹$amt", fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RaspberryPrimary,
                                        selectedLabelColor = PureWhite
                                    )
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = customInrText,
                                onValueChange = { customInrText = it.filter { ch -> ch.isDigit() }.take(5) },
                                label = { Text("Amount (₹)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_coin_inr_input")
                            )
                            Button(
                                onClick = {
                                    if (customInr < 1L) {
                                        onShowMessage("Enter at least ₹1")
                                    } else {
                                        checkoutSelection = CoinCheckoutSelection(
                                            packageId = "custom_${customInr}_inr",
                                            packageName = "Custom ₹$customInr (${formatter.format(customCoins)} Coins)",
                                            coins = customCoins,
                                            bonusCoins = 0L,
                                            points = customPoints,
                                            priceInr = customInr
                                        )
                                    }
                                },
                                enabled = !busy && customInr >= 1L,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                                modifier = Modifier.testTag("custom_coin_buy_btn")
                            ) {
                                Text(
                                    text = "Get ${formatter.format(customCoins)} 🪙",
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Buy coins section header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Paid,
                        contentDescription = null,
                        tint = RaspberryPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Coin Packages (UPI / INR Wallet)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Coin Package Cards
            items(packages, key = { it.id }) { pkg ->
                val isSelected = selectedPkg?.id == pkg.id
                val isBestValue = pkg.badge.equals("Best Value", ignoreCase = true)
                val stackCount = when {
                    pkg.coins >= 10000L -> 4
                    pkg.coins >= 5000L -> 3
                    pkg.coins >= 1000L -> 2
                    else -> 1
                }

                val borderStroke = when {
                    isSelected -> BorderStroke(2.dp, RaspberryPrimary)
                    isBestValue -> BorderStroke(2.dp, Color(0xFFCBB144))
                    else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (pkg.badge.isNotBlank()) 10.dp else 0.dp)
                ) {
                    Surface(
                        onClick = { selectedPkg = pkg },
                        shape = RoundedCornerShape(22.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.surface else SoftCardBg,
                        border = borderStroke,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("coin_package_${pkg.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = if (pkg.badge.isNotBlank()) 22.dp else 18.dp,
                                    bottom = 18.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                CoinStackTile(stackCount = stackCount, tileSize = 62.dp)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = formatter.format(pkg.coins),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "coins",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }
                                    if (pkg.bonusCoins > 0L) {
                                        BonusCoinChip(text = "+${formatter.format(pkg.bonusCoins)} bonus")
                                    }
                                    PointsStarChip(text = "+${pkg.points} points")
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) RaspberryPrimary else RaspberryLight
                            ) {
                                Text(
                                    text = "₹${formatter.format(pkg.priceInr)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PureWhite else RaspberryPrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }

                    if (pkg.badge.isNotBlank()) {
                        val (badgeBg, badgeFg, showStar) = when {
                            pkg.badge.equals("Best Value", ignoreCase = true) ->
                                Triple(Color(0xFFCFF4D2), Color(0xFF1D7A39), true)
                            pkg.badge.equals("Super Saver", ignoreCase = true) ->
                                Triple(Color(0xFFFCD9DF), Color(0xFFB41443), false)
                            else ->
                                Triple(Color(0xFFF7EBC5), Color(0xFF7B5700), false)
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = badgeBg,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = 18.dp, y = (-11).dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                if (showStar) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = GoldCoin,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = pkg.badge,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeFg
                                )
                            }
                        }
                    }
                }
            }

            // Recent Coin Purchase UPI Requests Status List
            if (coinRequests.isNotEmpty()) {
                item {
                    Text(
                        text = "Your Coin Payment Requests",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                items(coinRequests, key = { it.id }) { req ->
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
                                    text = "${formatter.format(req.coinsToCredit)} Coins • ${formatInrFromPaise(req.amountPaise)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "UTR: ${req.utrNumber} • $dateLabel",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                if (req.adminNote.isNotBlank()) {
                                    Text(
                                        text = "Note: ${req.adminNote}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RaspberryPrimary
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

        // Sticky Bottom Payment Bar
        AnimatedVisibility(
            visible = selectedPkg != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 84.dp)
        ) {
            selectedPkg?.let { pkg ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 12.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GoldFlameCoin(size = 20.dp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "${formatter.format(pkg.coins + pkg.bonusCoins)} coins",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BlueStarCoin(size = 18.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+${pkg.points} points",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${formatter.format(pkg.priceInr)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RaspberryPrimary
                            )
                        }

                        Button(
                            onClick = {
                                checkoutSelection = CoinCheckoutSelection(
                                    packageId = pkg.id,
                                    packageName = pkg.name.ifBlank { "${formatter.format(pkg.coins)} Coins Pack" },
                                    coins = pkg.coins,
                                    bonusCoins = pkg.bonusCoins,
                                    points = pkg.points,
                                    priceInr = pkg.priceInr
                                )
                            },
                            enabled = !busy,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RaspberryPrimary,
                                contentColor = PureWhite
                            ),
                            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                            modifier = Modifier.testTag("wallet_pay_btn")
                        ) {
                            if (busy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = PureWhite,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pay",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Real Coin Checkout Dialog (UPI QR + UTR OR Instant INR Wallet)
    val activeSelection = checkoutSelection
    if (activeSelection != null) {
        CoinCheckoutDialog(
            selection = activeSelection,
            profile = profile,
            appConfig = appConfig,
            busy = busy,
            onDismiss = { checkoutSelection = null },
            onPayWithInrWallet = { sel ->
                onBuyCoinsWithWallet(
                    sel,
                    {
                        checkoutSelection = null
                        onOpenAddFunds()
                    },
                    { added ->
                        checkoutSelection = null
                        selectedPkg = null
                        lastAddedCoins = added
                    }
                )
            },
            onSubmitUpiPayment = { sel, utr, proof ->
                onSubmitCoinUpi(sel, utr, proof) {
                    checkoutSelection = null
                    selectedPkg = null
                    lastSubmittedCoinUpi = sel
                }
            },
            onOpenAddFunds = onOpenAddFunds,
            onShowMessage = onShowMessage
        )
    }

    if (lastAddedCoins != null) {
        AlertDialog(
            onDismissRequest = { lastAddedCoins = null },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(46.dp)
                )
            },
            title = { Text("Coins Added Instantly!", fontWeight = FontWeight.Bold) },
            text = {
                Text("${formatter.format(lastAddedCoins)} coins have been added to your wallet from your INR balance and recorded in Wallet History.")
            },
            confirmButton = {
                Button(
                    onClick = { lastAddedCoins = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary)
                ) {
                    Text("Done", color = PureWhite)
                }
            }
        )
    }

    lastSubmittedCoinUpi?.let { sel ->
        AlertDialog(
            onDismissRequest = { lastSubmittedCoinUpi = null },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(46.dp)
                )
            },
            title = { Text("Payment Submitted!", fontWeight = FontWeight.Bold) },
            text = {
                Text("Your ₹${formatter.format(sel.priceInr)} UPI payment for ${formatter.format(sel.totalCoins)} coins has been sent to Admin. Once UTR is verified, coins will be credited automatically!")
            },
            confirmButton = {
                Button(
                    onClick = { lastSubmittedCoinUpi = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary)
                ) {
                    Text("OK", color = PureWhite)
                }
            }
        )
    }
}

@Composable
fun WalletHistoryScreen(
    transactions: List<WalletTransaction>,
    onBack: () -> Unit
) {
    val tabs = listOf("All", "Coins Added", "Coins Spent", "Rewards")
    var selectedTab by remember { mutableStateOf("All") }
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }

    val filtered = remember(transactions, selectedTab) {
        when (selectedTab) {
            "Coins Added" -> transactions.filter { it.coinDelta > 0 }
            "Coins Spent" -> transactions.filter { it.coinDelta < 0 }
            "Rewards" -> transactions.filter { it.txType in listOf("BONUS", "DAILY_REWARD", "SPIN_REWARD", "REFERRAL") }
            else -> transactions
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    onClick = onBack,
                    testTag = "history_back_btn"
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Wallet History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tabs) { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RaspberryPrimary,
                            selectedLabelColor = PureWhite
                        )
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SoftCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No transactions yet in this category.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(filtered, key = { it.id.ifBlank { "${it.title}_${it.createdAt}" } }) { tx ->
                val isPositive = tx.coinDelta >= 0
                val dateLabel = tx.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "Just now"
                Surface(
                    shape = RoundedCornerShape(18.dp),
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
                                text = tx.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (tx.description.isNotBlank()) {
                                Text(
                                    text = tx.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$dateLabel • ${tx.status}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${if (isPositive) "+" else ""}${tx.coinDelta} coins",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) LevelGreen else RaspberryPrimary
                            )
                            if (tx.priceInr > 0) {
                                Text(
                                    text = "₹${tx.priceInr}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
