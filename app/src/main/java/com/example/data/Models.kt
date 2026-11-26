package com.example.data

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Creator",
    val email: String = "",
    val country: String = "India",
    val coins: Long = 0L,
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
