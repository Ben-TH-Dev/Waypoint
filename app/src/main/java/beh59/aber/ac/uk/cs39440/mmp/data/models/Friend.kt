package beh59.aber.ac.uk.cs39440.mmp.data.models

import androidx.annotation.Keep

/**
 * Friend
 * Represents another user of the application the current user has agreed to become friends with.
 * @param uid The unique identifier of the friend provided by Firebase authentication.
 * @param displayName The friend's display name provided by Firebase.
 * @param username The friend's username which is a custom data field populated during onboarding
 * @param photourl The friend's photourl provided by Firebase.
 * @param email The friend's email provided by Firebase.
 * @param phonenumber The friend's phone number which is a custom data field populated during
 * onboarding.
 * @param friendsSince The date that the friend became users with the current user.
 * @param lat An optional field that represents the friend's location in the real world.
 * @param long An optional field that represents the friend's location in the real world.
 */
@Keep
//Used to create a remote friend object from information retrieved from Firestore.
data class Friend(
    val uid: String = "",
    val displayName: String = "",
    val username: String = "",
    val photourl: String = "",
    val email: String = "",
    val phonenumber: String = "",
    val friendsSince: String = ""
)
