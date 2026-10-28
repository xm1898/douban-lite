package app.doubanlite.ui.feed

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.doubanlite.data.FeedSection
import app.doubanlite.data.MediaType
import app.doubanlite.data.SubjectRepository
import app.doubanlite.data.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val _state = mutableStateOf<UiState<List<FeedSection>>>(UiState.Loading)
    val state: State<UiState<List<FeedSection>>> = _state

    private var loadedType: MediaType? = null
    private var job: Job? = null

    fun load(type: MediaType, force: Boolean = false) {
        if (!force && loadedType == type && _state.value is UiState.Success) return
        loadedType = type
        job?.cancel()
        _state.value = UiState.Loading
        job = viewModelScope.launch {
            val result = SubjectRepository.feed(type)
            if (loadedType != type) return@launch
            _state.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "网络请求失败") },
            )
        }
    }
}
