package com.subho.olikh.browser

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Tab

private val Void = Color(0xFF090B10)
private val SurfaceDark = Color(0xFF11151D)
private val SurfaceRaised = Color(0xFF181E28)
private val Line = Color(0xFF242C38)
private val Ice = Color(0xFFE8EEF5)
private val Muted = Color(0xFF8E9AAA)
private val Cyan = Color(0xFF62E8F2)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OlikhTheme {
                BrowserShell()
            }
        }
    }
}

@Composable
private fun OlikhTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = Void,
            surface = SurfaceDark,
            onSurface = Ice,
            primary = Cyan
        ),
        content = content
    )
}

@Composable
private fun BrowserShell() {
    val context = LocalContext.current

    var address by remember { mutableStateOf("") }
    var pressed by remember { mutableStateOf(false) }

    val buttonScale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonScale"
    )

    val webView = remember {
        WebView(context).apply {
            webViewClient = WebViewClient()

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false

            loadUrl("https://www.google.com")
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Void)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { webView }
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceRaised,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Line
                    )
                ) {
                    TextField(
                        value = address,
                        onValueChange = { address = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(
                                "Search or enter address",
                                color = Muted,
                                fontSize = 14.sp
                            )
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Ice,
                            fontSize = 15.sp
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.padding(4.dp))

                IconButton(
                    onClick = {
                        pressed = !pressed
                    },
                    modifier = Modifier.scale(buttonScale)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tab,
                        contentDescription = "Tabs",
                        tint = Ice
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                shape = RoundedCornerShape(22.dp),
                color = SurfaceDark,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Line
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = { webView.goBack() }
                    ) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Ice
                        )
                    }

                    IconButton(
                        onClick = { webView.goForward() }
                    ) {
                        Icon(
                            Icons.Outlined.ArrowForward,
                            contentDescription = "Forward",
                            tint = Ice
                        )
                    }

                    IconButton(
                        onClick = { webView.reload() }
                    ) {
                        Text(
                            "↻",
                            color = Ice,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Light
                        )
                    }

                    IconButton(
                        onClick = { }
                    ) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = "New tab",
                            tint = Ice
                        )
                    }

                    IconButton(
                        onClick = { }
                    ) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = "Menu",
                            tint = Ice
                        )
                    }
                }
            }
        }
    }
}
