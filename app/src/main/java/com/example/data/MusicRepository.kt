package com.example.data

import com.example.model.AudioQualityFormat
import com.example.model.Playlist
import com.example.model.PlaylistTrackCrossRef
import com.example.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class MusicRepository(
    private val trackDao: TrackDao,
    private val playlistDao: PlaylistDao
) {

    val allTracks: Flow<List<Track>> = trackDao.getAllTracks()
    val favoriteTracks: Flow<List<Track>> = trackDao.getFavoriteTracks()
    val playlists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    suspend fun seedInitialDataIfEmpty() {
        val existingTracks = trackDao.getAllTracks().first()
        if (existingTracks.isEmpty()) {
            val initialTracks = listOf(
                Track(
                    id = "track_1",
                    title = "Cybernetic Resonance",
                    artist = "Aura Synthetica",
                    album = "Neo Tokyo Horizon",
                    durationSeconds = 214,
                    format = AudioQualityFormat.FLAC_24BIT,
                    sampleRate = "96 kHz / 24-bit Master",
                    bitrate = "2304 kbps",
                    isHiRes = true,
                    drawableResName = "img_album_art_1_1786224712179",
                    synthFrequencyHz = 432f,
                    synthStyle = "SYNTH_WAVE",
                    isFavorite = true
                ),
                Track(
                    id = "track_2",
                    title = "Midnight Rain Reverie",
                    artist = "Lofi Chill Ensemble",
                    album = "Cozy Midnight Sessions",
                    durationSeconds = 188,
                    format = AudioQualityFormat.WAV_LOSSLESS,
                    sampleRate = "44.1 kHz / 16-bit Uncompressed",
                    bitrate = "1411 kbps",
                    isHiRes = true,
                    drawableResName = "img_album_art_2_1786224723517",
                    synthFrequencyHz = 528f,
                    synthStyle = "LOFI_BEAT",
                    isFavorite = false
                ),
                Track(
                    id = "track_3",
                    title = "Celestial Nebula Symphony",
                    artist = "Orchestral Cosmos",
                    album = "Interstellar Suite",
                    durationSeconds = 265,
                    format = AudioQualityFormat.FLAC_24BIT,
                    sampleRate = "192 kHz / 24-bit Studio",
                    bitrate = "4608 kbps",
                    isHiRes = true,
                    drawableResName = "img_album_art_3_1786224733759",
                    synthFrequencyHz = 396f,
                    synthStyle = "ORCHESTRAL",
                    isFavorite = true
                ),
                Track(
                    id = "track_4",
                    title = "Deep Bass Odyssey",
                    artist = "SubHarmonic Lab",
                    album = "Frequency Modulation",
                    durationSeconds = 195,
                    format = AudioQualityFormat.MP3_320,
                    sampleRate = "48 kHz / 16-bit HD",
                    bitrate = "320 kbps",
                    isHiRes = false,
                    drawableResName = "img_hero_banner_1786224700548",
                    synthFrequencyHz = 220f,
                    synthStyle = "BASS_GROOVE",
                    isFavorite = false
                ),
                Track(
                    id = "track_5",
                    title = "Velvet Evening Chimes",
                    artist = "Ambient Solitude",
                    album = "Pure Acoustic Frequencies",
                    durationSeconds = 230,
                    format = AudioQualityFormat.AAC_HIGH,
                    sampleRate = "44.1 kHz / 16-bit AAC",
                    bitrate = "320 kbps",
                    isHiRes = false,
                    drawableResName = "img_album_art_2_1786224723517",
                    synthFrequencyHz = 639f,
                    synthStyle = "AMBIENT_CHIME",
                    isFavorite = false
                )
            )
            trackDao.insertTracks(initialTracks)

            // Seed initial playlists
            val playlist1Id = playlistDao.insertPlaylist(
                Playlist(
                    name = "Hi-Res Audiophile Master",
                    description = "Lossless 24-bit/96kHz & WAV Master Recordings",
                    iconName = "ic_audiophile"
                )
            )
            val playlist2Id = playlistDao.insertPlaylist(
                Playlist(
                    name = "Late Night Chill & Lofi",
                    description = "Soothing atmospheric beats for deep focus and sleep",
                    iconName = "ic_chill"
                )
            )

            playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlist1Id, "track_1", 0))
            playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlist1Id, "track_3", 1))
            playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlist2Id, "track_2", 0))
            playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlist2Id, "track_5", 1))
        }
    }

    suspend fun toggleFavorite(trackId: String, currentIsFav: Boolean) {
        trackDao.setFavorite(trackId, !currentIsFav)
    }

    suspend fun createPlaylist(name: String, description: String): Long {
        return playlistDao.insertPlaylist(
            Playlist(name = name, description = description)
        )
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: String) {
        playlistDao.addTrackToPlaylist(
            PlaylistTrackCrossRef(playlistId = playlistId, trackId = trackId)
        )
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>> {
        return playlistDao.getTracksForPlaylist(playlistId)
    }

    suspend fun insertImportedTrack(
        title: String,
        artist: String,
        album: String,
        durationSeconds: Int,
        filePath: String
    ) {
        val newTrack = Track(
            id = UUID.randomUUID().toString(),
            title = title,
            artist = artist,
            album = album,
            durationSeconds = durationSeconds,
            format = AudioQualityFormat.WAV_LOSSLESS,
            sampleRate = "44.1 kHz / 16-bit",
            bitrate = "1411 kbps",
            isHiRes = true,
            drawableResName = "img_hero_banner_1786224700548",
            filePath = filePath
        )
        trackDao.insertTracks(listOf(newTrack))
    }
}
