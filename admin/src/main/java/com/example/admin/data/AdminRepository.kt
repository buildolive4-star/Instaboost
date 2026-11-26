package com.example.admin.data

import com.example.admin.firebase.AdminOpType
import com.example.admin.firebase.logAdminFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.Locale

class AdminRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    companion object {
        const val MAIN_ADMIN_EMAIL = "buildolive4@gmail.com"

        fun computeStaffKey(name: String, password: String): String {
            val normalized = "${name.trim().lowercase(Locale.US)}:${password.trim()}"
            val digest = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray(Charsets.UTF_8))
            val hex = digest.joinToString("") { "%02x".format(it) }
            return "s_${hex.take(32)}"
        }
    }

    private fun requireUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Please sign in first.")

    fun isSignedInAsMainAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        return user.email.equals(MAIN_ADMIN_EMAIL, ignoreCase = true) && user.isEmailVerified
    }

    suspend fun resolveExistingSession(): AdminSession? {
        val user = auth.currentUser ?: return null
        if (isSignedInAsMainAdmin()) {
            return AdminSession(
                uid = user.uid,
                displayName = user.displayName?.takeIf { it.isNotBlank() } ?: "Admin",
                email = user.email.orEmpty(),
                isMainAdmin = true
            )
        }
        return try {
            val grantSnap = db.collection("admins").document(user.uid).get().await()
            if (!grantSnap.exists() || grantSnap.getBoolean("active") != true) return null
            val staffKey = grantSnap.getString("staffKey").orEmpty()
            if (staffKey.isBlank()) return null
            val staffSnap = db.collection("adminStaff").document(staffKey).get().await()
            if (!staffSnap.exists() || staffSnap.getBoolean("active") != true) return null
            AdminSession(
                uid = user.uid,
                displayName = grantSnap.getString("displayName")?.takeIf { it.isNotBlank() } ?: "Admin",
                email = user.email.orEmpty(),
                isMainAdmin = false
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.GET, "admins/${user.uid}")
            null
        }
    }

    suspend fun signInWithNameAndPassword(name: String, password: String): Result<AdminSession> {
        val cleanName = name.trim()
        val cleanPassword = password.trim()
        if (cleanName.length < 2) {
            return Result.failure(IllegalArgumentException("Enter your valid admin name."))
        }
        if (cleanPassword.length < 4) {
            return Result.failure(IllegalArgumentException("Enter your valid password."))
        }
        return try {
            val user = auth.currentUser ?: error("Complete Google verification first.")
            val staffKey = computeStaffKey(cleanName, cleanPassword)
            val staffRef = db.collection("adminStaff").document(staffKey)
            val staffSnap = staffRef.get().await()
            if (!staffSnap.exists() || staffSnap.getBoolean("active") != true) {
                return Result.failure(IllegalArgumentException("Invalid name or password."))
            }
            val officialName = staffSnap.getString("staffName")?.takeIf { it.isNotBlank() } ?: cleanName
            val grantRef = db.collection("admins").document(user.uid)

            // Replace any stale grant document first so create rule passes cleanly
            val existingGrant = grantRef.get().await()
            if (existingGrant.exists()) {
                grantRef.delete().await()
            }

            grantRef.set(
                mapOf(
                    "userId" to user.uid,
                    "staffKey" to staffKey,
                    "displayName" to officialName,
                    "active" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            staffRef.update(
                mapOf(
                    "linkedUid" to user.uid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(
                AdminSession(
                    uid = user.uid,
                    displayName = officialName,
                    email = user.email.orEmpty(),
                    isMainAdmin = false
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.WRITE, "admins/session")
            Result.failure(IllegalArgumentException("Invalid name or password."))
        }
    }

    // --- REALTIME STREAMS ---

    fun observeAllUsers(): Flow<List<AdminUserProfile>> = flow {
        emitAll(
            db.collection("users")
                .snapshots()
                .map { snap ->
                    snap.toObjects(AdminUserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.coins }
                }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logAdminFirestoreError(e as? Exception ?: Exception(e), AdminOpType.LIST, "users")
                    emit(emptyList())
                }
        )
    }

    fun observeAllServices(): Flow<List<AdminCatalogService>> = flow {
        emitAll(
            db.collection("catalogServices")
                .snapshots()
                .map { snap ->
                    snap.toObjects(AdminCatalogService::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.name.lowercase(Locale.US) }
                }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logAdminFirestoreError(e as? Exception ?: Exception(e), AdminOpType.LIST, "catalogServices")
                    emit(emptyList())
                }
        )
    }

    fun observeAllCoinPackages(): Flow<List<AdminCoinPackage>> = flow {
        emitAll(
            db.collection("coinPackages")
                .snapshots()
                .map { snap ->
                    snap.toObjects(AdminCoinPackage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.sortOrder }
                }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logAdminFirestoreError(e as? Exception ?: Exception(e), AdminOpType.LIST, "coinPackages")
                    emit(emptyList())
                }
        )
    }

    fun observeAppConfig(): Flow<AdminAppConfig> = flow {
        emitAll(
            db.collection("appConfig").document("global")
                .snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(AdminAppConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            ?: AdminAppConfig()
                    } else AdminAppConfig()
                }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logAdminFirestoreError(e as? Exception ?: Exception(e), AdminOpType.GET, "appConfig/global")
                    emit(AdminAppConfig())
                }
        )
    }

    fun observeAuditLogs(): Flow<List<AdminAuditLog>> = flow {
        emitAll(
            db.collection("adminAuditLogs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(30)
                .snapshots()
                .map { snap ->
                    snap.toObjects(AdminAuditLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logAdminFirestoreError(e as? Exception ?: Exception(e), AdminOpType.LIST, "adminAuditLogs")
                    emit(emptyList())
                }
        )
    }

    fun observeAdminStaff(): Flow<List<AdminStaffEntry>> = flow {
        emitAll(
            db.collection("adminStaff")
                .snapshots()
                .map { snap ->
                    snap.toObjects(AdminStaffEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.staffName.lowercase(Locale.US) }
                }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logAdminFirestoreError(e as? Exception ?: Exception(e), AdminOpType.LIST, "adminStaff")
                    emit(emptyList())
                }
        )
    }

    fun observeUserOrders(userId: String): Flow<List<AdminOrderRecord>> = flow {
        emitAll(
            db.collection("users").document(userId).collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap -> snap.toObjects(AdminOrderRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { e ->
                    if (e is CancellationException) throw e
                    emit(emptyList())
                }
        )
    }

    fun observeUserTickets(userId: String): Flow<List<AdminSupportTicket>> = flow {
        emitAll(
            db.collection("users").document(userId).collection("supportTickets")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap -> snap.toObjects(AdminSupportTicket::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { e ->
                    if (e is CancellationException) throw e
                    emit(emptyList())
                }
        )
    }

    // --- AUDIT HELPER ---

    private suspend fun recordAudit(actorName: String, action: String, targetPath: String, summary: String) {
        try {
            val uid = requireUid()
            db.collection("adminAuditLogs").document().set(
                mapOf(
                    "actorUid" to uid,
                    "actorName" to actorName.take(80),
                    "action" to action.take(60),
                    "targetPath" to targetPath.take(200),
                    "summary" to summary.take(300),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (_: Exception) {
        }
    }

    // --- USER MANAGEMENT ---

    suspend fun updateUserAccount(
        user: AdminUserProfile,
        newCoins: Long,
        newPoints: Long,
        newLevel: Long,
        blocked: Boolean,
        reason: String,
        actorName: String
    ): Result<Unit> {
        val path = "users/${user.userId}"
        return try {
            requireUid()
            val safeCoins = newCoins.coerceIn(0L, 10_000_000L)
            val safePoints = newPoints.coerceIn(0L, 10_000_000L)
            val safeLevel = newLevel.coerceIn(1L, 10L)
            val coinDelta = safeCoins - user.coins
            val pointsDelta = safePoints - user.points

            val userRef = db.collection("users").document(user.userId)
            val batch = db.batch()
            batch.update(
                userRef,
                mapOf(
                    "coins" to safeCoins,
                    "points" to safePoints,
                    "level" to safeLevel,
                    "blocked" to blocked,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            if (coinDelta != 0L || pointsDelta != 0L) {
                val txRef = userRef.collection("transactions").document()
                val note = reason.trim().ifBlank { "Wallet adjusted by Admin" }
                batch.set(
                    txRef,
                    mapOf(
                        "userId" to user.userId,
                        "txType" to if (coinDelta >= 0) "BONUS" else "SPEND",
                        "title" to "Admin Adjustment",
                        "description" to note.take(240),
                        "coinDelta" to coinDelta.coerceIn(-1_000_000L, 1_000_000L),
                        "pointsDelta" to pointsDelta.coerceIn(-100_000L, 100_000L),
                        "priceInr" to 0L,
                        "status" to "SUCCESSFUL",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                val notifRef = userRef.collection("notifications").document()
                batch.set(
                    notifRef,
                    mapOf(
                        "userId" to user.userId,
                        "title" to if (coinDelta >= 0) "Coins Credited!" else "Wallet Updated",
                        "body" to "$note (${if (coinDelta >= 0) "+$coinDelta" else "$coinDelta"} coins)",
                        "category" to "SYSTEM",
                        "read" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            recordAudit(
                actorName = actorName,
                action = "UPDATE_USER",
                targetPath = path,
                summary = "${user.displayName}: coins=$safeCoins, pts=$safePoints, lvl=$safeLevel, blocked=$blocked"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(
        userId: String,
        orderId: String,
        newStatus: String,
        actorName: String
    ): Result<Unit> {
        val path = "users/$userId/orders/$orderId"
        return try {
            requireUid()
            db.collection("users").document(userId)
                .collection("orders").document(orderId)
                .update(
                    mapOf(
                        "status" to newStatus,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            recordAudit(actorName, "ORDER_STATUS", path, "Order set to $newStatus")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateTicketStatus(
        userId: String,
        ticketId: String,
        newStatus: String,
        actorName: String
    ): Result<Unit> {
        val path = "users/$userId/supportTickets/$ticketId"
        return try {
            requireUid()
            db.collection("users").document(userId)
                .collection("supportTickets").document(ticketId)
                .update(
                    mapOf(
                        "status" to newStatus,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            recordAudit(actorName, "TICKET_STATUS", path, "Support ticket set to $newStatus")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun sendNotificationToUser(
        userId: String,
        title: String,
        body: String,
        actorName: String
    ): Result<Unit> {
        val path = "users/$userId/notifications"
        return try {
            requireUid()
            val cleanTitle = title.trim()
            val cleanBody = body.trim()
            require(cleanTitle.isNotBlank() && cleanBody.isNotBlank()) { "Title and message are required." }
            db.collection("users").document(userId).collection("notifications").document().set(
                mapOf(
                    "userId" to userId,
                    "title" to cleanTitle.take(120),
                    "body" to cleanBody.take(400),
                    "category" to "SYSTEM",
                    "read" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            recordAudit(actorName, "SEND_NOTIF", path, "Sent '$cleanTitle'")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun broadcastNotification(
        users: List<AdminUserProfile>,
        title: String,
        body: String,
        actorName: String
    ): Result<Int> {
        return try {
            requireUid()
            val cleanTitle = title.trim()
            val cleanBody = body.trim()
            require(cleanTitle.isNotBlank() && cleanBody.isNotBlank()) { "Title and message are required." }
            var sent = 0
            users.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { u ->
                    val ref = db.collection("users").document(u.userId).collection("notifications").document()
                    batch.set(
                        ref,
                        mapOf(
                            "userId" to u.userId,
                            "title" to cleanTitle.take(120),
                            "body" to cleanBody.take(400),
                            "category" to "SYSTEM",
                            "read" to false,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                    sent++
                }
                batch.commit().await()
            }
            recordAudit(actorName, "BROADCAST", "users/*/notifications", "Broadcast '$cleanTitle' to $sent users")
            Result.success(sent)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.WRITE, "users/*/notifications")
            Result.failure(e)
        }
    }

    // --- CATALOG SERVICES & COIN PACKAGES ---

    suspend fun saveCatalogService(
        existingId: String?,
        categoryId: String,
        categoryName: String,
        name: String,
        packType: String,
        coinPrice: Long,
        trending: Boolean,
        published: Boolean,
        contentLines: List<String>,
        actorName: String
    ): Result<Unit> {
        var path = "catalogServices"
        return try {
            requireUid()
            val cleanName = name.trim()
            val cleanLines = contentLines.map { it.trim() }.filter { it.isNotBlank() }.take(60)
            require(cleanName.length in 2..120) { "Service name must be 2-120 characters." }
            require(cleanLines.isNotEmpty()) { "Enter at least one line of content." }
            if (existingId.isNullOrBlank()) {
                val ref = db.collection("catalogServices").document()
                path = ref.path
                ref.set(
                    mapOf(
                        "categoryId" to categoryId.take(60),
                        "categoryName" to categoryName.take(60),
                        "name" to cleanName,
                        "packType" to packType,
                        "itemCount" to cleanLines.size.toLong(),
                        "coinPrice" to coinPrice.coerceIn(0L, 100_000L),
                        "trending" to trending,
                        "published" to published,
                        "contentLines" to cleanLines,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                recordAudit(actorName, "CREATE_SERVICE", path, "Published '$cleanName'")
            } else {
                val ref = db.collection("catalogServices").document(existingId)
                path = ref.path
                ref.update(
                    mapOf(
                        "categoryId" to categoryId.take(60),
                        "categoryName" to categoryName.take(60),
                        "name" to cleanName,
                        "packType" to packType,
                        "itemCount" to cleanLines.size.toLong(),
                        "coinPrice" to coinPrice.coerceIn(0L, 100_000L),
                        "trending" to trending,
                        "published" to published,
                        "contentLines" to cleanLines,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                recordAudit(actorName, "UPDATE_SERVICE", path, "Updated '$cleanName'")
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteCatalogService(serviceId: String, serviceName: String, actorName: String): Result<Unit> {
        val path = "catalogServices/$serviceId"
        return try {
            requireUid()
            db.collection("catalogServices").document(serviceId).delete().await()
            recordAudit(actorName, "DELETE_SERVICE", path, "Deleted '$serviceName'")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun saveCoinPackage(
        existingId: String?,
        name: String,
        coins: Long,
        bonusCoins: Long,
        points: Long,
        priceInr: Long,
        badge: String,
        sortOrder: Long,
        active: Boolean,
        actorName: String
    ): Result<Unit> {
        var path = "coinPackages"
        return try {
            requireUid()
            val cleanName = name.trim()
            require(cleanName.length in 2..80) { "Package name must be 2-80 characters." }
            val common = mutableMapOf<String, Any>(
                "name" to cleanName,
                "coins" to coins.coerceIn(10L, 1_000_000L),
                "bonusCoins" to bonusCoins.coerceIn(0L, 500_000L),
                "points" to points.coerceIn(0L, 10_000L),
                "priceInr" to priceInr.coerceIn(1L, 500_000L),
                "badge" to badge.trim().take(40),
                "sortOrder" to sortOrder.coerceIn(0L, 100L),
                "active" to active,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (existingId.isNullOrBlank()) {
                val ref = db.collection("coinPackages").document()
                path = ref.path
                common["createdAt"] = FieldValue.serverTimestamp()
                ref.set(common).await()
                recordAudit(actorName, "CREATE_PACKAGE", path, "Created '$cleanName'")
            } else {
                val ref = db.collection("coinPackages").document(existingId)
                path = ref.path
                ref.update(common).await()
                recordAudit(actorName, "UPDATE_PACKAGE", path, "Updated '$cleanName'")
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteCoinPackage(pkgId: String, pkgName: String, actorName: String): Result<Unit> {
        val path = "coinPackages/$pkgId"
        return try {
            requireUid()
            db.collection("coinPackages").document(pkgId).delete().await()
            recordAudit(actorName, "DELETE_PACKAGE", path, "Deleted '$pkgName'")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- GLOBAL APP CONFIG ---

    suspend fun saveAppConfig(config: AdminAppConfig, actorName: String): Result<Unit> {
        val path = "appConfig/global"
        return try {
            requireUid()
            db.collection("appConfig").document("global").set(
                mapOf(
                    "maintenanceMode" to config.maintenanceMode,
                    "maintenanceMessage" to config.maintenanceMessage.trim().take(300),
                    "announcementTitle" to config.announcementTitle.trim().take(100),
                    "announcementBody" to config.announcementBody.trim().take(300),
                    "welcomeCoins" to config.welcomeCoins.coerceIn(0L, 100_000L),
                    "referralBonusCoins" to config.referralBonusCoins.coerceIn(0L, 100_000L),
                    "spinEnabled" to config.spinEnabled,
                    "dailyRewardsEnabled" to config.dailyRewardsEnabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            recordAudit(
                actorName,
                "UPDATE_CONFIG",
                path,
                "maintenance=${config.maintenanceMode}, welcome=${config.welcomeCoins}, spin=${config.spinEnabled}"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- MAIN ADMIN ONLY: STAFF CREATION (NAME + PASSWORD ONLY) & REVOCATION ---

    suspend fun createAdminAccess(name: String, password: String, actorName: String): Result<Unit> {
        val cleanName = name.trim()
        val cleanPassword = password.trim()
        if (cleanName.length < 2 || cleanName.length > 80) {
            return Result.failure(IllegalArgumentException("Name must be 2 to 80 characters."))
        }
        if (cleanPassword.length < 4) {
            return Result.failure(IllegalArgumentException("Password must be at least 4 characters."))
        }
        val staffKey = computeStaffKey(cleanName, cleanPassword)
        val path = "adminStaff/$staffKey"
        return try {
            requireUid()
            db.collection("adminStaff").document(staffKey).set(
                mapOf(
                    "staffName" to cleanName,
                    "active" to true,
                    "linkedUid" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            recordAudit(actorName, "CREATE_ADMIN", path, "Added admin '$cleanName'")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleAdminAccess(entry: AdminStaffEntry, active: Boolean, actorName: String): Result<Unit> {
        val path = "adminStaff/${entry.id}"
        return try {
            requireUid()
            db.collection("adminStaff").document(entry.id).update(
                mapOf(
                    "active" to active,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            recordAudit(actorName, "TOGGLE_ADMIN", path, "${entry.staffName} active=$active")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteAdminAccess(entry: AdminStaffEntry, actorName: String): Result<Unit> {
        val path = "adminStaff/${entry.id}"
        return try {
            requireUid()
            val batch = db.batch()
            batch.delete(db.collection("adminStaff").document(entry.id))
            if (entry.linkedUid.isNotBlank()) {
                batch.delete(db.collection("admins").document(entry.linkedUid))
            }
            batch.commit().await()
            recordAudit(actorName, "REMOVE_ADMIN", path, "Removed admin '${entry.staffName}'")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.DELETE, path)
            Result.failure(e)
        }
    }
}
