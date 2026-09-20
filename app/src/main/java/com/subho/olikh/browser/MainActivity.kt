package com.subho.olikh.browser
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.DeleteOutline
import android.content.Intent
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import android.webkit.WebView
import android.webkit.WebViewClient
import android.net.http.SslError
import android.webkit.SslErrorHandler
import androidx.webkit.WebViewClientCompat
import androidx.webkit.SafeBrowsingResponseCompat
import android.app.DownloadManager
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.key
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tab
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subho.olikh.browser.presentation.BrowserViewModel
import com.subho.olikh.browser.presentation.OlikhBrowserSurface
import com.subho.olikh.browser.presentation.OlikhCommandDock
import com.subho.olikh.browser.presentation.OlikhTabSurface
import com.subho.olikh.browser.presentation.OlikhDesignSystem.Dimensions
import dagger.hilt.android.AndroidEntryPoint

private val Obsidian = Color(0xFF080B12)
private val DeepNavy = Color(0xFF0B0D13)
private val Glass = Color(0xFF141C2B)
private val Border = Color(0xFF1E2330)
private val Ice = Color(0xFFEDEDF2)
private val Slate = Color(0xFF7A839E)
private val Sapphire = Color(0xFF6366F1)
private val Electric = Color(0xFF79A7FF)

private val AD_DOMAINS = setOf(
    "doubleclick.net", "googleadservices.com", "googlesyndication.com",
    "adnxs.com", "pagead2.googlesyndication.com", "adservice.google.com"
)

@AndroidEntryPoint
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
            surface = Glass,
            primary = Sapphire,
            onSurface = Ice,
            onBackground = Ice
        ),
        content = content
    )
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowserScreen(viewModel: BrowserViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var showTabs by rememberSaveable { mutableStateOf(false) }
    val browserSettings by viewModel.settings.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val currentAdBlockEnabled by androidx.compose.runtime.rememberUpdatedState(browserSettings.adBlockEnabled)
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showBookmarksDialog by remember { mutableStateOf(false) }
    val savedWebViewStates = remember { mutableStateMapOf<String, Bundle>() }
    val activeTab = uiState.tabs.first { it.id == uiState.activeTabId }
    BackHandler(enabled = showTabs || uiState.canGoBack) {
        if (showTabs) showTabs = false else if (webView?.canGoBack() == true) webView?.goBack()
    }

    LaunchedEffect(uiState.currentUrl) {
        val view = webView ?: return@LaunchedEffect
        if (view.url != uiState.currentUrl) view.loadUrl(uiState.currentUrl)
    }

    LaunchedEffect(
        browserSettings.desktopMode,
        browserSettings.webDarkMode,
        browserSettings.adBlockEnabled
    ) {
        webView?.reload()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        key(activeTab.id) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                WebView(context).apply {
                    webView = this
                    webViewClient = object : WebViewClientCompat() {
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

                            val resolvedUrl = url.orEmpty()
                            if (resolvedUrl.isNotBlank()) {
                                viewModel.addHistory(
                                    resolvedUrl,
                                    view?.title.orEmpty()
                                )
                            }
                        }

                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: SslErrorHandler?,
                            error: SslError?
                        ) {
                            handler?.cancel()
                            Toast.makeText(
                                context,
                                "Secure connection error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onSafeBrowsingHit(
                            view: WebView,
                            request: WebResourceRequest,
                            threatType: Int,
                            callback: SafeBrowsingResponseCompat
                        ) {
                            if (
                                WebViewFeature.isFeatureSupported(
                                    WebViewFeature.SAFE_BROWSING_RESPONSE_BACK_TO_SAFETY
                                )
                            ) {
                                callback.backToSafety(true)
                                Toast.makeText(
                                    view.context,
                                    "Unsafe web page blocked.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
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

                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val host = request?.url?.host?.lowercase() ?: return null
                            if (
                                currentAdBlockEnabled &&
                                AD_DOMAINS.any { domain ->
                                    host == domain || host.endsWith(".$domain")
                                }
                            ) {
                                return WebResourceResponse(
                                    "text/plain",
                                    "utf-8",
                                    ByteArrayInputStream(ByteArray(0))
                                )
                            }
                            return super.shouldInterceptRequest(view, request)
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest
                        ): Boolean {
                            val uri = request?.url ?: return false
                            val scheme = uri.scheme?.lowercase()

                            if (scheme == "http" || scheme == "https") {
                                return false
                            }

                            return try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, uri)
                                )
                                true
                            } catch (_: android.content.ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    "No app can open this link",
                                    Toast.LENGTH_SHORT
                                ).show()
                                true
                            }
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            viewModel.onProgressChanged(newProgress)
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            viewModel.onTitleChanged(title)
                        }
                    }
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.mixedContentMode =
                        android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    settings.setSupportMultipleWindows(false)
                    settings.loadsImagesAutomatically = true
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    if (
                        WebViewFeature.isFeatureSupported(
                            WebViewFeature.SAFE_BROWSING_ENABLE
                        )
                    ) {
                        WebSettingsCompat.setSafeBrowsingEnabled(settings, true)
                    }

                    settings.userAgentString =
                        android.webkit.WebSettings.getDefaultUserAgent(context)
                    settings.useWideViewPort = false
                    settings.loadWithOverviewMode = false
                    if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                        WebSettingsCompat.setForceDark(
                            settings,
                            if (browserSettings.webDarkMode) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
                        )
                    }
                    setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
                        val request = DownloadManager.Request(Uri.parse(url)).apply {
                            setMimeType(mimetype)
                            addRequestHeader("User-Agent", userAgent)
                            setDescription("Downloading file...")
                            setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
                            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype))
                        }
                        val dm = context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as DownloadManager
                        dm.enqueue(request)
                        Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show()
                    }

                    val savedState = savedWebViewStates[activeTab.id]
                    if (savedState != null) {
                        restoreState(savedState)
                    } else {
                        loadUrl(activeTab.url)
                    }
                }
            },
            update = { view ->
                webView = view

                val desktopUserAgent =
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

                view.settings.userAgentString =
                    if (browserSettings.desktopMode) {
                        desktopUserAgent
                    } else {
                        android.webkit.WebSettings.getDefaultUserAgent(context)
                    }

                view.settings.useWideViewPort = browserSettings.desktopMode
                view.settings.loadWithOverviewMode = browserSettings.desktopMode

                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(
                        view.settings,
                        if (browserSettings.webDarkMode) {
                            WebSettingsCompat.FORCE_DARK_ON
                        } else {
                            WebSettingsCompat.FORCE_DARK_OFF
                        }
                    )
                }
            },
            onRelease = { released ->
                val state = Bundle()
                released.saveState(state)
                savedWebViewStates[activeTab.id] = state
                released.destroy()
            }
        )
    }

        Box(modifier = Modifier.fillMaxSize()) {
            OlikhBrowserSurface(
                modifier = Modifier.fillMaxSize(),
                title = activeTab.title,
                address = uiState.address,
                isSecure = uiState.address.startsWith("https://", ignoreCase = true),
                isChromeVisible = true,
                tabCount = uiState.tabs.size,
                onSurfaceClick = {
                    viewModel.onAddressChanged(uiState.address)
                },
                onMenuClick = {
                    showTabs = true
                }
            ) {
                key(activeTab.id) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { webView!! }
                    )
                }
            }

            OlikhCommandDock(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(
                        horizontal = Dimensions.SpaceLg,
                        vertical = Dimensions.SpaceSm
                    ),
                canGoBack = uiState.canGoBack,
                canGoForward = uiState.canGoForward,
                tabCount = uiState.tabs.size,
                onBack = { webView?.goBack() },
                onForward = { webView?.goForward() },
                onRefresh = { webView?.reload() },
                onTabs = { showTabs = true }
            )
        }

        if (showBookmarksDialog) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showBookmarksDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DeepNavy,
                    border = BorderStroke(1.dp, Border),
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bookmarks", color = Ice, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            if (bookmarks.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearBookmarks() }) {
                                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Clear Bookmarks", tint = Slate)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        if (bookmarks.isEmpty()) {
                            Text("No bookmarks saved.", color = Slate, fontSize = 14.sp)
                        } else {
                            bookmarks.forEach { item ->
                                val bTitle = item.title
                                val bUrl = item.url
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.onAddressChanged(bUrl)
                                            viewModel.submitAddress()
                                            showBookmarksDialog = false
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(bTitle, color = Ice, fontSize = 14.sp, maxLines = 1)
                                        Text(bUrl, color = Slate, fontSize = 11.sp, maxLines = 1)
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeBookmark(item) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove bookmark",
                                            tint = Slate,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showHistoryDialog) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showHistoryDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DeepNavy,
                    border = BorderStroke(1.dp, Border),
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("History", color = Ice, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            if (history.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearHistory() }) {
                                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Clear History", tint = Slate)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        if (history.isEmpty()) {
                            Text("No browsing history.", color = Slate, fontSize = 14.sp)
                        } else {
                            history.take(20).forEach { item ->
                                val hTitle = item.title
                                val hUrl = item.url
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.onAddressChanged(hUrl)
                                            viewModel.submitAddress()
                                            showHistoryDialog = false
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(hTitle, color = Ice, fontSize = 14.sp, maxLines = 1)
                                        Text(hUrl, color = Slate, fontSize = 11.sp, maxLines = 1)
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeHistory(item) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove item",
                                            tint = Slate,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showTabs) {
            ModalBottomSheet(
                onDismissRequest = { showTabs = false },
                containerColor = Obsidian,
                contentColor = Ice
            ) {
                OlikhTabSurface(
                    tabs = uiState.tabs,
                    activeTabId = uiState.activeTabId,
                    onNewTab = {
                        showTabs = false
                        viewModel.newTab()
                    },
                    onSelectTab = { id ->
                        showTabs = false
                        viewModel.selectTab(id)
                    },
                    onCloseTab = { id ->
                        viewModel.closeTab(id)
                        savedWebViewStates.remove(id)
                    },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }

}

}
