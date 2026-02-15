package com.spiderybook

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import java.lang.ref.WeakReference

@HiltAndroidApp
class SpideryBookApp : Application(), ImageLoaderFactory {

    companion object {
        private var _instance: WeakReference<SpideryBookApp>? = null
        val instance: SpideryBookApp? get() = _instance?.get()

        val context: Context? get() = instance?.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        _instance = WeakReference(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .build()
    }
}

