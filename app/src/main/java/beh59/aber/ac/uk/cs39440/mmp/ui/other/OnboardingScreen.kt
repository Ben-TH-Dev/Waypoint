package beh59.aber.ac.uk.cs39440.mmp.ui.other

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import beh59.aber.ac.uk.cs39440.mmp.R
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

/**
 * OnboardingScreen
 * Used to gather custom data that is not available through the authenticated FirebaseUser object
 * @param userViewModel ViewModel containing user related data and operations
 */
@Composable
fun OnboardingScreen(userViewModel: UserViewModel) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface
    val userState by userViewModel.uiState.collectAsState()
    val user = userState.user
    val isFree = userState.isUsernameAvailable
    val coroutineScope = rememberCoroutineScope()

    val needsUsername = !userState.hasSetUsername
    val needsPhoneNumber = !userState.hasSetPhoneNumber

    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    val tooShort = username.length < 4
    val tooLong = username.length > 12
    val usernameValid = !tooShort && !tooLong && isFree == true
    val phoneNumberValid = phoneNumber.isNotEmpty() && phoneNumber.isDigitsOnly()

    val buttonEnabled = when {
        needsUsername && needsPhoneNumber -> usernameValid && phoneNumberValid
        needsUsername -> usernameValid
        needsPhoneNumber -> phoneNumberValid
        else -> false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(130.dp)
                    .padding(top = 15.dp)
            ) {
                Canvas(
                    modifier = Modifier.matchParentSize()
                ) {
                    drawCircle(
                        color = primaryColor,
                        radius = size.minDimension / 2
                    )
                }

                Image(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .requiredSize(80.dp),
                    painter = painterResource(
                        id = R.drawable.filled_location_on_24px
                    ),
                    contentDescription = "Search Icon"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Onboarding",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (needsUsername) {
                Text(
                    text = "Please enter a username for your Waypoint profile. This can be changed later.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = username.lowercase(),
                    onValueChange = {
                        val newUsername = it.take(12)
                        username = newUsername
                        if (newUsername.length < 4) {
                            userViewModel.resetUsernameQuery()
                        } else {
                            coroutineScope.launch {
                                userViewModel.queryUsername(it)
                            }
                        }
                    },
                    label = { Text("Username") },
                    singleLine = true
                )

                when {
                    tooShort -> Text(
                        text = "Enter at least four characters",
                        color = textColor
                    )

                    tooLong -> Text(
                        text = "❌ Username must not be longer than 12 characters",
                        color = textColor
                    )

                    isFree == false -> Text(
                        text = "❌ Username taken",
                        color = textColor
                    )

                    isFree == true -> Text(
                        text = "✅ Username available",
                        color = textColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (needsPhoneNumber) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    singleLine = true
                )

                if (phoneNumber.isNotEmpty() && !phoneNumber.isDigitsOnly()) {
                    Text(
                        text = "❌ Phone number must contain only numbers",
                        color = textColor
                    )
                } else if (phoneNumber.isNotEmpty()) {
                    Text(
                        text = "✅",
                        color = textColor
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter),
        ) {
            Button(
                onClick = {
                    if (needsUsername && needsPhoneNumber) {
                        //Attempts to submit both fields to Firestore where needed
                        userViewModel.setUsername(
                            username,
                            user,
                            onSuccess = {
                                userViewModel.setPhoneNumber(
                                    phoneNumber,
                                    user,
                                    onSuccess = {
                                        Log.d(
                                            "OnboardingScreen",
                                            "Username and phone number set successfully"
                                        )
                                    },
                                    onFailure = {
                                        Log.e("OnboardingScreen", "Failed to set phone number")
                                    }
                                )
                            },
                            onFailure = {
                                Log.e("OnboardingScreen", "Failed to set username")
                            }
                        )
                    } else if (needsUsername) {
                        //Attempts to submit username to Firestore
                        userViewModel.setUsername(
                            username,
                            user,
                            onSuccess = {
                                Log.d("OnboardingScreen", "Username set successfully")
                            },
                            onFailure = {
                                Log.e("OnboardingScreen", "Failed to set username")
                            }
                        )
                    } else if (needsPhoneNumber) {
                        //Attempts to submit phone number to Firestore
                        userViewModel.setPhoneNumber(
                            phoneNumber,
                            user,
                            onSuccess = {
                                Log.d("OnboardingScreen", "Phone number set successfully")
                            },
                            onFailure = {
                                Log.e("OnboardingScreen", "Failed to set phone number")
                            }
                        )
                    }
                },
                enabled = buttonEnabled
            ) {
                Text(
                    text = "Start Organising"
                )
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}