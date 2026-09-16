package com.subho.olikh.browser.presentation

import com.subho.olikh.browser.BrowserTab
import com.subho.olikh.browser.DEFAULT_HOME_URL

data class BrowserUiState(
    val tabs: List<BrowserTab> = listOf(
        BrowserTab(id = "tab-1", url = DEFAULT_HOME_URL)
    ),
    val activeTabId: String = "tab-1",
    val address: String = "",
    val currentUrl: String = DEFAULT_HOME_URL,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Int = 0
)
