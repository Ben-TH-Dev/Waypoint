package beh59.aber.ac.uk.cs39440.mmp.data.models

import androidx.annotation.Keep
import com.google.firebase.Timestamp

/**
 * Conversation
 * Represents a group of texts between two or more users.
 * @param conversationID The unique identifier of each conversation that is the same as the document
 * id in the relevant document in Firestore.
 * @param title The title of the conversation which describes its purpose
 * @param lastMessageContent The last message sent in this conversation, used for display purposes
 * in the overview.
 * @param lastMessageTimestamp The time the last message was sent in this conversation, used for
 * display purposes in the overview.
 * @param createdAt The time the conversation itself was created.
 * @param members Each member involved in the conversation.
 */
@Keep
data class Conversation(
    val conversationID: String = "",
    val title: String = "",
    val lastMessageContent: String = "",
    val lastMessageTimestamp: Timestamp = Timestamp.now(),
    val createdAt: Timestamp = Timestamp.now(),
    val members: List<ConversationMember> = emptyList()
)