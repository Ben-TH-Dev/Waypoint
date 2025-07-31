package beh59.aber.ac.uk.cs39440.mmp.data.repository.impl

import android.util.Log
import beh59.aber.ac.uk.cs39440.mmp.data.models.User
import beh59.aber.ac.uk.cs39440.mmp.data.models.states.UserState
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IUserRepository
import beh59.aber.ac.uk.cs39440.mmp.utils.OnboardingState
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * UserRepository
 * An implementation of the IUserRepository interface
 * Contains the business logic related to user management within the application. Handles
 * calls to Firebase for user authentication, profile management, and onboarding
 * @param db The current instance of Firestore used by the application
 * @property userState A public StateFlow that exposes the state of user data and is observable by
 * the UI
 */
class UserRepository @Inject constructor(
    //Instance of FirebaseFirestore is injected.
    private val db: FirebaseFirestore
) : IUserRepository {
    private val _userState = MutableStateFlow(UserState())
    override val userState: StateFlow<UserState> = _userState.asStateFlow()

    /**
     * updateLocalUser
     * Updates the user data in state with a new user object
     * @param newUser The new user data to hold in state
     */
    override fun updateLocalUser(newUser: User) {
        _userState.value = _userState.value.copy(user = newUser)
    }

    /**
     * updateUsername
     * Updates the username data in state
     * @param username The new username to set
     */
    override fun updateUsername(username: String) {
        val currentUser = _userState.value.user
        _userState.value = _userState.value.copy(user = currentUser.copy(username = username))
    }

    /**
     * updateFirebaseUser
     * Updates the FirebaseUser in state and logs the update
     * @param firebaseUser The new FirebaseUser to store in state
     */
    override fun updateFirebaseUser(firebaseUser: FirebaseUser?) {
        if (firebaseUser != null) {
            Log.d(
                "AuthRepository",
                "updateFirebaseUser: Updating Firebase user"
            )
        } else {
            Log.d("AuthRepository", "updateFirebaseUser: Clearing Firebase user")
        }
        _userState.value = _userState.value.copy(firebaseUser = firebaseUser)
    }

    /**
     * updateLoadingState
     * Updates the loading state flag in state
     * @param isLoading Whether the application is currently loading data
     */
    override fun updateLoadingState(isLoading: Boolean) {
        _userState.value = _userState.value.copy(isLoading = isLoading)
    }

    /**
     * updateHasCompletedOnboarding
     * Updates the onboarding status in state for both username and phone number
     * @param usernameStatus Whether the user has set their username
     * @param phoneNumberStatus Whether the user has set their phone number
     */
    override fun updateHasCompletedOnboarding(usernameStatus: Boolean, phoneNumberStatus: Boolean) {
        _userState.value = _userState.value.copy(hasSetUsername = usernameStatus)
        _userState.value = _userState.value.copy(hasSetPhoneNumber = phoneNumberStatus)
    }

    /**
     * setDarkMode
     * Updates the dark mode preference in state
     * @param status Whether dark mode is enabled
     */
    override fun setDarkMode(status: Boolean) {
        _userState.value = _userState.value.copy(isDarkMode = status)
    }

    /**
     * addToFirestore
     * Adds a new user to Firestore or retrieves existing user data, and decides the appropriate
     * onboarding state
     * @param user The Firebase user to add to Firestore
     * @return The onboarding state of the user after checking their data
     */
    @AddTrace(name = "userRepoAddToFirestore")
    override suspend fun addToFirestore(user: FirebaseUser): OnboardingState =
        withContext(Dispatchers.IO) {
            try {
                //Acquires a reference to the location where the user's document will be stored in Firestore.
                val userDocument = db.collection("users").document(user.uid)

                //Attempts to retrieve any information that might exist at that location already.
                val remoteUser = userDocument.get().await()

                //Creates a flag signalling whether that user has ever signed in before with Google by
                //checking if anything exists at the location where the user's document will be stored.
                val newUser = !remoteUser.exists()

                //If the user has never signed in before with google, their information is uploaded to the
                //database for use later.
                if (newUser) {
                    //Maps each variable's value to the fields in the user's document in Firestore
                    val userData = hashMapOf(
                        "uid" to user.uid,
                        "displayName" to user.displayName,
                        "email" to user.email,
                        "photourl" to user.photoUrl
                    )

                    //Writes the information in userData to the user's document in Firestore and returns
                    //a success message and a UserState that tells the application the user needs to now
                    //complete the onboarding process and assign themselves a username.
                    userDocument.set(userData).await()

                    val usernameQuery = db.collection("usernames")
                        .whereEqualTo("uid", user.uid)
                        .get()
                        .await()

                    if (!usernameQuery.isEmpty) {
                        return@withContext OnboardingState.EXISTING_USER_NEEDS_NUMBER
                    }

                    return@withContext OnboardingState.NEW_USER_NEEDS_BOTH
                }

                //If the user is not signing in for the first time, query their username and check if it
                //exists. This prevents edge cases where users may sign in with Google but then exit the app
                //before assigning themselves a username.
                val usernameQuery = db.collection("usernames")
                    .whereEqualTo("uid", user.uid)
                    .get()
                    .await()

                val usernameExists = !usernameQuery.isEmpty

                val userDoc = db.collection("users").document(user.uid).get().await()
                val phoneNumberExists = if (userDoc.exists()) {
                    val phoneNumber = userDoc.getString("phonenumber")
                    phoneNumber != null && phoneNumber.isNotEmpty()
                } else {
                    false
                }

                //If the user already has a username, returns a UserState describing that the application
                //may proceed to its main content. Otherwise it signals that the user must go through
                //the onboarding process.
                return@withContext if (usernameExists && phoneNumberExists) {
                    OnboardingState.EXISTING_USER_COMPLETED_ONBOARDING
                } else if (usernameExists && !phoneNumberExists) {
                    OnboardingState.EXISTING_USER_NEEDS_NUMBER
                } else if (!usernameExists && phoneNumberExists) {
                    OnboardingState.EXISTING_USER_NEEDS_USERNAME
                } else {
                    OnboardingState.EXISTING_USER_NEEDS_BOTH
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "addToFirestore: Error adding user to Firestore", e)
                return@withContext OnboardingState.ERROR
            }
        }

    /**
     * setUsername
     * Sets a username for a user in Firestore
     * @param uid The unique identifier of the user
     * @param username The username to set
     * @return True if successful, false otherwise
     */
    @AddTrace(name = "userRepoSetUsername")
    override suspend fun setUsername(uid: String, username: String): Boolean {
        return try {
            //Runs in a transaction to prevent two users creating duplicate usernames at the same
            //time. Runs on background thread due to calls to Firestore.
            withContext(Dispatchers.IO) {
                db.runTransaction { transaction ->
                    //Acquires a reference to the location the username will be stored.
                    val usernameLocation = db.collection("usernames").document(username)

                    //Returns a snapshot of what currently exists at the referenced location.
                    val documentSnapshot = transaction.get(usernameLocation)

                    //Checks if a document with the given username already exists at
                    //the referenced location.
                    if (documentSnapshot.exists()) {
                        throw Exception("Username taken")
                    }

                    //Maps the uid parameter to the uid field of the username document
                    val usernameData = hashMapOf("uid" to uid)

                    //Sets the id of the document to the username chosen by the user and populates
                    //the 'uid' field.
                    transaction.set(usernameLocation, usernameData)
                }.await() //Waits for the transaction to finish before proceeding
            }

            true
        } catch (e: Exception) {
            Log.e(
                "AuthRepository",
                "setUsername: Error setting username '$username' for user $uid",
                e
            )
            false
        }
    }

    /**
     * setPhoneNumber
     * Sets a phone number for a user in Firestore and updates the local state
     * @param uid The unique identifier of the user
     * @param phoneNumber The phone number to set
     */
    override suspend fun setPhoneNumber(uid: String, phoneNumber: String) {
        try {
            withContext(Dispatchers.IO) {
                val userReference = db.collection("users").document(uid)
                val userDocument = userReference.get().await()

                if (userDocument.exists()) {
                    userReference.update("phonenumber", phoneNumber).await()
                    val currentUser = _userState.value.user
                    _userState.value = _userState.value.copy(
                        user = currentUser.copy(phonenumber = phoneNumber),
                        hasSetPhoneNumber = true
                    )
                } else {
                    Log.d("UserRepository", "Couldn't retrieve user data")
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error setting phone number for user $uid", e)
            throw e
        }
    }

    /**
     * hasSetPhoneNumber
     * Checks if a user has set their phone number in Firestore
     * @param uid The unique identifier of the user
     * @return True if the user has set a phone number, false otherwise
     */
    override suspend fun hasSetPhoneNumber(uid: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val userDocument = db.collection("users").document(uid).get().await()

                if (userDocument.exists()) {
                    val phoneNumber = userDocument.getString("phonenumber")
                    val hasPhone = phoneNumber != null && phoneNumber.isNotEmpty()
                    hasPhone
                } else {
                    Log.d(
                        "UserRepository",
                        "Error retrieving user document"
                    )
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error checking if user has set phone number: ${e.message}", e)
            false
        }
    }

    /**
     * hasSetUsername
     * Checks if a user has set their username by querying the usernames collection in Firestore
     * @param uid The unique identifier of the user
     * @return True if the user has set a username, false otherwise
     */
    override suspend fun hasSetUsername(uid: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                //Queries the usernames collection to find documents which contain the given UID
                val usernameQuery = db.collection("usernames")
                    .whereEqualTo("uid", uid)
                    .get()
                    .await()

                //If the query returns any documents, the user has set a username
                !usernameQuery.isEmpty
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error checking if user has set username", e)
            false
        }
    }

    /**
     * getUserDetails
     * Retrieves user details from Firestore including their username from a separate collection
     * @param uid The unique identifier of the user
     * @return The user data retrieved from Firestore
     */
    @AddTrace(name = "UserRepoGetUserDetails")
    //Retrieves the user's details from Firestore and stores them inside an instance of the data
    //class User in the models package. Also acquires the user's username from the separate Firestore
    //collection and appends it.
    override suspend fun getUserDetails(uid: String): User {
        return try {
            //Runs on the background thread whilst doing queries to the Firestore to prevent
            //blocking up the main thread
            withContext(Dispatchers.IO) {
                //Acquires a reference to the user's document in the users collection
                val userLocation = db.collection("users").document(uid).get().await()

                //If the process fails, return a default user.
                val userData = userLocation.toObject(User::class.java) ?: User(uid, "", "", "", "")

                //Searches the username collection and looks for any documents whose UID field
                //matches the user's UID.
                val usernameQuery = db.collection("usernames")
                    .whereEqualTo("uid", uid)
                    .get()
                    .await()

                val username = if (usernameQuery.isEmpty) "" else usernameQuery.documents.first().id

                //Appends the retrieved username to the instance of User, if it exists.
                val finalUser = userData.copy(username = username)
                Log.d(
                    "AuthRepository",
                    "getUserDetails: Final user object with username: $finalUser"
                )
                finalUser
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "getUserDetails: Error getting user details for $uid", e)
            User(uid, "Unknown", "unknown@example.com", "", "")
        }
    }

    /**
     * checkIfTaken
     * Checks if a username is already taken by looking for a document with that ID in the usernames
     * collection
     * @param username The username to check
     * @return True if the username is taken, false otherwise
     */
    @AddTrace(name = "userRepoCheckIfTaken")
    override suspend fun checkIfTaken(username: String): Boolean {
        Log.d("AuthRepository", "checkIfTaken: Checking if username '$username' is taken")
        return try {
            withContext(Dispatchers.IO) {
                //Creates a reference to the usernames collection in Firestore
                val usernameLocation = db.collection("usernames").document(username).get().await()
                //Checks if anything exists at that location in Firestore. with the same ID as the username parameter to see if anything
                //exists at the location.
                val exists = usernameLocation.exists()
                Log.d("AuthRepository", "checkIfTaken: $exists")
                exists
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "checkIfTaken: Error checking if username is taken")
            false
        }
    }

    /**
     * checkUsernameAvailability
     * Checks if a username is available and updates the state with the result
     * @param username The username to check for availability
     */
    override suspend fun checkUsernameAvailability(username: String) {
        try {
            val isTaken = checkIfTaken(username)
            _userState.value = _userState.value.copy(isUsernameAvailable = !isTaken)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error checking username availability", e)
            _userState.value = _userState.value.copy(isUsernameAvailable = false)
        }
    }

    /**
     * resetUsernameQuery
     * Resets the username availability query state to null
     */
    override fun resetUsernameQuery() {
        _userState.value = _userState.value.copy(isUsernameAvailable = null)
    }
}