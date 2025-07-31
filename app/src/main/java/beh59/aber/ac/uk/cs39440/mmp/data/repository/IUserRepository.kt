package beh59.aber.ac.uk.cs39440.mmp.data.repository

import beh59.aber.ac.uk.cs39440.mmp.data.models.User
import beh59.aber.ac.uk.cs39440.mmp.data.models.states.UserState
import beh59.aber.ac.uk.cs39440.mmp.utils.OnboardingState
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

/**
 * IUserRepository
 * An interface that serves as an abstraction for the methods required by the user management
 * systems in the application.
 * @property userState A StateFlow holding the state of data needed by user features that can
 * be observed in the UI
 */
interface IUserRepository {
    val userState: StateFlow<UserState>

    /**
     * updateLocalUser
     * Defines a method that should update the user data in state
     * @param newUser The new user data to hold in state
     */
    fun updateLocalUser(newUser: User)

    /**
     * updateUsername
     * Defines a method that should update the username data in state
     * @param username The new username to set
     */
    fun updateUsername(username: String)

    /**
     * updateFirebaseUser
     * Defines a method that should update the FirebaseUser in state
     * @param firebaseUser The new FirebaseUser to store in state
     */
    fun updateFirebaseUser(firebaseUser: FirebaseUser?)

    /**
     * updateLoadingState
     * Defines a method that should update the loading state
     * @param isLoading Whether the application is currently loading data
     */
    fun updateLoadingState(isLoading: Boolean)

    /**
     * updateHasCompletedOnboarding
     * Defines a method that should update the onboarding completion status in state
     * @param usernameStatus Whether the user has set their username
     * @param phoneNumberStatus Whether the user has set their phone number
     */
    fun updateHasCompletedOnboarding(usernameStatus: Boolean, phoneNumberStatus: Boolean)

    /**
     * setDarkMode
     * Defines a method that should update the dark mode preference in state
     * @param status Whether dark mode is enabled
     */
    fun setDarkMode(status: Boolean)

    /**
     * checkUsernameAvailability
     * Defines a method that should check whether a username is available and update state
     * @param username The username to check for availability
     */
    suspend fun checkUsernameAvailability(username: String)

    /**
     * resetUsernameQuery
     * Defines a method that should reset the username availability query state
     */
    fun resetUsernameQuery()

    /**
     * addToFirestore
     * Defines a method that should add user data to Firestore if they are a new user and return
     * other information if not
     * @param user The Firebase user to add to Firestore
     * @return The onboarding state of the user after adding them to Firestore
     */
    suspend fun addToFirestore(user: FirebaseUser): OnboardingState

    /**
     * setUsername
     * Defines a method that should set a username for a user in Firestore
     * @param uid The unique identifier of the user
     * @param username The username to set
     * @return True if successful, false otherwise
     */
    suspend fun setUsername(uid: String, username: String): Boolean

    /**
     * setPhoneNumber
     * Defines a method that should set a phone number for a user in Firestore
     * @param uid The unique identifier of the user
     * @param phoneNumber The phone number to set
     */
    suspend fun setPhoneNumber(uid: String, phoneNumber: String)

    /**
     * getUserDetails
     * Defines a method that should retrieve user details from Firestore
     * @param uid The unique identifier of the user
     * @return The user data retrieved from Firestore
     */
    suspend fun getUserDetails(uid: String): User

    /**
     * checkIfTaken
     * Defines a method that should check if a username is already taken
     * @param username The username to check
     * @return True if the username is taken, false otherwise
     */
    suspend fun checkIfTaken(username: String): Boolean

    /**
     * hasSetPhoneNumber
     * Defines a method that should check if a user has set their phone number
     * @param uid The unique identifier of the user
     * @return True if the user has set a phone number, false otherwise
     */
    suspend fun hasSetPhoneNumber(uid: String): Boolean

    /**
     * hasSetUsername
     * Defines a method that should check if a user has set their username
     * @param uid The unique identifier of the user
     * @return True if the user has set a username, false otherwise
     */
    suspend fun hasSetUsername(uid: String): Boolean
}