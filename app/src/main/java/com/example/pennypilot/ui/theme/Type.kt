package com.example.pennypilot.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles using the default system font
val Typography = Typography(

    // Display Styles: Typically for very large, short, important text or numbers.
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // Can be FontWeight.Bold for more impact
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headline Styles: For high-emphasis text, shorter than display styles.
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // Can be FontWeight.SemiBold or Medium
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle( // Suitable for screen titles in TopAppBars
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Giving headlines a bit more emphasis
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // Corrected: Was 'FontWeight.Normal, // Can be \n FontWeight.Medium'
        // You can change this to FontWeight.Medium if desired, for example:
        // fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title Styles: For medium-emphasis text, shorter than headlines. Often used for sub-sections.
    titleLarge = TextStyle( // Suitable for prominent text like the Amount field
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Make titles distinct
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle( // Suitable for section titles like "Expense Details"
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp // Slightly increased letter spacing for readability
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body Styles: The main text content of your application.
    bodyLarge = TextStyle( // Default body text
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp // Default Material spacing
    ),
    bodyMedium = TextStyle( // For slightly less prominent body text or secondary information
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle( // For fine print or less important details
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label Styles: For smaller text used in components like buttons, captions, overline text.
    labelLarge = TextStyle( // Good for button text
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Buttons often use medium weight for clarity
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp // Added missing letterSpacing
    ),
    labelMedium = TextStyle( // For smaller labels or descriptive text next to icons
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle( // For very small text, like captions or overlines
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
