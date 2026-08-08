package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPreset by remember { mutableStateOf("Bass Boost") }
    var bassBoost by remember { mutableFloatStateOf(65f) }
    var surround3D by remember { mutableFloatStateOf(40f) }

    val bandFrequencies = remember { listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz") }
    var bandGains by remember { mutableStateOf(floatArrayOf(3f, 5f, 2f, 4f, 6f)) }

    val presets = remember { listOf("Flat", "Bass Boost", "Electronic", "Acoustic", "Rock", "Vocal") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AuraSurfaceDark,
        contentColor = AuraTextPrimary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = null,
                        tint = AuraNeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "5-Band Audio Equalizer",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = AuraTextPrimary
                    )
                }

                TextButton(onClick = {
                    bandGains = floatArrayOf(0f, 0f, 0f, 0f, 0f)
                    bassBoost = 0f
                    surround3D = 0f
                    selectedPreset = "Flat"
                }) {
                    Text("Reset", color = AuraNeonCyan)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets Horizontal Row
            Text(text = "EQ PRESETS", style = MaterialTheme.typography.labelSmall, color = AuraTextMuted)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets) { preset ->
                    val isSelected = preset == selectedPreset
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                selectedPreset = preset
                                when (preset) {
                                    "Flat" -> bandGains = floatArrayOf(0f, 0f, 0f, 0f, 0f)
                                    "Bass Boost" -> bandGains = floatArrayOf(6f, 4f, 1f, 2f, 3f)
                                    "Electronic" -> bandGains = floatArrayOf(5f, 3f, 0f, 4f, 6f)
                                    "Acoustic" -> bandGains = floatArrayOf(2f, 3f, 4f, 3f, 2f)
                                    "Rock" -> bandGains = floatArrayOf(5f, 3f, -1f, 3f, 5f)
                                    "Vocal" -> bandGains = floatArrayOf(-2f, 1f, 5f, 3f, 0f)
                                }
                            },
                        color = if (isSelected) AuraNeonCyan else AuraSurfaceVariantDark,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = preset,
                            color = if (isSelected) Color.Black else AuraTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5 Band Frequencies Sliders
            Text(text = "FREQUENCY GAINS (dB)", style = MaterialTheme.typography.labelSmall, color = AuraTextMuted)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                bandFrequencies.forEachIndexed { index, freq ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${bandGains[index].toInt()}dB",
                            fontSize = 11.sp,
                            color = AuraNeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = bandGains[index],
                            onValueChange = { newVal ->
                                val updated = bandGains.copyOf()
                                updated[index] = newVal
                                bandGains = updated
                            },
                            valueRange = -6f..12f,
                            colors = SliderDefaults.colors(
                                thumbColor = AuraNeonCyan,
                                activeTrackColor = AuraNeonCyan,
                                inactiveTrackColor = AuraSurfaceVariantDark
                            ),
                            modifier = Modifier.height(120.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = freq,
                            fontSize = 11.sp,
                            color = AuraTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bass Boost & 3D Surround Effects
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Bass Boost", style = MaterialTheme.typography.bodyMedium, color = AuraTextPrimary)
                    Slider(
                        value = bassBoost,
                        onValueChange = { bassBoost = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = AuraElectricViolet,
                            activeTrackColor = AuraElectricViolet
                        )
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "3D Surround", style = MaterialTheme.typography.bodyMedium, color = AuraTextPrimary)
                    Slider(
                        value = surround3D,
                        onValueChange = { surround3D = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = AuraGoldHiRes,
                            activeTrackColor = AuraGoldHiRes
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioQualitySheet(
    track: Track,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AuraSurfaceDark,
        contentColor = AuraTextPrimary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = Color(track.format.badgeColorHex),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "High-Quality Audio Specs",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AuraTextPrimary
                    )
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = AuraTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Spec Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpecRow("Audio Format", track.format.displayName, Color(track.format.badgeColorHex))
                SpecRow("Sample Rate", track.sampleRate, AuraTextPrimary)
                SpecRow("Bitrate", track.bitrate, AuraTextPrimary)
                SpecRow("Hi-Res Master", if (track.isHiRes) "Certified 24-bit Lossless" else "High Definition Compressed", AuraGoldHiRes)
                SpecRow("DAC Output", "Direct PCM 16-bit / 24-bit Passthrough", AuraNeonCyan)
                SpecRow("Audio Source", if (!track.filePath.isNullOrEmpty()) "Device Storage File" else "Aura Hi-Res Offline Master", AuraTextSecondary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AuraNeonCyan, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Close Specs", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SpecRow(label: String, value: String, valueColor: Color) {
    Surface(
        color = AuraSurfaceVariantDark,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = AuraTextSecondary)
            Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = valueColor)
        }
    }
}
