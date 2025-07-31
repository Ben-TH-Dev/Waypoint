package beh59.aber.ac.uk.cs39440.mmp.data.source.remote

import android.util.Log
import beh59.aber.ac.uk.cs39440.mmp.data.models.Conversation
import beh59.aber.ac.uk.cs39440.mmp.data.models.ConversationMember
import beh59.aber.ac.uk.cs39440.mmp.data.models.Message
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * ChatDataSource
 * Handles direct communication with Firestore for chat-related data
 * Responsible for retrieving conversations, messages, and setting up real-time
 * listeners for message updates
 * @param db A Firebase Firestore instance
 */
class ChatDataSource @Inject constructor(
    private val db: FirebaseFirestore
) {
    /**
     * retrieveConversations
     * Retrieves conversations that the user is a member of from Firestore
     * @param currentUserUID The unique identifier of the user to retrieve conversations for
     * @return A list of Conversation objects sorted by most recent message
     */
    @AddTrace(name = "chatDSRetrieveConversations")
    suspend fun retrieveConversations(currentUserUID: String): List<Conversation> {
        val conversationList = mutableListOf<Conversation>()

        //Queries the separate conversation-memberships collection and finds documents containing
        //the given current user UID
        val membershipQuery = db.collection("conversation-memberships")
            .whereEqualTo("uid", currentUserUID)
            .get()
            .await()

        //Iterates through each of the returned membership documents from Firestore
        for (membership in membershipQuery.documents) {
            try {
                //Gets the conversation ID from the membership document to lookup the actual conversation
                val conversationID = membership.getString("conversationID")

                if (conversationID != null) {
                    //Queries the conversations collection for a document with the ID found in the
                    //previous step
                    val conversationDetails = db.collection("conversations")
                        .document(conversationID)
                        .get()
                        .await()

                    //If the document exists, we find each member of the conversation and map
                    //their details to a ConversationMember object if they are not null
                    if (conversationDetails.exists()) {
                        val membersQuery = db.collection("conversation-memberships")
                            .whereEqualTo("conversationID", conversationID)
                            .get()
                            .await()

                        val members = membersQuery.documents.mapNotNull { memberDocument ->
                            val uid = memberDocument.getString("uid") ?: return@mapNotNull null
                            val memberRole = memberDocument.getString("role") ?: "member"
                            val dateJoined = memberDocument.getTimestamp("dateJoined")

                            ConversationMember(
                                uid = uid,
                                role = memberRole,
                                conversationID = conversationID,
                                dateJoined = dateJoined ?: Timestamp.now()
                            )
                        }

                        //Creates a complete Conversation object with the data that has been
                        //retrieved and adds it to the list of conversations before iterating over
                        //the next membership document.
                        val conversation = Conversation(
                            conversationID = conversationID,
                            title = conversationDetails.getString("title") ?: "",
                            lastMessageContent = conversationDetails.getString("lastMessageContent")
                                ?: "",
                            lastMessageTimestamp = conversationDetails.getTimestamp("lastMessageTimestamp")
                                ?: Timestamp.now(),
                            createdAt = conversationDetails.getTimestamp("createdAt")
                                ?: Timestamp.now(),
                            members = members
                        )

                        conversationList.add(conversation)
                    } else {
                        Log.d("ChatDataSource", "Error retrieving conversation document")
                    }
                }
            } catch (e: Exception) {
                Log.d("ChatDataSource", "Error retrieving individual conversation", e)
            }
        }

        //Sorts the retrieved list by the most recent message using the lastMessageTimestamp field
        return conversationList.sortedByDescending { it.lastMessageTimestamp }
    }

    /**
     * retrieveMessages
     * Retrieves messages for a specific conversation from Firestore
     * @param conversationID The unique identifier of the conversation the messages belong to
     * @return A list of Message objects associated with that conversation
     */
    @AddTrace(name = "chatDSRetrieveMessages")
    suspend fun retrieveMessages(conversationID: String): List<Message> {
        val messagesList = mutableListOf<Message>()

        try {
            //Queries the messages collection for any documents that contain a matching
            //conversationID field to the value passed as a parameter. Orders them via timestamp
            val messagesQuery = db.collection("messages")
                .whereEqualTo("conversationID", conversationID)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            //Iterates through the list of documents returned by the query and captures their
            //values before creating an instance of Message with those details
            for (messageDocument in messagesQuery.documents) {
                val messageID = messageDocument.id
                val senderID = messageDocument.getString("senderID") ?: ""
                val receiverID = messageDocument.getString("receiverID") ?: ""
                val content = messageDocument.getString("content") ?: ""
                val timestamp = messageDocument.getTimestamp("timestamp") ?: Timestamp.now()

                val message = Message(
                    messageID = messageID,
                    senderID = senderID,
                    receiverID = receiverID,
                    content = content,
                    timestamp = timestamp,
                    conversationID = conversationID
                )

                //Adds the instance of message to the list of messages before iterating to the next
                //one
                messagesList.add(message)
            }
        } catch (e: Exception) {
            //If there are any problems, log the error
            Log.e("ChatDataSource", "Error retrieving messages", e)
        }

        //Return the populated messagesList
        return messagesList
    }

    /**
     * listenForMessages
     * Sets up a real-time listener for messages in a conversation
     * @param conversationID The unique identifier of the conversation to listen to
     * @param onMessagesUpdate Function that will be called whenever new messages are received
     */
    fun listenForMessages(
        conversationID: String,
        onMessagesUpdate: (List<Message>) -> Unit
    ) {
        //Listens to documents in the messages collection only where the conversationID field of the
        //document is the same as the one given as the conversationID parameter. Orders the result
        //by timestamp
        db.collection("messages")
            .whereEqualTo("conversationID", conversationID)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            //Begins listening to the query. Each time any change is detected it will take a
            //snapshot of the document which contains the data at the time of the change and perform
            //this code for each snapshot
            .addSnapshotListener { snapshot, error ->
                //Initially checks for any returned errors, ending the code execution early and
                //logging the error if there are
                if (error != null) {
                    Log.e("ChatDataSource", "Error listening for message updates", error)
                    return@addSnapshotListener
                }

                //Ensures that the snapshot contains valid data before proceeding
                if (snapshot != null) {
                    //Iterates through the matching documents returned by the previous query
                    //and for each one maps the data into a Message object
                    val messages = snapshot.documents.map { messageDocument ->
                        val messageID = messageDocument.id
                        val senderID = messageDocument.getString("senderID") ?: ""
                        val receiverID = messageDocument.getString("receiverID") ?: ""
                        val content = messageDocument.getString("content") ?: ""
                        val timestamp = messageDocument.getTimestamp("timestamp") ?: Timestamp.now()

                        Message(
                            messageID = messageID,
                            senderID = senderID,
                            receiverID = receiverID,
                            content = content,
                            timestamp = timestamp,
                            conversationID = conversationID
                        )
                    }

                    //This is where onMessagesUpdate is invoked which is used in ChatRepository
                    //to update state with the received messages.
                    onMessagesUpdate(messages)
                }
            }
    }
} 