package com.spiderybook.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.spiderybook.data.repository.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: LocalRepository
) : ViewModel() {
    val history = repository.getHistory().asLiveData()

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
