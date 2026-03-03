package com.spiderybook.plugins.providers

import com.spiderybook.domain.model.HomePageList
import com.spiderybook.domain.model.HomePageResponse
import com.spiderybook.domain.model.SearchResponse
import com.spiderybook.domain.model.TvType
import com.spiderybook.plugins.MainAPI
import org.jsoup.Jsoup
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import kotlinx.coroutines.*

class AnimeFlvProvider @Inject constructor() : MainAPI() {
    override val name = "AnimeFLV"
    override val mainUrl = "https://www3.animeflv.net"
    
    override suspend fun getMainPage(page: Int): HomePageResponse? {
        return try {
            val client = OkHttpClient.Builder().followRedirects(true).build()
            val request = Request.Builder().url(mainUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0")
                .header("Accept-Language", "es-MX,es;q=0.9,en;q=0.8")
                .build()
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: ""
            response.body?.close()
            val doc = Jsoup.parse(html)
            val items = mutableListOf<HomePageList>()

            // 1. Latest Episodes
            val episodeList = mutableListOf<SearchResponse>()
            val episodeElements = doc.select("ul.ListEpisodios li")
            for (element in episodeElements) {
                val title = element.select("strong.Title").text()
                val episodeText = element.select("span.Capi").text()
                val link = element.select("a").attr("href")
                val imagePath = element.select("span.Image img").attr("src")
                val imageUrl = if (imagePath.startsWith("http")) imagePath else "https://animeflv.net$imagePath"
                
                // Combine title and episode for display
                val displayTitle = "$title - $episodeText"

                episodeList.add(
                    SearchResponse(
                        name = displayTitle,
                        url = if (link.startsWith("http")) link else "$mainUrl$link",
                        apiName = name,
                        type = TvType.Anime,
                        posterUrl = imageUrl,
                        year = null
                    )
                )
            }
            if (episodeList.isNotEmpty()) {
                items.add(HomePageList("Últimos Episodios", episodeList, isHorizontal = true))
            }

            // 2. Latest Animes
            val animeList = mutableListOf<SearchResponse>()
            val animeElements = doc.select("ul.ListAnimes li")
            for (element in animeElements) {
                val title = element.select("h3.Title").text()
                val link = element.select("article a").attr("href")
                val imagePath = element.select("div.Image img").attr("src")
                val imageUrl = if (imagePath.startsWith("http")) imagePath else "https://animeflv.net$imagePath"

                animeList.add(
                    SearchResponse(
                        name = title,
                        url = if (link.startsWith("http")) link else "$mainUrl$link",
                        apiName = name,
                        type = TvType.Anime,
                        posterUrl = imageUrl,
                        year = null
                    )
                )
            }
            if (animeList.isNotEmpty()) {
                items.add(HomePageList("Ultimos Animes Agregados", animeList, isHorizontal = true))
            }

            // 2b. Concurrent Fetch for Data Sections (Year & Movies)
            try {
                coroutineScope {
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    
                    // Fetch Current Year (2026) and Previous Year (2025)
                    val yearDeferred = async { 
                        val list2026 = fetchSearchResponseList("$mainUrl/browse?year=$currentYear&order=default")
                        val list2025 = fetchSearchResponseList("$mainUrl/browse?year=${currentYear - 1}&order=default")
                        list2026 + list2025
                    }
                    val moviesDeferred = async { 
                        val moviePages = (1..10).map { page ->
                            async { fetchSearchResponseList("$mainUrl/browse?type=movie&order=default&page=$page") }
                        }
                        moviePages.awaitAll().flatten()
                    }
                    
                    val yearList = yearDeferred.await()
                    if (yearList.isNotEmpty()) {
                        items.add(HomePageList("Ultimos del año", yearList, isHorizontal = true))
                    }
                    
                    val moviesList = moviesDeferred.await()
                    if (moviesList.isNotEmpty()) {
                        items.add(HomePageList("Peliculas", moviesList, isHorizontal = false, isExpanded = false))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 3. All Animes (Browse) - Sectioned by Letter
            try {
                // Fetch pages 1 to 200 concurrently (approx 4800 items - Full Catalog)
                // Using coroutineScope to wait for all
                 val allAnimes = coroutineScope {
                    val deferreds = (1..200).map { i ->
                        async {
                            try {
                                val browseUrl = "$mainUrl/browse?order=title&page=$i"
                                val client = OkHttpClient.Builder().followRedirects(true).build()
                                val request = Request.Builder().url(browseUrl)
                                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0")
                                    .header("Accept-Language", "es-MX,es;q=0.9,en;q=0.8")
                                    .build()
                                val response = client.newCall(request).execute()
                                val html = response.body?.string() ?: ""
                                response.body?.close()
                                val browseDoc = Jsoup.parse(html)
                                    
                                val browseElements = browseDoc.select("ul.ListAnimes li")
                                val pageItems = mutableListOf<SearchResponse>()
                                
                                for (element in browseElements) {
                                     val title = element.select("h3.Title").text()
                                     val link = element.select("article a").attr("href")
                                     val imagePath = element.select("div.Image img").attr("src")
                                     val imageUrl = if (imagePath.startsWith("http")) imagePath else "https://animeflv.net$imagePath"
                
                                     pageItems.add(
                                        SearchResponse(
                                            name = title,
                                            url = if (link.startsWith("http")) link else "$mainUrl$link",
                                            apiName = name,
                                            type = TvType.Anime,
                                            posterUrl = imageUrl,
                                            year = null
                                        )
                                    )
                                }
                                pageItems
                            } catch (e: Exception) {
                                e.printStackTrace()
                                emptyList<SearchResponse>()
                            }
                        }
                    }
                    deferreds.awaitAll().flatten()
                }

                if (allAnimes.isNotEmpty()) {
                    // Sort locally by name to ensure correct order after concurrent fetch
                    val sortedAnimes = allAnimes.sortedBy { it.name }
                    
                    // Group by First Letter
                    val grouped = sortedAnimes.groupBy { 
                        val firstChar = it.name.firstOrNull()?.uppercaseChar()
                        if (firstChar != null && firstChar.isLetter()) firstChar.toString() else "#"
                    }
                    
                    // Add "#" section first if exists
                    grouped["#"]?.let { list ->
                        items.add(HomePageList("#", list, isHorizontal = false, isExpanded = false))
                    }
                    
                    // Add Letter sections A-Z
                    grouped.keys.filter { it != "#" }.sorted().forEach { letter ->
                         grouped[letter]?.let { list ->
                            items.add(HomePageList(letter, list, isHorizontal = false, isExpanded = false))
                         }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            HomePageResponse(items)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    // New function for dedicated Browse/Directory
    override suspend fun getBrowsePage(page: Int): List<SearchResponse> {
         return try {
            val url = "$mainUrl/browse?order=title&page=$page"
            // Use a common User-Agent to avoid blocking
            val client = OkHttpClient.Builder().followRedirects(true).build()
            val request = Request.Builder().url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0")
                .header("Accept-Language", "es-MX,es;q=0.9,en;q=0.8")
                .build()
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: ""
            response.body?.close()
            val doc = Jsoup.parse(html)
            val items = mutableListOf<SearchResponse>()
            
            val elements = doc.select("ul.ListAnimes li")
            for (element in elements) {
                val title = element.select("h3.Title").text()
                val link = element.select("article a").attr("href")
                val imagePath = element.select("div.Image img").attr("src")
                val imageUrl = if (imagePath.startsWith("http")) imagePath else "https://animeflv.net$imagePath"

                items.add(
                    SearchResponse(
                        name = title,
                        url = if (link.startsWith("http")) link else "$mainUrl$link",
                        apiName = name,
                        type = TvType.Anime,
                        posterUrl = imageUrl,
                        year = null
                    )
                )
            }
            items
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private val genreMap = mapOf(
        "Acción" to "accion",
        "Artes Marciales" to "artes-marciales",
        "Aventuras" to "aventura",
        "Carreras" to "carreras",
        "Ciencia Ficción" to "ciencia-ficcion",
        "Comedia" to "comedia",
        "Demencia" to "demencia",
        "Demonios" to "demonios",
        "Deportes" to "deportes",
        "Drama" to "drama",
        "Ecchi" to "ecchi",
        "Escolares" to "escolares",
        "Espacial" to "espacial",
        "Fantasía" to "fantasia",
        "Harem" to "harem",
        "Historico" to "historico",
        "Infantil" to "infantil",
        "Josei" to "josei",
        "Juegos" to "juegos",
        "Magia" to "magia",
        "Mecha" to "mecha",
        "Militar" to "militar",
        "Misterio" to "misterio",
        "Música" to "musica",
        "Parodia" to "parodia",
        "Policía" to "policia",
        "Psicológico" to "psicologico",
        "Recuentos de la vida" to "recuentos-de-la-vida",
        "Romance" to "romance",
        "Samurai" to "samurai",
        "Seinen" to "seinen",
        "Shoujo" to "shoujo",
        "Shounen" to "shounen",
        "Sobrenatural" to "sobrenatural",
        "Superpoderes" to "superpoderes",
        "Suspenso" to "suspenso",
        "Terror" to "terror",
        "Vampiros" to "vampiros",
        "Yaoi" to "yaoi",
        "Yuri" to "yuri"
    )

    override suspend fun getGenres(): List<String> {
        return genreMap.keys.toList()
    }

    override suspend fun getTopSearches(): List<SearchResponse> {
        return try {
            val url = "$mainUrl/browse?order=rating"
            val client = OkHttpClient.Builder().followRedirects(true).build()
            val request = Request.Builder().url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0")
                .header("Accept-Language", "es-MX,es;q=0.9,en;q=0.8")
                .build()
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: ""
            response.body?.close()
            val doc = Jsoup.parse(html)
            val items = mutableListOf<SearchResponse>()
            
            // Limit to top 10 for "Top Searches"
            val elements = doc.select("ul.ListAnimes li").take(10)
            for (element in elements) {
                val title = element.select("h3.Title").text()
                val link = element.select("article a").attr("href")
                val imagePath = element.select("div.Image img").attr("src")
                val imageUrl = if (imagePath.startsWith("http")) imagePath else "https://animeflv.net$imagePath"

                items.add(
                    SearchResponse(
                        name = title,
                        url = if (link.startsWith("http")) link else "$mainUrl$link",
                        apiName = name,
                        type = TvType.Anime,
                        posterUrl = imageUrl,
                        year = null
                    )
                )
            }
            items
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun search(query: String, page: Int): List<SearchResponse>? {
        return try {
            // Check if query is an exact genre category
            val mappedGenre = genreMap.entries.firstOrNull { it.key.equals(query, ignoreCase = true) }?.value
            val url = if (mappedGenre != null) {
                "$mainUrl/browse?genre%5B%5D=$mappedGenre&page=$page" // ?genre[]=slug&page=1
            } else {
                "$mainUrl/browse?q=$query&page=$page"
            }
            
            val client = OkHttpClient.Builder().followRedirects(true).build()
            val request = Request.Builder().url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0")
                .header("Accept-Language", "es-MX,es;q=0.9,en;q=0.8")
                .build()
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: ""
            response.body?.close()
            val doc = Jsoup.parse(html)
            val animeList = mutableListOf<SearchResponse>()
            
            val animeElements = doc.select("ul.ListAnimes li")
            for (element in animeElements) {
                val title = element.select("h3.Title").text()
                val link = element.select("article a").attr("href")
                val imagePath = element.select("div.Image img").attr("src")
                val imageUrl = if (imagePath.startsWith("http")) imagePath else "https://animeflv.net$imagePath"

                animeList.add(
                    SearchResponse(
                        name = title,
                        url = if (link.startsWith("http")) link else "$mainUrl$link",
                        apiName = name,
                        type = TvType.Anime,
                        posterUrl = imageUrl,
                        year = null
                    )
                )
            }
            animeList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun load(url: String): com.spiderybook.domain.model.LoadResponse? {
        return try {
            val client = OkHttpClient.Builder().followRedirects(true).build()
            val request = Request.Builder().url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0")
                .header("Accept-Language", "es-MX,es;q=0.9,en;q=0.8")
                .build()
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: ""
            response.body?.close()
            val doc = Jsoup.parse(html)

            // Check if it's an episode page (URL contains /ver/)
            if (url.contains("/ver/")) {
                val animeLink = doc.select("nav.Brdcrmb a[href^=/anime/]").attr("href")
                if (animeLink.isNotEmpty()) {
                    val fullAnimeUrl = if (animeLink.startsWith("http")) animeLink else "$mainUrl$animeLink"
                    return load(fullAnimeUrl)
                }
            }

            // Parse details
            val title = doc.select("h1.Title").text()
            val plot = doc.select("div.Description p").text()
            val rating = doc.select("span#votes_prmd").text().toDoubleOrNull()
            val year = null // Difficult to extract reliably without more parsing
            val posterPath = doc.select("div.Image img").attr("src")
            val posterUrl = if (posterPath.startsWith("http")) posterPath else "https://animeflv.net$posterPath"
            
            // Parse additional info
            val genres = doc.select("nav.Nvgnrs a").map { it.text() }

            // Parse episodes from script
            val scripts = doc.select("script")
            var episodeList = listOf<com.spiderybook.domain.model.Episode>()
            var animeId: String? = null
            
            for (script in scripts) {
                val html = script.html()
                
                // Parse anime_info for ID
                if (html.contains("var anime_info =")) {
                    val pattern = java.util.regex.Pattern.compile("var anime_info = (\\[.*?\\]);")
                    val matcher = pattern.matcher(html)
                    if (matcher.find()) {
                        val json = matcher.group(1)
                        // ["2439","One Piece","one-piece-tv","2026-02-15"]
                        val parts = json?.replace("[", "")?.replace("]", "")?.replace("\"", "")?.split(",")
                        if (!parts.isNullOrEmpty()) {
                            animeId = parts[0]
                        }
                    }
                }

                if (html.contains("var episodes =")) {
                    // Extract episodes array: [[num, id], [num, id], ...]
                    val pattern = java.util.regex.Pattern.compile("var episodes = (\\[\\[.*?\\]\\]);")
                    val matcher = pattern.matcher(html)
                    if (matcher.find()) {
                        val json = matcher.group(1)
                        // Manual parsing of simple JSON array of arrays
                        val cleanJson = json?.removePrefix("[")?.removeSuffix("]")
                        if (cleanJson != null) {
                            val items = cleanJson.split("],[")
                            episodeList = items.mapNotNull { item ->
                                val parts = item.replace("[", "").replace("]", "").split(",")
                                if (parts.size >= 2) {
                                    val number = parts[0].trim()
                                    // Construct episode URL: /ver/{anime-slug}-{number}
                                    val msgUrl = url.replace("https://www3.animeflv.net", "")
                                        .replace("https://animeflv.net", "")
                                        .replace("/anime/", "")
                                    
                                    val episodeUrl = "/ver/$msgUrl-$number"
                                    
                                    // Construct Thumbnail URL
                                    // https://cdn.animeflv.net/screenshots/{animeId}/{number}/th_3.jpg
                                    val safePoster = if (!animeId.isNullOrEmpty()) {
                                        "https://cdn.animeflv.net/screenshots/$animeId/$number/th_3.jpg"
                                    } else {
                                        posterUrl // Fallback to main poster
                                    }
                                    
                                    com.spiderybook.domain.model.Episode(
                                        name = "Episodio $number",
                                        url = "$mainUrl$episodeUrl", // Absolute URL
                                        season = 1,
                                        episode = number.toIntOrNull() ?: 0,
                                        posterUrl = safePoster
                                    )
                                } else null
                            }
                        }
                    }
                    // Don't break immediately, we might find anime_info in the same or next script
                     if (episodeList.isNotEmpty() && animeId != null) break
                }
            }
            
            // Parse Related Animes (Recommendations / Temporadas)
            // Strategy: 
            // 1. Get explicit relations from page (ListAnmRel) - these have "RelType" (Secuela, etc) but no images.
            // 2. Perform a Search(title) to find ALL related content (Movies, OVAs, etc) - these have images.
            // 3. Merge lists: Explicit first (with images fetched), then Search results (excluding duplicates).

            val relatedElements = doc.select("ul.ListAnmRel li, ul.AnmRel li")
            
            val recommendations = coroutineScope {
                // Task A: Fetch explicit relations and get their images
                val explicitDeferred = async {
                    relatedElements.map { element ->
                        async {
                            try {
                                val link = element.select("a").attr("href")
                                val titleText = element.select("a").text()
                                var relType = element.ownText().replace("(", "").replace(")", "").trim()
                                if (relType.isEmpty()) {
                                     relType = element.select("i").text().replace("(", "").replace(")", "").trim()
                                }
                                val fullTitle = if (relType.isNotEmpty()) "$titleText ($relType)" else titleText
                                val fullUrl = if (link.startsWith("http")) link else "$mainUrl$link"

                                if (titleText.isNotEmpty()) {
                                    // Fetch details page to get image
                                    val client = OkHttpClient.Builder().followRedirects(true).build()
                                    val request = Request.Builder().url(fullUrl)
                                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0")
                                        .header("Accept-Language", "es-MX,es;q=0.9,en;q=0.8")
                                        .build()
                                    val response = client.newCall(request).execute()
                                    val html = response.body?.string() ?: ""
                                    response.body?.close()
                                    val detailsDoc = Jsoup.parse(html)
                                        
                                    val relPosterPath = detailsDoc.select("div.Image img").attr("src")
                                    val relPosterUrl = if (relPosterPath.startsWith("http")) relPosterPath else "https://animeflv.net$relPosterPath"
                                    
                                    SearchResponse(
                                        name = fullTitle,
                                        url = fullUrl,
                                        apiName = name,
                                        type = TvType.Anime,
                                        posterUrl = relPosterUrl,
                                        year = null
                                    )
                                } else null
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                        }
                    }.awaitAll().filterNotNull()
                }

                // Task B: Search for the anime title to find other related versions/movies
                val searchDeferred = async {
                    // Search using the raw title (e.g. "Dragon Ball")
                    // If title contains "TV", remove it? Usually title is clean.
                    search(title) ?: emptyList()
                }
                
                val explicitList = explicitDeferred.await()
                val searchList = searchDeferred.await()
                
                // Merge Logic Split:
                // 1. explicitList -> recommendations (Seasons/Sequels)
                // 2. searchList (without duplicates) -> related (Movies/OVAs/etc)
                
                val relatedList = mutableListOf<SearchResponse>()
                val currentUrl = url
                
                searchList.forEach { searchItem ->
                    // Check if already in explicit (by URL match)
                    val isDuplicate = explicitList.any { it.url == searchItem.url }
                    // Check if is self
                    val isSelf = searchItem.url == currentUrl
                    
                    if (!isDuplicate && !isSelf) {
                        relatedList.add(searchItem)
                    }
                }
                
                Pair(explicitList, relatedList)
            }
            
            com.spiderybook.domain.model.LoadResponse(
                url = url,
                name = title,
                apiName = name,
                type = TvType.Anime,
                posterUrl = posterUrl,
                year = year,
                plot = plot,
                tags = genres,
                rating = rating,
                episodes = episodeList,
                recommendations = recommendations.first,
                related = recommendations.second
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun loadLinks(data: String, callback: (ExtractorLink) -> Unit): Boolean {
        return try {
            val client = OkHttpClient.Builder().followRedirects(true).build()
            val request = Request.Builder().url(data) // 'data' is the episode URL here
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0")
                .header("Accept-Language", "es-MX,es;q=0.9,en;q=0.8")
                .build()
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: ""
            response.body?.close()
            val doc = Jsoup.parse(html)
            val scripts = doc.select("script")
            
            for (script in scripts) {
                val html = script.html()
                if (html.contains("var videos =")) {
                    // Extract videos JSON
                    val pattern = java.util.regex.Pattern.compile("var videos = (\\{.*?\\});")
                    val matcher = pattern.matcher(html)
                    if (matcher.find()) {
                        val jsonInfo = matcher.group(1)
                        if (jsonInfo != null) {
                             // Simple string parsing to avoid full JSON parser complexity if possible, 
                             // but JSON parsing is safer. Let's use JSONObject since this is Android.
                             try {
                                 val jsonObject = org.json.JSONObject(jsonInfo)
                                 val subArray = jsonObject.optJSONArray("SUB")
                                 
                                 if (subArray != null) {
                                     val linksToEmit = mutableListOf<ExtractorLink>()
                                     
                                     for (i in 0 until subArray.length()) {
                                         val serverObj = subArray.getJSONObject(i)
                                         val serverName = serverObj.optString("title")
                                         val code = serverObj.optString("code")
                                         val url = serverObj.optString("url")
                                         
                                         // Prefer 'code' as it usually contains the embed URL, fall back to 'url'
                                         val linkUrl = if (code.isNotEmpty()) code else url
                                         
                                         if (linkUrl.isNotEmpty()) {
                                             if (serverName.equals("stape", ignoreCase = true) || linkUrl.contains("streamtape")) {
                                                 val extractor = com.spiderybook.plugins.extractors.StreamtapeExtractor(this@AnimeFlvProvider)
                                                 val links = extractor.extract(linkUrl)
                                                 if (links.isEmpty()) {
                                                     linksToEmit.add(ExtractorLink(name = serverName, url = linkUrl, referer = mainUrl, quality = 0))
                                                 } else {
                                                     linksToEmit.addAll(links)
                                                 }
                                             } else if (serverName.equals("SW", ignoreCase = true) || linkUrl.contains("streamwish") || linkUrl.contains("filemoon")) {
                                                 val extractor = com.spiderybook.plugins.extractors.StreamwishExtractor(this@AnimeFlvProvider)
                                                 val links = extractor.extract(linkUrl)
                                                 if (links.isEmpty()) {
                                                     linksToEmit.add(ExtractorLink(name = serverName, url = linkUrl, referer = mainUrl, quality = 0))
                                                 } else {
                                                     linksToEmit.addAll(links)
                                                 }
                                             } else {
                                                 // Restore fallback: emit unhandled servers directly so the list isn't empty
                                                 if (!serverName.equals("Netu", ignoreCase = true) && 
                                                     !serverName.equals("Hqq", ignoreCase = true) &&
                                                     !serverName.equals("Mega", ignoreCase = true)) {
                                                     
                                                     linksToEmit.add(
                                                         ExtractorLink(
                                                             name = serverName,
                                                             url = linkUrl,
                                                             referer = mainUrl,
                                                             quality = 0, // Unknown quality
                                                             isM3u8 = linkUrl.contains(".m3u8")
                                                         )
                                                     )
                                                 }
                                             }
                                         }
                                     }
                                     
                                     // Sort: Prioritize Streamtape (or any link that is NOT just the raw embed)
                                     // Since Streamtape extractor returns "Streamtape" as name, and raw links return their original name (e.g. "MEGA", "SW")
                                     // We put "Streamtape" first.
                                     val sortedLinks = linksToEmit.sortedByDescending { it.name == "Streamtape" }
                                     
                                     sortedLinks.forEach { callback(it) }
                                 }
                             } catch (e: Exception) {
                                 e.printStackTrace()
                             }
                        }
                    }
                    break
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    private fun fetchSearchResponseList(url: String): List<SearchResponse> {
        return try {
            val doc = Jsoup.connect(url)
                 .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                 .timeout(10000)
                 .get()
            
            val items = mutableListOf<SearchResponse>()
            val elements = doc.select("ul.ListAnimes li")
            
            for (element in elements) {
                val title = element.select("h3.Title").text()
                val link = element.select("article a").attr("href")
                val imagePath = element.select("div.Image img").attr("src")
                val imageUrl = if (imagePath.startsWith("http")) imagePath else "https://animeflv.net$imagePath"

                items.add(
                    SearchResponse(
                        name = title,
                        url = if (link.startsWith("http")) link else "$mainUrl$link",
                        apiName = name,
                        type = TvType.Anime,
                        posterUrl = imageUrl,
                        year = null
                    )
                )
            }
            items
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
