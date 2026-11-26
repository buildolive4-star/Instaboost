package com.example.admin.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

data class AdminSession(
    val uid: String,
    val displayName: String,
    val email: String,
    val isMainAdmin: Boolean
)

@Keep
@IgnoreExtraProperties
data class AdminUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Creator",
    val email: String = "",
    val country: String = "India",
    val coins: Long = 0L,
    val points: Long = 0L,
    val level: Long = 1L,
    val referralCode: String = "",
    val dailyStreak: Long = 0L,
    val spinsToday: Long = 0L,
    val mascotEnabled: Boolean = true,
    val darkMode: Boolean = false,
    val blocked: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AdminOrderRecord(
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
data class AdminSupportTicket(
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
data class AdminCatalogService(
    @DocumentId val id: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val name: String = "",
    val packType: String = "HASHTAGS",
    val itemCount: Long = 10L,
    val coinPrice: Long = 5L,
    val trending: Boolean = false,
    val published: Boolean = true,
    val contentLines: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AdminCoinPackage(
    @DocumentId val id: String = "",
    val name: String = "",
    val coins: Long = 500L,
    val bonusCoins: Long = 0L,
    val points: Long = 2L,
    val priceInr: Long = 249L,
    val badge: String = "",
    val sortOrder: Long = 1L,
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AdminAppConfig(
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
data class AdminStaffEntry(
    @DocumentId val id: String = "",
    val staffName: String = "",
    val active: Boolean = true,
    val linkedUid: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AdminAuditLog(
    @DocumentId val id: String = "",
    val actorUid: String = "",
    val actorName: String = "",
    val action: String = "",
    val targetPath: String = "",
    val summary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
