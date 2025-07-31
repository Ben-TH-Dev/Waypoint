package beh59.aber.ac.uk.cs39440.mmp.ui.components.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import beh59.aber.ac.uk.cs39440.mmp.R
import beh59.aber.ac.uk.cs39440.mmp.data.models.FriendRequest
import beh59.aber.ac.uk.cs39440.mmp.data.models.User
import beh59.aber.ac.uk.cs39440.mmp.ui.components.IconButton
import beh59.aber.ac.uk.cs39440.mmp.ui.friends.FriendViewModel

/**
 * FriendRequestItem
 * Displayed when the user presses the add friends button on the Friends screen. Displays
 * information about an existing friend request in the system.
 * @param friendRequest The FriendRequest object that we are representing in UI
 * @param currentUser The current user of the application
 * @param friendViewModel ViewModel that handles friend data and operations
 */
@Composable
fun FriendRequestItem(
    friendRequest: FriendRequest,
    currentUser: User,
    friendViewModel: FriendViewModel
) {
    //Retrieves both involved IDs
    val senderID = friendRequest.senderID
    val receiverID = friendRequest.receiverID

    //Determines if the current user sent the request or if the other user did
    val currentUserSent = senderID == currentUser.uid

    //Gets user details for each user involved in a friend request
    val friendState by friendViewModel.uiState.collectAsState()
    val friendRequestUsers = friendState.friendRequestUsers

    //Finds the first case where the uid of the friendRequestUser matches the sender ID or the
    //receiver ID.
    val sender = friendRequestUsers.firstOrNull { it.uid == senderID }
    val receiver = friendRequestUsers.firstOrNull { it.uid == receiverID }

    //Calls the Jetpack Compose to construct a FriendRequestItem, used to populate the lazy column
    //of friend requests on FriendSearchScreen
    FriendRequestItemContent(
        currentUserSent,
        sender,
        receiver,
        onAcceptRequest = {
            //Communicates with the repository to accept the request
            if (sender != null && receiver != null) {
                friendViewModel.acceptFriendRequest(sender.uid, receiver.uid)
            }
        },
        onRejectRequest = {
            //Communicates with the repository to reject the request
            if (sender != null && receiver != null) {
                friendViewModel.rejectFriendRequest(sender.uid, receiver.uid)
            }
        }
    )
}

@Composable
fun FriendRequestItemContent(
    currentUserSent: Boolean,
    sender: User?,
    receiver: User?,
    onAcceptRequest: () -> Unit,
    onRejectRequest: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable {},
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .width(60.dp)
                    .height(60.dp),
                painter = painterResource(
                    id = R.drawable.generic_avatar,
                ),
                contentDescription = "Sender avatar"
            )

            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1f)
            ) {
                if (currentUserSent) {
                    if (receiver != null) {
                        Text(
                            text = receiver.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    if (sender != null) {
                        Text(
                            text = sender.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (currentUserSent) {
                if (receiver != null) {
                    IconButton(
                        onClick = {
                            onRejectRequest()
                        },
                        iconId = R.drawable.close_24px,
                        contentDescription = "Add User as Friend"
                    )
                }
            } else {
                if (sender != null) {
                    Row {
                        IconButton(
                            onClick = { onAcceptRequest() },
                            iconId = R.drawable.check_24px,
                            contentDescription = "Add User as Friend"
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                onRejectRequest()
                            },
                            iconId = R.drawable.close_24px,
                            contentDescription = "Deny User as Friend"
                        )
                    }
                }
            }
        }
    }
}