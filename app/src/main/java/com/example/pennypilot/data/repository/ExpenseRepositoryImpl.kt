package com.example.pennypilot.data.repository

import com.example.pennypilot.data.model.Expense
import com.example.pennypilot.data.model.Result // Your Result class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseExpenseRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ExpenseRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val EXPENSES_SUBCOLLECTION = "expenses"
        private const val FIELD_DATE = "date"
    }

    private fun userExpensesCollection(userId: String) =
        firestore.collection(USERS_COLLECTION).document(userId).collection(EXPENSES_SUBCOLLECTION)

    override suspend fun addExpense(expense: Expense): Result<String> {
        if (expense.userId.isBlank()) {
            val errorMsg = "User ID is missing in the expense object."
            Timber.w(errorMsg)
            // Create a specific exception or a generic one for these validation errors
            return Result.Error(exception = IllegalArgumentException(errorMsg), message = errorMsg)
        }
        return try {
            val documentRef = if (expense.id.isNotBlank()) {
                userExpensesCollection(expense.userId).document(expense.id)
            } else {
                userExpensesCollection(expense.userId).document()
            }
            val expenseToSave = expense.copy(id = documentRef.id)

            documentRef.set(expenseToSave).await()
            Result.Success(documentRef.id)
        } catch (e: Exception) {
            Timber.e(e, "Error adding expense for user ${expense.userId}")
            Result.Error(exception = e, message = e.localizedMessage ?: "Failed to add expense.")
        }
    }

    override fun getExpensesForUserFlow(userId: String, limit: Int?): Flow<List<Expense>> = callbackFlow {
        if (userId.isBlank()) {
            Timber.w("User ID is blank in getExpensesForUserFlow. Emitting empty list.")
            trySend(emptyList()) // This is for the flow, not Result.Error
            // Closing the flow with an exception is the correct pattern here
            close(IllegalArgumentException("User ID cannot be blank for fetching expenses."))
            return@callbackFlow
        }

        Timber.d("Setting up Firestore listener for expenses, User: $userId, Limit: $limit")
        var query: Query = userExpensesCollection(userId)
            .orderBy(FIELD_DATE, Query.Direction.DESCENDING)

        if (limit != null && limit > 0) {
            query = query.limit(limit.toLong())
        }

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.w(error, "Expenses listener error for user $userId")
                close(error) // Close the flow with the Firestore error
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val expensesList = snapshot.toObjects<Expense>()
                Timber.d("Expenses updated via listener for user $userId: ${expensesList.size} items")
                trySend(expensesList).isSuccess
            } else {
                Timber.d("Expense snapshot is null for user $userId")
                trySend(emptyList()).isSuccess
            }
        }
        awaitClose {
            Timber.d("Closing expenses listener for user: $userId")
            listenerRegistration.remove()
        }
    }

    override suspend fun getAllExpensesOnce(userId: String): Result<List<Expense>> {
        if (userId.isBlank()) {
            val errorMsg = "User ID cannot be blank."
            Timber.w(errorMsg)
            return Result.Error(exception = IllegalArgumentException(errorMsg), message = errorMsg)
        }
        return try {
            val snapshot = userExpensesCollection(userId)
                .orderBy(FIELD_DATE, Query.Direction.DESCENDING)
                .get()
                .await()
            Result.Success(snapshot.toObjects<Expense>())
        } catch (e: Exception) {
            Timber.e(e, "Error getting all expenses once for user $userId")
            Result.Error(exception = e, message = e.localizedMessage ?: "Failed to retrieve expenses.")
        }
    }

    override suspend fun getExpenseById(userId: String, expenseId: String): Result<Expense?> {
        if (userId.isBlank()) {
            val errorMsg = "User ID cannot be blank."
            Timber.w(errorMsg)
            return Result.Error(exception = IllegalArgumentException(errorMsg), message = errorMsg)
        }
        if (expenseId.isBlank()) {
            val errorMsg = "Expense ID cannot be blank."
            Timber.w(errorMsg)
            return Result.Error(exception = IllegalArgumentException(errorMsg), message = errorMsg)
        }
        return try {
            val documentSnapshot = userExpensesCollection(userId).document(expenseId).get().await()
            if (documentSnapshot.exists()) {
                Result.Success(documentSnapshot.toObject<Expense>())
            } else {
                Result.Success(null) // Expense not found
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting expense by ID $expenseId for user $userId")
            Result.Error(exception = e, message = e.localizedMessage ?: "Failed to retrieve expense details.")
        }
    }

    override suspend fun updateExpense(expense: Expense): Result<Unit> {
        if (expense.userId.isBlank()) {
            val errorMsg = "User ID is missing in expense object."
            Timber.w(errorMsg)
            return Result.Error(exception = IllegalArgumentException(errorMsg), message = errorMsg)
        }
        if (expense.id.isBlank()) {
            val errorMsg = "Expense ID is missing for update."
            Timber.w(errorMsg)
            return Result.Error(exception = IllegalArgumentException(errorMsg), message = errorMsg)
        }
        return try {
            userExpensesCollection(expense.userId).document(expense.id)
                .set(expense)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating expense ${expense.id} for user ${expense.userId}")
            Result.Error(exception = e, message = e.localizedMessage ?: "Failed to update expense.")
        }
    }

    override suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit> {
        if (userId.isBlank()) {
            val errorMsg = "User ID cannot be blank."
            Timber.w(errorMsg)
            return Result.Error(exception = IllegalArgumentException(errorMsg), message = errorMsg)
        }
        if (expenseId.isBlank()) {
            val errorMsg = "Expense ID cannot be blank for deletion."
            Timber.w(errorMsg)
            return Result.Error(exception = IllegalArgumentException(errorMsg), message = errorMsg)
        }
        return try {
            userExpensesCollection(userId).document(expenseId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting expense $expenseId for user $userId")
            Result.Error(exception = e, message = e.localizedMessage ?: "Failed to delete expense.")
        }
    }

    // --- START OF FIX ---

    // This function was causing the crash. It now calls the correct existing logic.
    @Deprecated(
        "This function is redundant. Use getAllExpensesOnce(userId) instead.",
        ReplaceWith("getAllExpensesOnce(userId)")
    )
    override suspend fun getExpensesForUser(userId: String): Result<List<Expense>> {
        return getAllExpensesOnce(userId)
    }

    // This function was also unimplemented. It now gets expenses for the currently logged-in user.
    override suspend fun getExpenses(): Result<List<Expense>> {
        val currentUserId = auth.currentUser?.uid
        // First, check if a user is authenticated.
        if (currentUserId == null) {
            val errorMsg = "User not authenticated for getExpenses."
            Timber.w(errorMsg)
            return Result.Error(exception = IllegalStateException(errorMsg), message = errorMsg)
        }
        // If authenticated, call the function that fetches all expenses.
        return getAllExpensesOnce(currentUserId)
    }

    // --- END OF FIX ---
}
