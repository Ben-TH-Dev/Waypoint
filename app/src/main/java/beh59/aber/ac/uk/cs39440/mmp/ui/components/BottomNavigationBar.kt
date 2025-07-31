package beh59.aber.ac.uk.cs39440.mmp.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import beh59.aber.ac.uk.cs39440.mmp.R

/**
 * BottomNavItem
 * Sealed class defining navigation items for the bottom navigation bar
 * @param route The navigation route associated with the item
 * @param filledIcon The filled icon used when the item is selected
 * @param outlinedIcon The outlined icon used when the item is not selected
 * @param label Text label for the navigation item
 */
sealed class BottomNavItem(
    val route: String,
    val filledIcon: Int,
    val outlinedIcon: Int,
    val label: String
) {
    //Creates an item that navigates you to the profile screen
    data object Profile : BottomNavItem(
        "Profile",
        R.drawable.filled_account_circle_24px,
        R.drawable.outlined_account_circle_24px,
        ""
    )

    //Creates an item that navigates you to the friends screen
    data object Friends :
        BottomNavItem("Friends", R.drawable.filled_group_24px, R.drawable.outlined_group_24px, "")

    //Creates an item that navigates you to the map screen
    data object Map : BottomNavItem(
        "Map",
        R.drawable.filled_location_on_24px,
        R.drawable.outlined_location_on_24px,
        ""
    )

    //Creates an item that navigates you to the projects screen
    data object Projects : BottomNavItem(
        "Projects",
        R.drawable.filled_group_work_24px,
        R.drawable.outlined_group_work_24px,
        ""
    )

    //Creates an item that navigates you to the chat screen
    data object Chat : BottomNavItem(
        "Chat",
        R.drawable.filled_chat_bubble_24px,
        R.drawable.outlined_chat_bubble_24px,
        ""
    )
}

/**
 * BottomNavigationBar
 * Creates the app's bottom navigation bar from the BottomNavItems defined
 * @param navController Handles navigation between screens
 */
@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val items = listOf(
            BottomNavItem.Profile,
            BottomNavItem.Friends,
            BottomNavItem.Map,
            BottomNavItem.Projects,
            BottomNavItem.Chat
        )

        items.forEach { item ->
            val isSelected = when (item) {
                BottomNavItem.Friends -> currentRoute == item.route ||
                        currentRoute?.startsWith("Friend") == true

                BottomNavItem.Chat -> currentRoute == item.route ||
                        currentRoute?.startsWith("TextFriendScreen") == true

                BottomNavItem.Projects -> currentRoute == item.route ||
                        currentRoute?.startsWith("Project") == true

                else -> currentRoute == item.route
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = if (isSelected) item.filledIcon else item.outlinedIcon),
                        contentDescription = item.label
                    )
                }
            )
        }
    }
}