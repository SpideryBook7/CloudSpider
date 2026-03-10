package com.spiderybook.data.repository

import com.spiderybook.data.remote.TMDBApi
import com.spiderybook.data.remote.dto.TMDBMediaItem
import javax.inject.Inject

class TMDBRepository @Inject constructor(
    private val api: TMDBApi
) {
    suspend fun getMetadata(title: String): TMDBMediaItem? {
        // Clean title before searching (e.g., removing 'Sub Español' or similar tags)
        val cleanTitle = title.replace(Regex("\\(.*\\)"), "")
            .replace(Regex("\\[.*\\]"), "")
            .replace("Sub Español", "", ignoreCase = true)
            .replace("Latino", "", ignoreCase = true)
            .trim()

        val response = api.searchMulti(cleanTitle)
        val results = response?.results ?: return null
        
        // Return the first valid item with a poster or backdrop
        return results.firstOrNull { it.posterPath != null || it.backdropPath != null }
    }

    suspend fun getTrailerKey(mediaId: Int, mediaType: String): String? {
        val response = api.getVideos(mediaId, mediaType)
        val videos = response?.results ?: return null
        
        // Prefer official YouTube trailers, fallback to teasers/clips
        return videos.firstOrNull { it.site == "YouTube" && it.type == "Trailer" }?.key
            ?: videos.firstOrNull { it.site == "YouTube" && it.type == "Teaser" }?.key
            ?: videos.firstOrNull { it.site == "YouTube" }?.key
    }
}
