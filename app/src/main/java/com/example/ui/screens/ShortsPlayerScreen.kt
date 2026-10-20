package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.VideoRepository
import com.example.model.CommentItem
import com.example.model.VideoItem
import com.example.ui.theme.ShotblastBorder
import com.example.ui.theme.ShotblastCoral
import com.example.ui.theme.ShotblastCyan
import com.example.ui.theme.ShotblastSurface
import com.example.ui.theme.ShotblastSurfaceVariant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * ShortsPlayerScreen:
 * - Vertical PageView / VerticalPager for smooth infinite scrolling through Shorts
 * - Fullscreen toggle buttons & landscape controls are ABSENT (strictly vertical edge-to-edge)
 * - Persistent action sidebar placed properly down side of the phone:
 *   Like, Dislike, Comment (full picture comments), Call (creator hotline / live audio), Share, Sound disc
 * - Double-tap-to-like gesture with animated bursting heart
 * - Live playback synchronization (only active page plays audio/video)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortsPlayerScreen(
    shortsList: List<VideoItem>,
    initialShortId: String? = null,
    onBackClick: () -> Unit,
    onCreateShortClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val repoVideos by VideoRepository.videos.collectAsStateWithLifecycle()
    val availableShorts = remember(shortsList, repoVideos) {
        val base = if (shortsList.isEmpty()) repoVideos.filter { it.isShort } else shortsList
        if (base.isEmpty()) repoVideos else base
    }

    if (availableShorts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("No shorts available", color = Color.White)
        }
        return
    }

    // Configure infinite pager wrapping
    val virtualCount = availableShorts.size * 1000
    val startBase = (virtualCount / 2) - ((virtualCount / 2) % availableShorts.size)
    val initialIndexInList = availableShorts.indexOfFirst { it.id == initialShortId }.coerceAtLeast(0)
    val initialPageIndex = startBase + initialIndexInList

    val pagerState = rememberPagerState(
        initialPage = initialPageIndex,
        pageCount = { virtualCount }
    )

    // Current active short item from the pager state
    val currentActualIndex = pagerState.currentPage % availableShorts.size
    val currentShort = availableShorts.getOrNull(currentActualIndex) ?: availableShorts.first()

    val commentsMap by VideoRepository.comments.collectAsStateWithLifecycle()
    val allComments = commentsMap[currentShort.id] ?: listOf(
        CommentItem("sc1", "Alex Hunter", "@alexhunter", "1 hour ago", "This short is absolute fire! 🔥", 240, true),
        CommentItem("sc2", "NatureLover", "@naturelover", "3 hours ago", "Incredible cinematography and crisp audio! ❤️", 185, false),
        CommentItem("sc3", "Sam Wilson", "@samwilson", "5 hours ago", "Rewatched 10 times already, great edit!", 92, false)
    )

    var showCommentsSheet by remember { mutableStateOf(false) }

    // Double-tap heart animation state
    var showHeartAnimation by remember { mutableStateOf(false) }
    val heartScale = remember { Animatable(0f) }
    val heartAlpha = remember { Animatable(1f) }

    // Trigger double-tap like action & heart animation
    fun onDoubleTapLike() {
        if (!currentShort.isLiked) {
            VideoRepository.toggleLike(currentShort.id)
        }
        coroutineScope.launch {
            showHeartAnimation = true
            heartScale.snapTo(0.2f)
            heartAlpha.snapTo(1f)
            heartScale.animateTo(
                targetValue = 1.3f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
            heartAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 350)
            )
            showHeartAnimation = false
        }
    }

    var isAutoNextEnabled by remember { mutableStateOf(true) }
    var isTransitioning by remember { mutableStateOf(false) }

    // Next Video Sequence Handler
    // Automatically transitions to the next short in the playlist when current short finishes
    fun transitionToNextShort() {
        if (!pagerState.isScrollInProgress && !isTransitioning) {
            isTransitioning = true
            coroutineScope.launch {
                try {
                    pagerState.animateScrollToPage(
                        page = pagerState.currentPage + 1,
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    )
                } finally {
                    delay(300)
                    isTransitioning = false
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("shorts_player_screen")
    ) {
        // Vertical PageView for infinite scrolling
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { pageIndex -> pageIndex }
        ) { page ->
            val shortItem = availableShorts[page % availableShorts.size]
            val isCurrentPage = page == pagerState.currentPage

            ShortVideoPageItem(
                video = shortItem,
                isCurrentPage = isCurrentPage,
                isAutoNextEnabled = isAutoNextEnabled,
                onDoubleTap = { onDoubleTapLike() },
                onVideoFinished = {
                    if (isAutoNextEnabled) {
                        transitionToNextShort()
                    }
                }
            )
        }

        // Gradient overlay for bottom controls & captions readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.92f)
                        )
                    )
                )
        )

        // Top Header: Back Button, Shorts Title, Auto-Next Sequence Toggle & Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Shorts",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            // Auto-Next Sequence Handler Toggle Pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isAutoNextEnabled) ShotblastCyan.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, if (isAutoNextEnabled) ShotblastCyan else Color.White.copy(alpha = 0.25f)),
                modifier = Modifier
                    .clickable { isAutoNextEnabled = !isAutoNextEnabled }
                    .testTag("auto_next_toggle")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = if (isAutoNextEnabled) Icons.Filled.PlayArrow else Icons.Filled.Repeat,
                        contentDescription = "Auto Next Sequence",
                        tint = if (isAutoNextEnabled) ShotblastCyan else Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAutoNextEnabled) "Auto-Next ON" else "Loop ON",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Next Video Sequence Skip Button
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.size(36.dp)
            ) {
                IconButton(
                    onClick = { transitionToNextShort() },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("next_short_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next Short",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Animated double-tap heart indicator
        if (showHeartAnimation) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Liked",
                    tint = ShotblastCoral.copy(alpha = heartAlpha.value),
                    modifier = Modifier
                        .size(130.dp)
                        .scale(heartScale.value)
                )
            }
        }

        // Persistent Action Sidebar (Downside of the phone on the right)
        // Showing strictly: 👍🏻 Like, 👎🏻 Dislike, 💬 Comment, 🚀 Share
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 12.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 👍🏻 Like Option
            ShortActionItem(
                icon = if (currentShort.isLiked) Icons.Filled.ThumbUp else Icons.Filled.ThumbUpOffAlt,
                emoji = "👍🏻",
                label = if (currentShort.likeCount > 0) formatShortCount(currentShort.likeCount) else "Like",
                tint = if (currentShort.isLiked) ShotblastCoral else Color.White,
                onClick = { VideoRepository.toggleLike(currentShort.id) },
                testTag = "shorts_like_button"
            )

            // 2. 👎🏻 Dislike Option
            ShortActionItem(
                icon = if (currentShort.isDisliked) Icons.Filled.ThumbDown else Icons.Filled.ThumbDownOffAlt,
                emoji = "👎🏻",
                label = "Dislike",
                tint = if (currentShort.isDisliked) ShotblastCyan else Color.White,
                onClick = { VideoRepository.toggleDislike(currentShort.id) },
                testTag = "shorts_dislike_button"
            )

            // 3. 💬 Comment Option
            ShortActionItem(
                icon = Icons.Filled.Comment,
                emoji = "💬",
                label = if (allComments.isNotEmpty()) formatShortCount(allComments.size + 2450L) else "Comment",
                tint = Color.White,
                onClick = { showCommentsSheet = true },
                testTag = "shorts_comment_button"
            )

            // 4. 🚀 Share Option
            ShortActionItem(
                icon = Icons.Filled.Share,
                emoji = "🚀",
                label = "Share",
                tint = Color.White,
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Watch this Shotblast Short: ${currentShort.title}\n${currentShort.videoUrl}")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Short"))
                },
                testTag = "shorts_share_button"
            )
        }

        // Lower-Left Details: Creator channel info, subscribe button, caption, and audio track info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.75f)
                .navigationBarsPadding()
                .padding(start = 14.dp, bottom = 22.dp)
        ) {
            // Creator / Channel Row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ShotblastCyan.copy(alpha = 0.2f))
                        .border(1.5.dp, ShotblastCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = currentShort.channelName,
                        tint = ShotblastCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = currentShort.channelName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                if (currentShort.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Verified",
                        tint = ShotblastCyan,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = { VideoRepository.toggleChannelSubscription(currentShort.channelName) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentShort.isSubscribed) ShotblastSurfaceVariant else Color(0xFFE53935),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = if (currentShort.isSubscribed) "Subscribed" else "Subscribe",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Caption / Title
            Text(
                text = currentShort.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Audio track wave
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = "Sound wave",
                    tint = ShotblastCyan,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${currentShort.channelName} • Original Sound track",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }

    // Full Picture Comments Modal Sheet (Showing all comments with profile pictures & full details)
    if (showCommentsSheet) {
        val commentsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var newCommentText by remember { mutableStateOf("") }
        val emojis = listOf("❤️", "🔥", "👏", "😍", "🚀", "😂", "🙌", "💯")

        ModalBottomSheet(
            onDismissRequest = { showCommentsSheet = false },
            sheetState = commentsSheetState,
            containerColor = ShotblastSurface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Header with video thumbnail picture & total comment count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Video thumbnail picture
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ShotblastSurfaceVariant)
                        ) {
                            if (currentShort.thumbnailDrawable != null) {
                                AsyncImage(
                                    model = currentShort.thumbnailDrawable,
                                    contentDescription = currentShort.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Comments (${allComments.size})",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentShort.title,
                                color = Color.Gray,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(
                        onClick = { showCommentsSheet = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = ShotblastBorder)

                // Quick Emoji Bar
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(emojis) { emoji ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = ShotblastSurfaceVariant,
                            modifier = Modifier.clickable {
                                newCommentText += emoji
                            }
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Full Comments List with Pictures / Avatars
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(320.dp)
                ) {
                    items(allComments) { comment ->
                        FullPictureCommentItem(comment = comment)
                        HorizontalDivider(color = ShotblastBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                    }
                }

                // Interactive Comment Input Box at Bottom with User Avatar Picture
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(ShotblastCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("U", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Add a comment as @you...", color = Color.Gray, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShotblastCyan,
                            unfocusedBorderColor = ShotblastBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(22.dp),
                        trailingIcon = {
                            if (newCommentText.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        VideoRepository.addComment(currentShort.id, newCommentText)
                                        newCommentText = ""
                                    },
                                    modifier = Modifier.testTag("post_comment_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Send,
                                        contentDescription = "Post",
                                        tint = ShotblastCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("shorts_comment_input")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/**
 * Individual Page Item for the VerticalPager
 * Seamlessly manages ExoPlayer instance for this specific short video.
 * Automatically signals when playback completes to trigger next video transition.
 */
@OptIn(UnstableApi::class)
@Composable
private fun ShortVideoPageItem(
    video: VideoItem,
    isCurrentPage: Boolean,
    isAutoNextEnabled: Boolean,
    onDoubleTap: () -> Unit,
    onVideoFinished: () -> Unit
) {
    val context = LocalContext.current

    var isBuffering by remember { mutableStateOf(true) }
    var isUserPaused by remember { mutableStateOf(false) }
    var activeUrl by remember(video.videoUrl) { mutableStateOf(video.videoUrl) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var totalDurationMs by remember { mutableLongStateOf(1L) }

    val exoPlayer = remember(activeUrl) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(10000)
            .setReadTimeoutMs(10000)
        val defaultDataSourceFactory = androidx.media3.datasource.DefaultDataSource.Factory(context, httpDataSourceFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(defaultDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                val mediaItem = MediaItem.fromUri(activeUrl)
                setMediaItem(mediaItem)
                repeatMode = if (isAutoNextEnabled) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ONE
                prepare()
            }
    }

    // Keep repeat mode in sync with user's auto-next preference
    LaunchedEffect(isAutoNextEnabled) {
        exoPlayer.repeatMode = if (isAutoNextEnabled) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ONE
    }

    // Play/Pause according to current page visibility
    LaunchedEffect(isCurrentPage, isUserPaused) {
        if (isCurrentPage && !isUserPaused) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    // Track playback progress
    LaunchedEffect(isCurrentPage, isUserPaused) {
        if (isCurrentPage) {
            while (true) {
                val pos = exoPlayer.currentPosition
                val dur = exoPlayer.duration
                if (dur > 0) {
                    currentPositionMs = pos
                    totalDurationMs = dur
                }
                delay(100)
            }
        }
    }

    DisposableEffect(exoPlayer, isCurrentPage, isAutoNextEnabled) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                // Automatically transition to the next video in sequence when current video finishes
                if (playbackState == Player.STATE_ENDED && isCurrentPage && isAutoNextEnabled) {
                    onVideoFinished()
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("ShortsPlayerScreen", "Error playing short: ${error.message}")
                isBuffering = false
                val fallbackUrl = "https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4"
                if (activeUrl != fallbackUrl) {
                    activeUrl = fallbackUrl
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(video.id) {
                detectTapGestures(
                    onTap = {
                        isUserPaused = !isUserPaused
                    },
                    onDoubleTap = {
                        onDoubleTap()
                    }
                )
            }
    ) {
        // Video Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering indicator
        if (isBuffering && isCurrentPage) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ShotblastCyan,
                    strokeWidth = 3.dp
                )
            }
        }

        // Tap to pause overlay icon indicator
        if (isUserPaused && isCurrentPage) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }

        // Thin glowing video progress line along the bottom edge
        if (totalDurationMs > 0 && isCurrentPage) {
            val progress = (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress)
                        .height(2.5.dp)
                        .background(ShotblastCyan)
                )
            }
        }
    }
}

@Composable
private fun FullPictureCommentItem(comment: CommentItem) {
    var isLiked by remember { mutableStateOf(comment.isLiked) }
    var likesCount by remember { mutableStateOf(comment.likesCount) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Author Avatar Picture with gradient
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(ShotblastCyan, ShotblastCoral)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.authorName.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.authorHandle,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = comment.timeAgo,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }

            Text(
                text = comment.text,
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable {
                        isLiked = !isLiked
                        likesCount = if (isLiked) likesCount + 1 else likesCount - 1
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) ShotblastCoral else Color.Gray,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (likesCount > 0) likesCount.toString() else "",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Reply",
                    color = ShotblastCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ShortActionItem(
    icon: ImageVector,
    emoji: String,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            // Distinct emoji badge for options 👍🏻 like, 👎🏻 dislike, 💬 comment, 🚀 share
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-2).dp)
                    .background(Color.Black.copy(alpha = 0.85f), CircleShape)
                    .border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                    .padding(horizontal = 2.dp, vertical = 1.dp)
            ) {
                Text(
                    text = emoji,
                    fontSize = 10.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatShortCount(count: Long): String {
    return when {
        count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}
