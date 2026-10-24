package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NovaOrb
import com.example.ui.components.NovaTopBar
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaDarkBg
import com.example.ui.theme.NovaDarkBorder
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.viewmodel.NovaViewModel
import com.example.ui.viewmodel.NovaVoiceState

@Composable
fun ThinkingScreen(
    viewModel: NovaViewModel,
    onCancel: () -> Unit
) {
    val currentQuestion by viewModel.currentQuestion.collectAsState()

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
                onNavigationClick = onCancel
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Thinking...",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaTextPrimary
                )

                Spacer(modifier = Modifier.height(36.dp))

                NovaOrb(
                    state = NovaVoiceState.PROCESSING,
                    size = 200.dp
                )

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "Let me think...",
                    fontSize = 15.sp,
                    color = NovaTextSecondary
                )

                if (currentQuestion.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "\"$currentQuestion\"",
                        fontSize = 13.sp,
                        color = NovaTextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = NovaCyanBright,
                    strokeWidth = 2.5.dp
                )
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("thinking_cancel_button"),
                shape = RoundedCornerShape(26.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(NovaDarkBorder)
                )
            ) {
                Text(
                    text = "Cancel Request",
                    fontSize = 15.sp,
                    color = NovaTextMuted
                )
            }
        }
    }
}
