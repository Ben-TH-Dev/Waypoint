package beh59.aber.ac.uk.cs39440.mmp.di

import beh59.aber.ac.uk.cs39440.mmp.data.repository.IAuthRepository
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IUserRepository
import beh59.aber.ac.uk.cs39440.mmp.data.repository.impl.AuthRepository
import beh59.aber.ac.uk.cs39440.mmp.data.source.remote.AuthDataSource
import beh59.aber.ac.uk.cs39440.mmp.domain.auth.LoadUserDataUseCase
import beh59.aber.ac.uk.cs39440.mmp.domain.auth.SignInWithGoogleUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AuthModule
 * Part of the dependency injection for components required by the authentication systems in the
 * application
 */
@Module
@InstallIn(SingletonComponent::class)
class AuthModule {
    /**
     * provideAuthDataSource
     * Provides a singleton instance of AuthDataSource when needed with FirebaseAuth injected
     * @param firebaseAuth An instance of FirebaseAuth
     */
    @Provides
    @Singleton
    fun provideAuthDataSource(firebaseAuth: FirebaseAuth): AuthDataSource {
        return AuthDataSource(firebaseAuth)
    }

    /**
     * provideAuthRepository
     * Provides a singleton instance of AuthRepository when needed with AuthDataSource injected
     * @param authDataSource The singleton instance of AuthDataSource
     */
    @Provides
    @Singleton
    fun provideAuthRepository(authDataSource: AuthDataSource): IAuthRepository {
        return AuthRepository(authDataSource)
    }

    /**
     * provideSignInWithGoogleUseCase
     * Provides a singleton instance of the SignInWithGoogleUseCase, part of the optional domain
     * layer of the application
     * @param authRepository The singleton instance of AuthRepository
     */
    @Provides
    @Singleton
    fun provideSignInWithGoogleUseCase(authRepository: IAuthRepository): SignInWithGoogleUseCase {
        return SignInWithGoogleUseCase(authRepository)
    }

    /**
     * provideLoadUserDataUseCase
     * Provides a singleton instance of LoadUserDataUseCase, part of the optional domain layer of
     * the application
     * @param authRepository The singleton instance of AuthRepository
     * @param userRepository The singleton instance of UserRepository, which is provided in
     * UserModule
     */
    @Provides
    @Singleton
    fun provideLoadUserDataUseCase(
        authRepository: IAuthRepository,
        userRepository: IUserRepository
    ): LoadUserDataUseCase {
        return LoadUserDataUseCase(authRepository, userRepository)
    }
} 