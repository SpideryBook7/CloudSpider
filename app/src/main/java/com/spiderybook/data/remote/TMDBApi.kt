package com.spiderybook.data.remote

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.spiderybook.data.remote.dto.TMDBMultiSearchResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TMDBApi @Inject constructor(
    private val client: OkHttpClient
) {
    private val mapper = jacksonObjectMapper()

    // Public demo key often used for testing TMDB tutorials
    private val API_KEY = "16dc659bdad59197aa64c56fbbe1759d"

    suspend fun searchMulti(query: String): TMDBMultiSearchResponse? = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "https://api.themoviedb.org/3/search/multi?api_key=$API_KEY&language=es-MX&query=$encodedQuery&page=1"
            val request = Request.Builder().url(url).build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    return@withContext mapper.readValue<TMDBMultiSearchResponse>(body)
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getVideos(mediaId: Int, mediaType: String): com.spiderybook.data.remote.dto.TMDBVideoResponse? = withContext(Dispatchers.IO) {
        try {
            // Include multiple languages to ensure Anime/Asian/Latin trailers are caught and not filtered out
            val url = "https://api.themoviedb.org/3/$mediaType/$mediaId/videos?api_key=$API_KEY&include_video_language=en,es,ja,ko,null"
            val request = Request.Builder().url(url).build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    try {
                        val parsed = mapper.readValue<com.spiderybook.data.remote.dto.TMDBVideoResponse>(body)
                        android.util.Log.d("TMDB_DEBUG", "Successfully parsed ${parsed.results?.size} videos")
                        return@withContext parsed
                    } catch (parseEx: Exception) {
                        android.util.Log.e("TMDB_DEBUG", "Error parsing YouTube response", parseEx)
                        return@withContext null
                    }
                }
            }
            null
        } catch (e: Exception) {
            android.util.Log.e("TMDB_DEBUG", "Network error fetching videos", e)
            null
        }
    }
}
