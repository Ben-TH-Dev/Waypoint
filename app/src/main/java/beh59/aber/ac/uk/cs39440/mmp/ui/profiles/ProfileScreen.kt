package beh59.aber.ac.uk.cs39440.mmp.ui.profiles

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import beh59.aber.ac.uk.cs39440.mmp.R
import beh59.aber.ac.uk.cs39440.mmp.data.models.Project
import beh59.aber.ac.uk.cs39440.mmp.ui.components.IconButton
import beh59.aber.ac.uk.cs39440.mmp.ui.components.projects.ProjectListItem
import beh59.aber.ac.uk.cs39440.mmp.ui.projects.ProjectViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.UserViewModel
import coil.compose.AsyncImage

/**
 * ProfileScreen
 * Used to display information about the current user including details retrieved from Firestore
 * @param userViewModel Contains current user related data and operations
 * @param projectsViewModel Contains project related data and operations
 * @param navController Handles navigation between screens
 */
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    projectsViewModel: ProjectViewModel,
    navController: NavController
) {
    val userState by userViewModel.uiState.collectAsState()
    val user = userState.user

    val projectState by projectsViewModel.uiState.collectAsState()
    val projects = projectState.projects

    ProfileScreenContent(user.displayName, user.username, user.photourl, projects, navController)
}

@Composable
fun ProfileScreenContent(
    displayName: String,
    username: String,
    photourl: String,
    projects: List<Project>,
    navController: NavController
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
                        model = photourl,
                        contentDescription = "Profile Picture",
                        placeholder = painterResource(R.drawable.generic_avatar),
                        error = painterResource(R.drawable.generic_avatar),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = username,
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
                        onClick = {},
                        iconId = R.drawable.filled_mail_24px,
                        contentDescription = "Mail Button"
                    )

                    IconButton(
                        onClick = {},
                        iconId = R.drawable.filled_call_24px,
                        contentDescription = "Call Button"
                    )

                    IconButton(
                        onClick = {},
                        iconId = R.drawable.filled_chat_bubble_24px,
                        contentDescription = "Chat Button"
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.outline
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "My Projects",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Start
                )
            }

            if (projects.isEmpty()) {
                item {
                    Text(
                        text = "You're not involved in any projects yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(projects) { project ->
                    ProjectListItem(
                        project = project,
                        onClick = { navController.navigate("ProjectDetail/${project.projectID}") }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    val navController = rememberNavController()
    ProfileScreenContent(
        displayName = "Ben Ben",
        username = "beh59",
        photourl = "null",
        projects = emptyList(),
        navController = navController
    )
}
