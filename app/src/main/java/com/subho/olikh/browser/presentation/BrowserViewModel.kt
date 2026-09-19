package com.subho.olikh.browser.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subho.olikh.browser.BrowserTab
import com.subho.olikh.browser.DEFAULT_HOME_URL
import com.subho.olikh.browser.data.BrowserRepository
import com.subho.olikh.browser.data.BrowserSettings
import com.subho.olikh.browser.data.BrowserSettingsDataSource
import com.subho.olikh.browser.data.BrowserStorageDataSource
import com.subho.olikh.browser.data.BookmarkEntity
import com.subho.olikh.browser.data.HistoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val repository: BrowserRepository,
    private val storageRepository: BrowserStorageDataSource,
    private val settingsRepository: BrowserSettingsDataSource
) : ViewModel() {

    private var nextTabNumber = 2

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    val bookmarks: StateFlow<List<BookmarkEntity>> =
        storageRepository.bookmarks.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val history: StateFlow<List<HistoryEntity>> =
        storageRepository.history.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val settings: StateFlow<BrowserSettings> =
        settingsRepository.settings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            BrowserSettings()
        )

    fun onAddressChanged(value: String) {
        _uiState.value = _uiState.value.copy(address = value)
    }

    fun submitAddress() {
        val resolved = repository.resolveInput(_uiState.value.address)
        updateActiveTab { it.copy(url = resolved) }
        _uiState.value = _uiState.value.copy(
            address = resolved,
            currentUrl = resolved,
            isLoading = true,
            progress = 0
        )
    }

    fun newTab() {
        val id = "tab-${nextTabNumber++}"
        val tab = BrowserTab(id = id)

        _uiState.value = _uiState.value.copy(
            tabs = _uiState.value.tabs + tab,
            activeTabId = id,
            address = "",
            currentUrl = DEFAULT_HOME_URL,
            canGoBack = false,
            canGoForward = false,
            isLoading = false,
            progress = 100
        )
    }

    fun selectTab(tabId: String) {
        val tab = _uiState.value.tabs.firstOrNull { it.id == tabId } ?: return

        _uiState.value = _uiState.value.copy(
            activeTabId = tab.id,
            address = if (tab.url == DEFAULT_HOME_URL) "" else tab.url,
            currentUrl = tab.url,
            canGoBack = false,
            canGoForward = false,
            isLoading = false,
            progress = 100
        )
    }

    fun closeTab(tabId: String) {
        val state = _uiState.value

        if (state.tabs.size == 1) {
            val id = "tab-${nextTabNumber++}"
            _uiState.value = BrowserUiState(
                tabs = listOf(BrowserTab(id = id)),
                activeTabId = id
            )
            return
        }

        val closingIndex = state.tabs.indexOfFirst { it.id == tabId }
        if (closingIndex < 0) return

        val remaining = state.tabs.filterNot { it.id == tabId }

        val nextActiveId = if (state.activeTabId == tabId) {
            remaining[
                (closingIndex - 1)
                    .coerceAtLeast(0)
                    .coerceAtMost(remaining.lastIndex)
            ].id
        } else {
            state.activeTabId
        }

        val nextTab = remaining.first { it.id == nextActiveId }

        _uiState.value = state.copy(
            tabs = remaining,
            activeTabId = nextActiveId,
            address = if (nextTab.url == DEFAULT_HOME_URL) "" else nextTab.url,
            currentUrl = nextTab.url,
            canGoBack = false,
            canGoForward = false,
            isLoading = false,
            progress = 100
        )
    }

    fun onPageStarted(url: String?) {
        val resolved = url.orEmpty()
        updateActiveTab { it.copy(url = resolved) }

        _uiState.value = _uiState.value.copy(
            currentUrl = resolved,
            address = resolved,
            isLoading = true,
            progress = 0
        )
    }

    fun onPageFinished(url: String?) {
        val resolved = url.orEmpty()
        updateActiveTab { it.copy(url = resolved) }

        _uiState.value = _uiState.value.copy(
            currentUrl = resolved,
            address = resolved,
            isLoading = false,
            progress = 100
        )
    }

    fun onTitleChanged(title: String?) {
        val clean = title.orEmpty().trim()
        if (clean.isNotBlank()) {
            updateActiveTab { it.copy(title = clean) }
        }
    }

    fun onProgressChanged(progress: Int) {
        _uiState.value = _uiState.value.copy(
            progress = progress.coerceIn(0, 100),
            isLoading = progress < 100
        )
    }

    fun onNavigationStateChanged(
        canGoBack: Boolean,
        canGoForward: Boolean
    ) {
        _uiState.value = _uiState.value.copy(
            canGoBack = canGoBack,
            canGoForward = canGoForward
        )
    }

    fun addBookmark(url: String, title: String) {
        if (url.isBlank()) return

        viewModelScope.launch {
            storageRepository.addBookmark(
                url = url,
                title = title.ifBlank { url }
            )
        }
    }

    fun removeBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            storageRepository.removeBookmark(bookmark)
        }
    }

    fun clearBookmarks() {
        viewModelScope.launch {
            storageRepository.clearBookmarks()
        }
    }

    fun addHistory(url: String, title: String) {
        if (url.isBlank() || url.startsWith("data:", ignoreCase = true)) return

        viewModelScope.launch {
            storageRepository.addHistory(
                url = url,
                title = title.ifBlank { url }
            )
        }
    }

    fun removeHistory(history: HistoryEntity) {
        viewModelScope.launch {
            storageRepository.removeHistory(history)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            storageRepository.clearHistory()
        }
    }

    fun setDesktopMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDesktopMode(enabled)
        }
    }

    fun setWebDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setWebDarkMode(enabled)
        }
    }

    fun setAdBlockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAdBlockEnabled(enabled)
        }
    }

    private fun updateActiveTab(
        transform: (BrowserTab) -> BrowserTab
    ) {
        val state = _uiState.value
        _uiState.value = state.copy(
            tabs = state.tabs.map {
                if (it.id == state.activeTabId) transform(it) else it
            }
        )
    }
}
