package com.example.pennypilot.data.repository

import com.example.pennypilot.data.model.Expense
import com.example.pennypilot.data.model.Result // Your Result class
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {

    /**
     * Adds a new expense to Firestore for the given user.
     * The expense.id will be populated by Firestore if initially empty.
     * The expense.date will be set by Firestore if initially null and @ServerTimestamp is used.
     * @param expense The Expense object to add. userId should be pre-filled.
     * @return Result<String> containing the ID of the newly created expense document on success.
     */
    suspend fun addExpense(expense: Expense): Result<String>

    /**
     * Retrieves a real-time Flow of expenses for a specific user.
     * @param userId The ID of the user whose expenses are to be fetched.
     * @param limit Optional limit on the number of recent expenses to fetch.
     * @return Flow<List<Expense>> emitting the list of expenses.
     */
    fun getExpensesForUserFlow(userId: String, limit: Int? = null): Flow<List<Expense>>

    /**
     * Retrieves a list of all expenses for a user once (not real-time).
     * @param userId The ID of the user.
     * @return Result<List<Expense>>
     */
    suspend fun getAllExpensesOnce(userId: String): Result<List<Expense>> // Renamed from getExpenses

    /**
     * Retrieves a specific expense by its ID for a given user.
     * @param userId The ID of the user who owns the expense.
     * @param expenseId The ID of the expense document.
     * @return Result<Expense?>
     */
    suspend fun getExpenseById(userId: String, expenseId: String): Result<Expense?>

    /**
     * Updates an existing expense in Firestore.
     * @param expense The Expense object with updated details. Must have a valid id and userId.
     * @return Result<Unit>
     */
    suspend fun updateExpense(expense: Expense): Result<Unit>

    /**
     * Deletes an expense from Firestore.
     * @param userId The ID of the user who owns the expense.
     * @param expenseId The ID of the expense document to delete.
     * @return Result<Unit>
     */
    suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit>
    @Deprecated(
        "Use getAllExpensesOnce(userId) instead.",
        ReplaceWith("getAllExpensesOnce(userId)")
    )
    suspend fun getExpensesForUser(userId: String): Result<List<Expense>>

    // This is the version without a userId, for fetching the current user's expenses.
    suspend fun getExpenses(): Result<List<Expense>>


}


