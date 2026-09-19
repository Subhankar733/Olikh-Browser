package com.subho.olikh.browser.presentation

import com.subho.olikh.browser.DEFAULT_HOME_URL
import com.subho.olikh.browser.data.BookmarkEntity
import com.subho.olikh.browser.data.BrowserRepository
import com.subho.olikh.browser.data.BrowserSettings
import com.subho.olikh.browser.data.BrowserSettingsDataSource
import com.subho.olikh.browser.data.BrowserStorageDataSource
import com.subho.olikh.browser.data.HistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserViewModelTest {

    private val repository = object : BrowserRepository {
        override fun resolveInput(input: String): String = input
    }

    private val storage = object : BrowserStorageDataSource {
        override val bookmarks: Flow<List<BookmarkEntity>> =
            MutableStateFlow(emptyList())

        override val history: Flow<List<HistoryEntity>> =
            MutableStateFlow(emptyList())

        override suspend fun addBookmark(url: String, title: String) = Unit

        override suspend fun removeBookmark(bookmark: BookmarkEntity) = Unit

        override suspend fun clearBookmarks() = Unit

        override suspend fun addHistory(url: String, title: String) = Unit

        override suspend fun removeHistory(history: HistoryEntity) = Unit

        override suspend fun clearHistory() = Unit
    }

    private val settings = object : BrowserSettingsDataSource {
        override val settings: Flow<BrowserSettings> =
            MutableStateFlow(BrowserSettings())

        override suspend fun setDesktopMode(enabled: Boolean) = Unit

        override suspend fun setWebDarkMode(enabled: Boolean) = Unit

        override suspend fun setAdBlockEnabled(enabled: Boolean) = Unit
    }

    private fun createViewModel(): BrowserViewModel =
        BrowserViewModel(
            repository = repository,
            storageRepository = storage,
            settingsRepository = settings
        )

    @Test
    fun newTabCreatesAndActivatesSecondTab() {
        val vm = createViewModel()

        vm.newTab()

        assertEquals(2, vm.uiState.value.tabs.size)
        assertEquals("tab-2", vm.uiState.value.activeTabId)
        assertEquals(DEFAULT_HOME_URL, vm.uiState.value.currentUrl)
    }

    @Test
    fun selectTabRestoresTabUrl() {
        val vm = createViewModel()

        vm.onAddressChanged("https://example.com")
        vm.submitAddress()

        val first = vm.uiState.value.activeTabId

        vm.newTab()
        val second = vm.uiState.value.activeTabId

        vm.selectTab(first)

        assertEquals(first, vm.uiState.value.activeTabId)
        assertEquals("https://example.com", vm.uiState.value.currentUrl)
        assertTrue(first != second)
    }

    @Test
    fun closingOnlyTabLeavesExactlyOneTab() {
        val vm = createViewModel()
        val onlyTab = vm.uiState.value.activeTabId

        vm.closeTab(onlyTab)

        assertEquals(1, vm.uiState.value.tabs.size)
        assertEquals(DEFAULT_HOME_URL, vm.uiState.value.currentUrl)
    }
}
