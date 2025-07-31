package beh59.aber.ac.uk.cs39440.mmp.data.models.states

import androidx.annotation.Keep
import beh59.aber.ac.uk.cs39440.mmp.data.models.Conversation
import beh59.aber.ac.uk.cs39440.mmp.data.models.Message

/**
 * ChatState
 * Represents the state of the chat functionality in the application
 * @param conversations List of all conversations the user is part of
 * @param currentConversation The conversation the user is currently viewing
 * @param messages List of messages in the current conversation
 * @param isLoading Indicates whether data is currently being loaded
 */
@Keep
data class ChatState(
    val conversations: List<Conversation> = emptyList(),
    val currentConversation: Conversation? = null,
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false
)