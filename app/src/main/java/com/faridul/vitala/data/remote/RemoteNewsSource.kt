package com.faridul.vitala.data.remote

import android.util.Xml
import com.faridul.vitala.data.model.NewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class RemoteNewsSource(
    private val okHttpClient: okhttp3.OkHttpClient = NetworkModule.okHttpClient,
    private val pubMedApi: PubMedApi = NetworkModule.pubMedApi
) {
    suspend fun fetchWhoNews(limit: Int = 8): List<NewsArticle> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://www.who.int/rss-feeds/news-english.xml")
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (!response.isSuccessful || body.isNullOrBlank()) return@withContext emptyList()
                parseRssItems(body, limit)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchJournalArticles(
        query: String = "public health",
        limit: Int = 6
    ): List<NewsArticle> = withContext(Dispatchers.IO) {
        try {
            val ids = pubMedApi.search(term = query, retmax = limit).esearchresult?.idlist.orEmpty()
            if (ids.isEmpty()) return@withContext emptyList()

            val resultObj = pubMedApi.summary(ids = ids.joinToString(",")).result ?: return@withContext emptyList()
            ids.mapNotNull { id ->
                val entry = resultObj.getAsJsonObject(id) ?: return@mapNotNull null
                val title = entry.get("title")?.asString?.trim().orEmpty()
                if (title.isBlank()) return@mapNotNull null

                val journal = entry.get("fulljournalname")?.asString
                    ?: entry.get("source")?.asString
                    ?: "PubMed"

                NewsArticle(
                    id = "pubmed-$id",
                    title = title,
                    category = "journal",
                    source = journal,
                    publishedAt = parsePubMedDate(entry.get("pubdate")?.asString),
                    summary = "Indexed on PubMed. Tap to view the full record.",
                    url = "https://pubmed.ncbi.nlm.nih.gov/$id/"
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseRssItems(xml: String, limit: Int): List<NewsArticle> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xml))

        val articles = mutableListOf<NewsArticle>()
        var eventType = parser.eventType
        var currentTag: String? = null
        var insideItem = false
        var title: String? = null
        var link: String? = null
        var pubDate: String? = null
        var description: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT && articles.size < limit) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                    if (currentTag == "item") {
                        insideItem = true
                        title = null; link = null; pubDate = null; description = null
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideItem) {
                        val text = parser.text?.trim()
                        if (!text.isNullOrEmpty()) {
                            when (currentTag) {
                                "title" -> title = text
                                "link" -> link = text
                                "pubDate" -> pubDate = text
                                "description" -> description = text
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "item" && insideItem) {
                        val safeTitle = title
                        val safeLink = link
                        if (!safeTitle.isNullOrBlank() && !safeLink.isNullOrBlank()) {
                            articles += NewsArticle(
                                id = "who-" + safeLink.hashCode(),
                                title = safeTitle,
                                category = "news",
                                source = "World Health Organization",
                                publishedAt = parseRfc822Date(pubDate),
                                summary = description?.take(220) ?: "",
                                url = safeLink
                            )
                        }
                        insideItem = false
                    }
                    currentTag = null
                }
            }
            eventType = parser.next()
        }
        return articles
    }

    private fun parseRfc822Date(raw: String?): Long {
        if (raw.isNullOrBlank()) return System.currentTimeMillis()
        return try {
            ZonedDateTime.parse(raw, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) {
            System.currentTimeMillis()
        }
    }

    private fun parsePubMedDate(raw: String?): Long {
        if (raw.isNullOrBlank()) return System.currentTimeMillis()

        try {
            val dayFormatter = DateTimeFormatter.ofPattern("yyyy MMM d", Locale.ENGLISH)
            return LocalDate.parse(raw, dayFormatter).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) { /* try next pattern */ }

        try {
            val monthFormatter = DateTimeFormatter.ofPattern("yyyy MMM", Locale.ENGLISH)
            return YearMonth.parse(raw, monthFormatter).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) { /* try next pattern */ }

        try {
            val yearFormatter = DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH)
            return Year.parse(raw, yearFormatter).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) { /* fall through */ }

        return System.currentTimeMillis()
    }
}
