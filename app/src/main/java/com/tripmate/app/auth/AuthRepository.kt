package com.tripmate.app.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.tripmate.app.R
import com.tripmate.app.data.UserProfile
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class AuthRepository(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    fun currentUserProfile(): UserProfile? = auth.currentUser?.toUserProfile()

    suspend fun signInWithGoogle(activity: Activity): UserProfile {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialResult = credentialManager.getCredential(
            context = activity,
            request = request
        )
        val googleCredential = GoogleIdTokenCredential.createFrom(credentialResult.credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
        val authResult = auth.signInWithCredential(firebaseCredential).await()
        val user = requireNotNull(authResult.user) { "Firebase user is missing after Google sign-in." }
        val profile = user.toUserProfile()
        saveUserProfile(profile)
        return profile
    }

    suspend fun saveCurrentUserIfSignedIn(): UserProfile? {
        val profile = currentUserProfile() ?: return null
        saveUserProfile(profile)
        return profile
    }

    fun signOut() {
        auth.signOut()
    }

    private suspend fun saveUserProfile(profile: UserProfile) {
        val docRef = firestore.collection("users").document(profile.id)
        val existing = docRef.get().await()
        val data = mutableMapOf<String, Any?>(
            "id" to profile.id,
            "displayName" to profile.displayName,
            "email" to profile.email,
            "photoUrl" to profile.photoUrl,
            "updatedAt" to Timestamp.now()
        )
        if (!existing.exists()) {
            data["createdAt"] = Timestamp.now()
        }
        docRef.set(data, SetOptions.merge()).await()
    }

    private fun FirebaseUser.toUserProfile(): UserProfile = UserProfile(
        id = uid,
        displayName = displayName.orEmpty(),
        email = email.orEmpty(),
        photoUrl = photoUrl?.toString().orEmpty()
    )
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    addOnCanceledListener { continuation.cancel() }
}
