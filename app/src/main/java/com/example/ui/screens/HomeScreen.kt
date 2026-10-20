package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.VideoRepository
import com.example.model.VideoItem
import com.example.ui.theme.ShotblastBackground
import com.example.ui.theme.ShotblastBorder
import com.example.ui.theme.ShotblastCoral
import com.example.ui.theme.ShotblastCyan
import com.example.ui.theme.ShotblastSurface
import com.example.ui.theme.ShotblastSurfaceVariant
import com.example.ui.theme.ShotblastTextPrimary
import com.example.ui.theme.ShotblastTextSecondary
import com.example.ui.theme.ThemeManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AuthManager

@Composable
fun HomeScreen(
    videos: List<VideoItem>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onShortsClick: (VideoItem) -> Unit,
    onChannelClick: (String) -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onVideoOptionsClick: (VideoItem) -> Unit,
    onSearchClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkTheme by ThemeManager.isDarkTheme.collectAsStateWithLifecycle()
    val currentUser by AuthManager.currentUser.collectAsStateWithLifecycle()
    val userProfile by AuthManager.userProfile.collectAsStateWithLifecycle()

    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf("All", "Gaming", "Music", "Movies", "Travel", "Nature", "Anime", "Shorts")

    // Filter videos according to selected category and search
    val filteredVideos = remember(videos, selectedCategory, searchQuery) {
        videos.filter { video ->
            val matchesCategory = if (selectedCategory == "All") true else {
                if (selectedCategory == "Shorts") video.isShort
                else video.category.equals(selectedCategory, ignoreCase = true)
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                video.title.contains(searchQuery, ignoreCase = true) ||
                        video.channelName.contains(searchQuery, ignoreCase = true) ||
                        video.tags.any { it.contains(searchQuery, ignoreCase = true) }
            }
            matchesCategory && matchesSearch
        }
    }

    val regularVideos = filteredVideos.filter { !it.isShort }
    val shortsVideos = videos.filter { it.isShort }
    val featuredVideo = regularVideos.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ShotblastBackground)
    ) {
        // App Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Wordmark (Circular cyan play icon + SHOTBLAST)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("app_brand_header")
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .border(2.dp, ShotblastCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Shotblast logo",
                        tint = ShotblastCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SHOT",
                        color = ShotblastTextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        text = "BLAST",
                        color = ShotblastCyan,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            // Top action icons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Quick Theme Toggle (Dark / Light Mode)
                IconButton(
                    onClick = { ThemeManager.toggleTheme(context) },
                    modifier = Modifier.testTag("top_bar_theme_toggle")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                        tint = ShotblastCyan
                    )
                }

                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.testTag("search_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = ShotblastTextPrimary
                    )
                }

                // Notification Bell with unread indicator badge
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.testTag("notification_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = ShotblastTextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp, end = 8.dp)
                            .size(8.dp)
                            .background(ShotblastCoral, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Profile Avatar with dynamic Channel Initial / Person icon
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, ShotblastCyan, CircleShape)
                        .background(ShotblastSurfaceVariant)
                        .clickable(onClick = onProfileClick)
                        .testTag("top_bar_profile_button"),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = userProfile?.channelName?.firstOrNull()?.uppercaseChar()
                    if (initial != null) {
                        Text(
                            text = initial.toString(),
                            color = ShotblastCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = ShotblastCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Expandable Search Bar
        AnimatedVisibility(visible = isSearchExpanded) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search videos, channels, or tags...", color = Color.Gray, fontSize = 13.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = ShotblastCyan)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShotblastCyan,
                    unfocusedBorderColor = ShotblastBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .height(50.dp)
                    .testTag("search_text_input")
            )
        }

        // Category Chips Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) ShotblastCyan else ShotblastSurfaceVariant,
                    modifier = Modifier
                        .clickable { onCategorySelected(category) }
                        .testTag("category_chip_$category")
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) (if (isDarkTheme) Color.Black else Color.White) else ShotblastTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // Feed Content
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Creator Channel Onboarding Banner
            if (currentUser == null || userProfile?.isChannelCreated != true) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .clickable { onProfileClick() }
                            .testTag("creator_channel_banner"),
                        colors = CardDefaults.cardColors(containerColor = ShotblastSurfaceVariant),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShotblastCyan.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(ShotblastCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SmartDisplay,
                                    contentDescription = null,
                                    tint = ShotblastCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Create Your Channel",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Sign in & set up your channel name to upload to Firebase",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = onProfileClick,
                                colors = ButtonDefaults.buttonColors(containerColor = ShotblastCyan),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = "Start",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .clickable { onProfileClick() },
                        color = ShotblastSurfaceVariant.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(ShotblastCyan, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Channel: ${userProfile?.channelName ?: "My Channel"} (${userProfile?.channelHandle ?: "@creator"})",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "Firebase Sync",
                                color = ShotblastCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Featured Big Video Card (if any)
            if (featuredVideo != null) {
                item {
                    FeaturedVideoCard(
                        video = featuredVideo,
                        onClick = { onVideoClick(featuredVideo) },
                        onChannelClick = { onChannelClick(featuredVideo.channelName) },
                        onOptionsClick = { onVideoOptionsClick(featuredVideo) }
                    )
                }
            }

            // Shorts Shelf Section (Matching Mockup)
            if (shortsVideos.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(ShotblastCoral, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Bolt,
                                        contentDescription = "Shorts",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Shorts",
                                    color = ShotblastTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "View all",
                                color = ShotblastCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable { onCategorySelected("Shorts") }
                                    .testTag("view_all_shorts_button")
                            )
                        }

                        // Horizontal carousel of vertical cards
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(shortsVideos) { short ->
                                ShortVideoCard(
                                    video = short,
                                    onClick = { onShortsClick(short) }
                                )
                            }
                        }
                    }
                }
            }

            // "Recommended for you" Section
            val remainingVideos = regularVideos.filter { it.id != featuredVideo?.id }
            if (remainingVideos.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recommended for you",
                            color = ShotblastTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "More",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                items(remainingVideos) { video ->
                    FeedVideoCard(
                        video = video,
                        onClick = { onVideoClick(video) },
                        onOptionsClick = { onVideoOptionsClick(video) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun FeaturedVideoCard(
    video: VideoItem,
    onClick: () -> Unit,
    onChannelClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("featured_video_card")
    ) {
        // 16:9 Thumbnail
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(ShotblastSurfaceVariant)
        ) {
            val thumbModel = video.thumbnailUri ?: video.thumbnailDrawable ?: R.drawable.ic_shotblast_logo
            AsyncImage(
                model = thumbModel,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Duration badge in bottom right corner
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                Text(
                    text = video.durationStr,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Details Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Channel Avatar
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ShotblastCyan.copy(alpha = 0.2f))
                    .border(1.dp, ShotblastCyan, CircleShape)
                    .clickable(onClick = onChannelClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = video.channelName,
                    tint = ShotblastCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    color = ShotblastTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 19.sp,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onChannelClick)
                ) {
                    Text(
                        text = video.channelName,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    if (video.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Verified",
                            tint = ShotblastCyan,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = " • ${video.viewsStr} • ${video.timeAgo}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(
                onClick = onOptionsClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Options",
                    tint = Color.LightGray
                )
            }
        }
    }
}

@Composable
private fun ShortVideoCard(
    video: VideoItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ShotblastSurfaceVariant),
        modifier = Modifier
            .width(135.dp)
            .height(230.dp)
            .clickable(onClick = onClick)
            .testTag("short_card_${video.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val thumbModel = video.thumbnailUri ?: video.thumbnailDrawable ?: R.drawable.ic_shotblast_logo
            AsyncImage(
                model = thumbModel,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Duration Pill
            Surface(
                color = Color.Black.copy(alpha = 0.8f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            ) {
                Text(
                    text = video.durationStr,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // Gradient scrim at bottom for text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                        )
                    )
            )

            // Caption & Views
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 15.sp,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${video.channelName} • ${video.viewsStr}",
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun FeedVideoCard(
    video: VideoItem,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Thumbnail Box
        Box(
            modifier = Modifier
                .width(140.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(ShotblastSurfaceVariant)
        ) {
            val thumbModel = video.thumbnailUri ?: video.thumbnailDrawable ?: R.drawable.ic_shotblast_logo
            AsyncImage(
                model = thumbModel,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
            ) {
                Text(
                    text = video.durationStr,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                color = ShotblastTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = video.channelName,
                color = Color.Gray,
                fontSize = 11.sp
            )
            Text(
                text = "${video.viewsStr} • ${video.timeAgo}",
                color = Color.DarkGray,
                fontSize = 10.sp
            )
        }

        IconButton(
            onClick = onOptionsClick,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Options",
                tint = Color.LightGray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
