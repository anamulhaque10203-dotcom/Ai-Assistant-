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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ManageHistory
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NovaBottomBar
import com.example.ui.components.NovaNavTab
import com.example.ui.components.NovaOrb
import com.example.ui.components.NovaTopBar
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaDarkBg
import com.example.ui.theme.NovaDarkBorder
import com.example.ui.theme.NovaDarkSurface
import com.example.ui.theme.NovaElectricPurple
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.viewmodel.NovaViewModel
import com.example.ui.viewmodel.NovaVoiceState

@Composable
fun SettingsScreen(
    viewModel: NovaViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToVoice: () -> Unit,
    onNavigateToApiConfig: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val preferredLang by viewModel.preferredLanguage.collectAsState()
    val speechSpeed by viewModel.speechSpeed.collectAsState()
    val selectedVoice by viewModel.selectedVoice.collectAsState()

    val langLabel = when (preferredLang) {
        "bn" -> "Bengali (বাংলা)"
        "hi" -> "Hindi (हिन्दी)"
        "ar" -> "Arabic (العربية)"
        "es" -> "Spanish (Español)"
        else -> "English"
    }

    val speedLabel = when {
        speechSpeed <= 0.85f -> "Slow"
        speechSpeed >= 1.2f -> "Fast"
        else -> "Normal"
    }

    val voiceLabel = if (selectedVoice.isNotBlank()) {
        selectedVoice.take(18)
    } else {
        "Google Default"
    }

    Scaffold(
        containerColor = NovaDarkBg,
        topBar = {
            NovaTopBar(
                title = "Settings",
                showBack = false
            )
        },
        bottomBar = {
            NovaBottomBar(
                currentTab = NovaNavTab.SETTINGS,
                onTabSelected = { tab ->
                    when (tab) {
                        NovaNavTab.HOME -> onNavigateToHome()
                        NovaNavTab.HISTORY -> onNavigateToHistory()
                        NovaNavTab.SETTINGS -> {}
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Card at top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(NovaDarkSurface)
                    .border(1.dp, NovaDarkBorder, RoundedCornerShape(22.dp))
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NovaOrb(
                    state = NovaVoiceState.IDLE,
                    size = 54.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "NOVA AI",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Personal Assistant",
                        fontSize = 13.sp,
                        color = NovaCyanBright
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Settings List
            SettingsRowItem(
                icon = Icons.Default.Language,
                title = "Preferred Language",
                value = langLabel,
                testTag = "settings_language_row",
                onClick = onNavigateToLanguage
            )

            SettingsRowItem(
                icon = Icons.Default.Speed,
                title = "Speech Speed",
                value = speedLabel,
                testTag = "settings_speech_speed_row",
                onClick = onNavigateToVoice
            )

            SettingsRowItem(
                icon = Icons.Default.RecordVoiceOver,
                title = "Voice Selection",
                value = voiceLabel,
                testTag = "settings_voice_selection_row",
                onClick = onNavigateToVoice
            )

            SettingsRowItem(
                icon = Icons.Default.Key,
                title = "API Configuration",
                value = "Gemini API Key",
                testTag = "settings_api_config_row",
                onClick = onNavigateToApiConfig
            )

            SettingsRowItem(
                icon = Icons.Default.ManageHistory,
                title = "Conversation History",
                value = "Manage & Clear",
                testTag = "settings_history_row",
                onClick = onNavigateToHistory
            )

            SettingsRowItem(
                icon = Icons.Default.Security,
                title = "Privacy & Security",
                value = "Data & Permissions",
                testTag = "settings_privacy_row",
                onClick = onNavigateToAbout
            )

            SettingsRowItem(
                icon = Icons.Default.Info,
                title = "App Information",
                value = "Version 1.0.0",
                testTag = "settings_about_row",
                onClick = onNavigateToAbout
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    value: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NovaDarkSurface)
            .border(1.dp, NovaDarkBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(NovaElectricPurple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = NovaCyanBright,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = NovaTextPrimary,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            fontSize = 13.sp,
            color = NovaTextMuted
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = NovaTextMuted,
            modifier = Modifier.size(13.dp)
        )
    }
}
