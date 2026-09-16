package com.subho.olikh.browser.presentation

import com.subho.olikh.browser.DEFAULT_HOME_URL
import com.subho.olikh.browser.data.BrowserRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserViewModelTest {

    private val repository = object : BrowserRepository {
        override fun resolveInput(input: String): String = input
    }

    @Test
    fun newTabCreatesAndActivatesSecondTab() {
        val vm = BrowserViewModel(repository)

        vm.newTab()

        assertEquals(2, vm.uiState.value.tabs.size)
        assertEquals("tab-2", vm.uiState.value.activeTabId)
        assertEquals(DEFAULT_HOME_URL, vm.uiState.value.currentUrl)
    }

    @Test
    fun selectTabRestoresTabUrl() {
        val vm = BrowserViewModel(repository)

        vm.onAddressChanged("https://example.com")
        vm.submitAddress()

        val first = vm.uiState.value.activeTabId
        vm.newTab()
        val second = vm.uiState.value.activeTabId

        vm.selectTab(first)

        assertEquals(first, vm.uiState.value.activeTabId)
        assertEquals("https://example.com", vm.uiState.value.currentUrl)
        assertTrue(first != second)
    }

    @Test
    fun closingOnlyTabLeavesExactlyOneTab() {
        val vm = BrowserViewModel(repository)
        val onlyTab = vm.uiState.value.activeTabId

        vm.closeTab(onlyTab)

        assertEquals(1, vm.uiState.value.tabs.size)
        assertEquals(DEFAULT_HOME_URL, vm.uiState.value.currentUrl)
    }
}
