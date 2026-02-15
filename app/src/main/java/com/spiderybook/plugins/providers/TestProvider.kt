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
                null,
                2023
            )
        }
    }
}
