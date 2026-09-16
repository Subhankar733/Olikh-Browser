package com.subho.olikh.browser.presentation

import androidx.lifecycle.ViewModel
import com.subho.olikh.browser.data.BrowserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val repository: BrowserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    fun onAddressChanged(value: String) {
        _uiState.value = _uiState.value.copy(address = value)
    }

    fun submitAddress() {
        val resolved = repository.resolveInput(_uiState.value.address)
        _uiState.value = _uiState.value.copy(
            address = resolved,
            currentUrl = resolved
        )
    }

    fun onPageStarted(url: String?) {
        _uiState.value = _uiState.value.copy(
            currentUrl = url.orEmpty(),
            address = url.orEmpty(),
            isLoading = true,
            progress = 0
        )
    }

    fun onPageFinished(url: String?) {
        _uiState.value = _uiState.value.copy(
            currentUrl = url.orEmpty(),
            address = url.orEmpty(),
            isLoading = false,
            progress = 100
        )
    }

    fun onProgressChanged(progress: Int) {
        _uiState.value = _uiState.value.copy(
            progress = progress.coerceIn(0, 100),
            isLoading = progress < 100
        )
    }

    fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.value = _uiState.value.copy(
            canGoBack = canGoBack,
            canGoForward = canGoForward
        )
    }
}
