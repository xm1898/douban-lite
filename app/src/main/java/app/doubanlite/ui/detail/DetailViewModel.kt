package app.doubanlite.ui.detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.doubanlite.data.MediaType
import app.doubanlite.data.SubjectDetail
import app.doubanlite.data.SubjectRepository
import app.doubanlite.data.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {

    private val _state = mutableStateOf<UiState<SubjectDetail>>(UiState.Loading)
    val state: State<UiState<SubjectDetail>> = _state

    private var key: String? = null
    private var job: Job? = null

    fun load(type: MediaType, id: String) {
        val newKey = "${type.apiValue}:$id"
        if (key == newKey && _state.value is UiState.Success) return
        key = newKey
        job?.cancel()
        _state.value = UiState.Loading
        job = viewModelScope.launch {
            val result = SubjectRepository.detail(type, id)
            if (key != newKey) return@launch
            _state.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "网络请求失败") },
            )
        }
    }
}
