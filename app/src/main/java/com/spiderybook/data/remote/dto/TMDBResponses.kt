package com.spiderybook.data.remote.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TMDBMultiSearchResponse(
    @JsonProperty("page") val page: Int?,
    @JsonProperty("results") val results: List<TMDBMediaItem>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TMDBMediaItem(
    @JsonProperty("id") val id: Int?,
    @JsonProperty("media_type") val mediaType: String?, // "movie" or "tv"
    @JsonProperty("title") val title: String?, // For movies
    @JsonProperty("name") val name: String?, // For TV
    @JsonProperty("original_title") val originalTitle: String?,
    @JsonProperty("backdrop_path") val backdropPath: String?,
    @JsonProperty("poster_path") val posterPath: String?,
    @JsonProperty("overview") val overview: String?,
    @JsonProperty("vote_average") val voteAverage: Double?
) {
    fun getDisplayTitle(): String = title ?: name ?: originalTitle ?: ""
    fun getPosterUrl(): String? = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    fun getBackdropUrl(): String? = backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TMDBVideoResponse(
    @JsonProperty("id") val id: Int?,
    @JsonProperty("results") val results: List<TMDBVideoItem>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TMDBVideoItem(
    @JsonProperty("id") val id: String?,
    @JsonProperty("key") val key: String?,
    @JsonProperty("site") val site: String?,
    @JsonProperty("type") val type: String?,
    @JsonProperty("official") val official: Boolean?
)
