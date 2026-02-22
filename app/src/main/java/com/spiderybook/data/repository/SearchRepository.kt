package com.spiderybook.data.repository

import com.spiderybook.domain.model.SearchResponse
import com.spiderybook.plugins.PluginManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val pluginManager: PluginManager
) {
    suspend fun getGenres(apiName: String): List<String> {
        return pluginManager.getAPI(apiName)?.getGenres() ?: emptyList()
    }

    suspend fun getTopSearches(apiName: String): List<SearchResponse> {
        return pluginManager.getAPI(apiName)?.getTopSearches() ?: emptyList()
    }

    suspend fun search(apiName: String, query: String, page: Int = 1): List<SearchResponse>? {
        return pluginManager.getAPI(apiName)?.search(query, page)
    }
}
