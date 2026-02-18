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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(media: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE url = :url")
    suspend fun deleteFavorite(url: String)

    @Query("SELECT EXISTS(SELECT * FROM favorites WHERE url = :url)")
    fun isFavorite(url: String): Flow<Boolean>

    // --- History ---
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE url = :url LIMIT 1")
    suspend fun getHistoryItem(url: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(media: HistoryEntity)
    
    @Query("DELETE FROM history WHERE url = :url")
    suspend fun deleteHistory(url: String)
    
    @Query("DELETE FROM history")
    suspend fun clearHistory()
}
