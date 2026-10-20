package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.VideoRepository
import com.example.model.CommentItem
import com.example.model.VideoItem
import com.example.ui.components.VideoOptionsBottomSheet
import com.example.ui.player.ExoVideoPlayer
import com.example.ui.theme.ShotblastBackground
import com.example.ui.theme.ShotblastBorder
import com.example.ui.theme.ShotblastCyan
import com.example.ui.theme.ShotblastSurface
import com.example.ui.theme.ShotblastSurfaceVariant
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailScreen(
    video: VideoItem,
    allVideos: List<VideoItem>,
    comments: List<CommentItem>,
    snackbarHostState: SnackbarHostState,
    isFullScreen: Boolean,
    onToggleFullScreen: () -> Unit,
    onBackClick: () -> Unit,
    onSelectVideo: (VideoItem) -> Unit,
    onChannelClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showOptionsSheet by remember { mutableStateOf(false) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var isAutoplayOn by remember { mutableStateOf(true) }
    var newCommentText by remember { mutableStateOf("") }
    var showAllComments by remember { mutableStateOf(false) }

    // Intercept back key when in fullscreen to exit fullscreen cleanly
    androidx.activity.compose.BackHandler(enabled = isFullScreen) {
        onToggleFullScreen()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showOptionsSheet) {
        VideoOptionsBottomSheet(
            video = video,
            sheetState = sheetState,
            onDismiss = { showOptionsSheet = false },
            onSaveWatchLater = {
                val saved = VideoRepository.toggleSave(video.id)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        if (saved) "Added to Watch Later!" else "Removed from Watch Later"
                    )
                }
            },
            onShare = {
                shareVideo(context, video)
            },
            onDownload = {
                val downloaded = VideoRepository.toggleDownload(video.id)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        if (downloaded) "Video saved for offline watching!" else "Download removed"
                    )
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ShotblastBackground)
    ) {
        // Video Player Box
        ExoVideoPlayer(
            videoUrl = video.videoUrl,
            title = video.title,
            isFullScreen = isFullScreen,
            onToggleFullScreen = onToggleFullScreen,
            onBackClick = onBackClick,
            onSwipeUp = { showOptionsSheet = true },
            isShort = false,
            onDoubleTap = { VideoRepository.toggleLike(video.id) },
            modifier = Modifier.fillMaxWidth()
        )

        if (!isFullScreen) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                // Title and 3-dots
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = video.title,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("video_title_text")
                        )
                        IconButton(
                            onClick = { showOptionsSheet = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Video options",
                                tint = Color.LightGray
                            )
                        }
                    }

                    // Metadata & Hashtags
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${video.viewsStr} • ${video.timeAgo}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = video.tags.joinToString(" "),
                            color = ShotblastCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Channel Info Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onChannelClick(video.channelName) }
                                .testTag("channel_profile_row")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(ShotblastCyan.copy(alpha = 0.2f), CircleShape)
                                    .border(1.5.dp, ShotblastCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = video.channelName,
                                    tint = ShotblastCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = video.channelName,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (video.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Verified channel",
                                            tint = ShotblastCyan,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = video.channelSubscribers,
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Subscribe Button
                        Button(
                            onClick = {
                                val isNowSubscribed = VideoRepository.toggleChannelSubscription(video.channelName)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (isNowSubscribed) "Subscribed to ${video.channelName}!" else "Unsubscribed from ${video.channelName}"
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (video.isSubscribed) ShotblastSurfaceVariant else ShotblastCyan,
                                contentColor = if (video.isSubscribed) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("subscribe_button")
                        ) {
                            if (video.isSubscribed) {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsActive,
                                    contentDescription = "Subscribed",
                                    tint = ShotblastCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Subscribed",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text(
                                    text = "Subscribe",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Interactive Action Pills (Like, Dislike, Share, Download, Save)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Like & Dislike combined pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ShotblastSurfaceVariant,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Row(
                                    modifier = Modifier
                                        .clickable { VideoRepository.toggleLike(video.id) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("like_button"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (video.isLiked) Icons.Filled.ThumbUp else Icons.Filled.ThumbUpOffAlt,
                                        contentDescription = "Like",
                                        tint = if (video.isLiked) ShotblastCyan else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = formatCount(video.likeCount),
                                        color = if (video.isLiked) ShotblastCyan else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(18.dp)
                                        .background(ShotblastBorder)
                                )

                                IconButton(
                                    onClick = { VideoRepository.toggleDislike(video.id) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("dislike_button")
                                ) {
                                    Icon(
                                        imageVector = if (video.isDisliked) Icons.Filled.ThumbDown else Icons.Filled.ThumbDownOffAlt,
                                        contentDescription = "Dislike",
                                        tint = if (video.isDisliked) ShotblastCyan else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Share
                        ActionPill(
                            icon = Icons.Filled.Share,
                            label = "Share",
                            onClick = { shareVideo(context, video) }
                        )

                        // Download
                        ActionPill(
                            icon = if (video.isDownloaded) Icons.Filled.FileDownloadDone else Icons.Filled.Download,
                            label = if (video.isDownloaded) "Downloaded" else "Download",
                            isHighlighted = video.isDownloaded,
                            onClick = {
                                val downloaded = VideoRepository.toggleDownload(video.id)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (downloaded) "Downloaded for offline playback!" else "Download removed"
                                    )
                                }
                            }
                        )

                        // Save
                        ActionPill(
                            icon = if (video.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            label = if (video.isSaved) "Saved" else "Save",
                            isHighlighted = video.isSaved,
                            onClick = {
                                val saved = VideoRepository.toggleSave(video.id)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (saved) "Saved to Watch Later playlist!" else "Removed from playlist"
                                    )
                                }
                            }
                        )

                        // Remix
                        ActionPill(
                            icon = Icons.Filled.AutoAwesome,
                            label = "Remix",
                            onClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Remixing audio clip for Shorts...")
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // About this video Card
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ShotblastSurfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                            .animateContentSize()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "About this video",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = video.description,
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 2
                            )
                            Text(
                                text = if (isDescriptionExpanded) "...less" else "...more",
                                color = ShotblastCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Comments Section Header
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ShotblastSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAllComments = !showAllComments }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Comments ${comments.size + 2800}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (showAllComments) "Collapse" else "View all",
                                    color = ShotblastCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Interactive comment input field
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(ShotblastCyan, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("U", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = newCommentText,
                                    onValueChange = { newCommentText = it },
                                    placeholder = { Text("Add a comment...", color = Color.Gray, fontSize = 12.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ShotblastCyan,
                                        unfocusedBorderColor = ShotblastBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    trailingIcon = {
                                        if (newCommentText.isNotBlank()) {
                                            IconButton(onClick = {
                                                VideoRepository.addComment(video.id, newCommentText)
                                                newCommentText = ""
                                                showAllComments = true
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Filled.Send,
                                                    contentDescription = "Post comment",
                                                    tint = ShotblastCyan,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("comment_input_field")
                                )
                            }

                            // Top Comment Preview
                            val topComment = comments.firstOrNull()
                            if (topComment != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(ShotblastCyan.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = topComment.authorName.take(1),
                                            color = ShotblastCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = topComment.authorHandle,
                                                color = Color.LightGray,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = topComment.timeAgo,
                                                color = Color.Gray,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Text(
                                            text = topComment.text,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // If user tapped to see all comments
                if (showAllComments) {
                    items(comments.drop(1)) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(ShotblastCyan.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = comment.authorName.take(1),
                                    color = ShotblastCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${comment.authorHandle} • ${comment.timeAgo}",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = comment.text,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Next Up Header with Autoplay Switch
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next up",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Autoplay",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = isAutoplayOn,
                                onCheckedChange = { isAutoplayOn = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ShotblastCyan,
                                    checkedTrackColor = ShotblastCyan.copy(alpha = 0.4f),
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = ShotblastBorder
                                )
                            )
                        }
                    }
                }

                // Related Videos List
                val relatedVideos = allVideos.filter { it.id != video.id && !it.isShort }
                items(relatedVideos) { item ->
                    RelatedVideoRow(
                        video = item,
                        onClick = { onSelectVideo(item) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ActionPill(
    icon: ImageVector,
    label: String,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isHighlighted) ShotblastCyan.copy(alpha = 0.2f) else ShotblastSurfaceVariant,
        modifier = Modifier
            .height(36.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isHighlighted) ShotblastCyan else Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isHighlighted) ShotblastCyan else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RelatedVideoRow(
    video: VideoItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(130.dp)
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
                color = Color.Black.copy(alpha = 0.8f),
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
                color = Color.White,
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
    }
}

private fun shareVideo(context: Context, video: VideoItem) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Check out this video on Shotblast: \"${video.title}\"\n${video.videoUrl}")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Shotblast Video")
    context.startActivity(shareIntent)
}

private fun formatCount(count: Long): String {
    return when {
        count >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}
