package beh59.aber.ac.uk.cs39440.mmp.data.repository.impl

import beh59.aber.ac.uk.cs39440.mmp.data.models.states.MapState
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IMapRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * MapRepository
 * An implementation of the IMapRepository interface and its required methods.
 * @property mapState A StateFlow holding the state of data needed by map features that can
 * be observed in the UI
 */
class MapRepository @Inject constructor() : IMapRepository {
    private val _mapState = MutableStateFlow(MapState())
    override val mapState: StateFlow<MapState> = _mapState.asStateFlow()

    /**
     * updateShowMap
     * Updates the value of showMap in state, which the app uses to determine if it should display
     * the MapboxMap in MapViewController
     * @param show Whether or not to show the map
     */
    override fun updateShowMap(show: Boolean) {
        _mapState.value = _mapState.value.copy(showMap = show)
    }

    /**
     * updateLoadingStatus
     * Updates the value of isLoadingFriends in state which the app uses to determine if friend
     * location data is currently being loaded
     * @param isLoading Whether ot not friend location data is loaded
     */
    override fun updateLoadingStatus(isLoading: Boolean) {
        _mapState.value = _mapState.value.copy(isLoadingFriends = isLoading)
    }
}