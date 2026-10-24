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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speech.NovaVoiceOption
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

@Composable
fun VoiceSpeechScreen(
    viewModel: NovaViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentSpeed by viewModel.speechSpeed.collectAsState()
    val currentVoice by viewModel.selectedVoice.collectAsState()
    val availableVoices by viewModel.availableVoices.collectAsState()

    var sliderSpeed by remember(currentSpeed) { mutableFloatStateOf(currentSpeed) }
    var selectedVoiceId by remember(currentVoice, availableVoices) {
        mutableStateOf(currentVoice.ifBlank { availableVoices.firstOrNull()?.id ?: "" })
    }

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
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                NovaTopBar(
                    title = "Voice & Speech",
                    showBack = true,
                    onNavigationClick = onNavigateBack
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Speech Speed Section
                Text(
                    text = "Speech Speed",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(NovaDarkSurface)
                        .border(1.dp, NovaDarkBorder, RoundedCornerShape(18.dp))
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Column {
                        Slider(
                            value = sliderSpeed,
                            onValueChange = { sliderSpeed = it },
                            valueRange = 0.7f..1.4f,
                            steps = 2,
                            colors = SliderDefaults.colors(
                                thumbColor = NovaElectricPurple,
                                activeTrackColor = NovaElectricPurple,
                                inactiveTrackColor = NovaDarkSurfaceVariant
                            ),
                            modifier = Modifier.testTag("speech_speed_slider")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Slow", fontSize = 12.sp, color = NovaTextMuted)
                            Text("Normal", fontSize = 12.sp, color = if (kotlin.math.abs(sliderSpeed - 1.0f) < 0.15f) NovaCyanBright else NovaTextMuted)
                            Text("Fast", fontSize = 12.sp, color = NovaTextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Voice Selection Section
                Text(
                    text = "Voice Selection",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                availableVoices.forEach { voice ->
                    val isSelected = selectedVoiceId == voice.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(NovaDarkSurface)
                            .border(
                                1.dp,
                                if (isSelected) NovaCyanBright.copy(alpha = 0.6f) else NovaDarkBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedVoiceId = voice.id }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("voice_option_${voice.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NovaElectricPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = NovaCyanBright,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = voice.displayName,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = NovaTextPrimary
                            )
                            Text(
                                text = "Locale: ${voice.localeTag}",
                                fontSize = 11.sp,
                                color = NovaTextMuted
                            )
                        }

                        // Preview Voice Audio Play button
                        IconButton(
                            onClick = { viewModel.testSampleVoice(voice) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Preview voice",
                                tint = NovaCyanBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedVoiceId = voice.id },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = NovaCyanBright,
                                unselectedColor = NovaDarkBorder
                            )
                        )
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.saveSpeechSpeed(sliderSpeed)
                    viewModel.saveSelectedVoice(selectedVoiceId)
                    Toast.makeText(context, "Voice settings saved", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_voice_settings_button"),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaElectricPurple,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Save",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
