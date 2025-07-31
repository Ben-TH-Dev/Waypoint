package beh59.aber.ac.uk.cs39440.mmp.data.repository

import beh59.aber.ac.uk.cs39440.mmp.data.models.Job
import beh59.aber.ac.uk.cs39440.mmp.data.models.Project
import beh59.aber.ac.uk.cs39440.mmp.data.models.ProjectMember
import beh59.aber.ac.uk.cs39440.mmp.data.models.states.ProjectState
import kotlinx.coroutines.flow.StateFlow

/**
 * IProjectRepository
 * An interface that serves as an abstraction for the methods required by the project systems
 * in the application.
 * @property projectState A StateFlow holding the state of data needed by project features that can
 * be observed in the UI
 * @property projects A StateFlow containing the list of projects that the current user is a member
 * of
 * @property jobs A StateFlow containing the list of jobs associated with the user's projects
 * @property groupedProjects A special StateFlow that holds a grouped organization of projects for
 * UI display
 */
interface IProjectRepository {
    val projectState: StateFlow<ProjectState>
    val projects: StateFlow<List<Project>>
    val jobs: StateFlow<List<Job>>
    val groupedProjects: StateFlow<Map<String, List<Project>>>

    //Project methods

    /**
     * getProjects
     * Defines a method that should retrieve projects for a given user and update state
     * @param currentUserID The unique identifier of the user to retrieve projects for
     * @return A StateFlow containing the list of projects
     */
    suspend fun getProjects(currentUserID: String): StateFlow<List<Project>>

    /**
     * submitProject
     * Defines a method that should create a new project in the database
     * @param project The project to be created
     * @param currentUserID The unique identifier of the user creating the project
     */
    suspend fun submitProject(project: Project, currentUserID: String)

    /**
     * addToProjectList
     * Defines a method that should update the projects list in state
     * @param newProjects The new list of projects to hold in state
     */
    fun addToProjectList(newProjects: List<Project>)

    /**
     * updateActiveProject
     * Defines a method that should update the active project in state
     * @param projectID The unique identifier of the project to be set as active
     */
    fun updateActiveProject(projectID: String?)

    /**
     * isUserMemberOfProject
     * Defines a method that should check if a user is a member of a project
     * @param userID The unique identifier of the user to check
     * @param projectID The unique identifier of the project to check
     * @return True if the user is a member of the project, false otherwise
     */
    fun isUserMemberOfProject(userID: String, projectID: String): Boolean

    /**
     * addProjectMember
     * Defines a method that should add a member to a project
     * @param projectID The unique identifier of the project to add the member to
     * @param memberID The unique identifier of the member to add
     * @param role The role of the member in the project
     * @return True if successful, false otherwise
     */
    suspend fun addProjectMember(
        projectID: String,
        memberID: String,
        currentUserID: String,
        role: String
    ): Boolean

    /**
     * deleteProject
     * Defines a method that should delete a project from the database
     * @param project The project to delete
     * @return True if successful, false otherwise
     */
    suspend fun deleteProject(project: Project): Boolean

    /**
     * updateGroupedProjects
     * Defines a method that should update the grouped projects in state
     */
    fun updateGroupedProjects()

    /**
     * removeMember
     * Defines a method that should remove a member from a project
     * @param project The project to remove the member from
     * @param member The member to remove
     * @return True if successful, false otherwise
     */
    suspend fun removeMember(project: Project, member: ProjectMember): Boolean

    //Job methods

    /**
     * getJobs
     * Defines a method that should retrieve jobs for a list of projects
     * @param projectsList The list of projects to retrieve jobs for
     * @return A StateFlow containing the list of jobs
     */
    suspend fun getJobs(projectsList: List<Project>): StateFlow<List<Job>>

    /**
     * submitJob
     * Defines a method that should create a new job in the database
     * @param job The job to be created
     * @return True if successful, false otherwise
     */
    suspend fun submitJob(job: Job): Boolean

    /**
     * updateJob
     * Defines a method that should update an existing job in the database
     * @param jobID The unique identifier of the job to update
     * @param updates The updates to apply to the job
     * @return True if successful, false otherwise
     */
    suspend fun updateJob(jobID: String, updates: Map<String, Any>): Boolean

    suspend fun updateProject(project: Project, updates: Map<String, Any>): Boolean

    /**
     * addToJobsList
     * Defines a method that should update the jobs list in state
     * @param newJobs The new list of jobs to hold in state
     */
    fun addToJobsList(newJobs: List<Job>)

    /**
     * deleteJob
     * Defines a method that should delete a job from the database
     * @param job The job to delete
     * @return True if successful, false otherwise
     */
    suspend fun deleteJob(job: Job): Boolean

    /**
     * toggleViewMode
     * Defines a method that should switch between the different view modes available
     * for displaying project information
     */
    fun toggleViewMode()
}