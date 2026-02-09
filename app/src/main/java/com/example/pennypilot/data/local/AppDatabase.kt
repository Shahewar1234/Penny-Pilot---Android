package com.example.pennypilot.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pennypilot.data.local.dao.ExpenseDao
import com.example.pennypilot.data.local.dao.UserDao
import com.example.pennypilot.data.local.entity.ExpenseEntity
import com.example.pennypilot.data.local.entity.UserEntity

@Database(entities = [ExpenseEntity::class, UserEntity::class],version = 3)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun userDao(): UserDao
}
