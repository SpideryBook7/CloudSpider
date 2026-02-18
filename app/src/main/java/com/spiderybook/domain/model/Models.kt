package com.spiderybook.domain.model

data class HomePageResponse(
    val items: List<HomePageList>,
    val hasNext: Boolean = false
)

data class HomePageList(
    val name: String,
    val list: List<SearchResponse>,
    val isHorizontal: Boolean = true,
    var isExpanded: Boolean = true
)

data class SearchResponse(
    val name: String,
    val url: String,
    val apiName: String,
    val type: TvType? = null,
    val posterUrl: String? = null,
    val year: Int? = null,
    val quality: String? = null
)

data class LoadResponse(
    val url: String,
    val name: String,
    val apiName: String,
    val type: TvType? = null,
    val posterUrl: String? = null,
    val year: Int? = null,
    val plot: String? = null,
    val tags: List<String>? = null,
    val rating: Double? = null,
    val actors: List<String>? = null,
    val recommendations: List<SearchResponse> = emptyList(),
    val related: List<SearchResponse> = emptyList(),
    val episodes: List<Episode> = emptyList()
)

data class Episode(
    val name: String,
    val url: String, // Data/link
    val season: Int? = null,
    val episode: Int? = null,
    val posterUrl: String? = null,
    val rating: Int? = null,
    val description: String? = null
)

enum class TvType {
    Movie, TvSeries, Anime, AnimeMovie, Cartoon, Documental, OVA, Torrent, Unknown
}
