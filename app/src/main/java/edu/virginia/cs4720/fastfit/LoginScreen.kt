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

    if (isLoggedIn) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome to FastFit!")

            Text(auth.currentUser?.email ?: "")

            Button(
                onClick = {
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
            ) {
                Text("Logout")
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
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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