package beh59.aber.ac.uk.cs39440.mmp.ui.projects

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import beh59.aber.ac.uk.cs39440.mmp.data.models.OptionItem
import beh59.aber.ac.uk.cs39440.mmp.ui.components.AlphabetHeader
import beh59.aber.ac.uk.cs39440.mmp.ui.components.OptionsMenu
import beh59.aber.ac.uk.cs39440.mmp.ui.components.projects.ProjectItem
import beh59.aber.ac.uk.cs39440.mmp.ui.components.projects.ProjectListItem
import beh59.aber.ac.uk.cs39440.mmp.utils.ViewMode

/**
 * ProjectsOverview
 * Main screen that displays all projects the user is a member of
 * @param projectsViewModel ViewModel that handles project related data and operations
 * @param navController Handles navigation between screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsOverview(projectsViewModel: ProjectViewModel, navController: NavController) {
    val uiState by projectsViewModel.uiState.collectAsState()
    val projects = uiState.projects
    val groupedProjects by projectsViewModel.groupedProjects.collectAsState()
    val viewMode = uiState.viewMode

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${projects.size} Projects",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    OptionsMenu(
                        options = listOf(
                            OptionItem(
                                text = if (viewMode == ViewMode.GRID) "List View" else "Grid View",
                                onClick = { projectsViewModel.toggleViewMode() }
                            )
                        )
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("ProjectCreation") },
                modifier = Modifier,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Add project"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outline
            )

            when (viewMode) {
                ViewMode.GRID -> {
                    LazyColumn {
                        groupedProjects.forEach { (letter, projects) ->
                            item {
                                AlphabetHeader(letter)
                            }

                            items(projects) { project ->
                                ProjectItem(project, navController)
                            }
                        }
                    }
                }

                ViewMode.LIST -> {
                    LazyColumn {
                        groupedProjects.forEach { (letter, projects) ->
                            item {
                                AlphabetHeader(letter)
                            }

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
        }
    }
}