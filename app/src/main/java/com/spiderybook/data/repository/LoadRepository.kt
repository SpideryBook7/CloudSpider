package com.spiderybook.data.repository

import com.spiderybook.domain.model.LoadResponse
import com.spiderybook.plugins.PluginManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadRepository @Inject constructor(
    private val pluginManager: PluginManager
) {
    suspend fun load(apiName: String, url: String): LoadResponse? {
        return pluginManager.getAPI(apiName)?.load(url)
    }

    suspend fun loadLinks(apiName: String, data: String, callback: (com.spiderybook.plugins.MainAPI.ExtractorLink) -> Unit): Boolean {
        return pluginManager.getAPI(apiName)?.loadLinks(data, callback) ?: false
    }
}

