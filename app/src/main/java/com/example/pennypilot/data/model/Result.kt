// In app/src/main/java/com/example/pennypilot/data/model/Result.kt
package com.example.pennypilot.data.model

import java.lang.Exception // It's good practice to add the import

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    // Recommended new implementation
    data class Error(val exception: Exception, val message: String) : Result<Nothing>()

    object Loading : Result<Nothing>()
}
