package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NovaTopBar
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaDarkBg
import com.example.ui.theme.NovaDarkBorder
import com.example.ui.theme.NovaDarkSurface
import com.example.ui.theme.NovaDarkSurfaceVariant
import com.example.ui.theme.NovaElectricPurple
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.viewmodel.NovaViewModel
import com.example.ui.viewmodel.NovaVoiceState

@Composable
fun AiResponseScreen(
    viewModel: NovaViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onAskAnother: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val currentAnswer by viewModel.currentAnswer.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val isSpeaking = voiceState == NovaVoiceState.SPEAKING

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                NovaTopBar(
                    title = "NOVA AI",
                    showBack = true,
                    onNavigationClick = onNavigateBack,
                    actions = {
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("response_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = NovaTextPrimary
                            )
                        }
                    }
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 10.dp, bottom = 16.dp)
                ) {
                    // User Question Bubble (Right aligned)
                    if (currentQuestion.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                                    .background(NovaElectricPurple.copy(alpha = 0.25f))
                                    .border(1.dp, NovaElectricPurple.copy(alpha = 0.5f), RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = currentQuestion,
                                    fontSize = 15.sp,
                                    color = NovaTextPrimary,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Assistant Response Card (Left aligned)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(NovaDarkSurface)
                            .border(1.dp, NovaDarkBorder, RoundedCornerShape(20.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Text(
                                text = currentAnswer.ifBlank { "No response content available." },
                                fontSize = 15.sp,
                                color = NovaTextPrimary,
                                lineHeight = 24.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action footer with Gemini tag, speak and copy buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(NovaDarkSurfaceVariant)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Gemini",
                                        tint = NovaCyanBright,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Gemini",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NovaCyanBright
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (currentAnswer.isNotBlank()) {
                                                clipboardManager.setText(AnnotatedString(currentAnswer))
                                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy text",
                                            tint = NovaTextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (isSpeaking) {
                                                viewModel.stopSpeaking()
                                            } else {
                                                viewModel.speakCurrentAnswer()
                                            }
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSpeaking) Icons.Default.Pause else Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = if (isSpeaking) "Pause reading" else "Read aloud",
                                            tint = if (isSpeaking) NovaCyanBright else NovaTextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Speaking Control Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                if (isSpeaking) {
                    WaveformVisualizer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        barCount = 26,
                        height = 42.dp,
                        isActive = true,
                        isSpeakingMode = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(NovaDarkSurface)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Speaking",
                                tint = NovaCyanBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Speaking...",
                                fontSize = 13.sp,
                                color = NovaCyanBright
                            )
                        }

                        Button(
                            onClick = { viewModel.stopSpeaking() },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NovaError,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("stop_speaking_button")
                        ) {
                            Text("Stop", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Ask another question button
                Button(
                    onClick = onAskAnother,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("ask_another_button"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaElectricPurple,
                        contentColor = Color.White
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Ask Another",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ask Another Question",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
