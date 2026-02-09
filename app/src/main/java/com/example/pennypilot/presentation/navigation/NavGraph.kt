package com.example.pennypilot.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pennypilot.presentation.screens.ai.AiAssistantScreen
import com.example.pennypilot.presentation.screens.analytics.AnalyticsScreen
import com.example.pennypilot.presentation.screens.dashboard.DashboardScreen
import com.example.pennypilot.presentation.screens.expenses.AddExpenseScreen
import com.example.pennypilot.presentation.screens.expenses.SmartExpenseScreen
import com.example.pennypilot.presentation.screens.home.HomeScreen
import com.example.pennypilot.presentation.screens.login.LoginScreen
import com.example.pennypilot.presentation.screens.signup.SignUpScreen
import com.example.pennypilot.presentation.screens.profile.ProfileScreen
import com.example.pennypilot.presentation.screens.travel.TravelModeScreen
import com.example.pennypilot.presentation.screens.wallets.SharedWalletsScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "dashboard"
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("dashboard") { DashboardScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("add_expense") { AddExpenseScreen(navController) }
        composable("expenses") { SmartExpenseScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable(
            route = "travel/{userHomeCurrency}",
            arguments = listOf(navArgument("userHomeCurrency") { type = NavType.StringType })
        ) {
            TravelModeScreen(navController)
        }
        composable("shared_wallet") { SharedWalletsScreen(navController) }
        composable("analytics") { AnalyticsScreen(navController) }
        composable("ai_assistant") { AiAssistantScreen(navController) }
    }
}