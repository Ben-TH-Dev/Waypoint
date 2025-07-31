package beh59.aber.ac.uk.cs39440.mmp.data.repository

import beh59.aber.ac.uk.cs39440.mmp.data.models.Conversation
import beh59.aber.ac.uk.cs39440.mmp.data.models.Message
import beh59.aber.ac.uk.cs39440.mmp.data.models.states.ChatState
import kotlinx.coroutines.flow.StateFlow

/**
 * IChatRepository
 * An interface that serves as an abstraction for the methods required by the chatting systems
 * in the application (with friends, and in projects.)
 * @property chatState A StateFlow holding the state of data needed by chatting features that can
 * be observed in the UI
 */
interface IChatRepository {
    val chatState: StateFlow<ChatState>

    /**
     * createConversation
     * Defines a method that should create a conversation between two or more users.
     * @param participants A list of UIDs belonging to the members of the conversation
     * @param title The textual title of the conversation
     * @return The unique identifier of the new conversation, which is also its document ID in
     * Firestore
     */
    suspend fun createConversation(participants: List<String>, title: String): String

    /**
     * sendMessage
     * Defines a method that should write a user-inputted text message to Firestore
     * @param message The text message to save
     * @return True if successful, false otherwise
     */
    suspend fun sendMessage(message: Message): Boolean

    /**
     * loadConversations
     * Defines a method that should load conversations for a user given their unique identifier
     * @param userID The unique identifier of the user.
     */
    suspend fun loadConversations(userID: String)

    /**
     * loadMessages
     * Defines a method that should load messages for a specified conversation given its unique
     * identifier.
     * @param conversationID The unique identifier of the conversation.
     */
    suspend fun loadMessages(conversationID: String)

    /**
     * setCurrentConversation
     * Defines a method that sets a specified conversation as active for ease of access
     * @param conversation The conversation that the user has tapped on in the ChatOverview screen
     */
    fun setCurrentConversation(conversation: Conversation?)

    /**
     * clearMessages
     * Defines a method that should clear any current messages from state to ensure the UI is kept
     * up to date
     */
    fun clearMessages()

    /**
     * deleteConversation
     * Defines a method that should delete a conversation and any associated members and messages
     * @param conversation The conversation to delete
     */
    suspend fun deleteConversation(conversation: Conversation)

    /**
     * startMessageListener
     * Defines a method that should begin listening for changes to the relevant collections and
     * documents in Firestore and update state accordingly
     * @param conversationID The unique identifier of the conversation, used to narrow down the
     * specific document to listen to
     */
    fun startMessageListener(conversationID: String)
}