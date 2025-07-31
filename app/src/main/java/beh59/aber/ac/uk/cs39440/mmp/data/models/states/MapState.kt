package beh59.aber.ac.uk.cs39440.mmp.data.models.states

import androidx.annotation.Keep
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.MapState as MapboxMapState

/**
 * MapState
 * Represents the state of the map functionality in the application
 * @param showMap Tracks if the map should be displayed
 * @param mapState Stores the state of the Mapbox map
 * @param mapViewportState Stores the viewport state of the map
 * @param isLoadingFriends Tracks whether friend location data is currently being loaded
 */
@Keep
data class MapState(
    val showMap: Boolean = false,
    val mapState: MapboxMapState = MapboxMapState(),
    val mapViewportState: MapViewportState = MapViewportState(),
    val isLoadingFriends: Boolean = true
) 