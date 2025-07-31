package beh59.aber.ac.uk.cs39440.mmp.ui.components.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import beh59.aber.ac.uk.cs39440.mmp.R
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.UserViewModel

/**
 * GoogleSignInButton
 * Creates a custom button that follows Google's branding guidelines for initiating the sign-in
 * and authentication process of the application. Used in MainActivity.kt's AuthFlow()
 * Guidelines can be found at https://developers.google.com/identity/branding-guidelines
 * @param onClick Callback that allows behaviour to be defined for it where GoogleSignInButton is
 * clicked
 * @param userViewModel ViewModel that handles user data and operations
 */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    userViewModel: UserViewModel
) {
    val userState by userViewModel.uiState.collectAsState()
    val isDarkMode = userState.isDarkMode

    if (isDarkMode) {
        Image(
            modifier = Modifier
                .clickable {
                    onClick()
                },
            painter = painterResource(
                id = R.drawable.google_sign_in_4x
            ),
            contentDescription = "Google Sign-In Button"
        )
    } else {
        Image(
            modifier = Modifier
                .clickable {
                    onClick()
                },
            painter = painterResource(
                id = R.drawable.light_google_sign_in_4x
            ),
            contentDescription = "Google Sign-In Button"
        )
    }
}