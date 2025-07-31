package beh59.aber.ac.uk.cs39440.mmp.di

import beh59.aber.ac.uk.cs39440.mmp.data.repository.IMapRepository
import beh59.aber.ac.uk.cs39440.mmp.data.repository.impl.MapRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * MapModule
 * Part of the dependency injection for components required by the map functionality in the
 * application
 */
@Module
@InstallIn(SingletonComponent::class)
class MapModule {
    /**
     * provideMapRepository
     * Provides a singleton instance of MapRepository when needed
     */
    @Provides
    @Singleton
    fun provideMapRepository(): IMapRepository {
        return MapRepository()
    }
}