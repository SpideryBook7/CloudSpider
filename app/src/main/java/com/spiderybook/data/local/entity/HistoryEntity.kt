package com.spiderybook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val url: String,
    val name: String,
    val posterUrl: String,
    val apiName: String,
    val type: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val playbackPosition: Long = 0,
    val duration: Long = 0,
    val showTitle: String = ""
)
