package beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import beh59.aber.ac.uk.cs39440.mmp.data.models.states.AuthState
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IAuthRepository
import beh59.aber.ac.uk.cs39440.mmp.domain.auth.LoadUserDataUseCase
import beh59.aber.ac.uk.cs39440.mmp.domain.auth.SignInWithGoogleUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AuthViewModel
 * Mediates communication between the UI and the authentication systems in the app
 * @param authRepository Handles authentication related data and operations
 * @param signInWithGoogleUseCase Use case for signing in with Google
 * @param loadUserDataUseCase Use case for loading user data
 * @property uiState Exposes UI state from the repository to be used in the UI
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val loadUserDataUseCase: LoadUserDataUseCase
) : ViewModel() {
    //Exposes the state of AuthState to the UI
    val uiState: StateFlow<AuthState> = authRepository.authState

    //If the FirebaseUser still exists in storage (as a feature of the FirebaseAuth API) after
    //closing and re-opening the app then this block of code will sign them in automatically to
    //reduce friction for the user and speed up times.
    init {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            //Updates the FirebaseUser in state
            authRepository.updateFirebaseUser(currentUser)
            //Signals to the control flow in MainActivity that the application is no longer
            //processing authentication since the user is pre-authenticated
            authRepository.updateProcessingStatus(false)
            //Signals to the control flow in MainActivity that the authentication process is fully
            //complete.
            authRepository.updateAuthenticationStatus(true)

            //Invokes LoadUserDataCase retrieves user data and sets userDataLoadedStatus to true.
            //This is the last flag needed by the control flow in MainActivity to proceed to the
            //rest of the application.
            viewModelScope.launch {
                loadUserData(currentUser)
            }
        }
    }

    /**
     * signInWithGoogle
     * Invokes the SignInWithGoogleUseCase to manage signing in the user and authenticating them
     * @param context The context needed for Google's CredentialManager
     */
    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            try {
                val result = signInWithGoogleUseCase(context)

                if (result.isSuccess) {
                    val firebaseUser = result.getOrNull()
                    firebaseUser?.let {
                        loadUserData(it)
                    }
                } else {
                    Log.e("AuthViewModel", "AuthFlow: Authentication failed")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "AuthFlow: Error during signInWithGoogle() method execution")
            }
        }
    }

    /**
     * loadUserData
     * Invokes the LoadUserDataUseCase to retrieve user data given an authenticated FirebaseUser
     * @param firebaseUser An authenticated FirebaseUser containing information about the user
     */
    private suspend fun loadUserData(firebaseUser: FirebaseUser) {
        try {
            val result = loadUserDataUseCase(firebaseUser)
            if (result.isSuccess) {
                Log.d("AuthViewModel", "AuthFlow: User data loaded successfully")
            } else {
                Log.e("AuthViewModel", "AuthFlow: Failed to load user data")
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error loading user data", e)
        }
    }

    /**
     * signOut
     * Signs the current user out via communication with AuthRepository
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error signing out", e)
            }
        }
    }
} 