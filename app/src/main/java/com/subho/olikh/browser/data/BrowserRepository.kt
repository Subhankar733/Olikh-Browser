package com.subho.olikh.browser.data

interface BrowserRepository {
    fun resolveInput(input: String): String
}
