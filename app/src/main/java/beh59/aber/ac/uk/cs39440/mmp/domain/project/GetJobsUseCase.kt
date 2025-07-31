package beh59.aber.ac.uk.cs39440.mmp.domain.project

import beh59.aber.ac.uk.cs39440.mmp.data.models.Job
import beh59.aber.ac.uk.cs39440.mmp.data.models.Project
import beh59.aber.ac.uk.cs39440.mmp.data.repository.impl.ProjectRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * GetJobsUseCase
 * Encapsulates and coordinates logic related to retrieving job data for projects
 * @param projectRepository Contains business logic and Firestore calls related to project and job
 * systems in the application
 */
class GetJobsUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    /**
     * invoke
     * Called when GetJobsUseCase is called. Calls ProjectRepository to retrieve jobs
     * @param projectsList A list of projects to retrieve jobs for
     * @return A StateFlow containing a list of Job objects associated with the provided projects
     */
    suspend operator fun invoke(projectsList: List<Project>): StateFlow<List<Job>> {
        return projectRepository.getJobs(projectsList)
    }
}