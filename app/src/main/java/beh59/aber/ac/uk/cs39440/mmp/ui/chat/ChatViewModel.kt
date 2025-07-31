package beh59.aber.ac.uk.cs39440.mmp.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import beh59.aber.ac.uk.cs39440.mmp.data.models.Conversation
import beh59.aber.ac.uk.cs39440.mmp.data.models.Message
import beh59.aber.ac.uk.cs39440.mmp.data.models.User
import beh59.aber.ac.uk.cs39440.mmp.data.models.states.ChatState
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IChatRepository
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IUserRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ChatViewModel
 * Acts as a bridge between the repository which holds and performs operations on data and the
 * user interface for the chat systems of the application
 * @param chatRepository Repository that handles chat operations and data
 * @param userRepository Repository that handles user operations and data
 * @property uiState Exposes UI state from the repository to be used in the UI
 * @property currentUser Exposes the current user of the application from UserRepository
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: IChatRepository,
    private val userRepository: IUserRepository
) : ViewModel() {

    //Exposes the state of ChatState to the UI
    val uiState: StateFlow<ChatState> = chatRepository.chatState

    //Gets the current user from UserRepository
    val currentUser: User
        get() = userRepository.userState.value.user

    //Runs when ChatViewModel is created
    init {
        viewModelScope.launch {
            //Ensures that the current user is valid
            if (currentUser.uid.isNotEmpty()) {
                //Loads conversations that the current user is part of
                chatRepository.loadConversations(currentUser.uid)
            }
        }
    }

    /**
     * selectConversation
     * Sets the current active conversation and starts listening for messages
     * @param conversation The conversation to be set as active
     */
    fun selectConversation(conversation: Conversation) {
        viewModelScope.launch {
            chatRepository.setCurrentConversation(conversation)
            chatRepository.startMessageListener(conversation.conversationID)
        }
    }

    /**
     * sendMessage
     * Creates and sends a new message in the current conversation
     * @param content The text content of the message to be sent
     */
    fun sendMessage(content: String) {
        viewModelScope.launch {
            val conversation = uiState.value.currentConversation

            if (conversation != null && currentUser.uid.isNotEmpty() && content.isNotBlank()) {
                //Finds the other members in the conversation
                val otherMember = conversation.members.find {
                    it.uid != currentUser.uid
                }

                if (otherMember != null) {
                    val message = Message(
                        //Generates a random unique identifier
                        messageID = UUID.randomUUID().toString(),
                        senderID = currentUser.uid,
                        receiverID = otherMember.uid,
                        content = content,
                        timestamp = Timestamp.now(),
                        conversationID = conversation.conversationID
                    )

                    chatRepository.sendMessage(message)
                }
            }
        }
    }

    /**
     * refreshConversations
     * Manually refreshes the conversations list. Called when the chat overview screen is displayed
     * to ensure the most recent conversation data is shown
     */
    fun refreshConversations() {
        viewModelScope.launch {
            if (currentUser.uid.isNotEmpty()) {
                clearCurrentConversation()
                chatRepository.loadConversations(currentUser.uid)
            }
        }
    }

    /**
     * createConversation
     * Creates a new conversation with another user or selects an existing one
     * @param otherUserID The unique identifier of the other participant
     * @param onComplete Callback function that contains a boolean that can be accessed where
     * createConversation is called
     */
    fun createConversation(otherUserID: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                if (currentUser.uid.isNotEmpty()) {
                    //Check if a conversation already exists between the two involved users
                    val existingConversation = uiState.value.conversations.find { conversation ->
                        conversation.members.size == 2 &&
                                conversation.members.any { it.uid == currentUser.uid } &&
                                conversation.members.any { it.uid == otherUserID }
                    }

                    //If it does, it selects that conversation instead and ends the method with
                    //onComplete(true)
                    if (existingConversation != null) {
                        selectConversation(existingConversation)
                        onComplete(true)
                    } else {
                        //Otherwise, it creates the new conversation and then calls onComplete(true)
                        //here
                        val participants = listOf(currentUser.uid, otherUserID)
                        val title = "Chat between ${currentUser.uid} and $otherUserID"
                        chatRepository.createConversation(participants, title)

                        onComplete(true)
                    }
                } else {
                    //Doesn't create a conversation if the current user is not valid and calls
                    //onComplete(false)
                    onComplete(false)
                }
            } catch (e: Exception) {
                //If there are any problems, doesn't create a conversation and calls
                //onComplete(false)
                onComplete(false)
            }
        }
    }

    /**
     * clearCurrentConversation
     * Clears the current active conversation and its messages
     */
    fun clearCurrentConversation() {
        viewModelScope.launch {
            chatRepository.setCurrentConversation(null)
            chatRepository.clearMessages()
        }
    }

    /**
     * deleteConversation
     * Deletes a conversation and all its messages from Firestore
     * @param conversation The conversation to be deleted
     */
    fun deleteConversation(conversation: Conversation?) {
        viewModelScope.launch {
            if (conversation != null) {
                chatRepository.deleteConversation(conversation)
            }
        }
    }

    /**
     * isMessageFromCurrentUser
     * Checks if a message was sent by the current user
     * @param message The message to check
     * @return True if the message sender is the current user, false otherwise
     */
    fun isMessageFromCurrentUser(message: Message): Boolean {
        return message.senderID == currentUser.uid
    }

    /**
     * getOtherParticipantID
     * Gets the user ID of the other participant in a conversation
     * @param conversation The conversation to check
     * @return The user ID of the other participant, or null if not found
     */
    fun getOtherParticipantID(conversation: Conversation): String? {
        return conversation.members
            .firstOrNull { it.uid != currentUser.uid }
            ?.uid
    }
}