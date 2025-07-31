package beh59.aber.ac.uk.cs39440.mmp.domain.auth

import android.util.Log
import beh59.aber.ac.uk.cs39440.mmp.data.models.User
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IAuthRepository
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IUserRepository
import beh59.aber.ac.uk.cs39440.mmp.utils.OnboardingState
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.perf.metrics.AddTrace
import javax.inject.Inject

/**
 * LoadUserDataUseCase
 * Encapsulates and coordinates logic related to retrieving and loading user data
 * @param authRepository Contains business logic and communication with AuthDataSource related to
 * authentication within the application, used here to help with control flow
 * @param userRepository Contains business logic and Firestore calls related to user systems in the
 * application
 */
class LoadUserDataUseCase @Inject constructor(
    private val authRepository: IAuthRepository,
    private val userRepository: IUserRepository
) {
    /**
     * invoke
     * Called when LoadUserDataUseCase is called. Manages the overall process of loading user data,
     * but leaves the details to UserRepository.
     * @param firebaseUser The authenticated FirebaseUser to retrieve the details about
     * @return A result containing a populated User object if successful and an exception if not
     */
    @AddTrace(name = "loadUserDataUseCase")
    suspend operator fun invoke(firebaseUser: FirebaseUser): Result<User> {
        return try {
            //Runs addToFirestore, which registers new users in the Firestore database and returns
            //information if you are an already existing user on OnboardingState
            val userData = userRepository.addToFirestore(firebaseUser)

            //Assigns hasUsername based on the value of the OnboardingState enum returned by
            //addToFirestore
            val hasUsername = when (userData) {
                OnboardingState.NEW_USER_NEEDS_BOTH -> false
                OnboardingState.EXISTING_USER_NEEDS_BOTH -> false
                OnboardingState.EXISTING_USER_NEEDS_USERNAME -> false
                OnboardingState.EXISTING_USER_NEEDS_NUMBER -> true
                OnboardingState.EXISTING_USER_COMPLETED_ONBOARDING -> true
                OnboardingState.ERROR -> {
                    return Result.failure(Exception("Error finding state of username"))
                }
            }

            //Assigns hasPhoneNumber based on the value of the OnboardingState enum returned by
            //addToFirestore
            val hasPhoneNumber = when (userData) {
                OnboardingState.EXISTING_USER_NEEDS_NUMBER -> false
                OnboardingState.NEW_USER_NEEDS_BOTH -> false
                OnboardingState.EXISTING_USER_NEEDS_BOTH -> false
                OnboardingState.EXISTING_USER_NEEDS_USERNAME -> true
                OnboardingState.EXISTING_USER_COMPLETED_ONBOARDING -> true
                OnboardingState.ERROR -> {
                    return Result.failure(Exception("Error finding state of phone number"))
                }
            }

            //Updates the value of hasCompletedOnboarding in state to allow the app to re-route
            //the user if needed
            userRepository.updateHasCompletedOnboarding(hasUsername, hasPhoneNumber)
            //Retrieves the user's details from Firestore and updates state with the new user
            val userDetails = userRepository.getUserDetails(firebaseUser.uid)
            userRepository.updateLocalUser(userDetails)
            //Updates state to signal that user data is loaded
            authRepository.updateUserDataLoadedStatus(true)

            //Returns a result containing the retrieved details
            Result.success(userDetails)
        } catch (e: Exception) {
            //If there any problems, logs the error and returns a result containing the exception
            Log.e("LoadUserDataUseCase", "Error loading user data", e)
            Result.failure(e)
        }
    }
} 