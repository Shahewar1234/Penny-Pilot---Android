package com.example.pennypilot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.pennypilot.data.repository.UserRepository
import com.example.pennypilot.di.AppModule
import com.example.pennypilot.di.RepositoryModule
import com.example.pennypilot.presentation.navigation.NavGraph
import com.example.pennypilot.ui.theme.PennyPilotTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class, RepositoryModule::class)
class SignUpScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.setContent {
            navController = TestNavHostController(ApplicationProvider.getApplicationContext())
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            PennyPilotTheme {
                NavGraph(navController = navController, startDestination = "signup")
            }
        }
    }

    @Test
    fun signUpScreen_initialState_displaysAllUIElements() {
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Display Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email Address").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password (min. 6 characters)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Default Currency").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
        composeTestRule.onNodeWithText("Already have an account? Log In").assertIsDisplayed()
    }

    @Test
    fun loginButton_whenClicked_navigatesToLoginScreen() {
        // 1. Find the "Already have an account? Log In" text button and click it.
        composeTestRule.onNodeWithText("Already have an account? Log In").performClick()

        // 2. Verify that the NavController's current destination is the login screen.
        val route = navController.currentBackStackEntry?.destination?.route
        assert(route == "login")
    }
}
