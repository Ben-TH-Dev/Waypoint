package beh59.aber.ac.uk.cs39440.mmp.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import beh59.aber.ac.uk.cs39440.mmp.ui.components.TextItem
import beh59.aber.ac.uk.cs39440.mmp.ui.friends.FriendViewModel
import beh59.aber.ac.uk.cs39440.mmp.utils.formatTimeAgo

/**
 * ChatOverview
 * Provides an overview of the conversations a user is involved with
 * @param chatViewModel ViewModel that handles chat related data and operations
 * @param friendViewModel ViewModel that handles friend related data and operations
 * @param navController Handles navigation between screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatOverview(
    chatViewModel: ChatViewModel,
    friendViewModel: FriendViewModel,
    navController: NavController
) {
    val uiState by chatViewModel.uiState.collectAsState()

    //Refreshes conversation data when the screen is loaded
    LaunchedEffect(Unit) {
        if (chatViewModel.currentUser.uid.isNotEmpty()) {
            chatViewModel.refreshConversations()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        TopAppBar(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            title = {
                Text(
                    text = "${uiState.conversations.size} Conversations",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.outline
        )

        if (uiState.conversations.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "No conversations yet. Add a friend and start chatting!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                items(uiState.conversations) { conversation ->
                    //Finds the other participant's unique identifier and uses it to retrieve needed
                    //data
                    val otherParticipantID = chatViewModel.getOtherParticipantID(conversation) ?: ""
                    val otherUser = friendViewModel.getFriendByID(otherParticipantID)

                    if (otherUser != null) {
                        val displayName = otherUser.displayName

                        //Formats the complicated timestamp to readable text
                        val timeAgo = formatTimeAgo(conversation.lastMessageTimestamp, false)

                        TextItem(
                            name = displayName,
                            message = conversation.lastMessageContent,
                            timeAgo = timeAgo,
                            onClick = {
                                chatViewModel.selectConversation(conversation)
                                navController.navigate("TextFriendScreen")
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}