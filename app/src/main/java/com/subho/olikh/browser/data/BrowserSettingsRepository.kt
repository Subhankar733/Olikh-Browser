package com.subho.olikh.browser.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.browserSettingsDataStore by preferencesDataStore(
    name = "browser_settings"
)

@Singleton
class BrowserSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val desktopMode = booleanPreferencesKey("desktop_mode")
        val webDarkMode = booleanPreferencesKey("web_dark_mode")
        val adBlockEnabled = booleanPreferencesKey("ad_block_enabled")
    }

    val settings: Flow<BrowserSettings> =
        context.browserSettingsDataStore.data
            .catch { error ->
                if (error is IOException) {
                    emit(androidx.datastore.preferences.core.emptyPreferences())
                } else {
                    throw error
                }
            }
            .map { preferences ->
                BrowserSettings(
                    desktopMode = preferences[Keys.desktopMode] ?: false,
                    webDarkMode = preferences[Keys.webDarkMode] ?: false,
                    adBlockEnabled = preferences[Keys.adBlockEnabled] ?: true
                )
            }

    suspend fun setDesktopMode(enabled: Boolean) {
        context.browserSettingsDataStore.edit { preferences ->
            preferences[Keys.desktopMode] = enabled
        }
    }

    suspend fun setWebDarkMode(enabled: Boolean) {
        context.browserSettingsDataStore.edit { preferences ->
            preferences[Keys.webDarkMode] = enabled
        }
    }

    suspend fun setAdBlockEnabled(enabled: Boolean) {
        context.browserSettingsDataStore.edit { preferences ->
            preferences[Keys.adBlockEnabled] = enabled
        }
    }
}
