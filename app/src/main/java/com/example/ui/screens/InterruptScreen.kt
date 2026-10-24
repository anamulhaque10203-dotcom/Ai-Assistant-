package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NovaTopBar
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.NovaDarkBg
import com.example.ui.theme.NovaDarkSurface
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.viewmodel.NovaViewModel

@Composable
fun InterruptScreen(
    viewModel: NovaViewModel,
    onInterrupt: () -> Unit
) {
    val currentAnswer by viewModel.currentAnswer.collectAsState()

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
            NovaTopBar(
                title = "NOVA AI",
                showBack = true,
                onNavigationClick = onInterrupt
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Speaking...",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaTextPrimary
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Prominent Speaking Waveform
                WaveformVisualizer(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    barCount = 32,
                    height = 80.dp,
                    isActive = true,
                    isSpeakingMode = true
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Large Interrupt Button
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(NovaDarkSurface)
                        .clickable {
                            viewModel.stopSpeaking()
                            onInterrupt()
                        }
                        .testTag("interrupt_speech_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Interrupt",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaError
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
