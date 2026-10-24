package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.NovaScreen
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AiResponseScreen
import com.example.ui.screens.ApiConfigScreen
import com.example.ui.screens.ApiKeySetupScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InterruptScreen
import com.example.ui.screens.LanguageSelectionScreen
import com.example.ui.screens.MicrophonePermissionScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TextInputScreen
import com.example.ui.screens.ThinkingScreen
import com.example.ui.screens.VoiceListeningScreen
import com.example.ui.screens.VoiceSpeechScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaDarkBg
import com.example.ui.theme.NovaDarkSurfaceVariant
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.viewmodel.NovaViewModel
import com.example.ui.viewmodel.NovaVoiceState
import kotlinx.coroutines.launch

@Composable
fun NovaApp(
    viewModel: NovaViewModel = viewModel()
) {
    val isOnboardingDone by viewModel.isOnboardingCompleted.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var currentScreen by remember {
        mutableStateOf(if (isOnboardingDone) NovaScreen.HOME else NovaScreen.WELCOME)
    }

    // Keep screen state synchronized with Voice State Machine
    LaunchedEffect(voiceState) {
        when (voiceState) {
            NovaVoiceState.LISTENING -> {
                if (currentScreen != NovaScreen.VOICE_LISTENING) {
                    currentScreen = NovaScreen.VOICE_LISTENING
                }
            }
            NovaVoiceState.PROCESSING -> {
                if (currentScreen != NovaScreen.THINKING) {
                    currentScreen = NovaScreen.THINKING
                }
            }
            NovaVoiceState.SPEAKING -> {
                if (currentScreen != NovaScreen.AI_RESPONSE && currentScreen != NovaScreen.INTERRUPT) {
                    currentScreen = NovaScreen.AI_RESPONSE
                }
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaDarkBg)
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "NovaScreenTransition"
        ) { screen ->
            when (screen) {
                NovaScreen.WELCOME -> {
                    WelcomeScreen(
                        onGetStarted = { currentScreen = NovaScreen.ONBOARDING }
                    )
                }

                NovaScreen.ONBOARDING -> {
                    BackHandler { currentScreen = NovaScreen.WELCOME }
                    OnboardingScreen(
                        onNext = {
                            if (viewModel.getEffectiveApiKey().isEmpty()) {
                                currentScreen = NovaScreen.API_KEY_SETUP
                            } else {
                                currentScreen = NovaScreen.MICROPHONE_PERMISSION
                            }
                        }
                    )
                }

                NovaScreen.API_KEY_SETUP -> {
                    BackHandler { currentScreen = NovaScreen.ONBOARDING }
                    ApiKeySetupScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = NovaScreen.ONBOARDING },
                        onSaveSuccess = {
                            currentScreen = NovaScreen.MICROPHONE_PERMISSION
                        }
                    )
                }

                NovaScreen.MICROPHONE_PERMISSION -> {
                    BackHandler {
                        viewModel.completeOnboarding()
                        currentScreen = NovaScreen.HOME
                    }
                    MicrophonePermissionScreen(
                        onPermissionGranted = {
                            viewModel.completeOnboarding()
                            currentScreen = NovaScreen.HOME
                        },
                        onSkip = {
                            viewModel.completeOnboarding()
                            currentScreen = NovaScreen.HOME
                        }
                    )
                }

                NovaScreen.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToSettings = { currentScreen = NovaScreen.SETTINGS },
                        onNavigateToHistory = { currentScreen = NovaScreen.CONVERSATION_HISTORY },
                        onNavigateToTextInput = { currentScreen = NovaScreen.TEXT_INPUT },
                        onStartVoiceListening = {
                            viewModel.startListening()
                            currentScreen = NovaScreen.VOICE_LISTENING
                        },
                        onQuickPromptSelected = { prompt ->
                            viewModel.submitQuestion(prompt)
                            currentScreen = NovaScreen.THINKING
                        }
                    )
                }

                NovaScreen.VOICE_LISTENING -> {
                    BackHandler {
                        viewModel.cancelListening()
                        currentScreen = NovaScreen.HOME
                    }
                    VoiceListeningScreen(
                        viewModel = viewModel,
                        onCancel = {
                            viewModel.cancelListening()
                            currentScreen = NovaScreen.HOME
                        },
                        onStop = {
                            viewModel.stopListening()
                        }
                    )
                }

                NovaScreen.THINKING -> {
                    BackHandler {
                        currentScreen = NovaScreen.HOME
                    }
                    ThinkingScreen(
                        viewModel = viewModel,
                        onCancel = {
                            currentScreen = NovaScreen.HOME
                        }
                    )
                }

                NovaScreen.AI_RESPONSE -> {
                    BackHandler {
                        viewModel.stopSpeaking()
                        currentScreen = NovaScreen.HOME
                    }
                    AiResponseScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            viewModel.stopSpeaking()
                            currentScreen = NovaScreen.HOME
                        },
                        onNavigateToSettings = { currentScreen = NovaScreen.SETTINGS },
                        onAskAnother = {
                            viewModel.stopSpeaking()
                            viewModel.startListening()
                            currentScreen = NovaScreen.VOICE_LISTENING
                        }
                    )
                }

                NovaScreen.INTERRUPT -> {
                    BackHandler {
                        viewModel.stopSpeaking()
                        currentScreen = NovaScreen.HOME
                    }
                    InterruptScreen(
                        viewModel = viewModel,
                        onInterrupt = {
                            viewModel.stopSpeaking()
                            currentScreen = NovaScreen.HOME
                        }
                    )
                }

                NovaScreen.TEXT_INPUT -> {
                    BackHandler { currentScreen = NovaScreen.HOME }
                    TextInputScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = NovaScreen.HOME },
                        onNavigateToHome = { currentScreen = NovaScreen.HOME },
                        onNavigateToHistory = { currentScreen = NovaScreen.CONVERSATION_HISTORY },
                        onNavigateToSettings = { currentScreen = NovaScreen.SETTINGS },
                        onSubmitPrompt = { text ->
                            viewModel.submitQuestion(text)
                            currentScreen = NovaScreen.THINKING
                        }
                    )
                }

                NovaScreen.CONVERSATION_HISTORY -> {
                    BackHandler { currentScreen = NovaScreen.HOME }
                    HistoryScreen(
                        viewModel = viewModel,
                        onNavigateToHome = { currentScreen = NovaScreen.HOME },
                        onNavigateToSettings = { currentScreen = NovaScreen.SETTINGS },
                        onSelectConversation = { item ->
                            viewModel.submitQuestion(item.question)
                        }
                    )
                }

                NovaScreen.SETTINGS -> {
                    BackHandler { currentScreen = NovaScreen.HOME }
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToHome = { currentScreen = NovaScreen.HOME },
                        onNavigateToHistory = { currentScreen = NovaScreen.CONVERSATION_HISTORY },
                        onNavigateToLanguage = { currentScreen = NovaScreen.LANGUAGE_SELECTION },
                        onNavigateToVoice = { currentScreen = NovaScreen.VOICE_SPEECH },
                        onNavigateToApiConfig = { currentScreen = NovaScreen.API_CONFIG },
                        onNavigateToAbout = { currentScreen = NovaScreen.ABOUT }
                    )
                }

                NovaScreen.LANGUAGE_SELECTION -> {
                    BackHandler { currentScreen = NovaScreen.SETTINGS }
                    LanguageSelectionScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = NovaScreen.SETTINGS }
                    )
                }

                NovaScreen.VOICE_SPEECH -> {
                    BackHandler { currentScreen = NovaScreen.SETTINGS }
                    VoiceSpeechScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = NovaScreen.SETTINGS }
                    )
                }

                NovaScreen.API_CONFIG -> {
                    BackHandler { currentScreen = NovaScreen.SETTINGS }
                    ApiConfigScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = NovaScreen.SETTINGS },
                        onEditKey = { currentScreen = NovaScreen.API_KEY_SETUP }
                    )
                }

                NovaScreen.ABOUT -> {
                    BackHandler { currentScreen = NovaScreen.SETTINGS }
                    AboutScreen(
                        onNavigateBack = { currentScreen = NovaScreen.SETTINGS }
                    )
                }
            }
        }

        // Error Dialog
        if (errorMessage != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissError() },
                title = {
                    Text(
                        text = "NOVA AI Notice",
                        color = NovaTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = errorMessage ?: "",
                        color = NovaTextSecondary,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissError() }) {
                        Text("OK", color = NovaCyanBright)
                    }
                },
                dismissButton = {
                    if (voiceState == NovaVoiceState.ERROR) {
                        TextButton(
                            onClick = {
                                viewModel.dismissError()
                                viewModel.retryLastQuestion()
                            }
                        ) {
                            Text("Retry", color = NovaCyanBright)
                        }
                    }
                },
                containerColor = NovaDarkSurfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
