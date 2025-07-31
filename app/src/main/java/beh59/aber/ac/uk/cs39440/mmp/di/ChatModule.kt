package beh59.aber.ac.uk.cs39440.mmp.di

import beh59.aber.ac.uk.cs39440.mmp.data.repository.IChatRepository
import beh59.aber.ac.uk.cs39440.mmp.data.repository.impl.ChatRepository
import beh59.aber.ac.uk.cs39440.mmp.data.source.remote.ChatDataSource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * ChatModule
 * Part of the dependency injection for components required by the chat functionality in the
 * application
 */
@Module
@InstallIn(SingletonComponent::class)
class ChatModule {
    /**
     * provideChatDataSource
     * Provides a singleton instance of ChatDataSource when needed with FirebaseFirestore injected
     * @param db An instance of FirebaseFirestore
     */
    @Provides
    @Singleton
    fun provideChatDataSource(db: FirebaseFirestore): ChatDataSource {
        return ChatDataSource(db)
    }

    /**
     * provideChatRepository
     * Provides a singleton instance of ChatRepository when needed with ChatDataSource and
     * FirebaseFirestore injected
     * @param source The singleton instance of ChatDataSource
     * @param db An instance of FirebaseFirestore
     */
    @Provides
    @Singleton
    fun provideChatRepository(
        source: ChatDataSource,
        db: FirebaseFirestore
    ): IChatRepository {
        return ChatRepository(db, source)
    }
} 