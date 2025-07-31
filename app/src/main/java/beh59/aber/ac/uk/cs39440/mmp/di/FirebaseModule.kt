package beh59.aber.ac.uk.cs39440.mmp.di

import android.content.Context
import beh59.aber.ac.uk.cs39440.mmp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.perf.FirebasePerformance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * FirebaseModule
 * Part of the dependency injection for Firebase components required throughout the
 * application
 */
@Module
@InstallIn(SingletonComponent::class)
class FirebaseModule {
    /**
     * provideFirebaseAuth
     * Provides a singleton instance of FirebaseAuth for authentication services
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    /**
     * provideFirebaseRealtime
     * Provides a singleton instance of FirebaseDatabase for realtime database operations.
     * Connects to the URL displayed in the Firebase console
     */
    @Provides
    @Singleton
    fun provideFirebaseRealtime(@ApplicationContext context: Context): FirebaseDatabase {
        return FirebaseDatabase.getInstance(context.getString(R.string.firebase_realtime_db_url))
    }

    /**
     * provideFirebaseFirestore
     * Provides a singleton instance of FirebaseFirestore for Firestore database operations
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    /**
     * provideFirebasePerformance
     * Provides a singleton instance of FirebasePerformance for performance monitoring
     */
    @Provides
    @Singleton
    fun provideFirebasePerformance(): FirebasePerformance {
        return FirebasePerformance.getInstance()
    }
}