package com.spiderybook.data.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.spiderybook.data.local.entity.FavoriteEntity
import com.spiderybook.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.first
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

data class BackupData(
    val history: List<HistoryEntity> = emptyList(),
    val favorites: List<FavoriteEntity> = emptyList()
)

class LocalBackupManager @Inject constructor(
    private val localRepository: LocalRepository
) {
    private val mapper = jacksonObjectMapper()

    suspend fun exportDatabase(outputStream: OutputStream) {
        val history = localRepository.getHistory().first()
        val favorites = localRepository.getFavorites().first()
        
        val backupData = BackupData(history, favorites)
        
        outputStream.use { stream ->
            mapper.writeValue(stream, backupData)
        }
    }

    suspend fun importDatabase(inputStream: InputStream): Result<Unit> {
        return try {
            val backupData = inputStream.use { stream ->
                mapper.readValue(stream, BackupData::class.java)
            }
            
            if (backupData.history.isNotEmpty()) {
                backupData.history.forEach { localRepository.insertHistory(it) }
            }
            
            if (backupData.favorites.isNotEmpty()) {
                backupData.favorites.forEach { localRepository.insertFavorite(it) }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
