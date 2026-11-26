package com.example.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin.data.AdminAppConfig
import com.example.admin.data.AdminAuditLog
import com.example.admin.data.AdminCatalogService
import com.example.admin.data.AdminCoinPackage
import com.example.admin.data.AdminOrderRecord
import com.example.admin.data.AdminRepository
import com.example.admin.data.AdminSession
import com.example.admin.data.AdminStaffEntry
import com.example.admin.data.AdminSupportTicket
import com.example.admin.data.AdminUserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MS = 5000L

@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModel(
    private val repository: AdminRepository,
    val session: AdminSession
) : ViewModel() {

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()

    private val _selectedUserId = MutableStateFlow<String?>(null)
    val selectedUserId: StateFlow<String?> = _selectedUserId.asStateFlow()

    val users: StateFlow<List<AdminUserProfile>> = repository.observeAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val services: StateFlow<List<AdminCatalogService>> = repository.observeAllServices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val coinPackages: StateFlow<List<AdminCoinPackage>> = repository.observeAllCoinPackages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val appConfig: StateFlow<AdminAppConfig> = repository.observeAppConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AdminAppConfig())

    val auditLogs: StateFlow<List<AdminAuditLog>> = repository.observeAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val adminStaff: StateFlow<List<AdminStaffEntry>> =
        (if (session.isMainAdmin) repository.observeAdminStaff() else flowOf(emptyList()))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val selectedUserOrders: StateFlow<List<AdminOrderRecord>> = _selectedUserId
        .flatMapLatest { uid ->
            if (uid.isNullOrBlank()) flowOf(emptyList()) else repository.observeUserOrders(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val selectedUserTickets: StateFlow<List<AdminSupportTicket>> = _selectedUserId
        .flatMapLatest { uid ->
            if (uid.isNullOrBlank()) flowOf(emptyList()) else repository.observeUserTickets(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    fun clearToast() {
        _toast.value = null
    }

    fun selectUser(userId: String?) {
        _selectedUserId.value = userId
    }

    fun updateUser(
        user: AdminUserProfile,
        coins: Long,
        points: Long,
        level: Long,
        blocked: Boolean,
        reason: String,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _busy.value = true
            repository.updateUserAccount(user, coins, points, level, blocked, reason, session.displayName)
                .onSuccess {
                    _toast.value = "Updated ${user.displayName}"
                    onDone()
                }
                .onFailure { _toast.value = it.localizedMessage ?: "Failed to update user" }
            _busy.value = false
        }
    }

    fun updateOrderStatus(userId: String, orderId: String, status: String) {
        viewModelScope.launch {
            _busy.value = true
            repository.updateOrderStatus(userId, orderId, status, session.displayName)
                .onSuccess { _toast.value = "Order updated to $status" }
                .onFailure { _toast.value = it.localizedMessage ?: "Failed to update order" }
            _busy.value = false
        }
    }

    fun updateTicketStatus(userId: String, ticketId: String, status: String) {
        viewModelScope.launch {
            _busy.value = true
            repository.updateTicketStatus(userId, ticketId, status, session.displayName)
                .onSuccess { _toast.value = "Ticket marked $status" }
                .onFailure { _toast.value = it.localizedMessage ?: "Failed to update ticket" }
            _busy.value = false
        }
    }

    fun sendUserNotification(userId: String, title: String, body: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _busy.value = true
            repository.sendNotificationToUser(userId, title, body, session.displayName)
                .onSuccess {
                    _toast.value = "Notification sent"
                    onDone()
                }
                .onFailure { _toast.value = it.localizedMessage ?: "Failed to send notification" }
            _busy.value = false
        }
    }

    fun broadcastNotification(title: String, body: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _busy.value = true
            repository.broadcastNotification(users.value, title, body, session.displayName)
                .onSuccess { count ->
                    _toast.value = "Broadcast sent to $count users"
                    onDone()
                }
                .onFailure { _toast.value = it.localizedMessage ?: "Broadcast failed" }
            _busy.value = false
        }
    }

    fun saveService(
        existingId: String?,
        categoryId: String,
        categoryName: String,
        name: String,
        packType: String,
        coinPrice: Long,
        trending: Boolean,
        published: Boolean,
        contentLines: List<String>,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _busy.value = true
            repository.saveCatalogService(
                existingId, categoryId, categoryName, name, packType,
                coinPrice, trending, published, contentLines, session.displayName
            ).onSuccess {
                _toast.value = if (existingId == null) "Service published!" else "Service updated!"
                onDone()
            }.onFailure { _toast.value = it.localizedMessage ?: "Failed to save service" }
            _busy.value = false
        }
    }

    fun deleteService(service: AdminCatalogService) {
        viewModelScope.launch {
            _busy.value = true
            repository.deleteCatalogService(service.id, service.name, session.displayName)
                .onSuccess { _toast.value = "Deleted '${service.name}'" }
                .onFailure { _toast.value = it.localizedMessage ?: "Delete failed" }
            _busy.value = false
        }
    }

    fun savePackage(
        existingId: String?,
        name: String,
        coins: Long,
        bonusCoins: Long,
        points: Long,
        priceInr: Long,
        badge: String,
        sortOrder: Long,
        active: Boolean,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _busy.value = true
            repository.saveCoinPackage(
                existingId, name, coins, bonusCoins, points, priceInr, badge, sortOrder, active, session.displayName
            ).onSuccess {
                _toast.value = if (existingId == null) "Package created!" else "Package updated!"
                onDone()
            }.onFailure { _toast.value = it.localizedMessage ?: "Failed to save package" }
            _busy.value = false
        }
    }

    fun deletePackage(pkg: AdminCoinPackage) {
        viewModelScope.launch {
            _busy.value = true
            repository.deleteCoinPackage(pkg.id, pkg.name, session.displayName)
                .onSuccess { _toast.value = "Deleted '${pkg.name}'" }
                .onFailure { _toast.value = it.localizedMessage ?: "Delete failed" }
            _busy.value = false
        }
    }

    fun saveConfig(config: AdminAppConfig) {
        viewModelScope.launch {
            _busy.value = true
            repository.saveAppConfig(config, session.displayName)
                .onSuccess { _toast.value = "App settings updated live!" }
                .onFailure { _toast.value = it.localizedMessage ?: "Failed to save settings" }
            _busy.value = false
        }
    }

    // Main Admin exclusive actions
    fun createAdminMember(name: String, password: String, onDone: () -> Unit = {}) {
        if (!session.isMainAdmin) return
        viewModelScope.launch {
            _busy.value = true
            repository.createAdminAccess(name, password, session.displayName)
                .onSuccess {
                    _toast.value = "Admin '$name' created!"
                    onDone()
                }
                .onFailure { _toast.value = it.localizedMessage ?: "Failed to create admin" }
            _busy.value = false
        }
    }

    fun toggleAdminMember(entry: AdminStaffEntry, active: Boolean) {
        if (!session.isMainAdmin) return
        viewModelScope.launch {
            _busy.value = true
            repository.toggleAdminAccess(entry, active, session.displayName)
                .onSuccess { _toast.value = "${entry.staffName} ${if (active) "enabled" else "paused"}" }
                .onFailure { _toast.value = it.localizedMessage ?: "Update failed" }
            _busy.value = false
        }
    }

    fun deleteAdminMember(entry: AdminStaffEntry) {
        if (!session.isMainAdmin) return
        viewModelScope.launch {
            _busy.value = true
            repository.deleteAdminAccess(entry, session.displayName)
                .onSuccess { _toast.value = "Removed ${entry.staffName}" }
                .onFailure { _toast.value = it.localizedMessage ?: "Remove failed" }
            _busy.value = false
        }
    }
}
