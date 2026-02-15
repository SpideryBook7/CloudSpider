package com.spiderybook.data.repository

import com.spiderybook.domain.model.SearchResponse
import com.spiderybook.plugins.PluginManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val pluginManager: PluginManager
) {
    suspend fun search(apiName: String, query: String): List<SearchResponse>? {
        return pluginManager.getAPI(apiName)?.search(query)
    }
}
