package com.mlbpark.bullpen.data

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.concurrent.TimeUnit

/**
 * MLB Park bullpen 게시판을 HTTP로 가져와서 파싱하는 단일 책임 클래스.
 *
 * - HTTP는 OkHttp로 직접 호출 (User-Agent를 모바일 브라우저로 위장).
 * - HTML 파싱은 Jsoup의 CSS 선택자로 수행.
 * - 네트워크 호출은 IO 디스패처에서 호출되어야 함 (ViewModel이 책임).
 */
class BullpenRepository {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * 목록 페이지를 가져와 최신 글 [limit]개를 반환한다.
     *
     * 파싱 전략:
     *  - PRD에 정의된 4가지 CSS 선택자(a.txt / a.list_word / span.nick / span.date)는
     *    모두 같은 게시글 행(<tr>)에 들어 있다는 가정.
     *  - 따라서 a.txt 를 기준으로 가장 가까운 조상 <tr> 을 찾고, 그 안에서 나머지
     *    선택자를 다시 찾아 같은 행 단위로 묶는다.
     *  - 게시판 상단의 공지/광고 행에 a.txt 가 없거나 nick/date가 없을 수 있으므로
     *    필수 필드(title/author/time)가 빠진 행은 건너뛴다.
     */
    fun fetchList(limit: Int = 10): List<PostSummary> {
        val doc = fetchDocument(LIST_URL)

        val rows: List<Element> = doc.select("a.txt").mapNotNull { titleAnchor ->
            // 가장 가까운 <tr> 을 행 단위로 사용. 없으면 한 단계 위 부모로 폴백.
            titleAnchor.closest("tr") ?: titleAnchor.parent()?.parent()
        }.distinct()

        val results = mutableListOf<PostSummary>()
        for (row in rows) {
            val titleEl = row.selectFirst("a.txt") ?: continue
            val title = titleEl.text().trim()
            if (title.isEmpty()) continue

            val author = row.selectFirst("span.nick")?.text()?.trim().orEmpty()
            val time = row.selectFirst("span.date")?.text()?.trim().orEmpty()
            // 필수 필드가 비어 있으면 광고/공지로 간주하고 스킵.
            if (author.isEmpty() || time.isEmpty()) continue

            val category = row.selectFirst("a.list_word")?.text()?.trim()

            val detailUrl = absoluteUrl(titleEl.attr("href"))
            val id = detailUrl // URL 자체를 식별자로 사용

            results += PostSummary(
                id = id,
                title = title,
                category = category?.takeIf { it.isNotEmpty() },
                author = author,
                time = time,
                detailUrl = detailUrl,
            )
            if (results.size >= limit) break
        }
        return results
    }

    /**
     * 글 상세 페이지를 가져온다.
     *
     * 파싱 전략 (사용자가 확인해 준 mlbpark 상세 페이지의 실제 셀렉터):
     *  - 글 컨테이너:  div.contents
     *  - 제목:        div.titles
     *  - 카테고리:    span.word
     *  - 작성자:      span.nick
     *  - 작성시간:    span.val
     *  - 본문:        div.view_context
     *
     * 우선 글 컨테이너(div.contents) 안에서 메타데이터를 찾고, 없으면 문서 전체에서
     * 같은 셀렉터로 한 번 더 시도한다. 이렇게 하면 메인 게시글 외에 관련글 섹션이
     * 같은 클래스를 재사용하더라도 본문 메타와 섞이지 않는다.
     */
    fun fetchDetail(detailUrl: String): PostDetail {
        val doc = fetchDocument(detailUrl)

        // 글 컨테이너. 없으면 문서 전체를 fallback 으로 사용.
        val container: Element = doc.selectFirst("div.contents") ?: doc

        val title = container.selectFirst("div.titles")?.text()?.trim()
            // fallback: 일부 페이지에서 제목이 컨테이너 밖에 있을 가능성 대비
            ?: doc.selectFirst("div.titles")?.text()?.trim().orEmpty()
        val category = container.selectFirst("span.word")?.text()?.trim()
            ?.takeIf { it.isNotEmpty() }
        val author = container.selectFirst("span.nick")?.text()?.trim().orEmpty()
        val time = container.selectFirst("span.val")?.text()?.trim().orEmpty()

        // 본문 — 정확히 사용자 가이드 그대로
        val bodyEl: Element? = container.selectFirst("div.view_context")
            ?: doc.selectFirst("div.view_context")

        val blocks: List<BodyBlock> = bodyEl?.let { extractBodyBlocks(it) } ?: emptyList()

        return PostDetail(
            title = title,
            category = category,
            author = author,
            time = time,
            body = blocks,
        )
    }

    /**
     * 본문 컨테이너에서 텍스트와 이미지를 순서대로 뽑아내 [BodyBlock] 리스트로 변환.
     * - <img> 는 절대 URL로 변환 후 [BodyBlock.Image] 로.
     * - 그 외 텍스트는 줄바꿈을 보존해 [BodyBlock.Text] 로.
     */
    private fun extractBodyBlocks(container: Element): List<BodyBlock> {
        val blocks = mutableListOf<BodyBlock>()
        val textBuf = StringBuilder()

        fun flushText() {
            val t = textBuf.toString().trim()
            if (t.isNotEmpty()) blocks += BodyBlock.Text(t)
            textBuf.clear()
        }

        // <br> 을 개행으로, <p>/<div> 종료시 두 줄 띄움 형태로 직렬화하기 위해
        // 직접 DFS 순회.
        fun walk(el: Element) {
            for (node in el.childNodes()) {
                when (node) {
                    is org.jsoup.nodes.TextNode -> {
                        val t = node.text()
                        if (t.isNotBlank()) textBuf.append(t)
                    }
                    is Element -> {
                        when (node.tagName().lowercase()) {
                            "br" -> textBuf.append('\n')
                            "img" -> {
                                flushText()
                                val src = node.attr("src").ifEmpty { node.attr("data-original") }
                                if (src.isNotEmpty()) {
                                    blocks += BodyBlock.Image(absoluteUrl(src))
                                }
                            }
                            "script", "style", "iframe" -> {
                                // 무시
                            }
                            "p", "div", "li" -> {
                                walk(node)
                                if (textBuf.isNotEmpty() && !textBuf.endsWith("\n")) {
                                    textBuf.append('\n')
                                }
                            }
                            else -> walk(node)
                        }
                    }
                }
            }
        }

        walk(container)
        flushText()
        return blocks
    }

    private fun fetchDocument(url: String): Document {
        val request = Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10; SM-G975N) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/120.0.0.0 Mobile Safari/537.36",
            )
            .header("Accept-Language", "ko-KR,ko;q=0.9,en;q=0.8")
            .build()

        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) {
                throw RuntimeException("HTTP ${resp.code} for $url")
            }
            val bodyBytes = resp.body?.bytes() ?: throw RuntimeException("Empty body")
            // mlbpark는 EUC-KR(혹은 UTF-8) 가능성 — Jsoup 파서가 charset 메타를 보고 결정.
            return Jsoup.parse(bodyBytes.inputStream(), null, BASE_URL)
        }
    }

    private fun absoluteUrl(href: String): String {
        if (href.isEmpty()) return ""
        return when {
            href.startsWith("http://") || href.startsWith("https://") -> href
            href.startsWith("//") -> "https:$href"
            href.startsWith("/") -> "https://mlbpark.donga.com$href"
            else -> "$BASE_URL/$href"
        }
    }

    companion object {
        const val BASE_URL = "https://mlbpark.donga.com/mp"
        const val LIST_URL = "https://mlbpark.donga.com/mp/b.php?b=bullpen"
    }
}
