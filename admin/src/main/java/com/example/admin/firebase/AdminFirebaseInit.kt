package com.example.admin.firebase

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.admin.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "AdminFirebase"

enum class AdminOpType { GET, LIST, CREATE, UPDATE, DELETE, WRITE }

fun ensureAdminFirebaseInitialized(context: Context) {
    val appCtx = context.applicationContext
    if (FirebaseApp.getApps(appCtx).isNotEmpty()) return

    val projectId = appCtx.getString(R.string.firebase_project_id)
    val appId = appCtx.getString(R.string.firebase_app_id)
    val apiKey = appCtx.getString(R.string.firebase_api_key)
    val storageBucket = appCtx.getString(R.string.firebase_storage_bucket)
    val gcmSenderId = appCtx.getString(R.string.firebase_gcm_sender_id)

    if (projectId.isNotBlank() && appId.isNotBlank() && apiKey.isNotBlank()) {
        val builder = FirebaseOptions.Builder()
            .setProjectId(projectId)
            .setApplicationId(appId)
            .setApiKey(apiKey)
        if (storageBucket.isNotBlank()) builder.setStorageBucket(storageBucket)
        if (gcmSenderId.isNotBlank()) builder.setGcmSenderId(gcmSenderId)
        FirebaseApp.initializeApp(appCtx, builder.build())
    } else {
        FirebaseApp.initializeApp(appCtx)
    }
}

fun getAdminFirestore(context: Context): FirebaseFirestore {
    ensureAdminFirebaseInitialized(context)
    val dbId = context.applicationContext.getString(R.string.firestore_database_id)
    return FirebaseFirestore.getInstance(dbId)
}

fun logAdminFirestoreError(error: Exception, op: AdminOpType, path: String) {
    val uid = Firebase.auth.currentUser?.uid ?: "unauthenticated"
    Log.e(TAG, "Admin Firestore $op failed on $path (uid=$uid): ${error.message}", error)
}

fun Context.findAdminActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

fun adminGoogleSignIn(
    context: Context,
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onCancelled: () -> Unit = {}
) {
    val clientId = context.getString(R.string.default_web_client_id)
    if (clientId.isBlank()) {
        onError("Firebase Web Client ID is missing. Make sure GOOGLE_SERVICES_JSON is configured.")
        return
    }
    val activity = context.findAdminActivity()
    if (activity == null) {
        onError("Unable to open sign-in window.")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

    scope.launch {
        try {
            val cred = credentialManager.getCredential(activity, request).credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onSuccess()
            } else {
                onError("Unexpected credential returned.")
            }
        } catch (e: GetCredentialCancellationException) {
            onCancelled()
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            onError(e.localizedMessage ?: "Sign-in failed. Verify SHA-1 & package in Firebase Console.")
        }
    }
}

fun adminSignOut(
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onDone: () -> Unit
) {
    scope.launch {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) {
            } finally {
                Firebase.auth.signOut()
                onDone()
            }
        }
    }
}
