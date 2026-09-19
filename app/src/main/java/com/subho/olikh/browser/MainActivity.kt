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
                                .background(Brush.horizontalGradient(listOf(Sapphire, Electric)))
                        )
                    }
                }
            }

            BrowserControls(
                canGoBack = uiState.canGoBack,
                canGoForward = uiState.canGoForward,
                tabCount = uiState.tabs.size,
                onBack = { webView?.goBack() },
                onForward = { webView?.goForward() },
                onRefresh = { webView?.reload() },
                onTabs = { showTabs = true },
                isDesktopMode = browserSettings.desktopMode,
                isWebDark = browserSettings.webDarkMode,
                isAdBlockEnabled = browserSettings.adBlockEnabled,
                onToggleAdBlock = {
                    viewModel.setAdBlockEnabled(!browserSettings.adBlockEnabled)
                },
                onShowHistory = { showHistoryDialog = true },
                onAddBookmark = {
                    val currentUrl = webView?.url.orEmpty()
                    val currentTitle = webView?.title.orEmpty().ifBlank { currentUrl }

                    if (currentUrl.isBlank()) {
                        Toast.makeText(
                            context,
                            "Nothing to bookmark",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (bookmarks.any { it.url == currentUrl }) {
                        Toast.makeText(
                            context,
                            "Already bookmarked",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        viewModel.addBookmark(currentUrl, currentTitle)
                        Toast.makeText(
                            context,
                            "Bookmark added",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onShowBookmarks = { showBookmarksDialog = true },
                onClearData = {
                    webView?.clearCache(true)
                    webView?.clearHistory()
                    WebStorage.getInstance().deleteAllData()
                    CookieManager.getInstance().removeAllCookies(null)
                    viewModel.clearHistory()
                    Toast.makeText(
                        context,
                        "Browsing data cleared",
                        Toast.LENGTH_SHORT
                    ).show()
                    webView?.reload()
                },
                onToggleWebDark = {
                    viewModel.setWebDarkMode(!browserSettings.webDarkMode)
                },
                onShare = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, uiState.address)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Link"))
                },
                onToggleDesktopMode = {
                    viewModel.setDesktopMode(!browserSettings.desktopMode)
                }
            )
        }
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
                TabsSheet(
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
                    }
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
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(26.dp),
        color = DeepNavy.copy(alpha = 0.95f),
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, Border.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isHttps = value.startsWith("https://", ignoreCase = true)
            Icon(
                imageVector = if (isHttps) Icons.Outlined.Lock else Icons.Outlined.Search,
                contentDescription = null,
                tint = if (isHttps) Sapphire else Slate,
                modifier = Modifier.size(18.dp)
            )

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                singleLine = true,
                textStyle = TextStyle(color = Ice, fontSize = 14.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { onSubmit() }),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                stringResource(R.string.search_or_enter_address),
                                color = Slate,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (value.isNotEmpty()) {
                IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.clear), tint = Slate, modifier = Modifier.size(16.dp))
                }
            } else {
                IconButton(onClick = onSubmit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.search), tint = Ice, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun BrowserControls(
    canGoBack: Boolean,
    canGoForward: Boolean,
    tabCount: Int,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onRefresh: () -> Unit,
    onTabs: () -> Unit,
    isDesktopMode: Boolean,
    isWebDark: Boolean,
    isAdBlockEnabled: Boolean,
    onToggleAdBlock: () -> Unit,
    onShowHistory: () -> Unit,
    onAddBookmark: () -> Unit,
    onShowBookmarks: () -> Unit,
    onClearData: () -> Unit,
    onToggleWebDark: () -> Unit,
    onShare: () -> Unit,
    onToggleDesktopMode: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = DeepNavy.copy(alpha = 0.95f),
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, Border.copy(alpha = 0.7f))
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
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = if (canGoBack) Ice else Slate
                )
            }
            IconButton(onClick = onForward, enabled = canGoForward) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = stringResource(R.string.forward),
                    tint = if (canGoForward) Ice else Slate
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.refresh), tint = Ice)
            }
            IconButton(onClick = onTabs) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Tab, contentDescription = stringResource(R.string.tabs), tint = Ice)
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd),
                        shape = RoundedCornerShape(5.dp),
                        color = Sapphire
                    ) {
                        Text(
                            text = tabCount.toString(),
                            color = Ice,
                            fontSize = 8.sp,
                            lineHeight = 9.sp,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            var menuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Menu", tint = Ice)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(DeepNavy)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Border.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("Share", color = Ice, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null, tint = Ice) },
                        onClick = {
                            menuExpanded = false
                            onShare()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Dark web content", color = Ice, fontSize = 14.sp) },
                        trailingIcon = {
                            Checkbox(
                                checked = isWebDark,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Sapphire,
                                    checkmarkColor = Ice,
                                    uncheckedColor = Slate
                                )
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleWebDark()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Clear data", color = Ice, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Outlined.Close, contentDescription = null, tint = Ice) },
                        onClick = {
                            menuExpanded = false
                            onClearData()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Block ads", color = Ice, fontSize = 14.sp) },
                        trailingIcon = {
                            Checkbox(
                                checked = isAdBlockEnabled,
                                onCheckedChange = null
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleAdBlock()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Desktop site", color = Ice, fontSize = 14.sp) },
                        trailingIcon = {
                            Checkbox(
                                checked = isDesktopMode,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Sapphire,
                                    checkmarkColor = Ice,
                                    uncheckedColor = Slate
                                )
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleDesktopMode()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabsSheet(
    tabs: List<BrowserTab>,
    activeTabId: String,
    onNewTab: () -> Unit,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.tabs),
                style = MaterialTheme.typography.headlineSmall,
                color = Ice,
                modifier = Modifier.weight(1f)
            )
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = Sapphire,
                shadowElevation = 7.dp,
                onClick = onNewTab
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.new_tab),
                        tint = Ice,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(390.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tabs, key = { it.id }) { tab ->
                val active = tab.id == activeTabId
                val tabShape = RoundedCornerShape(18.dp)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(tabShape)
                        .background(
                            if (active) {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF1B2D4D), Color(0xFF101827))
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(DeepNavy, DeepNavy)
                                )
                            }
                        )
                        .border(
                            width = if (active) 1.5.dp else 1.dp,
                            color = if (active) Sapphire else Border,
                            shape = tabShape
                        ),
                    shape = tabShape,
                    color = Color.Transparent,
                    onClick = { onSelectTab(tab.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 5.dp, top = 13.dp, bottom = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(if (active) Electric else Border)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tab.title.ifBlank { stringResource(R.string.new_tab) },
                                color = Ice,
                                maxLines = 1,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = tab.url,
                                color = Slate,
                                maxLines = 1,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { onCloseTab(tab.id) }) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.close_tab),
                                tint = if (active) Ice else Slate
                            )
                        }
                    }
                }
            }
        }
    }
}
