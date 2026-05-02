package com.spiderybook.plugins

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PluginManager @Inject constructor(@ApplicationContext private val context: Context) {
    
    private val _apis = mutableListOf<MainAPI>()
    val apis: List<MainAPI> get() = _apis.toList()
    
    fun register(api: MainAPI) {
        if (_apis.none { it.name == api.name }) {
            _apis.add(api)
        }
    }
    
    init {
        register(com.spiderybook.plugins.providers.AnimeFlvProvider(context))
        register(com.spiderybook.plugins.providers.PelisPlusProvider())
        register(com.spiderybook.plugins.providers.TeraboxProvider())
        register(com.spiderybook.plugins.providers.CinecalidadProvider())
    }
    
    fun getAPI(name: String): MainAPI? {
        return _apis.find { it.name == name }
    }
}
