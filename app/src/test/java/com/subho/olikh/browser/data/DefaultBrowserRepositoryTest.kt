package com.subho.olikh.browser.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultBrowserRepositoryTest {
    private val repository = DefaultBrowserRepository()

    @Test
    fun `blank input opens home`() {
        assertEquals("https://www.google.com", repository.resolveInput("   "))
    }

    @Test
    fun `host input gets https scheme`() {
        assertEquals("https://example.com", repository.resolveInput("example.com"))
    }

    @Test
    fun `search text becomes google search`() {
        assertEquals(
            "https://www.google.com/search?q=hello+world",
            repository.resolveInput("hello world")
        )
    }
}
