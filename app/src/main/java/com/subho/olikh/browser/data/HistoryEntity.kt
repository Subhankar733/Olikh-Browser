package com.subho.olikh.browser.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey
    val url: String,
    val title: String,
    val visitedAt: Long = System.currentTimeMillis()
)
