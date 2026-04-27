package com.mlbpark.bullpen.ui

/**
 * 화면 공통 UI 상태.
 *
 * - [Loading]: 첫 로딩 중. 스켈레톤/스피너를 보여줄 때 사용.
 * - [Success]: 데이터를 정상적으로 받아온 상태.
 * - [Error]: 네트워크/파싱 실패. 사용자에게 메시지와 재시도 버튼을 노출.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
