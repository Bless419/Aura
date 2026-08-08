package com.example.data

import androidx.room.TypeConverter
import com.example.model.AudioQualityFormat

class Converters {
    @TypeConverter
    fun fromAudioQualityFormat(value: AudioQualityFormat): String {
        return value.name
    }

    @TypeConverter
    fun toAudioQualityFormat(value: String): AudioQualityFormat {
        return try {
            AudioQualityFormat.valueOf(value)
        } catch (e: Exception) {
            AudioQualityFormat.FLAC_24BIT
        }
    }
}
