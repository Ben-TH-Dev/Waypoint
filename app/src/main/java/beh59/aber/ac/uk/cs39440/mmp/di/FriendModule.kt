package beh59.aber.ac.uk.cs39440.mmp.di

import beh59.aber.ac.uk.cs39440.mmp.data.repository.IFriendRepository
import beh59.aber.ac.uk.cs39440.mmp.data.repository.impl.FriendRepository
import beh59.aber.ac.uk.cs39440.mmp.data.source.remote.FriendDataSource
import beh59.aber.ac.uk.cs39440.mmp.domain.friend.GetFriendsUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * FriendModule
 * Part of the dependency injection for components required by the friend management systems in the
 * application
 */
@Module
@InstallIn(SingletonComponent::class)
class FriendModule {
    /**
     * provideGetFriendsUseCase
     * Provides a singleton instance of GetFriendsUseCase, part of the optional domain layer of
     * the application
     * @param friendRepository The singleton instance of FriendRepository
     */
    @Provides
    @Singleton
    fun provideGetFriendsUseCase(friendRepository: IFriendRepository): GetFriendsUseCase {
        return GetFriendsUseCase(friendRepository)
    }

    /**
     * provideFriendRepository
     * Provides a singleton instance of FriendRepository when needed with FriendDataSource and
     * FirebaseFirestore injected
     * @param source The singleton instance of FriendDataSource
     * @param db An instance of FirebaseFirestore
     */
    @Provides
    @Singleton
    fun provideFriendRepository(
        source: FriendDataSource,
        db: FirebaseFirestore
    ): IFriendRepository {
        return FriendRepository(source, db)
    }
}