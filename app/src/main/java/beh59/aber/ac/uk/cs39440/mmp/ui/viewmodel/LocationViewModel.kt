package beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import beh59.aber.ac.uk.cs39440.mmp.data.models.Friend
import beh59.aber.ac.uk.cs39440.mmp.data.models.ProjectMember
import beh59.aber.ac.uk.cs39440.mmp.data.models.User
import beh59.aber.ac.uk.cs39440.mmp.data.models.states.LocationState
import beh59.aber.ac.uk.cs39440.mmp.data.repository.ILocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * LocationViewModel
 * Mediates communication between the UI and location systems in the app
 * @param locationRepository Contains located relation data and operations
 * @param fusedLocationProviderClient Provides the application with data from the OS's GPS systems
 * @property uiState Exposes UI state from the repository to be used in the UI
 */
@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: ILocationRepository,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : ViewModel() {
    val uiState: StateFlow<LocationState> = locationRepository.locationState

    /**
     * initializeLocationSharing
     * Part of the SharedPreferences system allowing you to toggle on and off location sharing
     * at any time
     * @param isEnabled Whether or not location sharing is enabled based on the key:value stored
     * in the app's storage
     */
    fun initializeLocationSharing(isEnabled: Boolean) {
        locationRepository.initializeLocationSharing(isEnabled)
    }

    /**
     * GetCurrentLocation
     * Uses FusedLocationProviderClient to retrieve location data from the phone's GPS and
     * updates state with the data
     */
    //Permissions are explicitly checked in MapViewController before this method is called
    //Used to ensure the initial location of the user is loaded before the map is rendered.
    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    locationRepository.updateLocation(location.latitude, location.longitude)
                    locationRepository.updateFetchedStatus(true)
                }
            }
    }

    /**
     * updateLocation
     * Updates state with a new latitude and longitude based on the inputted parameters
     * @param latitude The new latitude to save to state
     * @param longitude The new longitude to save to state
     */
    fun updateLocation(latitude: Double, longitude: Double) {
        locationRepository.updateLocation(latitude, longitude)
    }

    /**
     * saveLocationToDatabase
     * Saves the current user's location to firestore via communication with the LocationRepository
     * @param user The current user of the application
     */
    fun saveLocationToDatabase(user: User) {
        val locationState = uiState.value
        val isEnabled = uiState.value.locationUpdatesEnabled

        //If location sharing has been disabled using the map's controls, doesn't write location
        if (!isEnabled) {
            return
        }

        //Doesn't write location if the application has not received GPS data yet
        if (!locationState.hasInitialLocation) {
            return
        }

        //Communicates with the repository to write location to the Realtime Database
        viewModelScope.launch {
            locationRepository.saveUserLocation(
                user,
                locationState.latitude,
                locationState.longitude
            )
        }
    }

    /**
     * toggleLocationUpdates
     * Updates state in LocationRepository to signal whether location sharing is on or off
     * @param locationEnabled Whether or not location sharing should be enabled
     * @param currentUser The current user of the application
     */
    fun toggleLocationUpdates(locationEnabled: Boolean, currentUser: User) {
        viewModelScope.launch {
            locationRepository.toggleLocationUpdates(locationEnabled, currentUser)
        }
    }

    /**
     * toggleLocationUpdates
     * An overload that just takes the current user and just toggles the value no matter its current
     * value
     * @param currentUser The current user of the application
     */
    suspend fun toggleLocationUpdates(currentUser: User) {
        locationRepository.toggleLocationUpdates(currentUser)
    }

    /**
     * startLocationUpdates
     * Begins the process of listening to the Firebase Realtime Database for the locations of
     * each of the current user's friends
     * @param friends A list of the current user's friends
     * @param currentUserID The unique identifier of the current user
     */
    fun startLocationUpdates(friends: List<Friend>, currentUserID: String = "") {
        viewModelScope.launch {
            locationRepository.startLocationUpdates(friends, currentUserID)
        }
    }

    /**
     * startProjectMemberLocationUpdates
     * Begins the process of listening to the Firebase Realtime Database for the locations of
     * each of the current project's members
     * @param projectMembers A list of members associated with the active project
     * @param currentUserID The unique identifier of the current user
     */
    fun startProjectMemberLocationUpdates(
        projectMembers: List<ProjectMember>,
        currentUserID: String = ""
    ) {
        viewModelScope.launch {
            locationRepository.startProjectMemberLocationUpdates(projectMembers, currentUserID)
        }
    }

    /**
     * refreshFriendLocations
     * Called to manually refresh friend locations and data
     * @param friends A list of the current user's friends
     * @param currentUserID The unique identifier of the current user
     */
    fun refreshFriendLocations(friends: List<Friend>, currentUserID: String = "") {
        viewModelScope.launch {
            locationRepository.refreshFriendLocations(friends, currentUserID)
        }
    }

    /**
     * refreshProjectMemberLocations
     * Called to manually refresh project member locations and data associated with the current
     * active project
     * @param projectMembers A list of project members in the current active project
     * @param currentUserID The unique identifier of the current user
     */
    fun refreshProjectMemberLocations(
        projectMembers: List<ProjectMember>,
        currentUserID: String = ""
    ) {
        viewModelScope.launch {
            locationRepository.refreshProjectMemberLocations(projectMembers, currentUserID)
        }
    }
}