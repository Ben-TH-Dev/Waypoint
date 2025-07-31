package beh59.aber.ac.uk.cs39440.mmp.ui.friends

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import beh59.aber.ac.uk.cs39440.mmp.R
import beh59.aber.ac.uk.cs39440.mmp.data.models.OptionItem
import beh59.aber.ac.uk.cs39440.mmp.ui.components.AlphabetHeader
import beh59.aber.ac.uk.cs39440.mmp.ui.components.IconButton
import beh59.aber.ac.uk.cs39440.mmp.ui.components.OptionsMenu
import beh59.aber.ac.uk.cs39440.mmp.ui.components.friends.FriendItem
import beh59.aber.ac.uk.cs39440.mmp.ui.components.friends.FriendListItem
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.UserViewModel
import beh59.aber.ac.uk.cs39440.mmp.utils.ViewMode

/**
 * FriendsOverview
 * A composable that displays an overview of the user's friends.
 * @param userViewModel ViewModel used to access user related data
 * @param friendViewModel ViewModel used to manage friends and friend related operations
 * @param navController Handles navigation between screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsOverview(
    navController: NavController
) {
    val friendViewModel: FriendViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    //Observes the state of the list of Friend objects in the ViewModel, defaulting to an empty list.
    val friendState by friendViewModel.uiState.collectAsState()
    val showSearchFriends = friendState.showSearchFriends
    val viewMode = friendState.viewMode

    var query by remember { mutableStateOf("") }

    val filteredGroupedFriends = remember(query) {
        friendViewModel.getFilteredGroupedFriends(query)
    }

    if (showSearchFriends) {
        FriendSearchScreen(
            userViewModel,
            friendViewModel,
            navController
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopAppBar(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface),
                title = {
                    //Interactive text field that will serve as the search bar and is styled like
                    //one. Functionality needs updating to allow users to search for other user's
                    //usernames.
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text(text = "Search for friends or users") },
                        modifier = Modifier
                            .fillMaxWidth(0.96f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(50.dp)),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            friendViewModel.beginSearch()
                        },
                        iconId = R.drawable.filled_person_add_24px,
                        contentDescription = "Add Friend"
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OptionsMenu(
                        options = listOf(
                            OptionItem(
                                text = if (viewMode == ViewMode.GRID) "List View" else "Grid View",
                                iconId = if (viewMode == ViewMode.GRID)
                                    R.drawable.generic_avatar else R.drawable.generic_avatar,
                                onClick = { friendViewModel.toggleViewMode() }
                            )
                        )
                    )
                }
            )

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outline
            )

            if (filteredGroupedFriends.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No friends available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                when (viewMode) {
                    ViewMode.GRID -> {
                        LazyColumn {
                            filteredGroupedFriends.forEach { (letter, friends) ->
                                item {
                                    AlphabetHeader(letter)
                                }

                                items(friends) { friend ->
                                    FriendItem(friend, navController)
                                }
                            }
                        }
                    }

                    ViewMode.LIST -> {
                        LazyColumn {
                            filteredGroupedFriends.forEach { (letter, friends) ->
                                item {
                                    AlphabetHeader(letter)
                                }

                                items(friends) { friend ->
                                    FriendListItem(friend, navController)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun FriendsOverviewPreview() {
    val navController = rememberNavController()

    FriendsOverview(navController)
}