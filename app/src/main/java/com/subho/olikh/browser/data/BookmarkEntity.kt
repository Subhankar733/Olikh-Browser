package com.subho.olikh.browser.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val url: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)
