package beh59.aber.ac.uk.cs39440.mmp.di

import beh59.aber.ac.uk.cs39440.mmp.data.repository.IUserRepository
import beh59.aber.ac.uk.cs39440.mmp.data.repository.impl.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * UserModule
 * Part of the dependency injection for components required by the user management systems in the
 * application
 */
@Module
@InstallIn(SingletonComponent::class)
class UserModule {
    /**
     * provideUserRepository
     * Provides a singleton instance of UserRepository when needed with FirebaseFirestore injected
     * @param firestore An instance of FirebaseFirestore
     */
    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore
    ): IUserRepository {
        return UserRepository(firestore)
    }
}