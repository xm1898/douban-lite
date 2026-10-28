package app.doubanlite.ui.explore

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.doubanlite.data.MediaType
import app.doubanlite.data.Subject
import app.doubanlite.data.SubjectRepository
import app.doubanlite.data.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ExploreViewModel : ViewModel() {

    private val _items = mutableStateOf<List<Subject>>(emptyList())
    val items: State<List<Subject>> = _items

    private val _state = mutableStateOf<UiState<Unit>>(UiState.Loading)
    val state: State<UiState<Unit>> = _state

    private val _loadingMore = mutableStateOf(false)
    val loadingMore: State<Boolean> = _loadingMore

    private val _end = mutableStateOf(false)
    val end: State<Boolean> = _end

    private var type = MediaType.MOVIE
    private var sort = "T"
    private var tags: String? = null
    private var scoreRange = "0,10"

    private var start = 0
    private var generation = 0
    private var job: Job? = null

    val pageSize = 21

    fun reload(type: MediaType, sort: String, tags: String?, scoreRange: String) {
        this.type = type
        this.sort = sort
        this.tags = tags
        this.scoreRange = scoreRange
        generation++
        val gen = generation
        job?.cancel()
        start = 0
        _end.value = false
        _items.value = emptyList()
        _state.value = UiState.Loading
        job = viewModelScope.launch { fetch(gen, first = true) }
    }

    fun loadMore() {
        if (_loadingMore.value || _end.value || _state.value !is UiState.Success) return
        _loadingMore.value = true
        val gen = generation
        viewModelScope.launch {
            try {
                fetch(gen, first = false)
            } finally {
                if (gen == generation) _loadingMore.value = false
            }
        }
    }

    private suspend fun fetch(gen: Int, first: Boolean) {
        val result = SubjectRepository.explore(type, sort, start, pageSize, tags, scoreRange)
        if (gen != generation) return
        result.fold(
            onSuccess = { page ->
                val clean = page.distinctBy { it.id }
                _items.value = if (first) clean else (_items.value + clean).distinctBy { it.id }
                if (page.size < pageSize) _end.value = true
                start += pageSize
                _state.value = UiState.Success(Unit)
            },
            onFailure = { error ->
                if (first) {
                    _state.value = UiState.Error(error.message ?: "网络请求失败")
                } else {
                    _end.value = true
                }
            },
        )
    }
}
