package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelProfileScreen(
    channelName: String,
    allVideos: List<VideoItem>,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val channelVideos = remember(allVideos, channelName) {
        allVideos.filter { it.channelName.equals(channelName, ignoreCase = true) }
    }

    val isSubscribed = remember(channelVideos) {
        channelVideos.firstOrNull()?.isSubscribed ?: false
    }

    val tabs = listOf("Latest", "Popular")

    // Filter into Latest and Popular lists
    val latestVideos = remember(channelVideos, allVideos) {
        if (channelVideos.isNotEmpty()) channelVideos else allVideos.take(5)
    }

    val popularVideos = remember(channelVideos, allVideos) {
        if (channelVideos.isNotEmpty()) channelVideos.sortedByDescending { it.viewsCount }
        else allVideos.sortedByDescending { it.viewsCount }.take(5)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ShotblastBackground)
    ) {
        // App Bar
        Surface(
            color = ShotblastSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("channel_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = channelName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Channel Banner & Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            ShotblastSurfaceVariant,
                            ShotblastCyan.copy(alpha = 0.25f),
                            ShotblastCoral.copy(alpha = 0.2f)
                        )
                    )
                )
        )

        // Channel Information Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(ShotblastCyan.copy(alpha = 0.2f))
                            .border(2.dp, ShotblastCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = channelName,
                            tint = ShotblastCyan,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = channelName,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Verified channel",
                                tint = ShotblastCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "@${channelName.lowercase().replace(" ", "")} • 1.2M Subscribers",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                // Subscribe Button
                Button(
                    onClick = {
                        val newSub = VideoRepository.toggleChannelSubscription(channelName)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (newSub) "Subscribed to $channelName!" else "Unsubscribed from $channelName"
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSubscribed) ShotblastSurfaceVariant else Color(0xFFE53935),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("channel_subscribe_button")
                ) {
                    if (isSubscribed) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "Subscribed",
                            tint = ShotblastCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Subscribed", fontSize = 12.sp)
                    } else {
                        Text("Subscribe", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                text = "Official channel of $channelName. Sharing high quality videos, 4K travel experiences, nature wonders, and cinematic edits.",
                color = Color.LightGray,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 10.dp, bottom = 12.dp)
            )
        }

        // Two Tabs: Latest & Popular
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = ShotblastSurface,
            contentColor = ShotblastCyan,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    color = ShotblastCyan,
                    modifier = Modifier.tabIndicatorOffset(selectedTabIndex)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTabIndex == index) ShotblastCyan else Color.Gray,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Tab Content List
        val currentDisplayList = if (selectedTabIndex == 0) latestVideos else popularVideos

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            items(currentDisplayList) { video ->
                ChannelVideoItemRow(
                    video = video,
                    isPopular = selectedTabIndex == 1,
                    onClick = { onVideoClick(video) }
                )
                HorizontalDivider(color = ShotblastBorder, modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp))
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun ChannelVideoItemRow(
    video: VideoItem,
    isPopular: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(130.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(ShotblastSurfaceVariant)
        ) {
            if (video.thumbnailDrawable != null) {
                AsyncImage(
                    model = video.thumbnailDrawable,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPopular) Icons.Filled.LocalFireDepartment else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = if (isPopular) ShotblastCoral else ShotblastCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

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

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPopular) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = "Trending Hit",
                        tint = ShotblastCoral,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                    text = "${video.viewsStr} • ${video.timeAgo}",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}
