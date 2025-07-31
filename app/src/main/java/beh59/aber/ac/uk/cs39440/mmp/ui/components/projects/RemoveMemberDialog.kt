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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import beh59.aber.ac.uk.cs39440.mmp.data.models.Project
import beh59.aber.ac.uk.cs39440.mmp.data.models.ProjectMember

/**
 * RemoveMemberDialog
 * Displays a dialog that allows a project moderator to select and remove members from a project
 * @param project The project from which a member will be removed
 * @param onDismiss Callback that executes when the dialog is dismissed
 * @param onConfirm Callback that executes when a member is selected for removal, passing the
 * project and member
 */
@Composable
fun RemoveMemberDialog(
    project: Project,
    onDismiss: () -> Unit,
    onConfirm: (Project, ProjectMember) -> Unit
) {
    val projectMembers = project.members

    val filteredMembers = projectMembers.filter { member ->
        member.role != "moderator"
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
                    text = "Which member would you like to remove from the project?",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn {
                    if (filteredMembers.isEmpty()) {
                        item {
                            Text(
                                text = "No valid members found",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(filteredMembers) { member ->
                            ProjectMemberListItem(
                                projectMember = member,
                                onClick = { onConfirm(project, member) }
                            )
                        }
                    }
                }
            }
        }
    }
}