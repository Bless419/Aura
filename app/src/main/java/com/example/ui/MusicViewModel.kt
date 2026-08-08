package com.example.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEngine
import com.example.data.MusicDatabase
import com.example.data.MusicRepository
import com.example.model.Playlist
import com.example.model.Track
import com.example.service.MusicPlaybackService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MusicDatabase.getDatabase(application)
    private val repository = MusicRepository(db.trackDao(), db.playlistDao())
    val audioEngine = AudioEngine(application)

    private var musicService: MusicPlaybackService? = null
    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlaybackService.LocalBinder
            musicService = binder.getService()
            isServiceBound = true
            musicService?.updateTrackState(currentTrack.value, isPlaying.value)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isServiceBound = false
        }
    }

    // UI States
    val allTracks: StateFlow<List<Track>> = repository.allTracks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteTracks: StateFlow<List<Track>> = repository.favoriteTracks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val playlists: StateFlow<List<Playlist>> = repository.playlists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    private val _selectedPlaylistTracks = MutableStateFlow<List<Track>>(emptyList())
    val selectedPlaylistTracks: StateFlow<List<Track>> = _selectedPlaylistTracks.asStateFlow()

    // Active track & playback state
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    val isPlaying: StateFlow<Boolean> = audioEngine.isPlaying
    val currentPositionSeconds: StateFlow<Int> = audioEngine.currentPositionSeconds
    val durationSeconds: StateFlow<Int> = audioEngine.durationSeconds

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow("OFF") // "OFF", "ALL", "ONE"
    val repeatMode: StateFlow<String> = _repeatMode.asStateFlow()

    // Filter & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("ALL") // ALL, HI_RES, FAVORITES, RECENT
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    // Navigation & Sheet Dialogs
    private val _showFullPlayer = MutableStateFlow(false)
    val showFullPlayer: StateFlow<Boolean> = _showFullPlayer.asStateFlow()

    private val _showEqualizer = MutableStateFlow(false)
    val showEqualizer: StateFlow<Boolean> = _showEqualizer.asStateFlow()

    private val _showAudioDetailsSheet = MutableStateFlow(false)
    val showAudioDetailsSheet: StateFlow<Boolean> = _showAudioDetailsSheet.asStateFlow()

    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()

    private val _addToPlaylistTrack = MutableStateFlow<Track?>(null)
    val addToPlaylistTrack: StateFlow<Track?> = _addToPlaylistTrack.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        // Start & bind background service
        val context = getApplication<Application>()
        val serviceIntent = Intent(context, MusicPlaybackService::class.java)
        try {
            context.startService(serviceIntent)
            context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Keep service notification synchronized with current track and playing state
        viewModelScope.launch {
            combine(currentTrack, isPlaying) { track, playing -> Pair(track, playing) }
                .collect { (track, playing) ->
                    musicService?.updateTrackState(track, playing)
                }
        }
    }

    fun playTrack(track: Track, newQueue: List<Track> = allTracks.value) {
        _currentTrack.value = track
        _queue.value = newQueue
        audioEngine.playTrack(track) {
            handleTrackCompletion()
        }
    }

    fun togglePlayPause() {
        if (_currentTrack.value == null && allTracks.value.isNotEmpty()) {
            playTrack(allTracks.value.first())
            return
        }

        if (isPlaying.value) {
            audioEngine.pause()
        } else {
            audioEngine.resume()
        }
    }

    fun seekTo(seconds: Int) {
        audioEngine.seekTo(seconds)
    }

    fun playNextTrack() {
        val current = _currentTrack.value ?: return
        val currentList = _queue.value.ifEmpty { allTracks.value }
        if (currentList.isEmpty()) return

        if (_isShuffleEnabled.value) {
            val randomIndex = (currentList.indices).random()
            playTrack(currentList[randomIndex], currentList)
            return
        }

        val currentIndex = currentList.indexOfFirst { it.id == current.id }
        val nextIndex = if (currentIndex != -1 && currentIndex < currentList.size - 1) {
            currentIndex + 1
        } else {
            0 // Loop back
        }
        playTrack(currentList[nextIndex], currentList)
    }

    fun playPreviousTrack() {
        val current = _currentTrack.value ?: return
        val currentList = _queue.value.ifEmpty { allTracks.value }
        if (currentList.isEmpty()) return

        val currentIndex = currentList.indexOfFirst { it.id == current.id }
        val prevIndex = if (currentIndex > 0) {
            currentIndex - 1
        } else {
            currentList.size - 1
        }
        playTrack(currentList[prevIndex], currentList)
    }

    private fun handleTrackCompletion() {
        when (_repeatMode.value) {
            "ONE" -> {
                _currentTrack.value?.let { playTrack(it, _queue.value) }
            }
            "ALL", "OFF" -> {
                playNextTrack()
            }
        }
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            "OFF" -> "ALL"
            "ALL" -> "ONE"
            else -> "OFF"
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            repository.toggleFavorite(track.id, track.isFavorite)
            if (_currentTrack.value?.id == track.id) {
                _currentTrack.value = _currentTrack.value?.copy(isFavorite = !track.isFavorite)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun selectPlaylist(playlist: Playlist?) {
        _selectedPlaylist.value = playlist
        if (playlist != null) {
            viewModelScope.launch {
                repository.getTracksForPlaylist(playlist.id).collect { tracks ->
                    _selectedPlaylistTracks.value = tracks
                }
            }
        } else {
            _selectedPlaylistTracks.value = emptyList()
        }
    }

    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch {
            repository.createPlaylist(name, description)
            _showCreatePlaylistDialog.value = false
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
            if (_selectedPlaylist.value?.id == playlist.id) {
                _selectedPlaylist.value = null
            }
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
            _addToPlaylistTrack.value = null
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun importOfflineFile(title: String, artist: String, path: String) {
        viewModelScope.launch {
            repository.insertImportedTrack(title, artist, "Imported Music", 180, path)
        }
    }

    fun setShowFullPlayer(show: Boolean) {
        _showFullPlayer.value = show
    }

    fun setShowEqualizer(show: Boolean) {
        _showEqualizer.value = show
    }

    fun setShowAudioDetailsSheet(show: Boolean) {
        _showAudioDetailsSheet.value = show
    }

    fun setShowCreatePlaylistDialog(show: Boolean) {
        _showCreatePlaylistDialog.value = show
    }

    fun setAddToPlaylistTrack(track: Track?) {
        _addToPlaylistTrack.value = track
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stop()
        if (isServiceBound) {
            try {
                getApplication<Application>().unbindService(serviceConnection)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
