package com.spiderybook.plugins.providers

import com.spiderybook.domain.model.Episode
import com.spiderybook.domain.model.HomePageList
import com.spiderybook.domain.model.HomePageResponse
import com.spiderybook.domain.model.LoadResponse
import com.spiderybook.domain.model.SearchResponse
import com.spiderybook.domain.model.TvType
import com.spiderybook.plugins.MainAPI
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*

class CinecalidadProvider : MainAPI() {
    override val name = "Cinecalidad"
    override val mainUrl = "https://www.cinecalidad.ro"
    override val lang = "es"
    override val supportedTypes = setOf(TvType.Movie)
    override val hasMainPage = true

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun getHtml(url: String): String {
        val req = Request.Builder().url(url).header("User-Agent", ua).build()
        return client.newCall(req).execute().body?.string() ?: ""
    }

    /**
     * Parse movie items from any page that uses the `div.upw-image a` pattern.
     * HTML example:
     *   <div class=upw-image>
     *     <a href="https://www.cinecalidad.ro/pelicula/..." title="Movie Title (2025)">
     *       <img src="...thumbnail.jpg" srcset="...220px.jpg 220w">
     *     </a>
     *   </div>
     */
    private fun parseMovieItems(html: String): List<SearchResponse> {
        val doc = Jsoup.parse(html)
        // Select any link to a movie that contains an image
        return doc.select("a[href*=/pelicula/]:has(img)").mapNotNull { a ->
            val href = a.attr("href")
            val imgElement = a.select("img").first() ?: return@mapNotNull null
            
            // Title can be in the <a> title, or <img> title/alt
            val rawTitle = a.attr("title").ifEmpty {
                imgElement.attr("title").ifEmpty { imgElement.attr("alt") }
            }
            if (href.isEmpty() || rawTitle.isEmpty()) return@mapNotNull null

            // Best image definition: 220w srcset fallback to src
            val srcset = imgElement.attr("srcset")
            val hdMatch = Regex("(https?://\\S+-220x\\S+\\.jpg)\\s+220w").find(srcset)
            val posterUrl = hdMatch?.groupValues?.get(1) ?: imgElement.attr("src")

            val year = Regex("\\((\\d{4})\\)").find(rawTitle)?.groupValues?.get(1)?.toIntOrNull()
            val cleanTitle = rawTitle.replace(Regex("\\s*\\(\\d{4}\\)\\s*"), "").trim()

            SearchResponse(cleanTitle, href, name, TvType.Movie, posterUrl, year)
        }
    }

    // ── API Implementation ────────────────────────────────────────────────────

    override suspend fun getMainPage(page: Int): HomePageResponse? {
        return try {
            val items = mutableListOf<HomePageList>()

            coroutineScope {
                // Fetch first 4 pages of the 4K Ultra HD section concurrently
                val pages4k = (1..14).map { pNum ->
                    async {
                        val url = if (pNum == 1) "$mainUrl/peliculas/4k-ultra-hd/" else "$mainUrl/peliculas/4k-ultra-hd/page/$pNum/"
                        parseMovieItems(getHtml(url))
                    }
                }
                
                val homeLatest = async {
                    parseMovieItems(getHtml(mainUrl))
                }

                val all4kMovies = pages4k.awaitAll()
                    .flatten()
                    .distinctBy { it.url }

                if (all4kMovies.isNotEmpty()) {
                    items.add(HomePageList("4K UHD", all4kMovies, isHorizontal = false, isExpanded = true))
                }

                val latestMovies = homeLatest.await().distinctBy { it.url }
                if (latestMovies.isNotEmpty()) {
                    val existing4kUrls = all4kMovies.map { it.url }.toSet()
                    val newLatest = latestMovies.filterNot { it.url in existing4kUrls }
                    if (newLatest.isNotEmpty()) {
                        items.add(HomePageList("Recién Agregadas", newLatest.take(30), isHorizontal = false, isExpanded = true))
                    }
                }
            }

            if (items.isEmpty()) return null
            HomePageResponse(items)
        } catch (e: Exception) {
            android.util.Log.e("CinecalidadProvider", "getMainPage error: ${e.message}")
            null
        }
    }

    override suspend fun search(query: String, page: Int): List<SearchResponse>? {
        return try {
            val url = "$mainUrl/?s=${query.trim().replace(" ", "+")}"
            parseMovieItems(getHtml(url))
        } catch (e: Exception) {
            android.util.Log.e("CinecalidadProvider", "search error: ${e.message}")
            null
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        return try {
            val doc = Jsoup.parse(getHtml(url))

            val rawTitle = doc.select("h1").first()?.text() ?: ""
            val movieTitle = rawTitle
                .replace(Regex("online o descargar gratis.*", RegexOption.IGNORE_CASE), "")
                .trim()

            // Poster: the main movie image is in single_left table
            val poster = doc.select("div.single_left img").first()?.let { img ->
                img.attr("src").ifEmpty { img.attr("data-src") }
            } ?: ""

            // Synopsis: first paragraph inside the nested table cell with description
            val plot = doc.select("div.single_left table td:last-child p").firstOrNull()?.text() ?: ""

            val year = Regex("\\((\\d{4})\\)").find(rawTitle)?.groupValues?.get(1)?.toIntOrNull()

            LoadResponse(
                url = url,
                name = movieTitle.ifEmpty { rawTitle },
                apiName = name,
                type = TvType.Movie,
                posterUrl = poster,
                year = year,
                plot = plot,
                episodes = listOf(Episode("Película Completa", url, 1, 1, poster))
            )
        } catch (e: Exception) {
            android.util.Log.e("CinecalidadProvider", "load error: ${e.message}")
            null
        }
    }

    override suspend fun loadLinks(data: String, callback: (ExtractorLink) -> Unit): Boolean {
        return try {
            val doc = Jsoup.parse(getHtml(data))
            var found = false

            // HTML structure:
            // <a class="link onlinelink" service="OnlineFilemoon" data="CODE"><li>Filemoon</li></a>
            doc.select("a.link[service]").forEach { a ->
                val service = a.attr("service").lowercase()
                val code = a.attr("data").trim()
                val labelText = a.text().trim()
                if (code.isEmpty()) return@forEach

                when {
                    service.contains("filemoon") || labelText.lowercase().contains("filemoon") -> {
                        val extractor = com.spiderybook.plugins.extractors.VidhideExtractor(this)
                        val links = extractor.extract("https://filemoon.sx/e/$code", "Filemoon HD/4K", client)
                        if (links.isEmpty()) {
                            callback(ExtractorLink("Filemoon HD/4K", "https://filemoon.sx/e/$code", mainUrl, 1080, false))
                        } else {
                            links.forEach { link -> callback(link) }
                        }
                        found = true
                    }
                    service.contains("voe") || labelText.lowercase().contains("voe") -> {
                        callback(ExtractorLink("Voe HD", "https://voe.sx/e/$code", mainUrl, 1080, false))
                        found = true
                    }
                    service.contains("doodstream") || service.contains("dood") || labelText.lowercase().contains("dood") -> {
                        callback(ExtractorLink("Doodstream", "https://dood.to/e/$code", mainUrl, 720, false))
                        found = true
                    }
                    service.contains("mega") && service.contains("online") -> {
                        if (code.contains("#")) {
                            callback(ExtractorLink("Mega 4K/HD", "https://mega.nz/file/$code", mainUrl, 1080, false))
                        } else {
                            callback(ExtractorLink("Mega HD", "https://mega.nz/file/$code", mainUrl, 1080, false))
                        }
                        found = true
                    }
                }
            }
            found
        } catch (e: Exception) {
            android.util.Log.e("CinecalidadProvider", "loadLinks error: ${e.message}")
            false
        }
    }
}
