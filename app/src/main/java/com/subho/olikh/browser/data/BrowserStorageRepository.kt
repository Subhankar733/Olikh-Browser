package com.subho.olikh.browser.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class BrowserStorageRepository @Inject constructor(
    private val dao: BrowserDao
) : BrowserStorageDataSource {
    val bookmarks: Flow<List<BookmarkEntity>> =
        dao.observeBookmarks()

    val history: Flow<List<HistoryEntity>> =
        dao.observeHistory()

    suspend fun addBookmark(
        url: String,
        title: String
    ) {
        dao.insertBookmark(
            BookmarkEntity(
                url = url,
                title = title
            )
        )
    }

    suspend fun removeBookmark(bookmark: BookmarkEntity) {
        dao.deleteBookmark(bookmark)
    }

    suspend fun clearBookmarks() {
        dao.clearBookmarks()
    }

    suspend fun addHistory(
        url: String,
        title: String
    ) {
        dao.insertHistory(
            HistoryEntity(
                url = url,
                title = title
            )
        )
    }

    suspend fun removeHistory(history: HistoryEntity) {
        dao.deleteHistory(history)
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }
}
