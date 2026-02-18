package com.spiderybook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val url: String,
    val name: String,
    val posterUrl: String,
    val apiName: String,
    val type: String?, // TvType name
    val timestamp: Long = System.currentTimeMillis()
)
