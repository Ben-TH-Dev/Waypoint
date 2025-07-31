package beh59.aber.ac.uk.cs39440.mmp.ui.friends

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import beh59.aber.ac.uk.cs39440.mmp.R
import beh59.aber.ac.uk.cs39440.mmp.data.models.Friend
import beh59.aber.ac.uk.cs39440.mmp.data.models.OptionItem
import beh59.aber.ac.uk.cs39440.mmp.ui.chat.ChatViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.components.IconButton
import beh59.aber.ac.uk.cs39440.mmp.ui.components.OptionsMenu
import beh59.aber.ac.uk.cs39440.mmp.ui.components.friends.RemoveFriendDialog
import beh59.aber.ac.uk.cs39440.mmp.ui.components.projects.AddToProjectDialog
import beh59.aber.ac.uk.cs39440.mmp.ui.components.projects.ProjectListItem
import beh59.aber.ac.uk.cs39440.mmp.ui.projects.ProjectViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.theme.outlineVariantLight
import coil.compose.AsyncImage
import androidx.core.net.toUri

/**
 * FriendProfileScreen
 * A composable that displays a profile screen for a friend
 * @param friend The Friend object containing the friend's details
 * @param navController Handles navigation between screens
 */
@Composable
fun FriendProfileScreen(
    friend: Friend,
    navController: NavController
) {
    val projectsViewModel: ProjectViewModel = hiltViewModel()
    val friendViewModel: FriendViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()
    val mutualProjects = projectsViewModel.getMutualProjects(friend.uid)
    val context = LocalContext.current
    var showAddToProjectDialog by remember { mutableStateOf(false) }
    var showRemoveFriendDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 40.dp, start = 8.dp),
                    iconId = R.drawable.close_24px,
                    contentDescription = stringResource(R.string.exit_friend_profile)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    ) {
                        AsyncImage(
                            model = friend.photourl,
                            contentDescription = stringResource(R.string.profile_picture),
                            placeholder = painterResource(R.drawable.generic_avatar),
                            error = painterResource(R.drawable.generic_avatar),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))


                    Text(
                        text = friend.displayName,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = friend.username,
                        style = MaterialTheme.typography.titleSmall
                    )


                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        IconButton(
                            onClick = {
                                val email = friend.email
                                Log.d("FriendProfileScreen", "email = $email")

                                if (email.isNullOrBlank()) {
                                    Toast.makeText(context, "Email address is missing", Toast.LENGTH_SHORT).show()
                                    return@IconButton
                                }

                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$email")
                                }

                                val chooser = Intent.createChooser(intent, "Send email")
                                if (chooser.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(chooser)
                                } else {
                                    Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                                }
                            },
                            iconId = R.drawable.filled_mail_24px,
                            contentDescription = stringResource(R.string.mail_button)
                        )

                        IconButton(
                            onClick = {
                                val phoneNumber = friend.phonenumber

                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = "tel:$phoneNumber".toUri()
                                }

                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                }
                            },
                            iconId = R.drawable.filled_call_24px,
                            contentDescription = stringResource(R.string.call_button)
                        )

                        IconButton(
                            onClick = {
                                chatViewModel.createConversation(friend.uid) { success ->
                                    if (success) {
                                        navController.navigate("TextFriendScreen")
                                    }
                                }
                            },
                            iconId = R.drawable.filled_chat_bubble_24px,
                            contentDescription = stringResource(R.string.chat_button)
                        )
                    }
                }

                OptionsMenu(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 40.dp, end = 8.dp),
                    options = listOf(
                        OptionItem(
                            text = "Add to project",
                            onClick = {
                                showAddToProjectDialog = true
                            }
                        ),
                        OptionItem(
                            text = stringResource(R.string.remove_friend_option),
                            onClick = {
                                showRemoveFriendDialog = true
                            }
                        )
                    )
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                color = outlineVariantLight
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(16.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.mutual_projects),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Start
                    )
                }

                if (mutualProjects.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.not_involved_mutual_projects),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(mutualProjects) { project ->
                        ProjectListItem(
                            project = project,
                            onClick = { navController.navigate("ProjectDetail/${project.projectID}") }
                        )
                    }
                }
            }
        }

        if (showAddToProjectDialog) {
            AddToProjectDialog(
                friend,
                projectsViewModel,
                onDismiss = { showAddToProjectDialog = false },
                onConfirm = { projectID ->
                    showAddToProjectDialog = false
                    projectsViewModel.addProjectMember(projectID, friend.uid, "member")
                    navController.navigate("Friends")
                }
            )
        }

        if (showRemoveFriendDialog) {
            RemoveFriendDialog(
                friendName = friend.displayName,
                onDismiss = { showRemoveFriendDialog = false },
                onConfirm = {
                    showRemoveFriendDialog = false
                    friendViewModel.removeFriend(friend)
                    navController.popBackStack()
                }
            )
        }
    }
}

@Preview
@Composable
fun FriendProfileScreenPreview() {
    val navController = rememberNavController()
    
    FriendProfileScreen(friend = Friend("TestUserUID", "Test User", "TestUser", "n/a", "testuser@gmail.com"), navController)
}