package beh59.aber.ac.uk.cs39440.mmp.data.models

import androidx.annotation.Keep

/**
 * UserLocation
 * A data class that is specialised for holding location data of users in the application. It is
 * role agnostic, so it can be used by the user, the user's friends, or project members, and holds
 * additional information about the user for quick access and usage by the UI
 * @param uid The unique identifier of the user the data belongs to
 * @param displayName The display name of the user provided by Firebase authentication
 * @param photourl The photo URL of the user provided by Firebase authentication
 * @param latitude The latitude of the user
 * @param longitude The longitude of the user
 */
@Keep
data class UserLocation(
    val uid: String,
    val displayName: String,
    val photourl: String,
    val latitude: Double,
    val longitude: Double
)