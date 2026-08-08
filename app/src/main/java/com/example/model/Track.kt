package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AudioQualityFormat(val displayName: String, val badgeColorHex: Long) {
    FLAC_24BIT("FLAC 24-bit/96kHz", 0xFFFFD700), // Gold Master
    WAV_LOSSLESS("WAV 16-bit/44.1kHz", 0xFF00E5FF), // Cyan Lossless
    MP3_320("MP3 320kbps HD", 0xFF9D4EDD), // Violet HD
    AAC_HIGH("AAC 320kbps", 0xFF43A047) // Green High Quality
}

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val format: AudioQualityFormat,
    val sampleRate: String = "96 kHz / 24-bit",
    val bitrate: String = "2304 kbps",
    val isHiRes: Boolean = true,
    val drawableResName: String,
    val synthFrequencyHz: Float = 440f,
    val synthStyle: String = "AMBIENT_CHIME", // AMBIENT_CHIME, BASS_GROOVE, SYNTH_WAVE, LOFI_BEAT, ORCHESTRAL
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val filePath: String? = null // For imported offline files from device storage
)
