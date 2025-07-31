package beh59.aber.ac.uk.cs39440.mmp.ui.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import beh59.aber.ac.uk.cs39440.mmp.data.models.Friend
import beh59.aber.ac.uk.cs39440.mmp.data.models.User
import beh59.aber.ac.uk.cs39440.mmp.data.models.states.FriendState
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IFriendRepository
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IUserRepository
import beh59.aber.ac.uk.cs39440.mmp.domain.friend.GetFriendsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * FriendViewModel
 * ViewModel responsible for managing friend related operations and state.
 * @param userRepository Repository for user related operations
 * @param friendRepository Repository for friend related operations
 * @param getFriendsUseCase Use case for retrieving friends
 * @property uiState Exposes UI state from the repository to be used in the UI
 * @property currentUser Exposes the current user of the application from UserRepository
 */
@HiltViewModel
class FriendViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    private val friendRepository: IFriendRepository,
    private val getFriendsUseCase: GetFriendsUseCase
) : ViewModel() {
    val uiState: StateFlow<FriendState> = friendRepository.friendState

    private val currentUser get() = userRepository.userState.value.user

    init {
        refreshData()
    }

    /**
     * refreshData
     * Refreshes all friend-related data
     */
    fun refreshData() {
        viewModelScope.launch {
            loadFriends()
            loadFriendRequests()
            updateGroupedFriends()
        }
    }

    /**
     * loadFriends
     * Loads the current user's friends and assigns the value to the StateFlow localFriends
     */
    private fun loadFriends() {
        viewModelScope.launch {
            try {
                getFriendsUseCase(currentUser.uid)
            } catch (e: Exception) {
                Log.e("FriendViewModel", "Error loading friends", e)
            }
        }
    }

    /**
     * loadFriendRequests
     * Loads friend requests to view on the FriendSearchScreen
     */
    private fun loadFriendRequests() {
        viewModelScope.launch {
            try {
                val friendRequests = friendRepository.loadFriendRequests(currentUser)
                friendRepository.addToFriendRequestList(friendRequests)

                //Creates a list of User objects to hold the other information about the users
                //in a friend request. Retrieves cases where the active user is the receiver
                //or the sender and adds those user details to friendRequestUsers.
                val friendRequestUsers = mutableListOf<User>()

                for (friendRequest in friendRequests) {
                    val senderDetails = friendRepository.getFriendDetails(friendRequest.senderID)
                    val receiverDetails =
                        friendRepository.getFriendDetails(friendRequest.receiverID)

                    friendRequestUsers.add(senderDetails)
                    friendRequestUsers.add(receiverDetails)
                }

                //Returns only unique users from the list to the state of the StateFlow variable to
                //ensure that there are no duplicates of the user details.
                friendRepository.addToFriendRequestUsersList(friendRequestUsers.distinct())
            } catch (e: Exception) {
                Log.e("FriendViewModel", "Error loading friend requests and user details", e)
            }
        }
    }

    /**
     * sendFriendRequest
     * Sends a friend request to a user
     * @param receiverID The unique identifier of the user receiving the friend request
     */
    fun sendFriendRequest(receiverID: String) {
        viewModelScope.launch {
            //Result is true if friendRepository successfully sends a friend request to the specified
            //user and if anything goes wrong in the process, false is returned/
            val result = friendRepository.sendFriendRequest(currentUser.uid, receiverID)

            //If the friend request was sent successfully then the state of the hasSentRequest
            //StateFlow variable is set to true.
            if (result) {
                loadFriendRequests()
            }
        }
    }

    /**
     * acceptFriendRequest
     * Accepts a friend request between two users
     * @param user1uid The unique identifier of the user who sent the friend request
     * @param user2uid The unique identifier of the user who is receiving the friend request
     */
    fun acceptFriendRequest(user1uid: String, user2uid: String) {
        viewModelScope.launch {
            //Communicates with FriendRepository to accept the friend request, which initiates
            //a data transfer/creation in Firestore and handles both cases where the active user
            //is the sender or the receiver.
            friendRepository.acceptFriendRequest(user1uid, user2uid)

            //Ensures data is kept up to data due to the lack of database listeners
            refreshData()
        }
    }

    /**
     * rejectFriendRequest
     * Rejects a friend request between two users
     * @param user1uid The unique identifier of the user who sent the friend request
     * @param user2uid The unique identifier of the user who is receiving the friend request
     */
    fun rejectFriendRequest(user1uid: String, user2uid: String) {
        viewModelScope.launch {
            //Communicates with the FriendRepository to reject a friend request.
            friendRepository.rejectFriendRequest(user1uid, user2uid)

            //Ensures data is kept up to data due to the lack of database listeners
            refreshData()
        }
    }

    /**
     * beginSearch
     * Updates the UI state to signal that the app should show the search friends screen.
     */
    fun beginSearch() {
        viewModelScope.launch {
            //Updates the StateFlow variable to be observed.
            friendRepository.setShowSearchFriends(true)
        }
    }

    /**
     * endSearch
     * Updates state to end the friend search process.
     */
    fun endSearch() {
        viewModelScope.launch {
            friendRepository.setShowSearchFriends(false)
        }
    }

    /**
     * clearSearchResults
     * Clears the search results list.
     */
    fun clearSearchResults() {
        viewModelScope.launch {
            //Sets the state of the StateFlow variable to an empty list.
            friendRepository.setEmptySearchResults()
        }
    }

    /**
     * searchUsers
     * Searches for users based on a query and their username
     * @param query The search term to look for
     * @param localFriends The current list of friends to filter out from results
     */
    fun searchUsers(query: String, localFriends: List<Friend>) {
        viewModelScope.launch {
            friendRepository.searchUsers(query, localFriends, currentUser)
        }
    }

    /**
     * getFriendByID
     * Retrieves a friend by their unique identifier
     * @param friendID The unique identifier of the friend to retrieve
     * @return The friend object if found, null otherwise
     */
    fun getFriendByID(friendID: String?): Friend? {
        return friendRepository.getFriendByID(friendID)
    }

    /**
     * removeFriend
     * Removes a friend from the current user's friend list.
     * @param friend The friend object to remove
     */
    fun removeFriend(friend: Friend) {
        viewModelScope.launch {
            friendRepository.removeFriend(friend, currentUser)
            refreshData()
        }
    }

    /**
     * updateGroupedFriends
     * Updates the list of grouped friends in FriendRepository
     */
    private fun updateGroupedFriends() {
        viewModelScope.launch {
            friendRepository.updateGroupedFriends()
        }
    }

    /**
     * getFilteredGroupFriends
     * Retrieves a filtered and grouped list of friends based on a search query.
     * @param query The search query to filter friends by
     * @return A map of alphabetical headers to lists of friends, e.g D -> Daniel, Dave.
     */
    fun getFilteredGroupedFriends(query: String): Map<String, List<Friend>> {
        return friendRepository.getFilteredGroupedFriends(query)
    }

    /**
     * toggleViewMode
     * Toggles between list and grid view modes for the friends list.
     */
    fun toggleViewMode() {
        viewModelScope.launch {
            friendRepository.toggleViewMode()
        }
    }
}