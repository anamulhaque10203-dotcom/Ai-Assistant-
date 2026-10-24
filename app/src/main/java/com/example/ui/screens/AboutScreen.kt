package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NovaOrb
import com.example.ui.components.NovaTopBar
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaDarkBg
import com.example.ui.theme.NovaDarkBorder
import com.example.ui.theme.NovaDarkSurface
import com.example.ui.theme.NovaDarkSurfaceVariant
import com.example.ui.theme.NovaElectricPurple
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.viewmodel.NovaVoiceState

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NovaTopBar(
                    title = "About NOVA AI",
                    showBack = true,
                    onNavigationClick = onNavigateBack
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Orb Logo in Center
                NovaOrb(
                    state = NovaVoiceState.IDLE,
                    size = 110.dp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "NOVA AI",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaTextPrimary
                )

                Text(
                    text = "Version 1.0.0",
                    fontSize = 13.sp,
                    color = NovaTextMuted
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Your Personal Voice Assistant",
                    fontSize = 14.sp,
                    color = NovaCyanBright,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Feature Tech Stack Badges
                TechFeatureRow(
                    icon = Icons.Default.AutoAwesome,
                    label = "Powered by Google Gemini"
                )

                TechFeatureRow(
                    icon = Icons.Default.Mic,
                    label = "Speech-to-Text (Android)"
                )

                TechFeatureRow(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    label = "Text-to-Speech (Android)"
                )

                TechFeatureRow(
                    icon = Icons.Default.Code,
                    label = "Built with Kotlin & Jetpack Compose"
                )
            }

            // Bottom Policy & Terms Links
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Privacy Policy",
                    fontSize = 13.sp,
                    color = NovaTextSecondary,
                    modifier = Modifier
                        .clickable { showPrivacyDialog = true }
                        .padding(8.dp)
                        .testTag("about_privacy_link")
                )

                Text(
                    text = "•",
                    fontSize = 13.sp,
                    color = NovaTextMuted,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Text(
                    text = "Terms of Service",
                    fontSize = 13.sp,
                    color = NovaTextSecondary,
                    modifier = Modifier
                        .clickable { showTermsDialog = true }
                        .padding(8.dp)
                        .testTag("about_terms_link")
                )
            }
        }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", color = NovaTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "NOVA AI is designed with user privacy in mind:\n\n" +
                            "• Conversation History: Stored strictly on your local device via Room Database.\n" +
                            "• Microphone Access: Used solely for real-time speech recognition when you tap the mic.\n" +
                            "• API Keys: Safely stored locally in Android DataStore.\n" +
                            "• External Requests: Audio is transcribed on device, and only the text prompt is sent to Google's Gemini API.",
                    color = NovaTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close", color = NovaCyanBright)
                }
            },
            containerColor = NovaDarkSurfaceVariant,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms of Service", color = NovaTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "By using NOVA AI, you agree to:\n\n" +
                            "• Use the personal voice assistant responsibly and ethically.\n" +
                            "• Abide by Google Generative AI terms and rate limits.\n" +
                            "• Understand that AI responses are generative and should be verified for critical matters.",
                    color = NovaTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Close", color = NovaCyanBright)
                }
            },
            containerColor = NovaDarkSurfaceVariant,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun TechFeatureRow(
    icon: ImageVector,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NovaDarkSurface)
            .border(1.dp, NovaDarkBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(NovaElectricPurple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NovaCyanBright,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = NovaTextPrimary
        )
    }
}
