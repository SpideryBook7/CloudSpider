

package com.spiderybook.plugins.providers

import android.util.Base64
import com.spiderybook.domain.model.Episode
import com.spiderybook.domain.model.HomePageList
import com.spiderybook.domain.model.HomePageResponse
import com.spiderybook.domain.model.LoadResponse
import com.spiderybook.domain.model.SearchResponse
import com.spiderybook.domain.model.TvType
import com.spiderybook.plugins.MainAPI
import org.jsoup.Jsoup
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.*

class PelisPlusProvider : MainAPI() {
    override val name = "PelisPlus"
    override val mainUrl = "https://tioplus.app"

    override suspend fun getMainPage(page: Int): HomePageResponse? {
        return try {
            val items = mutableListOf<HomePageList>()
            
            kotlinx.coroutines.coroutineScope {
                // 1. Fetch Homepage (Estrenos / Inicio)
                val estrenosDeferred = async {
                    try {
                        val doc = Jsoup.connect(mainUrl)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .get()
                        
                        val estrenosList = mutableListOf<SearchResponse>()
                        
                        // Parse "Episodios En Estreno" or "Ultimas Pelicúlas" sections from Home
                        // We'll grab the first carousel/grid usually found in ".owl-carousel" or similar lists
                        // Based on source, looking for common items in home
                        val homeItems = doc.select("article.item")
                        // Limit to first 20-30 items to act as "Featured"
                        homeItems.take(24).forEach { it ->
                            val title = it.select("h2").text()
                            val link = it.select("a").attr("href")
                            val poster = it.select("img").attr("data-src").ifEmpty { it.select("img").attr("src") }
                            val type = if (link.contains("/serie/") || link.contains("/episodio/")) TvType.TvSeries else TvType.Movie
                             
                            if (title.isNotEmpty() && link.isNotEmpty()) {
                                estrenosList.add(
                                    SearchResponse(
                                        name = title,
                                        url = link,
                                        apiName = name,
                                        type = type,
                                        posterUrl = if (poster.startsWith("http")) poster else "$mainUrl$poster",
                                        year = null
                                    )
                                )
                            }
                        }
                        estrenosList
                    } catch (e: Exception) {
                        e.printStackTrace()
                        emptyList<SearchResponse>()
                    }
                }

                // 2. Fetch Movies (Pages 1-5)
                val moviesDeferred = async {
                    val deferreds = (1..5).map { i ->
                        async { fetchSearchResponseList("$mainUrl/peliculas/$i") }
                    }
                    deferreds.awaitAll().flatten()
                }
                
                // 3. Fetch Series (Pages 1-3)
                val seriesDeferred = async {
                    val deferreds = (1..3).map { i ->
                        async { fetchSearchResponseList("$mainUrl/series/$i") }
                    }
                    deferreds.awaitAll().flatten()
                }
                
                // 4. Fetch All Genres (Page 1)
                // Tabs: Series, Dorama, Kids, Reality (Handled by HomeViewModel whitelist)
                // Rest: Will appear in "Inicio" as sections
                val genres = listOf(
                    "Acción" to "accion", "Animación" to "animacion", "Aventura" to "aventura",
                    "Bélica" to "belica", "Ciencia Ficción" to "ciencia-ficcion", "Comedia" to "comedia",
                    "Crimen" to "crimen", "Documental" to "documental", "Dorama" to "dorama",
                    "Drama" to "drama", "Familia" to "familia", "Fantasía" to "fantasia",
                    "Historia" to "historia", "Kids" to "kids", "Misterio" to "misterio",
                    "Música" to "musica", "Película de TV" to "pelicula-de-tv", "Reality" to "reality",
                    "Romance" to "romance", "Soap" to "soap", "Suspense" to "suspense",
                    "Terror" to "terror", "War & Politics" to "war-politics", "Western" to "western"
                )
                
                val genresDeferred = genres.map { (name, slug) ->
                    async {
                        val list = fetchSearchResponseList("$mainUrl/genero/$slug")
                        if (list.isNotEmpty()) {
                            HomePageList(name, list, isHorizontal = true)
                        } else null
                    }
                }
                
                // Wait for results
                val estrenosList = estrenosDeferred.await()
                if (estrenosList.isNotEmpty()) {
                    // This is for "Inicio" tab
                    items.add(HomePageList("Estrenos", estrenosList, isHorizontal = true)) 
                }

                val moviesList = moviesDeferred.await()
                if (moviesList.isNotEmpty()) {
                    // Standardize name to "Peliculas" (no accent) for easier logic match
                    items.add(HomePageList("Peliculas", moviesList, isHorizontal = false))
                }
                
                val seriesList = seriesDeferred.await()
                if (seriesList.isNotEmpty()) {
                    items.add(HomePageList("Series", seriesList, isHorizontal = false))
                }
                
                items.addAll(genresDeferred.awaitAll().filterNotNull())
            }
            
            HomePageResponse(items)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private val genreMap = mapOf(
        "Acción" to "accion", "Animación" to "animacion", "Aventura" to "aventura",
        "Bélica" to "belica", "Ciencia Ficción" to "ciencia-ficcion", "Comedia" to "comedia",
        "Crimen" to "crimen", "Documental" to "documental", "Dorama" to "dorama",
        "Drama" to "drama", "Familia" to "familia", "Fantasía" to "fantasia",
        "Historia" to "historia", "Kids" to "kids", "Misterio" to "misterio",
        "Música" to "musica", "Película de TV" to "pelicula-de-tv", "Reality" to "reality",
        "Romance" to "romance", "Soap" to "soap", "Suspense" to "suspense",
        "Terror" to "terror", "War & Politics" to "war-politics", "Western" to "western"
    )

    override suspend fun getGenres(): List<String> {
        return genreMap.keys.toList()
    }

    override suspend fun getTopSearches(): List<SearchResponse> {
        return try {
            val doc = Jsoup.connect(mainUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .timeout(10000)
                .get()
            
            val list = mutableListOf<SearchResponse>()
            // Using "Estrenos" from the Home page as "Top Searches" for now
            val items = doc.select("article.item").take(10)
            
            for (it in items) {
                // Titles could be inside h2 (slider/others) or div.title_over span (grid items)
                val title = it.select("h2, div.title_over span").firstOrNull()?.text().orEmpty()
                val link = it.select("a").attr("href")
                val poster = it.select("img").attr("data-src").ifEmpty { it.select("img").attr("src") }
                val type = if (link.contains("/serie/") || link.contains("/episodio/")) TvType.TvSeries else TvType.Movie
                 
                if (title.isNotEmpty() && link.isNotEmpty()) {
                    list.add(
                        SearchResponse(
                            name = title,
                            url = link,
                            apiName = name,
                            type = type,
                            posterUrl = if (poster.startsWith("http")) poster else "$mainUrl$poster",
                            year = null
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun search(query: String, page: Int): List<SearchResponse>? {
        val mappedGenre = genreMap.entries.firstOrNull { it.key.equals(query, ignoreCase = true) }?.value
        val url = if (mappedGenre != null) {
            val pagePath = if (page > 1) "/page/$page" else ""
            "$mainUrl/genero/$mappedGenre$pagePath"
        } else {
            // Standard PelisPlus search with query param
            val pageParam = if (page > 1) "?page=$page" else ""
            "$mainUrl/search/$query$pageParam"
        }
        return fetchSearchResponseList(url)
    }
    
    // New Helper Function
    private fun fetchSearchResponseList(url: String): List<SearchResponse> {
        return try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .timeout(10000)
                .get()
                
            val items = doc.select("article.item, article.item.liste.relative")
            val list = mutableListOf<SearchResponse>()
            for (it in items) {
                val title = it.select("h2, div.title_over span").firstOrNull()?.text().orEmpty()
                val link = it.select("a").attr("href")
                val poster = it.select("img").attr("data-src").ifEmpty { it.select("img").attr("src") }
                
                // Differentiate type based on url or context if possible, default to Movie/Anime
                // Usually Series URLs contain /serie/
                val type = if (link.contains("/serie/")) TvType.TvSeries else TvType.Movie
                
                if (title.isNotEmpty() && link.isNotEmpty()) {
                    list.add(
                        SearchResponse(
                            name = title,
                            url = link,
                            apiName = name,
                            type = type,
                            posterUrl = if (poster.startsWith("http")) poster else "$mainUrl$poster",
                            year = null
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        return try {
            val urlSafe = if (url.startsWith("http")) url else "$mainUrl$url"
            val doc = Jsoup.connect(urlSafe)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .timeout(10000)
                .get()
            val title = doc.select("h1.slugh1").text()
            val poster = doc.select(".bg").attr("style").substringAfter("url(\"").substringBefore("\")")
            val synopsis = doc.select(".description p").text()
            val rating = doc.select(".genres.rating span:contains(Rating)").text().replace("Rating:", "").trim().toDoubleOrNull()
            val year = doc.select(".genres.rating span:contains(Año) a").text().toIntOrNull()

            val episodes = mutableListOf<Episode>()

            // Detect if it is a Series
            val isSeries = url.contains("/serie/") || doc.select(".season").isNotEmpty()

            if (isSeries) {
                // Parse Episodes
                // Expected structure: a list of links to episodes
                // Selector strategy: Look for links containing "/season/" and "/episode/"
                val episodeLinks = doc.select("a[href*='/season/'][href*='/episode/']")
                
                episodeLinks.forEach { link ->
                    val href = link.attr("href")
                    val name = link.text()
                    // Extract Season and Episode from URL: .../season/1/episode/3
                    val seasonMatch = Regex("season/(\\d+)").find(href)
                    val episodeMatch = Regex("episode/(\\d+)").find(href)
                    
                    val season = seasonMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
                    val episodeNum = episodeMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
                    
                    episodes.add(
                        Episode(
                            name = name,
                            url = if (href.startsWith("http")) href else "$mainUrl$href",
                            season = season,
                            episode = episodeNum,
                            posterUrl = if (poster.startsWith("http")) poster else "$mainUrl$poster"
                        )
                    )
                }
                // Reverse to show S1E1 first if site lists newest first
                // Usually sites list newest first (S1E3, S1E2...), so reversing might be good.
                // However, without knowing for sure, let's sort by Season then Episode.
                episodes.sortWith(compareBy({ it.season }, { it.episode }))
            } 
            
            // Fallback for Movies or if no episodes found (treat as movie)
            if (episodes.isEmpty()) {
                episodes.add(Episode("Película", url, 1, 1))
            }

            LoadResponse(
                url = url,
                name = title,
                apiName = name,
                type = if (isSeries) TvType.TvSeries else TvType.Movie,
                posterUrl = if (poster.startsWith("http")) poster else "https://image.tmdb.org/t/p/w500$poster",
                year = year,
                plot = synopsis,
                rating = rating,
                episodes = episodes
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun loadLinks(data: String, callback: (ExtractorLink) -> Unit): Boolean {
        return try {
            val doc = Jsoup.connect(data).get()
            val options = doc.select(".subselect li")
            for (option in options) {
                val serverName = option.select("span").first()?.text() ?: "Server"
                val dataServer = option.attr("data-server")
                if (dataServer.isNotEmpty()) {
                    try {
                        // JS Logic: btoa(dataServer) -> Encoded, not decoded
                        val encoded = Base64.encodeToString(dataServer.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
                        val playerUrl = "$mainUrl/player/$encoded"
                        
                        // Fetch the player page with a proper User-Agent
                        val playerDoc = Jsoup.connect(playerUrl)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .get()
                        
                        var targetUrl = ""

                        // 1. Check for JS redirect (window.location.href)
                        val scripts = playerDoc.select("script")
                        for (script in scripts) {
                            val content = script.html()
                            val match = Regex("window\\.location\\.href\\s*=\\s*'([^']+)'").find(content)
                            if (match != null) {
                                targetUrl = match.groupValues[1]
                                break
                            }
                        }

                        // 2. Fallback to iframe
                        if (targetUrl.isEmpty()) {
                            targetUrl = playerDoc.select("iframe").attr("src")
                        }

                        if (targetUrl.isNotEmpty()) {
                            val resolvedSrc = if (targetUrl.startsWith("//")) "https:$targetUrl" else targetUrl
                            
                            if (resolvedSrc.contains("streamtape") || resolvedSrc.contains("stape")) {
                                val extractor = com.spiderybook.plugins.extractors.StreamtapeExtractor(this)
                                val links = extractor.extract(resolvedSrc)
                                if (links.isEmpty()) {
                                    callback(ExtractorLink(name = "$serverName - 1. Web/Raw", url = resolvedSrc, referer = playerUrl, quality = 0))
                                } else {
                                    for (link in links) {
                                        callback(link)
                                    }
                                }
                            } else if (resolvedSrc.contains("vidhide") || resolvedSrc.contains("filemoon") || resolvedSrc.contains("moonplayer")) {
                                val extractor = com.spiderybook.plugins.extractors.VidhideExtractor(this)
                                val links = extractor.extract(resolvedSrc)
                                if (links.isEmpty()) {
                                    callback(ExtractorLink(name = "$serverName - 1. Web/Raw", url = resolvedSrc, referer = playerUrl, quality = 0))
                                } else {
                                    for (link in links) {
                                        callback(link)
                                    }
                                }
                            } else if (resolvedSrc.contains("streamwish") || resolvedSrc.contains("sw")) {
                                val extractor = com.spiderybook.plugins.extractors.StreamwishExtractor(this)
                                val links = extractor.extract(resolvedSrc)
                                if (links.isEmpty()) {
                                    callback(ExtractorLink(name = "$serverName - 1. Web/Raw", url = resolvedSrc, referer = playerUrl, quality = 0))
                                } else {
                                    for (link in links) {
                                        callback(link)
                                    }
                                }
                            } else {
                                // MULTI-OPTION STRATEGY:
                                // 1. Provide the Raw/Web link IMMEDIATELY. This is the "Fallback" that always appears.
                                callback(
                                    ExtractorLink(
                                        name = "$serverName - 1. Web/Raw",
                                        url = resolvedSrc,
                                        referer = playerUrl,
                                        quality = 0
                                    )
                                )

                                // 2. Try Smart Extraction (Heavy operation, takes time)
                                // If successful, these will pop up as "2. Extracted" options.
                                try {
                                    val genericExtractor = com.spiderybook.plugins.extractors.GenericExtractor(this)
                                    val genericLinks = genericExtractor.extract(resolvedSrc, serverName, playerUrl)
                                    for (link in genericLinks) {
                                        callback(link)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

