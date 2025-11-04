package com.dolphin.jetpack.presentation.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.dolphin.jetpack.util.AnalyticsHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val currentUser = auth.currentUser
        _authState.value = if (currentUser != null) {
            AuthState.Authenticated(currentUser)
        } else {
            AuthState.Unauthenticated
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                val credentialManager = CredentialManager.create(context)

                // Web client ID from your google-services.json
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("954842353363-3es8j5j88oinub65ln43563atq6166r5.apps.googleusercontent.com")
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                handleSignIn(result)
            } catch (e: Exception) {
                // Check if user cancelled the sign-in
                when {
                    e is GetCredentialCancellationException -> {
                        // User cancelled - just return to unauthenticated state without error
                        _authState.value = AuthState.Unauthenticated
                    }
                    e.message?.contains("cancel", ignoreCase = true) == true ||
                    e.message?.contains("user", ignoreCase = true) == true -> {
                        // User cancelled via other means
                        _authState.value = AuthState.Unauthenticated
                    }
                    else -> {
                        // Actual error occurred
                        _authState.value = AuthState.Error("Sign-in failed: ${e.message}")
                    }
                }
            }
        }
    }

    private suspend fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken

                        // Authenticate with Firebase
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        val authResult = auth.signInWithCredential(firebaseCredential).await()

                        authResult.user?.let { user ->
                            AnalyticsHelper.logLoginEvent("google")
                            AnalyticsHelper.setUserProperties(user.uid, user.email)
                            _authState.value = AuthState.Authenticated(user)
                        } ?: run {
                            _authState.value = AuthState.Error("Authentication failed")
                        }
                    } catch (e: GoogleIdTokenParsingException) {
                        _authState.value = AuthState.Error("Invalid Google ID token: ${e.message}")
                    }
                } else {
                    _authState.value = AuthState.Error("Unexpected credential type")
                }
            }
            else -> {
                _authState.value = AuthState.Error("Unexpected credential type")
            }
        }
    }

    // Email/Password Sign-Up
    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                if (email.isBlank() || password.isBlank()) {
                    _authState.value = AuthState.Error("Email and password cannot be empty")
                    return@launch
                }

                if (password.length < 6) {
                    _authState.value = AuthState.Error("Password must be at least 6 characters")
                    return@launch
                }

                val authResult = auth.createUserWithEmailAndPassword(email, password).await()

                authResult.user?.let { user ->
                    AnalyticsHelper.logSignUpEvent("email")
                    AnalyticsHelper.setUserProperties(user.uid, user.email)
                    _authState.value = AuthState.Authenticated(user)
                } ?: run {
                    _authState.value = AuthState.Error("Sign-up failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Sign-up error: ${e.message}")
            }
        }
    }

    // Email/Password Sign-In
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                if (email.isBlank() || password.isBlank()) {
                    _authState.value = AuthState.Error("Email and password cannot be empty")
                    return@launch
                }

                val authResult = auth.signInWithEmailAndPassword(email, password).await()

                authResult.user?.let { user ->
                    AnalyticsHelper.logLoginEvent("email")
                    AnalyticsHelper.setUserProperties(user.uid, user.email)
                    _authState.value = AuthState.Authenticated(user)
                } ?: run {
                    _authState.value = AuthState.Error("Sign-in failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Sign-in error: ${e.message}")
            }
        }
    }

    // Reset Password
    fun resetPassword(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                if (email.isBlank()) {
                    _authState.value = AuthState.Error("Email cannot be empty")
                    return@launch
                }

                auth.sendPasswordResetEmail(email).await()
                onSuccess()
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Password reset error: ${e.message}")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}
