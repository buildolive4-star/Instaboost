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
                        .sortedByDescending { it.walletBalancePaise + it.coins }
                }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logAdminFirestoreError(e as? Exception ?: Exception(e), AdminOpType.LIST, "users")
                    emit(emptyList())
                }
        )
    }

    fun observeAllCategories(): Flow<List<AdminCatalogCategory>> = flow {
        emitAll(
            db.collection("catalogCategories")
                .snapshots()
                .map { snap ->
                    snap.toObjects(AdminCatalogCategory::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedWith(compareBy<AdminCatalogCategory> { it.sortOrder }.thenBy { it.name.lowercase(Locale.US) })
                }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logAdminFirestoreError(e as? Exception ?: Exception(e), AdminOpType.LIST, "catalogCategories")
                    emit(emptyList())
                }
        )
    }

    fun observeAllServiceFiles(): Flow<List<AdminServiceFile>> = flow {
        emitAll(
            db.collection("serviceFiles")
                .snapshots()
                .map { snap ->
                    snap.toObjects(AdminServiceFile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedWith(compareBy<AdminServiceFile> { it.sortOrder }.thenBy { it.name.lowercase(Locale.US) })
                }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logAdminFirestoreError(e as? Exception ?: Exception(e), AdminOpType.LIST, "serviceFiles")
                    emit(emptyList())
                }
        )
    }

    fun observeAllServicePlans(): Flow<List<AdminServicePlan>> = flow {
        emitAll(
            db.collection("servicePlans")
                .snapshots()
                .map { snap ->
                    snap.toObjects(AdminServicePlan::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedWith(compareBy<AdminServicePlan> { it.sortOrder }.thenBy { it.name.lowercase(Locale.US) })
                }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logAdminFirestoreError(e as? Exception ?: Exception(e), AdminOpType.LIST, "servicePlans")
                    emit(emptyList())
                }
        )
    }

    fun observeAllServiceOrders(): Flow<List<AdminServiceOrder>> = flow {
        emitAll(
            db.collection("serviceOrders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap ->
                    snap.toObjects(AdminServiceOrder::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logAdminFirestoreError(e as? Exception ?: Exception(e), AdminOpType.LIST, "serviceOrders")
                    emit(emptyList())
                }
        )
    }

    fun observeAllFundRequests(): Flow<List<AdminFundRequest>> = flow {
        emitAll(
            db.collection("fundRequests")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap ->
                    snap.toObjects(AdminFundRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logAdminFirestoreError(e as? Exception ?: Exception(e), AdminOpType.LIST, "fundRequests")
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
        newWalletBalancePaise: Long,
        newPoints: Long,
        newLevel: Long,
        blocked: Boolean,
        reason: String,
        actorName: String
    ): Result<Unit> {
        val targetUid = user.userId.ifBlank { user.id }
        val path = "users/$targetUid"
        return try {
            requireUid()
            val safeCoins = newCoins.coerceIn(0L, 10_000_000L)
            val safePaise = newWalletBalancePaise.coerceIn(0L, 1_000_000_000L)
            val safePoints = newPoints.coerceIn(0L, 10_000_000L)
            val safeLevel = newLevel.coerceIn(1L, 10L)
            val coinDelta = safeCoins - user.coins
            val paiseDelta = safePaise - user.walletBalancePaise
            val pointsDelta = safePoints - user.points

            val userRef = db.collection("users").document(targetUid)
            val batch = db.batch()
            batch.update(
                userRef,
                mapOf(
                    "coins" to safeCoins,
                    "walletBalancePaise" to safePaise,
                    "points" to safePoints,
                    "level" to safeLevel,
                    "blocked" to blocked,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            if (coinDelta != 0L || pointsDelta != 0L || paiseDelta != 0L) {
                val txRef = userRef.collection("transactions").document()
                val note = reason.trim().ifBlank { "Wallet updated by Admin" }
                val inrAbs = kotlin.math.abs(paiseDelta / 100L)
                batch.set(
                    txRef,
                    mapOf(
                        "userId" to user.userId,
                        "txType" to when {
                            paiseDelta > 0L -> "DEPOSIT"
                            paiseDelta < 0L -> "SPEND"
                            coinDelta >= 0L -> "BONUS"
                            else -> "SPEND"
                        },
                        "title" to if (paiseDelta != 0L) "Wallet Balance Adjustment" else "Admin Adjustment",
                        "description" to "$note (${formatInrFromPaise(safePaise)} balance)".take(240),
                        "coinDelta" to coinDelta.coerceIn(-1_000_000L, 1_000_000L),
                        "pointsDelta" to pointsDelta.coerceIn(-100_000L, 100_000L),
                        "priceInr" to inrAbs.coerceIn(0L, 5_000_000L),
                        "status" to "SUCCESSFUL",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                val notifRef = userRef.collection("notifications").document()
                val detail = buildString {
                    append(note)
                    if (paiseDelta != 0L) {
                        val sign = if (paiseDelta > 0) "+" else "-"
                        append(" • $sign${formatInrFromPaise(kotlin.math.abs(paiseDelta))}")
                    }
                    if (coinDelta != 0L) {
                        append(" • ${if (coinDelta > 0) "+$coinDelta" else "$coinDelta"} coins")
                    }
                }
                batch.set(
                    notifRef,
                    mapOf(
                        "userId" to user.userId,
                        "title" to if (paiseDelta > 0 || coinDelta > 0) "Wallet Credited!" else "Wallet Updated",
                        "body" to detail.take(400),
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
                summary = "${user.displayName}: ${formatInrFromPaise(safePaise)}, coins=$safeCoins, blocked=$blocked"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- SERVICE ORDERS & FUND REQUESTS ---

    suspend fun updateServiceOrderStatus(
        order: AdminServiceOrder,
        newStatus: String,
        adminNote: String,
        refundWallet: Boolean,
        actorName: String
    ): Result<Unit> {
        val path = "serviceOrders/${order.id}"
        return try {
            requireUid()
            val cleanNote = adminNote.trim().take(300)
            val shouldRefund = newStatus == "CANCELLED" &&
                refundWallet &&
                order.paymentMethod == "WALLET" &&
                order.status != "CANCELLED"

            db.runTransaction { tx ->
                val orderRef = db.collection("serviceOrders").document(order.id)
                val userRef = db.collection("users").document(order.userId)
                val userSnap = if (shouldRefund) tx.get(userRef) else null

                tx.update(
                    orderRef,
                    mapOf(
                        "status" to newStatus,
                        "adminNote" to cleanNote,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                if (shouldRefund && userSnap != null && userSnap.exists()) {
                    val currentPaise = userSnap.getLong("walletBalancePaise") ?: 0L
                    val refundedPaise = (currentPaise + order.totalPricePaise).coerceAtMost(1_000_000_000L)
                    tx.update(
                        userRef,
                        mapOf(
                            "walletBalancePaise" to refundedPaise,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    val txRef = userRef.collection("transactions").document()
                    tx.set(
                        txRef,
                        mapOf(
                            "userId" to order.userId,
                            "txType" to "REFUND",
                            "title" to "Order Refund: ${order.planName}".take(120),
                            "description" to "Refunded ${formatInrFromPaise(order.totalPricePaise)} for cancelled order".take(240),
                            "coinDelta" to 0L,
                            "pointsDelta" to 0L,
                            "priceInr" to (order.totalPricePaise / 100L).coerceIn(0L, 5_000_000L),
                            "status" to "REFUNDED",
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                val notifRef = userRef.collection("notifications").document()
                val statusLabel = newStatus.replace("_", " ")
                val body = buildString {
                    append("${order.planName} (Qty ${order.quantity}) is now $statusLabel.")
                    if (shouldRefund) append(" ${formatInrFromPaise(order.totalPricePaise)} refunded to wallet.")
                    if (cleanNote.isNotBlank()) append(" Note: $cleanNote")
                }
                tx.set(
                    notifRef,
                    mapOf(
                        "userId" to order.userId,
                        "title" to "Order $statusLabel",
                        "body" to body.take(400),
                        "category" to "ORDER",
                        "read" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()

            recordAudit(
                actorName = actorName,
                action = "SERVICE_ORDER",
                targetPath = path,
                summary = "${order.planName} (${order.userName}) -> $newStatus${if (shouldRefund) " + refunded" else ""}"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun approveFundRequest(
        request: AdminFundRequest,
        adminNote: String,
        actorName: String
    ): Result<Unit> {
        val path = "fundRequests/${request.id}"
        return try {
            requireUid()
            if (request.status == "APPROVED") {
                return Result.failure(IllegalStateException("Already approved."))
            }
            val cleanNote = adminNote.trim().ifBlank { "Verified UTR ${request.utrNumber}" }.take(300)
            val isCoinPack = request.isCoinPurchase

            db.runTransaction { tx ->
                val reqRef = db.collection("fundRequests").document(request.id)
                val userRef = db.collection("users").document(request.userId)
                val reqSnap = tx.get(reqRef)
                if (reqSnap.getString("status") == "APPROVED") {
                    return@runTransaction
                }
                val userSnap = tx.get(userRef)

                tx.update(
                    reqRef,
                    mapOf(
                        "status" to "APPROVED",
                        "adminNote" to cleanNote,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                if (isCoinPack) {
                    val coinsAdd = request.coinsToCredit.coerceIn(1L, 10_000_000L)
                    val pointsAdd = request.pointsToCredit.coerceIn(0L, 100_000L)
                    val currentCoins = userSnap.getLong("coins") ?: 0L
                    val currentPoints = userSnap.getLong("points") ?: 0L
                    val updatedCoins = (currentCoins + coinsAdd).coerceAtMost(10_000_000L)
                    val updatedPoints = (currentPoints + pointsAdd).coerceAtMost(10_000_000L)
                    val updatedLevel = calculateAdminLevel(updatedPoints)

                    if (userSnap.exists()) {
                        tx.update(
                            userRef,
                            mapOf(
                                "coins" to updatedCoins,
                                "points" to updatedPoints,
                                "level" to updatedLevel,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                    val txRef = userRef.collection("transactions").document()
                    tx.set(
                        txRef,
                        mapOf(
                            "userId" to request.userId,
                            "txType" to "PURCHASE",
                            "title" to request.packageName.ifBlank { "$coinsAdd Coins Pack" }.take(120),
                            "description" to "UPI Verified (${formatInrFromPaise(request.amountPaise)}) • UTR ${request.utrNumber}".take(240),
                            "coinDelta" to coinsAdd.coerceAtMost(1_000_000L),
                            "pointsDelta" to pointsAdd.coerceAtMost(100_000L),
                            "priceInr" to (request.amountPaise / 100L).coerceIn(0L, 5_000_000L),
                            "status" to "SUCCESSFUL",
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                    val notifRef = userRef.collection("notifications").document()
                    tx.set(
                        notifRef,
                        mapOf(
                            "userId" to request.userId,
                            "title" to "Coins Credited: +$coinsAdd Coins!",
                            "body" to "Your ${formatInrFromPaise(request.amountPaise)} UPI payment (UTR ${request.utrNumber}) was approved. +$coinsAdd coins added!".take(400),
                            "category" to "PURCHASE",
                            "read" to false,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else {
                    val currentPaise = userSnap.getLong("walletBalancePaise") ?: 0L
                    val updatedPaise = (currentPaise + request.amountPaise).coerceAtMost(1_000_000_000L)

                    if (userSnap.exists()) {
                        tx.update(
                            userRef,
                            mapOf(
                                "walletBalancePaise" to updatedPaise,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                    val txRef = userRef.collection("transactions").document()
                    tx.set(
                        txRef,
                        mapOf(
                            "userId" to request.userId,
                            "txType" to "DEPOSIT",
                            "title" to "Wallet Top-Up Approved",
                            "description" to "Added ${formatInrFromPaise(request.amountPaise)} • UTR ${request.utrNumber}".take(240),
                            "coinDelta" to 0L,
                            "pointsDelta" to 0L,
                            "priceInr" to (request.amountPaise / 100L).coerceIn(0L, 5_000_000L),
                            "status" to "SUCCESSFUL",
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                    val notifRef = userRef.collection("notifications").document()
                    tx.set(
                        notifRef,
                        mapOf(
                            "userId" to request.userId,
                            "title" to "Funds Added: ${formatInrFromPaise(request.amountPaise)}",
                            "body" to "Your UPI deposit (UTR ${request.utrNumber}) was approved. New balance: ${formatInrFromPaise(updatedPaise)}.".take(400),
                            "category" to "PURCHASE",
                            "read" to false,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }.await()

            recordAudit(
                actorName = actorName,
                action = if (isCoinPack) "APPROVE_COINS" else "APPROVE_DEPOSIT",
                targetPath = path,
                summary = if (isCoinPack) {
                    "Credited +${request.coinsToCredit} coins (${formatInrFromPaise(request.amountPaise)}) to ${request.userName}"
                } else {
                    "Approved ${formatInrFromPaise(request.amountPaise)} for ${request.userName} (UTR ${request.utrNumber})"
                }
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun rejectFundRequest(
        request: AdminFundRequest,
        adminNote: String,
        actorName: String
    ): Result<Unit> {
        val path = "fundRequests/${request.id}"
        return try {
            requireUid()
            val cleanNote = adminNote.trim().ifBlank { "Payment could not be verified" }.take(300)
            val isCoinPack = request.isCoinPurchase
            val batch = db.batch()
            batch.update(
                db.collection("fundRequests").document(request.id),
                mapOf(
                    "status" to "REJECTED",
                    "adminNote" to cleanNote,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val notifRef = db.collection("users").document(request.userId).collection("notifications").document()
            batch.set(
                notifRef,
                mapOf(
                    "userId" to request.userId,
                    "title" to if (isCoinPack) "Coin Payment Rejected" else "Deposit Request Rejected",
                    "body" to "${formatInrFromPaise(request.amountPaise)} (UTR ${request.utrNumber}) was rejected. $cleanNote".take(400),
                    "category" to "SYSTEM",
                    "read" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            recordAudit(
                actorName,
                if (isCoinPack) "REJECT_COINS" else "REJECT_DEPOSIT",
                path,
                "Rejected ${formatInrFromPaise(request.amountPaise)} for ${request.userName}"
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

    // --- CATALOG CATEGORIES, SERVICE FILES, SERVICE PLANS & COIN PACKAGES ---

    suspend fun saveCatalogCategory(
        existingId: String?,
        name: String,
        subtitle: String,
        description: String,
        emoji: String,
        photoUrl: String,
        sortOrder: Long,
        active: Boolean,
        actorName: String
    ): Result<Unit> {
        var path = "catalogCategories"
        return try {
            requireUid()
            val cleanName = name.trim()
            val cleanSubtitle = subtitle.trim().ifBlank { "Instant Services" }.take(100)
            val cleanDesc = description.trim().ifBlank { "Explore custom $cleanName services" }.take(240)
            val cleanEmoji = emoji.trim().ifBlank { "✨" }.take(16)
            val cleanPhoto = photoUrl.trim().take(250_000)
            require(cleanName.length in 2..80) { "Category name must be 2-80 characters." }

            val fields = mutableMapOf<String, Any>(
                "name" to cleanName,
                "subtitle" to cleanSubtitle,
                "description" to cleanDesc,
                "emoji" to cleanEmoji,
                "photoUrl" to cleanPhoto,
                "sortOrder" to sortOrder.coerceIn(0L, 200L),
                "active" to active,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (existingId.isNullOrBlank()) {
                val slug = cleanName.lowercase(Locale.US)
                    .replace(Regex("[^a-z0-9]+"), "_")
                    .trim('_')
                    .take(36)
                val ref = if (slug.length >= 2) {
                    val candidate = db.collection("catalogCategories").document(slug)
                    val alreadyExists = try {
                        candidate.get().await().exists()
                    } catch (_: Exception) {
                        true
                    }
                    if (alreadyExists) db.collection("catalogCategories").document() else candidate
                } else {
                    db.collection("catalogCategories").document()
                }
                path = ref.path
                fields["createdAt"] = FieldValue.serverTimestamp()
                ref.set(fields).await()
                recordAudit(actorName, "CREATE_CATEGORY", path, "Created category '$cleanName'")
            } else {
                val ref = db.collection("catalogCategories").document(existingId)
                path = ref.path
                ref.update(fields).await()
                recordAudit(actorName, "UPDATE_CATEGORY", path, "Updated category '$cleanName'")
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteCatalogCategory(categoryId: String, categoryName: String, actorName: String): Result<Unit> {
        val path = "catalogCategories/$categoryId"
        return try {
            requireUid()
            db.collection("catalogCategories").document(categoryId).delete().await()
            recordAudit(actorName, "DELETE_CATEGORY", path, "Deleted category '$categoryName'")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun saveServiceFile(
        existingId: String?,
        categoryId: String,
        categoryName: String,
        name: String,
        subtitle: String,
        emoji: String,
        photoUrl: String,
        sortOrder: Long,
        active: Boolean,
        actorName: String
    ): Result<Unit> {
        var path = "serviceFiles"
        return try {
            requireUid()
            val cleanName = name.trim()
            val cleanCatId = categoryId.trim().take(80)
            val cleanCatName = categoryName.trim().take(80)
            require(cleanCatId.isNotBlank() && cleanCatName.isNotBlank()) { "Select a category first." }
            require(cleanName.length in 2..100) { "File name must be 2-100 characters." }

            val fields = mutableMapOf<String, Any>(
                "categoryId" to cleanCatId,
                "categoryName" to cleanCatName,
                "name" to cleanName,
                "subtitle" to subtitle.trim().take(160),
                "emoji" to emoji.trim().ifBlank { "⚡" }.take(16),
                "photoUrl" to photoUrl.trim().take(2048),
                "sortOrder" to sortOrder.coerceIn(0L, 500L),
                "active" to active,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (existingId.isNullOrBlank()) {
                val ref = db.collection("serviceFiles").document()
                path = ref.path
                fields["createdAt"] = FieldValue.serverTimestamp()
                ref.set(fields).await()
                recordAudit(actorName, "CREATE_FILE", path, "Added '$cleanName' under $cleanCatName")
            } else {
                val ref = db.collection("serviceFiles").document(existingId)
                path = ref.path
                ref.update(fields).await()
                recordAudit(actorName, "UPDATE_FILE", path, "Updated '$cleanName'")
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteServiceFile(fileId: String, fileName: String, actorName: String): Result<Unit> {
        val path = "serviceFiles/$fileId"
        return try {
            requireUid()
            db.collection("serviceFiles").document(fileId).delete().await()
            recordAudit(actorName, "DELETE_FILE", path, "Deleted file '$fileName'")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun saveServicePlan(
        existingId: String?,
        categoryId: String,
        categoryName: String,
        fileId: String,
        fileName: String,
        name: String,
        description: String,
        photoUrl: String,
        pricePer1kPaise: Long,
        minQty: Long,
        maxQty: Long,
        inputHint: String,
        presetTiers: List<String>,
        sortOrder: Long,
        active: Boolean,
        actorName: String
    ): Result<Unit> {
        var path = "servicePlans"
        return try {
            requireUid()
            val cleanName = name.trim()
            require(categoryId.isNotBlank() && fileId.isNotBlank()) { "Choose a Category and File first." }
            require(cleanName.length in 2..120) { "Plan name must be 2-120 characters." }
            val safeMin = minQty.coerceIn(1L, 10_000_000L)
            val safeMax = maxQty.coerceIn(safeMin, 100_000_000L)
            val safePrice = pricePer1kPaise.coerceIn(1L, 100_000_000L)
            val cleanTiers = presetTiers
                .map { it.trim() }
                .filter { it.matches(Regex("^\\d+:\\d{1,2}$")) }
                .take(12)
                .ifEmpty { listOf("100:0", "500:0", "1000:0", "2000:5", "5000:6", "10000:9") }

            val fields = mutableMapOf<String, Any>(
                "categoryId" to categoryId.trim().take(80),
                "categoryName" to categoryName.trim().take(80),
                "fileId" to fileId.trim().take(80),
                "fileName" to fileName.trim().take(100),
                "name" to cleanName,
                "description" to description.trim().take(300),
                "photoUrl" to photoUrl.trim().take(2048),
                "pricePer1kPaise" to safePrice,
                "minQty" to safeMin,
                "maxQty" to safeMax,
                "inputHint" to inputHint.trim().ifBlank { "Enter target link or username" }.take(120),
                "presetTiers" to cleanTiers,
                "sortOrder" to sortOrder.coerceIn(0L, 500L),
                "active" to active,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (existingId.isNullOrBlank()) {
                val ref = db.collection("servicePlans").document()
                path = ref.path
                fields["createdAt"] = FieldValue.serverTimestamp()
                ref.set(fields).await()
                recordAudit(actorName, "CREATE_PLAN", path, "Created plan '$cleanName' (${formatRatePer1k(safePrice)})")
            } else {
                val ref = db.collection("servicePlans").document(existingId)
                path = ref.path
                ref.update(fields).await()
                recordAudit(actorName, "UPDATE_PLAN", path, "Updated plan '$cleanName' (${formatRatePer1k(safePrice)})")
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteServicePlan(planId: String, planName: String, actorName: String): Result<Unit> {
        val path = "servicePlans/$planId"
        return try {
            requireUid()
            db.collection("servicePlans").document(planId).delete().await()
            recordAudit(actorName, "DELETE_PLAN", path, "Deleted plan '$planName'")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun seedSocialStarterCatalog(
        existingCategoryIds: Set<String>,
        existingFileIds: Set<String>,
        existingPlanIds: Set<String>,
        actorName: String
    ): Result<Int> {
        return try {
            requireUid()
            val batch = db.batch()
            var added = 0

            val starterCategories = listOf(
                Triple("instagram", "Instagram", "📸") to "Followers, Reel Views, Likes & Reach",
                Triple("facebook", "Facebook", "📘") to "Page Followers, Likes & Video Views",
                Triple("telegram", "Telegram", "✈️") to "Channel Members & Post Views",
                Triple("youtube", "YouTube", "▶️") to "Views, Subscribers & Watch Hours"
            )
            starterCategories.forEachIndexed { idx, (meta, sub) ->
                val (id, name, emoji) = meta
                if (id !in existingCategoryIds) {
                    batch.set(
                        db.collection("catalogCategories").document(id),
                        mapOf(
                            "name" to name,
                            "subtitle" to sub,
                            "description" to "Custom $name growth services controlled from Admin Panel",
                            "emoji" to emoji,
                            "photoUrl" to "",
                            "sortOrder" to (idx + 1).toLong(),
                            "active" to true,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    added++
                }
            }

            data class StarterFile(val id: String, val catId: String, val catName: String, val name: String, val sub: String, val emoji: String, val order: Long)
            val starterFiles = listOf(
                StarterFile("ig_followers", "instagram", "Instagram", "Instagram Followers", "High-quality & guaranteed profiles", "👥", 1L),
                StarterFile("ig_reel_views", "instagram", "Instagram", "Instagram Reel Views", "Superfast reel & dashboard views", "🎬", 2L),
                StarterFile("ig_likes", "instagram", "Instagram", "Instagram Likes", "Instant real-looking post & reel likes", "❤️", 3L),
                StarterFile("ig_reach", "instagram", "Instagram", "Instagram Reach & Impressions", "Boost explore & reel reach", "🚀", 4L),
                StarterFile("ig_story_views", "instagram", "Instagram", "Instagram Story Views", "Fast active story viewers", "👁️", 5L),
                StarterFile("ig_comments", "instagram", "Instagram", "Instagram Comments", "Custom & emoji comments", "💬", 6L),
                StarterFile("fb_followers", "facebook", "Facebook", "Facebook Page Followers", "Page & profile followers", "👥", 1L),
                StarterFile("tg_members", "telegram", "Telegram", "Telegram Channel Members", "Non-drop channel & group members", "📢", 1L),
                StarterFile("yt_views", "youtube", "YouTube", "YouTube Video & Shorts Views", "High retention views", "▶️", 1L)
            )
            starterFiles.forEach { f ->
                if (f.id !in existingFileIds) {
                    batch.set(
                        db.collection("serviceFiles").document(f.id),
                        mapOf(
                            "categoryId" to f.catId,
                            "categoryName" to f.catName,
                            "name" to f.name,
                            "subtitle" to f.sub,
                            "emoji" to f.emoji,
                            "photoUrl" to "",
                            "sortOrder" to f.order,
                            "active" to true,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    added++
                }
            }

            data class StarterPlan(
                val id: String, val catId: String, val catName: String,
                val fileId: String, val fileName: String, val name: String,
                val desc: String, val pricePaise: Long, val minQty: Long, val maxQty: Long,
                val hint: String, val tiers: List<String>, val order: Long
            )
            val starterPlans = listOf(
                StarterPlan(
                    "plan_ig_reel_superfast", "instagram", "Instagram", "ig_reel_views", "Instagram Reel Views",
                    "IG Reel Views - Superfast ⚡", "Instant start • High speed delivery", 100L, 100L, 100_000L,
                    "Paste your Instagram Reel link", listOf("100:0", "500:0", "1000:0", "2000:5", "5000:6", "10000:9", "20000:10"), 1L
                ),
                StarterPlan(
                    "plan_ig_reel_dashboard", "instagram", "Instagram", "ig_reel_views", "Instagram Reel Views",
                    "IG Reel Views 🚀 : Reel+Dashboard", "Shows in Instagram insights & dashboard", 200L, 100L, 100_000L,
                    "Paste your Instagram Reel link", listOf("100:0", "500:0", "1000:0", "2000:5", "5000:6", "10000:9"), 2L
                ),
                StarterPlan(
                    "plan_ig_reel_cheapest", "instagram", "Instagram", "ig_reel_views", "Instagram Reel Views",
                    "IG Reel Views Cheapest", "Budget-friendly fast views", 80L, 100L, 100_000L,
                    "Paste your Instagram Reel link", listOf("500:0", "1000:0", "2000:5", "5000:6", "10000:9"), 3L
                ),
                StarterPlan(
                    "plan_ig_followers_hq", "instagram", "Instagram", "ig_followers", "Instagram Followers",
                    "IG Followers HQ • Non-Drop", "30-day refill guarantee • Real look", 20_000L, 50L, 10_000L,
                    "Enter Instagram Username or Profile Link", listOf("100:0", "500:0", "1000:0", "2000:5", "5000:6", "10000:9"), 1L
                ),
                StarterPlan(
                    "plan_ig_likes_fast", "instagram", "Instagram", "ig_likes", "Instagram Likes",
                    "IG Likes - Superfast ❤️", "Instant delivery on posts & reels", 2_500L, 50L, 50_000L,
                    "Paste your Instagram Post/Reel link", listOf("100:0", "500:0", "1000:0", "2000:5", "5000:8"), 1L
                ),
                StarterPlan(
                    "plan_ig_reach_boost", "instagram", "Instagram", "ig_reach", "Instagram Reach & Impressions",
                    "IG Reach + Impressions 🚀", "Boost explore reach & profile visits", 1_500L, 100L, 50_000L,
                    "Paste your Instagram Post/Reel link", listOf("500:0", "1000:0", "2000:5", "5000:8", "10000:10"), 1L
                ),
                StarterPlan(
                    "plan_yt_views_hq", "youtube", "YouTube", "yt_views", "YouTube Video & Shorts Views",
                    "YouTube Views • High Retention", "Safe organic-style delivery", 15_000L, 100L, 50_000L,
                    "Paste your YouTube Video or Shorts link", listOf("100:0", "500:0", "1000:0", "2000:5", "5000:8"), 1L
                )
            )
            starterPlans.forEach { p ->
                if (p.id !in existingPlanIds) {
                    batch.set(
                        db.collection("servicePlans").document(p.id),
                        mapOf(
                            "categoryId" to p.catId,
                            "categoryName" to p.catName,
                            "fileId" to p.fileId,
                            "fileName" to p.fileName,
                            "name" to p.name,
                            "description" to p.desc,
                            "photoUrl" to "",
                            "pricePer1kPaise" to p.pricePaise,
                            "minQty" to p.minQty,
                            "maxQty" to p.maxQty,
                            "inputHint" to p.hint,
                            "presetTiers" to p.tiers,
                            "sortOrder" to p.order,
                            "active" to true,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    added++
                }
            }

            if (added > 0) {
                batch.commit().await()
                recordAudit(actorName, "SEED_SOCIAL_CATALOG", "catalogCategories", "Seeded $added social categories, files & plans")
            }
            Result.success(added)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logAdminFirestoreError(e, AdminOpType.WRITE, "catalogCategories")
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

    // --- GLOBAL APP CONFIG & UPI PAYMENT SETTINGS ---

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
                    "upiId" to config.upiId.trim().take(100),
                    "upiMerchantName" to config.upiMerchantName.trim().ifBlank { "Fame Maker" }.take(100),
                    "paymentQrUrl" to config.paymentQrUrl.trim().take(2048),
                    "paymentInstructions" to config.paymentInstructions.trim().take(500),
                    "minDepositInr" to config.minDepositInr.coerceIn(1L, 100_000L),
                    "coinRatePerInr" to config.coinRatePerInr.coerceIn(1L, 10_000L),
                    "supportLink" to config.supportLink.trim().take(300),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            recordAudit(
                actorName,
                "UPDATE_CONFIG",
                path,
                "Updated live config (UPI=${config.upiId.ifBlank { "unset" }}, maintenance=${config.maintenanceMode})"
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
