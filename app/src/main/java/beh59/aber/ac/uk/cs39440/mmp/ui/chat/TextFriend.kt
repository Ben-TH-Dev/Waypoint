package beh59.aber.ac.uk.cs39440.mmp.ui.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import beh59.aber.ac.uk.cs39440.mmp.data.models.OptionItem
import beh59.aber.ac.uk.cs39440.mmp.data.models.User
import beh59.aber.ac.uk.cs39440.mmp.ui.components.ChatBox
import beh59.aber.ac.uk.cs39440.mmp.ui.components.ChatItem
import beh59.aber.ac.uk.cs39440.mmp.ui.components.OptionsMenu
import beh59.aber.ac.uk.cs39440.mmp.ui.components.chat.DeleteConversationDialog
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.UserViewModel

/**
 * TextFriend
 * Chat screen for messaging between users
 * @param chatViewModel ViewModel that handles chat related data and operations
 * @param userViewModel ViewModel that handles user data and operations
 * @param navController Handles navigation between screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFriend(
    chatViewModel: ChatViewModel,
    userViewModel: UserViewModel,
    navController: NavController
) {
    val uiState by chatViewModel.uiState.collectAsState()
    val currentUser = chatViewModel.currentUser
    val messages = uiState.messages
    val listState = rememberLazyListState()

    //A list of users that initializes as an empty list and is remembered across recomposition.
    var memberDetails by remember { mutableStateOf<List<User>>(emptyList()) }
    //Used to conditionally display a dialog allowing the user to delete the current conversation
    var showDeleteConversationDialog by remember { mutableStateOf(false) }

    //Launches when the Composable is first displayed and when its key, currentConversation is
    //changed
    LaunchedEffect(uiState.currentConversation) {
        uiState.currentConversation?.let { conversation ->
            val members = conversation.members
            val memberList = mutableListOf<User>()

            //Fetches details for users involved in conversation that are not the current user
            for (member in members) {
                if (member.uid != currentUser.uid) {
                    try {
                        //Communicates with ViewModel to begin process of retrieving details
                        val userDetails = userViewModel.getUserDetails(member.uid)
                        //Once retrieved, they are added to the list in memory.
                        memberList.add(userDetails)
                    } catch (e: Exception) {
                        //If there is a problem getting details, creates a placeholder user
                        memberList.add(User(uid = member.uid, displayName = "Unknown User"))
                    }
                }
            }

            //Writes the total list of members and their data to memberDetails
            memberDetails = memberList
        }
    }

    //Dynamic display title in cases of error
    val displayTitle = when {
        memberDetails.isEmpty() -> "No valid members"
        else -> "${memberDetails.size + 1} Members"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = displayTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        //Ensures that conversation data is cleared when navigating away
                        chatViewModel.clearCurrentConversation()
                        chatViewModel.refreshConversations()
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to conversations"
                        )
                    }
                },
                actions = {
                    OptionsMenu(listOf(
                        OptionItem(
                            text = "Delete conversation",
                            onClick = { showDeleteConversationDialog = true }
                        )
                    ))
                }
            )
        },
        bottomBar = {
            ChatBox(
                modifier = Modifier
                    .fillMaxWidth(),
                onSend = { content ->
                    if (content.isNotBlank()) {
                        chatViewModel.sendMessage(content)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (messages.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    state = listState
                ) {
                    items(messages) { message ->
                        val isFromCurrentUser = chatViewModel.isMessageFromCurrentUser(message)
                        ChatItem(
                            message = message,
                            isFromCurrentUser = isFromCurrentUser
                        )
                    }
                }
            }
        }

        if (showDeleteConversationDialog) {
            DeleteConversationDialog(
                onDismiss = { showDeleteConversationDialog = false },
                onConfirm = {
                    chatViewModel.deleteConversation(uiState.currentConversation)
                    chatViewModel.clearCurrentConversation()
                    chatViewModel.refreshConversations()
                    navController.navigate("Chat")
                }
            )
        }

    }
}