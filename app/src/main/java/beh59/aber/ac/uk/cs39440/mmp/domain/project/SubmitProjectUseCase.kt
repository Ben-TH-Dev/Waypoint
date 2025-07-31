package beh59.aber.ac.uk.cs39440.mmp.domain.project

import beh59.aber.ac.uk.cs39440.mmp.data.models.Project
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IProjectRepository
import javax.inject.Inject

/**
 * SubmitProjectUseCase
 * Encapsulates and coordinates logic related to submitting new project data
 * @param projectRepository Contains business logic and Firestore calls related to project
 * systems in the application
 */
class SubmitProjectUseCase @Inject constructor(
    private val projectRepository: IProjectRepository
) {
    /**
     * invoke
     * Called when SubmitProjectUseCase is called. Delegates the submission of projects to the
     * ProjectRepository.
     * @param project The Project object to be submitted to Firestore
     * @param currentUserUID The unique identifier of the current user
     */
    suspend operator fun invoke(project: Project, currentUserUID: String) {
        projectRepository.submitProject(project, currentUserUID)
    }
}