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
    private val loadRepository: com.spiderybook.data.repository.LoadRepository,
    private val pluginManager: PluginManager,
    private val dataStoreManager: com.spiderybook.data.local.DataStoreManager,
    private val localRepository: com.spiderybook.data.repository.LocalRepository
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

    private val _featuredIsFavorite = MutableLiveData<Boolean>()
    val featuredIsFavorite: LiveData<Boolean> = _featuredIsFavorite
    private var favoriteJob: kotlinx.coroutines.Job? = null
    
    private val _playFirstEpisodeEvent = MutableLiveData<com.spiderybook.domain.model.LoadResponse?>()
    val playFirstEpisodeEvent: LiveData<com.spiderybook.domain.model.LoadResponse?> = _playFirstEpisodeEvent

    fun clearPlayFirstEpisodeEvent() {
        _playFirstEpisodeEvent.value = null
    }

    private val _filterCategories = MutableLiveData<List<String>>()
    val filterCategories: LiveData<List<String>> = _filterCategories

    private val _selectedCategory = MutableLiveData<String>("Inicio")
    val selectedCategory: LiveData<String> = _selectedCategory
    
    // Holds the currently displayed content. Can be List<HomePageList> (Inicio) or List<SearchResponse> (Grid)
    private val _displayedContent = MutableLiveData<Any>()
    val displayedContent: LiveData<Any> = _displayedContent
    
    private var fullHomePageResponse: HomePageResponse? = null
    private var currentHistoryList: List<com.spiderybook.data.local.entity.HistoryEntity> = emptyList()

    init {
        loadProviders()
        observeHistory()
    }
    
    private fun observeHistory() {
        launchIO {
            localRepository.getHistory().collect { history ->
                currentHistoryList = history
                // Refresh content if we are currently on the "Inicio" tab
                if (_selectedCategory.value == "Inicio") {
                    selectCategory("Inicio")
                }
            }
        }
    }

    fun loadHomePage(apiName: String) {
        if (_selectedProvider.value != apiName) {
            _selectedProvider.value = apiName
            launchIO {
                dataStoreManager.saveString(com.spiderybook.data.local.DataStoreManager.API_URL, apiName)
                _homePage.setLoading()
                val result = homeRepository.getHomePage(apiName)
                if (result != null) {
                    fullHomePageResponse = result
                    
                    // Default Trending rows to collapsed (showing only 3)
                    result.items.forEach { 
                        if (it.name == "Últimos Episodios" || it.name == "Últimos Animes" || it.name == "Trending Now") {
                            it.isExpanded = false
                        }
                    }
                    
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
                            val featured = firstList.random()
                            _featuredItem.postValue(featured)
                            checkIfFeaturedIsFavorite(featured)
                        } else {
                            _featuredItem.postValue(null)
                            checkIfFeaturedIsFavorite(null)
                        }
                    } else {
                        _featuredItem.postValue(null)
                        checkIfFeaturedIsFavorite(null)
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
            }.toMutableList()
            
            // Build the "Continue Watching" row for the currently selected provider
            val currentProvider = _selectedProvider.value
            val providerHistory = currentHistoryList.filter { it.apiName == currentProvider }
            
            if (providerHistory.isNotEmpty()) {
                val distinctHistory = providerHistory.distinctBy { 
                    if (it.showTitle.isNotEmpty()) it.showTitle else it.name 
                }
                
                val historyResponses = distinctHistory.map { historyObj ->
                    val calculatedProgress = if (historyObj.duration > 0) {
                        (historyObj.playbackPosition.toFloat() / historyObj.duration.toFloat()).coerceIn(0f, 1f)
                    } else 0f
                    
                    val timeString = if (historyObj.duration > 0) {
                        " (${formatTime(historyObj.playbackPosition)} / ${formatTime(historyObj.duration)})"
                    } else ""
                    
                    val sub = if (historyObj.showTitle.isNotEmpty() && historyObj.name != historyObj.showTitle) {
                        historyObj.name.replace(historyObj.showTitle, "").trim(' ', '-', ':') + timeString
                    } else {
                        "Episodio" + timeString // generic fallback if we don't have a distinct show title
                    }

                    com.spiderybook.domain.model.SearchResponse(
                        name = if (historyObj.showTitle.isNotEmpty()) historyObj.showTitle else historyObj.name,
                        url = historyObj.url,
                        apiName = historyObj.apiName,
                        type = if (historyObj.type != null) {
                             runCatching { enumValueOf<com.spiderybook.domain.model.TvType>(historyObj.type) }.getOrNull() 
                        } else null,
                        posterUrl = historyObj.posterUrl,
                        year = null,
                        quality = null,
                        progress = calculatedProgress,
                        subtitle = sub
                    )
                }.take(15) // Limit to top 15 recently watched to avoid large carousels
                
                val continueWatchingList = com.spiderybook.domain.model.HomePageList(
                    name = "Continuar Viendo",
                    list = historyResponses,
                    isHorizontal = true
                )
                inicioItems.add(0, continueWatchingList)
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

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    private fun checkIfFeaturedIsFavorite(featured: com.spiderybook.domain.model.SearchResponse?) {
        favoriteJob?.cancel()
        if (featured == null) {
            _featuredIsFavorite.postValue(false)
            return
        }
        favoriteJob = launchIO {
            localRepository.isFavorite(featured.url, featured.name).collect { isFav ->
                _featuredIsFavorite.postValue(isFav)
            }
        }
    }
    
    fun toggleFeaturedFavorite() = launchIO {
        val featured = _featuredItem.value ?: return@launchIO
        val isFav = _featuredIsFavorite.value ?: false
        if (isFav) {
            localRepository.deleteFavorite(featured.url, featured.name)
        } else {
            localRepository.insertFavorite(
                com.spiderybook.data.local.entity.FavoriteEntity(
                    url = featured.url,
                    name = featured.name,
                    posterUrl = featured.posterUrl ?: "",
                    apiName = featured.apiName,
                    type = featured.type?.name
                )
            )
        }
    }
    
    fun playFeaturedItem() = launchIO {
        val featured = _featuredItem.value ?: return@launchIO
        _homePage.setLoading() // Show some loading state while fetching detailed episodes
        val data = loadRepository.load(featured.apiName, featured.url)
        _homePage.setSuccess(fullHomePageResponse!!) // Restore home page state
        
        if (data != null && data.episodes.isNotEmpty()) {
            _playFirstEpisodeEvent.postValue(data)
        }
    }
}
