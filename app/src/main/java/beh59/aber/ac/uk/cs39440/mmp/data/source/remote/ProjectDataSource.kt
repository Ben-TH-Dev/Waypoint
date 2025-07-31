package beh59.aber.ac.uk.cs39440.mmp.data.source.remote

import android.util.Log
import beh59.aber.ac.uk.cs39440.mmp.data.models.Job
import beh59.aber.ac.uk.cs39440.mmp.data.models.Project
import beh59.aber.ac.uk.cs39440.mmp.data.models.ProjectMember
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * ProjectDataSource
 * Handles direct communication with Firestore for project and job data
 * @param db An instance of Firebase Firestore
 */
class ProjectDataSource @Inject constructor(
    private val db: FirebaseFirestore
) {
    /**
     * retrieveProjects
     * Retrieves projects that a user is a member of from Firestore
     * @param currentUserUID The unique identifier of the current user
     * @return A list of Project objects with populated data
     */
    @AddTrace(name = "projectDSRetrieveProjects")
    suspend fun retrieveProjects(currentUserUID: String): List<Project> {
        val projectList = mutableListOf<Project>()

        //Queries the project-memberships collection for documents with a field matching the current
        //user ID given as a parameter
        val projectMembershipQuery = db.collection("project-memberships")
            .whereEqualTo("uid", currentUserUID)
            .get()
            .await()

        //Iterates through each document returned by the query
        for (membership in projectMembershipQuery.documents) {
            try {
                //Gets the project ID from the membership document to lookup the actual project
                //in the projects collection and retrieve its details.
                val projectID = membership.getString("projectID")
                //Role is stored in the same place as the project members.
                val role = membership.getString("role") ?: "Unknown"

                if (projectID != null) {
                    //Uses the projectID stored in the membership document to look up the project
                    //in the projects collection and retrieve its details.
                    val projectDetails = db.collection("projects")
                        .document(projectID)
                        .get()
                        .await()

                    if (projectDetails.exists()) {
                        val membersQuery = db.collection("project-memberships")
                            .whereEqualTo("projectID", projectID)
                            .get()
                            .await()

                        //Responsible for acquiring each member of the project and relevant
                        //information about them so we can access their information quickly and
                        //easily and prevent further calls to the database.
                        val members = membersQuery.documents.mapNotNull { memberDoc ->
                            val uid = memberDoc.getString("uid") ?: return@mapNotNull null
                            val memberRole = memberDoc.getString("role") ?: "Unknown"

                            //Locates the project member's document in the users collection using
                            //their UID, which contains more data.
                            val userDoc = db.collection("users").document(uid).get().await()

                            val displayName = userDoc.getString("displayName") ?: ""
                            val photourl = userDoc.getString("photourl") ?: ""

                            //Retrieves the username of the project member from the seperate
                            //usernames collection
                            val usernameQuery = db.collection("usernames")
                                .whereEqualTo("uid", uid)
                                .get()
                                .await()

                            val username =
                                if (usernameQuery.isEmpty) "" else usernameQuery.documents.first().id

                            //Creates a ProjectMember object from the retrieved data, allowing us
                            //to easily access the local data quickly.
                            ProjectMember(
                                uid = uid,
                                role = memberRole,
                                displayName = displayName,
                                photourl = photourl,
                                username = username
                            )
                        }

                        //Declares a new project object and populates its fields with the retrieved
                        //data
                        val project = Project(
                            projectID = projectID,
                            title = projectDetails.getString("title") ?: "Error",
                            description = projectDetails.getString("description") ?: "Error",
                            role = role,
                            members = members
                        )

                        //Finally adds the completed project to the application's local list of
                        //projects.
                        projectList.add(project)
                    } else {
                        Log.d("ProjectDataSource", "Error retrieving project document")
                    }
                }
            } catch (e: Exception) {
                Log.d("ProjectDataSource", "Error retrieving individual project")
            }
        }

        return projectList
    }

    /**
     * retrieveJobs
     * Retrieves jobs of projects the user is a member of from Firestore
     * @param projectsList The list of projects to retrieve jobs for
     * @return A list of Job objects
     */
    @AddTrace(name = "projectDSRetrieveJobs")
    suspend fun retrieveJobs(projectsList: List<Project>): List<Job> {
        Log.d("ProjectDataSource", "Retrieving jobs")

        val jobList = mutableListOf<Job>()

        //For each project in memory, queries the jobs collection for documents with a matching
        //projectID field
        projectsList.forEach { project ->
            val jobQuery = db.collection("jobs")
                .whereEqualTo("projectID", project.projectID)
                .get()
                .await()

            //Iterates through the list of returned documents and for each casts it to a Job object
            //and copies the jobID (stored separately as the document ID) to the object.
            for (job in jobQuery.documents) {
                try {
                    val jobID = job.id

                    val jobData = job.toObject(Job::class.java) ?: continue

                    val completeJob = jobData.copy(
                        jobID = jobID
                    )

                    jobList.add(completeJob)
                } catch (e: Exception) {
                    //If there's any problems, log the error
                    Log.d("ProjectDataSource", "Error retrieving jobs", e)
                }
            }
        }

        //Return the final list of retrieved jobs
        return jobList
    }
}