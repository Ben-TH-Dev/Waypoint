package beh59.aber.ac.uk.cs39440.mmp.data.models

import androidx.annotation.Keep

/**
 * FriendRequest
 * Represents a friend request, created when one user sends a friend request to another user.
 * @param id The unique identifier of the friend request.
 * @param senderID The unique UID of the user who sent the request.
 * @param receiverID The unique UID of the user who receives the request.
 * @param status The status of the friend request
 * @param dateAdded The date at which the friend request was sent
 */
@Keep
data class FriendRequest(
    val id: String = "",
    val senderID: String = "",
    val receiverID: String = "",
    val status: String = "",
    val dateAdded: String = ""
)