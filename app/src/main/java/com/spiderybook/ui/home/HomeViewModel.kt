package com.spiderybook.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.spiderybook.data.repository.HomeRepository
import com.spiderybook.domain.model.HomePageResponse
import com.spiderybook.plugins.PluginManager
import com.spiderybook.ui.common.BaseViewModel
import com.spiderybook.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val pluginManager: PluginManager
) : BaseViewModel() {

    private val _selectedProvider = MutableLiveData<String>()
    val selectedProvider: LiveData<String> = _selectedProvider

    private val _homePage = MutableLiveData<Resource<HomePageResponse>>()
    val homePage: LiveData<Resource<HomePageResponse>> = _homePage
    
    private val _availableProviders = MutableLiveData<List<String>>()
    val availableProviders: LiveData<List<String>> = _availableProviders

    init {
        loadProviders()
    }
    
    fun loadProviders() {
        val providers = pluginManager.apis.map { it.name }
        _availableProviders.postValue(providers)
        
        if (providers.isNotEmpty()) {
            val current = _selectedProvider.value
            if (current == null || !providers.contains(current)) {
                loadHomePage(providers.first())
            } else {
                // If we already have a selection, ensure we reload only if needed or just keep current state
                // Actually, if we return from backstack, we might want to ensure the UI reflects this.
            }
        }
    }

    private val _featuredItem = MutableLiveData<com.spiderybook.domain.model.SearchResponse?>()
    val featuredItem: LiveData<com.spiderybook.domain.model.SearchResponse?> = _featuredItem

    private val _filterCategories = MutableLiveData<List<String>>()
    val filterCategories: LiveData<List<String>> = _filterCategories

    private val _selectedCategory = MutableLiveData<String>("Inicio")
    val selectedCategory: LiveData<String> = _selectedCategory
    
    // Holds the currently displayed content. Can be List<HomePageList> (Inicio) or List<SearchResponse> (Grid)
    private val _displayedContent = MutableLiveData<Any>()
    val displayedContent: LiveData<Any> = _displayedContent
    
    private var fullHomePageResponse: HomePageResponse? = null

    fun loadHomePage(apiName: String) {
        if (_selectedProvider.value != apiName) {
            _selectedProvider.value = apiName
            launchIO {
                _homePage.setLoading()
                val result = homeRepository.getHomePage(apiName)
                if (result != null) {
                    fullHomePageResponse = result
                    _homePage.setSuccess(result)
                    
                    // Process Categories
                    val categories = mutableListOf("Inicio")
                    
                    // Add Special Tabs (Peliculas, Series, Dorama, Kids, Reality)
                    val specialTabs = listOf("Peliculas", "Series", "Dorama", "Kids", "Reality")
                    specialTabs.forEach { tab ->
                        if (result.items.any { it.name == tab }) {
                            categories.add(tab)
                        }
                    }
                    
                    val letterCategories = result.items
                        .filter { it.name.length == 1 || it.name == "#" } // Simple heuristic for our single letter names
                        .map { it.name }
                    categories.addAll(letterCategories)
                    
                    _filterCategories.postValue(categories)
                    
                    // Pick a random item from the first list as Featured (usually episodes)
                    if (result.items.isNotEmpty()) {
                        val firstList = result.items.first().list
                        if (firstList.isNotEmpty()) {
                            _featuredItem.postValue(firstList.random())
                        } else {
                            _featuredItem.postValue(null)
                        }
                    } else {
                        _featuredItem.postValue(null)
                    }
                    
                    // Load default category
                    selectCategory("Inicio")
                    
                } else {
                    _homePage.setError("Failed to load home page")
                    _featuredItem.postValue(null)
                }
            }
        }
    }
    
    fun selectCategory(category: String) {
        _selectedCategory.postValue(category)
        val data = fullHomePageResponse ?: return
        
        if (category == "Inicio") {
            // Show only non-letter sections (Updates, Episodes) AND exclude Special Tabs
            val specialTabs = listOf("Peliculas", "Series", "Dorama", "Kids", "Reality", "#")
            val inicioItems = data.items.filter { 
                it.name.length > 1 && !specialTabs.contains(it.name)
            }
            _displayedContent.postValue(inicioItems)
        } else {
            // Show the grid for the specific letter OR Peliculas
            val section = data.items.find { it.name == category }
            if (section != null) {
                _displayedContent.postValue(section.list)
            } else {
                 _displayedContent.postValue(emptyList<com.spiderybook.domain.model.SearchResponse>())
            }
        }
    }

    fun search(query: String) {
        val currentProvider = _selectedProvider.value ?: return
        if (query.isBlank()) {
             // Reset to home
             loadHomePage(currentProvider)
             return
        }
        
        launchIO {
            _homePage.setLoading()
            val api = pluginManager.apis.find { it.name == currentProvider }
            if (api != null) {
                val results = api.search(query)
                if (results != null) {
                    val searchList = com.spiderybook.domain.model.HomePageList(
                        name = "Search Results: $query",
                        list = results,
                        isHorizontal = false
                    )
                    // For search, we treat it like "Inicio" (List of Lists)
                    _homePage.setSuccess(HomePageResponse(listOf(searchList)))
                    _displayedContent.postValue(listOf(searchList))
                } else {
                    _homePage.setError("No results found")
                }
            }
        }
    }
}
