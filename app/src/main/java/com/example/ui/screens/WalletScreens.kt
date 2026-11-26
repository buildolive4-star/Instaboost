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
import com.example.data.CoinPackageItem
import com.example.data.DefaultCatalog
import com.example.data.UserProfile
import com.example.data.WalletTransaction
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
    busy: Boolean,
    onPurchasePackage: (CoinPackageItem, (Long) -> Unit) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val formatter = remember { NumberFormat.getIntegerInstance(Locale.US) }
    var selectedPkg by remember { mutableStateOf<CoinPackageItem?>(null) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var lastAddedCoins by remember { mutableStateOf<Long?>(null) }

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

            // Warm Cream Balance Card (Exact match to Wallet screenshot!)
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
                        text = "Buy coins",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Coin Package Cards matching exact visual hierarchy of reference screenshots
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

                            // Price Pill Button
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

                    // Floating Promotional Badge Pill at top-left (Exact match to screenshots!)
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
        }

        // Sticky Bottom Payment Bar (Exact match to second Wallet screenshot!)
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
                            onClick = { showCheckoutDialog = true },
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

    // Checkout Confirmation Dialog
    val activePkg = selectedPkg
    if (showCheckoutDialog && activePkg != null) {
        AlertDialog(
            onDismissRequest = { if (!busy) showCheckoutDialog = false },
            title = { Text("Confirm Coin Purchase", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Package: ${formatter.format(activePkg.coins)} Coins")
                    if (activePkg.bonusCoins > 0) {
                        Text("Bonus: +${formatter.format(activePkg.bonusCoins)} Free Coins", color = GoldCoinDeep, fontWeight = FontWeight.SemiBold)
                    }
                    Text("Reward Points: +${activePkg.points} Points")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "Total: ₹${formatter.format(activePkg.priceInr)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RaspberryPrimary
                    )
                    Text(
                        text = "Demo Preview Checkout — verifies & records the transaction to your cloud Wallet History.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCheckoutDialog = false
                        onPurchasePackage(activePkg) { added ->
                            selectedPkg = null
                            lastAddedCoins = added
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary),
                    modifier = Modifier.testTag("confirm_pay_btn")
                ) {
                    Text("Complete Pay ₹${formatter.format(activePkg.priceInr)}", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) {
                    Text("Cancel")
                }
            }
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
            title = { Text("Payment Successful!", fontWeight = FontWeight.Bold) },
            text = {
                Text("${formatter.format(lastAddedCoins)} coins have been added to your wallet and recorded in Wallet History.")
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
