package com.subho.olikh.browser.data

import kotlinx.coroutines.flow.Flow

interface BrowserSettingsDataSource {
    val settings: Flow<BrowserSettings>

    suspend fun setDesktopMode(enabled: Boolean)
    suspend fun setWebDarkMode(enabled: Boolean)
    suspend fun setAdBlockEnabled(enabled: Boolean)
}
