package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.model.VideoItem
import com.example.ui.theme.ShotblastBackground
import com.example.ui.theme.ShotblastBorder
import com.example.ui.theme.ShotblastCoral
import com.example.ui.theme.ShotblastCyan
import com.example.ui.theme.ShotblastSurface
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AuthManager
import com.example.ui.components.ThemeToggleCard
import com.example.ui.theme.ShotblastTextPrimary
import com.example.ui.theme.ShotblastTextSecondary
import com.example.ui.theme.ShotblastSurfaceVariant

@Composable
fun LibraryScreen(
    videos: List<VideoItem>,
    onVideoClick: (VideoItem) -> Unit,
    onProfileClick: () -> Unit = {},
    onReplayIntro: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userProfile by AuthManager.userProfile.collectAsStateWithLifecycle()
    val currentUser by AuthManager.currentUser.collectAsStateWithLifecycle()

    val savedVideos = videos.filter { it.isSaved }
    val likedVideos = videos.filter { it.isLiked }
    val downloadedVideos = videos.filter { it.isDownloaded }
    val historyVideos = videos.take(4)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ShotblastBackground)
            .padding(vertical = 12.dp)
    ) {
        // User Profile Card
        item {
            Card(
                onClick = onProfileClick,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ShotblastSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ShotblastCyan.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .border(2.dp, ShotblastCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "User profile",
                            tint = ShotblastCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile?.channelName ?: if (currentUser != null) "Shotblast Creator" else "Sign In / Create Channel",
                            color = ShotblastTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentUser != null) "${userProfile?.channelHandle ?: "@creator"} • ${userProfile?.subscribersCount ?: 1} Subscribers" else "Tap to log in or create your channel",
                            color = ShotblastCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Firebase Cloud Integration Status Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ShotblastSurface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    ShotblastCyan.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ShotblastCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.CloudDone,
                                    contentDescription = "Firebase Connected",
                                    tint = ShotblastCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Firebase Cloud Connected",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Project: shotblast-8cd76",
                                    color = ShotblastCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0E382B)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ONLINE",
                                    color = Color(0xFF22C55E),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = ShotblastBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Storage,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cloud Firestore", color = Color.LightGray, fontSize = 12.sp)
                        }
                        Text("Active (Realtime Sync)", color = ShotblastCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.CloudDownload,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Firebase Storage", color = Color.LightGray, fontSize = 12.sp)
                        }
                        Text("shotblast-8cd76.firebasestorage.app", color = ShotblastTextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        // App Theme Toggle (Dark Mode / Light Mode)
        item {
            ThemeToggleCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        // Anime Opening Intro Showcase Card
        item {
            Card(
                onClick = onReplayIntro,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ShotblastSurface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(listOf(ShotblastCyan, ShotblastCoral))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("library_anime_intro_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_shotblast_logo),
                        contentDescription = "Shotblast Logo",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Anime Opening Intro",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = ShotblastCyan.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "SPECIAL",
                                    color = ShotblastCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Watch the high-octane anime opening animation",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play anime intro",
                        tint = ShotblastCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // History Section
        item {
            SectionHeader(
                icon = Icons.Filled.History,
                title = "History",
                action = "View all"
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyVideos) { video ->
                    HistoryCard(
                        video = video,
                        onClick = { onVideoClick(video) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = ShotblastBorder, modifier = Modifier.padding(horizontal = 14.dp))
        }

        // Playlists & Quick Sections
        item {
            LibraryRowItem(
                icon = Icons.Filled.Bookmark,
                title = "Watch Later",
                subtitle = "${savedVideos.size} videos",
                onClick = {}
            )

            LibraryRowItem(
                icon = Icons.Filled.ThumbUp,
                title = "Liked Videos",
                subtitle = "${likedVideos.size} videos",
                onClick = {}
            )

            LibraryRowItem(
                icon = Icons.Filled.Download,
                title = "Downloads",
                subtitle = "${downloadedVideos.size} videos available offline",
                onClick = {}
            )

            LibraryRowItem(
                icon = Icons.Filled.VideoLibrary,
                title = "Your Videos & Uploads",
                subtitle = "Manage published clips",
                onClick = {}
            )
        }

        // Offline Downloads Preview (if any)
        if (downloadedVideos.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(
                    icon = Icons.Filled.CloudDownload,
                    title = "Offline Ready Downloads",
                    action = ""
                )
            }

            items(downloadedVideos) { video ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onVideoClick(video) }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ShotblastSurfaceVariant)
                    ) {
                        val thumbModel = video.thumbnailUri ?: video.thumbnailDrawable ?: R.drawable.ic_shotblast_logo
                        AsyncImage(
                            model = thumbModel,
                            contentDescription = video.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = "${video.channelName} • 1080p Offline",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = "Downloaded",
                        tint = ShotblastCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    action: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ShotblastCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (action.isNotBlank()) {
            Text(
                text = action,
                color = ShotblastCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HistoryCard(
    video: VideoItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = video.title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
        Text(
            text = video.channelName,
            color = Color.Gray,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun LibraryRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(ShotblastSurfaceVariant, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = ShotblastCyan,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Color.DarkGray,
            modifier = Modifier.size(14.dp)
        )
    }
}
