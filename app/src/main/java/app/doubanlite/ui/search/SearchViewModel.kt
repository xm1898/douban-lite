package app.doubanlite.ui.search

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.doubanlite.data.Subject
import app.doubanlite.data.SubjectRepository
import app.doubanlite.data.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val _state = mutableStateOf<UiState<List<Subject>>>(UiState.Success(emptyList()))
    val state: State<UiState<List<Subject>>> = _state

    private var job: Job? = null
    private var lastQuery: String = ""

    fun search(query: String) {
        val q = query.trim()
        if (q == lastQuery) return
        lastQuery = q
        job?.cancel()
        if (q.isEmpty()) {
            _state.value = UiState.Success(emptyList())
            return
        }
        _state.value = UiState.Loading
        job = viewModelScope.launch {
            delay(280)
            val result = SubjectRepository.search(q)
            if (lastQuery != q) return@launch
            _state.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "搜索失败") },
            )
        }
    }
}
