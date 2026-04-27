package com.mlbpark.bullpen.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mlbpark.bullpen.data.BullpenRepository
import com.mlbpark.bullpen.data.PostDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailViewModel(
    private val repository: BullpenRepository,
    private val detailUrl: String,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<PostDetail>>(UiState.Loading)
    val state: StateFlow<UiState<PostDetail>> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() {
        _state.value = UiState.Loading
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val detail = withContext(Dispatchers.IO) { repository.fetchDetail(detailUrl) }
                _state.value = UiState.Success(detail)
            } catch (t: Throwable) {
                _state.value = UiState.Error(t.message ?: "네트워크 오류")
            }
        }
    }

    class Factory(
        private val repository: BullpenRepository,
        private val detailUrl: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DetailViewModel(repository, detailUrl) as T
        }
    }
}
