package beh59.aber.ac.uk.cs39440.mmp.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import beh59.aber.ac.uk.cs39440.mmp.data.models.states.MapState
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IMapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MapViewModel
 * ViewModel that handles map related data and operation
 * @param mapRepository Repository for map related operations
 * @property uiState Exposes UI state from the repository to be used in the UI
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapRepository: IMapRepository
) : ViewModel() {
    val uiState: StateFlow<MapState> = mapRepository.mapState

    init {
        updateShowMap()
    }

    fun updateShowMap() {
        viewModelScope.launch {
            mapRepository.updateShowMap(true)
        }
    }

    fun setLoading(isLoading: Boolean) {
        mapRepository.updateLoadingStatus(isLoading)
    }

    fun refreshMapState() {
        //Attempts to trigger recomposition by modifying state
        viewModelScope.launch {
            setLoading(true)
            setLoading(false)
        }
    }
}