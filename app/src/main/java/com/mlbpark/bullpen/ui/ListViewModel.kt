package com.mlbpark.bullpen.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mlbpark.bullpen.data.BullpenRepository
import com.mlbpark.bullpen.data.PostSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 목록 화면의 상태를 관리한다.
 *
 * - [state]   : 첫 로딩/성공/오류 상태.
 * - [isRefreshing] : Pull-to-Refresh 중인지 여부 (별도 플래그 — 기존 데이터를 유지하면서 갱신).
 */
class ListViewModel(
    private val repository: BullpenRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<PostSummary>>>(UiState.Loading)
    val state: StateFlow<UiState<List<PostSummary>>> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        load(initial = true)
    }

    fun refresh() {
        load(initial = false)
    }

    fun retry() {
        _state.value = UiState.Loading
        load(initial = true)
    }

    private fun load(initial: Boolean) {
        viewModelScope.launch {
            if (!initial) _isRefreshing.value = true
            try {
                val list = withContext(Dispatchers.IO) { repository.fetchList(limit = 10) }
                _state.value = UiState.Success(list)
            } catch (t: Throwable) {
                if (initial || _state.value !is UiState.Success) {
                    _state.value = UiState.Error(t.message ?: "네트워크 오류")
                }
                // refresh 실패 시 기존 Success 데이터는 그대로 두고 isRefreshing만 끔.
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    class Factory(private val repository: BullpenRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ListViewModel(repository) as T
        }
    }
}
