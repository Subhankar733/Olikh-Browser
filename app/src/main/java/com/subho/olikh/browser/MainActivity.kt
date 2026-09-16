package com.subho.olikh.browser

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tab
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subho.olikh.browser.presentation.BrowserViewModel

private val Obsidian = Color(0xFF0B0E14)
private val Graphite = Color(0xFF141A24)
private val Border = Color(0xFF222A36)
private val Ice = Color(0xFFF1F5F9)
private val Slate = Color(0xFF8A96A8)
private val Sapphire = Color(0xFF2F6BFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OlikhTheme { BrowserScreen() } }
    }
}

@Composable
private fun OlikhTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = Obsidian,
            surface = Graphite,
            primary = Sapphire,
            onSurface = Ice,
            onBackground = Ice
        ),
        content = content
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun BrowserScreen(viewModel: BrowserViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(uiState.currentUrl) {
        val view = webView ?: return@LaunchedEffect
        if (view.url != uiState.currentUrl) view.loadUrl(uiState.currentUrl)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                WebView(context).apply {
                    webView = this
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(
                            view: WebView?,
                            url: String?,
                            favicon: android.graphics.Bitmap?
                        ) {
                            viewModel.onPageStarted(url)
                            viewModel.onNavigationStateChanged(
                                view?.canGoBack() == true,
                                view?.canGoForward() == true
                            )
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            viewModel.onPageFinished(url)
                            viewModel.onNavigationStateChanged(
                                view?.canGoBack() == true,
                                view?.canGoForward() == true
                            )
                        }

                        override fun doUpdateVisitedHistory(
                            view: WebView?,
                            url: String?,
                            isReload: Boolean
                        ) {
                            viewModel.onNavigationStateChanged(
                                view?.canGoBack() == true,
                                view?.canGoForward() == true
                            )
                            super.doUpdateVisitedHistory(view, url, isReload)
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean = false
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            viewModel.onProgressChanged(newProgress)
                        }
                    }
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.allowFileAccessFromFileURLs = false
                    settings.allowUniversalAccessFromFileURLs = false
                    settings.mixedContentMode =
                        android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    settings.setSupportMultipleWindows(false)
                    settings.loadsImagesAutomatically = true
                    loadUrl(uiState.currentUrl)
                }
            },
            update = { view -> webView = view }
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Omnibox(
                    value = uiState.address,
                    onValueChange = viewModel::onAddressChanged,
                    onSubmit = viewModel::submitAddress,
                    onClear = { viewModel.onAddressChanged("") }
                )

                AnimatedVisibility(
                    visible = uiState.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Border)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(uiState.progress / 100f)
                                .height(2.dp)
                                .background(Sapphire)
                        )
                    }
                }
            }

            BrowserControls(
                canGoBack = uiState.canGoBack,
                canGoForward = uiState.canGoForward,
                onBack = { webView?.goBack() },
                onForward = { webView?.goForward() },
                onRefresh = { webView?.reload() }
            )
        }
    }
}

@Composable
private fun Omnibox(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        shape = RoundedCornerShape(24.dp),
        color = Graphite,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = Slate)

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                singleLine = true,
                textStyle = TextStyle(color = Ice, fontSize = 15.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { onSubmit() }),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                "Search or enter address",
                                color = Slate,
                                fontSize = 15.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (value.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Outlined.Close, contentDescription = "Clear", tint = Slate)
                }
            } else {
                IconButton(onClick = onSubmit) {
                    Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Ice)
                }
            }
        }
    }
}

@Composable
private fun BrowserControls(
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        shape = RoundedCornerShape(22.dp),
        color = Graphite.copy(alpha = 0.98f),
        border = BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, enabled = canGoBack) {
                Icon(
                    Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = if (canGoBack) Ice else Slate
                )
            }
            IconButton(onClick = onForward, enabled = canGoForward) {
                Icon(
                    Icons.Outlined.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (canGoForward) Ice else Slate
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Refresh", tint = Ice)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Outlined.Tab, contentDescription = "Tabs", tint = Ice)
            }
        }
    }
}
