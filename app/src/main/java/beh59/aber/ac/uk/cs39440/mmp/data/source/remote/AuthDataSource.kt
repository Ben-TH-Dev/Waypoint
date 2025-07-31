package beh59.aber.ac.uk.cs39440.mmp.data.source.remote

import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * AuthDataSource
 * Handles direct communication with Firebase Authentication
 * Provides methods for the sign-in and authentication process of the application
 * @param firebaseAuth A Firebase Authentication instance
 */
class AuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    /**
     * signInWithGoogle
     * Authenticates a user with Google by exchanging their ID token for an authenticated
     * FirebaseUser
     * @param idToken The ID token obtained from Google Sign-In
     * @return A Result containing the FirebaseUser if successful, or an error if the sign-in fails
     */
    @AddTrace(name = "authDSSignIn")
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            val result: AuthResult = firebaseAuth.signInWithCredential(credential).await()

            val user = result.user

            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("AuthFlow: User not found after successful authentication"))
            }
        } catch (e: Exception) {
            Log.e("AuthDataSource", "AuthFlow: Authentication failed", e)
            Result.failure(e)
        }
    }

    /**
     * signOut
     * Signs out the currently authenticated user from Firebase
     */
    fun signOut() {
        Log.d("AuthDataSource", "signOut: Signing out current user")
        firebaseAuth.signOut()
    }
} 