package com.spiderybook.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.spiderybook.data.local.DataStoreManager
import com.spiderybook.plugins.PluginManager
import com.spiderybook.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val pluginManager: PluginManager
) : BaseViewModel() {

    private val _availableProviders = MutableLiveData<List<String>>()
    val availableProviders: LiveData<List<String>> = _availableProviders

    private val _selectedProvider = MutableLiveData<String>()
    val selectedProvider: LiveData<String> = _selectedProvider

    private val _currentTheme = MutableLiveData<Int>()
    val currentTheme: LiveData<Int> = _currentTheme

    init {
        loadProviders()
        loadPreferences()
    }

    private fun loadProviders() {
        _availableProviders.value = pluginManager.apis.map { it.name }
    }

    private fun loadPreferences() {
        launchIO {
            dataStoreManager.readString(DataStoreManager.API_URL).collect { provider ->
                if (provider.isNotEmpty()) {
                    _selectedProvider.postValue(provider)
                } else if (!pluginManager.apis.isNullOrEmpty()) {
                     _selectedProvider.postValue(pluginManager.apis.first().name)
                }
            }
        }
        
        launchIO {
            dataStoreManager.readInt(DataStoreManager.THEME_MODE).collect { mode ->
                // Default to FOLLOW_SYSTEM(-1) if not set (which comes as 0 from DataStore default usually, so handle carefully)
                val resolvedMode = if (mode == 0) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else mode
                _currentTheme.postValue(resolvedMode)
            }
        }
    }

    fun saveProvider(provider: String) {
        launchIO {
            dataStoreManager.saveString(DataStoreManager.API_URL, provider)
        }
    }

    fun saveTheme(mode: Int) {
        launchIO {
            dataStoreManager.saveInt(DataStoreManager.THEME_MODE, mode)
        }
    }
}
