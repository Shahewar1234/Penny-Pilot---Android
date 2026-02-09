package com.example.pennypilot.presentation.screens.signup

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.pennypilot.presentation.viewmodel.AuthResult
import com.example.pennypilot.presentation.viewmodel.SignUpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedUICurrency by remember { mutableStateOf("USD") }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var currencyDropdownExpanded by remember { mutableStateOf(false) }

    val availableCurrencies = listOf("USD", "EUR", "INR", "GBP", "JPY", "CAD", "AUD")

    val signUpResult by viewModel.signUpResultState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(signUpResult) {
        when (val result = signUpResult) {
            is AuthResult.Success -> {
                Toast.makeText(context, "Sign up successful! Welcome ${displayName.takeIf { it.isNotBlank() } ?: ""}.", Toast.LENGTH_LONG).show()
                navController.navigate("login") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
                viewModel.resetSignUpState()
            }
            is AuthResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                viewModel.resetSignUpState()
            }
            is AuthResult.Loading -> {}
            is AuthResult.Idle -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF006837).copy(alpha = 0.9f)
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF006837),
                            Color(0xFF002010),
                            Color.Black
                        )
                    )
                )
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Join PennyPilot",
                            color = Color(0xFF80C692),
                            fontSize = 26.sp,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            colors = signUpTextFieldColors()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it.trim() },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            colors = signUpTextFieldColors()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password (min. 6 characters)") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (showPassword) "Hide password" else "Show password",
                                        tint = Color.LightGray
                                    )
                                }
                            },
                            colors = signUpTextFieldColors()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            isError = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword,
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                    Icon(
                                        imageVector = if (showConfirmPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (showConfirmPassword) "Hide password" else "Show password",
                                        tint = Color.LightGray
                                    )
                                }
                            },
                            colors = signUpTextFieldColors(isError = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword)
                        )
                        if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                            Text(
                                "Passwords do not match.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        ExposedDropdownMenuBox(
                            expanded = currencyDropdownExpanded,
                            onExpandedChange = { currencyDropdownExpanded = !currencyDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedUICurrency,
                                onValueChange = { /* Read Only */ },
                                label = { Text("Default Currency") },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                // Using .copy() on your helper is cleaner
                                colors = signUpTextFieldColors().copy(
                                    disabledTextColor = Color.White.copy(alpha = 0.9f),
                                    disabledLabelColor = Color.LightGray.copy(alpha = 0.8f),
                                    disabledTrailingIconColor = Color.LightGray,
                                    // Make sure this is also themed if needed for the disabled dropdown
                                    //disabledBorderColor = Color.Gray.copy(alpha = 0.5f)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = currencyDropdownExpanded,
                                onDismissRequest = { currencyDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF00381A))
                            ) {
                                availableCurrencies.forEach { currencyCode ->
                                    DropdownMenuItem(
                                        text = { Text(currencyCode, color = Color.White) },
                                        onClick = {
                                            selectedUICurrency = currencyCode
                                            currencyDropdownExpanded = false
                                        },
                                        colors = MenuDefaults.itemColors(
                                            textColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = {
                                if (password != confirmPassword) {
                                    Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.signUp(
                                    email = email,
                                    password = password,
                                    displayName = displayName,
                                    selectedCurrency = selectedUICurrency // Correct parameter name
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = signUpResult != AuthResult.Loading &&
                                    email.isNotBlank() &&
                                    displayName.isNotBlank() &&
                                    password.isNotBlank() &&
                                    confirmPassword.isNotBlank() &&
                                    password == confirmPassword, // Added password match check
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF009146),
                                disabledContainerColor = Color(0xFF009146).copy(alpha = 0.5f)
                            )
                        ) {
                            if (signUpResult == AuthResult.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Sign Up", color = Color.White, fontSize = 16.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = {
                            navController.navigate("login") { // Ensure "login_route" is correct
                                popUpTo("signup") { inclusive = true }
                            }
                        }) {
                            Text("Already have an account? Log In", color = Color(0xFFB0D6C0))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun signUpTextFieldColors(isError: Boolean = false): TextFieldColors {
    val errorColor = MaterialTheme.colorScheme.error
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = if (isError) errorColor else Color(0xFF61BC79),
        unfocusedBorderColor = if (isError) errorColor else Color.Gray.copy(alpha = 0.7f),
        errorBorderColor = errorColor,
        cursorColor = Color.White,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = if (isError) errorColor else Color(0xFF80C692),
        unfocusedLabelColor = Color.LightGray.copy(alpha = 0.8f),
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledTextColor = Color.White.copy(alpha = 0.7f),
        disabledBorderColor = Color.Gray.copy(alpha = 0.5f), // Corrected: disabLED
        disabledLabelColor = Color.LightGray.copy(alpha = 0.5f),
        disabledTrailingIconColor = Color.Gray.copy(alpha = 0.5f) // Added for completeness
    )
}
