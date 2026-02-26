package com.spiderybook.plugins

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PluginManager @Inject constructor() {
    
    private val _apis = mutableListOf<MainAPI>()
    val apis: List<MainAPI> get() = _apis.toList()
    
    fun register(api: MainAPI) {
        if (_apis.none { it.name == api.name }) {
            _apis.add(api)
        }
    }
    
    init {
        register(com.spiderybook.plugins.providers.AnimeFlvProvider())
        register(com.spiderybook.plugins.providers.PelisPlusProvider())
        register(com.spiderybook.plugins.providers.TeraboxProvider())
    }
    
    fun getAPI(name: String): MainAPI? {
        return _apis.find { it.name == name }
    }
}
