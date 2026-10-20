package com.example.data

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.model.UserProfile
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manages Firebase Authentication (Email/Password & Google Sign-In)
 * and User Channel Profile creation/syncing in Cloud Firestore.
 */
object AuthManager {

    private const val TAG = "AuthManager"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseAuth initialization error", e)
            null
        }

    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseFirestore initialization error", e)
            null
        }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        try {
            val a = auth
            if (a != null) {
                _currentUser.value = a.currentUser
                a.addAuthStateListener { firebaseAuth ->
                    val user = firebaseAuth.currentUser
                    _currentUser.value = user
                    if (user != null) {
                        scope.launch {
                            loadOrInitProfile(user)
                        }
                    } else {
                        _userProfile.value = null
                    }
                }

                // Initial fetch if already logged in
                a.currentUser?.let { user ->
                    scope.launch {
                        loadOrInitProfile(user)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Auth listener error: ${e.message}")
        }
    }

    /**
     * Sign In with Email and Password
     */
    suspend fun signInWithEmail(email: String, pass: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        val a = auth ?: return@withContext Result.failure(Exception("Firebase Auth not initialized"))
        try {
            _isLoading.value = true
            val authResult = a.signInWithEmailAndPassword(email.trim(), pass).awaitTask()
            val user = authResult.user ?: throw Exception("User is null after sign in")
            val profile = loadOrInitProfile(user)
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "signInWithEmail error", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Register new user with Email, Password and create their initial Channel Name
     */
    suspend fun registerWithEmail(
        email: String,
        pass: String,
        channelName: String,
        channelHandle: String
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        val a = auth ?: return@withContext Result.failure(Exception("Firebase Auth not initialized"))
        try {
            _isLoading.value = true
            val authResult = a.createUserWithEmailAndPassword(email.trim(), pass).awaitTask()
            val user = authResult.user ?: throw Exception("User is null after registration")

            val cleanHandle = if (channelHandle.startsWith("@")) channelHandle.trim() else "@${channelHandle.trim().ifBlank { user.email?.substringBefore("@") ?: "creator" }}"
            val cleanChannelName = channelName.trim().ifBlank { "My Shotblast Channel" }

            val profile = UserProfile(
                uid = user.uid,
                email = user.email ?: email,
                displayName = cleanChannelName,
                channelName = cleanChannelName,
                channelHandle = cleanHandle,
                bio = "Welcome to my official Shotblast channel!",
                subscribersCount = 1L,
                isVerified = false,
                isChannelCreated = true
            )

            saveProfileToFirestore(profile)
            _userProfile.value = profile
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "registerWithEmail error", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Updates or creates the user's channel details
     */
    suspend fun updateChannel(
        channelName: String,
        channelHandle: String,
        bio: String
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("Not logged in"))
        val current = _userProfile.value ?: UserProfile(uid = user.uid, email = user.email ?: "")

        val cleanHandle = if (channelHandle.startsWith("@")) channelHandle.trim() else "@${channelHandle.trim()}"
        val updated = current.copy(
            channelName = channelName.trim().ifBlank { current.channelName.ifBlank { "My Channel" } },
            channelHandle = cleanHandle.ifBlank { current.channelHandle.ifBlank { "@${user.email?.substringBefore("@") ?: "creator"}" } },
            bio = bio.trim(),
            isChannelCreated = true
        )

        try {
            saveProfileToFirestore(updated)
            _userProfile.value = updated
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign In with Google via Credential Manager
     */
    suspend fun signInWithGoogle(
        context: Context,
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.Main) {
        val a = auth
        if (a == null) {
            onError("Firebase Auth is not available")
            return@withContext
        }

        val credentialManager = CredentialManager.create(context)
        val clientId = try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else null
        } catch (e: Exception) {
            null
        }

        if (clientId.isNullOrBlank()) {
            onError("Google Web Client ID is not configured yet in Firebase Console. Please use Email Sign-In or add SHA-1/OAuth client.")
            return@withContext
        }

        try {
            _isLoading.value = true
            val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = a.signInWithCredential(authCredential).awaitTask()
                val user = authResult.user
                if (user != null) {
                    val profile = withContext(Dispatchers.IO) { loadOrInitProfile(user) }
                    onSuccess(profile)
                } else {
                    onError("Failed to obtain Firebase user")
                }
            } else {
                onError("Unexpected credential format")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Google Sign-In cancelled by user")
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            onError(e.localizedMessage ?: "Google Sign-In failed")
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Sign out user and clear state
     */
    fun signOut(context: Context? = null) {
        try {
            auth?.signOut()
            _currentUser.value = null
            _userProfile.value = null
            if (context != null) {
                scope.launch {
                    try {
                        val cm = CredentialManager.create(context)
                        cm.clearCredentialState(ClearCredentialStateRequest())
                    } catch (e: Exception) {
                        Log.w(TAG, "clearCredentialState error: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "signOut error", e)
        }
    }

    private suspend fun loadOrInitProfile(user: FirebaseUser): UserProfile {
        val db = firestore
        if (db != null) {
            try {
                val doc = db.collection("users").document(user.uid).get().awaitTask()
                if (doc.exists()) {
                    val profile = UserProfile(
                        uid = user.uid,
                        email = doc.getString("email") ?: user.email ?: "",
                        displayName = doc.getString("displayName") ?: user.displayName ?: "Creator",
                        channelName = doc.getString("channelName") ?: user.displayName ?: "Shotblast Creator",
                        channelHandle = doc.getString("channelHandle") ?: "@${user.email?.substringBefore("@") ?: "creator"}",
                        bio = doc.getString("bio") ?: "Shotblast Creator",
                        subscribersCount = doc.getLong("subscribersCount") ?: 1L,
                        isVerified = doc.getBoolean("isVerified") ?: false,
                        isChannelCreated = doc.getBoolean("isChannelCreated") ?: true
                    )
                    _userProfile.value = profile
                    return profile
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error fetching user doc: ${e.message}")
            }
        }

        // Default profile if document doesn't exist yet
        val defaultName = user.displayName?.ifBlank { null } ?: (user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Shotblast Creator")
        val defaultHandle = "@${user.email?.substringBefore("@") ?: "creator"}"
        val initialProfile = UserProfile(
            uid = user.uid,
            email = user.email ?: "",
            displayName = defaultName,
            channelName = defaultName,
            channelHandle = defaultHandle,
            bio = "Official Shotblast Channel",
            subscribersCount = 1L,
            isVerified = false,
            isChannelCreated = false
        )

        try {
            saveProfileToFirestore(initialProfile)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to write initial profile: ${e.message}")
        }

        _userProfile.value = initialProfile
        return initialProfile
    }

    private suspend fun saveProfileToFirestore(profile: UserProfile) {
        val db = firestore ?: return
        val data = hashMapOf(
            "uid" to profile.uid,
            "email" to profile.email,
            "displayName" to profile.displayName,
            "channelName" to profile.channelName,
            "channelHandle" to profile.channelHandle,
            "bio" to profile.bio,
            "subscribersCount" to profile.subscribersCount,
            "isVerified" to profile.isVerified,
            "isChannelCreated" to profile.isChannelCreated,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(profile.uid).set(data, SetOptions.merge()).awaitTask()
    }
}
