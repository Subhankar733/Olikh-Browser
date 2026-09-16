package com.subho.olikh.browser.presentation

data class BrowserUiState(
    val address: String = "",
    val currentUrl: String = "https://www.google.com",
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Int = 0
)
