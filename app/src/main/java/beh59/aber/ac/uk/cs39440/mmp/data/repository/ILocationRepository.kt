package beh59.aber.ac.uk.cs39440.mmp.data.repository

import beh59.aber.ac.uk.cs39440.mmp.data.models.Friend
import beh59.aber.ac.uk.cs39440.mmp.data.models.ProjectMember
import beh59.aber.ac.uk.cs39440.mmp.data.models.User
import beh59.aber.ac.uk.cs39440.mmp.data.models.states.LocationState
import kotlinx.coroutines.flow.StateFlow

/**
 * ILocationRepository
 * An abstraction of the methods required to share and load location within the app
 * @property locationState A StateFlow holding the state of data needed by location features that
 * can be observed in the UI
 */
interface ILocationRepository {
    val locationState: StateFlow<LocationState>

    /**
     * updateLocation
     * Defines a method that should update the current latitude and longitude in state.
     * @param latitude The new latitude value.
     * @param longitude The new longitude value.
     */
    fun updateLocation(latitude: Double, longitude: Double)

    /**
     * updateFetchedStatus
     * Defines a method that should update the status indicating if location data fetching is
     * complete in state.
     * @param completed True if fetching is complete, false otherwise.
     */
    fun updateFetchedStatus(completed: Boolean)

    /**
     * roundToFiveDecimalPlaces
     * Defines a helper method to round a Double value to five decimal places, used for trimming
     * location data
     * @param value The Double value to round.
     * @return The rounded Double value.
     */
    fun roundToFiveDecimalPlaces(value: Double): Double

    /**
     * removeLocationData
     * Defines a method that should remove the location data associated with a specific user from
     * storage.
     * @param user The user whose location data should be removed.
     * @return A Result indicating success or failure.
     */
    suspend fun removeLocationData(user: User): Result<Unit>

    /**
     * saveUserLocation
     * Defines a method that should save the user's current location to storage.
     * @param user The user whose location is being saved.
     * @param latitude The user's current latitude.
     * @param longitude The user's current longitude.
     * @return A Result indicating success or failure.
     */
    suspend fun saveUserLocation(user: User, latitude: Double, longitude: Double): Result<Unit>

    /**
     * initializeLocationSharing
     * Defines a method to set the initial state of location sharing preference.
     * @param isEnabled The initial status of location sharing.
     */
    fun initializeLocationSharing(isEnabled: Boolean)

    /**
     * toggleLocationUpdates
     * Defines a method that should enable or disable location updates based on a boolean flag and
     * given the current user.
     * @param locationEnabled True to enable location updates, false to disable.
     * @param currentUser The user whose location updating is being toggled.
     */
    suspend fun toggleLocationUpdates(locationEnabled: Boolean, currentUser: User)

    /**
     * toggleLocationUpdates (Overload)
     * Defines a method that should toggle the current state of location updates for the given user.
     * @param currentUser The user whose location updating is being toggled
     */
    suspend fun toggleLocationUpdates(currentUser: User)

    /**
     * startLocationUpdates
     * Defines a method that should start listening for location updates for a list of friends.
     * @param friends The list of friends whose locations should be retrieved
     * @param currentUserID The unique identifier of the current user
     */
    suspend fun startLocationUpdates(friends: List<Friend>, currentUserID: String)

    /**
     * startProjectMemberLocationUpdates
     * Defines a method that should start listening for location updates for a list of project
     * members.
     * @param projectMembers The list of project members whose locations should be monitored.
     * @param currentUserID The unique identifier of the current user
     */
    suspend fun startProjectMemberLocationUpdates(
        projectMembers: List<ProjectMember>,
        currentUserID: String
    )

    /**
     * refreshFriendLocations
     * Defines a method that should manually refresh location data for the specified friends.
     * @param friends The list of friends whose locations should be refreshed.
     * @param currentUserID The unique identifier of the current user
     */
    suspend fun refreshFriendLocations(friends: List<Friend>, currentUserID: String)

    /**
     * refreshProjectMemberLocations
     * Defines a method that should manually refresh location data for the specified project
     * members.
     * @param projectMembers The list of project members whose locations should be refreshed.
     * @param currentUserID The unique identifier of the user initiating the refresh.
     */
    suspend fun refreshProjectMemberLocations(
        projectMembers: List<ProjectMember>,
        currentUserID: String
    )
}