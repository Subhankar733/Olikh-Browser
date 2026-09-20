#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

TARGET="app/src/main/java/com/subho/olikh/browser/presentation/OlikhDesignSystem.kt"

mkdir -p "$(dirname "$TARGET")"

cat > "$TARGET" <<'KOTLIN'
package com.subho.olikh.browser.presentation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object OlikhDesignSystem {

    object Colors {
        val Void = Color(0xFF06070B)
        val Obsidian = Color(0xFF0A0C12)
        val Surface = Color(0xFF10131A)
        val SurfaceElevated = Color(0xFF151923)
        val SurfaceBright = Color(0xFF1B202C)

        val Glass = Color(0xCC11151E)
        val GlassStrong = Color(0xF2141822)
        val GlassSoft = Color(0x88171C27)
        val Divider = Color(0x261F2430)

        val Aurora = Color(0xFF8B7CFF)
        val AuroraBright = Color(0xFFA99FFF)
        val AuroraDeep = Color(0xFF6255E8)

        val Cyan = Color(0xFF66D9FF)
        val CyanBright = Color(0xFF9AE7FF)

        val Secure = Color(0xFF65D6A0)
        val Warning = Color(0xFFFFC857)
        val Danger = Color(0xFFFF6B7A)

        val TextPrimary = Color(0xFFF4F5FA)
        val TextSecondary = Color(0xFFB4BAC9)
        val TextTertiary = Color(0xFF737B8F)
        val TextDisabled = Color(0xFF4E5566)

        val Transparent = Color.Transparent
    }

    object Dimensions {
        val ChromeHorizontal = 12.dp
        val ChromeVertical = 8.dp

        val OmniboxHeight = 52.dp
        val OmniboxRadius = 18.dp
        val OmniboxHorizontalPadding = 16.dp

        val DockHeight = 64.dp
        val DockRadius = 22.dp

        val TouchTarget = 48.dp
        val CompactTouchTarget = 40.dp

        val IconSmall = 18.dp
        val IconMedium = 22.dp
        val IconLarge = 26.dp

        val SpaceXs = 4.dp
        val SpaceSm = 8.dp
        val SpaceMd = 12.dp
        val SpaceLg = 16.dp
        val SpaceXl = 20.dp
        val SpaceXxl = 24.dp
    }

    object Shapes {
        val Omnibox = RoundedCornerShape(Dimensions.OmniboxRadius)
        val Dock = RoundedCornerShape(Dimensions.DockRadius)
        val Card = RoundedCornerShape(20.dp)

        val Sheet = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )

        val Pill = RoundedCornerShape(50)
        val Small = RoundedCornerShape(12.dp)
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
KOTLIN

chmod +x create_olikh_design_system.sh
echo "✅ Script created: create_olikh_design_system.sh"
echo "✅ Target created: $TARGET"
