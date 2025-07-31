package beh59.aber.ac.uk.cs39440.mmp.ui.components.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import beh59.aber.ac.uk.cs39440.mmp.data.models.UserLocation
import coil.compose.AsyncImage

/**
 * FriendMapItem
 * Used to display friend's location on the live map
 * @param userLocation Contains the friend's location data
 * @param navController Handles navigation between screens
 */
@Composable
fun FriendMapItem(userLocation: UserLocation, navController: NavController) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .clickable { navController.navigate("FriendProfile/${userLocation.uid}") },
    ) {
        AsyncImage(
            model = userLocation.photourl,
            contentDescription = "Profile Picture",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}