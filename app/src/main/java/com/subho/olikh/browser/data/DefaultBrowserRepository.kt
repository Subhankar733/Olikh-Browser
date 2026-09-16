package com.subho.olikh.browser.data

import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultBrowserRepository @Inject constructor() : BrowserRepository {
    override fun resolveInput(input: String): String {
        val value = input.trim()
        if (value.isEmpty()) return DEFAULT_HOME

        val hasScheme = value.startsWith("https://", ignoreCase = true) ||
            value.startsWith("http://", ignoreCase = true)

        if (hasScheme) return value

        if (value.contains('.') && !value.contains(' ')) {
            return "https://$value"
        }

        val encoded = URLEncoder.encode(value, "UTF-8")
        return "https://www.google.com/search?q=$encoded"
    }

    private companion object {
        const val DEFAULT_HOME = "https://www.google.com"
    }
}
