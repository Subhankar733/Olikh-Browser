package com.subho.olikh.browser.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class BrowserStorageRepository @Inject constructor(
    private val dao: BrowserDao
) : BrowserStorageDataSource {
    override val bookmarks: Flow<List<BookmarkEntity>> =
        dao.observeBookmarks()

    override val history: Flow<List<HistoryEntity>> =
        dao.observeHistory()

    override suspend fun addBookmark(
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

    override suspend fun removeBookmark(bookmark: BookmarkEntity) {
        dao.deleteBookmark(bookmark)
    }

    override suspend fun clearBookmarks() {
        dao.clearBookmarks()
    }

    override suspend fun addHistory(
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

    override suspend fun removeHistory(history: HistoryEntity) {
        dao.deleteHistory(history)
    }

    override suspend fun clearHistory() {
        dao.clearHistory()
    }
}
