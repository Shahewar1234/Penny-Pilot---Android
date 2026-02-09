// In ui/theme/Color.kt
package com.example.pennypilot.ui.theme

import androidx.compose.ui.graphics.Color

// Your Specific App/Home Screen Palette
val AppBackgroundStart = Color(0xFF004D2B) // Was HomeMutedGreen
val AppBackgroundEnd = Color(0xFF001A0E)   // Was HomeBackground
val AppPrimaryText = Color.White          // Was HomePrimaryText
val AppSecondaryText = Color.LightGray    // Was HomeSecondaryText
val AppAccentColor = Color(0xFF61BC79)    // Was HomeAccentGreen
val AppMutedColor = Color(0xFF004D2B)     // Was HomeMutedGreen
val AppCardBackground = Color.White.copy(alpha = 0.08f) // Was HomeCardBg

// You can still have your Material 3 semantic colors for components
// that might not fit the "Home" screen's specific styling,
// or for a more standard fallback.
val PennyPilotPrimary = AppAccentColor // e.g., Green as primary
val PennyPilotOnPrimary = Color.Black // Or White, depending on the Primary
val PennyPilotSecondary = AppMutedColor // e.g., Muted Green as secondary
val PennyPilotOnSecondary = Color.White
val PennyPilotBackground = AppBackgroundEnd // Using one of your dark backgrounds
val PennyPilotOnBackground = AppPrimaryText
val PennyPilotSurface = Color.Transparent // If most surfaces are on the gradient
val PennyPilotOnSurface = AppPrimaryText
// ... and so on for other Material color roles (surfaceVariant, error, etc.)