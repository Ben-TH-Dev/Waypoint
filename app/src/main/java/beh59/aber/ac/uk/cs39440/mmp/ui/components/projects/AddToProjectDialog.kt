package beh59.aber.ac.uk.cs39440.mmp.ui.components.projects

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import beh59.aber.ac.uk.cs39440.mmp.data.models.Friend
import beh59.aber.ac.uk.cs39440.mmp.ui.projects.ProjectViewModel

/**
 * AddToProjectDialog
 * Displays a dialog that allows users to add a friend to one of their projects
 * @param friend The friend to be added to a project
 * @param projectViewModel The ViewModel that handles project data and operations
 * @param onDismiss Callback that executes when the dialog is dismissed
 * @param onConfirm Callback that executes when a project is selected, receiving the project ID
 */
@Composable
fun AddToProjectDialog(
    friend: Friend,
    projectViewModel: ProjectViewModel,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val projectState by projectViewModel.uiState.collectAsState()
    val projects = projectState.projects

    val filteredProjects = projects.filter { project ->
        project.role == "moderator" && !project.members.any { member -> member.uid == friend.uid }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Which project would you like to add your friend to?",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn {
                    if (filteredProjects.isEmpty()) {
                        item {
                            Text(
                                text = "You are not the moderator of any projects that your friend isn't already in",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(filteredProjects) { project ->
                            ProjectListItem(
                                project = project,
                                onClick = { onConfirm(project.projectID) }
                            )
                        }
                    }
                }
            }
        }
    }
}