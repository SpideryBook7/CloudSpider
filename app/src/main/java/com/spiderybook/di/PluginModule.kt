package com.spiderybook.di

import android.content.Context
import com.spiderybook.plugins.PluginManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PluginModule {

    @Provides
    @Singleton
    fun providePluginManager(@ApplicationContext context: Context): PluginManager {
        return PluginManager(context)
    }
}
