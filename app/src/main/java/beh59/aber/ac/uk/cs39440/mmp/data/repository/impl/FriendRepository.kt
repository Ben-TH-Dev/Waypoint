package beh59.aber.ac.uk.cs39440.mmp.data.repository.impl

import android.util.Log
import beh59.aber.ac.uk.cs39440.mmp.data.models.Friend
import beh59.aber.ac.uk.cs39440.mmp.data.models.FriendRequest
import beh59.aber.ac.uk.cs39440.mmp.data.models.User
import beh59.aber.ac.uk.cs39440.mmp.data.models.states.FriendState
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IFriendRepository
import beh59.aber.ac.uk.cs39440.mmp.data.source.remote.FriendDataSource
import beh59.aber.ac.uk.cs39440.mmp.utils.ViewMode
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * FriendRepository
 * An implementation of the IFriendRepository interface and its required methods
 * Handles the business logic related to managing friends, friend requests, and user searches
 * @param source Handles retrieval of friend-related data from Firestore
 * @param db The Firestore instance used for database operations
 * @property friendState Exposes ChatState and its data to the UI
 * @property groupedFriends Exposes a grouped list of friends containing the current friends list
 * grouped alphabetically by display name for UI display.
 */
class FriendRepository @Inject constructor(
    private val source: FriendDataSource,
    private val db: FirebaseFirestore
) : IFriendRepository {
    private val _friendState = MutableStateFlow(FriendState())
    override val friendState: StateFlow<FriendState> = _friendState.asStateFlow()

    private val _groupedFriends = MutableStateFlow<Map<String, List<Friend>>>(emptyMap())
    override val groupedFriends: StateFlow<Map<String, List<Friend>>> =
        _groupedFriends.asStateFlow()

    /**
     * getFriends
     * Communicates with FriendDataSource to retrieve a list of friends for the specified user.
     * Updates the state with the retrieved data
     * @param currentUserUID The unique identifier of the user whose friends are to be fetched.
     */
    override suspend fun getFriends(currentUserUID: String) {
        try {
            val newFriends = source.retrieveFriends(currentUserUID)

            _friendState.value = _friendState.value.copy(
                friends = newFriends
            )
        } catch (e: Exception) {
            Log.e("FriendRepository", "Error retrieving friends", e)
        }
    }

    /**
     * addToFriendList
     * Updates the friends list in the state with the new list.
     * @param newFriends The new list of Friend objects to set in the state.
     */
    override fun addToFriendList(newFriends: List<Friend>) {
        _friendState.value = _friendState.value.copy(
            friends = newFriends
        )
    }

    /**
     * addToFriendRequestList
     * Updates the friend request list in state with the new list.
     * @param newRequests The new list of FriendRequest objects to set in the state.
     */
    override fun addToFriendRequestList(newRequests: List<FriendRequest>) {
        _friendState.value = _friendState.value.copy(
            friendRequests = newRequests
        )
    }

    /**
     * addToFriendRequestUsersList
     * Updates the list of users associated with friend requests in state.
     * @param newUsers The new list of User objects to store in state
     */
    override fun addToFriendRequestUsersList(newUsers: List<User>) {
        _friendState.value = _friendState.value.copy(
            friendRequestUsers = newUsers
        )
    }

    /**
     * addToSearchResultList
     * Updates the user search results list in state.
     * @param newResults The new list of User objects found during a search to hold in state.
     */
    override fun addToSearchResultList(newResults: List<User>) {
        _friendState.value = _friendState.value.copy(
            searchResults = newResults
        )
    }

    /**
     * setEmptySearchResults
     * Clears the user search results list in state.
     */
    override fun setEmptySearchResults() {
        _friendState.value = _friendState.value.copy(
            searchResults = emptyList()
        )
    }

    /**
     * setShowSearchFriends
     * Updates the flag in state indicating whether the friend search UI should be shown.
     * @param status True if the user is currently searching, false if not
     */
    override fun setShowSearchFriends(status: Boolean) {
        _friendState.value = _friendState.value.copy(
            showSearchFriends = status
        )
    }

    /**
     * sendFriendRequest
     * Sends a friend request from the sender to the receiver and handles related calls to
     * Firestore.
     * @param senderID The UID of the user sending the request.
     * @param receiverID The UID of the user receiving the request.
     * @return `true` if the request was successfully written to Firestore, `false` if there are
     * any problems
     */
    @AddTrace(name = "friendRepoSendFriendRequest")
    override suspend fun sendFriendRequest(senderID: String, receiverID: String): Boolean {
        return try {
            //Maps each field of the Firestore document to each value given
            val friendRequest = hashMapOf(
                "senderID" to senderID,
                "receiverID" to receiverID,
                "status" to "pending",
                "dateAdded" to System.currentTimeMillis().toString()
            )

            //Runs on a background thread during network calls to prevent blocking up the main
            withContext(Dispatchers.IO) {
                //Writes the friend request to Firestore.
                db.collection("friendRequests")
                    .add(friendRequest)
                    .await()
            }

            //Returns true to signal that the operation was successful.
            true
        } catch (e: Exception) {
            Log.e("FriendRepository", "Error sending friend request")
            //Returns false to signal that the operation was a failure.
            false
        }
    }

    /**
     * loadFriendRequests
     * Loads friend requests for the given user from Firestore. It queries for cases where the user
     * is both the sender and the receiver.
     * @param user The User object for whom to load friend requests.
     * @return A list containing both sent and received FriendRequest objects. Returns an empty list
     * if any errors occurs.
     */
    @AddTrace(name = "friendRepoLoadFriendRequests")
    override suspend fun loadFriendRequests(user: User): List<FriendRequest> {
        //Initializes an empty mutableList
        val friendRequestList = mutableListOf<FriendRequest>()

        //Runs on a background thread to prevent blocking up the main
        return try {
            withContext(Dispatchers.IO) {
                //Queries for friend requests where the current user is the sender
                val friendRequestSentQuery = db.collection("friendRequests")
                    .whereEqualTo("senderID", user.uid)
                    .whereEqualTo("status", "pending")
                    .get()
                    .await()

                //Queries for friend requests where the current user is the receiver
                val friendRequestReceivedQuery = db.collection("friendRequests")
                    .whereEqualTo("receiverID", user.uid)
                    .whereEqualTo("status", "pending")
                    .get()
                    .await()

                //Goes through each of the documents returned by the first query and extracts the
                //details given, creating a FriendRequest object from them.
                friendRequestSentQuery.documents.forEach { document ->
                    val senderID = document.getString("senderID") ?: ""
                    val receiverID = document.getString("receiverID") ?: ""
                    val status = document.getString("status") ?: ""
                    val dateAdded = document.getString("dateAdded") ?: ""

                    val friendRequest = FriendRequest(
                        id = document.id,
                        senderID = senderID,
                        receiverID = receiverID,
                        status = status,
                        dateAdded = dateAdded
                    )

                    //Adds the FriendRequest object to the list.
                    friendRequestList.add(friendRequest)
                }

                //Goes through each of the documents returned by the second query and extracts the
                //details given, creating a FriendRequest object from them.
                friendRequestReceivedQuery.documents.forEach { document ->
                    val senderID = document.getString("senderID") ?: ""
                    val receiverID = document.getString("receiverID") ?: ""
                    val status = document.getString("status") ?: ""
                    val dateAdded = document.getString("dateAdded") ?: ""

                    val friendRequest = FriendRequest(
                        id = document.id,
                        senderID = senderID,
                        receiverID = receiverID,
                        status = status,
                        dateAdded = dateAdded
                    )

                    //Add received request to the list if it's not already in the sent requests list
                    if (friendRequestList.none { it.id == friendRequest.id }) {
                        friendRequestList.add(friendRequest)
                    }
                }

                //Returns the complete FriendRequest list with both sent and received requests
                //and their distinction
                friendRequestList
            }
        } catch (e: Exception) {
            Log.e("FriendRepository", "Error retrieving friend requests")
            //Returns an empty list if there are any problems.
            emptyList()
        }
    }

    /**
     * acceptFriendRequest
     * Accepts a friend request between two users and handles the related Firestore calls
     * @param user1uid The unique identifier of the user who sent the request
     * @param user2uid The unique identifier of the user who received the request
     */
    @AddTrace(name = "friendRepoAcceptFriendRequest")
    override suspend fun acceptFriendRequest(user1uid: String, user2uid: String) {
        //Acquires the friends collection
        val friendsRef = try {
            db.collection("friends")
                .get()
                .await()
        } catch (e: Exception) {
            Log.e(
                "FriendRepository",
                "Error retrieving friends collection to accept friend request"
            )
        }

        if (friendsRef != null) {
            val friendshipData = mapOf(
                "user1uid" to user1uid,
                "user2uid" to user2uid,
                "dateBecameFriends" to FieldValue.serverTimestamp().toString()
            )

            try {
                withContext(Dispatchers.IO) {
                    db.collection("friends")
                        .add(friendshipData)
                        .await()
                }
            } catch (e: Exception) {
                Log.e("FriendRepository", "Error creating friendship", e)
            }
        }

        //Checks for both combinations, user1uid as sender and user2uid as receiver and vice versa.
        //It returns query 1 if the first query is a success or query 2 if query 1 is empty.
        val friendRequest = try {
            withContext(Dispatchers.IO) {
                val query1 = db.collection("friendRequests")
                    .whereEqualTo("senderID", user1uid)
                    .whereEqualTo("receiverID", user2uid)
                    .get()
                    .await()

                if (query1.isEmpty) {
                    db.collection("friendRequests")
                        .whereEqualTo("senderID", user2uid)
                        .whereEqualTo("receiverID", user1uid)
                        .get()
                        .await()
                } else {
                    query1
                }
            }
        } catch (e: Exception) {
            Log.e("FriendRepository", "Error retrieving friend request for deletion", e)
            null
        }

        //Validates that the document exists before proceeding with the database operation.
        if (friendRequest != null && !friendRequest.isEmpty) {
            //Firestore returns a list of the documents matching the conditions and we iterate
            //through these.
            friendRequest.documents.forEach { document ->
                try {
                    //Deletes any documents matching the conditional logic used to initialize
                    //friendRequest.
                    withContext(Dispatchers.IO) {
                        db.collection("friendRequests")
                            .document(document.id)
                            .delete()
                            .await()
                    }
                } catch (e: Exception) {
                    Log.e("FriendRepository", "Error deleting friend request", e)
                }
            }
        } else {
            Log.d("FriendRepository", "No matching friend request found")
        }
    }

    /**
     * rejectFriendRequest
     * Rejects a friend request between two users by finding and deleting the corresponding
     * document from the `friendRequests` collection in Firestore. Works both for the receiver
     * rejecting a request, and the sender cancelling one.
     * @param user1uid The unique identifier of the user who sent the request
     * @param user2uid The unique identifier of the user who received the request
     */
    @AddTrace(name = "friendRepoRejectFriendRequest")
    override suspend fun rejectFriendRequest(user1uid: String, user2uid: String) {
        val friendRequest = try {
            withContext(Dispatchers.IO) {
                //Queries firestore for the case where user1 is the sender
                val query1 = db.collection("friendRequests")
                    .whereEqualTo("senderID", user1uid)
                    .whereEqualTo("receiverID", user2uid)
                    .get()
                    .await()

                //Queries Firestore for the case where user2 is the sender
                if (query1.isEmpty) {
                    db.collection("friendRequests")
                        .whereEqualTo("senderID", user2uid)
                        .whereEqualTo("receiverID", user1uid)
                        .get()
                        .await()
                } else {
                    //If the conditional logic of if (query1.isEmpty) fails then the results of
                    //query1 are assigned to the val friendRequest. Otherwise, the other conditional
                    //block is applied.
                    query1
                }
            }
        } catch (e: Exception) {
            Log.e("FriendRepository", "Error retrieving friend request for deletion", e)
            null
        }

        //Ensures the document exists before proceeding with the database operation.
        if (friendRequest != null && !friendRequest.isEmpty) {
            //Firestore returns a list of the documents matching the conditions and we iterate
            //through these.
            friendRequest.documents.forEach { document ->
                try {
                    //Deletes any documents matching the conditional logic used to initialize
                    //friendRequest.
                    withContext(Dispatchers.IO) {
                        db.collection("friendRequests")
                            .document(document.id)
                            .delete()
                            .await()

                        Log.d(
                            "FriendRepository",
                            "Friend request ${document.id} deleted successfully"
                        )
                    }
                } catch (e: Exception) {
                    Log.e("FriendRepository", "Error deleting friend request ${document.id}", e)
                }
            }
        } else {
            Log.d(
                "FriendRepository",
                "No matching friend request found for $user1uid and $user2uid"
            )
        }
    }

    /**
     * getFriendDetails
     * Retrieves the user details for a given friend UID via communication with FriendDataSource.
     * @param uid The unique identifier of the friend whose details are being retrieved
     * @return A User object containing the friend's details.
     */
    @AddTrace(name = "friendRepoGetFriendDetails")
    override suspend fun getFriendDetails(uid: String): User {
        return try {
            //Runs on the background thread whilst doing queries to the Firestore to prevent
            //blocking up the main thread
            withContext(Dispatchers.IO) {
                //Acquires a reference to the user's document in the users collection
                val userLocation = db.collection("users").document(uid).get().await()
                //If the process fails, return a default user.
                val userData = userLocation.toObject(User::class.java) ?: User(uid, "", "", "", "")

                //Searches the username collection and looks for any documents whose UID field
                //matches the user's UID.
                val usernameQuery = db.collection("usernames")
                    .whereEqualTo("uid", uid)
                    .get()
                    .await()

                //Appends the retrieved username to the instance of User, if it exists.
                userData.copy(username = if (usernameQuery.isEmpty) "" else usernameQuery.documents.first().id)
            }
        } catch (e: Exception) {
            Log.e("FriendRepository", "Error getting user details", e)
            User(uid, "Unknown", "unknown@example.com", "", "")
        }
    }

    /**
     * searchUsers
     * Searches for users in Firestore based on a user-inputted string that matches against user's
     * display names
     * @param query The user input entered in the search
     * @param localFriends A list of the active user's current friends
     * @param activeUser The currently user performing the search
     * @return A list of User objects matching the query. Returns an empty list if an error occurs.
     */
    @AddTrace(name = "friendRepoSearchUsers")
    override suspend fun searchUsers(
        query: String,
        localFriends: List<Friend>,
        activeUser: User
        //Runs on a background thread to prevent UI lag when searching for users
    ): List<User> = withContext(Dispatchers.IO) {
        //If the query is too small, nothing is returned. Usernames must be over 4 characters.
        if (query.length < 4) {
            return@withContext emptyList()
        }

        //Acquires a snapshot of the usernames collection
        val querySnapshot = try {
            db.collection("usernames")
                .get()
                .await()
        } catch (e: Exception) {
            Log.e("FriendRepository", "Error querying usernames", e)
            return@withContext emptyList()
        }

        //Filters through the results of the lookup by finding document ids that start with the
        //same characters as the input query entered into the search bar.
        val users = querySnapshot.documents
            .filter { document -> document.id.startsWith(query, ignoreCase = true) }

        //Fetches some of that user's details to display back to the current user.
        try {
            val userList = mutableListOf<User>()

            for (document in users) {
                val uid = document.getString("uid") ?: continue
                val userDetails = getFriendDetails(uid)

                if (userDetails.uid.isNotEmpty() && !localFriends.any { it.uid == userDetails.uid } && userDetails.uid != activeUser.uid) {
                    userList.add(userDetails)
                }
            }

            //Updates state with the results of the search
            _friendState.value = _friendState.value.copy(searchResults = userList)

            //Returns a list of the results
            userList
        } catch (e: Exception) {
            //If there any any problems, logs the errors and returns an empty list
            Log.e("FriendRepository", "Error fetching user details for search", e)
            emptyList()
        }
    }

    /**
     * getFriendByID
     * Retrieves a specific friend from the current friends list stored in state.
     * @param friendID The unique identifier of the friend to find.
     * @return If successful, the Friend object with the same UID is returned. Otherwise, null is
     * returned
     */
    override fun getFriendByID(friendID: String?): Friend? {
        return if (friendID == null) {
            null
        } else {
            _friendState.value.friends.find { it.uid == friendID }
        }
    }

    /**
     * removeFriend
     * Removes a friendship between the given user and friend and handles related Firestore calls
     * @param friend The Friend object representing the friendship to remove.
     * @param user The User object representing the current user.
     */
    @AddTrace(name = "friendRepoRemoveFriend")
    override suspend fun removeFriend(friend: Friend, user: User) {
        Log.d("FriendRepository", "Attempting to remove user as friend")

        val friendshipDocuments = try {
            withContext(Dispatchers.IO) {
                //Queries firestore for the case where user1 is the sender
                val senderOrReceiverQuery = db.collection("friends")
                    .whereEqualTo("user1uid", friend.uid)
                    .whereEqualTo("user2uid", user.uid)
                    .get()
                    .await()

                //Queries Firestore for the case where user2 is the sender
                if (senderOrReceiverQuery.isEmpty) {
                    db.collection("friends")
                        .whereEqualTo("user1uid", user.uid)
                        .whereEqualTo("user2uid", friend.uid)
                        .get()
                        .await()
                } else {
                    //If the conditional logic of if (query1.isEmpty) fails then the results of
                    //query1 are assigned to the val friendRequest. Otherwise, the other conditional
                    //block is applied.
                    senderOrReceiverQuery
                }
            }
        } catch (e: Exception) {
            Log.e("FriendRepository", "Error retrieving friend document for deletion", e)
            null
        }

        //Validates that the document exists before proceeding with the database operation.
        if (friendshipDocuments != null && !friendshipDocuments.isEmpty) {
            //Firestore returns a list of the documents matching the conditions and we iterate
            //through these.
            friendshipDocuments.documents.forEach { document ->
                try {
                    //Deletes any documents matching the conditional logic used to initialize
                    //friendRequest.
                    withContext(Dispatchers.IO) {
                        db.collection("friends")
                            .document(document.id)
                            .delete()
                            .await()

                        Log.d("FriendRepository", "Friendship ${document.id} deleted successfully")
                    }
                } catch (e: Exception) {
                    Log.e("FriendRepository", "Error deleting friendship ${document.id}", e)
                }
            }
        } else {
            Log.d("FriendRepository", "No matching friend found")
        }

        val updatedFriends = _friendState.value.friends.filter { it.uid != friend.uid }
        _friendState.value = _friendState.value.copy(friends = updatedFriends)
    }

    /**
     * updateGroupedFriends
     * Updates the groupedFriends StateFlow by sorting the current friends list alphabetically by
     * `displayName` and grouping them by the first letter.
     */
    override fun updateGroupedFriends() {
        val friends = _friendState.value.friends
        val grouped = friends
            .sortedBy { it.displayName }
            .groupBy { it.displayName.first().uppercaseChar().toString() }
        _groupedFriends.value = grouped
    }

    /**
     * getFilteredGroupedFriends
     * Filters the current friends list based on the search query matching against displayName.
     * Then, it groups the filtered results alphabetically by the first letter of the displayName
     * @param query The search string to filter friends by.
     * @return A map where keys are uppercase first letters and values are lists of Friend objects
     * matching the query and starting with that letter.
     */
    override fun getFilteredGroupedFriends(query: String): Map<String, List<Friend>> {
        val filteredFriends = _friendState.value.friends.filter {
            it.displayName.contains(query, ignoreCase = true)
        }

        return filteredFriends
            .sortedBy { it.displayName }
            .groupBy { it.displayName.first().uppercaseChar().toString() }
    }

    /**
     * toggleViewMode
     * Switches the view mode stored in the state
     */
    override fun toggleViewMode() {
        val currentViewMode = _friendState.value.viewMode
        _friendState.value = _friendState.value.copy(
            viewMode = if (currentViewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
        )
    }
}