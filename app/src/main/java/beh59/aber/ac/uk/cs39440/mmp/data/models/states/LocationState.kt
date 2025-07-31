package beh59.aber.ac.uk.cs39440.mmp.data.models.states

import androidx.annotation.Keep
import beh59.aber.ac.uk.cs39440.mmp.data.models.UserLocation

/**
 * LocationState
 * Represents the state of location data in the application
 * @param latitude The user's current latitude
 * @param longitude The user's current longitude
 * @param hasInitialLocation Tracks if the app has an initial location ready or not for the current
 * user
 * @param friendLocations A list of user locations belonging to each friend of the user, where
 * applicable
 * @param projectMemberLocations A list of user locations belonging to project members, where
 * applicable
 */
@Keep
data class LocationState(
    val latitude: Double = 12345.6,
    val longitude: Double = 12345.6,
    val hasInitialLocation: Boolean = false,
    val friendLocations: List<UserLocation> = emptyList(),
    val projectMemberLocations: List<UserLocation> = emptyList(),
    val locationUpdatesEnabled: Boolean = false
)