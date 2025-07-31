package beh59.aber.ac.uk.cs39440.mmp.data.repository

import beh59.aber.ac.uk.cs39440.mmp.data.models.states.AuthState
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

/**
 * IAuthRepository
 * An interface that serves as an abstraction for the methods required for the authentication flow
 * of the application.
 * @property authState A StateFlow holding the state of AuthState that can be observed by the UI
 * when needed
 */
interface IAuthRepository {
    val authState: StateFlow<AuthState>

    /**
     * signInWithGoogle
     * Attempts to exchange an idToken with an authenticated FirebaseUser.
     * @param idToken Created when the user signs in with Google and is exchanged for a FirebaseUser
     * @return Returns a Result that contains a FirebaseUser if successful, and allows us to handle
     * failure gracefully
     */
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser>

    /**
     * updateFirebaseUser
     * Responsible for updating the state with a new FirebaseUser object safely
     * @param user The new FirebaseUser object to store in the auth state
     */
    fun updateFirebaseUser(user: FirebaseUser?)

    /**
     * updateAuthenticationStatus
     * Responsible for updating the state of the boolean isAuthenticated safely, which tracks if the
     * authentication process has completed.
     * @param isAuthenticated Signals whether authentication is completed successfully or not in the
     * auth state
     */
    fun updateAuthenticationStatus(isAuthenticated: Boolean)

    /**
     * updateProcessingStatus
     * Responsible for updating the state of the boolean isProcessing safely, which tracks if
     * authentication is still currently ongoing
     * @param isProcessing Signals whether authentication is currently ongoing in the auth state
     */
    fun updateProcessingStatus(isProcessing: Boolean)

    /**
     * updateUserDataLoadedStatus
     * Responsible for updating the state of the boolean isLoaded safely, signalling whether user
     * data is successfully loaded in or not
     * @param isLoaded Tracks if user data is finished loading in the auth state
     */
    fun updateUserDataLoadedStatus(isLoaded: Boolean)

    /**
     * signOut
     * Attempts to sign the current user out of the application using Firebase.
     */
    suspend fun signOut()
} 