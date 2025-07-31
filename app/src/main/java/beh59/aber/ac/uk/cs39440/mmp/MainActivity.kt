package beh59.aber.ac.uk.cs39440.mmp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import beh59.aber.ac.uk.cs39440.mmp.nav.WaypointNav
import beh59.aber.ac.uk.cs39440.mmp.ui.other.SplashPage
import beh59.aber.ac.uk.cs39440.mmp.ui.theme.MMPTheme
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.AuthViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.UserViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity
 * The starting point of the application, responsible for starting the application, vital components
 * such as userViewModel, authViewModel, and fusedLocationClient. Controls the initial flow of the
 * application before it is handed off to WaypointNav.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val userViewModel: UserViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        splashScreen.setKeepOnScreenCondition { true }

        setContent {
            splashScreen.setKeepOnScreenCondition { false }

            MMPTheme {
                userViewModel.setDarkMode(isSystemInDarkTheme())
                AuthFlow()
            }
        }
    }

    /**
     * AuthFlow
     * Responsible for managing the control flow of the sign in and authentication process and
     * ensuring that the application does not proceed until the user is successfully authenticated
     * and their data fully loaded. Uses loading displays to reduce friction and let the user know
     * progress is being made
     */
    @Composable
    fun AuthFlow() {
        val context = LocalContext.current
        val authState by authViewModel.uiState.collectAsState()

        when {
            authState.isProcessingAuth -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            authState.isAuthenticated && !authState.isUserDataLoaded -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            authState.isAuthenticated && authState.firebaseUser != null -> {
                WaypointNav()
            }

            else -> {
                SplashPage(userViewModel) {
                    authViewModel.signInWithGoogle(context)
                }
            }
        }
    }
}