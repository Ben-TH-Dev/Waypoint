package beh59.aber.ac.uk.cs39440.mmp.data.repository.impl

import android.util.Log
import beh59.aber.ac.uk.cs39440.mmp.data.models.Job
import beh59.aber.ac.uk.cs39440.mmp.data.models.Project
import beh59.aber.ac.uk.cs39440.mmp.data.models.ProjectMember
import beh59.aber.ac.uk.cs39440.mmp.data.models.states.ProjectState
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IProjectRepository
import beh59.aber.ac.uk.cs39440.mmp.data.source.remote.ProjectDataSource
import beh59.aber.ac.uk.cs39440.mmp.utils.ViewMode
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ProjectRepository
 * An implementation of the IProjectRepository interface
 * Contains the business logic related to projects and jobs within the application. Handles
 * calls to Firebase and communication with ProjectDataSource
 * @param projectDataSource Contains network logic that communicates with Firestore to retrieve and
 * update project and job data
 * @param db The current instance of Firestore used by the application
 * @property projectState A public StateFlow that exposes ProjectState and is observable by the
 * UI
 * @property projects A public StateFlow that exposes the list of projects for the current user
 * @property jobs A public StateFlow that exposes the list of jobs associated with the user's
 * projects
 * @property groupedProjects A public StateFlow that exposes the grouped projects for UI display
 */
class ProjectRepository @Inject constructor(
    private val projectDataSource: ProjectDataSource,
    private val db: FirebaseFirestore
) : IProjectRepository {
    private val _projectState = MutableStateFlow(ProjectState())
    override val projectState: StateFlow<ProjectState> get() = _projectState.asStateFlow()

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    override val projects: StateFlow<List<Project>> = _projects

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    override val jobs: StateFlow<List<Job>> = _jobs

    private val _groupedProjects = MutableStateFlow<Map<String, List<Project>>>(emptyMap())
    override val groupedProjects: StateFlow<Map<String, List<Project>>> = _groupedProjects

    //Project methods

    /**
     * getProjects
     * Communicates with ProjectDataSource to retrieve projects for a given user from the
     * ProjectDataSource and updates state
     * @param currentUserID The unique identifier of the user to retrieve projects for
     * @return A StateFlow containing the list of projects
     */
    @AddTrace(name = "projectRepoGetProjects")
    override suspend fun getProjects(currentUserID: String): StateFlow<List<Project>> {
        return try {
            val newProjects = projectDataSource.retrieveProjects(currentUserID)

            _projects.value = newProjects
            _projectState.value = _projectState.value.copy(
                projects = newProjects
            )

            projects
        } catch (e: Exception) {
            Log.e("ProjectRepository", "Error retrieving projects")
            _projects.value = emptyList()
            projects
        }
    }

    /**
     * submitProject
     * Creates a new project in Firestore given its data and the user ID of the creator
     * @param project The project to be created
     * @param currentUserID The unique identifier of the user creating the project
     */
    @AddTrace(name = "projectRepoSubmitProject")
    override suspend fun submitProject(project: Project, currentUserID: String) {
        withContext(Dispatchers.IO) {
            try {
                db.runTransaction { transaction ->
                    val projectReference = db.collection("projects").document()
                    val projectMembershipReference = db.collection("project-memberships").document()

                    val projectDocument = transaction.get(projectReference)
                    if (projectDocument.exists()) {
                        throw Exception("Project document already exists")
                    }

                    transaction.set(
                        projectReference, hashMapOf(
                            "title" to project.title,
                            "description" to project.description
                        )
                    )

                    transaction.set(
                        projectMembershipReference, hashMapOf(
                            "dateJoined" to Timestamp.now(),
                            "projectID" to projectReference.id,
                            "role" to "moderator",
                            "uid" to currentUserID
                        )
                    )
                }.await()
            } catch (e: Exception) {
                Log.e("ProjectRepository", "Error submitting project: ${e.message}")
                throw e
            }
        }
    }

    /**
     * addToProjectList
     * Updates the projects list in state with a new list of projects
     * @param newProjects The new list of projects to hold in state
     */
    override fun addToProjectList(newProjects: List<Project>) {
        _projectState.value = _projectState.value.copy(
            projects = newProjects
        )
    }

    /**
     * updateActiveProject
     * Updates the state with the value of the active project ID, selected on the map screen
     * @param projectID The unique identifier of the project to be set as active
     */
    override fun updateActiveProject(projectID: String?) {
        _projectState.value = _projectState.value.copy(
            activeProjectID = projectID
        )
    }

    /**
     * isUserMemberOfProject
     * Checks if a user is a member of a given project by comparing the user ID with the members of
     * the project
     * @param userID The unique identifier of the user to check
     * @param projectID The unique identifier of the project to check
     * @return True if the user is a member of the project, false otherwise
     */
    override fun isUserMemberOfProject(userID: String, projectID: String): Boolean {
        val project = _projectState.value.projects.find { it.projectID == projectID }
        if (project != null) {
            return project.members.any { it.uid == userID }
        } else {
            Log.d("ProjectRepository", "Failed to query project membership")
            return false
        }
    }

    /**
     * addProjectMember
     * Adds a new member to a project in Firestore and refreshes project and job data
     * @param projectID The unique identifier of the project to add the member to
     * @param memberID The unique identifier of the member to add
     * @param role The role of the member in the project
     * @return True if successful, false otherwise
     */
    @AddTrace(name = "projectRepoAddMember")
    override suspend fun addProjectMember(
        projectID: String,
        memberID: String,
        currentUserID: String,
        role: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val membershipRef = db.collection("project-memberships").document()
                val membershipData = hashMapOf(
                    "dateJoined" to Timestamp.now(),
                    "projectID" to projectID,
                    "role" to role,
                    "uid" to memberID
                )

                membershipRef.set(membershipData).await()

                val refreshedProjects = projectDataSource.retrieveProjects(currentUserID)

                _projects.value = refreshedProjects
                _projectState.value = _projectState.value.copy(
                    projects = refreshedProjects
                )

                val refreshedJobs = projectDataSource.retrieveJobs(refreshedProjects)
                _jobs.value = refreshedJobs
                _projectState.value = _projectState.value.copy(
                    jobs = refreshedJobs
                )

                true
            } catch (e: Exception) {
                Log.e("ProjectRepository", "Error adding member to project: ${e.message}")
                false
            }
        }
    }

    /**
     * deleteProject
     * Deletes a project and all its associated data from Firestore
     * @param project The project to delete
     * @return True if successful, false otherwise
     */
    override suspend fun deleteProject(project: Project): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val projectRef = db.collection("projects")
                    .document(project.projectID)
                    .get()
                    .await()

                val membershipRef = db.collection("project-memberships")
                    .whereEqualTo("projectID", project.projectID)
                    .get()
                    .await()

                val jobRef = db.collection("jobs")
                    .whereEqualTo("projectID", project.projectID)
                    .get()
                    .await()

                if (projectRef.exists()) {
                    for (document in membershipRef.documents) {
                        db.collection("project-memberships")
                            .document(document.id)
                            .delete()
                            .await()
                    }

                    for (document in jobRef.documents) {
                        db.collection("jobs")
                            .document(document.id)
                            .delete()
                            .await()
                    }

                    db.collection("projects")
                        .document(project.projectID)
                        .delete()
                        .await()

                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("ProjectRepository", "Failed to delete project", e)
                false
            }
        }
    }

    /**
     * updateGroupedProjects
     * Updates the grouped projects state by organising projects by their first letter for easier
     * display in the UI
     */
    override fun updateGroupedProjects() {
        val projects = _projectState.value.projects
        val grouped = projects
            .sortedBy { it.title }
            .groupBy {
                it.title.first().uppercaseChar().toString()
            }
        _groupedProjects.value = grouped
    }

    /**
     * removeMember
     * Removes a member from a project by deleting their membership document from Firestore
     * @param project The project to remove the member from
     * @param member The member to remove
     * @return True if successful, false otherwise
     */
    override suspend fun removeMember(project: Project, member: ProjectMember): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val jobsReference = db.collection("jobs")
                    .whereEqualTo("projectID", project.projectID)
                    .whereEqualTo("assignedUserUID", member.uid)
                    .get()
                    .await()

                for (jobDocument in jobsReference.documents) {
                    db.collection("jobs")
                        .document(jobDocument.id)
                        .update("assignedUserUID", "")
                        .await()
                }

                val membershipReference = db.collection("project-memberships")
                    .whereEqualTo("projectID", project.projectID)
                    .whereEqualTo("uid", member.uid)
                    .get()
                    .await()

                if (membershipReference.isEmpty) {
                    return@withContext false
                }

                //Gets the first document in the list returned, as there should only be one
                //membership document for one user in a given project.
                val membershipDocument = membershipReference.documents.first()

                db.collection("project-memberships")
                    .document(membershipDocument.id)
                    .delete()
                    .await()

                true
            } catch (e: Exception) {
                Log.e("ProjectRepository", "Failed to remove member from project", e)
                false
            }
        }
    }

    //Job methods

    /**
     * getJobs
     * Retrieves jobs for a list of projects via communication with ProjectDataSource and updates
     * state with the results
     * @param projectsList The list of projects to retrieve jobs for
     * @return A StateFlow containing the list of jobs
     */
    @AddTrace(name = "projectRepoGetJobs")
    override suspend fun getJobs(projectsList: List<Project>): StateFlow<List<Job>> {
        return try {
            val newJobs = projectDataSource.retrieveJobs(projectsList)

            _jobs.value = newJobs
            _projectState.value = _projectState.value.copy(
                jobs = newJobs
            )

            jobs
        } catch (e: Exception) {
            _jobs.value = emptyList()
            jobs
        }
    }

    /**
     * submitJob
     * Creates a new job in Firestore with the data provided
     * @param job The job to be created
     * @return True if successful, false otherwise
     */
    @AddTrace(name = "projectRepoSubmitJob")
    override suspend fun submitJob(job: Job): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val jobData = hashMapOf(
                    "assignedUserUID" to job.assignedUserUID,
                    "deadline" to job.deadline,
                    "jobDescription" to job.jobDescription,
                    "jobTitle" to job.jobTitle,
                    "priority" to job.priority.name,
                    "projectID" to job.projectID,
                    "status" to job.status.name
                )

                db.collection("jobs").add(jobData).await()
                true
            } catch (e: Exception) {
                Log.e("ProjectRepository", "Error submitting job: ${e.message}")
                false
            }
        }
    }

    /**
     * updateJob
     * Updates an existing job in Firestore with the provided updates
     * @param jobID The unique identifier of the job to update
     * @param updates A map of field names to their new values
     * @return True if successful, false otherwise
     */
    @AddTrace(name = "projectRepoUpdateJob")
    override suspend fun updateJob(jobID: String, updates: Map<String, Any>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val jobQuery = db.collection("jobs")
                    .whereEqualTo("jobID", jobID)
                    .get()
                    .await()

                if (jobQuery.isEmpty) {
                    db.collection("jobs")
                        .document(jobID)
                        .update(updates)
                        .await()
                } else {
                    val jobDoc = jobQuery.documents.first()
                    db.collection("jobs")
                        .document(jobDoc.id)
                        .update(updates)
                        .await()
                }

                val refreshedJobs = projectDataSource.retrieveJobs(_projectState.value.projects)
                _jobs.value = refreshedJobs
                _projectState.value = _projectState.value.copy(
                    jobs = refreshedJobs
                )

                true
            } catch (e: Exception) {
                Log.e("ProjectRepository", "Error updating job: ${e.message}")
                false
            }
        }
    }

    override suspend fun updateProject(project: Project, updates: Map<String, Any>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                 db.collection("projects")
                    .document(project.projectID)
                    .update(updates)
                    .await()

                true
            } catch (e: Exception) {
                Log.e("ProjectRepository", "Error updating project")
                false
            }
        }
    }

    /**
     * addToJobsList
     * Updates the jobs list in state with a new list of jobs
     * @param newJobs The new list of jobs to hold in state
     */
    override fun addToJobsList(newJobs: List<Job>) {
        _projectState.value = _projectState.value.copy(
            jobs = newJobs
        )
    }

    /**
     * deleteJob
     * Deletes a job from Firestore
     * @param job The job to delete
     * @return True if successful, false otherwise
     */
    override suspend fun deleteJob(job: Job): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val jobReference = db.collection("jobs")
                    .document(job.jobID)
                    .get()
                    .await()

                if (jobReference.exists()) {
                    db.collection("jobs")
                        .document(job.jobID)
                        .delete()
                        .await()

                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("ProjectRepository", "Failed to delete job", e)
                false
            }
        }
    }

    /**
     * toggleViewMode
     * Switches between the different view modes available
     */
    override fun toggleViewMode() {
        val currentViewMode = _projectState.value.viewMode
        _projectState.value = _projectState.value.copy(
            viewMode = if (currentViewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
        )
    }
}