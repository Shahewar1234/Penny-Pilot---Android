package com.example.pennypilot.data.model // Or your preferred model package path

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Currency
import java.util.Date
import java.util.Locale

// This class will be directly mapped to a document in your "users" collection in Firestore.
data class User(
    // It's good practice to ensure property names match Firestore field names
    // or use @PropertyName if they differ. For simplicity, we'll aim for matching names.

    @get:PropertyName("uid") @set:PropertyName("uid") var uid: String = "",
    @get:PropertyName("email") @set:PropertyName("email") var email: String = "", // Assuming email is non-null after auth
    @get:PropertyName("displayName") @set:PropertyName("displayName") var displayName: String = "",
    @get:PropertyName("profileImageUrl") @set:PropertyName("profileImageUrl") var profileImageUrl: String? = null,

    @ServerTimestamp // Firestore will automatically populate this on document creation
    @get:PropertyName("registrationDate") @set:PropertyName("registrationDate") var registrationDate: Date? = null,

    @ServerTimestamp // Firestore can update this on specific operations if configured
    @get:PropertyName("lastLoginDate") @set:PropertyName("lastLoginDate") var lastLoginDate: Date? = null,

    // Renamed for clarity and to store the user's active choice
    @get:PropertyName("selectedCurrencyCode") @set:PropertyName("selectedCurrencyCode") var selectedCurrencyCode: String = "USD", // e.g., "USD", "EUR", "INR" - set during signup

    // Added for dynamic balance
    @get:PropertyName("currentBalance") @set:PropertyName("currentBalance") var currentBalance: Double = 0.0,

    @get:PropertyName("dateFormatPreference") @set:PropertyName("dateFormatPreference") var dateFormatPreference: String = "dd/MM/yyyy",

    // For nested objects, ensure NotificationPreferences is also Firestore-compatible
    @get:PropertyName("notificationPreferences") @set:PropertyName("notificationPreferences") var notificationPreferences: NotificationPreferences = NotificationPreferences(),

    @get:PropertyName("themePreference") @set:PropertyName("themePreference") var themePreference: String = "system", // e.g., "light", "dark", "system"
    @get:PropertyName("weeklyReportEnabled") @set:PropertyName("weeklyReportEnabled") var weeklyReportEnabled: Boolean = true
    // ... other fields from your comprehensive list can be added here
) {
    // --- START OF ADDED CODE ---

    /**
     * Returns the currency symbol for the user's selected currency code.
     * Defaults to "$" if the code is invalid or not found.
     */
    fun getCurrencySymbol(): String {
        return try {
            // Attempt to get the currency instance for the given code
            Currency.getInstance(selectedCurrencyCode).getSymbol(Locale.getDefault())
        } catch (e: Exception) {
            // Fallback to "$" if the code is invalid or any other error occurs
            "$"
        }
    }

    // --- END OF ADDED CODE ---

    // A public no-argument constructor is required by Firestore for deserialization.
    // Kotlin data classes with default values for all properties often provide this implicitly.
    // If you encounter issues, you can explicitly define it:
    constructor() : this(
        uid = "",
        email = "",
        displayName = "",
        profileImageUrl = null,
        registrationDate = null,
        lastLoginDate = null,
        selectedCurrencyCode = "USD", // Default
        currentBalance = 0.0,
        dateFormatPreference = "dd/MM/yyyy",
        notificationPreferences = NotificationPreferences(),
        themePreference = "system",
        weeklyReportEnabled = true,

    )
}

// This class will be stored as a nested object (a "map") within the User document in Firestore.
data class NotificationPreferences(
    @get:PropertyName("expenseReminders") @set:PropertyName("expenseReminders") var expenseReminders: Boolean = true,
    @get:PropertyName("budgetAlerts") @set:PropertyName("budgetAlerts") var budgetAlerts: Boolean = true,
    @get:PropertyName("newsAndUpdatePromotions") @set:PropertyName("newsAndUpdatePromotions") var newsAndUpdatePromotions: Boolean = false
) {
    // No-argument constructor for Firestore
    constructor() : this(true, true, false)
}
