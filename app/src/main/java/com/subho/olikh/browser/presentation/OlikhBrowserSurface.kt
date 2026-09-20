package com.subho.olikh.browser.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.subho.olikh.browser.presentation.OlikhDesignSystem.Colors
import com.subho.olikh.browser.presentation.OlikhDesignSystem.Dimensions
import com.subho.olikh.browser.presentation.OlikhDesignSystem.Shapes

/**
 * Olikh Browser — adaptive browser surface.
 *
 * Web content remains the primary visual layer.
 * Browser chrome floats above it and can later become
 * contextual, gesture-driven and state-aware.
 */
@Composable
fun OlikhBrowserSurface(
    modifier: Modifier = Modifier,
    title: String = "",
    address: String = "",
    isSecure: Boolean = false,
    isChromeVisible: Boolean = true,
    tabCount: Int = 1,
    onSurfaceClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Colors.Void)
    ) {

        // Primary web/content layer.
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }

        // Subtle edge illumination gives the surface depth
        // without covering the actual web content.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Colors.Void.copy(alpha = 0.28f),
                            Colors.Transparent
                        )
                    )
                )
        )

        AnimatedVisibility(
            visible = isChromeVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            BrowserCommandSurface(
                title = title,
                address = address,
                isSecure = isSecure,
                tabCount = tabCount,
                onClick = onSurfaceClick,
                onMenuClick = onMenuClick
            )
        }
    }
}

@Composable
private fun BrowserCommandSurface(
    title: String,
    address: String,
    isSecure: Boolean,
    tabCount: Int,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimensions.ChromeHorizontal,
                vertical = Dimensions.ChromeVertical
            ),
        shape = Shapes.Pill,
        color = Colors.Glass,
        tonalElevation = 0.dp,
        shadowElevation = OlikhDesignSystem.Elevation.Medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Dimensions.SpaceSm,
                    end = Dimensions.SpaceXs
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier
                    .padding(start = Dimensions.SpaceXs)
                    .size(34.dp),
                shape = CircleShape,
                color = if (isSecure) {
                    Colors.Secure.copy(alpha = 0.16f)
                } else {
                    Colors.Aurora.copy(alpha = 0.14f)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search or address",
                        tint = if (isSecure) {
                            Colors.Secure
                        } else {
                            Colors.AuroraBright
                        },
                        modifier = Modifier.size(Dimensions.IconSmall)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        horizontal = Dimensions.SpaceMd,
                        vertical = Dimensions.SpaceSm
                    ),
                verticalArrangement = Arrangement.Center
            ) {
                if (title.isNotBlank()) {
                    Text(
                        text = title,
                        color = Colors.TextPrimary,
                        maxLines = 1
                    )
                }

                if (address.isNotBlank()) {
                    Text(
                        text = address,
                        color = Colors.TextTertiary,
                        maxLines = 1
                    )
                } else if (title.isBlank()) {
                    Text(
                        text = "Search or enter address",
                        color = Colors.TextSecondary,
                        maxLines = 1
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .padding(end = Dimensions.SpaceXs)
                    .size(34.dp),
                shape = CircleShape,
                color = Colors.SurfaceBright
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = tabCount.coerceAtLeast(1).toString(),
                        color = Colors.TextPrimary
                    )
                }
            }

            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(Dimensions.TouchTarget)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "Browser menu",
                    tint = Colors.TextPrimary
                )
            }
        }
    }
}
