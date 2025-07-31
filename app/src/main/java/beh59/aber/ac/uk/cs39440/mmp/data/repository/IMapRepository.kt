package beh59.aber.ac.uk.cs39440.mmp.data.repository

import beh59.aber.ac.uk.cs39440.mmp.data.models.states.MapState
import kotlinx.coroutines.flow.StateFlow

/**
 * IMapRepository
 * An abstraction of the methods required for the map systems of the application
 * @property mapState A StateFlow holding the state of data needed by map features that can
 * be observed in the UI
 */
interface IMapRepository {
    val mapState: StateFlow<MapState>

    /**
     * updateShowMap
     * Defines a method that should update the visibility of the map in state.
     * @param show True to show the map, false to hide it.
     */
    fun updateShowMap(show: Boolean)

    /**
     * updateLoadingStatus
     * Defines a method that should update the loading status related to map operations.
     * @param isLoading True if the map is currently loading data or performing an operation, false otherwise.
     */
    fun updateLoadingStatus(isLoading: Boolean)
}