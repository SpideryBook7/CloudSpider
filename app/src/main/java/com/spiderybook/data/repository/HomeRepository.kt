package com.spiderybook.data.repository

import com.spiderybook.domain.model.HomePageResponse
import com.spiderybook.plugins.PluginManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val pluginManager: PluginManager
) {
    
    suspend fun getHomePage(apiName: String): HomePageResponse? {
        return pluginManager.getAPI(apiName)?.getMainPage(1)
    }
}
