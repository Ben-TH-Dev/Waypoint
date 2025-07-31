package beh59.aber.ac.uk.cs39440.mmp.data.source.remote

import android.util.Log
import beh59.aber.ac.uk.cs39440.mmp.data.models.Friend
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * FriendDataSource
 * Handles direct communication with Firestore for friend-related data
 * Responsible for retrieving friendship information from the database
 * @param db An instance of Firebase Firestore
 */
class FriendDataSource @Inject constructor(
    private val db: FirebaseFirestore
) {
    /**
     * retrieveFriends
     * Retrieves a user's friends from Firestore by checking both the user1uid and user2uid fields
     * @param currentUserUID The unique identifier of the user
     * @return A list of Friend objects with populated data
     */
    @AddTrace(name = "friendDSRetrieveFriends")
    //Retrieves a list of instances of the Friend data class from Firestore.
    //This is quite inefficient and would be better with a batch query to Firestore.
    suspend fun retrieveFriends(currentUserUID: String): List<Friend> {
        //Initializes a list of instances of Friend to be populated by the method and returned.
        val friendsList = mutableListOf<Friend>()

        return try {
            //Runs on a background thread to prevent blocking the main thread with network delay.
            withContext(Dispatchers.IO) {
                //Accesses and creates a reference to the user's friends sub-collection in Firestore.
                val user1Query = db.collection("friends")
                    .whereEqualTo("user1uid", currentUserUID)
                    .get()
                    .await()

                val user2Query = db.collection("friends")
                    .whereEqualTo("user2uid", currentUserUID)
                    .get()
                    .await()

                //Iterates though the documents in the user's friends sub-collection.
                for (user1Results in user1Query.documents) {
                    try {
                        //Retrieves the dateBecameFriends field from the friends sub-collection of the
                        //Firestore. Defaults to unknown in case of failure.
                        val dateBecameFriends =
                            user1Results.getString("dateBecameFriends") ?: "Unknown"

                        val friendID = user1Results.getString("user2uid") ?: "Unknown"

                        //Retrieves all of the friend's information from their document in the users
                        //collection.
                        val friendRemote = db.collection("users").document(friendID).get().await()
                        //Maps the information retrieved from Firestore into the an object created
                        //from the data class Friend. If it fails for any reason, it moves onto the next
                        //friend in the list.
                        val friendData = friendRemote.toObject(Friend::class.java) ?: continue

                        //Queries Firestore to find the friend's username, checks if any of the
                        //documents in the usernames collection has a field UID that matches the
                        //friendID.
                        val usernameQuery = db.collection("usernames")
                            .whereEqualTo("uid", friendID)
                            .get()
                            .await()

                        //If there is any problems acquiring the friend's username the process is
                        //aborted and the system moves onto the next friend.
                        if (usernameQuery.isEmpty) {
                            Log.e("FriendDataSource", "Error retrieving friend's username")
                        } else {
                            //Appends the username and the date that users became friends to the Friend
                            //object created previously once all data has been retrieved and checked.
                            val localFriend = friendData.copy(
                                //Only one document should be returned by the query since each
                                //document's id is the username and its id retrieved with
                                //first because a list is returned by Firestore.
                                username = usernameQuery.documents.first().id,
                                friendsSince = dateBecameFriends,
                                phonenumber = friendRemote.getString("phonenumber") ?: ""
                            )

                            //Adds the instance of the Friend data class to the list.
                            friendsList.add(localFriend)
                        }

                    } catch (e: Exception) {
                        //If anything in the process fails, the error will be logged and the loop will
                        //move onto the next item.
                        Log.e("FriendDataSource", "Error retrieving friend", e)
                    }
                }

                for (user2Results in user2Query.documents) {
                    try {
                        //Retrieves the dateBecameFriends field from the friends sub-collection of the
                        //Firestore. Defaults to unknown in case of failure.
                        val dateBecameFriends =
                            user2Results.getString("dateBecameFriends") ?: "Unknown"

                        val friendID = user2Results.getString("user1uid") ?: "Unknown"

                        //Retrieves all of the friend's information from their document in the users
                        //collection.
                        val friendRemote = db.collection("users").document(friendID).get().await()
                        //Maps the information retrieved from Firestore into the an object created
                        //from the data class Friend. If it fails for any reason, it moves onto the next
                        //friend in the list.
                        val friendData = friendRemote.toObject(Friend::class.java) ?: continue

                        //Queries Firestore to find the friend's username, checks if any of the
                        //documents in the usernames collection has a field UID that matches the
                        //friendID.
                        val usernameQuery = db.collection("usernames")
                            .whereEqualTo("uid", friendID)
                            .get()
                            .await()

                        //If there is any problems acquiring the friend's username the process is
                        //aborted and the system moves onto the next friend.
                        if (usernameQuery.isEmpty) {
                            Log.e("FriendDataSource", "Error retrieving friend")
                        } else {
                            //Appends the username and the date that users became friends to the Friend
                            //object created previously once all data has been retrieved and checked.
                            val localFriend = friendData.copy(
                                //Only one document should be returned by the query since each
                                //document's id is the username and its id retrieved with
                                //first because a list is returned by Firestore.
                                username = usernameQuery.documents.first().id,
                                friendsSince = dateBecameFriends,
                                phonenumber = friendRemote.getString("phonenumber") ?: ""
                            )

                            //Adds the instance of the Friend data class to the list.
                            friendsList.add(localFriend)
                        }

                    } catch (e: Exception) {
                        //If anything in the process fails, the error will be logged and the loop will
                        //move onto the next item.
                        Log.e("FriendDataSource", "Error retrieving friend: ${e.message}", e)
                    }
                }

                //Submits the complete list of Friend objects created by the for loop.
                friendsList
            }
        } catch (e: Exception) {
            //If something causes the entire process to fail, an empty list will be returned,
            //which prevents the app from crashing.
            Log.e("FriendDataSource", "Error retrieving friends from Firestore")
            emptyList()
        }
    }
}