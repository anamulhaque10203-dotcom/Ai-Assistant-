package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NovaTopBar
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaCyanGlow
import com.example.ui.theme.NovaDarkBg
import com.example.ui.theme.NovaDarkBorder
import com.example.ui.theme.NovaDarkSurface
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.viewmodel.NovaViewModel

@Composable
fun VoiceListeningScreen(
    viewModel: NovaViewModel,
    onCancel: () -> Unit,
    onStop: () -> Unit
) {
    val partialText by viewModel.speechRecognitionManager.partialText.collectAsState()
    val rmsLevel by viewModel.speechRecognitionManager.rmsLevel.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "ActiveMicPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            NovaTopBar(
                title = "NOVA AI",
                showBack = false
            )

            // Center Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Listening...",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaTextPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Cyan Waveform
                WaveformVisualizer(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    barCount = 30,
                    height = 70.dp,
                    isActive = true,
                    isSpeakingMode = false,
                    audioLevel = rmsLevel
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "I'm listening. Speak now...",
                    fontSize = 15.sp,
                    color = NovaTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Live recognized text preview box
                if (partialText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(NovaDarkSurface)
                            .border(1.dp, NovaCyanGlow.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = partialText,
                            fontSize = 15.sp,
                            color = NovaCyanBright,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Big Active Mic Button with pulsing aura
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(NovaCyanGlow.copy(alpha = 0.2f))
                        .border(2.dp, NovaCyanBright, CircleShape)
                        .clickable { onStop() }
                        .testTag("listening_mic_active_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(NovaCyanGlow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Active Microphone",
                            tint = Color.Black,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }

            // Bottom Actions: Cancel and Stop buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("voice_cancel_button"),
                    shape = RoundedCornerShape(26.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NovaDarkBorder)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 15.sp,
                        color = NovaTextMuted
                    )
                }

                Button(
                    onClick = onStop,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("voice_stop_button"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaError.copy(alpha = 0.9f),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Stop",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
