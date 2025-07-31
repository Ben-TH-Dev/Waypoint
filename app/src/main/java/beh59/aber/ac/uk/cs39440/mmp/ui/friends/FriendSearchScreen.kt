package beh59.aber.ac.uk.cs39440.mmp.ui.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import beh59.aber.ac.uk.cs39440.mmp.R
import beh59.aber.ac.uk.cs39440.mmp.ui.components.IconButton
import beh59.aber.ac.uk.cs39440.mmp.ui.components.friends.FriendRequestItem
import beh59.aber.ac.uk.cs39440.mmp.ui.components.friends.SearchItem
import beh59.aber.ac.uk.cs39440.mmp.ui.theme.outlineVariantLight
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.UserViewModel
import kotlinx.coroutines.delay


/**
 * FriendSearchScreen
 * A composable that allows users to search for other users and manage friend requests.
 * @param userViewModel ViewModel used to access user related data
 * @param friendViewModel ViewModel used to manage friends and friend requests
 * @param navController Handles navigation between screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendSearchScreen(
    userViewModel: UserViewModel,
    friendViewModel: FriendViewModel,
    navController: NavController
) {
    //Observes the state of the list of Friend objects in the ViewModel, defaulting to an empty list.
    val friendState by friendViewModel.uiState.collectAsState()
    val localFriends = friendState.friends
    val searchResults = friendState.searchResults
    val friendRequests = friendState.friendRequests
    val userState by userViewModel.uiState.collectAsState()
    val currentUser = userState.user
    var query by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        if (query.length < 4) {
            friendViewModel.clearSearchResults()
        } else {
            //Prevents unnecessary calls to Firestore
            delay(100)
            friendViewModel.searchUsers(query, localFriends)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        TopAppBar(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            title = {
                //Interactive text field that will serve as the search bar and is styled like one.
                //Functionality needs updating to allow users to search for other user's usernames.
                TextField(
                    value = query,
                    onValueChange = { newQuery ->
                        query = newQuery
                        isSearching = newQuery.isNotEmpty()
                    },
                    placeholder = { Text(text = "Search...") },
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .clickable { isSearching = true },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            },
            actions = {
                if (isSearching) {
                    IconButton(
                        onClick = {
                            isSearching = false
                        },
                        iconId = R.drawable.close_24px,
                        contentDescription = "Settings Gear"
                    )
                } else {
                    IconButton(
                        onClick = {
                            //userViewModel.testWrite()
                            friendViewModel.endSearch()
                        },
                        iconId = R.drawable.close_24px,
                        contentDescription = "Settings Gear"
                    )
                }
            }
        )

        if (isSearching) {
            if (query.length < 4) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Type at least four characters to search.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else if (query.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Could not find a user with that username",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = outlineVariantLight
                )

                LazyColumn {
                    items(searchResults) { user ->
                        SearchItem(user, friendViewModel, navController)

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = outlineVariantLight
                        )
                    }
                }
            }
        } else {
            if (friendRequests.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "No friend requests yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = outlineVariantLight
                )

                LazyColumn {
                    items(friendRequests) { request ->
                        FriendRequestItem(request, currentUser, friendViewModel)

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = outlineVariantLight
                        )
                    }
                }
            }
        }
    }
}