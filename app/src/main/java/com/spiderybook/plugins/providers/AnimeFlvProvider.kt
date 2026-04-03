package com.spiderybook.plugins.providers

import android.content.Context
import com.spiderybook.domain.model.*
import com.spiderybook.plugins.MainAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.*

class AnimeFlvProvider @Inject constructor(@ApplicationContext private val context: Context) : MainAPI() {
    override val name = "AnimeFLV"
    override val mainUrl = "https://www4.animeflv.net"
    
    // IP de tu servidor GCP (Bridge)
    private val bridgeUrl = "http://136.109.20.199:5000/get_links?url="

    /**
     * EXTRACTOR: Ahora usa el Bridge de Google Cloud para saltar el JS Challenge
     */
    // Reemplaza tu función loadLinks por esta versión más "agresiva"
    override suspend fun loadLinks(data: String, callback: (ExtractorLink) -> Unit): Boolean {
        return try {
        val bridgeUrl = "http://136.109.20.199:5000/get_links?url=$data"
        
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Aumentamos tiempo por seguridad
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
            
        val request = Request.Builder().url(bridgeUrl).build()
        val response = client.newCall(request).execute()
        val jsonBody = response.body?.string() ?: ""
        
        android.util.Log.d("SpideryDebug", "JSON de GCP: $jsonBody") // ESTO ES CLAVE

        if (jsonBody.isNotEmpty() && !jsonBody.contains("error")) {
            val jsonObject = org.json.JSONObject(jsonBody)
            val subArray = jsonObject.optJSONArray("SUB")
            
            if (subArray != null) {
                for (i in 0 until subArray.length()) {
                    val serverObj = subArray.getJSONObject(i)
                    val serverName = serverObj.optString("server").lowercase()
                    val code = serverObj.optString("code")

                    // Lógica corregida para detectar servidores y armar URLs embebidas
                    when {
                        serverName.contains("sw") || serverName.contains("wish") -> {
                            val extractor = com.spiderybook.plugins.extractors.StreamwishExtractor(this, client)
                            val embedUrl = if (code.contains("http")) code else "https://embedwish.com/e/$code"
                            extractor.extract(embedUrl).forEach { callback(it) }
                        }
                        serverName.contains("stape") || serverName.contains("tape") -> {
                            val extractor = com.spiderybook.plugins.extractors.StreamtapeExtractor(this, client)
                            val embedUrl = if (code.contains("http")) code else "https://streamtape.com/e/$code"
                            extractor.extract(embedUrl).forEach { callback(it) }
                        }
                        serverName.contains("yourupload") -> {
                            // Extractor de YourUpload o link directo
                            callback(ExtractorLink("YourUpload", code, mainUrl, 0, false))
                        }
                        else -> {
                            // Cualquier otro servidor que no necesite extracción pesada
                            if (!listOf("mega", "hqq", "netu").contains(serverName)) {
                                callback(ExtractorLink(serverName.uppercase(), code, mainUrl, 0, code.contains(".m3u8")))
                            }
                        }
                    }
                }
                return true
            }
        }
        false
    } catch (e: Exception) {
        android.util.Log.e("SpideryDebug", "Error en loadLinks: ${e.message}")
        false
    }
}

    /**
     * MAIN PAGE: Carga masiva de catálogo (A-Z), Películas y Estrenos
     */
    override suspend fun getMainPage(page: Int): HomePageResponse? {
        return try {
            val client = OkHttpClient.Builder().followRedirects(true).build()
            val request = Request.Builder().url(mainUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0")
                .build()
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: ""
            val doc = Jsoup.parse(html)
            val items = mutableListOf<HomePageList>()

            // 1. Últimos Episodios
            val episodeList = doc.select("ul.ListEpisodios li").map { element ->
                val title = element.select("strong.Title").text()
                val ep = element.select("span.Capi").text()
                val link = element.select("a").attr("href")
                val img = element.select("span.Image img").attr("src")
                SearchResponse("$title - $ep", "$mainUrl$link", name, TvType.Anime, if (img.startsWith("http")) img else "$mainUrl$img")
            }
            if (episodeList.isNotEmpty()) items.add(HomePageList("Últimos Episodios", episodeList, isHorizontal = true))

            // 2. Lógica de Secciones Concurrentes (Año y Películas)
            coroutineScope {
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                
                val yearDeferred = async { 
                    fetchSearchResponseList("$mainUrl/browse?year=$currentYear") + 
                    fetchSearchResponseList("$mainUrl/browse?year=${currentYear - 1}") 
                }
                
                val moviesDeferred = async { 
                    (1..5).map { i -> async { fetchSearchResponseList("$mainUrl/browse?type=movie&page=$i") } }
                        .awaitAll().flatten()
                }

                val yearList = yearDeferred.await()
                if (yearList.isNotEmpty()) items.add(HomePageList("Tendencias $currentYear", yearList, isHorizontal = true))
                
                val moviesList = moviesDeferred.await()
                if (moviesList.isNotEmpty()) items.add(HomePageList("Películas", moviesList, isHorizontal = false, isExpanded = true))
            }

            // 3. El Catálogo Completo A-Z (Tu especialidad)
            val allAnimes = coroutineScope {
                (1..30).map { i -> // Reducido a 30 para evitar timeouts, puedes subirlo a 200
                    async { fetchSearchResponseList("$mainUrl/browse?order=title&page=$i") }
                }.awaitAll().flatten()
            }

            if (allAnimes.isNotEmpty()) {
                allAnimes.sortedBy { it.name }.groupBy { 
                    val char = it.name.firstOrNull()?.uppercaseChar()
                    if (char != null && char.isLetter()) char.toString() else "#"
                }.forEach { (letter, list) ->
                    items.add(HomePageList(letter, list, isHorizontal = false, isExpanded = false))
                }
            }

            HomePageResponse(items)
        } catch (e: Exception) { null }
    }

    /**
     * LOAD: Detalles, Episodios y Recomendaciones cruzadas
     */
    override suspend fun load(url: String): LoadResponse? {
        return try {
            val html = OkHttpClient().newCall(Request.Builder().url(url).build()).execute().body?.string() ?: ""
            val doc = Jsoup.parse(html)

            // Manejo de links tipo /ver/ (Redirección interna)
            if (url.contains("/ver/")) {
                val animeLink = doc.select("nav.Brdcrmb a[href^=/anime/]").attr("href")
                if (animeLink.isNotEmpty()) return load("$mainUrl$animeLink")
            }

            val title = doc.select("h1.Title").text()
            val plot = doc.select("div.Description p").text()
            val poster = doc.select("div.Image img").attr("src").let { if (it.startsWith("http")) it else "$mainUrl$it" }
            
            // Script de episodios
            val script = doc.select("script").firstOrNull { it.html().contains("var episodes =") }?.html() ?: ""
            // Parse anime_info array to get the 4th element (next episode date) if it exists
            val animeInfoRegex = "var anime_info = \\[(.*?)\\];".toRegex().find(script)?.groupValues?.get(1) ?: ""
            val animeInfoParts = animeInfoRegex.split(",").map { it.trim().replace("\"", "") }
            val animeId = animeInfoParts.getOrNull(0) ?: ""
            val nextEpisodeDate = if (animeInfoParts.size >= 4) animeInfoParts[3] else ""
            
            val animeSlug = url.substringAfterLast("/")
            
            val episodes = mutableListOf<Episode>()
            
            "\\[(\\d+),\\d+\\]".toRegex().findAll(script).forEach { match ->
                val num = match.groupValues[1]
                episodes.add(Episode("Episodio $num", "$mainUrl/ver/$animeSlug-$num", 1, num.toInt(), "https://cdn.animeflv.net/screenshots/$animeId/$num/th_3.jpg"))
            }
            
            // If the anime is actively broadcasting, add the next episode notice to the top
            if (nextEpisodeDate.isNotEmpty() && nextEpisodeDate != "false" && nextEpisodeDate != "null") {
                val nextNum = if (episodes.isNotEmpty()) (episodes.first().episode ?: 0) + 1 else 1
                episodes.add(0, Episode("Próximo Episodio: $nextEpisodeDate", "next_episode", 1, nextNum, "https://i.imgur.com/K5a1a1J.png"))
            }

            // Recomendaciones (Tus Secuelas/Precuelas)
            val related = doc.select("ul.ListAnmRel li").map { element ->
                val link = element.select("a").attr("href")
                val relTitle = element.select("a").text()
                val type = element.ownText().replace("(", "").replace(")", "").trim()
                SearchResponse("$relTitle ($type)", "$mainUrl$link", name, TvType.Anime, "")
            }

            LoadResponse(url, title, name, TvType.Anime, poster, plot = plot, episodes = episodes, recommendations = related)
        } catch (e: Exception) { null }
    }

    private fun fetchSearchResponseList(url: String): List<SearchResponse> {
        return try {
            val html = OkHttpClient().newCall(Request.Builder().url(url).build()).execute().body?.string() ?: ""
            Jsoup.parse(html).select("ul.ListAnimes li").map { element ->
                val title = element.select("h3.Title").text()
                val link = element.select("article a").attr("href")
                val img = element.select("div.Image img").attr("src")
                SearchResponse(title, "$mainUrl$link", name, TvType.Anime, if (img.startsWith("http")) img else "$mainUrl$img")
            }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getGenres(): List<String> {
        return listOf(
            "Accion", "Artes Marciales", "Aventuras", "Carreras", "Ciencia Ficcion", 
            "Comedia", "Demencia", "Demonios", "Deportes", "Drama", "Ecchi", 
            "Escolares", "Espacial", "Fantasia", "Harem", "Historico", "Infantil", 
            "Josei", "Juegos", "Magia", "Mecha", "Militar", "Misterio", "Musica", 
            "Parodia", "Policial", "Psicologico", "Recuentos de la vida", "Romance", 
            "Samurai", "Seinen", "Shoujo", "Shounen", "Sobrenatural", "Superpoderes", 
            "Suspenso", "Terror", "Vampiros", "Yaoi", "Yuri"
        )
    }

    override suspend fun search(query: String, page: Int): List<SearchResponse>? {
        val genresList = getGenres().map { it.lowercase() }
        val queryLower = query.lowercase().trim()
        
        // If the query perfectly matches a known genre, search by genre parameter
        return if (genresList.contains(queryLower)) {
            val genreSlug = queryLower.replace(" ", "-")
            fetchSearchResponseList("$mainUrl/browse?genre=$genreSlug&page=$page")
        } else {
            fetchSearchResponseList("$mainUrl/browse?q=$query&page=$page")
        }
    }
}