package com.example.data

import android.content.Context
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreatorBoostRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun todayIso(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun generateReferralCode(uid: String): String {
        val clean = uid.filter { it.isLetterOrDigit() }.uppercase(Locale.US)
        val suffix = clean.take(6).padEnd(6, 'X')
        return "CB$suffix"
    }

    fun isCurrentUserAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        return user.email.equals("buildolive4@gmail.com", ignoreCase = true) && user.isEmailVerified
    }

    // --- REALTIME OBSERVATION STREAMS ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(ex, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeOrders(userId: String): Flow<List<OrderRecord>> = flow {
        val path = "users/$userId/orders"
        emitAll(
            db.collection("users").document(userId).collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap -> snap.toObjects(OrderRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeTransactions(userId: String): Flow<List<WalletTransaction>> = flow {
        val path = "users/$userId/transactions"
        emitAll(
            db.collection("users").document(userId).collection("transactions")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap -> snap.toObjects(WalletTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeFavorites(userId: String): Flow<List<FavoritePack>> = flow {
        val path = "users/$userId/favorites"
        emitAll(
            db.collection("users").document(userId).collection("favorites")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap -> snap.toObjects(FavoritePack::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeNotifications(userId: String): Flow<List<AppNotification>> = flow {
        val path = "users/$userId/notifications"
        emitAll(
            db.collection("users").document(userId).collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap -> snap.toObjects(AppNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeSupportTickets(userId: String): Flow<List<SupportTicket>> = flow {
        val path = "users/$userId/supportTickets"
        emitAll(
            db.collection("users").document(userId).collection("supportTickets")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap -> snap.toObjects(SupportTicket::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observePublishedCatalogServices(): Flow<List<CatalogService>> = flow {
        val uid = requireUserId()
        val path = "catalogServices"
        emitAll(
            db.collection("catalogServices")
                .whereEqualTo("published", true)
                .snapshots()
                .map { snap -> snap.toObjects(CatalogService::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    emit(emptyList())
                }
        )
    }

    fun observeActiveCatalogCategories(): Flow<List<CatalogCategory>> = flow {
        val uid = requireUserId()
        val path = "catalogCategories"
        emitAll(
            db.collection("catalogCategories")
                .whereEqualTo("active", true)
                .snapshots()
                .map { snap ->
                    snap.toObjects(CatalogCategory::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.sortOrder }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    emit(emptyList())
                }
        )
    }

    fun observeActiveCoinPackages(): Flow<List<CoinPackageItem>> = flow {
        val uid = requireUserId()
        val path = "coinPackages"
        emitAll(
            db.collection("coinPackages")
                .whereEqualTo("active", true)
                .snapshots()
                .map { snap -> snap.toObjects(CoinPackageItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    emit(emptyList())
                }
        )
    }

    fun observeAppConfig(): Flow<AppConfig> = flow {
        val uid = requireUserId()
        val path = "appConfig/global"
        emitAll(
            db.collection("appConfig").document("global")
                .snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(AppConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: AppConfig()
                    } else AppConfig()
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.GET, path)
                    }
                    emit(AppConfig())
                }
        )
    }

    fun observeActiveServiceFiles(): Flow<List<ServiceFileItem>> = flow {
        val uid = requireUserId()
        val path = "serviceFiles"
        emitAll(
            db.collection("serviceFiles")
                .whereEqualTo("active", true)
                .snapshots()
                .map { snap ->
                    snap.toObjects(ServiceFileItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.sortOrder }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    emit(emptyList())
                }
        )
    }

    fun observeActiveServicePlans(): Flow<List<ServicePlanItem>> = flow {
        val uid = requireUserId()
        val path = "servicePlans"
        emitAll(
            db.collection("servicePlans")
                .whereEqualTo("active", true)
                .snapshots()
                .map { snap ->
                    snap.toObjects(ServicePlanItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.sortOrder }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    emit(emptyList())
                }
        )
    }

    fun observeMyServiceOrders(userId: String): Flow<List<ServiceOrderRecord>> = flow {
        val path = "serviceOrders"
        emitAll(
            db.collection("serviceOrders")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(ServiceOrderRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: Long.MAX_VALUE }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    emit(emptyList())
                }
        )
    }

    fun observeMyFundRequests(userId: String): Flow<List<FundRequestRecord>> = flow {
        val path = "fundRequests"
        emitAll(
            db.collection("fundRequests")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(FundRequestRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: Long.MAX_VALUE }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    emit(emptyList())
                }
        )
    }

    // --- CRASH-SAFE MUTATIONS (Result<T>) ---

    suspend fun ensureUserProfileInitialized(): Result<Unit> {
        var docPath = "users/unauthenticated"
        return try {
            val user = auth.currentUser ?: error("User not signed in")
            val uid = user.uid
            docPath = "users/$uid"
            val userRef = db.collection("users").document(uid)
            val snap = userRef.get().await()
            if (!snap.exists()) {
                val safeName = (user.displayName?.trim()?.take(80)).takeUnless { it.isNullOrBlank() } ?: "Creator"
                val safeEmail = (user.email?.trim()?.take(120)).orEmpty()
                val welcomeCoins = try {
                    val cfgSnap = db.collection("appConfig").document("global").get().await()
                    cfgSnap.getLong("welcomeCoins")?.coerceIn(0L, 100000L) ?: 15L
                } catch (_: Exception) {
                    15L
                }
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "email" to safeEmail,
                    "country" to "India",
                    "coins" to welcomeCoins,
                    "walletBalancePaise" to 0L,
                    "points" to 0L,
                    "level" to 1L,
                    "referralCode" to generateReferralCode(uid),
                    "dailyStreak" to 0L,
                    "spinsToday" to 0L,
                    "mascotEnabled" to true,
                    "darkMode" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                val batch = db.batch()
                batch.set(userRef, payload)
                val txRef = userRef.collection("transactions").document()
                batch.set(
                    txRef,
                    mapOf(
                        "userId" to uid,
                        "txType" to "BONUS",
                        "title" to "Welcome Bonus",
                        "description" to "Starter gift coins to try your first packs",
                        "coinDelta" to welcomeCoins,
                        "pointsDelta" to 0L,
                        "priceInr" to 0L,
                        "status" to "SUCCESSFUL",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                val notifRef = userRef.collection("notifications").document()
                batch.set(
                    notifRef,
                    mapOf(
                        "userId" to uid,
                        "title" to "Welcome to Fame Maker!",
                        "body" to "You received $welcomeCoins welcome bonus coins. Unlock your first pack today!",
                        "category" to "REWARD",
                        "read" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun purchaseCoinPackage(profile: UserProfile, pkg: CoinPackageItem): Result<Long> {
        var path = "users/${profile.userId}"
        return try {
            val uid = requireUserId()
            check(uid == profile.userId) { "User mismatch" }
            path = "users/$uid"
            val totalCoinsAdded = pkg.coins + pkg.bonusCoins
            val newCoins = profile.coins + totalCoinsAdded
            val newPoints = profile.points + pkg.points
            val newLevel = DefaultCatalog.calculateLevel(newPoints)

            val userRef = db.collection("users").document(uid)
            val txRef = userRef.collection("transactions").document()
            val notifRef = userRef.collection("notifications").document()

            val batch = db.batch()
            batch.update(
                userRef,
                mapOf(
                    "coins" to newCoins,
                    "points" to newPoints,
                    "level" to newLevel,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val bonusPart = if (pkg.bonusCoins > 0) " (+${pkg.bonusCoins} bonus)" else ""
            batch.set(
                txRef,
                mapOf(
                    "userId" to uid,
                    "txType" to "PURCHASE",
                    "title" to "${pkg.coins} Coins Pack",
                    "description" to "Added $totalCoinsAdded coins$bonusPart & +${pkg.points} pts",
                    "coinDelta" to totalCoinsAdded,
                    "pointsDelta" to pkg.points,
                    "priceInr" to pkg.priceInr,
                    "status" to "SUCCESSFUL",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                notifRef,
                mapOf(
                    "userId" to uid,
                    "title" to "Coins Added!",
                    "body" to "$totalCoinsAdded coins and +${pkg.points} reward points were added to your wallet.",
                    "category" to "PURCHASE",
                    "read" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(totalCoinsAdded)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun unlockServicePack(
        profile: UserProfile,
        service: CatalogService,
        contentLines: List<String> = service.contentLines
    ): Result<OrderRecord> {
        var path = "users/${profile.userId}/orders"
        return try {
            val uid = requireUserId()
            check(uid == profile.userId) { "User mismatch" }
            if (profile.coins < service.coinPrice) {
                return Result.failure(IllegalStateException("INSUFFICIENT_COINS"))
            }
            val newCoins = profile.coins - service.coinPrice
            val earnedPoints = 1L
            val newPoints = profile.points + earnedPoints
            val newLevel = DefaultCatalog.calculateLevel(newPoints)

            val userRef = db.collection("users").document(uid)
            val orderRef = userRef.collection("orders").document()
            val txRef = userRef.collection("transactions").document()
            val notifRef = userRef.collection("notifications").document()
            path = orderRef.path

            val safeLines = contentLines.take(60)
            val batch = db.batch()
            batch.update(
                userRef,
                mapOf(
                    "coins" to newCoins,
                    "points" to newPoints,
                    "level" to newLevel,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                orderRef,
                mapOf(
                    "userId" to uid,
                    "serviceId" to service.id.take(80),
                    "serviceName" to service.name.take(120),
                    "categoryId" to service.categoryId.take(60),
                    "categoryName" to service.categoryName.take(60),
                    "itemCount" to safeLines.size.coerceAtLeast(1).toLong(),
                    "coinPrice" to service.coinPrice,
                    "status" to "COMPLETED",
                    "contentLines" to safeLines,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                txRef,
                mapOf(
                    "userId" to uid,
                    "txType" to "SPEND",
                    "title" to "Unlocked ${service.name.take(80)}",
                    "description" to "${service.categoryName} pack (${safeLines.size} items)",
                    "coinDelta" to -service.coinPrice,
                    "pointsDelta" to earnedPoints,
                    "priceInr" to 0L,
                    "status" to "SUCCESSFUL",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                notifRef,
                mapOf(
                    "userId" to uid,
                    "title" to "Pack Unlocked!",
                    "body" to "${service.name} is ready in My Orders. Copy anytime!",
                    "category" to "ORDER",
                    "read" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(
                OrderRecord(
                    id = orderRef.id,
                    userId = uid,
                    serviceId = service.id,
                    serviceName = service.name,
                    categoryId = service.categoryId,
                    categoryName = service.categoryName,
                    itemCount = safeLines.size.toLong(),
                    coinPrice = service.coinPrice,
                    status = "COMPLETED",
                    contentLines = safeLines
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateOrderContent(orderId: String, newLines: List<String>): Result<Unit> {
        var path = "users/unauthenticated/orders/$orderId"
        return try {
            val uid = requireUserId()
            require(orderId.isNotBlank())
            val orderRef = db.collection("users").document(uid).collection("orders").document(orderId)
            path = orderRef.path
            orderRef.update(
                mapOf(
                    "contentLines" to newLines.take(60),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun claimDailyReward(profile: UserProfile): Result<DailyRewardDay> {
        var path = "users/${profile.userId}"
        return try {
            val uid = requireUserId()
            check(uid == profile.userId)
            val today = todayIso()
            if (profile.lastDailyClaimDay == today) {
                return Result.failure(IllegalStateException("Today's daily reward has already been claimed!"))
            }
            val nextDay = ((profile.dailyStreak % 7L) + 1L).toInt()
            val reward = DefaultCatalog.dailyRewards.first { it.day == nextDay }
            val newCoins = profile.coins + reward.coins
            val newPoints = profile.points + reward.points
            val newLevel = DefaultCatalog.calculateLevel(newPoints)

            val userRef = db.collection("users").document(uid)
            val txRef = userRef.collection("transactions").document()
            val notifRef = userRef.collection("notifications").document()
            path = userRef.path

            val batch = db.batch()
            batch.update(
                userRef,
                mapOf(
                    "coins" to newCoins,
                    "points" to newPoints,
                    "level" to newLevel,
                    "dailyStreak" to nextDay.toLong(),
                    "lastDailyClaimDay" to today,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                txRef,
                mapOf(
                    "userId" to uid,
                    "txType" to "DAILY_REWARD",
                    "title" to "Day $nextDay Check-in Reward",
                    "description" to "Claimed +${reward.coins} coins & +${reward.points} pts",
                    "coinDelta" to reward.coins,
                    "pointsDelta" to reward.points,
                    "priceInr" to 0L,
                    "status" to "SUCCESSFUL",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                notifRef,
                mapOf(
                    "userId" to uid,
                    "title" to "Day $nextDay Streak Claimed!",
                    "body" to "You received ${reward.coins} coins and +${reward.points} points.",
                    "category" to "REWARD",
                    "read" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(reward)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun recordSpinResult(profile: UserProfile, segment: SpinSegment): Result<Unit> {
        var path = "users/${profile.userId}"
        return try {
            val uid = requireUserId()
            check(uid == profile.userId)
            val today = todayIso()
            val currentSpins = if (profile.lastSpinDay == today) profile.spinsToday else 0L
            if (currentSpins >= 3L) {
                return Result.failure(IllegalStateException("Daily spin limit (3/3) reached. Come back tomorrow!"))
            }
            val newCoins = profile.coins + segment.coins
            val newPoints = profile.points + segment.points
            val newLevel = DefaultCatalog.calculateLevel(newPoints)

            val userRef = db.collection("users").document(uid)
            path = userRef.path
            val batch = db.batch()
            batch.update(
                userRef,
                mapOf(
                    "coins" to newCoins,
                    "points" to newPoints,
                    "level" to newLevel,
                    "lastSpinDay" to today,
                    "spinsToday" to (currentSpins + 1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            if (segment.coins > 0L || segment.points > 0L) {
                val txRef = userRef.collection("transactions").document()
                batch.set(
                    txRef,
                    mapOf(
                        "userId" to uid,
                        "txType" to "SPIN_REWARD",
                        "title" to "Spin & Win: ${segment.label}",
                        "description" to "Lucky wheel prize",
                        "coinDelta" to segment.coins,
                        "pointsDelta" to segment.points,
                        "priceInr" to 0L,
                        "status" to "SUCCESSFUL",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun redeemReferralCode(
        profile: UserProfile,
        rawCode: String,
        configuredBonusCoins: Long = 25L
    ): Result<Long> {
        var path = "users/${profile.userId}"
        return try {
            val uid = requireUserId()
            check(uid == profile.userId)
            val code = rawCode.trim().uppercase(Locale.US)
            if (code.length < 4 || code.length > 20) {
                return Result.failure(IllegalArgumentException("Enter a valid referral code (4-20 characters)."))
            }
            if (code == profile.referralCode) {
                return Result.failure(IllegalArgumentException("You cannot redeem your own referral code."))
            }
            if (profile.referredBy.isNotBlank()) {
                return Result.failure(IllegalStateException("Referral bonus already claimed on this account."))
            }

            val bonusCoins = configuredBonusCoins.coerceIn(0L, 100000L)
            val bonusPoints = 5L
            val userRef = db.collection("users").document(uid)
            val txRef = userRef.collection("transactions").document()
            path = userRef.path

            // First update referredBy (profile action), then credit wallet (wallet action) cleanly
            userRef.update(
                mapOf(
                    "referredBy" to code,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val batch = db.batch()
            val newPoints = profile.points + bonusPoints
            batch.update(
                userRef,
                mapOf(
                    "coins" to (profile.coins + bonusCoins),
                    "points" to newPoints,
                    "level" to DefaultCatalog.calculateLevel(newPoints),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                txRef,
                mapOf(
                    "userId" to uid,
                    "txType" to "REFERRAL",
                    "title" to "Referral Bonus ($code)",
                    "description" to "Redeemed friend invite code",
                    "coinDelta" to bonusCoins,
                    "pointsDelta" to bonusPoints,
                    "priceInr" to 0L,
                    "status" to "SUCCESSFUL",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(bonusCoins)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun saveFavorite(
        serviceId: String,
        serviceName: String,
        categoryName: String,
        contentText: String
    ): Result<Unit> {
        var path = "users/unauthenticated/favorites"
        return try {
            val uid = requireUserId()
            val favRef = db.collection("users").document(uid).collection("favorites").document()
            path = favRef.path
            favRef.set(
                mapOf(
                    "userId" to uid,
                    "serviceId" to serviceId.take(80),
                    "serviceName" to serviceName.take(120),
                    "categoryName" to categoryName.take(60),
                    "contentText" to contentText.take(4000),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun removeFavorite(favId: String): Result<Unit> {
        var path = "users/unauthenticated/favorites/$favId"
        return try {
            val uid = requireUserId()
            val favRef = db.collection("users").document(uid).collection("favorites").document(favId)
            path = favRef.path
            favRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun markNotificationRead(notifId: String): Result<Unit> {
        var path = "users/unauthenticated/notifications/$notifId"
        return try {
            val uid = requireUserId()
            val ref = db.collection("users").document(uid).collection("notifications").document(notifId)
            path = ref.path
            ref.update(mapOf("read" to true)).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun markAllNotificationsRead(notifications: List<AppNotification>): Result<Unit> {
        var path = "users/unauthenticated/notifications"
        return try {
            val uid = requireUserId()
            val unread = notifications.filter { !it.read && it.id.isNotBlank() }
            if (unread.isEmpty()) return Result.success(Unit)
            path = "users/$uid/notifications"
            val batch = db.batch()
            unread.forEach { n ->
                val ref = db.collection("users").document(uid).collection("notifications").document(n.id)
                batch.update(ref, mapOf("read" to true))
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteNotification(notifId: String): Result<Unit> {
        var path = "users/unauthenticated/notifications/$notifId"
        return try {
            val uid = requireUserId()
            val ref = db.collection("users").document(uid).collection("notifications").document(notifId)
            path = ref.path
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun submitSupportTicket(
        subject: String,
        category: String,
        description: String
    ): Result<Unit> {
        var path = "users/unauthenticated/supportTickets"
        return try {
            val uid = requireUserId()
            val cleanSubject = subject.trim()
            val cleanDesc = description.trim()
            if (cleanSubject.length < 3) {
                return Result.failure(IllegalArgumentException("Subject must be at least 3 characters."))
            }
            if (cleanDesc.length < 5) {
                return Result.failure(IllegalArgumentException("Please describe your issue (at least 5 characters)."))
            }
            val ref = db.collection("users").document(uid).collection("supportTickets").document()
            path = ref.path
            ref.set(
                mapOf(
                    "userId" to uid,
                    "subject" to cleanSubject.take(120),
                    "category" to category.take(60),
                    "description" to cleanDesc.take(1000),
                    "status" to "OPEN",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateProfilePreferences(
        displayName: String? = null,
        country: String? = null,
        mascotEnabled: Boolean? = null,
        darkMode: Boolean? = null
    ): Result<Unit> {
        var path = "users/unauthenticated"
        return try {
            val uid = requireUserId()
            val ref = db.collection("users").document(uid)
            path = ref.path
            val updates = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
            if (displayName != null) {
                val clean = displayName.trim()
                require(clean.length in 1..80) { "Name must be 1 to 80 characters." }
                updates["displayName"] = clean
            }
            if (country != null) {
                val clean = country.trim()
                require(clean.length in 2..60) { "Country must be 2 to 60 characters." }
                updates["country"] = clean
            }
            if (mascotEnabled != null) updates["mascotEnabled"] = mascotEnabled
            if (darkMode != null) updates["darkMode"] = darkMode
            ref.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- ADMIN OPERATIONS (Verified Admin Only) ---

    suspend fun adminCreateCatalogService(
        categoryId: String,
        categoryName: String,
        name: String,
        packType: String,
        coinPrice: Long,
        trending: Boolean,
        contentLines: List<String>
    ): Result<Unit> {
        var path = "catalogServices"
        return try {
            requireUserId()
            val cleanName = name.trim()
            val cleanLines = contentLines.map { it.trim() }.filter { it.isNotBlank() }.take(60)
            require(cleanName.length in 2..120) { "Service name must be 2-120 characters." }
            require(cleanLines.isNotEmpty()) { "Add at least one hashtag, caption, or bio line." }
            val ref = db.collection("catalogServices").document()
            path = ref.path
            ref.set(
                mapOf(
                    "categoryId" to categoryId.take(60),
                    "categoryName" to categoryName.take(60),
                    "name" to cleanName,
                    "packType" to packType,
                    "itemCount" to cleanLines.size.toLong(),
                    "coinPrice" to coinPrice.coerceIn(0L, 100000L),
                    "trending" to trending,
                    "published" to true,
                    "contentLines" to cleanLines,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun adminDeleteCatalogService(serviceId: String): Result<Unit> {
        val path = "catalogServices/$serviceId"
        return try {
            requireUserId()
            db.collection("catalogServices").document(serviceId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun adminCreateCoinPackage(
        name: String,
        coins: Long,
        bonusCoins: Long,
        points: Long,
        priceInr: Long,
        badge: String
    ): Result<Unit> {
        var path = "coinPackages"
        return try {
            requireUserId()
            val cleanName = name.trim()
            require(cleanName.length in 2..80) { "Package name must be 2-80 characters." }
            val ref = db.collection("coinPackages").document()
            path = ref.path
            val payload = mutableMapOf<String, Any>(
                "name" to cleanName,
                "coins" to coins.coerceIn(10L, 1000000L),
                "bonusCoins" to bonusCoins.coerceIn(0L, 500000L),
                "points" to points.coerceIn(0L, 10000L),
                "priceInr" to priceInr.coerceIn(1L, 500000L),
                "sortOrder" to 10L,
                "active" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (badge.isNotBlank()) {
                payload["badge"] = badge.trim().take(40)
            }
            ref.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- SERVICE ORDERS & INR FUND REQUESTS ---

    suspend fun placeServiceOrder(
        profile: UserProfile,
        plan: ServicePlanItem,
        targetLink: String,
        quantity: Long,
        totalPricePaise: Long,
        paymentMethod: String,
        utrNumber: String = "",
        paymentProofUrl: String = ""
    ): Result<String> {
        var path = "serviceOrders"
        return try {
            val uid = requireUserId()
            check(uid == profile.userId) { "User mismatch" }
            val cleanLink = targetLink.trim()
            if (cleanLink.length < 2 || cleanLink.length > 400) {
                return Result.failure(IllegalArgumentException("Please enter a valid profile/post link or username."))
            }
            if (quantity < plan.minQty || quantity > plan.maxQty) {
                return Result.failure(
                    IllegalArgumentException("Quantity must be between ${plan.minQty} and ${plan.maxQty}.")
                )
            }
            if (totalPricePaise < 1L) {
                return Result.failure(IllegalArgumentException("Invalid order total."))
            }

            val cleanMethod = if (paymentMethod == "UPI_DIRECT") "UPI_DIRECT" else "WALLET"
            val cleanUtr = utrNumber.trim()
            if (cleanMethod == "WALLET") {
                if (profile.walletBalancePaise < totalPricePaise) {
                    return Result.failure(IllegalStateException("INSUFFICIENT_WALLET_BALANCE"))
                }
            } else {
                if (cleanUtr.length !in 6..40) {
                    return Result.failure(IllegalArgumentException("Enter a valid 6–40 character UTR / Transaction ID."))
                }
            }

            val orderRef = db.collection("serviceOrders").document()
            val userRef = db.collection("users").document(uid)
            val notifRef = userRef.collection("notifications").document()
            path = orderRef.path

            val orderPayload = mutableMapOf<String, Any>(
                "userId" to uid,
                "userName" to profile.displayName.ifBlank { "Creator" }.take(80),
                "categoryId" to plan.categoryId.ifBlank { "general" }.take(80),
                "categoryName" to plan.categoryName.ifBlank { "Service" }.take(80),
                "fileId" to plan.fileId.ifBlank { "general" }.take(80),
                "fileName" to plan.fileName.ifBlank { "Service" }.take(100),
                "planId" to plan.id.ifBlank { "plan" }.take(80),
                "planName" to plan.name.ifBlank { "Service Plan" }.take(120),
                "targetLink" to cleanLink,
                "quantity" to quantity,
                "totalPricePaise" to totalPricePaise,
                "paymentMethod" to cleanMethod,
                "status" to "PENDING",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (profile.email.isNotBlank()) {
                orderPayload["userEmail"] = profile.email.take(120)
            }
            if (cleanUtr.isNotBlank()) {
                orderPayload["utrNumber"] = cleanUtr.take(40)
            }
            val cleanProof = paymentProofUrl.trim()
            if (cleanProof.isNotBlank() && cleanProof.length <= 2048) {
                orderPayload["paymentProofUrl"] = cleanProof
            }

            val batch = db.batch()
            if (cleanMethod == "WALLET") {
                val newWalletPaise = (profile.walletBalancePaise - totalPricePaise).coerceAtLeast(0L)
                // Strictly only walletBalancePaise & updatedAt to satisfy zero-trust spend rule
                batch.update(
                    userRef,
                    mapOf(
                        "walletBalancePaise" to newWalletPaise,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                val txRef = userRef.collection("transactions").document()
                batch.set(
                    txRef,
                    mapOf(
                        "userId" to uid,
                        "txType" to "SERVICE_ORDER",
                        "title" to "Order: ${plan.name.take(90)}",
                        "description" to "${formatShortQty(quantity)} • ${formatInrFromPaise(totalPricePaise)} paid via Wallet",
                        "coinDelta" to 0L,
                        "pointsDelta" to 0L,
                        "priceInr" to (totalPricePaise / 100L).coerceAtLeast(0L),
                        "status" to "SUCCESSFUL",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            batch.set(orderRef, orderPayload)
            batch.set(
                notifRef,
                mapOf(
                    "userId" to uid,
                    "title" to "Service Order Placed!",
                    "body" to "${plan.name} (${formatShortQty(quantity)}) for ${formatInrFromPaise(totalPricePaise)} is now Pending.",
                    "category" to "ORDER",
                    "read" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(orderRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun submitFundRequest(
        profile: UserProfile,
        amountPaise: Long,
        utrNumber: String,
        proofImageUrl: String = ""
    ): Result<String> {
        var path = "fundRequests"
        return try {
            val uid = requireUserId()
            check(uid == profile.userId) { "User mismatch" }
            val cleanUtr = utrNumber.trim()
            if (amountPaise < 100L) {
                return Result.failure(IllegalArgumentException("Minimum deposit is ₹1.00."))
            }
            if (cleanUtr.length !in 6..40) {
                return Result.failure(IllegalArgumentException("Enter a valid 6–40 character UTR / Transaction ID."))
            }
            val reqRef = db.collection("fundRequests").document()
            val notifRef = db.collection("users").document(uid).collection("notifications").document()
            path = reqRef.path

            val payload = mutableMapOf<String, Any>(
                "userId" to uid,
                "userName" to profile.displayName.ifBlank { "Creator" }.take(80),
                "amountPaise" to amountPaise,
                "utrNumber" to cleanUtr,
                "status" to "PENDING",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (profile.email.isNotBlank()) {
                payload["userEmail"] = profile.email.take(120)
            }
            val cleanProof = proofImageUrl.trim()
            if (cleanProof.isNotBlank() && cleanProof.length <= 2048) {
                payload["proofImageUrl"] = cleanProof
            }

            val batch = db.batch()
            batch.set(reqRef, payload)
            batch.set(
                notifRef,
                mapOf(
                    "userId" to uid,
                    "title" to "Deposit Submitted",
                    "body" to "Your ${formatInrFromPaise(amountPaise)} UPI request (UTR: $cleanUtr) is awaiting verification.",
                    "category" to "PURCHASE",
                    "read" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(reqRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }
}
