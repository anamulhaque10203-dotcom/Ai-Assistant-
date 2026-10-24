package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaCyanGlow
import com.example.ui.theme.NovaElectricPurple
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaPurpleLight
import com.example.ui.theme.NovaVividBlue
import com.example.ui.viewmodel.NovaVoiceState
import kotlin.math.sin

@Composable
fun NovaOrb(
    state: NovaVoiceState,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    audioLevel: Float = 0f,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NovaOrbAnimation")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == NovaVoiceState.LISTENING) 700 else 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbPulse"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == NovaVoiceState.PROCESSING) 2000 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == NovaVoiceState.SPEAKING) 900 else 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f * 0.72f * pulseScale

            val (outerGlowColor, ringGradient, centerSoundColor) = when (state) {
                NovaVoiceState.IDLE -> Triple(
                    NovaPurpleLight.copy(alpha = 0.35f),
                    listOf(NovaElectricPurple, NovaVividBlue, NovaCyanGlow, NovaElectricPurple),
                    NovaCyanBright
                )
                NovaVoiceState.LISTENING -> Triple(
                    NovaCyanGlow.copy(alpha = 0.55f),
                    listOf(NovaCyanBright, NovaCyanGlow, NovaVividBlue, NovaCyanBright),
                    Color.White
                )
                NovaVoiceState.PROCESSING -> Triple(
                    NovaElectricPurple.copy(alpha = 0.5f),
                    listOf(NovaPurpleLight, NovaElectricPurple, NovaVividBlue, NovaCyanGlow),
                    NovaCyanGlow
                )
                NovaVoiceState.SPEAKING -> Triple(
                    NovaElectricPurple.copy(alpha = 0.55f),
                    listOf(NovaPurpleLight, NovaElectricPurple, NovaCyanBright, NovaPurpleLight),
                    Color.White
                )
                NovaVoiceState.ERROR -> Triple(
                    NovaError.copy(alpha = 0.45f),
                    listOf(NovaError, Color(0xFFFB7185), NovaError),
                    Color.White
                )
            }

            // 1. Soft Outermost Atmospheric Radial Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(outerGlowColor, outerGlowColor.copy(alpha = 0.08f), Color.Transparent),
                    center = center,
                    radius = baseRadius * 1.35f
                ),
                radius = baseRadius * 1.35f,
                center = center
            )

            // 2. Secondary Ring Pulse for listening / speaking
            if (state == NovaVoiceState.LISTENING || state == NovaVoiceState.SPEAKING) {
                val dynamicRingRadius = baseRadius * (1.12f + audioLevel * 0.22f)
                drawCircle(
                    color = outerGlowColor.copy(alpha = 0.35f),
                    radius = dynamicRingRadius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // 3. Rotating Gradient Outer Orbit
            rotate(rotationAngle, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(ringGradient, center = center),
                    radius = baseRadius,
                    center = center,
                    style = Stroke(width = 6.dp.toPx())
                )
            }

            // 4. Inner Dark Spherical Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF070B16),
                        Color(0xFF03050B)
                    ),
                    center = center,
                    radius = baseRadius * 0.88f
                ),
                radius = baseRadius * 0.88f,
                center = center
            )

            // 5. Delicate Inner Neon Border
            drawCircle(
                color = outerGlowColor.copy(alpha = 0.6f),
                radius = baseRadius * 0.86f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 6. Central Sound Waveform Bars (reproducing the iconic 5 audio bars in the reference image)
            val barCount = 5
            val maxBarHeight = baseRadius * 0.65f
            val barSpacing = 7.dp.toPx()
            val totalWidth = (barCount - 1) * barSpacing
            val startX = center.x - (totalWidth / 2f)

            for (i in 0 until barCount) {
                val x = startX + i * barSpacing
                val normalizedIdx = (i - 2).toFloat() // -2, -1, 0, 1, 2
                val baseFactor = 1f - (kotlin.math.abs(normalizedIdx) * 0.22f)

                val dynamicHeight = when (state) {
                    NovaVoiceState.IDLE -> {
                        val sine = sin(wavePhase + i * 0.9f)
                        maxBarHeight * (0.35f + 0.18f * sine) * baseFactor
                    }
                    NovaVoiceState.LISTENING -> {
                        val levelMultiplier = (0.35f + audioLevel * 0.95f).coerceIn(0.2f, 1.3f)
                        val sine = sin(wavePhase * 2.5f + i * 1.2f)
                        maxBarHeight * levelMultiplier * (0.5f + 0.5f * kotlin.math.abs(sine))
                    }
                    NovaVoiceState.PROCESSING -> {
                        val sine = sin(wavePhase * 1.5f + i * 0.8f)
                        maxBarHeight * (0.4f + 0.25f * sine) * baseFactor
                    }
                    NovaVoiceState.SPEAKING -> {
                        val sine = sin(wavePhase * 3.2f + i * 1.4f)
                        maxBarHeight * (0.5f + 0.5f * kotlin.math.abs(sine)) * baseFactor
                    }
                    NovaVoiceState.ERROR -> {
                        maxBarHeight * 0.3f * baseFactor
                    }
                }.coerceAtLeast(6.dp.toPx())

                drawLine(
                    color = centerSoundColor,
                    start = Offset(x, center.y - dynamicHeight / 2f),
                    end = Offset(x, center.y + dynamicHeight / 2f),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
