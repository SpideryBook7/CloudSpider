package com.spiderybook.plugins

import com.spiderybook.domain.model.HomePageResponse
import com.spiderybook.domain.model.LoadResponse
import com.spiderybook.domain.model.SearchResponse
import com.spiderybook.domain.model.TvType

abstract class MainAPI {
    abstract val name: String
    abstract val mainUrl: String
    open val lang: String = "en"
    
    open val supportedTypes: Set<TvType> = emptySet()
    
    open val hasMainPage: Boolean = false
    open val hasQuickSearch: Boolean = true
    open val hasSearch: Boolean = true

    open suspend fun getMainPage(page: Int): HomePageResponse? = null
    
    open suspend fun search(query: String): List<SearchResponse>? = null
    
    open suspend fun load(url: String): LoadResponse? = null
    
    // Simplification: We'll deal with video links later
    // open suspend fun loadLinks(...)
}
