package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Track
import com.example.ui.components.TrackItemRow
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    tracks: List<Track>,
    currentTrack: Track?,
    isPlaying: Boolean,
    searchQuery: String,
    selectedFilter: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onTrackClick: (Track) -> Unit,
    onFavoriteToggle: (Track) -> Unit,
    onAddToPlaylistClick: (Track) -> Unit,
    onImportFileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val heroBannerId = remember {
        context.resources.getIdentifier("img_hero_banner_1786224700548", "drawable", context.packageName)
            .takeIf { it != 0 } ?: R.drawable.ic_launcher_foreground
    }

    val filteredTracks = remember(tracks, searchQuery, selectedFilter) {
        tracks.filter { track ->
            val matchesQuery = searchQuery.isBlank() ||
                    track.title.contains(searchQuery, ignoreCase = true) ||
                    track.artist.contains(searchQuery, ignoreCase = true) ||
                    track.album.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "HI_RES" -> track.isHiRes
                "FAVORITES" -> track.isFavorite
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraBackgroundDark)
    ) {
        // Hero Section with visual banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Image(
                painter = painterResource(id = heroBannerId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                AuraBackgroundDark.copy(alpha = 0.7f),
                                AuraBackgroundDark
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = AuraNeonCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AURA AUDIO",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = AuraTextPrimary
                    )
                }
                Text(
                    text = "High-Fidelity Offline Master Collection",
                    style = MaterialTheme.typography.bodySmall,
                    color = AuraTextSecondary
                )
            }

            // Import audio file action
            IconButton(
                onClick = onImportFileClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(AuraSurfaceDark.copy(alpha = 0.8f), shape = RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = "Import Local File",
                    tint = AuraNeonCyan
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search title, artist, album...", color = AuraTextMuted) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = AuraNeonCyan)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = AuraTextMuted)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AuraNeonCyan,
                unfocusedBorderColor = AuraCardBorder,
                focusedContainerColor = AuraSurfaceDark,
                unfocusedContainerColor = AuraSurfaceDark
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        val filters = remember {
            listOf(
                "ALL" to "All Tracks",
                "HI_RES" to "Hi-Res Master 24-Bit",
                "FAVORITES" to "Favorites"
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { (filterKey, filterLabel) ->
                val isSelected = selectedFilter == filterKey
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onFilterChange(filterKey) },
                    color = if (isSelected) AuraNeonCyan else AuraSurfaceDark,
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, AuraCardBorder) else null,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = filterLabel,
                        color = if (isSelected) Color.Black else AuraTextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Track List
        if (filteredTracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MusicOff,
                        contentDescription = null,
                        tint = AuraTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "No matching audio tracks found", color = AuraTextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredTracks, key = { it.id }) { track ->
                    val isCurrentTrack = currentTrack?.id == track.id
                    TrackItemRow(
                        track = track,
                        isPlaying = isPlaying,
                        isCurrentTrack = isCurrentTrack,
                        onTrackClick = { onTrackClick(track) },
                        onFavoriteClick = { onFavoriteToggle(track) },
                        onAddToPlaylistClick = { onAddToPlaylistClick(track) }
                    )
                }
            }
        }
    }
}
