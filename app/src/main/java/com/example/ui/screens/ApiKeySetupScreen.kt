package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaDarkBg
import com.example.ui.theme.NovaDarkBorder
import com.example.ui.theme.NovaDarkSurface
import com.example.ui.theme.NovaDarkSurfaceVariant
import com.example.ui.theme.NovaElectricPurple
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaErrorBg
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSuccessBg
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.viewmodel.NovaViewModel

@Composable
fun ApiKeySetupScreen(
    viewModel: NovaViewModel,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val savedKey by viewModel.savedApiKey.collectAsState()
    val testLoading by viewModel.testConnectionLoading.collectAsState()
    val isConnected by viewModel.isApiConnected.collectAsState()
    val testMsg by viewModel.testConnectionMessage.collectAsState()

    var apiKeyInput by remember(savedKey) {
        mutableStateOf(if (savedKey.isNotBlank()) savedKey else viewModel.getEffectiveApiKey())
    }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Header with back arrow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("api_setup_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NovaTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Configure Gemini API",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Enter your Google AI Studio API key to connect NOVA AI with Gemini.",
                    fontSize = 14.sp,
                    color = NovaTextSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Input Field
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("API Key", color = NovaTextMuted) },
                    placeholder = { Text("AIzaSy...", color = NovaTextMuted) },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val clip = clipboardManager.getText()
                                    if (clip != null && clip.text.isNotBlank()) {
                                        apiKeyInput = clip.text.trim()
                                        Toast.makeText(context, "API Key Pasted", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste API key",
                                    tint = NovaCyanBright
                                )
                            }
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) "Hide API key" else "Show API key",
                                    tint = NovaTextMuted
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input_field"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NovaDarkSurface,
                        unfocusedContainerColor = NovaDarkSurface,
                        focusedBorderColor = NovaElectricPurple,
                        unfocusedBorderColor = NovaDarkBorder,
                        focusedTextColor = NovaTextPrimary,
                        unfocusedTextColor = NovaTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Link to instructions
                Text(
                    text = "How to get API key?",
                    fontSize = 13.sp,
                    color = NovaCyanBright,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { showHelpDialog = true }
                        .padding(vertical = 4.dp)
                        .testTag("how_to_get_api_key_link")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Test Connection Button
                Button(
                    onClick = {
                        viewModel.testApiConnection(apiKeyInput)
                    },
                    enabled = !testLoading && apiKeyInput.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("test_connection_button"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaElectricPurple.copy(alpha = 0.85f),
                        contentColor = Color.White
                    )
                ) {
                    if (testLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Testing Connection...")
                    } else {
                        Text("Test Connection", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Connection Result Pill / Status Banner
                if (isConnected == true) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(NovaSuccessBg.copy(alpha = 0.35f))
                            .border(1.dp, NovaSuccess.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = NovaSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Connection successful!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NovaSuccess
                            )
                            Text(
                                text = "Your API key is valid and connected.",
                                fontSize = 12.sp,
                                color = NovaTextSecondary
                            )
                        }
                    }
                } else if (isConnected == false && testMsg != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(NovaErrorBg.copy(alpha = 0.35f))
                            .border(1.dp, NovaError.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = NovaError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = testMsg ?: "Connection failed",
                            fontSize = 12.sp,
                            color = NovaError
                        )
                    }
                }
            }

            // Save and Cancel buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp)
            ) {
                Button(
                    onClick = {
                        if (apiKeyInput.isNotBlank()) {
                            viewModel.saveApiKey(apiKeyInput)
                            Toast.makeText(context, "API Key Saved", Toast.LENGTH_SHORT).show()
                            onSaveSuccess()
                        } else {
                            Toast.makeText(context, "Please enter an API key", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("api_key_save_button"),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaElectricPurple,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("api_key_cancel_button"),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NovaTextSecondary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(NovaDarkBorder)
                    )
                ) {
                    Text("Cancel", fontSize = 15.sp)
                }
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Text(
                    text = "Obtaining a Gemini API Key",
                    color = NovaTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "1. Visit Google AI Studio at aistudio.google.com\n" +
                            "2. Sign in with your Google account\n" +
                            "3. Click 'Get API key' and generate a new key\n" +
                            "4. Copy and paste it here into NOVA AI.\n\n" +
                            "Note: Your key is stored locally and securely on your device.",
                    color = NovaTextSecondary,
                    lineHeight = 20.sp,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Got it", color = NovaCyanBright)
                }
            },
            containerColor = NovaDarkSurfaceVariant,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
