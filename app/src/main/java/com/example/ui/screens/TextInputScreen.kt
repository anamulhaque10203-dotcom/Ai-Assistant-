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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NovaBottomBar
import com.example.ui.components.NovaNavTab
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
import com.example.ui.viewmodel.NovaViewModel
import com.example.ui.viewmodel.NovaVoiceState

@Composable
fun TextInputScreen(
    viewModel: NovaViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSubmitPrompt: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val voiceState by viewModel.voiceState.collectAsState()
    val isThinking = voiceState == NovaVoiceState.PROCESSING
    val allConversations by viewModel.allConversations.collectAsState()

    val exampleQuestions = listOf(
        "What is the weather today?",
        "Give me a coding example",
        "Tell me a fun fact",
        "How can I improve my English?"
    )

    Scaffold(
        containerColor = NovaDarkBg,
        topBar = {
            NovaTopBar(
                title = "NOVA AI",
                showBack = true,
                onNavigationClick = onNavigateBack
            )
        },
        bottomBar = {
            NovaBottomBar(
                currentTab = NovaNavTab.HOME,
                onTabSelected = { tab ->
                    when (tab) {
                        NovaNavTab.HOME -> onNavigateToHome()
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Input Bar with Send Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Type your message...", color = NovaTextMuted, fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("text_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = false,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NovaDarkSurface,
                            unfocusedContainerColor = NovaDarkSurface,
                            focusedBorderColor = NovaElectricPurple,
                            unfocusedBorderColor = NovaDarkBorder,
                            focusedTextColor = NovaTextPrimary,
                            unfocusedTextColor = NovaTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isThinking) NovaElectricPurple else NovaDarkSurface)
                            .border(1.dp, NovaDarkBorder, CircleShape)
                            .clickable(enabled = inputText.isNotBlank() && !isThinking) {
                                val query = inputText.trim()
                                inputText = ""
                                onSubmitPrompt(query)
                            }
                            .testTag("text_send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isThinking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = NovaCyanBright,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send message",
                                tint = if (inputText.isNotBlank()) Color.White else NovaTextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Example questions
                Text(
                    text = "Example questions:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaTextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                exampleQuestions.forEach { question ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(NovaDarkSurface)
                            .border(1.dp, NovaDarkBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                onSubmitPrompt(question)
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("example_q_${question.take(12)}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = null,
                                tint = NovaCyanBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = question,
                                fontSize = 14.sp,
                                color = NovaTextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
