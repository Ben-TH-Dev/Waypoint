package beh59.aber.ac.uk.cs39440.mmp.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import beh59.aber.ac.uk.cs39440.mmp.ui.components.map.FriendMapItem
import beh59.aber.ac.uk.cs39440.mmp.ui.components.map.ProjectMemberMapItem
import beh59.aber.ac.uk.cs39440.mmp.ui.friends.FriendViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.projects.ProjectViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.LocationViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.UserViewModel
import beh59.aber.ac.uk.cs39440.mmp.utils.RequestLocationPermission
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.style.GenericStyle
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlinx.coroutines.launch

/**
 * MapViewController
 * Core component responsible for rendering the map and handling map related functionality
 * @param userViewModel ViewModel that handles user data and operations
 * @param friendViewModel ViewModel that handles friend related data and operations
 * @param locationViewModel ViewModel that handles location related data and operations
 * @param mapViewModel ViewModel that handles map related data and operations
 * @param projectViewModel ViewModel that handles project related data and operations
 * @param navController Handles navigation between screens
 */
@Composable
fun MapViewController(
    userViewModel: UserViewModel,
    friendViewModel: FriendViewModel,
    locationViewModel: LocationViewModel,
    mapViewModel: MapViewModel,
    projectViewModel: ProjectViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val userState by userViewModel.uiState.collectAsState()
    val friendState by friendViewModel.uiState.collectAsState()
    val projectState by projectViewModel.uiState.collectAsState()
    val mapState by mapViewModel.uiState.collectAsState()
    val locationState by locationViewModel.uiState.collectAsState()
    val user = userState.user
    val isDarkMode = userState.isDarkMode
    val friends = friendState.friends
    val activeProject = projectViewModel.getActiveProject()
    val showMap = mapState.showMap
    val mapboxMapState = mapState.mapState
    val mapViewportState = mapState.mapViewportState
    val isLoadingFriends = mapState.isLoadingFriends

    //Runs when the composable is launched, persisting through recomposition but not navigation,
    //which will create a new key (Unit) and trigger the LaunchedEffect again.
    //Ensures that data is not refreshed unnecessarily every time the map is recomposed when a
    //state's value changes.
    LaunchedEffect(Unit) {
        mapViewModel.setLoading(true)
        friendViewModel.refreshData()

        //Checks if there's an active project when first loading the map
        activeProject?.let { project ->
            locationViewModel.refreshProjectMemberLocations(project.members, user.uid)
            locationViewModel.startProjectMemberLocationUpdates(project.members, user.uid)
        }

        mapViewModel.setLoading(false)
    }

    //Listens for changes to the key (active project in state) and refreshes the map when it changes
    LaunchedEffect(projectState.activeProjectID) {
        Log.d(
            "MapViewController",
            "Active project changed to: ${projectState.activeProjectID ?: "No active project"}"
        )

        //Refreshes map-related data to ensure accuracy
        mapViewModel.refreshMapState()

        //Refreshes friend locations with the current friends list
        if (friends.isNotEmpty()) {
            Log.d("MapViewController", "Refreshing friend locations after project change")
            locationViewModel.refreshFriendLocations(friends, user.uid)
        }

        //If there's an active project, also starts the process of monitoring project member
        //locations
        activeProject?.let { project ->
            Log.d(
                "MapViewController",
                "Starting project member location updates for ${project.title}"
            )
            locationViewModel.refreshProjectMemberLocations(project.members, user.uid)
            locationViewModel.startProjectMemberLocationUpdates(project.members, user.uid)
        }
    }

    //Runs every time the value of the key, which is the value of the friends state, changes.
    //Ensures the list is not empty and then begins the process of reading the friend's location from
    //Firestore. Keeps location data updated whilst on the map so that friend's ViewAnnotations
    //have accurate location. Note: system needs modifying to subscribe to the Realtime Database
    //so that it only updates when the location data actually changes to prevent unnecessary reads.
    LaunchedEffect(friends) {
        if (friends.isNotEmpty()) {
            locationViewModel.startLocationUpdates(friends, user.uid)
        }
    }

    RequestLocationPermission(
        onPermissionDenied = {
            scope.launch {
                snackbarHostState.showSnackbar("You need to accept location permissions.")
            }
        },
        onPermissionReady = {
            mapViewModel.updateShowMap()
        }
    )

    if (showMap && !isLoadingFriends) {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED ||
            coarseLocationPermission == PackageManager.PERMISSION_GRANTED
        ) {
            locationViewModel.getCurrentLocation()
        } else {
            Log.d("MapViewController", "Location permissions not granted.")
        }

        if (locationState.hasInitialLocation) {
            MapboxMap(
                Modifier.fillMaxSize(),
                mapState = mapboxMapState,
                mapViewportState = mapViewportState,
                style = {
                    GenericStyle(
                        style = if (isDarkMode) {
                            "mapbox://styles/minorcheetah788/cm6h07ooo000901sggj2qfc6m"
                        } else {
                            "mapbox://styles/mapbox/streets-v12"
                        }
                    )
                },
                scaleBar = {},
                compass = {}
            ) {
                MapEffect(Unit) { mapView ->
                    val locationComponent = mapView.location

                    //Creates a location puck at the user's location
                    locationComponent.updateSettings {
                        locationPuck = createDefault2DPuck(withBearing = true)
                        enabled = true
                    }

                    //Tells the map to start in a mode where it zooms in on the user's location
                    //and plays a small zoom-in animation.
                    mapViewportState.setCameraOptions(
                        CameraOptions.Builder()
                            .center(
                                Point.fromLngLat(
                                    locationState.longitude,
                                    locationState.latitude
                                )
                            )
                            .zoom(15.0)
                            .build()
                    )

                    //Used to track the duration since the last update to prevent unnecessary
                    //writes to the Realtime Database. Helps especially during the beginning
                    //of the application where FusedLocationProvider changes values rapidly.
                    var lastUpdateTime = 0L
                    val timeBetweenWrites = 5000L

                    //Part of the Mapbox Map: listens for changes in the current user's position using
                    //FusedLocationProvider and executes the code inside when the user's location changes.
                    val onIndicatorPositionChangedListener =
                        OnIndicatorPositionChangedListener { point ->
                            val currentTime = System.currentTimeMillis()

                            if (currentTime - lastUpdateTime >= timeBetweenWrites) {
                                lastUpdateTime = currentTime
                                //If there are any issues and the default constructor for localUser
                                //is used, location data is not written to the database.
                                if (user.uid == "" || user.uid == "UserState error") {
                                    Log.d(
                                        "MapViewController",
                                        "Default error user, not updating location"
                                    )
                                } else {
                                    if (locationViewModel.uiState.value.hasInitialLocation) {
                                        //Acquires the latitude and the longitude from the Point
                                        //returned by the Mapbox API and calls testWrite from
                                        //locationViewModel to write the data to the Firebase
                                        //Realtime Database.
                                        locationViewModel.updateLocation(
                                            point.latitude(),
                                            point.longitude()
                                        )

                                        locationViewModel.saveLocationToDatabase(user)
                                    }
                                }
                            }
                        }

                    //Adds the listener to the MapView which will begin listening for changes.
                    mapView.location.addOnIndicatorPositionChangedListener(
                        onIndicatorPositionChangedListener
                    )
                }


                if (activeProject != null) {
                    Log.d(
                        "MapViewController",
                        "Showing project members for project: ${activeProject.title}"
                    )

                    val projectMembersWithLocations = locationState.projectMemberLocations

                    Log.d(
                        "MapViewController",
                        "Found ${projectMembersWithLocations.size} project members with location data"
                    )

                    projectMembersWithLocations.forEach { memberWithLocation ->
                        ViewAnnotation(
                            options = viewAnnotationOptions {
                                geometry(
                                    Point.fromLngLat(
                                        memberWithLocation.longitude,
                                        memberWithLocation.latitude
                                    )
                                )
                                annotationAnchor {
                                    anchor(ViewAnnotationAnchor.BOTTOM)
                                }
                                allowOverlap(true)
                                visible(true)
                            }
                        ) {
                            ProjectMemberMapItem(memberWithLocation, navController)
                        }
                    }
                } else {
                    val friendsWithLocations = locationState.friendLocations

                    friendsWithLocations.forEach { friend ->
                        ViewAnnotation(
                            options = viewAnnotationOptions {
                                geometry(Point.fromLngLat(friend.longitude, friend.latitude))
                                annotationAnchor {
                                    anchor(ViewAnnotationAnchor.BOTTOM)
                                }
                                allowOverlap(true)
                                visible(true)
                            }
                        ) {
                            FriendMapItem(friend, navController)
                        }
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}