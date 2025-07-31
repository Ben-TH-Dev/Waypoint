package beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import beh59.aber.ac.uk.cs39440.mmp.data.models.User
import beh59.aber.ac.uk.cs39440.mmp.data.models.states.UserState
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * UserViewModel
 * Acts as a bridge between the repository which handles user data operations and the UI
 * @param userRepository Repository that handles user operations and data
 * @property uiState Exposes UI state from the repository to be used in the UI
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: IUserRepository
) : ViewModel() {
    val uiState: StateFlow<UserState> = userRepository.userState

    /**
     * queryUsername
     * Checks if a given username is available in Firestore
     * @param username The username to check for availability
     */
    fun queryUsername(username: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    userRepository.checkUsernameAvailability(username)
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error checking username availability for '$username'", e)
            }
        }
    }

    /**
     * resetUsernameQuery
     * Resets the isUsernameAvailable flag in state to let the user query again
     */
    fun resetUsernameQuery() {
        viewModelScope.launch {
            userRepository.resetUsernameQuery()
        }
    }

    /**
     * setDarkMode
     * Updates the user's dark mode preference in state
     * @param enabled Whether dark mode should be enabled
     */
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userRepository.setDarkMode(enabled)
        }
    }

    /**
     * getUserDetails
     * Retrieves user details for a given user ID and creates a User object from them
     * @param uid The unique identifier of the user to get details for
     * @return User object containing the user's details
     */
    suspend fun getUserDetails(uid: String): User {
        return try {
            userRepository.getUserDetails(uid)
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error getting user details for user $uid", e)
            User(uid = uid, displayName = "Unknown User")
        }
    }

    /**
     * setUsername
     * Updates the username for the current user in Firestore
     * @param username The new username to set
     * @param currentUser The current user
     * @param onSuccess Callback function to execute if username is set successfully
     * @param onFailure Callback function to execute if username setting fails
     */
    fun setUsername(
        username: String,
        currentUser: User,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                //Runs on a background thread to prevent Firestore queries lagging main thread
                withContext(Dispatchers.IO) {
                    //Ensures user is authenticated and valid
                    try {
                        //Runs and returns a boolean if successful
                        val success = userRepository.setUsername(currentUser.uid, username)

                        //If userRepository.setUsername is successful, then the following code runs
                        //and sets _hasSetUsername.value to true, which signifies to the app that it should
                        //switch from the Onboarding screen to the rest of the application once the user
                        //has submitted a valid username.
                        if (success) {
                            //Updates the local user object with the new username
                            userRepository.updateUsername(username)

                            val usernameSet = userRepository.hasSetUsername(currentUser.uid)

                            if (usernameSet) {
                                userRepository.updateHasCompletedOnboarding(
                                    true,
                                    userRepository.userState.value.hasSetPhoneNumber
                                )
                                //Calls the onSuccess parameter, allowing us to what define what should happen
                                //after this function succeeds in MainActivity.kt
                                onSuccess()
                            } else {
                                Log.e(
                                    "UserViewModel",
                                    "Username was not properly saved to Firestore"
                                )
                                onFailure()
                            }
                        } else {
                            Log.w("UserViewModel", "Failed to set username: $username")
                            //Calls the onFailure parameter, allowing us to what define what should happen
                            //after this function succeeds in MainActivity.kt
                            onFailure()
                        }
                    } catch (e: Exception) {
                        Log.e("UserViewModel", "Error setting username: ${e.message}", e)
                        onFailure()
                    }
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error setting username '$username'", e)
                onFailure()
            }
        }
    }

    /**
     * setPhoneNumber
     * Updates the phone number for the current user in Firestore
     * @param phoneNumber The phone number to set
     * @param currentUser The current user object
     * @param onSuccess Callback function to execute if phone number is set successfully
     * @param onFailure Callback function to execute if it fails
     */
    fun setPhoneNumber(
        phoneNumber: String,
        currentUser: User,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    try {
                        userRepository.setPhoneNumber(currentUser.uid, phoneNumber)

                        val phoneNumberSet = userRepository.hasSetPhoneNumber(currentUser.uid)

                        if (phoneNumberSet) {
                            userRepository.updateHasCompletedOnboarding(
                                userRepository.userState.value.hasSetUsername,
                                true
                            )
                            onSuccess()
                        } else {
                            Log.e("UserViewModel", "Error setting phone number")
                            onFailure()
                        }
                    } catch (e: Exception) {
                        Log.e("UserViewModel", "Error setting phone number", e)
                        onFailure()
                    }
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error setting phone number", e)
                onFailure()
            }
        }
    }
}