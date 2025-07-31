package beh59.aber.ac.uk.cs39440.mmp.ui.projects

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import beh59.aber.ac.uk.cs39440.mmp.R
import beh59.aber.ac.uk.cs39440.mmp.data.models.Project
import beh59.aber.ac.uk.cs39440.mmp.ui.components.projects.ProjectMemberItem
import beh59.aber.ac.uk.cs39440.mmp.ui.friends.FriendViewModel

/**
 * AddProjectMember
 * A screen that allows users to select a friend to add to a project. Displayed after pressing
 * the FAB button on the Project Members tab.
 * @param project The project that the user is adding a member to
 * @param navController Handles navigation between screens
 * @param projectViewModel Exposes data and repository methods to UI classes regarding projects
 * @param friendViewModel Exposes data and repository methods to UI classes regarding friends
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectMember(
    project: Project,
    navController: NavController,
    projectViewModel: ProjectViewModel,
    friendViewModel: FriendViewModel
) {
    //Observes the state of data in FriendViewModel
    val uiState by friendViewModel.uiState.collectAsState()
    val friendsList = uiState.friends

    //Observes the state of data in ProjectViewModel
    val projectsState by projectViewModel.uiState.collectAsState()
    val projectsListSize = projectsState.projects.size

    //Allows us to collect user input and remember the state of it through recomposition
    var searchQuery by remember { mutableStateOf("") }
    var selectedFriendID by remember { mutableStateOf<String?>(null) }

    //Used to track loading state whilst performing operations
    var isLoading by remember { mutableStateOf(false) }

    //Filters out any friends who are already part of the relevant project.
    val availableFriends = friendsList.filter { friend ->
        project.members.none { it.uid == friend.uid }
    }

    //Filters the availableFriends list to respond to the search query
    val filteredFriends = if (searchQuery.isBlank()) {
        availableFriends
    } else {
        availableFriends.filter { friend ->
            friend.displayName.contains(searchQuery, ignoreCase = true) ||
                    friend.username.contains(searchQuery, ignoreCase = true)
        }
    }

    //Launches when the UI is composed and when the state of the keys (isLoading, projectsListSize)
    //changes. Makes sure that the operation has time to go through before navigating the user
    LaunchedEffect(isLoading, projectsListSize) {
        if (isLoading && selectedFriendID != null) {
            kotlinx.coroutines.delay(1000)
            isLoading = false
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.add_project_member),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_content_description)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            //Allows the user to search for specific friends to add to the project.
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.search_friends)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.label_content_description)
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.which_friend),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredFriends.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_friends),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                //Displays a Lazy Column which allows you select a friend and keep track of the
                //selected friend.
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    items(filteredFriends) { friend ->
                        ProjectMemberItem(
                            friend = friend,
                            isSelected = selectedFriendID == friend.uid,
                            onClick = {
                                if (!isLoading) {
                                    selectedFriendID =
                                        if (selectedFriendID == friend.uid) null else friend.uid
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            //Once the user has selected a friend, it uses selectedFriendID to perform the database
            //operation that will add them to the project.
            Button(
                onClick = {
                    selectedFriendID?.let { friendID ->
                        isLoading = true
                        projectViewModel.addProjectMember(project.projectID, friendID)
                    }
                },
                enabled = selectedFriendID != null && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                //Shows a small loading display to provide visual feedback to the user instead of
                //just a delay which seems like lag.
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(24.dp)
                            .width(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(stringResource(R.string.adding))
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(stringResource(R.string.add_to_project))
                }
            }
        }
    }
} 