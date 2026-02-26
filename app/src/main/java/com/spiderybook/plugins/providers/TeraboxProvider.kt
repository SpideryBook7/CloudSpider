package com.spiderybook.plugins.providers

import com.spiderybook.BuildConfig
import com.spiderybook.domain.model.TvType
import com.spiderybook.plugins.MainAPI
import com.spiderybook.plugins.extractors.AlistExtractor
import com.spiderybook.plugins.extractors.AlistResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * A native provider that interacts directly with a local Terabox/Alist server,
 * fetching actual directories and files as if they were movies/shows.
 */
class TeraboxProvider : MainAPI() {

    override var mainUrl: String = com.spiderybook.BuildConfig.ALIST_URL.replace("\"", "").trimEnd('/')
    override var name: String = "Terabox"

    override val supportedTypes: Set<TvType> = setOf( TvType.Movie, TvType.TvSeries )
    override var lang: String = "es"

    private val client = OkHttpClient.Builder().followRedirects(true).build()

    /**
     * Replaces the homepage by scanning the root Terabox directory (`/terabox`).
     * Lists all folders/files and returns them as SearchResponse items.
     */
    override suspend fun getMainPage(page: Int): com.spiderybook.domain.model.HomePageResponse? = withContext(Dispatchers.IO) {
        val allVideos = fetchAllVideos("/terabox")
        if (allVideos.isEmpty()) return@withContext null

        val sections = mutableListOf<com.spiderybook.domain.model.HomePageList>()

        fun mapToResponse(json: JSONObject): com.spiderybook.domain.model.SearchResponse {
            val name = json.optString("name", "Desconocido").substringBeforeLast(".")
            val fullPath = json.optString("full_path", "")
            val thumbRaw = json.optString("thumb", "")
            
            val encodedName = java.net.URLEncoder.encode(name, "UTF-8")
            val poster = if (thumbRaw.isNotEmpty()) thumbRaw else "https://ui-avatars.com/api/?name=$encodedName&background=1B1B1B&color=fff&size=512&font-size=0.33"
            
            return com.spiderybook.domain.model.SearchResponse(
                name = name,
                url = fullPath,
                posterUrl = poster,
                apiName = this@TeraboxProvider.name,
                type = TvType.Movie
            )
        }

        // 1. Agregados Recientemente
        val recientes = allVideos.take(20).map { mapToResponse(it) }
        if (recientes.isNotEmpty()) {
            sections.add(com.spiderybook.domain.model.HomePageList("Agregados Recientemente", recientes))
        }

        val listDragonBall = mutableListOf<com.spiderybook.domain.model.SearchResponse>()
        val listInfantil = mutableListOf<com.spiderybook.domain.model.SearchResponse>()
        val listYears = mutableMapOf<Int, MutableList<com.spiderybook.domain.model.SearchResponse>>()
        val listOtros = mutableListOf<com.spiderybook.domain.model.SearchResponse>()

        for (video in allVideos) {
            val path = video.optString("full_path", "").lowercase()
            val year = video.optString("inferred_year", "").toIntOrNull()
            val responseItem = mapToResponse(video)

            // Skip checking the root if it's just "terabox", we check the path
            var categorized = false

            if (path.contains("dragon ball")) {
                listDragonBall.add(responseItem)
                categorized = true
            } else if (path.contains("infantil")) {
                listInfantil.add(responseItem)
                categorized = true
            } else if (year != null && year in 2020..2030) {
                if (!listYears.containsKey(year)) listYears[year] = mutableListOf()
                listYears[year]!!.add(responseItem)
                categorized = true
            }
            
            if (!categorized && !path.contains("movies")) {
                listOtros.add(responseItem)
            } else if (!categorized && path.contains("movies")) {
                // Si la carpeta se llama movies, va a otros a menos que queramos una fila "Movies"
                listOtros.add(responseItem)
            }
        }

        if (listDragonBall.isNotEmpty()) sections.add(com.spiderybook.domain.model.HomePageList("Dragon Ball", listDragonBall))
        if (listInfantil.isNotEmpty()) sections.add(com.spiderybook.domain.model.HomePageList("Infantil", listInfantil))
        
        for ((year, list) in listYears.toSortedMap(reverseOrder())) {
            sections.add(com.spiderybook.domain.model.HomePageList("Año $year", list))
        }

        if (listOtros.isNotEmpty()) {
            sections.add(com.spiderybook.domain.model.HomePageList("Otros", listOtros))
        }

        return@withContext com.spiderybook.domain.model.HomePageResponse(sections)
    }

    override suspend fun search(query: String, page: Int): List<com.spiderybook.domain.model.SearchResponse>? = withContext(Dispatchers.IO) {
        return@withContext listOf() 
    }

    override suspend fun load(url: String): com.spiderybook.domain.model.LoadResponse? = withContext(Dispatchers.IO) {
        val fileName = url.substringAfterLast("/")
        val parentDir = url.substringBeforeLast("/")
        
        val siblings = fetchDirectoryFiles(parentDir)
        val episodes = mutableListOf<com.spiderybook.domain.model.Episode>()
        val validExtensions = setOf("mp4", "mkv", "avi", "mov", "webm", "flv")

        val sortedSiblings = siblings.filter { !it.optBoolean("is_dir", false) }
                                     .filter { validExtensions.contains(it.optString("name", "").substringAfterLast(".").lowercase()) }
                                     .sortedBy { it.optString("name", "") }

        val genericParents = setOf("movies", "peliculas", "infantil", "dragon ball", "dragon ball z", "terabox", "2023", "2024", "2025", "2026")
        val isGenericParent = genericParents.any { parentDir.lowercase().contains(it) } || parentDir.substringAfterLast("/").toIntOrNull() != null
        
        val actualSiblings = if (isGenericParent) {
            // Only group if there is a specific custom show folder. Generic folders get 1 movie map
            listOf(sortedSiblings.find { it.optString("name") == fileName } ?: sortedSiblings.first())
        } else {
            // It's a specific show like "/terabox/The Batman/", list all components
            sortedSiblings
        }

        var epCount = 1
        for (fileJson in actualSiblings) {
            val name = fileJson.optString("name", "Archivo")
            val path = "$parentDir/$name"
            val thumb = fileJson.optString("thumb", "")
            
            val cleanName = name.substringBeforeLast(".")
            val encodedName = java.net.URLEncoder.encode(cleanName, "UTF-8")
            val poster = if (thumb.isNotEmpty()) thumb else "https://ui-avatars.com/api/?name=$encodedName&background=1B1B1B&color=fff&size=512&font-size=0.33"

            episodes.add(
                com.spiderybook.domain.model.Episode(
                    name = cleanName,
                    url = path,
                    episode = epCount,
                    season = 1,
                    posterUrl = poster
                )
            )
            epCount++
        }

        val cleanFileName = fileName.substringBeforeLast(".")
        val currentFileThumb = sortedSiblings.find { it.optString("name") == fileName }?.optString("thumb", "") ?: ""
        
        val folderName = parentDir.substringAfterLast("/")
        val encodedShowName = java.net.URLEncoder.encode(folderName, "UTF-8")
        val mainPoster = if (currentFileThumb.isNotEmpty()) currentFileThumb else "https://ui-avatars.com/api/?name=$encodedShowName&background=1B1B1B&color=fff&size=512&font-size=0.33"

        val yearRegex = Regex("\\b(19\\d{2}|20\\d{2})\\b")
        val inferredYear = yearRegex.find(parentDir)?.value?.toIntOrNull() ?: yearRegex.find(fileName)?.value?.toIntOrNull() ?: 2024

        return@withContext com.spiderybook.domain.model.LoadResponse(
            name = if (episodes.size > 1) folderName else cleanFileName, // TV Show name logic
            url = url,
            apiName = name,
            type = if (episodes.size > 1) TvType.TvSeries else TvType.Movie,
            posterUrl = mainPoster,
            year = inferredYear,
            plot = "Directorio local: $parentDir",
            tags = listOf("Local", "Alist"),
            rating = 10.0,
            episodes = episodes
        )
    }

    /**
     * Resolves the "episode" path into the raw direct MP4 link, executing our `AlistExtractor`.
     */
    override suspend fun loadLinks(
        data: String,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val extractor = AlistExtractor()
        // Data here is the Alist path: "/terabox/Testvideo.mp4"
        val result = extractor.getStreamUrl(data)
        
        if (result is AlistResult.Success) {
            callback(
                ExtractorLink(
                    name = "Terabox Directo",
                    url = result.url,
                    referer = "",
                    quality = 1080
                )
            )
            return true
        } else {
            return false
        }
    }

    // --- RECURSIVE FUNCTION TO FETCH ALL VIDEOS DIRECTLY ---
    private suspend fun fetchAllVideos(path: String, depth: Int = 0): List<JSONObject> = kotlinx.coroutines.coroutineScope {
        if (depth > 3) return@coroutineScope emptyList()
        val items = fetchDirectoryFiles(path)
        val result = mutableListOf<JSONObject>()
        val folders = mutableListOf<JSONObject>()

        for (item in items) {
            val isDir = item.optBoolean("is_dir", false)
            val name = item.optString("name", "")
            
            val fullPath = if (path.endsWith("/")) "$path$name" else "$path/$name"
            item.put("full_path", fullPath)
            
            if (isDir) {
                folders.add(item)
            } else {
                val ext = name.substringAfterLast(".").lowercase()
                if (setOf("mp4", "mkv", "avi", "mov", "webm", "flv").contains(ext)) {
                    val yearRegex = Regex("\\b(19\\d{2}|20\\d{2})\\b")
                    val year = yearRegex.find(path)?.value ?: yearRegex.find(name)?.value
                    item.put("inferred_year", year ?: "")
                    result.add(item)
                }
            }
        }

        val subResults = folders.map { folder ->
            this@coroutineScope.async { fetchAllVideos(folder.optString("full_path"), depth + 1) }
        }.awaitAll()

        result.addAll(subResults.flatten())
        result
    }

    // --- HELPER FUNCTION TO FETCH ALIST DIRS ---
    private suspend fun fetchDirectoryFiles(path: String): List<JSONObject> {
        return try {
            val endpoint = "$mainUrl/api/fs/list"
            val jsonBody = JSONObject().apply {
                put("path", path)
                put("password", "")
            }.toString()

            val body = jsonBody.toRequestBody("application/json".toMediaType())
            val authToken = BuildConfig.ALIST_TOKEN

            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .cacheControl(okhttp3.CacheControl.FORCE_NETWORK)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", authToken)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseString = response.body?.string()
                if (responseString != null) {
                    val json = JSONObject(responseString)
                    if (json.optInt("code") == 200) {
                        val contentArray = json.optJSONObject("data")?.optJSONArray("content")
                        if (contentArray != null) {
                            val items = mutableListOf<JSONObject>()
                            for (i in 0 until contentArray.length()) {
                                items.add(contentArray.getJSONObject(i))
                            }
                            return items
                        }
                    }
                }
            }
            emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
