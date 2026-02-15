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
            loadHomePage(providers.first())
        }
    }

    fun loadHomePage(apiName: String) = launchIO {
        _homePage.setLoading()
        val result = homeRepository.getHomePage(apiName)
        if (result != null) {
            _homePage.setSuccess(result)
        } else {
            _homePage.setError("Failed to load home page")
        }
    }
}
