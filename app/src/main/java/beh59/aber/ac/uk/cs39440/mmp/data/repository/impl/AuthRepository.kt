package beh59.aber.ac.uk.cs39440.mmp.data.repository.impl

import android.util.Log
import beh59.aber.ac.uk.cs39440.mmp.data.models.states.AuthState
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IAuthRepository
import beh59.aber.ac.uk.cs39440.mmp.data.source.remote.AuthDataSource
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * AuthRepository
 * An implementation of the IAuthRepository interface
 * Contains the business logic related to sign-in and authentication within the application. Handles
 * calls to Firebase alongside AuthDataSource
 * @param authDataSource Contains network logic that communicates with FirebaseAuth to sign in the
 * user and to sign them out
 * @property authState A public StateFlow that exposes the state of _authState and observable by the
 * UI
 */
class AuthRepository @Inject constructor(
    private val authDataSource: AuthDataSource
) : IAuthRepository {
    private val _authState = MutableStateFlow(AuthState())
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * signInWithGoogle
     * Takes in the user's ID token, returned by Google's CredentialManager, and exchanges it for
     * an authenticated FirebaseUser via communication with authDataSource.
     * @param idToken A unique identifier associated with a Google account, returned by the sign-in
     * process
     * @return A Result containing FirebaseUser if successful and an error if not.
     */
    @AddTrace(name = "authRepoSignInTrace")
    override suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        try {
            //Update state to signal that authentication is in progress
            _authState.value = _authState.value.copy(
                isProcessingAuth = true
            )

            //Communicates with AuthDataSource for network calls to FirebaseAuth
            val result = authDataSource.signInWithGoogle(idToken)

            //If the process was successful, this code will run and we extract the authenticated
            //FirebaseUser from the Result.
            if (result.isSuccess) {
                val firebaseUser = result.getOrNull()!!

                //Update auth state with successful authentication
                _authState.value = _authState.value.copy(
                    firebaseUser = firebaseUser,
                    isAuthenticated = true,
                    isProcessingAuth = false
                )

                //We pass along the result to the UI
                return Result.success(firebaseUser)
            } else {
                //If the process was not successful, this code runs which handles the failure and
                //updates isProcessing to signal that authentication is no longer taking place
                val exception =
                    result.exceptionOrNull() ?: Exception("Unknown authentication error")

                _authState.value = _authState.value.copy(
                    isProcessingAuth = false
                )

                Log.e("AuthRepository", "AuthFlow: Authentication failed", exception)
                //We pass along the failure to the UI
                return Result.failure(exception)
            }
        } catch (e: Exception) {
            //In case of any exceptions, also update the state to signal that authentication is over
            //and pass along the failure.
            _authState.value = _authState.value.copy(
                isProcessingAuth = false
            )
            Log.e("AuthRepository", "AuthFlow: Authentication failed", e)
            return Result.failure(e)
        }
    }

    /**
     * updateFirebaseUser
     * @param user The new FirebaseUser object we want to write to state.
     */
    override fun updateFirebaseUser(user: FirebaseUser?) {
        _authState.value = _authState.value.copy(firebaseUser = user)
    }

    /**
     * updateAuthenticationStatus
     * Updates state with the value of isAuthenticated, which signals to the application if the
     * user has finished authentication successfully
     * @param isAuthenticated Whether or not the user is successfully authenticated
     */
    override fun updateAuthenticationStatus(isAuthenticated: Boolean) {
        _authState.value = _authState.value.copy(isAuthenticated = isAuthenticated)
    }

    /**
     * updateProcessingStatus
     * Updates state with the value of isProcessing, which tracks whether the app is currently
     * processing the authentication of the current user
     * @param isProcessing Whether or not the user is currently being authenticated
     */
    override fun updateProcessingStatus(isProcessing: Boolean) {
        _authState.value = _authState.value.copy(isProcessingAuth = isProcessing)
    }

    /**
     * updateUserDataLoadedStatus
     * Updates state with the value of isLoaded, which tracks whether or not user data is fully
     * loaded in yet or not.
     * @param isLoaded Whether or not user data is fully loaded in yet.
     */
    override fun updateUserDataLoadedStatus(isLoaded: Boolean) {
        _authState.value = _authState.value.copy(isUserDataLoaded = isLoaded)
    }

    /**
     * signOut
     * Communicates with AuthDataSource to sign out the user with FirebaseAuth, then resets everything
     * in AuthState to ensure data is cleared
     */
    override suspend fun signOut() {
        authDataSource.signOut()
        _authState.value = AuthState()
    }
} 