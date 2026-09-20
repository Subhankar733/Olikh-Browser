package com.subho.olikh.browser.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Olikh Browser — visual foundation.
 *
 * This is intentionally independent from the existing screen UI.
 * Components will consume this system incrementally during Phase 2.
 */
object OlikhDesignSystem {

    object Colors {

        // Core surfaces
        val Void = Color(0xFF06070B)
        val Obsidian = Color(0xFF0A0C12)
        val Surface = Color(0xFF10131A)
        val SurfaceElevated = Color(0xFF151923)
        val SurfaceBright = Color(0xFF1B202C)

        // Glass / overlays
        val Glass = Color(0xCC11151E)
        val GlassStrong = Color(0xF2141822)
        val GlassSoft = Color(0x88171C27)
        val Divider = Color(0x261F2430)

        // Primary identity
        val Aurora = Color(0xFF8B7CFF)
        val AuroraBright = Color(0xFFA99FFF)
        val AuroraDeep = Color(0xFF6255E8)

        // Secondary accent
        val Cyan = Color(0xFF66D9FF)
        val CyanBright = Color(0xFF9AE7FF)

        // Security / status
        val Secure = Color(0xFF65D6A0)
        val Warning = Color(0xFFFFC857)
        val Danger = Color(0xFFFF6B7A)

        // Content
        val TextPrimary = Color(0xFFF4F5FA)
        val TextSecondary = Color(0xFFB4BAC9)
        val TextTertiary = Color(0xFF737B8F)
        val TextDisabled = Color(0xFF4E5566)

        // Transparent utility colors
        val Transparent = Color.Transparent
    }

    object Dimensions {

        // Browser chrome
        val ChromeHorizontal = 12.dp
        val ChromeVertical = 8.dp

        // Omnibox
        val OmniboxHeight = 52.dp
        val OmniboxRadius = 18.dp
        val OmniboxHorizontalPadding = 16.dp

        // Bottom navigation / command dock
        val DockHeight = 64.dp
        val DockRadius = 22.dp

        // Touch targets
        val TouchTarget = 48.dp
        val CompactTouchTarget = 40.dp

        // Icons
        val IconSmall = 18.dp
        val IconMedium = 22.dp
        val IconLarge = 26.dp

        // Spacing
        val SpaceXs = 4.dp
        val SpaceSm = 8.dp
        val SpaceMd = 12.dp
        val SpaceLg = 16.dp
        val SpaceXl = 20.dp
        val SpaceXxl = 24.dp
    }

    object Shapes {

        val Omnibox = androidx.compose.foundation.shape.RoundedCornerShape(
            Dimensions.OmniboxRadius
        )

        val Dock = androidx.compose.foundation.shape.RoundedCornerShape(
            Dimensions.DockRadius
        )

        val Card = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)

        val Sheet = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )

        val Pill = androidx.compose.foundation.shape.RoundedCornerShape(50)

        val Small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    }

    object Alpha {
        const val Glass = 0.82f
        const val GlassSoft = 0.54f
        const val Divider = 0.15f
        const val Pressed = 0.12f
        const val Disabled = 0.38f
    }

    object Elevation {
        val None: Dp = 0.dp
        val Low: Dp = 2.dp
        val Medium: Dp = 6.dp
        val High: Dp = 12.dp
    }
}
