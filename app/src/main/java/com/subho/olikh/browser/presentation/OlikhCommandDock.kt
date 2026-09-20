package com.subho.olikh.browser.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tab
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.subho.olikh.browser.presentation.OlikhDesignSystem.Colors
import com.subho.olikh.browser.presentation.OlikhDesignSystem.Dimensions
import com.subho.olikh.browser.presentation.OlikhDesignSystem.Shapes

/**
 * Olikh Browser — contextual command dock.
 *
 * Kept independent from MainActivity so the browser chrome
 * can evolve without coupling navigation logic to the UI.
 */
@Composable
fun OlikhCommandDock(
    canGoBack: Boolean,
    canGoForward: Boolean,
    tabCount: Int,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onRefresh: () -> Unit,
    onTabs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = Shapes.Dock,
        color = Colors.GlassStrong,
        shadowElevation = OlikhDesignSystem.Elevation.High,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = Dimensions.SpaceXs,
                vertical = Dimensions.SpaceXs
            ),
            horizontalArrangement = Arrangement.spacedBy(
                Dimensions.SpaceXs,
                Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockButton(
                enabled = canGoBack,
                contentDescription = "Back",
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.IconMedium)
                )
            }

            DockButton(
                enabled = canGoForward,
                contentDescription = "Forward",
                onClick = onForward
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.IconMedium)
                )
            }

            DockButton(
                enabled = true,
                contentDescription = "Refresh",
                onClick = onRefresh
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.IconMedium)
                )
            }

            Surface(
                onClick = onTabs,
                shape = Shapes.Pill,
                color = Colors.SurfaceBright,
                modifier = Modifier.size(Dimensions.TouchTarget)
            ) {
                androidx.compose.foundation.layout.Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tab,
                        contentDescription = "Tabs",
                        tint = Colors.TextPrimary,
                        modifier = Modifier.size(Dimensions.IconMedium)
                    )

                    Text(
                        text = tabCount.coerceAtLeast(1).toString(),
                        color = Colors.TextPrimary,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 7.dp, bottom = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DockButton(
    enabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(Dimensions.TouchTarget)
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides
                if (enabled) Colors.TextPrimary else Colors.TextDisabled
        ) {
            content()
        }
    }
}
