package beh59.aber.ac.uk.cs39440.mmp.di

import beh59.aber.ac.uk.cs39440.mmp.data.repository.IProjectRepository
import beh59.aber.ac.uk.cs39440.mmp.data.repository.impl.ProjectRepository
import beh59.aber.ac.uk.cs39440.mmp.data.source.remote.ProjectDataSource
import beh59.aber.ac.uk.cs39440.mmp.domain.project.GetJobsUseCase
import beh59.aber.ac.uk.cs39440.mmp.domain.project.GetProjectsUseCase
import beh59.aber.ac.uk.cs39440.mmp.domain.project.SubmitJobUseCase
import beh59.aber.ac.uk.cs39440.mmp.domain.project.SubmitProjectUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * ProjectModule
 * Part of the dependency injection for components required by the project management systems in the
 * application
 */
@Module
@InstallIn(SingletonComponent::class)
class ProjectModule {
    /**
     * provideGetProjectsUseCase
     * Provides a singleton instance of GetProjectsUseCase, part of the optional domain layer of
     * the application
     * @param projectRepository The singleton instance of ProjectRepository
     */
    @Provides
    @Singleton
    fun provideGetProjectsUseCase(projectRepository: ProjectRepository): GetProjectsUseCase {
        return GetProjectsUseCase(projectRepository)
    }

    /**
     * provideGetJobsUseCase
     * Provides a singleton instance of GetJobsUseCase, part of the optional domain layer of
     * the application
     * @param projectRepository The singleton instance of ProjectRepository
     */
    @Provides
    @Singleton
    fun provideGetJobsUseCase(projectRepository: ProjectRepository): GetJobsUseCase {
        return GetJobsUseCase(projectRepository)
    }

    /**
     * provideSubmitProjectUseCase
     * Provides a singleton instance of SubmitProjectUseCase, part of the optional domain layer of
     * the application
     * @param projectRepository The singleton instance of ProjectRepository
     */
    @Provides
    @Singleton
    fun provideSubmitProjectUseCase(projectRepository: ProjectRepository): SubmitProjectUseCase {
        return SubmitProjectUseCase(projectRepository)
    }

    /**
     * provideSubmitJobUseCase
     * Provides a singleton instance of SubmitJobUseCase, part of the optional domain layer of
     * the application
     * @param projectRepository The singleton instance of ProjectRepository
     */
    @Provides
    @Singleton
    fun provideSubmitJobUseCase(projectRepository: ProjectRepository): SubmitJobUseCase {
        return SubmitJobUseCase(projectRepository)
    }

    /**
     * provideProjectRepository
     * Provides a singleton instance of ProjectRepository when needed with ProjectDataSource and
     * FirebaseFirestore injected
     * @param source The singleton instance of ProjectDataSource
     * @param firestore An instance of FirebaseFirestore
     */
    @Provides
    @Singleton
    fun provideProjectRepository(
        source: ProjectDataSource,
        firestore: FirebaseFirestore
    ): IProjectRepository {
        return ProjectRepository(source, firestore)
    }
}