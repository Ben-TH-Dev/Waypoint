package beh59.aber.ac.uk.cs39440.mmp.ui.components.avatars

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import beh59.aber.ac.uk.cs39440.mmp.R
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.UserViewModel
import coil.compose.AsyncImage

/**
 * AvatarS
 * A UI component that displays a small avatar with an image loaded asynchronously from the user's
 * photo URL field with a placeholder in case of failure
 * @param userViewModel ViewModel that handles user data and operations
 * @param navController Handles navigation between screens
 */
@Composable
fun AvatarS(userViewModel: UserViewModel, navController: NavController) {
    val userState by userViewModel.uiState.collectAsState()
    val user = userState.user
    val photourl = user.photourl

    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .clickable { navController.navigate("Profile") }
    ) {
        AsyncImage(
            model = photourl,
            contentDescription = "Profile Picture",
            placeholder = painterResource(id = R.drawable.generic_avatar),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}