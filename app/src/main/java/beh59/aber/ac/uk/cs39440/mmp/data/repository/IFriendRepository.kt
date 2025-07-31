package beh59.aber.ac.uk.cs39440.mmp.data.repository

import beh59.aber.ac.uk.cs39440.mmp.data.models.Friend
import beh59.aber.ac.uk.cs39440.mmp.data.models.FriendRequest
import beh59.aber.ac.uk.cs39440.mmp.data.models.User
import beh59.aber.ac.uk.cs39440.mmp.data.models.states.FriendState
import kotlinx.coroutines.flow.StateFlow

/**
 * IFriendRepository
 * An interface that serves as an abstraction of the methods required for the friends system in the
 * application
 * @property friendState A StateFlow holding the state of data needed by friend features that can
 * be observed in the UI
 * @property groupedFriends A special StateFlow that holds a sorted group of friends to be used in
 * the UI
 */
interface IFriendRepository {
    val friendState: StateFlow<FriendState>
    val groupedFriends: StateFlow<Map<String, List<Friend>>>

    /**
     * getFriends
     * Defines a method that should retrieve friends for a given user and update state
     * @param currentUserUID The unique identifier of the user to retrieve friends for
     */
    suspend fun getFriends(currentUserUID: String)

    /**
     * addToFriendList
     * Defines a method that should update the friends list in state.
     * @param newFriends The new list of friends to hold in state
     */
    fun addToFriendList(newFriends: List<Friend>)

    /**
     * addToFriendRequestList
     * Defines a method that should update the friend request list in state
     * @param newRequests The new list of friend requests to hold in state
     */
    fun addToFriendRequestList(newRequests: List<FriendRequest>)

    /**
     * addToFriendRequestUsersList
     * Defines a method that should update the list of friend request users in state
     * @param newUsers The new list of friend request users to hold in state
     */
    fun addToFriendRequestUsersList(newUsers: List<User>)

    /**
     * addToSearchResultList
     * Defines a method that should update the list of search results in state
     * @param newResults The new list of search results to hold in state
     */
    fun addToSearchResultList(newResults: List<User>)

    /**
     * setEmptySearchResults
     * Defines a method that should empty the list of search results in state
     */
    fun setEmptySearchResults()

    /**
     * setShowSearchFriends
     * Defines a method that should update the showSearchFriends flag in state
     * @param status Whether showSearchFriends is set to true or false
     */
    fun setShowSearchFriends(status: Boolean)

    /**
     * sendFriendRequest
     * Defines a method that should send a friend request to a user from another user.
     * @param senderID The unique identifier of the user sending the request
     * @param receiverID The unique identifier of the user receiving the request
     * @return True if successful, false otherwise
     */
    suspend fun sendFriendRequest(senderID: String, receiverID: String): Boolean

    /**
     * loadFriendRequests
     * Defines a method that should load friend requests for a given user.
     * @param user The user to load the friend requests for
     * @return A list of FriendRequest objects
     */
    suspend fun loadFriendRequests(user: User): List<FriendRequest>

    /**
     * acceptFriendRequest
     * Defines a method that should accept an existing friend request given the unique identifiers
     * of both parties involved
     * @param user1uid The unique identifier of the user who sent the request
     * @param user2uid The unique identifier of the user receiving the request
     */
    suspend fun acceptFriendRequest(user1uid: String, user2uid: String)

    /**
     * rejectFriendRequest
     * Defines a method that should reject an existing friend request given the unique identifiers
     * of both parties involved
     * @param user1uid The unique identifier of the user who sent the request
     * @param user2uid The unique identifier of the user receiving the request
     */
    suspend fun rejectFriendRequest(user1uid: String, user2uid: String)

    /**
     * getFriendDetails
     * Defines a method that should retrieve information about a friend given their unique
     * identifier
     * @param uid The unique identifier of the user to fetch information about
     * @return A User object containing the friend's details
     */
    suspend fun getFriendDetails(uid: String): User

    /**
     * searchUsers
     * Defines a method that should allow the user to search for other users of the application to
     * add as friends
     * @param query The user input that we use to query the database for other users. Currently
     * based on username data
     * @param localFriends A list of users that the current user is already friends with. This
     * prevents them from appearing in the search results
     * @param activeUser The currently active user for ease of access and filtering purposes
     * @return A list of users who fit the user's search criteria
     */
    suspend fun searchUsers(query: String, localFriends: List<Friend>, activeUser: User): List<User>

    /**
     * getFriendByID
     * Defines a method that should return a Friend object with valid data given the friend's
     * unique identifier
     * @param friendID The unique identifier of the friend to retrieve
     * @return A populated Friend object if successful and null if unsuccessful
     */
    fun getFriendByID(friendID: String?): Friend?

    /**
     * removeFriend
     * Defines a method that should allow the current user to remove an existing friend
     * @param friend The friend to remove
     * @param user The current user
     */
    suspend fun removeFriend(friend: Friend, user: User)

    /** updateGroupedFriends
     * Defines a method that is responsible for updating the list of grouped friends and updating
     * state with it. The grouped friends are used to display an alphabetically sorted list in the
     * UI
     */
    fun updateGroupedFriends()

    /**
     * getFilteredGroupedFriends
     * Defines a method that should filter and then group a list of friends for usage in the UI
     * when the user searches through their existing friends
     * @param query The user's search query
     * @return A map containing each unique first letter of the friends and for each a list of
     * friends whose displayName contains the query.
     * e.g query of 'a'
     * B -> Barney, Brandon
     * D -> Daniel
     */
    fun getFilteredGroupedFriends(query: String): Map<String, List<Friend>>

    /**
     * toggleViewMode
     * Defines a method that should switch between the two different viewmodes which use an enum
     * defined in Enums.kt
     */
    fun toggleViewMode()
}