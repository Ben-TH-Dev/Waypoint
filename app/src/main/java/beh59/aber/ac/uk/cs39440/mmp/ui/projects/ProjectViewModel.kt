package beh59.aber.ac.uk.cs39440.mmp.ui.projects

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import beh59.aber.ac.uk.cs39440.mmp.data.models.Job
import beh59.aber.ac.uk.cs39440.mmp.data.models.Project
import beh59.aber.ac.uk.cs39440.mmp.data.models.ProjectMember
import beh59.aber.ac.uk.cs39440.mmp.data.models.states.ProjectState
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IProjectRepository
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IUserRepository
import beh59.aber.ac.uk.cs39440.mmp.domain.project.GetJobsUseCase
import beh59.aber.ac.uk.cs39440.mmp.domain.project.GetProjectsUseCase
import beh59.aber.ac.uk.cs39440.mmp.domain.project.SubmitJobUseCase
import beh59.aber.ac.uk.cs39440.mmp.domain.project.SubmitProjectUseCase
import beh59.aber.ac.uk.cs39440.mmp.utils.JobPriority
import beh59.aber.ac.uk.cs39440.mmp.utils.JobStatus
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ProjectViewModel
 * ViewModel responsible for managing project related operations and state.
 * @param userRepository Repository for user related operations
 * @param projectRepository Repository for project related operations
 * @param getProjectsUseCase Use case for retrieving projects
 * @param getJobsUseCase Use case for retrieving jobs
 * @param submitProjectUseCase Use case for submitting jobs
 * @param submitJobUseCase Use case for submitting jobs
 * @property uiState Exposes UI state from the repository to be used in the UI
 * @property groupedProjects Contains a map with a list of projects grouped alphabetically
 * @property currentUser Exposes the current user of the application from UserRepository
 */
@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    private val projectRepository: IProjectRepository,
    private val getProjectsUseCase: GetProjectsUseCase,
    private val getJobsUseCase: GetJobsUseCase,
    private val submitProjectUseCase: SubmitProjectUseCase,
    private val submitJobUseCase: SubmitJobUseCase
) : ViewModel() {
    //Retrieves ProjectState from the repository.
    val uiState: StateFlow<ProjectState> = projectRepository.projectState
    val groupedProjects: StateFlow<Map<String, List<Project>>> = projectRepository.groupedProjects

    private val currentUser get() = userRepository.userState.value.user

    init {
        viewModelScope.launch {
            userRepository.userState.collect { userState ->
                if (userState.user.uid != "UserState error" && !userState.isLoading) {
                    Log.d("ProjectViewModel", "Retrieving projects")
                    loadProjects()
                }
            }
        }
    }

    /**
     * loadProjects
     * Invokes GetProjectsUseCase and collects its result, updating state with each new project
     * and then grouping the list of projects before attempting to load jobs.
     */
    private fun loadProjects() {
        viewModelScope.launch {
            try {
                getProjectsUseCase(currentUser.uid).collect { projects ->
                    projectRepository.addToProjectList(projects)
                    projectRepository.updateGroupedProjects()
                    loadJobs()
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error loading projects", e)
            }
        }
    }

    /**
     * loadJobs
     * Once the projects list is loaded, this method is called by loadProjects, which invokes
     * GetJobsUseCase and collects its results and updates the job list in state
     */
    private fun loadJobs() {
        viewModelScope.launch {
            try {
                getJobsUseCase(uiState.value.projects).collect { jobs ->
                    projectRepository.addToJobsList(jobs)
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error loading jobs", e)
            }
        }
    }

    /**
     * getProjectByID
     * Compares the list of projects currently in memory and checks if any have the same ID as the
     * parameter
     */
    fun getProjectByID(projectID: String): Project? {
        return uiState.value.projects.find { it.projectID == projectID }
    }

    /**
     * getJobByID
     * Compares the list of jobs currently in memory and checks if any have the same ID as the
     * parameter
     */
    fun getJobByID(jobID: String): Job? {
        return uiState.value.jobs.find { it.jobID == jobID }
    }

    /**
     * getMutualProjects
     * Finds any projects that the friend with the ID given as a parameter is in
     */
    fun getMutualProjects(friendID: String): List<Project> {
        return uiState.value.projects.filter { project ->
            project.members.any { member -> member.uid == friendID }
        }
    }

    /**
     * submitProject
     * Creates a local Project and assigns the current user as a moderator, then calls
     * SubmitProjectUseCase to submit to Firestore
     * @param title The title of the new project
     * @param description The description of the new project
     */
    fun submitProject(title: String, description: String) {
        viewModelScope.launch {
            try {
                val project = Project(
                    projectID = "",
                    title = title,
                    description = description,
                    role = "moderator"
                )

                submitProjectUseCase(project, currentUser.uid)
                loadProjects()
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error submitting project", e)
            }
        }
    }

    /**
     * toggleViewMode
     * Toggles the view mode state in state
     * */
    fun toggleViewMode() {
        viewModelScope.launch {
            projectRepository.toggleViewMode()
        }
    }

    /**
     * submitJob
     * Creates a local Job object and calls SubmitJobUseCase to save to Firestore
     * @param jobTitle The title of the new job
     * @param jobDescription The description of the new job
     * @param projectID The unique identifier of the project this job belongs to
     * @param priority The priority of the job
     * @param status The status of the job
     * @param deadline The deadline for the job
     */
    fun submitJob(
        jobTitle: String,
        jobDescription: String,
        projectID: String,
        priority: JobPriority,
        status: JobStatus,
        deadline: Timestamp,
    ) {
        viewModelScope.launch {
            try {
                val job = Job(
                    jobID = "",  //Automatically assigned by Firestore as the document ID.
                    jobTitle = jobTitle,
                    jobDescription = jobDescription,
                    projectID = projectID,
                    priority = priority,
                    status = status,
                    deadline = deadline,
                    assignedUserUID = ""
                    //Starts off initially empty and can be configured by the moderator of the
                    //project later.
                )

                //Invokes submitJobUseCase and if successful refreshes the job data available in the
                //app's memory
                val success = submitJobUseCase(job)

                if (success) {
                    loadJobs()
                    Log.d("ProjectViewModel", "Job submitted successfully: $jobTitle")
                } else {
                    Log.e("ProjectViewModel", "Failed to submit job")
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error submitting job", e)
            }
        }
    }

    /**
     * setActiveProject
     * Updates the active project ID in state
     * @param projectID The unique identifier of the project to set as active, or null to clear the
     * active project
     */
    fun setActiveProject(projectID: String?) {
        projectRepository.updateActiveProject(projectID)
    }

    /**
     * getActiveProject
     * Retrieves the currently active Project based on the activeProjectID stored in the state
     * @return The active Project object, or null if no project is active
     */
    fun getActiveProject(): Project? {
        val activeProjectID = uiState.value.activeProjectID ?: return null
        return uiState.value.projects.find { it.projectID == activeProjectID }
    }

    /**
     * addProjectMember
     * Adds a user as a member to a specified project with a given role
     * @param projectID The unique identifier of the project to add the member to
     * @param memberID The unique identifier of the user to add as a member
     * @param role The role to assign to the new member with a default member value
     */
    fun addProjectMember(projectID: String, memberID: String, role: String = "member") {
        viewModelScope.launch {
            try {
                val success = projectRepository.addProjectMember(projectID, memberID, currentUser.uid, role)
                if (success) {
                    loadProjects()
                    Log.d(
                        "ProjectViewModel",
                        "Successfully added member $memberID to project $projectID"
                    )
                } else {
                    Log.e(
                        "ProjectViewModel",
                        "Failed to add member $memberID to project $projectID"
                    )
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error adding member to project", e)
            }
        }
    }

    /**
     * deleteProject
     * Communicates with the repository to delete the specified project
     * @param project The project to delete.
     */
    fun deleteProject(project: Project) {
        viewModelScope.launch {
            try {
                val success = projectRepository.deleteProject(project)
                if (success) {
                    loadProjects()
                    Log.d("ProjectViewModel", "Successfully deleted existing project")
                } else {
                    Log.e("ProjectViewModel", "Failed to delete project")
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error deleting project", e)
            }
        }
    }

    /**
     * assignJobToUser
     * Assigns a specific job to a user by updating the job's assignedUserUID field via
     * communication with the repository
     * @param jobID The unique identifier of the job
     * @param userUID The unique identifier of the user being assigned the job
     */
    fun assignJobToUser(jobID: String, userUID: String) {
        viewModelScope.launch {
            try {
                val updates = mapOf("assignedUserUID" to userUID)
                val success = projectRepository.updateJob(jobID, updates)
                if (success) {
                    //If the process is successful, we reload job data for the user.
                    loadJobs()
                    Log.d("ProjectViewModel", "Successfully assigned job $jobID to user $userUID")
                } else {
                    Log.e("ProjectViewModel", "Failed to assign job $jobID to user $userUID")
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error assigning job to user", e)
            }
        }
    }

    /**
     * unassignJob
     * Unassigns a job by clearing the assignedUserUID field via communication with the repository
     * @param jobID The unique identifier of the job to unassign.
     */
    fun unassignJob(jobID: String) {
        viewModelScope.launch {
            try {
                val updates = mapOf("assignedUserUID" to "")
                val success = projectRepository.updateJob(jobID, updates)
                if (success) {
                    //If the process is successful, we reload job data for the user.
                    loadJobs()
                    Log.d("ProjectViewModel", "Successfully unassigned job $jobID")
                } else {
                    Log.e("ProjectViewModel", "Failed to unassign job $jobID")
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error unassigning job", e)
            }
        }
    }

    /**
     * deleteJob
     * Deletes the specified job via communication with the repository
     * @param job The job to delete
     */
    fun deleteJob(job: Job) {
        viewModelScope.launch {
            try {
                val success = projectRepository.deleteJob(job)
                if (success) {
                    loadProjects()
                    Log.d("ProjectViewModel", "Successfully deleted existing job")
                } else {
                    Log.e("ProjectViewModel", "Failed to delete job")
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error deleting job", e)
            }
        }
    }

    /**
     * removeMember
     * Removes a member from the specified project via communication with the repository
     * @param project The project from which to remove the member
     * @param member The ProjectMember to remove
     */
    fun removeMember(project: Project, member: ProjectMember) {
        viewModelScope.launch {
            try {
                val success = projectRepository.removeMember(project, member)
                if (success) {
                    loadProjects()
                } else {
                    Log.e("ProjectViewModel", "Failed to remove member")
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error removing member.", e)
            }
        }
    }

    fun updateJob(job: Job, updates: Map<String, Any>) {
        viewModelScope.launch {
            try {
                val success = projectRepository.updateJob(job.jobID, updates)
                if (success) {
                    loadProjects()
                } else {
                    Log.e("ProjectViewModel", "Failed to edit job")
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error editing job", e)
            }
        }
    }

    fun updateProject(project: Project, updates: Map<String, Any>) {
        viewModelScope.launch {
            try {
                val success = projectRepository.updateProject(project, updates)
                if (success) {
                    loadProjects()
                } else {
                    Log.e("ProjectViewModel", "Failed to edit job")
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error editing job", e)
            }
        }
    }
}