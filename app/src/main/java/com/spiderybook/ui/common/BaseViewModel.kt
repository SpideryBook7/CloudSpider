package com.spiderybook.ui.common

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spiderybook.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    protected fun <T> MutableLiveData<Resource<T>>.setLoading() {
        postValue(Resource.Loading)
    }

    protected fun <T> MutableLiveData<Resource<T>>.setSuccess(data: T) {
        postValue(Resource.Success(data))
    }

    protected fun <T> MutableLiveData<Resource<T>>.setError(message: String, exception: Throwable? = null) {
        postValue(Resource.Error(message, exception))
    }

    protected fun launchIO(block: suspend CoroutineScope.() -> Unit): Job {
        return viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
