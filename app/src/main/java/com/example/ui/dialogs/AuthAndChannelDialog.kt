package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AuthManager
import com.example.model.UserProfile
import com.example.ui.theme.ShotblastBackground
import com.example.ui.theme.ShotblastBorder
import com.example.ui.theme.ShotblastCoral
import com.example.ui.theme.ShotblastCyan
import com.example.ui.theme.ShotblastSurface
import com.example.ui.theme.ShotblastSurfaceVariant
import com.example.ui.theme.ShotblastTextPrimary
import com.example.ui.components.ThemeToggleCard
import kotlinx.coroutines.launch

@Composable
fun AuthAndChannelDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by AuthManager.currentUser.collectAsStateWithLifecycle()
    val userProfile by AuthManager.userProfile.collectAsStateWithLifecycle()
    val isLoading by AuthManager.isLoading.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Create Channel

    // Form inputs
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var channelNameInput by remember { mutableStateOf("") }
    var channelHandleInput by remember { mutableStateOf("") }
    var bioInput by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ShotblastSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ShotblastCyan.copy(alpha = 0.5f)),
            modifier = modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                                .border(1.5.dp, ShotblastCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SmartDisplay,
                                contentDescription = null,
                                tint = ShotblastCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (currentUser != null) "My Channel Profile" else "SHOTBLAST Account",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Firebase Authentication & Cloud Database",
                                color = ShotblastCyan,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ShotblastBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Error / Success Banners
                errorMessage?.let { error ->
                    Surface(
                        color = ShotblastCoral.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = error,
                            color = ShotblastCoral,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                successMessage?.let { success ->
                    Surface(
                        color = Color(0xFF0E382B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = success,
                            color = Color(0xFF22C55E),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (currentUser == null) {
                    // =================== NOT LOGGED IN: TABS ===================
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = ShotblastSurfaceVariant,
                        contentColor = ShotblastCyan,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = ShotblastCyan
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                errorMessage = null
                            },
                            text = {
                                Text(
                                    "Sign In",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                errorMessage = null
                            },
                            text = {
                                Text(
                                    "Create Channel",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedTab == 0) {
                        // ---------- SIGN IN FORM ----------
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address", color = Color.Gray) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = ShotblastCyan) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShotblastCyan,
                                unfocusedBorderColor = ShotblastBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password", color = Color.Gray) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = ShotblastCyan) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShotblastCyan,
                                unfocusedBorderColor = ShotblastBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (emailInput.isBlank() || passwordInput.isBlank()) {
                                    errorMessage = "Please enter both Email and Password"
                                    return@Button
                                }
                                errorMessage = null
                                coroutineScope.launch {
                                    val result = AuthManager.signInWithEmail(emailInput, passwordInput)
                                    result.onSuccess {
                                        successMessage = "Welcome back, ${it.channelName}!"
                                    }.onFailure {
                                        errorMessage = it.localizedMessage ?: "Sign in failed"
                                    }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ShotblastCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("auth_sign_in_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Google Sign In Button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    AuthManager.signInWithGoogle(
                                        context = context,
                                        onSuccess = { profile ->
                                            successMessage = "Google Sign-In successful as ${profile.channelName}!"
                                        },
                                        onError = { err ->
                                            errorMessage = err
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ShotblastSurfaceVariant,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("auth_google_sign_in_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = null,
                                tint = ShotblastCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Continue with Google Sign-In", fontSize = 13.sp)
                        }

                    } else {
                        // ---------- CREATE CHANNEL & REGISTER ----------
                        OutlinedTextField(
                            value = channelNameInput,
                            onValueChange = { channelNameInput = it },
                            label = { Text("Channel Name", color = Color.Gray) },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.SmartDisplay, contentDescription = null, tint = ShotblastCyan) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShotblastCyan,
                                unfocusedBorderColor = ShotblastBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("auth_channel_name_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = channelHandleInput,
                            onValueChange = { channelHandleInput = it },
                            label = { Text("Channel Handle (e.g. @nature_vibes)", color = Color.Gray) },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = ShotblastCyan) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShotblastCyan,
                                unfocusedBorderColor = ShotblastBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("auth_channel_handle_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address", color = Color.Gray) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = ShotblastCyan) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShotblastCyan,
                                unfocusedBorderColor = ShotblastBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("auth_register_email_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password (Min 6 characters)", color = Color.Gray) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = ShotblastCyan) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShotblastCyan,
                                unfocusedBorderColor = ShotblastBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("auth_register_password_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (channelNameInput.isBlank()) {
                                    errorMessage = "Please enter your Channel Name"
                                    return@Button
                                }
                                if (emailInput.isBlank() || passwordInput.isBlank()) {
                                    errorMessage = "Please enter Email and Password"
                                    return@Button
                                }
                                if (passwordInput.length < 6) {
                                    errorMessage = "Password must be at least 6 characters"
                                    return@Button
                                }
                                errorMessage = null
                                coroutineScope.launch {
                                    val result = AuthManager.registerWithEmail(
                                        email = emailInput,
                                        pass = passwordInput,
                                        channelName = channelNameInput,
                                        channelHandle = channelHandleInput
                                    )
                                    result.onSuccess {
                                        successMessage = "Channel '${it.channelName}' created successfully!"
                                    }.onFailure {
                                        errorMessage = it.localizedMessage ?: "Channel registration failed"
                                    }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ShotblastCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("auth_create_channel_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Create Channel & Account", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        ThemeToggleCard()
                    }
                } else {
                    // =================== LOGGED IN: VIEW / EDIT CHANNEL ===================
                    val profile = userProfile ?: UserProfile(
                        uid = currentUser!!.uid,
                        email = currentUser!!.email ?: "",
                        channelName = "Shotblast Creator",
                        channelHandle = "@creator"
                    )

                    var isEditingChannel by remember { mutableStateOf(false) }
                    var editChannelName by remember(profile.channelName) { mutableStateOf(profile.channelName) }
                    var editHandle by remember(profile.channelHandle) { mutableStateOf(profile.channelHandle) }
                    var editBio by remember(profile.bio) { mutableStateOf(profile.bio) }

                    // Channel Avatar & Status Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ShotblastSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShotblastCyan.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                                    .border(2.dp, ShotblastCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = ShotblastCyan,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = profile.channelName,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${profile.channelHandle} • ${profile.email}",
                                color = ShotblastCyan,
                                fontSize = 12.sp
                            )
                            if (profile.bio.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = profile.bio,
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = ShotblastBorder)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Subscribers", color = Color.Gray, fontSize = 11.sp)
                                    Text("${profile.subscribersCount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Status", color = Color.Gray, fontSize = 11.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Active", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isEditingChannel) {
                        Button(
                            onClick = { isEditingChannel = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ShotblastCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Channel Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        // Edit inputs
                        OutlinedTextField(
                            value = editChannelName,
                            onValueChange = { editChannelName = it },
                            label = { Text("Channel Name", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShotblastCyan,
                                unfocusedBorderColor = ShotblastBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editHandle,
                            onValueChange = { editHandle = it },
                            label = { Text("Channel Handle", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShotblastCyan,
                                unfocusedBorderColor = ShotblastBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editBio,
                            onValueChange = { editBio = it },
                            label = { Text("Channel Bio / Description", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShotblastCyan,
                                unfocusedBorderColor = ShotblastBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { isEditingChannel = false },
                                colors = ButtonDefaults.buttonColors(containerColor = ShotblastSurfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", color = Color.White)
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val res = AuthManager.updateChannel(editChannelName, editHandle, editBio)
                                        res.onSuccess {
                                            isEditingChannel = false
                                            successMessage = "Channel updated successfully!"
                                        }.onFailure {
                                            errorMessage = it.localizedMessage ?: "Update failed"
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ShotblastCyan, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Channel", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // App Theme (Dark Mode / Light Mode)
                    ThemeToggleCard()

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sign Out Button
                    Button(
                        onClick = {
                            AuthManager.signOut(context)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF261216),
                            contentColor = ShotblastCoral
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Icon(Icons.Filled.Logout, contentDescription = null, tint = ShotblastCoral, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
