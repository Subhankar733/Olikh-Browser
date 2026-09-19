package com.subho.olikh.browser.data

import kotlinx.coroutines.flow.Flow

interface BrowserStorageDataSource {
    val bookmarks: Flow<List<BookmarkEntity>>
    val history: Flow<List<HistoryEntity>>

    suspend fun addBookmark(url: String, title: String)
    suspend fun removeBookmark(bookmark: BookmarkEntity)
    suspend fun clearBookmarks()

    suspend fun addHistory(url: String, title: String)
    suspend fun removeHistory(history: HistoryEntity)
    suspend fun clearHistory()
}
