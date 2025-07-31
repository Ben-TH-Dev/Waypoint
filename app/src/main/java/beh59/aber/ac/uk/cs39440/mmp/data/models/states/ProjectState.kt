package beh59.aber.ac.uk.cs39440.mmp.data.models.states

import beh59.aber.ac.uk.cs39440.mmp.data.models.Job
import beh59.aber.ac.uk.cs39440.mmp.data.models.Project
import beh59.aber.ac.uk.cs39440.mmp.utils.ViewMode

/**
 * ProjectState
 * Represents the state of projects in the application
 * @param projects List of all projects the user is part of
 * @param jobs List of all jobs associated with the user's projects
 * @param viewMode The current view mode for displaying projects
 * @param activeProjectID The ID of the currently selected project
 */
data class ProjectState(
    val projects: List<Project> = emptyList(),
    val jobs: List<Job> = emptyList(),
    val viewMode: ViewMode = ViewMode.GRID,
    val activeProjectID: String? = null,
)
