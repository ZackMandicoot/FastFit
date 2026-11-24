package edu.virginia.cs4720.fastfit
import com.google.firebase.auth.FirebaseAuth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.ClearCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource

@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val auth = remember { FirebaseAuth.getInstance() }
    var isLoggedIn by remember {
        mutableStateOf(auth.currentUser != null)
    }
    val context = LocalContext.current
    val webClientId = "253072218842-7bdv01a1sajn2k225sq4ir4ub6nnrn5s"
    val coroutineScope = rememberCoroutineScope()

    val credentialManager = remember {
        CredentialManager.create(context)
    }

    var successMessage by remember { mutableStateOf("") }

    if (isLoggedIn) {
        val currentUserId = auth.currentUser?.uid
        var selectedRestaurant by remember { mutableStateOf<RestaurantItem?>(null) }
        var showFavorites by remember { mutableStateOf(false) }
        val favoriteMeals = remember(currentUserId) {
            mutableStateListOf<FavoriteMeal>().apply {
                addAll(loadUserFavorites(context, currentUserId))
            }
        }

        val logout: () -> Unit = {
            selectedRestaurant = null
            showFavorites = false
            auth.signOut()
            isLoggedIn = false
            password = ""
            errorMessage = ""

            coroutineScope.launch {
                try {
                    credentialManager.clearCredentialState(
                        ClearCredentialStateRequest()
                    )
                } catch (e: ClearCredentialException) {
                    // Firebase session is already signed out.
                }
            }
        }

        val currentRestaurant = selectedRestaurant
        when {
            showFavorites -> {
                FavoritesScreen(
                    favoriteMeals = favoriteMeals,
                    onRemoveFavorite = { meal ->
                        favoriteMeals.remove(meal)
                        saveUserFavorites(context, currentUserId, favoriteMeals)
                    },
                    onClearFavorites = {
                        favoriteMeals.clear()
                        saveUserFavorites(context, currentUserId, favoriteMeals)
                    },
                    onAddMoreClick = {
                        showFavorites = false
                        selectedRestaurant = null
                    },
                    onHomeClick = {
                        showFavorites = false
                        selectedRestaurant = null
                    },
                    onFoodLogClick = { /* Already on Favorites / Food Log */ }
                )
            }

            currentRestaurant != null -> {
                MenuScreen(
                    restaurantName = currentRestaurant.name,
                    restaurantId = currentRestaurant.id,
                    onBackClick = { selectedRestaurant = null },
                    onAddToLogClick = { item ->
                        favoriteMeals.add(item.toFavoriteMeal(currentRestaurant.name))
                        saveUserFavorites(context, currentUserId, favoriteMeals)
                    }
                )
            }

            else -> {
                RestaurantScreen(
                    onBackClick = logout,
                    onRestaurantClick = { restaurant ->
                        selectedRestaurant = restaurant
                    },
                    onHomeClick = {
                        showFavorites = false
                        selectedRestaurant = null
                    },
                    onFoodLogClick = { showFavorites = true }
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "FastFit",
            fontSize = 40.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please enter your email and password."
                } else {
                    errorMessage = ""

                    auth.signInWithEmailAndPassword(
                        email.trim(),
                        password
                    ).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            isLoggedIn = true
                            errorMessage = ""
                        } else {
                            errorMessage = "Login failed. Check your credentials."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        if (errorMessage.isNotEmpty()) {
            if (successMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = successMessage,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please enter your email and password."
                } else if (password.length < 6) {
                    errorMessage = "Password must be at least 6 characters."
                } else {
                    errorMessage = ""

                    auth.createUserWithEmailAndPassword(
                        email.trim(),
                        password
                    ).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            isLoggedIn = true
                            errorMessage = ""
                        } else {
                            errorMessage = task.exception?.localizedMessage
                                ?: "Unable to create account."
                        }
                    }
                }
            }
        ) {
            Text("First time? Create account")
        }

        TextButton(
            onClick = {
                errorMessage = ""
                successMessage = ""

                if (email.isBlank()) {
                    errorMessage = "Please enter your email address."
                } else {
                    auth.sendPasswordResetEmail(email.trim())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                successMessage =
                                    "If an account exists, a reset link will be sent."
                            } else {
                                errorMessage = "Unable to send password reset email."
                            }
                        }
                }
            }
        ) {
            Text("Forgot password?")
        }

        OutlinedButton(
            onClick = {
                errorMessage = ""

                coroutineScope.launch {
                    try {
                        val googleOption = GetSignInWithGoogleOption.Builder(
                            webClientId
                        ).build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleOption)
                            .build()

                        val result = credentialManager.getCredential(
                            context = context,
                            request = request
                        )

                        val credential = result.credential

                        if (credential is CustomCredential &&
                            credential.type ==
                            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        ) {
                            val googleCredential =
                                GoogleIdTokenCredential.createFrom(
                                    credential.data
                                )

                            val firebaseCredential =
                                GoogleAuthProvider.getCredential(
                                    googleCredential.idToken,
                                    null
                                )

                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        isLoggedIn = true
                                        errorMessage = ""
                                    } else {
                                        errorMessage = "Google login failed."
                                    }
                                }
                        } else {
                            errorMessage = "Unexpected credential type."
                        }

                    } catch (e: GetCredentialException) {
                        errorMessage = "Google sign-in failed: ${e.message}"
                    } catch (e: Exception) {
                        errorMessage = "Unable to complete Google sign-in."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign in with Google")
        }
    }
}