package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.sin

class AudioEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var playbackJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioTrack: AudioTrack? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionSeconds = MutableStateFlow(0)
    val currentPositionSeconds: StateFlow<Int> = _currentPositionSeconds.asStateFlow()

    private val _durationSeconds = MutableStateFlow(0)
    val durationSeconds: StateFlow<Int> = _durationSeconds.asStateFlow()

    private var currentTrack: Track? = null
    private var isLooping = false
    private var volumeLevel = 1.0f

    // Equalizer parameters
    var bassBoostPercent = 50f
    var surroundPercent = 30f
    var eqBandGains = floatArrayOf(0f, 2f, 4f, 1f, 3f) // 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz

    fun playTrack(track: Track, onCompleted: () -> Unit) {
        stop()
        currentTrack = track
        _durationSeconds.value = track.durationSeconds
        _currentPositionSeconds.value = 0
        _isPlaying.value = true

        if (!track.filePath.isNullOrEmpty() && File(track.filePath).exists()) {
            playLocalFile(track, onCompleted)
        } else {
            playSynthesizedAudio(track, onCompleted)
        }
    }

    private fun playLocalFile(track: Track, onCompleted: () -> Unit) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(track.filePath))
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnCompletionListener {
                    _isPlaying.value = false
                    onCompleted()
                }
                prepare()
                setVolume(volumeLevel, volumeLevel)
                start()
            }
            startPositionTracker()
        } catch (e: Exception) {
            Log.e("AudioEngine", "Failed to play local file, fallback to synth", e)
            playSynthesizedAudio(track, onCompleted)
        }
    }

    private fun playSynthesizedAudio(track: Track, onCompleted: () -> Unit) {
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_OUT_STEREO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(audioFormat)
            .setChannelMask(channelConfig)
            .build()

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(audioAttributes)
            .setAudioFormat(format)
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.setVolume(volumeLevel)
        audioTrack?.play()

        playbackJob = scope.launch {
            val baseFreq = track.synthFrequencyHz
            val totalSamples = track.durationSeconds * sampleRate
            val pcmBuffer = ShortArray(1024)
            var sampleIndex = 0

            while (isActive && sampleIndex < totalSamples && _isPlaying.value) {
                for (i in 0 until pcmBuffer.size step 2) {
                    val t = sampleIndex.toDouble() / sampleRate
                    val freq = baseFreq + (sin(t * 0.5) * 20)
                    
                    // Multi-harmonic musical synthesis with beat pulses
                    val melody = sin(2.0 * Math.PI * freq * t) * 0.4
                    val harmony = sin(2.0 * Math.PI * (freq * 1.5) * t) * 0.25
                    val subBass = sin(2.0 * Math.PI * (freq * 0.5) * t) * (0.3 + (bassBoostPercent / 100f) * 0.2)
                    
                    // Rhythm beat accent
                    val beatTime = t % 0.5
                    val beatEnvelope = Math.exp(-beatTime * 10.0)
                    val beatPulse = sin(2.0 * Math.PI * 60.0 * beatTime) * beatEnvelope * 0.3

                    val rawSample = (melody + harmony + subBass + beatPulse) * 0.6
                    val clampedSample = rawSample.coerceIn(-1.0, 1.0)
                    val pcmValue = (clampedSample * Short.MAX_VALUE).toInt().toShort()

                    pcmBuffer[i] = pcmValue // Left channel
                    pcmBuffer[i + 1] = (pcmValue * 0.95).toInt().toShort() // Right channel with slight stereo spatial width

                    sampleIndex++
                }

                audioTrack?.write(pcmBuffer, 0, pcmBuffer.size)
                _currentPositionSeconds.value = (sampleIndex / sampleRate)

                // Sleep slightly to throttle PCM buffer feeding
                val millisPerBuffer = (1024 / 2.0 / sampleRate * 1000).toLong()
                kotlinx.coroutines.delay(millisPerBuffer)
            }

            if (isActive && sampleIndex >= totalSamples) {
                _isPlaying.value = false
                _currentPositionSeconds.value = track.durationSeconds
                launch(Dispatchers.Main) { onCompleted() }
            }
        }
    }

    private fun startPositionTracker() {
        scope.launch {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _currentPositionSeconds.value = mp.currentPosition / 1000
                    }
                }
                kotlinx.coroutines.delay(500)
            }
        }
    }

    fun pause() {
        _isPlaying.value = false
        mediaPlayer?.pause()
        audioTrack?.pause()
    }

    fun resume() {
        if (currentTrack != null) {
            _isPlaying.value = true
            mediaPlayer?.start()
            audioTrack?.play()
        }
    }

    fun seekTo(seconds: Int) {
        _currentPositionSeconds.value = seconds
        mediaPlayer?.seekTo(seconds * 1000)
    }

    fun stop() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error stopping MediaPlayer", e)
        }

        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error stopping AudioTrack", e)
        }
    }

    fun setVolume(vol: Float) {
        volumeLevel = vol
        mediaPlayer?.setVolume(vol, vol)
        audioTrack?.setVolume(vol)
    }
}
