package beh59.aber.ac.uk.cs39440.mmp.data.models.states

import androidx.annotation.Keep
import com.google.firebase.auth.FirebaseUser

/**
 * AuthState
 * Stores information about the state of user authentication, used for control flow during the
 * initial stage of the application
 * @param firebaseUser Holds the FirebaseUser which is provided after authentication is
 * successful
 * @param isAuthenticated Tracks whether or not the user has successfully authenticated
 * @param isProcessingAuth Tracks whether or not the user is currently authenticating
 * @param isUserDataLoaded Tracks whether or not user data has been fully loaded and populated
 * locally yet
 */
@Keep
data class AuthState(
    val firebaseUser: FirebaseUser? = null,
    val isAuthenticated: Boolean = false,
    val isProcessingAuth: Boolean = false,
    val isUserDataLoaded: Boolean = false
) 