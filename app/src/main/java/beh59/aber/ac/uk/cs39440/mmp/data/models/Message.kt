package beh59.aber.ac.uk.cs39440.mmp.data.models

import androidx.annotation.Keep
import com.google.firebase.Timestamp

/**
 * Message
 * Represents an individual message in a wider conversation.
 * @param messageID The unique identifier of the message
 * @param senderID The unique identifier of the user sending the message
 * @param receiverID The unique identifier of the user receiving the message
 * @param content The content of the message itself
 * @param timestamp The time the message was sent
 * @param conversationID The unique identifier of the conversation the message is part of.
 */
@Keep
data class Message(
    val messageID: String = "",
    val senderID: String = "",
    val receiverID: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val conversationID: String = ""
)