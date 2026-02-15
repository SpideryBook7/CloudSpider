package com.spiderybook

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.PlatformContext
import dagger.hilt.android.HiltAndroidApp
import java.lang.ref.WeakReference

@HiltAndroidApp
class SpideryBookApp : Application(), SingletonImageLoader.Factory {

    companion object {
        private var _instance: WeakReference<SpideryBookApp>? = null
        val instance: SpideryBookApp? get() = _instance?.get()

        val context: Context? get() = instance?.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        _instance = WeakReference(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }
}
