package com.spiderybook.data.local.dao

import androidx.room.*
import com.spiderybook.data.local.entity.FavoriteEntity
import com.spiderybook.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MainDao {

    // --- Favorites ---
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE url = :url LIMIT 1")
    suspend fun getFavoriteItem(url: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(media: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE url = :url OR name = :name")
    suspend fun deleteFavorite(url: String, name: String)

    @Query("DELETE FROM favorites WHERE url IN (:urls)")
    suspend fun deleteFavorites(urls: List<String>)

    @Query("SELECT EXISTS(SELECT * FROM favorites WHERE url = :url OR name = :name)")
    fun isFavorite(url: String, name: String): Flow<Boolean>

    // --- History ---
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE url = :url LIMIT 1")
    suspend fun getHistoryItem(url: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(media: HistoryEntity)
    
    @Query("DELETE FROM history WHERE url = :url")
    suspend fun deleteHistory(url: String)
    
    @Query("DELETE FROM history WHERE url IN (:urls)")
    suspend fun deleteHistoryItems(urls: List<String>)
    
    @Query("DELETE FROM history")
    suspend fun clearHistory()
}
