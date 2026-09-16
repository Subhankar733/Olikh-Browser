package com.subho.olikh.browser.di

import com.subho.olikh.browser.data.BrowserRepository
import com.subho.olikh.browser.data.DefaultBrowserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBrowserRepository(
        implementation: DefaultBrowserRepository
    ): BrowserRepository
}
