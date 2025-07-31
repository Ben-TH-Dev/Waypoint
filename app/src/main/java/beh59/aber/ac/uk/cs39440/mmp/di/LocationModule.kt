package beh59.aber.ac.uk.cs39440.mmp.di

import android.content.Context
import beh59.aber.ac.uk.cs39440.mmp.data.repository.ILocationRepository
import beh59.aber.ac.uk.cs39440.mmp.data.repository.impl.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * LocationModule
 * Part of the dependency injection for components required by the location tracking features
 * in the application
 */
@Module
@InstallIn(SingletonComponent::class)
class LocationModule {
    /**
     * provideFusedLocationProvider
     * Provides a singleton instance of FusedLocationProviderClient for accessing device location
     * @param context The application context needed to initialize the location provider
     */
    @Provides
    @Singleton
    fun provideFusedLocationProvider(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    /**
     * provideLocationRepository
     * Provides a singleton instance of LocationRepository when needed with FirebaseDatabase
     * injected
     * @param realtime An instance of FirebaseDatabase for realtime location updates
     */
    @Provides
    @Singleton
    fun provideLocationRepository(
        realtime: FirebaseDatabase
    ): ILocationRepository {
        return LocationRepository(realtime)
    }
}