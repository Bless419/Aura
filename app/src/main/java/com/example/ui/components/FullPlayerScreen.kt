package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.Track
import com.example.ui.theme.*
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    track: Track,
    isPlaying: Boolean,
    currentPositionSeconds: Int,
    durationSeconds: Int,
    isShuffleEnabled: Boolean,
    repeatMode: String,
    onCloseClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    onSeek: (Int) -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onEqualizerClick: () -> Unit,
    onAudioQualityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drawableResId = remember(track.drawableResName) {
        context.resources.getIdentifier(track.drawableResName, "drawable", context.packageName)
            .takeIf { it != 0 } ?: R.drawable.ic_launcher_foreground
    }

    var sliderPosition by remember(currentPositionSeconds) { mutableStateOf(currentPositionSeconds.toFloat()) }
    var isUserSeeking by remember { mutableStateOf(false) }

    var volume by remember { mutableStateOf(0.85f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        AuraSurfaceDark,
                        AuraBackgroundDark,
                        Color(0xFF05060A)
                    )
                )
            )
            .padding(horizontal = 24.dp)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onCloseClick) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dismiss",
                        tint = AuraTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NOW PLAYING",
                        style = MaterialTheme.typography.labelMedium,
                        color = AuraNeonCyan,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = track.album,
                        style = MaterialTheme.typography.bodySmall,
                        color = AuraTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row {
                    IconButton(onClick = onEqualizerClick) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Equalizer",
                            tint = AuraNeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = onAudioQualityClick) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Audio Quality Specs",
                            tint = Color(track.format.badgeColorHex),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero Album Art Card with neon border
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .aspectRatio(1f)
                    .shadow(24.dp, shape = RoundedCornerShape(28.dp), spotColor = AuraNeonCyan)
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        2.dp,
                        Brush.linearGradient(
                            listOf(AuraNeonCyan.copy(alpha = 0.6f), AuraElectricViolet.copy(alpha = 0.6f))
                        ),
                        RoundedCornerShape(28.dp)
                    )
                    .background(AuraSurfaceDark)
            ) {
                AsyncImage(
                    model = drawableResId,
                    contentDescription = track.album,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isPlaying) {
                    // Animated glowing audio waveform over lower album art
                    RealtimeWaveVisualizer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .align(Alignment.BottomCenter)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Track info and Favorite Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = AuraTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = AuraTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = Color(track.format.badgeColorHex).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clickable { onAudioQualityClick() }
                        .border(
                            1.dp,
                            Color(track.format.badgeColorHex).copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HighQuality,
                            contentDescription = null,
                            tint = Color(track.format.badgeColorHex),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (track.isHiRes) "24-BIT" else "320K",
                            color = Color(track.format.badgeColorHex),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (track.isFavorite) Color(0xFFFF4081) else AuraTextMuted,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Scrubber Slider
            val activePos = if (isUserSeeking) sliderPosition else currentPositionSeconds.toFloat()

            Slider(
                value = activePos.coerceIn(0f, durationSeconds.toFloat().coerceAtLeast(1f)),
                onValueChange = {
                    isUserSeeking = true
                    sliderPosition = it
                },
                onValueChangeFinished = {
                    isUserSeeking = false
                    onSeek(sliderPosition.toInt())
                },
                valueRange = 0f..durationSeconds.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = AuraNeonCyan,
                    activeTrackColor = AuraNeonCyan,
                    inactiveTrackColor = AuraSurfaceVariantDark
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(activePos.toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = AuraTextSecondary
                )
                Text(
                    text = formatDuration(durationSeconds),
                    style = MaterialTheme.typography.bodySmall,
                    color = AuraTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Shuffle Toggle
                IconButton(onClick = onShuffleToggle) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleEnabled) AuraNeonCyan else AuraTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous
                IconButton(onClick = onPrevClick) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = AuraTextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play / Pause Large FAB
                FloatingActionButton(
                    onClick = onPlayPauseClick,
                    containerColor = AuraNeonCyan,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(12.dp, CircleShape, spotColor = AuraNeonCyan)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next
                IconButton(onClick = onNextClick) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = AuraTextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat Mode Toggle
                IconButton(onClick = onRepeatToggle) {
                    Icon(
                        imageVector = when (repeatMode) {
                            "ONE" -> Icons.Default.RepeatOne
                            "ALL" -> Icons.Default.RepeatOn
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (repeatMode != "OFF") AuraNeonCyan else AuraTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Volume Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeMute,
                    contentDescription = "Mute",
                    tint = AuraTextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    colors = SliderDefaults.colors(
                        thumbColor = AuraElectricViolet,
                        activeTrackColor = AuraElectricViolet,
                        inactiveTrackColor = AuraSurfaceVariantDark
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Max Volume",
                    tint = AuraElectricViolet,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun RealtimeWaveVisualizer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        val path = Path()
        path.moveTo(0f, centerY)

        for (x in 0..width.toInt() step 8) {
            val normalX = x / width
            val wave1 = sin(normalX * 4 * Math.PI + phase) * 16
            val wave2 = sin(normalX * 8 * Math.PI - phase) * 8
            val y = centerY + (wave1 + wave2).toFloat()
            path.lineTo(x.toFloat(), y)
        }

        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                listOf(
                    AuraNeonCyan.copy(alpha = 0.6f),
                    AuraElectricViolet.copy(alpha = 0.2f),
                    Color.Transparent
                )
            )
        )
    }
}
