package com.spiderybook.data.repository

import com.spiderybook.data.local.dao.MainDao
import com.spiderybook.data.local.entity.FavoriteEntity
import com.spiderybook.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepository @Inject constructor(
    private val mainDao: MainDao
) {

    // Favorites
    fun getFavorites(): Flow<List<FavoriteEntity>> = mainDao.getFavorites()
    
    suspend fun getFavoriteItem(url: String): FavoriteEntity? = mainDao.getFavoriteItem(url)

    suspend fun insertFavorite(media: FavoriteEntity) = mainDao.insertFavorite(media)

    suspend fun deleteFavorite(url: String, name: String) = mainDao.deleteFavorite(url, name)
    
    suspend fun deleteFavoriteItems(urls: List<String>) = mainDao.deleteFavorites(urls)

    fun isFavorite(url: String, name: String): Flow<Boolean> = mainDao.isFavorite(url, name)

    // History
    fun getHistory(): Flow<List<HistoryEntity>> = mainDao.getHistory()
    
    suspend fun getHistoryItem(url: String): HistoryEntity? = mainDao.getHistoryItem(url)

    suspend fun insertHistory(media: HistoryEntity) = mainDao.insertHistory(media)
    
    suspend fun deleteHistory(url: String) = mainDao.deleteHistory(url)
    
    suspend fun deleteHistoryItems(urls: List<String>) = mainDao.deleteHistoryItems(urls)
    
    suspend fun clearHistory() = mainDao.clearHistory()
}
