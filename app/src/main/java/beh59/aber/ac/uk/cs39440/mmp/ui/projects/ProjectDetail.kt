package beh59.aber.ac.uk.cs39440.mmp.ui.projects

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import beh59.aber.ac.uk.cs39440.mmp.data.models.OptionItem
import beh59.aber.ac.uk.cs39440.mmp.data.models.Project
import beh59.aber.ac.uk.cs39440.mmp.ui.components.ChatBox
import beh59.aber.ac.uk.cs39440.mmp.ui.components.OptionsMenu
import beh59.aber.ac.uk.cs39440.mmp.ui.components.projects.DeleteProjectDialog
import beh59.aber.ac.uk.cs39440.mmp.ui.components.projects.RemoveMemberDialog

/**
 * ProjectDetailTab
 * Enum class representing the different tabs available in the project detail screen
 *
 * @property title The display title of the tab
 */
enum class ProjectDetailTab(val title: String) {
    JOBS("Jobs"),
    MEMBERS("Members"),
    CHAT("Chat")
}

/**
 * ProjectDetail
 * Screen that displays detailed information about a project based on the user's role
 * @param project The project to display details for
 * @param navController Handles navigation between screens
 * @param projectViewModel ViewModel that handles project related data and operations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetail(
    project: Project,
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val tabs = ProjectDetailTab.entries.toTypedArray()
    val isModerator = project.role == "moderator"

    val projectState by projectViewModel.uiState.collectAsState()
    val updatedProject = projectState.projects.find { it.projectID == project.projectID } ?: project

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showDeleteProjectDialog by remember { mutableStateOf(false) }
    var showRemoveMemberDialog by remember { mutableStateOf(false) }

    if (!isModerator) {
        //Normal member UI
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = updatedProject.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                when (selectedTabIndex) {
                    2 -> ChatBox(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onSend = {
                        }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    //Only applies the scaffold's inner padding to the top of the page to prevent
                    //content being cut off.
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(tab.title) }
                        )
                    }
                }

                //Reserves space for the content of each tab and updates content accordingly based
                //on the selected tab.
                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedTabIndex) {
                        0 -> ProjectJobsTab(
                            project = updatedProject,
                            projectsViewModel = projectViewModel,
                            navController
                        )

                        1 -> ProjectMembersTab(
                            project = updatedProject
                        )

                        2 -> ProjectChatTab(project = updatedProject)
                    }
                }
            }
        }
    } else {
        //Moderator UI
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = updatedProject.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        OptionsMenu(
                            options = listOf(
                                OptionItem(
                                    text = "Edit project",
                                    onClick = {
                                        navController.navigate("ProjectEdit/${project.projectID}")
                                    }
                                ),
                                OptionItem(
                                    text = "Delete project",
                                    onClick = {
                                        showDeleteProjectDialog = true
                                    }
                                ),
                                OptionItem(
                                    text = "Remove member",
                                    onClick = {
                                        showRemoveMemberDialog = true
                                    }
                                ),
                            )
                        )
                    }
                )
            },
            bottomBar = {
                when (selectedTabIndex) {
                    2 -> ChatBox(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onSend = {
                        }
                    )
                }
            },
            floatingActionButton = {
                when (selectedTabIndex) {
                    0 ->
                        FloatingActionButton(
                            onClick = { navController.navigate("ProjectJobCreation/${updatedProject.projectID}") },
                            modifier = Modifier,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Add Job"
                            )
                        }

                    1 ->
                        FloatingActionButton(
                            onClick = { navController.navigate("AddProjectMember/${updatedProject.projectID}") },
                            modifier = Modifier,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Add Member"
                            )
                        }

                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    //Only applies the scaffold's inner padding to the top of the page to prevent
                    //content being cut off.
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(tab.title) }
                        )
                    }
                }

                //Reserves space for the content of each tab and updates content accordingly based
                //on the selected tab.
                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedTabIndex) {
                        0 -> ProjectJobsTab(
                            project = updatedProject,
                            projectsViewModel = projectViewModel,
                            navController
                        )

                        1 -> ProjectMembersTab(
                            project = updatedProject
                        )

                        2 -> ProjectChatTab(project = updatedProject)
                    }
                }
            }
        }

        if (showDeleteProjectDialog) {
            DeleteProjectDialog(
                project,
                onDismiss = { showDeleteProjectDialog = false },
                onConfirm = {
                    projectViewModel.deleteProject(project)
                    navController.navigate("Projects")
                }
            )
        }

        if (showRemoveMemberDialog) {
            RemoveMemberDialog(
                project,
                onDismiss = { showRemoveMemberDialog = false },
                onConfirm = { relevantProject, member ->
                    projectViewModel.removeMember(relevantProject, member)
                    navController.navigate("ProjectDetail/${relevantProject.projectID}")
                }
            )
        }
    }
}