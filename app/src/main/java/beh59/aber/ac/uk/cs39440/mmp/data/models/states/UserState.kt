package beh59.aber.ac.uk.cs39440.mmp.data.models.states

import androidx.annotation.Keep
import beh59.aber.ac.uk.cs39440.mmp.data.models.User
import com.google.firebase.auth.FirebaseUser

/**
 * UserState
 * Represents the state of the current user in the application
 * @param user The current user's information
 * @param firebaseUser The object returned by successful Firebase authentication
 * @param isDarkMode Tracks if dark mode is enabled
 * @param hasSetUsername Tracks if the user has set a username
 * @param hasSetPhoneNumber Indicates if the user has set a phone number
 * @param isUsernameAvailable Indicates whether the chosen username is available
 * @param isLoading Tracks if user data is currently being loaded
 */
@Keep
data class UserState(
    val user: User = User(
        "UserState error",
        "UserState error",
        "UserState error",
        "UserState error",
        "UserState error",
    ),
    val firebaseUser: FirebaseUser? = null,
    val isDarkMode: Boolean = false,
    val hasSetUsername: Boolean = false,
    val hasSetPhoneNumber: Boolean = false,
    val isUsernameAvailable: Boolean? = true,
    val isLoading: Boolean = false
)