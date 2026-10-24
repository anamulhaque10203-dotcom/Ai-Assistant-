package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaCyanGlow
import com.example.ui.theme.NovaElectricPurple
import com.example.ui.theme.NovaPurpleLight
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    modifier: Modifier = Modifier,
    barCount: Int = 28,
    height: Dp = 64.dp,
    isActive: Boolean = true,
    isSpeakingMode: Boolean = false,
    audioLevel: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeakingMode) 900 else 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveformPhase"
    )

    val gradientColors = if (isSpeakingMode) {
        listOf(NovaPurpleLight, NovaElectricPurple, NovaCyanBright, NovaPurpleLight)
    } else {
        listOf(NovaCyanGlow, NovaCyanBright, NovaElectricPurple, NovaCyanGlow)
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val centerY = size.height / 2f
        val step = size.width / (barCount + 1)
        val barStroke = (step * 0.45f).coerceIn(2.5.dp.toPx(), 4.5.dp.toPx())

        for (i in 0 until barCount) {
            val x = step * (i + 1)

            // Bell curve shape from edges towards center
            val normalizedDistFromCenter = kotlin.math.abs(i - barCount / 2f) / (barCount / 2f)
            val bellFactor = (1f - normalizedDistFromCenter * 0.7f).coerceIn(0.2f, 1f)

            val currentHeight = if (isActive) {
                val sine1 = sin(phase * 1.8f + i * 0.45f)
                val sine2 = sin(phase * 3.1f + i * 0.85f)
                val combinedSine = ((sine1 + sine2) / 2f) * 0.5f + 0.5f

                val levelBoost = if (!isSpeakingMode) audioLevel * 0.6f else 0.3f
                size.height * bellFactor * (0.25f + combinedSine * 0.5f + levelBoost)
            } else {
                size.height * 0.12f * bellFactor
            }.coerceIn(4.dp.toPx(), size.height * 0.95f)

            val brush = Brush.verticalGradient(
                colors = gradientColors,
                startY = centerY - currentHeight / 2f,
                endY = centerY + currentHeight / 2f
            )

            drawLine(
                brush = brush,
                start = Offset(x, centerY - currentHeight / 2f),
                end = Offset(x, centerY + currentHeight / 2f),
                strokeWidth = barStroke,
                cap = StrokeCap.Round
            )
        }
    }
}
