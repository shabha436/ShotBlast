package com.example.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkCyan as ShotblastCyan
import com.example.ui.theme.DarkCoral as ShotblastCoral
import com.example.ui.theme.DarkCyanDark as ShotblastCyanDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-octane Anime Intro Screen shown when opening Shotblast.
 * Features speed lines, energy shockwaves, glowing cyber logo impact,
 * Japanese anime styling, and smooth transition into the main application.
 */
@Composable
fun AnimeIntroScreen(
    onIntroFinished: () -> Unit
) {
    val view = LocalView.current
    BackHandler {
        onIntroFinished()
    }

    // Animation progress states
    val logoScale = remember { Animatable(0.1f) }
    val logoAlpha = remember { Animatable(0f) }
    val flashAlpha = remember { Animatable(0.9f) }
    val slashProgress = remember { Animatable(0f) }
    val ringScale = remember { Animatable(0.4f) }
    val ringAlpha = remember { Animatable(0.8f) }
    val textAlpha = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }

    var isFinished by remember { mutableStateOf(false) }

    fun finishIntro() {
        if (!isFinished) {
            isFinished = true
            try {
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            } catch (_: Exception) {}
            onIntroFinished()
        }
    }

    // Infinite breathing and particle transitions
    val infiniteTransition = rememberInfiniteTransition(label = "anime_infinite")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )
    val speedLinesRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Sequence Choreography
    LaunchedEffect(Unit) {
        // Play subtle futuristic chime
        launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 40)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
                delay(300)
                toneGen.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 220)
                delay(200)
                toneGen.release()
            } catch (_: Exception) {
                // Audio synthesis optional
            }
        }

        // Haptic feedback
        try {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        } catch (_: Exception) {}

        // Initial flash fade out
        launch {
            flashAlpha.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        }

        // Katana energy slash sweep
        launch {
            slashProgress.animateTo(1f, animationSpec = tween(550, easing = FastOutSlowInEasing))
        }

        // Logo impact burst
        launch {
            delay(150)
            logoAlpha.animateTo(1f, animationSpec = tween(250))
        }
        launch {
            delay(150)
            logoScale.animateTo(
                targetValue = 1.05f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            logoScale.animateTo(1.0f, animationSpec = tween(180))
        }

        // Shockwave rings
        launch {
            delay(250)
            ringScale.animateTo(2.4f, animationSpec = tween(900, easing = FastOutSlowInEasing))
        }
        launch {
            delay(250)
            ringAlpha.animateTo(0f, animationSpec = tween(900, easing = FastOutSlowInEasing))
        }

        // Text reveal
        launch {
            delay(400)
            textAlpha.animateTo(1f, animationSpec = tween(500))
        }

        // Overall progress timer (3 seconds)
        launch {
            progress.animateTo(1f, animationSpec = tween(3100, easing = LinearEasing))
            finishIntro()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060911))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                finishIntro()
            }
            .testTag("anime_intro_container"),
        contentAlignment = Alignment.Center
    ) {
        // 1. Anime Speed Lines & Energy Rays Background
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(speedLinesRotation)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = kotlin.math.max(size.width, size.height) * 0.9f
            val lineCount = 42

            for (i in 0 until lineCount) {
                val angle = (i * (360f / lineCount) + (i % 3) * 2.5f) * (PI.toFloat() / 180f)
                val innerDist = 120f + (i % 5) * 25f
                val outerDist = maxRadius * (0.6f + (i % 4) * 0.12f)

                val start = Offset(
                    x = center.x + innerDist * cos(angle),
                    y = center.y + innerDist * sin(angle)
                )
                val end = Offset(
                    x = center.x + outerDist * cos(angle),
                    y = center.y + outerDist * sin(angle)
                )

                val isAccent = i % 4 == 0
                val lineColor = if (isAccent) {
                    ShotblastCyan.copy(alpha = 0.22f)
                } else if (i % 3 == 0) {
                    ShotblastCoral.copy(alpha = 0.15f)
                } else {
                    Color.White.copy(alpha = 0.08f)
                }

                drawLine(
                    color = lineColor,
                    start = start,
                    end = end,
                    strokeWidth = if (isAccent) 3.5f else 1.8f,
                    cap = StrokeCap.Round
                )
            }
        }

        // 2. Radial Cyber Glow
        Box(
            modifier = Modifier
                .size(340.dp)
                .scale(pulseGlow)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ShotblastCyan.copy(alpha = 0.28f),
                            ShotblastCyanDark.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // 3. Shockwave Expanding Rings
        Canvas(
            modifier = Modifier.size(280.dp)
        ) {
            if (ringAlpha.value > 0f) {
                drawCircle(
                    color = ShotblastCyan.copy(alpha = ringAlpha.value),
                    radius = (size.minDimension / 2f) * ringScale.value,
                    style = Stroke(width = 4f)
                )
                drawCircle(
                    color = ShotblastCoral.copy(alpha = ringAlpha.value * 0.6f),
                    radius = (size.minDimension / 2.3f) * ringScale.value,
                    style = Stroke(width = 2f)
                )
            }
        }

        // 4. Center Main Content (App Icon Badge + Anime Titles)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Anime Studio / Badge Header
            Text(
                text = "⚡ SHOTBLAST ANIMATION STUDIOS PRESENTS ⚡",
                color = ShotblastCyan.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .padding(bottom = 18.dp)
            )

            // The App Icon Logo with glowing border and spring animation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            ) {
                // Neon back glow
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(ShotblastCyan.copy(alpha = 0.35f))
                        .blur(18.dp)
                )

                // The actual uploaded App Icon image
                Image(
                    painter = painterResource(id = R.drawable.ic_shotblast_logo),
                    contentDescription = "Shotblast App Icon",
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .border(
                            width = 2.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(ShotblastCyan, ShotblastCoral, ShotblastCyan)
                            ),
                            shape = RoundedCornerShape(30.dp)
                        )
                        .shadow(16.dp, RoundedCornerShape(30.dp))
                        .testTag("anime_intro_app_icon")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // English Subtitle Header
            Text(
                text = "NEXT-GEN STREAMING",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Main Cyber Title
            Text(
                text = "SHOTBLAST",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 6.sp,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .testTag("anime_intro_title")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cyber Badge: Unleash the stream
            Surface(
                color = Color(0x33101625),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(listOf(ShotblastCyan, ShotblastCoral))
                ),
                modifier = Modifier.alpha(textAlpha.value)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(ShotblastCyan, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "UNLEASH THE STREAM • BEYOND LIMITS",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }

        // 5. Diagonal Anime Energy Slash Sweep
        if (slashProgress.value > 0f && slashProgress.value < 1f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val p = slashProgress.value
                val startX = -100f + (size.width + 200f) * p
                val startY = -100f + (size.height + 200f) * p

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            ShotblastCyan,
                            Color.White,
                            ShotblastCoral,
                            Color.Transparent
                        )
                    ),
                    start = Offset(startX - 200f, startY + 200f),
                    end = Offset(startX + 200f, startY - 200f),
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )
            }
        }

        // 6. Flash Overlay
        if (flashAlpha.value > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(flashAlpha.value)
                    .background(Color.White)
            )
        }

        // 7. Top Right Skip Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 44.dp, end = 20.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Surface(
                onClick = { finishIntro() },
                shape = RoundedCornerShape(20.dp),
                color = Color(0x66000000),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier.testTag("skip_intro_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "SKIP INTRO",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Skip Intro",
                        tint = ShotblastCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 8. Bottom Progress Indicator & Hint
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 36.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tap anywhere to begin",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = ShotblastCyan,
                trackColor = Color.White.copy(alpha = 0.12f)
            )
        }
    }
}
