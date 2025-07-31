package beh59.aber.ac.uk.cs39440.mmp.data.models

import androidx.annotation.Keep
import com.google.firebase.Timestamp

/**
 * ConversationMember
 * Represents and contains information about each user involved in a conversation
 * @param uid The unique identifier of each user provided by Firebase authentication
 * @param role An optional parameter that represents a user's role within a project for project
 * group chats.
 * @param conversationID The unique identifier of the conversation the ConversationMember is
 * involved in.
 * @param dateJoined The date this member was added to the conversation.
 */
@Keep
data class ConversationMember(
    val uid: String = "",
    val role: String = "",
    val conversationID: String = "",
    val dateJoined: Timestamp = Timestamp.now()
) 