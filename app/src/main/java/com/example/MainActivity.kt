package com.example

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Playlist
import com.example.ui.MusicViewModel
import com.example.ui.components.*
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.theme.AuraBackgroundDark
import com.example.ui.theme.AuraMusicTheme
import com.example.ui.theme.AuraNeonCyan
import com.example.ui.theme.AuraSurfaceDark

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AuraMusicTheme {
                val viewModel: MusicViewModel = viewModel()

                // File picker launcher for importing device music
                val filePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        val fileName = getFileName(it) ?: "Imported Track"
                        viewModel.importOfflineFile(
                            title = fileName.substringBeforeLast("."),
                            artist = "Local Artist",
                            path = it.toString()
                        )
                        Toast.makeText(this, "Imported: $fileName", Toast.LENGTH_SHORT).show()
                    }
                }

                AuraMusicAppContent(
                    viewModel = viewModel,
                    onImportFile = { filePickerLauncher.launch("audio/*") }
                )
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}

@Composable
fun AuraMusicAppContent(
    viewModel: MusicViewModel,
    onImportFile: () -> Unit
) {
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val selectedPlaylistTracks by viewModel.selectedPlaylistTracks.collectAsStateWithLifecycle()

    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPositionSeconds by viewModel.currentPositionSeconds.collectAsStateWithLifecycle()
    val durationSeconds by viewModel.durationSeconds.collectAsStateWithLifecycle()

    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    val showFullPlayer by viewModel.showFullPlayer.collectAsStateWithLifecycle()
    val showEqualizer by viewModel.showEqualizer.collectAsStateWithLifecycle()
    val showAudioDetailsSheet by viewModel.showAudioDetailsSheet.collectAsStateWithLifecycle()
    val showCreatePlaylistDialog by viewModel.showCreatePlaylistDialog.collectAsStateWithLifecycle()
    val addToPlaylistTrack by viewModel.addToPlaylistTrack.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Library, 1: Playlists

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraBackgroundDark)
    ) {
        Scaffold(
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AuraSurfaceDark)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Sticky Mini Player Bar
                    currentTrack?.let { track ->
                        NowPlayingBar(
                            track = track,
                            isPlaying = isPlaying,
                            currentPositionSeconds = currentPositionSeconds,
                            durationSeconds = durationSeconds,
                            onBarClick = { viewModel.setShowFullPlayer(true) },
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onNextClick = { viewModel.playNextTrack() }
                        )
                    }

                    // Navigation Bar
                    NavigationBar(
                        containerColor = AuraSurfaceDark,
                        contentColor = AuraNeonCyan,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = activeTab == 0 && selectedPlaylist == null,
                            onClick = {
                                viewModel.selectPlaylist(null)
                                activeTab = 0
                            },
                            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
                            label = { Text("Library") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AuraNeonCyan,
                                selectedTextColor = AuraNeonCyan,
                                indicatorColor = AuraNeonCyan.copy(alpha = 0.15f)
                            )
                        )

                        NavigationBarItem(
                            selected = activeTab == 1 || selectedPlaylist != null,
                            onClick = { activeTab = 1 },
                            icon = { Icon(Icons.Default.QueueMusic, contentDescription = "Playlists") },
                            label = { Text("Playlists") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AuraNeonCyan,
                                selectedTextColor = AuraNeonCyan,
                                indicatorColor = AuraNeonCyan.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            },
            containerColor = AuraBackgroundDark
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (selectedPlaylist != null) {
                    PlaylistDetailScreen(
                        playlist = selectedPlaylist!!,
                        tracks = selectedPlaylistTracks,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        onBackClick = { viewModel.selectPlaylist(null) },
                        onPlayAllClick = {
                            if (selectedPlaylistTracks.isNotEmpty()) {
                                viewModel.playTrack(selectedPlaylistTracks.first(), selectedPlaylistTracks)
                            }
                        },
                        onTrackClick = { track -> viewModel.playTrack(track, selectedPlaylistTracks) },
                        onFavoriteToggle = { track -> viewModel.toggleFavorite(track) },
                        onRemoveFromPlaylistClick = { track ->
                            selectedPlaylist?.let { pl ->
                                viewModel.removeTrackFromPlaylist(pl.id, track.id)
                            }
                        },
                        onDeletePlaylistClick = {
                            selectedPlaylist?.let { pl ->
                                viewModel.deletePlaylist(pl)
                            }
                        }
                    )
                } else {
                    when (activeTab) {
                        0 -> LibraryScreen(
                            tracks = allTracks,
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            searchQuery = searchQuery,
                            selectedFilter = selectedFilter,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onFilterChange = { viewModel.setFilter(it) },
                            onTrackClick = { track -> viewModel.playTrack(track, allTracks) },
                            onFavoriteToggle = { track -> viewModel.toggleFavorite(track) },
                            onAddToPlaylistClick = { track -> viewModel.setAddToPlaylistTrack(track) },
                            onImportFileClick = onImportFile
                        )
                        1 -> PlaylistsScreen(
                            playlists = playlists,
                            onPlaylistClick = { playlist -> viewModel.selectPlaylist(playlist) },
                            onCreatePlaylistClick = { viewModel.setShowCreatePlaylistDialog(true) }
                        )
                    }
                }
            }
        }

        // Full Screen Overlay Player with Slide Transition
        AnimatedVisibility(
            visible = showFullPlayer && currentTrack != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            currentTrack?.let { track ->
                FullPlayerScreen(
                    track = track,
                    isPlaying = isPlaying,
                    currentPositionSeconds = currentPositionSeconds,
                    durationSeconds = durationSeconds,
                    isShuffleEnabled = isShuffleEnabled,
                    repeatMode = repeatMode,
                    onCloseClick = { viewModel.setShowFullPlayer(false) },
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onNextClick = { viewModel.playNextTrack() },
                    onPrevClick = { viewModel.playPreviousTrack() },
                    onSeek = { seconds -> viewModel.seekTo(seconds) },
                    onShuffleToggle = { viewModel.toggleShuffle() },
                    onRepeatToggle = { viewModel.toggleRepeat() },
                    onFavoriteToggle = { viewModel.toggleFavorite(track) },
                    onEqualizerClick = { viewModel.setShowEqualizer(true) },
                    onAudioQualityClick = { viewModel.setShowAudioDetailsSheet(true) }
                )
            }
        }

        // Equalizer Bottom Sheet
        if (showEqualizer) {
            EqualizerBottomSheet(
                onDismiss = { viewModel.setShowEqualizer(false) }
            )
        }

        // Audio Specs Sheet
        if (showAudioDetailsSheet && currentTrack != null) {
            AudioQualitySheet(
                track = currentTrack!!,
                onDismiss = { viewModel.setShowAudioDetailsSheet(false) }
            )
        }

        // Create Playlist Dialog
        if (showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                onDismiss = { viewModel.setShowCreatePlaylistDialog(false) },
                onCreate = { name, desc ->
                    viewModel.createPlaylist(name, desc)
                }
            )
        }

        // Add to Playlist Dialog
        addToPlaylistTrack?.let { track ->
            AddToPlaylistDialog(
                track = track,
                playlists = playlists,
                onDismiss = { viewModel.setAddToPlaylistTrack(null) },
                onPlaylistSelect = { playlistId ->
                    viewModel.addTrackToPlaylist(playlistId, track.id)
                },
                onCreateNewPlaylistClick = {
                    viewModel.setShowCreatePlaylistDialog(true)
                }
            )
        }
    }
}

