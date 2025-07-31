package beh59.aber.ac.uk.cs39440.mmp.nav

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import beh59.aber.ac.uk.cs39440.mmp.ui.chat.ChatOverview
import beh59.aber.ac.uk.cs39440.mmp.ui.chat.ChatViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.chat.TextFriend
import beh59.aber.ac.uk.cs39440.mmp.ui.components.BottomNavigationBar
import beh59.aber.ac.uk.cs39440.mmp.ui.friends.FriendProfileScreen
import beh59.aber.ac.uk.cs39440.mmp.ui.friends.FriendViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.friends.FriendsOverview
import beh59.aber.ac.uk.cs39440.mmp.ui.map.MapScreen
import beh59.aber.ac.uk.cs39440.mmp.ui.map.MapViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.other.OnboardingScreen
import beh59.aber.ac.uk.cs39440.mmp.ui.other.Settings
import beh59.aber.ac.uk.cs39440.mmp.ui.other.SplashPage
import beh59.aber.ac.uk.cs39440.mmp.ui.profiles.ProfileScreen
import beh59.aber.ac.uk.cs39440.mmp.ui.projects.AddProjectMember
import beh59.aber.ac.uk.cs39440.mmp.ui.projects.JobCreation
import beh59.aber.ac.uk.cs39440.mmp.ui.projects.JobDetail
import beh59.aber.ac.uk.cs39440.mmp.ui.projects.ProjectCreation
import beh59.aber.ac.uk.cs39440.mmp.ui.projects.ProjectDetail
import beh59.aber.ac.uk.cs39440.mmp.ui.projects.ProjectEdit
import beh59.aber.ac.uk.cs39440.mmp.ui.projects.ProjectViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.projects.ProjectsOverview
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.AuthViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.LocationViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.UserViewModel
import beh59.aber.ac.uk.cs39440.mmp.utils.PreferenceHelper

/**
 * WaypointNav
 * Responsible for controlling the flow of the application once the user has been fully
 * authenticated and all user data has been loaded. If they haven't set a username yet, they're
 * directed to the onboarding screen. Otherwise users are granted access to the application and the
 * routes inside are the five main screens of the application accessible via the bottom navigation
 * bar.
 */
@Composable
fun WaypointNav() {
    val userViewModel: UserViewModel = hiltViewModel()
    val friendViewModel: FriendViewModel = hiltViewModel()
    val locationViewModel: LocationViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()
    val projectViewModel: ProjectViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val mapViewModel: MapViewModel = hiltViewModel()
    val context = LocalContext.current
    val prefHelper = PreferenceHelper(context)
    val locationToggle = prefHelper.getBooleanPref(PreferenceHelper.BooleanPref.LocationToggle)
    val authState by authViewModel.uiState.collectAsState()
    //Creates and remembers an instance of NavController, which controls the navigation between
    //different screens.
    val navController = rememberNavController()
    //Observes the state of the ViewModel's hasSetUsername variable.
    val userState by userViewModel.uiState.collectAsState()
    val hasSetUsername = userState.hasSetUsername
    val hasSetPhoneNumber = userState.hasSetPhoneNumber

    locationViewModel.initializeLocationSharing(locationToggle)

    LaunchedEffect(locationToggle) {
        locationViewModel.toggleLocationUpdates(locationToggle, userState.user)
    }

    if (!authState.isAuthenticated || !authState.isUserDataLoaded) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    //Continues loading if authenticated, but Firestore calls for user data are still running.
    if (!authState.isUserDataLoaded) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
    //If the user has set a username, then the application proceeds as normal.
    //If they haven't, they are routed to the onboarding screen to pick a username.
    else if (hasSetUsername && hasSetPhoneNumber) {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            NavHost(
                modifier = Modifier
                    .padding(innerPadding),
                navController = navController,
                startDestination = "Map"
            ) {
                composable("Profile") {
                    ProfileScreen(userViewModel, projectViewModel, navController)
                }

                friendsNavGraph(
                    userViewModel,
                    friendViewModel,
                    projectViewModel,
                    navController,
                    chatViewModel
                )

                composable("Map") {
                    MapScreen(
                        userViewModel,
                        friendViewModel,
                        locationViewModel,
                        mapViewModel,
                        projectViewModel,
                        navController
                    )
                }

                projectsNavGraph(
                    projectViewModel,
                    friendViewModel,
                    navController,
                )

                chatNavGraph(
                    chatViewModel,
                    friendViewModel,
                    userViewModel,
                    navController
                )

                composable("Settings") {
                    Settings(authViewModel, navController)
                }

                composable("SplashScreen") {
                    SplashPage(userViewModel)
                }
            }
        }
    } else {
        OnboardingScreen(userViewModel)
    }
}

/**
 * friendsNavGraph
 * A nested navigation graph with all screens related to the friend system in the application
 * @param userViewModel ViewModel that handles user data and operations
 * @param friendViewModel ViewModel that handles friend related data and operations
 * @param projectViewModel ViewModel that handles project related data and operations
 * @param navController Handles navigation between screens
 * @param chatViewModel ViewModel that handles chat related data and operations
 */
fun NavGraphBuilder.friendsNavGraph(
    userViewModel: UserViewModel,
    friendViewModel: FriendViewModel,
    projectViewModel: ProjectViewModel,
    navController: NavController,
    chatViewModel: ChatViewModel
) {
    navigation(startDestination = "Friends", route = "FriendsNav") {
        composable("Friends") {
            FriendsOverview(
                navController
            )
        }
        composable("FriendProfile/{friendID}") { backStackEntry ->
            val friendID = backStackEntry.arguments?.getString("friendID")
            val friend = friendViewModel.getFriendByID(friendID)
            if (friend != null) {
                FriendProfileScreen(
                    friend,
                    navController
                )
            }
        }
    }
}

/**
 * projectsNavGraph
 * A nested navigation graph with all screens related to the projects system in the application
 * @param projectViewModel ViewModel that handles project related data and operations
 * @param friendViewModel ViewModel that handles friend related data and operations
 * @param navController Handles navigation between screens
 */
fun NavGraphBuilder.projectsNavGraph(
    projectViewModel: ProjectViewModel,
    friendViewModel: FriendViewModel,
    navController: NavController,
) {
    navigation(startDestination = "Projects", route = "ProjectsNav") {
        composable("Projects") {
            ProjectsOverview(
                projectViewModel,
                navController
            )
        }
        composable("ProjectDetail/{projectID}") { backStackEntry ->
            val projectID = backStackEntry.arguments?.getString("projectID")
            if (projectID != null) {
                val project = projectViewModel.getProjectByID(projectID)
                if (project != null) {
                    ProjectDetail(project, navController, projectViewModel)
                }
            }
        }
        composable("ProjectCreation") {
            ProjectCreation(navController, projectViewModel)
        }
        composable("ProjectEdit/{projectID}") { backStackEntry ->
            val projectID = backStackEntry.arguments?.getString("projectID")
            if (projectID != null) {
                val project = projectViewModel.getProjectByID(projectID)
                if (project != null) {
                    ProjectEdit(navController, project, projectViewModel)
                }
            }
        }
        composable("ProjectJobCreation/{projectID}") { backStackEntry ->
            val projectID = backStackEntry.arguments?.getString("projectID")
            if (projectID != null) {
                val project = projectViewModel.getProjectByID(projectID)
                if (project != null) {
                    JobCreation(project, projectViewModel, navController)
                }
            }
        }
        composable("JobDetailScreen/{jobID}") { backStackEntry ->
            val jobID = backStackEntry.arguments?.getString("jobID")
            if (jobID != null) {
                val job = projectViewModel.getJobByID(jobID)
                if (job != null) {
                    JobDetail(job, navController, projectViewModel)
                }
            }
        }
        composable("AddProjectMember/{projectID}") { backStackEntry ->
            val projectID = backStackEntry.arguments?.getString("projectID")
            if (projectID != null) {
                val project = projectViewModel.getProjectByID(projectID)
                if (project != null) {
                    AddProjectMember(project, navController, projectViewModel, friendViewModel)
                }
            }
        }
    }
}

/**
 * chatNavGraph
 * A nested navigation graph with all screens related to the chat system in the application
 * @param chatViewModel ViewModel that handles chat related data and operations
 * @param friendViewModel ViewModel that handles friend related data and operations
 * @param userViewModel ViewModel that handles user data and operations
 * @param navController Handles navigation between screens
 */
fun NavGraphBuilder.chatNavGraph(
    chatViewModel: ChatViewModel,
    friendViewModel: FriendViewModel,
    userViewModel: UserViewModel,
    navController: NavController,
) {
    navigation(startDestination = "Chat", route = "ChatNav") {
        composable("Chat") {
            ChatOverview(chatViewModel, friendViewModel, navController)
        }
        composable("TextFriendScreen") {
            TextFriend(chatViewModel, userViewModel, navController)
        }
    }
}