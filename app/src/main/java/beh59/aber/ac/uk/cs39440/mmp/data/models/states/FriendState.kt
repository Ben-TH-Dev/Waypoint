package beh59.aber.ac.uk.cs39440.mmp.data.models.states

import androidx.annotation.Keep
import beh59.aber.ac.uk.cs39440.mmp.data.models.Friend
import beh59.aber.ac.uk.cs39440.mmp.data.models.FriendRequest
import beh59.aber.ac.uk.cs39440.mmp.data.models.User
import beh59.aber.ac.uk.cs39440.mmp.utils.ViewMode

/**
 * FriendState
 * Represents the state of the friends system in the application
 * @param friends List of all the user's friends
 * @param friendRequests List of all friend requests
 * @param friendRequestUsers List of users who have sent the current user a friend request
 * @param isLoading Tracks whether friend data is being loaded or not
 * @param showSearchFriends Tracks if the friend search UI is active
 * @param searchResults List of users found when searching for new friends
 * @param viewMode The current view mode for displaying friends
 */
@Keep
data class FriendState(
    val friends: List<Friend> = emptyList(),
    val friendRequests: List<FriendRequest> = emptyList(),
    val friendRequestUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val showSearchFriends: Boolean = false,
    val searchResults: List<User> = emptyList(),
    val viewMode: ViewMode = ViewMode.GRID
)
