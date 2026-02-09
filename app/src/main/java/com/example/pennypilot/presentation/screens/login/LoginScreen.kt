package com.example.pennypilot.presentation.screens.login

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pennypilot.R
import com.example.pennypilot.presentation.viewmodel.AuthResult
import com.example.pennypilot.presentation.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import timber.log.Timber

@SuppressLint("LogNotTimber")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authResult = viewModel.authResultState
    val context = LocalContext.current

    // --- Google Sign-In Setup ---
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id)) // this line MUST be present!
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("GoogleSignIn", "Activity result received. Result code: ${result.resultCode}") // Log the result code
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                val idToken = account.idToken
                if (idToken != null) {
                    Timber.tag("GoogleSignIn").i("Google ID Token received successfully: $idToken") // Log the token (be mindful of logging sensitive data in production)
                    viewModel.handleGoogleIdToken(idToken)
                } else {
                    Timber.tag("GoogleSignIn")
                        .e("Google ID Token is NULL after successful account retrieval.")
                    viewModel.handleGoogleIdToken(null)
                    // Potentially show a more specific error to the user here
                    Toast.makeText(context, "Failed to get Google ID Token.", Toast.LENGTH_LONG).show()
                }
            } else {
                // This case should ideally not happen if task.getResult doesn't throw an exception.
                // But good for defensive coding.
                Timber.tag("GoogleSignIn").e("GoogleSignInAccount is NULL but no ApiException was thrown.")
                viewModel.handleGoogleIdToken(null)
                Toast.makeText(context, "Google Sign In failed: No account data.", Toast.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Google sign in failed with ApiException: ${e.message}", e)
            Log.e("GoogleSignIn", "ApiException Status Code: ${e.statusCode}") // VERY IMPORTANT
            // You can add more specific handling based on e.statusCode here
            // e.g., if (e.statusCode == CommonStatusCodes.NETWORK_ERROR) { ... }
            viewModel.handleGoogleIdToken(null)
            Toast.makeText(context, "Google Sign In Error: ${e.statusCode}", Toast.LENGTH_LONG).show() // Show status code
        } catch (e: Exception) {
            // Catch any other unexpected exceptions
            Log.e("GoogleSignIn", "Google sign in failed with general Exception: ${e.message}", e)
            viewModel.handleGoogleIdToken(null)
            Toast.makeText(context, "An unexpected error occurred during Google Sign In.", Toast.LENGTH_LONG).show()
        }
    }

    // --- End Google Sign-In Setup ---

    LaunchedEffect(authResult) {
        when (val result = authResult) {
            is AuthResult.Success -> {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
                viewModel.resetAuthState()
            }
            is AuthResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                viewModel.resetAuthState()
            }
            is AuthResult.Loading -> {}
            is AuthResult.Idle -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF006837), Color(0xFF000000))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Text("PENNY", color = Color.White, fontSize = 28.sp, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Text("PILOT", color = Color(0xFF61BC79), fontSize = 28.sp, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Log In",
                        color = Color(0xFF80C692),
                        fontSize = 24.sp,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_field"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF61BC79),
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFF80C692),
                            unfocusedLabelColor = Color.LightGray,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_field"),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF61BC79),
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFF80C692),
                            unfocusedLabelColor = Color.LightGray,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.login(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button"),
                        enabled = authResult != AuthResult.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009146))
                    ) {
                        if (authResult == AuthResult.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Log In", color = Color.White, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            launcher.launch(googleSignInClient.signInIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                        enabled = authResult != AuthResult.Loading
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google logo",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue with Google", color = Color.White, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { navController.navigate("signup") }) {
                        Text("Don't have an account? Sign Up", color = Color.White)
                    }
                }
            }
        }
    }
}