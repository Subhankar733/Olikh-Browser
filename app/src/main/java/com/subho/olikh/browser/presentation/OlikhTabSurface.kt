package com.subho.olikh.browser.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
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

@Composable
fun OlikhTabSurface(
    tabs: List<BrowserTab>,
    activeTabId: String,
    onNewTab: () -> Unit,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.SpaceLg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimensions.SpaceLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Your tabs",
                    color = Colors.TextPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(Dimensions.SpaceXs))

                Text(
                    text = "${tabs.size.coerceAtLeast(1)} active",
                    color = Colors.TextTertiary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }

            Surface(
                onClick = onNewTab,
                modifier = Modifier.size(Dimensions.TouchTarget),
                shape = CircleShape,
                color = Colors.Aurora
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "New tab",
                        tint = Colors.TextPrimary,
                        modifier = Modifier.size(Dimensions.IconMedium)
                    )
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpaceMd)
        ) {
            items(
                items = tabs,
                key = { it.id }
            ) { tab ->
                val active = tab.id == activeTabId

                OlikhTabCard(
                    tab = tab,
                    active = active,
                    onSelect = { onSelectTab(tab.id) },
                    onClose = { onCloseTab(tab.id) }
                )
            }
        }
    }
}

@Composable
private fun OlikhTabCard(
    tab: BrowserTab,
    active: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    val cardShape = Shapes.Card

    Surface(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        color = Colors.Transparent,
        shadowElevation = if (active) {
            OlikhDesignSystem.Elevation.Medium
        } else {
            OlikhDesignSystem.Elevation.Low
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(
                    if (active) {
                        Brush.horizontalGradient(
                            listOf(
                                Colors.AuroraDeep.copy(alpha = 0.34f),
                                Colors.SurfaceElevated,
                                Colors.Surface
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Colors.SurfaceElevated,
                                Colors.Surface
                            )
                        )
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Dimensions.SpaceLg,
                        top = Dimensions.SpaceLg,
                        end = Dimensions.SpaceSm,
                        bottom = Dimensions.SpaceLg
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) Colors.AuroraBright
                                else Colors.TextDisabled
                            )
                    )

                    Spacer(modifier = Modifier.size(Dimensions.SpaceMd))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = tab.title.ifBlank { "New tab" },
                            color = Colors.TextPrimary,
                            maxLines = 1,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = tab.url,
                            color = Colors.TextTertiary,
                            maxLines = 1,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(Dimensions.CompactTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close tab",
                            tint = if (active) {
                                Colors.TextPrimary
                            } else {
                                Colors.TextTertiary
                            },
                            modifier = Modifier.size(Dimensions.IconSmall)
                        )
                    }
                }
            }
        }
    }
}
