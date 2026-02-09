package com.example.pennypilot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.pennypilot.di.AppModule
import com.example.pennypilot.di.RepositoryModule
import com.example.pennypilot.presentation.navigation.NavGraph
import com.example.pennypilot.ui.theme.PennyPilotTheme
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class, RepositoryModule::class)
class LoginScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.setContent {
            navController = TestNavHostController(ApplicationProvider.getApplicationContext())
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            PennyPilotTheme {
                NavGraph(navController = navController, startDestination = "login")
            }
        }
    }

    @Test
    fun loginScreen_initialState_displaysAllUIElements() {
        // 1. Verify that the email and password fields are displayed.
        composeTestRule.onNodeWithTag("email_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("password_field").assertIsDisplayed()

        // 2. Verify that the 'Log In' and 'Continue with Google' buttons are displayed.
        composeTestRule.onNodeWithTag("login_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continue with Google").assertIsDisplayed()

        // 3. Verify that the 'Sign Up' text button is displayed.
        composeTestRule.onNodeWithText("Don't have an account? Sign Up").assertIsDisplayed()
    }

    @Test
    fun emailAndPasswordFields_whenTypedInto_updatesState() {
        val testEmail = "test@example.com"
        val testPassword = "password123"

        // 1. Find the email field, type text into it, and assert it shows the typed text.
        composeTestRule.onNodeWithTag("email_field")
            .performTextInput(testEmail)
        composeTestRule.onNodeWithText(testEmail).assertIsDisplayed()

        // 2. Find the password field, type text into it.
        composeTestRule.onNodeWithTag("password_field")
            .performTextInput(testPassword)
    }

    @Test
    fun loginButton_whenClicked_navigatesToHome() {
        val testEmail = "user@test.com"
        val testPassword = "password"

        // Arrange
        val authResult: AuthResult = mockk(relaxed = true)
        val successfulTask: Task<AuthResult> = Tasks.forResult(authResult)
        every { firebaseAuth.signInWithEmailAndPassword(testEmail, testPassword) } returns successfulTask

        // Act
        composeTestRule.onNodeWithTag("email_field").performTextInput(testEmail)
        composeTestRule.onNodeWithTag("password_field").performTextInput(testPassword)
        composeTestRule.onNodeWithTag("login_button").performClick()

        composeTestRule.waitForIdle()

        // Assert
        val route = navController.currentBackStackEntry?.destination?.route
        assert(route?.startsWith("home") == true)
    }

    @Test
    fun signUpButton_whenClicked_navigatesToSignUpScreen() {
        // 1. Find the "Sign Up" text button and click it.
        composeTestRule.onNodeWithText("Don't have an account? Sign Up").performClick()

        // 2. Verify that the NavController's current destination is the sign-up screen.
        val route = navController.currentBackStackEntry?.destination?.route
        assert(route == "signup")
    }
}
