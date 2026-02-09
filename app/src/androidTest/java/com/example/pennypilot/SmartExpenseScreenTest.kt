package com.example.pennypilot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.pennypilot.di.AppModule
import com.example.pennypilot.di.RepositoryModule
import com.example.pennypilot.presentation.navigation.NavGraph
import com.example.pennypilot.ui.theme.PennyPilotTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(AppModule::class, RepositoryModule::class)
class SmartExpenseScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.setContent {
            navController = TestNavHostController(ApplicationProvider.getApplicationContext())
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            PennyPilotTheme {
                NavGraph(navController = navController, startDestination = "expenses")
            }
        }
    }

    @Test
    fun smartExpenseScreen_initialState_displaysAllUIElements() {
        composeTestRule.onNodeWithText("Smart Expenses").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total Spent This Period").assertIsDisplayed()
    }
}
