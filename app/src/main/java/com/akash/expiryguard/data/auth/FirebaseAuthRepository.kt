package com.akash.expiryguard.data.auth

import com.akash.expiryguard.util.awaitResult
import com.akash.expiryguard.util.awaitVoid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun isSignedIn(): Boolean = auth.currentUser?.isAnonymous == false

    fun signOutAnonymousUserIfPresent() {
        if (auth.currentUser?.isAnonymous == true) auth.signOut()
    }

    suspend fun signUp(username: String, password: String) {
        val normalizedUsername = validateUsername(username)
        validatePassword(password)
        val result = auth.createUserWithEmailAndPassword(usernameEmail(normalizedUsername), password).awaitResult()
        val user = result.user ?: error("Sign-up did not return a user.")
        user.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(normalizedUsername).build()
        ).awaitVoid()
        try {
            firestore.collection("users").document(user.uid)
                .set(
                    mapOf(
                        "username" to normalizedUsername,
                        "createdAt" to System.currentTimeMillis(),
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .awaitVoid()
        } catch (_: FirebaseFirestoreException) {
            // Some existing rules only allow the item subcollection. Firebase Auth still
            // securely owns the account credentials and display name in that configuration.
        }
    }

    suspend fun logIn(username: String, password: String) {
        val normalizedUsername = validateUsername(username)
        if (password.isBlank()) throw IllegalArgumentException("Password is required.")
        auth.signInWithEmailAndPassword(usernameEmail(normalizedUsername), password).awaitResult()
    }

    private fun validateUsername(username: String): String {
        val normalized = username.trim().lowercase(Locale.US)
        require(USERNAME_REGEX.matches(normalized)) {
            "Use 3-30 letters, numbers, dots, underscores, or hyphens."
        }
        return normalized
    }

    private fun validatePassword(password: String) {
        require(password.length >= 8) { "Password must be at least 8 characters." }
    }

    private fun usernameEmail(username: String): String = "$username@expiryguard.local"

    private companion object {
        val USERNAME_REGEX = Regex("^[a-z0-9._-]{3,30}$")
    }
}
