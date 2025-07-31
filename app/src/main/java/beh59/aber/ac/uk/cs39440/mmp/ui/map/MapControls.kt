package beh59.aber.ac.uk.cs39440.mmp.ui.map

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import beh59.aber.ac.uk.cs39440.mmp.R
import beh59.aber.ac.uk.cs39440.mmp.data.models.Project
import beh59.aber.ac.uk.cs39440.mmp.data.models.User
import beh59.aber.ac.uk.cs39440.mmp.ui.components.IconButton
import beh59.aber.ac.uk.cs39440.mmp.ui.components.avatars.MapAvatar
import beh59.aber.ac.uk.cs39440.mmp.ui.projects.ProjectViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.LocationViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.UserViewModel
import beh59.aber.ac.uk.cs39440.mmp.utils.PreferenceHelper
import kotlinx.coroutines.launch

/**
 * MapControls
 * UI component that provides controls and user interface for the map screen
 * @param userViewModel ViewModel that handles user data and operations
 * @param locationViewModel ViewModel that handles location related data and operations
 * @param projectViewModel ViewModel that handles project related data and operations
 * @param navController Handles navigation between screens
 */
@Composable
fun MapControls(
    userViewModel: UserViewModel,
    locationViewModel: LocationViewModel,
    projectViewModel: ProjectViewModel,
    navController: NavController
) {
    val userState by userViewModel.uiState.collectAsState()
    val user = userState.user
    val displayName = user.displayName

    MapAvatar(userViewModel, navController)

    MapControlsContent(displayName, user, locationViewModel, projectViewModel, navController)
}

@Composable
fun MapControlsContent(
    displayName: String?,
    currentUser: User,
    locationViewModel: LocationViewModel,
    projectViewModel: ProjectViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val prefHelper = PreferenceHelper(context)
    val projectState by projectViewModel.uiState.collectAsState()
    val locationState by locationViewModel.uiState.collectAsState()
    val locationSharingEnabled = locationState.locationUpdatesEnabled
    val activeProject = projectViewModel.getActiveProject()
    var showProjectSelector by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.height(48.dp),
                    colors = ButtonColors(
                        contentColor = Color.Black,
                        containerColor = Color.White,
                        disabledContentColor = Color.White,
                        disabledContainerColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp,
                        hoveredElevation = 8.dp
                    ),
                    onClick = { showProjectSelector = true },
                ) {
                    Row(
                        modifier = Modifier
                            .width(150.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.outlined_group_work_24px),
                            contentDescription = "Group Work Icon"
                        )

                        Text(
                            text = activeProject?.title ?: "No Active Project",
                            textAlign = TextAlign.Center
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.filled_arrow_drop_down_24px),
                            contentDescription = "Group Work Icon"
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(27.dp, Alignment.CenterHorizontally)
            ) {
                IconButton(
                    onClick = { navController.navigate("Settings") },
                    iconId = R.drawable.filled_settings_24px,
                    contentDescription = "Settings Gear"
                )

                IconButton(
                    onClick = {
                        Log.d("DisplayName", "Display name: $displayName")
                    },
                    iconId = R.drawable.outlined_dark_mode_24px,
                    contentDescription = "Dark Mode Toggle"
                )

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            val newState = !locationSharingEnabled
                            Log.d(
                                "MapControls",
                                "Location sharing toggled"
                            )
                            locationViewModel.toggleLocationUpdates(currentUser)
                            prefHelper.setBooleanPref(
                                PreferenceHelper.BooleanPref.LocationToggle,
                                newState
                            )
                            Log.d(
                                "MapControls",
                                "SharedPreference locationToggle"
                            )
                        }
                    },
                    iconId = if (locationSharingEnabled)
                        R.drawable.filled_location_on_24px
                    else
                        R.drawable.filled_location_off_24px,
                    contentDescription = if (locationSharingEnabled)
                        "Disable Location Sharing"
                    else
                        "Enable Location Sharing"
                )
            }
        }

        if (showProjectSelector) {
            ProjectSelectorDialog(
                projects = projectState.projects,
                activeProjectID = projectState.activeProjectID,
                onProjectSelected = { projectID ->
                    projectViewModel.setActiveProject(projectID)
                    showProjectSelector = false
                },
                onDismiss = { showProjectSelector = false }
            )
        }
    }
}

@Composable
fun ProjectSelectorDialog(
    projects: List<Project>,
    activeProjectID: String?,
    onProjectSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Active Project",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Divider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProjectSelected(null) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = activeProjectID == null,
                        onClick = { onProjectSelected(null) }
                    )
                    Text(
                        text = "No Active Project (View Friends)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (activeProjectID == null) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Divider()

                LazyColumn {
                    items(projects) { project ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProjectSelected(project.projectID) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = activeProjectID == project.projectID,
                                onClick = { onProjectSelected(project.projectID) }
                            )
                            Column {
                                Text(
                                    text = project.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (activeProjectID == project.projectID) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "Role: ${project.role}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        if (project != projects.last()) {
                            Divider()
                        }
                    }
                }

                if (projects.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You are not a member of any projects yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                ) {
                    Text(text = "Close")
                }
            }
        }
    }
}