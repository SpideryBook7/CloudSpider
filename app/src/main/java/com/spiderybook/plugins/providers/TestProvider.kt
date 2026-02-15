package com.spiderybook.plugins.providers

import com.spiderybook.domain.model.HomePageList
import com.spiderybook.domain.model.HomePageResponse
import com.spiderybook.domain.model.SearchResponse
import com.spiderybook.domain.model.TvType
import com.spiderybook.plugins.MainAPI
import javax.inject.Inject

class TestProvider @Inject constructor() : MainAPI() {
    override val name = "Test Provider"
    override val mainUrl = "https://example.com"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override suspend fun getMainPage(page: Int): HomePageResponse {
        val movies = (1..5).map { 
            SearchResponse(
                "Movie $it", 
                "$mainUrl/movie/$it", 
                name, 
                TvType.Movie, 
                null, 
                2023
            ) 
        }
        
        val series = (1..5).map { 
            SearchResponse(
                "Series $it", 
                "$mainUrl/series/$it", 
                name, 
                TvType.TvSeries, 
                null, 
                2023
            ) 
        }

        return HomePageResponse(
            listOf(
                HomePageList("Popular Movies", movies),
                HomePageList("Trending Series", series)
            )
        )
    }

    override suspend fun search(query: String): List<SearchResponse> {
        return (1..10).map {
            SearchResponse(
                "$query Result $it",
                "$mainUrl/search/$it",
                name,
                TvType.Movie,
                "https://via.placeholder.com/300x450.png?text=Poster+$it",
                2023
            )
        }
    }

    override suspend fun load(url: String): com.spiderybook.domain.model.LoadResponse {
        return com.spiderybook.domain.model.LoadResponse(
            url = url,
            name = "Test Movie Details",
            apiName = name,
            type = TvType.Movie,
            posterUrl = "https://via.placeholder.com/300x450.png?text=Movie+Details",
            year = 2023,
            plot = "This is a test plot for the movie loaded from $url",
            episodes = (1..10).map {
                com.spiderybook.domain.model.Episode(
                    name = "Episode $it",
                    data = "$url/episode/$it",
                    episode = it,
                    posterUrl = "https://via.placeholder.com/300x200.png?text=Ep+$it"
                )
            }
        )
    }

    override suspend fun loadLinks(data: String, callback: (MainAPI.ExtractorLink) -> Unit): Boolean {
        callback(
            MainAPI.ExtractorLink(
                name = "Test 720p",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                referer = mainUrl,
                quality = 720
            )
        )
        return true
    }
}

