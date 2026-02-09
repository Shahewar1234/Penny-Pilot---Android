package com.example.pennypilot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users") // Or your table name
data class UserEntity(
    @PrimaryKey val userId: String, // Could be the email if it's guaranteed unique
    val email: String,
    val password: String // In a real app, hash this password!
    // val name: String? = null // Optional name field
)