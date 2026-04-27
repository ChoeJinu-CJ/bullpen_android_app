package com.mlbpark.bullpen.data

/**
 * 목록 화면에 표시되는 글 요약 정보.
 *
 * @param id        상세 페이지로 이동할 때 사용하는 글 식별자 (URL의 b, id 파라미터로 구성).
 * @param title     글 제목 (HTML class="txt"의 <a> 텍스트).
 * @param category  카테고리 (HTML class="list_word"의 <a> 텍스트). 없을 수 있음.
 * @param author    작성자 닉네임 (HTML class="nick"의 <span> 텍스트).
 * @param time      작성 시간 표기 (HTML class="date"의 <span> 텍스트, 원본 그대로).
 * @param detailUrl 글 상세 페이지의 절대 URL.
 */
data class PostSummary(
    val id: String,
    val title: String,
    val category: String?,
    val author: String,
    val time: String,
    val detailUrl: String,
)

/**
 * 글 상세 화면에 표시되는 정보.
 *
 * 본문은 HTML(텍스트 + 이미지)을 단순화한 [BodyBlock] 리스트로 보관해
 * Compose 측에서 텍스트/이미지를 자연스럽게 렌더링할 수 있게 한다.
 */
data class PostDetail(
    val title: String,
    val category: String?,
    val author: String,
    val time: String,
    val body: List<BodyBlock>,
)

/**
 * 본문 한 조각.
 */
sealed interface BodyBlock {
    data class Text(val text: String) : BodyBlock
    data class Image(val url: String) : BodyBlock
}
