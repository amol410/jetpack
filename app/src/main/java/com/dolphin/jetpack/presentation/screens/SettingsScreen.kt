package com.dolphin.jetpack.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.dolphin.jetpack.presentation.viewmodel.AuthViewModel
import com.dolphin.jetpack.util.ThemePreferences
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.collectAsState()
    val context = LocalContext.current
    val themePreferences = remember { ThemePreferences(context) }
    val isDarkMode by themePreferences.isDarkModeFlow.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = authState.value) {
                is com.dolphin.jetpack.presentation.viewmodel.AuthState.Loading -> {
                    CircularProgressIndicator()
                }
                is com.dolphin.jetpack.presentation.viewmodel.AuthState.Authenticated -> {
                    val user = state.user
                    
                    // Profile Image placeholder - using a simple approach without Coil
                    // The UI Avatars API generates a cartoon-style profile image
                    val profileImageUrl = user.photoUrl?.toString() ?: "https://ui-avatars.com/api/?name=${user.displayName ?: user.email?.split("@")?.get(0) ?: "User"}&background=0D8ABC&color=fff"
                    
                    // For now, using a simple text-based avatar since Coil is not configured
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.displayName?.firstOrNull()?.uppercase() ?: user.email?.firstOrNull { it != '@' }?.uppercase() ?: "U",
                            fontSize = 40.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email ID
                    Text(
                        text = user.email ?: "No email provided",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Dark Mode Toggle
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dark Mode",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { enabled ->
                                    coroutineScope.launch {
                                        themePreferences.setDarkMode(enabled)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign Out Button
                    Button(
                        onClick = {
                            authViewModel.signOut()
                            onBack() // Go back after signing out
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Sign Out")
                    }
                }
                is com.dolphin.jetpack.presentation.viewmodel.AuthState.Unauthenticated -> {
                    Text("Not signed in")
                    Button(onClick = onBack) {
                        Text("Back")
                    }
                }
                is com.dolphin.jetpack.presentation.viewmodel.AuthState.Error -> {
                    Text("Error: ${state.message}")
                    Button(onClick = onBack) {
                        Text("Back")
                    }
                }
            }
        }
    }
}