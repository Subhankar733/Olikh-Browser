import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.content.Intent
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.compose.material.icons.outlined.Share
package com.subho.olikh.browser

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
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
private val DeepNavy = Color(0xFF0F1522)
private val Glass = Color(0xFF141C2B)
private val Border = Color(0xFF273249)
private val Ice = Color(0xFFF4F7FB)
private val Slate = Color(0xFF8D99AE)
private val Sapphire = Color(0xFF4F7CFF)
private val Electric = Color(0xFF79A7FF)

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
    var isDesktopMode by rememberSaveable { mutableStateOf(false) }
    var isWebDark by rememberSaveable { mutableStateOf(false) }
    val savedWebViewStates = remember { mutableStateMapOf<String, Bundle>() }
    val activeTab = uiState.tabs.first { it.id == uiState.activeTabId }
    BackHandler(enabled = showTabs || uiState.canGoBack) {
        if (showTabs) showTabs = false else if (webView?.canGoBack() == true) webView?.goBack()
    }

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
        key(activeTab.id) {
            var swipeRefreshRef by remember { mutableStateOf<SwipeRefreshLayout?>(null) }
            LaunchedEffect(uiState.isLoading) {
                swipeRefreshRef?.isRefreshing = uiState.isLoading
            }
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val srl = SwipeRefreshLayout(ctx).apply {
                        setColorSchemeColors(0xFF2563EB.toInt())
                        setProgressBackgroundColorSchemeColor(0xFF0F1522.toInt())
                        setOnRefreshListener {
                            webView?.reload()
                        }
                    }
                    swipeRefreshRef = srl
                    val wv = 
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
                    val defaultUserAgent = settings.userAgentString
                    val desktopUserAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                    settings.userAgentString = if (isDesktopMode) desktopUserAgent else defaultUserAgent
                    settings.useWideViewPort = isDesktopMode
                    settings.loadWithOverviewMode = isDesktopMode
                    if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                        WebSettingsCompat.setForceDark(
                            settings,
                            if (isWebDark) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
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
                    srl.addView(this, android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    ))
                    srl
                }
            },
            update = { view -> webView = view },
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
                isDesktopMode = isDesktopMode,
                isWebDark = isWebDark,
                onClearData = {
                    webView?.clearCache(true)
                    webView?.clearHistory()
                    WebStorage.getInstance().deleteAllData()
                    CookieManager.getInstance().removeAllCookies(null)
                    Toast.makeText(context, "Browsing data cleared", Toast.LENGTH_SHORT).show()
                    webView?.reload()
                },
                onToggleWebDark = {
                    isWebDark = !isWebDark
                    webView?.settings?.let { s ->
                        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                            WebSettingsCompat.setForceDark(
                                s,
                                if (isWebDark) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
                            )
                        }
                    }
                    webView?.reload()
                },
                onNewTab = { viewModel.openTab("https://www.google.com") },
                onShare = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, uiState.address)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Link"))
                },
                onToggleDesktopMode = {
                    isDesktopMode = !isDesktopMode
                    webView?.settings?.let { s ->
                        s.userAgentString = if (isDesktopMode) "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36" else null
                        s.useWideViewPort = isDesktopMode
                        s.loadWithOverviewMode = isDesktopMode
                    }
                    webView?.reload()
                }
            )
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
            .padding(horizontal = 12.dp, vertical = 10.dp),
        shape = RoundedCornerShape(24.dp),
        color = DeepNavy,
        tonalElevation = 0.dp,
        shadowElevation = 3.dp,
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
                                stringResource(R.string.search_or_enter_address),
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
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.clear), tint = Slate)
                }
            } else {
                IconButton(onClick = onSubmit) {
                    Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.search), tint = Ice)
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
    onClearData: () -> Unit,
    onToggleWebDark: () -> Unit,
    onNewTab: () -> Unit,
    onShare: () -> Unit,
    onToggleDesktopMode: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        shape = RoundedCornerShape(22.dp),
        color = DeepNavy.copy(alpha = 0.98f),
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
                    modifier = Modifier.background(DeepNavy).border(1.dp, Border, RoundedCornerShape(8.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("New tab", color = Ice, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Outlined.Add, contentDescription = null, tint = Ice) },
                        onClick = {
                            menuExpanded = false
                            onNewTab()
                        }
                    )
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
