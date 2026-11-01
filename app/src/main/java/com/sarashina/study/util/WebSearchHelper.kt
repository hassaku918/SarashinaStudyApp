package com.sarashina.study.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class SearchHit(
    val title: String,
    val snippet: String,
    val url: String
)

object WebSearchHelper {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun search(query: String, maxResults: Int = 5): List<SearchHit> = withContext(Dispatchers.IO) {
        val q = query.trim().take(200)
        if (q.isEmpty()) return@withContext emptyList()
        val hits = mutableListOf<SearchHit>()
        try {
            hits += fromDuckDuckGo(q)
        } catch (_: Exception) {
        }
        if (hits.size < 2) {
            try {
                hits += fromWikipedia(q)
            } catch (_: Exception) {
            }
        }
        hits.distinctBy { it.url.ifBlank { it.title } }.take(maxResults)
    }

    fun formatForPrompt(hits: List<SearchHit>): String {
        if (hits.isEmpty()) return "（検索結果なし）"
        return buildString {
            appendLine("【Web検索結果】回答には出典URLを明示してください。")
            hits.forEachIndexed { i, h ->
                appendLine("${i + 1}. ${h.title}")
                if (h.snippet.isNotBlank()) appendLine("   ${h.snippet.take(400)}")
                if (h.url.isNotBlank()) appendLine("   URL: ${h.url}")
            }
        }
    }

    private fun fromDuckDuckGo(q: String): List<SearchHit> {
        val enc = URLEncoder.encode(q, "UTF-8")
        val url = "https://api.duckduckgo.com/?q=$enc&format=json&no_html=1&skip_disambig=1"
        val req = Request.Builder().url(url).header("User-Agent", "SarashinaStudy/1.3").build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return emptyList()
            val body = resp.body?.string().orEmpty()
            val json = JSONObject(body)
            val out = mutableListOf<SearchHit>()
            val abstract = json.optString("AbstractText")
            val abstractUrl = json.optString("AbstractURL")
            val heading = json.optString("Heading")
            if (abstract.isNotBlank()) {
                out += SearchHit(
                    title = heading.ifBlank { "DuckDuckGo Abstract" },
                    snippet = abstract,
                    url = abstractUrl
                )
            }
            val related = json.optJSONArray("RelatedTopics")
            if (related != null) {
                for (i in 0 until related.length()) {
                    if (out.size >= 5) break
                    val item = related.optJSONObject(i) ?: continue
                    val text = item.optString("Text")
                    val firstUrl = item.optJSONObject("FirstURL")?.optString("FirstURL")
                        ?: item.optString("FirstURL")
                    if (text.isNotBlank()) {
                        out += SearchHit(
                            title = text.take(80),
                            snippet = text,
                            url = firstUrl
                        )
                    }
                    val topics = item.optJSONArray("Topics")
                    if (topics != null) {
                        for (j in 0 until topics.length()) {
                            if (out.size >= 5) break
                            val t = topics.optJSONObject(j) ?: continue
                            val tText = t.optString("Text")
                            val tUrl = t.optString("FirstURL")
                            if (tText.isNotBlank()) {
                                out += SearchHit(tText.take(80), tText, tUrl)
                            }
                        }
                    }
                }
            }
            return out
        }
    }

    private fun fromWikipedia(q: String): List<SearchHit> {
        val enc = URLEncoder.encode(q, "UTF-8")
        val url =
            "https://ja.wikipedia.org/w/api.php?action=opensearch&search=$enc&limit=3&namespace=0&format=json"
        val req = Request.Builder().url(url).header("User-Agent", "SarashinaStudy/1.3").build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return emptyList()
            val arr = org.json.JSONArray(resp.body?.string().orEmpty())
            if (arr.length() < 4) return emptyList()
            val titles = arr.getJSONArray(1)
            val descs = arr.getJSONArray(2)
            val urls = arr.getJSONArray(3)
            val out = mutableListOf<SearchHit>()
            for (i in 0 until titles.length()) {
                out += SearchHit(
                    title = titles.optString(i),
                    snippet = descs.optString(i),
                    url = urls.optString(i)
                )
            }
            return out
        }
    }
}
