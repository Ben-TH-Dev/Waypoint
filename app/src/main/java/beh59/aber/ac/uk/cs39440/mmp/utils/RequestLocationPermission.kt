package beh59.aber.ac.uk.cs39440.mmp.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * RequestLocationPermission
 * Used to acquire the current user's location permissions so that they can be displayed on the live
 * map and the application can process the location data
 * @param requestCount The amount of times permission has been requested
 * @param onPermissionDenied Callback allowing us to define behaviour when permission is denied
 * where RequestLocationPermission is called
 * @param onPermissionReady Callback allowing us to define behaviour when permission is granted
 * where RequestLocationPermission is called
 */
//https://developer.android.com/develop/sensors-and-location/location/permissions/runtime
@Composable
fun RequestLocationPermission(
    requestCount: Int = 0,
    onPermissionDenied: () -> Unit,
    onPermissionReady: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissionsMap ->
        val granted = permissionsMap.values.all { it }
        if (granted) {
            onPermissionReady()
        } else {
            onPermissionDenied()
        }
    }
    LaunchedEffect(requestCount) {
        context.checkAndRequestLocationPermission(
            locationPermissions,
            launcher,
            onPermissionReady
        )
    }
}

private fun Context.checkAndRequestLocationPermission(
    permissions: Array<String>,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    onPermissionReady: () -> Unit
) {
    if (permissions.all {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    ) {
        onPermissionReady()
    } else {
        launcher.launch(permissions)
    }
}

private val locationPermissions = arrayOf(
    android.Manifest.permission.ACCESS_FINE_LOCATION,
    android.Manifest.permission.ACCESS_COARSE_LOCATION
)