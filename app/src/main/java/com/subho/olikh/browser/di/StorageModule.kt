package com.subho.olikh.browser.di

import android.content.Context
import androidx.room.Room
import com.subho.olikh.browser.data.BrowserDao
import com.subho.olikh.browser.data.BrowserDatabase
import com.subho.olikh.browser.data.BrowserSettingsDataSource
import com.subho.olikh.browser.data.BrowserSettingsRepository
import com.subho.olikh.browser.data.BrowserStorageDataSource
import com.subho.olikh.browser.data.BrowserStorageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideBrowserDatabase(
        @ApplicationContext context: Context
    ): BrowserDatabase =
        Room.databaseBuilder(
            context,
            BrowserDatabase::class.java,
            "olikh_browser.db"
        ).build()

    @Provides
    @Singleton
    fun provideBrowserDao(
        database: BrowserDatabase
    ): BrowserDao = database.browserDao()


    @Provides
    @Singleton
    fun provideBrowserStorageDataSource(
        repository: BrowserStorageRepository
    ): BrowserStorageDataSource = repository

    @Provides
    @Singleton
    fun provideBrowserSettingsDataSource(
        repository: BrowserSettingsRepository
    ): BrowserSettingsDataSource = repository
}
