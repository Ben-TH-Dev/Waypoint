package beh59.aber.ac.uk.cs39440.mmp.data.models

import androidx.annotation.Keep

/**
 * Project
 * Represents a project in the application and holds relevant data about it
 * @param projectID The unique identifier of the project.
 * @param title The textual title of the project, usually a summary of its purpose.
 * @param description The textual description of the project.
 * @param role The current user's role in the project.
 * @param members A list of the project's members.
 */
@Keep
data class Project(
    val projectID: String = "Error",
    val title: String = "Error",
    val description: String = "Error",
    val role: String = "Error",
    val members: List<ProjectMember> = emptyList()
)
