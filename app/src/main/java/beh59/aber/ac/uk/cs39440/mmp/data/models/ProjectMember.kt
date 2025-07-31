package beh59.aber.ac.uk.cs39440.mmp.data.models

import androidx.annotation.Keep

/**
 * ProjectMember
 * Represents and contains information about each user involved in a project
 * @param uid The unique identifier of the user
 * @param photourl The photo URL of the user provided by Firebase authentication
 * @param displayName The display name of the user provided by Firebase authentication
 * @param username The username, a custom data field populated during onboarding
 * @param role The user's role within the project.
 */
@Keep
data class ProjectMember(
    val uid: String = "",
    val photourl: String = "",
    val displayName: String = "",
    val username: String = "",
    val role: String = "",
)