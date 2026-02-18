package com.spiderybook.di

import android.app.Application
import androidx.room.Room
import com.spiderybook.data.local.AppDatabase
import com.spiderybook.data.local.dao.MainDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "spiderybook_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideMainDao(db: AppDatabase): MainDao {
        return db.mainDao()
    }
}
