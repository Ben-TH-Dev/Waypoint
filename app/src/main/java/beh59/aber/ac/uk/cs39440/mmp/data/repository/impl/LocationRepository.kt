package beh59.aber.ac.uk.cs39440.mmp.data.repository.impl

import android.util.Log
import beh59.aber.ac.uk.cs39440.mmp.data.models.Friend
import beh59.aber.ac.uk.cs39440.mmp.data.models.ProjectMember
import beh59.aber.ac.uk.cs39440.mmp.data.models.User
import beh59.aber.ac.uk.cs39440.mmp.data.models.UserLocation
import beh59.aber.ac.uk.cs39440.mmp.data.models.states.LocationState
import beh59.aber.ac.uk.cs39440.mmp.data.repository.ILocationRepository
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject

/**
 * LocationRepository
 * An implementation of the ILocationRepository interface and its required methods.
 * Contains the business logic related to location sharing and tracking within the application.
 * Communicates with the Firebase Realtime Database, a separate technology offered by Google's
 * Firebase that uses a JSON structure and is optimized for real-time data.
 * @param realtime The Firebase Realtime Database instance used for storing location data
 * @property locationState Exposes LocationState and its data to the UI
 */
class LocationRepository @Inject constructor(
    private val realtime: FirebaseDatabase
) : ILocationRepository {
    private val _locationState = MutableStateFlow(LocationState())
    override val locationState = _locationState.asStateFlow()

    /**
     * updateLocation
     * Updates the current user's location in the state
     * @param latitude The user's current latitude
     * @param longitude The user's current longitude
     */
    override fun updateLocation(latitude: Double, longitude: Double) {
        _locationState.value = _locationState.value.copy(
            latitude = roundToFiveDecimalPlaces(latitude),
            longitude = roundToFiveDecimalPlaces(longitude)
        )
    }

    /**
     * updateFetchedStatus
     * Updates the state to signal whether the initial location has been fetched
     * @param completed Whether the initial location has been retrieved
     */
    override fun updateFetchedStatus(completed: Boolean) {
        _locationState.value = _locationState.value.copy(
            hasInitialLocation = completed
        )
    }

    /**
     * roundToFiveDecimalPlaces
     * Rounds the large number returned by FusedLocationProvider to 5 decimal places to reduce
     * unnecessary sensitivity
     * Specifies locale to avoid warning and bugs in different languages
     * @param value The value to be rounded
     * @return The rounded value
     */
    override fun roundToFiveDecimalPlaces(value: Double): Double {
        return String.format(Locale.US, "%.5f", value).toDouble()
    }

    /**
     * removeLocationData
     * Removes a user's location data from the Firebase Realtime Database
     * @param user The user whose location data should be removed
     * @return Result indicating success or failure
     */
    @AddTrace(name = "locationRepoRemoveLocationData")
    override suspend fun removeLocationData(user: User): Result<Unit> {
        return try {
            val userReference = realtime.reference.child("users/${user.uid}")
            userReference.removeValue().await()
            Log.d("LocationRepository", "Removed location data")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("LocationRepository", "Failed to remove location data", e)
            Result.failure(e)
        }
    }

    /**
     * saveUserLocation
     * Saves a user's location data to the Firebase Realtime Database
     * @param user The user whose location should be saved
     * @param latitude The user's latitude
     * @param longitude The user's longitude
     * @return Result indicating success or failure
     */
    @AddTrace(name = "locationRepoSaveUserLocation")
    override suspend fun saveUserLocation(
        user: User,
        latitude: Double,
        longitude: Double
    ): Result<Unit> {
        return try {
            val userReference = realtime.reference.child("users/${user.uid}")

            val userData = hashMapOf(
                "lat" to latitude,
                "long" to longitude,
                "lastUpdated" to System.currentTimeMillis()
            )

            userReference.setValue(userData).await()
            Log.d("LocationRepository", "Location data saved successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("LocationRepository", "Failed to save location data", e)
            Result.failure(e)
        }
    }

    /**
     * initializeLocationSharing
     * Initializes the location sharing state
     * @param isEnabled Whether location sharing should be enabled
     */
    override fun initializeLocationSharing(isEnabled: Boolean) {
        _locationState.value = _locationState.value.copy(
            locationUpdatesEnabled = isEnabled
        )
    }

    /**
     * toggleLocationUpdates
     * Toggles location updates on or off and handles necessary data operations
     * @param locationEnabled Whether location updates should be enabled
     * @param currentUser The current user
     */
    override suspend fun toggleLocationUpdates(locationEnabled: Boolean, currentUser: User) {
        //Updates state with the value given as the locationEnabled parameter
        _locationState.value = _locationState.value.copy(
            locationUpdatesEnabled = locationEnabled
        )

        //If the value of locationEnabled is false, removes the user's location data from the
        //Realtime database. Otherwise, attempts to save the current user's location to the database
        //if valid data exists, keeping the database up to date
        if (!locationEnabled) {
            try {
                removeLocationData(currentUser)
            } catch (e: Exception) {
                Log.e("LocationRepository", "Failed to remove location data", e)
            }
        } else {
            val locationState = _locationState.value
            if (locationState.hasInitialLocation) {
                try {
                    saveUserLocation(
                        currentUser,
                        locationState.latitude,
                        locationState.longitude
                    )
                } catch (e: Exception) {
                    Log.e("LocationRepository", "Failed to save initial location data on enable", e)
                }
            }
        }
    }

    /**
     * toggleLocationUpdates
     * Overridden method that simply takes the current user and toggles the value for
     * locationUpdatesEnabled regardless of its current value
     * @param currentUser The current user
     */
    override suspend fun toggleLocationUpdates(currentUser: User) {
        toggleLocationUpdates(!_locationState.value.locationUpdatesEnabled, currentUser)
    }

    /**
     * startLocationUpdates
     * Starts continuously updating location data for friends
     * @param friends List of friends whose locations should be retrieved
     * @param currentUserID The current user's unique identifier
     */
    override suspend fun startLocationUpdates(friends: List<Friend>, currentUserID: String) {
        try {
            val initialLocations = retrieveFriendLocations(friends, currentUserID)
            _locationState.value = _locationState.value.copy(
                friendLocations = initialLocations
            )

            while (_locationState.value.locationUpdatesEnabled) {
                delay(5000)
                val refreshedFriends = retrieveFriendLocations(friends, currentUserID)
                _locationState.value = _locationState.value.copy(
                    friendLocations = refreshedFriends
                )
            }
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error updating friend locations", e)
        }
    }

    /**
     * startProjectMemberLocationUpdates
     * Starts a continuous loop of location updates for project members
     * @param projectMembers List of project members whose locations should be monitored
     * @param currentUserID The current user's ID to avoid self-updates
     */
    override suspend fun startProjectMemberLocationUpdates(
        projectMembers: List<ProjectMember>,
        currentUserID: String
    ) {
        try {
            try {
                val initialLocations = retrieveProjectMemberLocations(projectMembers, currentUserID)
                _locationState.value = _locationState.value.copy(
                    projectMemberLocations = initialLocations
                )
            } catch (e: Exception) {
                Log.e("LocationRepository", "Error retrieving initial project member locations", e)
            }


            while (_locationState.value.locationUpdatesEnabled) {
                delay(5000)
                try {
                    val refreshedLocations =
                        retrieveProjectMemberLocations(projectMembers, currentUserID)
                    _locationState.value = _locationState.value.copy(
                        projectMemberLocations = refreshedLocations
                    )
                    Log.d(
                        "LocationRepository",
                        "Refreshed project member locations: ${refreshedLocations.size}"
                    )
                } catch (e: Exception) {
                    Log.e(
                        "LocationRepository",
                        "Error refreshing project member locations",
                        e
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error refreshing project member locations", e)
        }
    }

    /**
     * refreshFriendLocations
     * Manually refreshes friend locations once
     * @param friends List of friends whose locations should be refreshed
     * @param currentUserID The current user's ID to avoid self-updates
     */
    override suspend fun refreshFriendLocations(friends: List<Friend>, currentUserID: String) {
        Log.d("LocationRepository", "Manually refreshing friend locations")
        try {
            val refreshedFriends = retrieveFriendLocations(friends, currentUserID)
            _locationState.value = _locationState.value.copy(
                friendLocations = refreshedFriends
            )
        } catch (e: Exception) {
            Log.e(
                "LocationRepository",
                "Error refreshing friend locations, keeping existing state",
                e
            )
        }
    }

    /**
     * refreshProjectMemberLocations
     * Manually refreshes project member locations once
     * @param projectMembers List of project members whose locations should be refreshed
     * @param currentUserID The current user's ID to avoid self-updates
     */
    override suspend fun refreshProjectMemberLocations(
        projectMembers: List<ProjectMember>,
        currentUserID: String
    ) {
        Log.d("LocationRepository", "Manually refreshing project member locations")
        try {
            val refreshedLocations = retrieveProjectMemberLocations(projectMembers, currentUserID)
            _locationState.value = _locationState.value.copy(
                projectMemberLocations = refreshedLocations
            )
        } catch (e: Exception) {
            Log.e(
                "LocationRepository",
                "Error refreshing project member locations, keeping existing state",
                e
            )
        }
    }

    /**
     * retrieveFriendLocations
     * Helper method that retrieves location data for a list of friends
     * @param friends List of friends whose locations should be retrieved
     * @param currentUserID The current user's ID to avoid self-updates
     * @return List of UserLocation objects for friends with location data
     */
    private suspend fun retrieveFriendLocations(
        friends: List<Friend>,
        currentUserID: String = ""
    ): List<UserLocation> {
        val updatedLocations = mutableListOf<UserLocation>()

        //Iterates through each of the friends in the list provided
        friends.forEach { friend ->
            //Skips the current user
            if (currentUserID.isNotEmpty() && friend.uid == currentUserID) {
                return@forEach
            }

            //Retrieve the current friend's location from the realtime database
            getUserLocation(friend.uid, friend.displayName, friend.photourl)
                //If the process succeeds it's added to the list and the process moves onto the
                //next friend
                .onSuccess { userLocation ->
                    if (userLocation != null) {
                        updatedLocations.add(userLocation)
                    }
                }
                //If the process fails we don't add it to the list and log the error
                .onFailure { e ->
                    Log.e(
                        "LocationRepository",
                        "Failed to get friend location for ${friend.displayName}",
                        e
                    )
                }
        }

        //Return the new populated list
        return updatedLocations
    }

    /**
     * retrieveProjectMemberLocations
     * Helper method that retrieves location data for a list of project members
     * @param projectMembers List of project members whose locations should be retrieved
     * @param currentUserID The current user's ID to avoid self-updates
     * @return List of UserLocation objects for project members with location data
     */
    private suspend fun retrieveProjectMemberLocations(
        projectMembers: List<ProjectMember>,
        currentUserID: String = ""
    ): List<UserLocation> {
        val updatedLocations = mutableListOf<UserLocation>()

        projectMembers.forEach { member ->
            if (currentUserID.isNotEmpty() && member.uid == currentUserID) {
                return@forEach
            }

            getUserLocation(member.uid, member.displayName, member.photourl)
                .onSuccess { userLocation ->
                    if (userLocation != null) {
                        updatedLocations.add(userLocation)
                    }
                }
                .onFailure { e ->
                    Log.e(
                        "LocationRepository",
                        "Failed to get project member location for ${member.displayName}",
                        e
                    )
                }
        }

        return updatedLocations
    }

    /**
     * getUserLocation
     * Retrieves a user's location from the Firebase Realtime Database
     * @param uid The user's unique ID
     * @param displayName The user's display name (for logging)
     * @param photourl The user's photo URL
     * @return Result containing a UserLocation object or null if no location data exists
     */
    @AddTrace(name = "locationRepoGetUserLocation")
    private suspend fun getUserLocation(
        uid: String,
        displayName: String,
        photourl: String
    ): Result<UserLocation?> {
        return try {
            val userReference = realtime.reference.child("users/$uid")

            val locationData = userReference.get().await()

            if (!locationData.exists()) {
                return Result.success(null)
            }

            val lat = locationData.child("lat").getValue(Double::class.java)
            val long = locationData.child("long").getValue(Double::class.java)

            if (lat == null || long == null) {
                Log.d(
                    "LocationRepository",
                    "Missing location data for user: $displayName"
                )
                Result.success(null)
            } else {
                val userLocation = UserLocation(
                    uid = uid,
                    displayName = displayName,
                    photourl = photourl,
                    latitude = lat,
                    longitude = long
                )
                Result.success(userLocation)
            }
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error fetching location data for user: $displayName", e)
            Result.failure(e)
        }
    }
}