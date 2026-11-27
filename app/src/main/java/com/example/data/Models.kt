package com.example.data

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Locale

fun formatInrFromPaise(paise: Long): String {
    val safe = paise.coerceAtLeast(0L)
    val rupees = safe / 100L
    val rem = safe % 100L
    return String.format(Locale.US, "₹%,d.%02d", rupees, rem)
}

fun formatRatePer1k(paise: Long): String {
    val safe = paise.coerceAtLeast(0L)
    val rupees = safe / 100L
    val rem = safe % 100L
    return String.format(Locale.US, "₹%d.%02d/k", rupees, rem)
}

fun formatShortQty(qty: Long): String {
    return when {
        qty >= 1_000_000L && qty % 1_000_000L == 0L -> "${qty / 1_000_000L}M"
        qty >= 1_000L && qty % 1_000L == 0L -> "${qty / 1_000L}K"
        else -> String.format(Locale.US, "%,d", qty)
    }
}

data class QuantityTier(
    val quantity: Long,
    val discountPct: Int
)

fun parsePresetTiers(rawTiers: List<String>, minQty: Long, maxQty: Long): List<QuantityTier> {
    val parsed = rawTiers.mapNotNull { entry ->
        val parts = entry.trim().split(":")
        val qty = parts.getOrNull(0)?.trim()?.toLongOrNull() ?: return@mapNotNull null
        val disc = parts.getOrNull(1)?.trim()?.toIntOrNull()?.coerceIn(0, 80) ?: 0
        if (qty in minQty..maxQty) QuantityTier(qty, disc) else null
    }
    if (parsed.isNotEmpty()) return parsed
    return listOf(100L, 500L, 1000L, 2000L, 5000L, 10000L)
        .filter { it in minQty..maxQty }
        .ifEmpty { listOf(minQty) }
        .map { QuantityTier(it, 0) }
}

fun calculateOrderPricePaise(pricePer1kPaise: Long, quantity: Long, discountPct: Int = 0): Long {
    val safeRate = pricePer1kPaise.coerceAtLeast(1L)
    val safeQty = quantity.coerceAtLeast(1L)
    val rawPaise = ((safeRate * safeQty) + 500L) / 1000L
    val safeDisc = discountPct.coerceIn(0, 80)
    val discounted = (rawPaise * (100L - safeDisc)) / 100L
    return discounted.coerceAtLeast(1L)
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Creator",
    val email: String = "",
    val country: String = "India",
    val coins: Long = 0L,
    val walletBalancePaise: Long = 0L,
    val points: Long = 0L,
    val level: Long = 1L,
    val referralCode: String = "",
    val referredBy: String = "",
    val dailyStreak: Long = 0L,
    val lastDailyClaimDay: String = "",
    val lastSpinDay: String = "",
    val spinsToday: Long = 0L,
    val mascotEnabled: Boolean = true,
    val darkMode: Boolean = false,
    val blocked: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AppConfig(
    @DocumentId val id: String = "global",
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "",
    val announcementTitle: String = "",
    val announcementBody: String = "",
    val welcomeCoins: Long = 15L,
    val referralBonusCoins: Long = 25L,
    val spinEnabled: Boolean = true,
    val dailyRewardsEnabled: Boolean = true,
    val upiId: String = "",
    val upiMerchantName: String = "Fame Maker",
    val paymentQrUrl: String = "",
    val paymentInstructions: String = "Scan the UPI QR code or copy UPI ID, complete payment, and submit the 12-digit UTR number.",
    val minDepositInr: Long = 10L,
    val coinRatePerInr: Long = 2L,
    val supportLink: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ServiceFileItem(
    @DocumentId val id: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val name: String = "",
    val subtitle: String = "",
    val emoji: String = "⚡",
    val photoUrl: String = "",
    val sortOrder: Long = 10L,
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ServicePlanItem(
    @DocumentId val id: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val fileId: String = "",
    val fileName: String = "",
    val name: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val pricePer1kPaise: Long = 100L,
    val minQty: Long = 50L,
    val maxQty: Long = 100_000L,
    val inputHint: String = "Enter profile or post link",
    val presetTiers: List<String> = listOf("100:0", "500:0", "1000:0", "2000:5", "5000:6", "10000:9"),
    val sortOrder: Long = 10L,
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ServiceOrderRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val fileId: String = "",
    val fileName: String = "",
    val planId: String = "",
    val planName: String = "",
    val targetLink: String = "",
    val quantity: Long = 0L,
    val totalPricePaise: Long = 0L,
    val paymentMethod: String = "WALLET",
    val utrNumber: String = "",
    val paymentProofUrl: String = "",
    val status: String = "PENDING",
    val adminNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FundRequestRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val requestType: String = "WALLET_INR",
    val packageId: String = "",
    val packageName: String = "",
    val coinsToCredit: Long = 0L,
    val pointsToCredit: Long = 0L,
    val amountPaise: Long = 0L,
    val utrNumber: String = "",
    val proofImageUrl: String = "",
    val status: String = "PENDING",
    val adminNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class OrderRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val itemCount: Long = 0L,
    val coinPrice: Long = 0L,
    val status: String = "COMPLETED",
    val contentLines: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WalletTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val txType: String = "PURCHASE",
    val title: String = "",
    val description: String = "",
    val coinDelta: Long = 0L,
    val pointsDelta: Long = 0L,
    val priceInr: Long = 0L,
    val status: String = "SUCCESSFUL",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FavoritePack(
    @DocumentId val id: String = "",
    val userId: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val categoryName: String = "",
    val contentText: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val category: String = "SYSTEM",
    val read: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SupportTicket(
    @DocumentId val id: String = "",
    val userId: String = "",
    val subject: String = "",
    val category: String = "General",
    val description: String = "",
    val status: String = "OPEN",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CatalogService(
    @DocumentId val id: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val name: String = "",
    val packType: String = "HASHTAGS",
    val itemCount: Long = 30L,
    val coinPrice: Long = 3L,
    val trending: Boolean = false,
    val published: Boolean = true,
    val providerName: String = "Fame Maker",
    val photoUrl: String = "",
    val contentLines: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CatalogCategory(
    @DocumentId val id: String = "",
    val name: String = "",
    val subtitle: String = "Trending packs",
    val description: String = "",
    val emoji: String = "✨",
    val photoUrl: String = "",
    val sortOrder: Long = 10L,
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CoinPackageItem(
    @DocumentId val id: String = "",
    val name: String = "",
    val coins: Long = 500L,
    val bonusCoins: Long = 0L,
    val points: Long = 2L,
    val priceInr: Long = 249L,
    val badge: String = "",
    val sortOrder: Long = 0L,
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class ServiceCategoryUi(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val imageRes: Int? = null,
    val photoUrl: String = "",
    val gradientStart: Color,
    val gradientEnd: Color,
    val bannerTint: Color,
    val emoji: String,
    val trending: Boolean = false
)

data class LevelTier(
    val level: Int,
    val title: String,
    val requiredPoints: Long,
    val rewardNote: String
)

data class DailyRewardDay(
    val day: Int,
    val coins: Long,
    val points: Long,
    val bonusLabel: String? = null
)

data class SpinSegment(
    val label: String,
    val coins: Long,
    val points: Long,
    val color: Color,
    val weight: Int
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
