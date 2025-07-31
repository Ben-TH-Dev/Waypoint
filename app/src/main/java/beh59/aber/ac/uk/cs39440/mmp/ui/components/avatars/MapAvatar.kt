package beh59.aber.ac.uk.cs39440.mmp.ui.components.avatars

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.UserViewModel

/**
 * MapAvatar
 * A UI component that displays an avatar on MapControls
 * @param userViewModel ViewModel that handles user data and operations
 * @param navController Handles navigation between screens
 */
@Composable
fun MapAvatar(userViewModel: UserViewModel, navController: NavController) {
    Box {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Spacer(modifier = Modifier.width(8.dp))

                AvatarS(userViewModel, navController)
            }
        }
    }
}