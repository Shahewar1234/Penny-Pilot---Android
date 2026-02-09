package com.example.pennypilot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pennypilot.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Or .ABORT / .IGNORE depending on desired behavior
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1") // Or your table name
    suspend fun getUserByEmail(email: String): UserEntity?

    // Your existing login query might be by userId/email and password
    @Query("SELECT * FROM users WHERE email = :emailOrPhone AND password = :password LIMIT 1")
    suspend fun loginUser(emailOrPhone: String, password: String): UserEntity?
}