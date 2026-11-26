package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import com.example.data.AiContentRepository
import com.example.data.AppConfig
import com.example.data.AppNotification
import com.example.data.CatalogService
import com.example.data.CoinPackageItem
import com.example.data.CreatorBoostRepository
import com.example.data.DailyRewardDay
import com.example.data.DefaultCatalog
import com.example.data.FavoritePack
import com.example.data.FundRequestRecord
import com.example.data.OrderRecord
import com.example.data.ServiceCategoryUi
import com.example.data.ServiceFileItem
import com.example.data.ServiceOrderRecord
import com.example.data.ServicePlanItem
import com.example.data.SpinSegment
import com.example.data.SupportTicket
import com.example.data.UiState
import com.example.data.UserProfile
import com.example.data.WalletTransaction
import com.example.data.formatInrFromPaise
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val VM_TAG = "CreatorBoostVM"
private const val STOP_TIMEOUT_MS = 5000L

data class AiStudioState(
    val loading: Boolean = false,
    val generatedTitle: String = "",
    val generatedLines: List<String> = emptyList(),
    val error: String? = null
)

class CreatorBoostViewModel(
    private val repository: CreatorBoostRepository,
    private val aiRepository: AiContentRepository,
    val currentUserId: String
) : ViewModel() {

    val isAdmin: Boolean = repository.isCurrentUserAdmin()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _mascotMessage = MutableStateFlow("Welcome back!")
    val mascotMessage: StateFlow<String> = _mascotMessage.asStateFlow()

    private val _busyAction = MutableStateFlow(false)
    val busyAction: StateFlow<Boolean> = _busyAction.asStateFlow()

    private val _aiState = MutableStateFlow(AiStudioState())
    val aiState: StateFlow<AiStudioState> = _aiState.asStateFlow()

    val profileState: StateFlow<UiState<UserProfile>> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile>> { profile ->
            if (profile != null) UiState.Success(profile) else UiState.Loading
        }
        .catch { error ->
            val fsError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (fsError == null && error is CancellationException) throw error
            val root = fsError ?: error
            Log.w(VM_TAG, "Error observing profile", root)
            emit(UiState.Error(root.message ?: "Failed to load profile"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val orders: StateFlow<List<OrderRecord>> = repository.observeOrders(currentUserId)
        .catch { error ->
            val fsError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (fsError == null && error is CancellationException) throw error
            Log.w(VM_TAG, "Error observing orders", fsError ?: error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val transactions: StateFlow<List<WalletTransaction>> = repository.observeTransactions(currentUserId)
        .catch { error ->
            val fsError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (fsError == null && error is CancellationException) throw error
            Log.w(VM_TAG, "Error observing transactions", fsError ?: error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val favorites: StateFlow<List<FavoritePack>> = repository.observeFavorites(currentUserId)
        .catch { error ->
            val fsError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (fsError == null && error is CancellationException) throw error
            Log.w(VM_TAG, "Error observing favorites", fsError ?: error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val notifications: StateFlow<List<AppNotification>> = repository.observeNotifications(currentUserId)
        .catch { error ->
            val fsError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (fsError == null && error is CancellationException) throw error
            Log.w(VM_TAG, "Error observing notifications", fsError ?: error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val supportTickets: StateFlow<List<SupportTicket>> = repository.observeSupportTickets(currentUserId)
        .catch { error ->
            val fsError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (fsError == null && error is CancellationException) throw error
            Log.w(VM_TAG, "Error observing tickets", fsError ?: error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val catalogServices: StateFlow<List<CatalogService>> = repository.observePublishedCatalogServices()
        .map { remote ->
            val remoteIds = remote.map { it.id }.toSet()
            remote + DefaultCatalog.defaultServices.filterNot { it.id in remoteIds }
        }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(DefaultCatalog.defaultServices)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DefaultCatalog.defaultServices)

    val categories: StateFlow<List<ServiceCategoryUi>> = repository.observeActiveCatalogCategories()
        .map { remote ->
            val palette = listOf(
                Triple(Color(0xFFF58529), Color(0xFFDD2A7B), Color(0xFFFCE7F3)),
                Triple(Color(0xFF1877F2), Color(0xFF0A4DA2), Color(0xFFE0F2FE)),
                Triple(Color(0xFF2AABEE), Color(0xFF0284C7), Color(0xFFE0F7FA)),
                Triple(Color(0xFFEF4444), Color(0xFFB91C1C), Color(0xFFFEE2E2)),
                Triple(Color(0xFF8B5CF6), Color(0xFF5B21B6), Color(0xFFEDE9FE)),
                Triple(Color(0xFF10B981), Color(0xFF047857), Color(0xFFD1FAE5))
            )
            if (remote.isNotEmpty()) {
                val starterById = DefaultCatalog.starterSocialCategories.associateBy { it.id }
                remote.mapIndexed { idx, cat ->
                    val base = starterById[cat.id]
                    val colors = palette[idx % palette.size]
                    ServiceCategoryUi(
                        id = cat.id,
                        name = cat.name.ifBlank { base?.name ?: "Category" },
                        subtitle = cat.subtitle.ifBlank { base?.subtitle ?: "Services available" },
                        description = cat.description.ifBlank { base?.description ?: "Custom services managed by Admin" },
                        imageRes = null,
                        photoUrl = cat.photoUrl,
                        gradientStart = base?.gradientStart ?: colors.first,
                        gradientEnd = base?.gradientEnd ?: colors.second,
                        bannerTint = base?.bannerTint ?: colors.third,
                        emoji = cat.emoji.ifBlank { base?.emoji ?: "✨" },
                        trending = true
                    )
                }
            } else {
                DefaultCatalog.starterSocialCategories
            }
        }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(DefaultCatalog.starterSocialCategories)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DefaultCatalog.starterSocialCategories)

    val serviceFiles: StateFlow<List<ServiceFileItem>> = repository.observeActiveServiceFiles()
        .map { remote ->
            if (remote.isNotEmpty()) remote else DefaultCatalog.starterServiceFiles
        }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(DefaultCatalog.starterServiceFiles)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DefaultCatalog.starterServiceFiles)

    val servicePlans: StateFlow<List<ServicePlanItem>> = repository.observeActiveServicePlans()
        .map { remote ->
            if (remote.isNotEmpty()) remote else DefaultCatalog.starterServicePlans
        }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(DefaultCatalog.starterServicePlans)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DefaultCatalog.starterServicePlans)

    val myServiceOrders: StateFlow<List<ServiceOrderRecord>> = repository.observeMyServiceOrders(currentUserId)
        .catch { error ->
            val fsError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (fsError == null && error is CancellationException) throw error
            Log.w(VM_TAG, "Error observing serviceOrders", fsError ?: error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val myFundRequests: StateFlow<List<FundRequestRecord>> = repository.observeMyFundRequests(currentUserId)
        .catch { error ->
            val fsError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (fsError == null && error is CancellationException) throw error
            Log.w(VM_TAG, "Error observing fundRequests", fsError ?: error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val coinPackages: StateFlow<List<CoinPackageItem>> = repository.observeActiveCoinPackages()
        .map { remote ->
            if (remote.isEmpty()) DefaultCatalog.defaultCoinPackages
            else (DefaultCatalog.defaultCoinPackages + remote).sortedBy { it.sortOrder }
        }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(DefaultCatalog.defaultCoinPackages)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DefaultCatalog.defaultCoinPackages)

    val appConfig: StateFlow<AppConfig> = repository.observeAppConfig()
        .catch { error ->
            if (error is CancellationException) throw error
            emit(AppConfig())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AppConfig())

    init {
        viewModelScope.launch {
            repository.ensureUserProfileInitialized().onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Unable to initialize profile"
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun updateMascotMessage(message: String) {
        _mascotMessage.value = message
    }

    fun currentProfileOrNull(): UserProfile? = (profileState.value as? UiState.Success)?.data

    fun purchasePackage(pkg: CoinPackageItem, onSuccess: (Long) -> Unit = {}) {
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            _busyAction.value = true
            repository.purchaseCoinPackage(profile, pkg)
                .onSuccess { added ->
                    _mascotMessage.value = "You received $added coins!"
                    _toastMessage.value = "+$added coins & +${pkg.points} points added!"
                    onSuccess(added)
                }
                .onFailure { err ->
                    _toastMessage.value = err.localizedMessage ?: "Payment verification failed."
                }
            _busyAction.value = false
        }
    }

    fun unlockService(
        service: CatalogService,
        customLines: List<String> = service.contentLines,
        onInsufficientCoins: () -> Unit,
        onUnlocked: (OrderRecord) -> Unit
    ) {
        val profile = currentProfileOrNull() ?: return
        if (profile.coins < service.coinPrice) {
            onInsufficientCoins()
            return
        }
        viewModelScope.launch {
            _busyAction.value = true
            repository.unlockServicePack(profile, service, customLines)
                .onSuccess { order ->
                    _mascotMessage.value = "Pack unlocked! Ready to copy."
                    _toastMessage.value = "Unlocked ${service.name}!"
                    onUnlocked(order)
                }
                .onFailure { err ->
                    if (err.message == "INSUFFICIENT_COINS") {
                        onInsufficientCoins()
                    } else {
                        _toastMessage.value = err.localizedMessage ?: "Could not complete order."
                    }
                }
            _busyAction.value = false
        }
    }

    fun claimDailyReward(onClaimed: (DailyRewardDay) -> Unit = {}) {
        if (!appConfig.value.dailyRewardsEnabled) {
            _toastMessage.value = "Daily rewards are temporarily paused."
            return
        }
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            _busyAction.value = true
            repository.claimDailyReward(profile)
                .onSuccess { reward ->
                    _mascotMessage.value = "Day ${reward.day} claimed! +${reward.coins} coins!"
                    _toastMessage.value = "Claimed +${reward.coins} coins & +${reward.points} pts!"
                    onClaimed(reward)
                }
                .onFailure { err ->
                    _toastMessage.value = err.localizedMessage ?: "Unable to claim reward."
                }
            _busyAction.value = false
        }
    }

    fun recordSpin(segment: SpinSegment, onDone: () -> Unit = {}) {
        if (!appConfig.value.spinEnabled) {
            _toastMessage.value = "Spin & Win is temporarily paused."
            return
        }
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            repository.recordSpinResult(profile, segment)
                .onSuccess {
                    _mascotMessage.value = if (segment.coins > 0) "Woohoo! Won ${segment.label}!" else "Keep spinning!"
                    onDone()
                }
                .onFailure { err ->
                    _toastMessage.value = err.localizedMessage ?: "Spin could not be recorded."
                }
        }
    }

    fun redeemReferral(code: String, onDone: () -> Unit = {}) {
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            _busyAction.value = true
            repository.redeemReferralCode(profile, code, appConfig.value.referralBonusCoins)
                .onSuccess { bonusCoins ->
                    _mascotMessage.value = "Referral bonus unlocked! +$bonusCoins coins!"
                    _toastMessage.value = "Code redeemed! +$bonusCoins coins & +5 points added."
                    onDone()
                }
                .onFailure { err ->
                    _toastMessage.value = err.localizedMessage ?: "Invalid referral code."
                }
            _busyAction.value = false
        }
    }

    fun toggleFavorite(serviceId: String, serviceName: String, categoryName: String, contentText: String) {
        val existing = favorites.value.firstOrNull { it.serviceId == serviceId && it.serviceName == serviceName }
        viewModelScope.launch {
            if (existing != null) {
                repository.removeFavorite(existing.id)
                    .onSuccess { _toastMessage.value = "Removed from Favorites" }
                    .onFailure { _toastMessage.value = it.localizedMessage ?: "Failed to remove favorite" }
            } else {
                repository.saveFavorite(serviceId, serviceName, categoryName, contentText)
                    .onSuccess { _toastMessage.value = "Saved to Favorites!" }
                    .onFailure { _toastMessage.value = it.localizedMessage ?: "Failed to save favorite" }
            }
        }
    }

    fun removeFavoriteById(favId: String) {
        viewModelScope.launch {
            repository.removeFavorite(favId)
                .onSuccess { _toastMessage.value = "Removed from Favorites" }
                .onFailure { _toastMessage.value = it.localizedMessage ?: "Failed to remove" }
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead(notifications.value)
                .onSuccess { _toastMessage.value = "All notifications marked as read" }
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch { repository.deleteNotification(id) }
    }

    fun submitTicket(subject: String, category: String, description: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _busyAction.value = true
            repository.submitSupportTicket(subject, category, description)
                .onSuccess {
                    _toastMessage.value = "Support ticket submitted!"
                    onDone()
                }
                .onFailure { err ->
                    _toastMessage.value = err.localizedMessage ?: "Could not submit ticket."
                }
            _busyAction.value = false
        }
    }

    fun updatePreferences(
        displayName: String? = null,
        country: String? = null,
        mascotEnabled: Boolean? = null,
        darkMode: Boolean? = null
    ) {
        viewModelScope.launch {
            repository.updateProfilePreferences(displayName, country, mascotEnabled, darkMode)
                .onSuccess { _toastMessage.value = "Settings saved" }
                .onFailure { _toastMessage.value = it.localizedMessage ?: "Failed to update settings" }
        }
    }

    fun generateAiContent(toolType: String, topic: String, tone: String, count: Int) {
        val cleanTopic = topic.trim()
        if (cleanTopic.isBlank()) {
            _aiState.value = AiStudioState(error = "Please enter a topic or niche first.")
            return
        }
        viewModelScope.launch {
            _aiState.value = AiStudioState(loading = true)
            aiRepository.generateCreatorPack(toolType, cleanTopic, tone, count)
                .onSuccess { (title, lines) ->
                    _aiState.value = AiStudioState(
                        loading = false,
                        generatedTitle = title,
                        generatedLines = lines
                    )
                    _mascotMessage.value = "Fresh content ready for you!"
                }
                .onFailure { err ->
                    _aiState.value = AiStudioState(
                        loading = false,
                        error = err.localizedMessage ?: "AI generation failed."
                    )
                }
        }
    }

    fun regenerateServiceLines(
        service: CatalogService,
        existingOrderId: String? = null,
        onResult: (List<String>) -> Unit
    ) {
        viewModelScope.launch {
            _busyAction.value = true
            aiRepository.generateCreatorPack(
                toolType = service.packType,
                topic = service.categoryName,
                tone = "Trendy & Engaging",
                count = service.itemCount.toInt().coerceIn(10, 30)
            ).onSuccess { (_, lines) ->
                if (!existingOrderId.isNullOrBlank()) {
                    repository.updateOrderContent(existingOrderId, lines)
                }
                _mascotMessage.value = "Regenerated fresh ${service.categoryName} tags!"
                _toastMessage.value = "Generated ${lines.size} fresh items!"
                onResult(lines)
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Could not regenerate content."
            }
            _busyAction.value = false
        }
    }

    // --- ADMIN ACTIONS ---
    fun adminAddService(
        categoryId: String,
        categoryName: String,
        name: String,
        packType: String,
        coinPrice: Long,
        trending: Boolean,
        contentLines: List<String>,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            _busyAction.value = true
            repository.adminCreateCatalogService(
                categoryId, categoryName, name, packType, coinPrice, trending, contentLines
            ).onSuccess {
                _toastMessage.value = "Published '$name' to Services!"
                onDone()
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Admin action failed."
            }
            _busyAction.value = false
        }
    }

    fun adminAddCoinPackage(
        name: String,
        coins: Long,
        bonusCoins: Long,
        points: Long,
        priceInr: Long,
        badge: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            _busyAction.value = true
            repository.adminCreateCoinPackage(name, coins, bonusCoins, points, priceInr, badge)
                .onSuccess {
                    _toastMessage.value = "Coin package '$name' added!"
                    onDone()
                }
                .onFailure { err ->
                    _toastMessage.value = err.localizedMessage ?: "Admin action failed."
                }
            _busyAction.value = false
        }
    }

    // --- NEW SERVICE ORDERS & INR FUND REQUESTS ---

    fun placeServiceOrder(
        plan: ServicePlanItem,
        targetLink: String,
        quantity: Long,
        totalPricePaise: Long,
        paymentMethod: String,
        utrNumber: String = "",
        paymentProofUrl: String = "",
        onInsufficientBalance: () -> Unit = {},
        onSuccess: (String) -> Unit = {}
    ) {
        val profile = currentProfileOrNull() ?: return
        if (paymentMethod == "WALLET" && profile.walletBalancePaise < totalPricePaise) {
            onInsufficientBalance()
            return
        }
        viewModelScope.launch {
            _busyAction.value = true
            repository.placeServiceOrder(
                profile = profile,
                plan = plan,
                targetLink = targetLink,
                quantity = quantity,
                totalPricePaise = totalPricePaise,
                paymentMethod = paymentMethod,
                utrNumber = utrNumber,
                paymentProofUrl = paymentProofUrl
            ).onSuccess { orderId ->
                _mascotMessage.value = "Order placed! Track it in My Orders."
                _toastMessage.value = "Order placed for ${formatInrFromPaise(totalPricePaise)}!"
                onSuccess(orderId)
            }.onFailure { err ->
                if (err.message == "INSUFFICIENT_WALLET_BALANCE") {
                    onInsufficientBalance()
                } else {
                    _toastMessage.value = err.localizedMessage ?: "Could not place order."
                }
            }
            _busyAction.value = false
        }
    }

    fun submitFundRequest(
        amountPaise: Long,
        utrNumber: String,
        proofImageUrl: String = "",
        onSuccess: () -> Unit = {}
    ) {
        val profile = currentProfileOrNull() ?: return
        val minInr = appConfig.value.minDepositInr.coerceAtLeast(1L)
        if (amountPaise < minInr * 100L) {
            _toastMessage.value = "Minimum deposit amount is ₹$minInr."
            return
        }
        viewModelScope.launch {
            _busyAction.value = true
            repository.submitFundRequest(
                profile = profile,
                amountPaise = amountPaise,
                utrNumber = utrNumber,
                proofImageUrl = proofImageUrl
            ).onSuccess {
                _mascotMessage.value = "Deposit submitted! Admin will credit it soon."
                _toastMessage.value = "Deposit request for ${formatInrFromPaise(amountPaise)} submitted!"
                onSuccess()
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Could not submit deposit request."
            }
            _busyAction.value = false
        }
    }
}
