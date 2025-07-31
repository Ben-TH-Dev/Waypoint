package beh59.aber.ac.uk.cs39440.mmp.domain.project

import beh59.aber.ac.uk.cs39440.mmp.data.models.Job
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IProjectRepository
import javax.inject.Inject

/**
 * SubmitJobUseCase
 * Encapsulates and coordinates logic related to submitting new job data for projects
 * @param projectRepository Contains business logic and Firestore calls related to project and job
 * systems in the application
 */
class SubmitJobUseCase @Inject constructor(
    private val projectRepository: IProjectRepository
) {
    /**
     * invoke
     * Called when SubmitJobUseCase is called. Calls ProjectRepository to handle the business logic
     * @param job The Job object to be submitted to Firestore
     * @return A boolean indicating whether the submission was successful
     */
    suspend operator fun invoke(job: Job): Boolean {
        return projectRepository.submitJob(job)
    }
}

