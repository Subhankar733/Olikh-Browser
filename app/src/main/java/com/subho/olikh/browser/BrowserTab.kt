package com.subho.olikh.browser

const val DEFAULT_HOME_URL = "https://www.google.com"

data class BrowserTab(
    val id: String,
    val title: String = "New Tab",
    val url: String = DEFAULT_HOME_URL
)
