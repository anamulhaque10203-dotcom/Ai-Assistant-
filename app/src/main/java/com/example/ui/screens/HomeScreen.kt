package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
fun HomeScreen(
    viewModel: NovaViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToTextInput: () -> Unit,
    onStartVoiceListening: () -> Unit,
    onQuickPromptSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val voiceState by viewModel.voiceState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onStartVoiceListening()
        } else {
            Toast.makeText(context, "Microphone permission is required for voice.", Toast.LENGTH_SHORT).show()
        }
    }

    val quickPrompts = listOf(
        "Tell me a story",
        "Explain a topic",
        "Help me learn English",
        "Give me a coding example",
        "বাংলাদেশের রাজধানী কোথায়?"
    )

    Scaffold(
        containerColor = NovaDarkBg,
        topBar = {
            NovaTopBar(
                title = "NOVA AI",
                showBack = false,
                onNavigationClick = {
                    Toast.makeText(context, "NOVA AI Voice Assistant", Toast.LENGTH_SHORT).show()
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = NovaTextPrimary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NovaBottomBar(
                currentTab = NovaNavTab.HOME,
                onTabSelected = { tab ->
                    when (tab) {
                        NovaNavTab.HOME -> {}
                        NovaNavTab.HISTORY -> onNavigateToHistory()
                        NovaNavTab.SETTINGS -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Main Central Orb & Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                NovaOrb(
                    state = if (voiceState == NovaVoiceState.IDLE) NovaVoiceState.IDLE else voiceState,
                    size = 230.dp,
                    onClick = {
                        val hasMic = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasMic) {
                            onStartVoiceListening()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Ready to listen",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaTextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tap the mic and speak",
                    fontSize = 14.sp,
                    color = NovaTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Control Action Row (Keyboard, Large Glowing Mic, History Shortcut)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Keyboard Button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(NovaDarkSurface)
                        .border(1.dp, NovaDarkBorder, CircleShape)
                        .clickable { onNavigateToTextInput() }
                        .testTag("home_keyboard_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Text Input",
                        tint = NovaCyanBright,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Large Glowing Microphone Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(NovaElectricPurple)
                        .clickable {
                            val hasMic = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasMic) {
                                onStartVoiceListening()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                        .testTag("home_big_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Start Voice Listening",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                // History / Circle Button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(NovaDarkSurface)
                        .border(1.dp, NovaDarkBorder, CircleShape)
                        .clickable { onNavigateToHistory() }
                        .testTag("home_history_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = NovaCyanBright,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Quick Prompts section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Quick Prompts",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaTextMuted,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    quickPrompts.forEach { prompt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(NovaDarkSurface)
                                .border(1.dp, NovaDarkBorder, RoundedCornerShape(20.dp))
                                .clickable { onQuickPromptSelected(prompt) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .testTag("quick_prompt_${prompt.take(10)}")
                        ) {
                            Text(
                                text = prompt,
                                fontSize = 13.sp,
                                color = NovaTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
