package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CatalogService
import com.example.data.DefaultCatalog
import com.example.data.UserProfile
import com.example.ui.components.BlueStarCoin
import com.example.ui.components.CategoryTileCard
import com.example.ui.components.GoldFlameCoin
import com.example.ui.components.HeaderIconButton
import com.example.ui.components.LevelCrownPill
import com.example.ui.theme.GoldCoinLight
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RaspberryLight
import com.example.ui.theme.RaspberryPrimary
import com.example.ui.theme.RaspberrySecondary
import com.example.ui.theme.SoftCardBg
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmBalanceCardBg
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    profile: UserProfile,
    services: List<CatalogService>,
    unreadNotifs: Int,
    onOpenCategory: (String) -> Unit,
    onOpenService: (CatalogService) -> Unit,
    onOpenServicesTab: () -> Unit,
    onOpenWalletTab: () -> Unit,
    onOpenDailyRewards: () -> Unit,
    onOpenSpinWheel: () -> Unit,
    onOpenAiStudio: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    onClaimDailyQuick: () -> Unit
) {
    val formatter = NumberFormat.getIntegerInstance(Locale.US)
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val dailyClaimedToday = profile.lastDailyClaimDay == today
    val (nextLevel, pointsRemaining) = DefaultCatalog.pointsToNextLevel(profile.points)
    val progress = DefaultCatalog.levelProgressFraction(profile.points)
    val trendingServices = services.filter { it.trending }.take(6)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hello, ${profile.displayName.substringBefore(" ")}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Discover packs & grow faster",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeaderIconButton(
                        icon = Icons.Filled.NotificationsNone,
                        contentDescription = "Notifications",
                        badgeCount = unreadNotifs,
                        onClick = onOpenNotifications,
                        testTag = "home_notifications_btn"
                    )
                    HeaderIconButton(
                        icon = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        onClick = onOpenSettings,
                        testTag = "home_settings_btn"
                    )
                }
            }
        }

        // Balance & Level Overview Card (mirrors Wallet aesthetic)
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = WarmBalanceCardBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenWalletTab)
                    .testTag("home_balance_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF0E5D1)),
                                contentAlignment = Alignment.Center
                            ) {
                                GoldFlameCoin(size = 36.dp)
                            }
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BlueStarCoin(size = 16.dp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${profile.points} points",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF171717),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = if (pointsRemaining > 0) "$pointsRemaining pts to Level $nextLevel" else "Max Level!",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        color = RaspberryPrimary,
                        trackColor = Color(0xFFE7DFCF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }

        // Quick Actions (Simple 4-item row — No Leaderboard!)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    icon = Icons.Filled.CardGiftcard,
                    label = "Daily",
                    bgColor = GoldCoinLight,
                    iconTint = Color(0xFFD97706),
                    onClick = onOpenDailyRewards,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_daily_rewards"
                )
                QuickActionTile(
                    icon = Icons.Filled.Casino,
                    label = "Spin",
                    bgColor = RaspberryLight,
                    iconTint = RaspberryPrimary,
                    onClick = onOpenSpinWheel,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_spin_win"
                )
                QuickActionTile(
                    icon = Icons.Filled.AutoAwesome,
                    label = "AI Studio",
                    bgColor = Color(0xFFEBF2FE),
                    iconTint = Color(0xFF2563EB),
                    onClick = onOpenAiStudio,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_ai_studio"
                )
                QuickActionTile(
                    icon = Icons.Filled.ReceiptLong,
                    label = "Orders",
                    bgColor = Color(0xFFE3F7EA),
                    iconTint = Color(0xFF16A34A),
                    onClick = onOpenOrders,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_orders"
                )
            }
        }

        // Daily Check-In Highlight Banner
        item {
            val nextDayIndex = ((profile.dailyStreak % 7L) + 1L).toInt()
            val todayReward = DefaultCatalog.dailyRewards.first { it.day == nextDayIndex }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(RaspberryPrimary, RaspberrySecondary)
                        )
                    )
                    .clickable(onClick = onOpenDailyRewards)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (dailyClaimedToday) "Daily Streak: Day ${profile.dailyStreak}" else "Day $nextDayIndex Reward Ready!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (dailyClaimedToday) "Come back tomorrow for your next bonus"
                            else "Claim +${todayReward.coins} coins & +${todayReward.points} points",
                            style = MaterialTheme.typography.bodySmall,
                            color = PureWhite.copy(alpha = 0.9f)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { if (dailyClaimedToday) onOpenDailyRewards() else onClaimDailyQuick() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = RaspberryPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("home_claim_daily_btn")
                    ) {
                        Text(
                            text = if (dailyClaimedToday) "View" else "Claim",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Popular Categories Horizontal Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onOpenServicesTab) {
                    Text("View All", color = RaspberryPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(DefaultCatalog.categories.take(6), key = { it.id }) { cat ->
                    val count = services.count { it.categoryId == cat.id }
                    CategoryTileCard(
                        category = cat,
                        packCount = count,
                        onClick = { onOpenCategory(cat.id) },
                        modifier = Modifier.width(108.dp)
                    )
                }
            }
        }

        // Trending Packs Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trending Packs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onOpenServicesTab) {
                    Text("Explore", color = RaspberryPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        items(trendingServices, key = { it.id }) { service ->
            Surface(
                onClick = { onOpenService(service) },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_trending_${service.id}")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = RaspberryLight,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = service.itemCount.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RaspberryPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = service.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${service.categoryName} • ${service.packType.lowercase().replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GoldFlameCoin(size = 20.dp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = service.coinPrice.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = SoftCardBg,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
