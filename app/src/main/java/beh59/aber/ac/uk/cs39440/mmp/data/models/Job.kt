package beh59.aber.ac.uk.cs39440.mmp.data.models

import androidx.annotation.Keep
import beh59.aber.ac.uk.cs39440.mmp.utils.JobPriority
import beh59.aber.ac.uk.cs39440.mmp.utils.JobStatus
import com.google.firebase.Timestamp

/**
 * Job
 * Represents a job for use in the projects system.
 * @param jobID The unique identifier of each job, which is also the document ID of each job's
 * document in Firestore.
 * @param assignedUserUID The unique identifier of the user assigned to the job by the moderator.
 * @param deadline The time that the job expires as marked by the moderator.
 * @param jobDescription The textual description of the job and what it requires
 * @param jobTitle The textual title of the job, usually a brief summary of what needs to be done
 * @param priority Serves as a marker representing the importance of the job.
 * @param projectID The unique identifier of the project the job falls under.
 * @param status The completion status of the job.
 */
@Keep
data class Job(
    val jobID: String = "Error",
    val assignedUserUID: String = "",
    val deadline: Timestamp = Timestamp.now(),
    val jobDescription: String = "Error",
    val jobTitle: String = "Error",
    val priority: JobPriority = JobPriority.HIGH,
    val projectID: String = "Error",
    val status: JobStatus = JobStatus.COMPLETE
)
