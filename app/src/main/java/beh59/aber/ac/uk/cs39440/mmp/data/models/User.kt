package beh59.aber.ac.uk.cs39440.mmp.data.models

import androidx.annotation.Keep

/**
 * User
 * Represents the current user of the application and stores their data after network retrieval
 * @param uid The unique identifier of the user, provided by Firebase authentication
 * @param displayName The display name of the user provided by Firebase authentication
 * @param username A custom data field populated during the app's onboarding
 * @param photourl The photo URL of the user provided by Firebase authentication
 * @param email The email address of the user provided by Firebase authentication
 * @param phonenumber A custom data field populated during the app's onboarding
 */
@Keep
data class User(
    val uid: String = "",
    val displayName: String = "",
    val username: String = "",
    val photourl: String = "",
    val email: String = "",
    val phonenumber: String = ""
)
