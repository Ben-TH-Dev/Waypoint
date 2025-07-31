package beh59.aber.ac.uk.cs39440.mmp

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

/**
 * WaypointApplication
 * Created when the application begins and referenced in AndroidManifest.xml
 * Annotated with @HiltAndroidApp which marks the class where Dagger and Hilt will generate
 * components at build to provide dependency injection between the different modules present in the
 * codebase
 */
@HiltAndroidApp
class WaypointApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
    }
}