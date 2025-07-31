package beh59.aber.ac.uk.cs39440.mmp.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import beh59.aber.ac.uk.cs39440.mmp.ui.friends.FriendViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.projects.ProjectViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.LocationViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.UserViewModel

/**
 * MapScreen
 * Main screen that combines the MapboxMap and MapControls
 * @param userViewModel ViewModel that handles user data and operations
 * @param friendViewModel ViewModel that handles friend related data and operations
 * @param locationViewModel ViewModel that handles location related data and operations
 * @param mapViewModel ViewModel that handles map related data and operations
 * @param projectViewModel ViewModel that handles project related data and operations
 * @param navController Handles navigation between screens
 */
@Composable
fun MapScreen(
    userViewModel: UserViewModel,
    friendViewModel: FriendViewModel,
    locationViewModel: LocationViewModel,
    mapViewModel: MapViewModel,
    projectViewModel: ProjectViewModel,
    navController: NavHostController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        MapViewController(
            userViewModel,
            friendViewModel,
            locationViewModel,
            mapViewModel,
            projectViewModel,
            navController
        )
        MapControls(userViewModel, locationViewModel, projectViewModel, navController)
    }
}