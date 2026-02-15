package com.spiderybook.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Coroutines {
    fun main(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.Main).launch {
            work()
        }

    fun io(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.IO).launch {
            work()
        }
    
    fun <T> ioSafe(work: suspend (() -> T)): kotlinx.coroutines.Job =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                work()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
}
