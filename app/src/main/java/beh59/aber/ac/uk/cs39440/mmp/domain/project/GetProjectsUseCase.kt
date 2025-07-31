package beh59.aber.ac.uk.cs39440.mmp.domain.project

import beh59.aber.ac.uk.cs39440.mmp.data.models.Project
import beh59.aber.ac.uk.cs39440.mmp.data.repository.impl.ProjectRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * GetProjectsUseCase
 * Encapsulates and coordinates logic related to retrieving project data
 * @param projectRepository Contains business logic and Firestore calls related to project systems
 * in the application
 */
class GetProjectsUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    /**
     * invoke
     * Called when GetProjectsUseCase is called. Calls ProjectRepository to retrieve projects
     * @param currentUserID The unique identifier of the current user
     * @return A StateFlow containing a list of Project objects associated with the user
     */
    suspend operator fun invoke(currentUserID: String): StateFlow<List<Project>> {
        return projectRepository.getProjects(currentUserID)
    }
}