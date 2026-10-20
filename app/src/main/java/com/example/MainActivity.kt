package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.VideoRepository
import com.example.model.VideoItem
import com.example.ui.components.BottomNavTab
import com.example.ui.components.ShotblastBottomNav
import com.example.ui.components.VideoOptionsBottomSheet
import com.example.ui.dialogs.CreateVideoDialog
import com.example.ui.dialogs.AuthAndChannelDialog
import com.example.ui.screens.AnimeIntroScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.ShortsPlayerScreen
import com.example.ui.screens.ShortsScreen
import com.example.ui.screens.SubscriptionsScreen
import com.example.ui.screens.VideoDetailScreen
import com.example.ui.theme.ShotblastCyan
import com.example.ui.theme.ShotblastSurface
import com.example.ui.theme.ShotblastSurfaceVariant
import com.example.ui.theme.ShotblastTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.ui.theme.ThemeManager.init(this)
        enableEdgeToEdge()
        setContent {
            ShotblastTheme {
                ShotblastAppRoot()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShotblastAppRoot() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val videos by VideoRepository.videos.collectAsStateWithLifecycle()
    val commentsMap by VideoRepository.comments.collectAsStateWithLifecycle()
    val selectedCategory by VideoRepository.selectedCategory.collectAsStateWithLifecycle()
    val channels by VideoRepository.channels.collectAsStateWithLifecycle()

    var showAnimeIntro by rememberSaveable { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(BottomNavTab.HOME) }
    var activeVideoForPlayer by remember { mutableStateOf<VideoItem?>(null) }
    var activeShortForPlayer by remember { mutableStateOf<VideoItem?>(null) }
    var activeChannelProfile by remember { mutableStateOf<String?>(null) }
    var isPlayerFullScreen by remember { mutableStateOf(false) }
    var isSearchScreenOpen by remember { mutableStateOf(false) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var videoForOptionsSheet by remember { mutableStateOf<VideoItem?>(null) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val createSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val optionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle Hardware & Predictive Back Navigation
    BackHandler(
        enabled = showAnimeIntro || isPlayerFullScreen || activeVideoForPlayer != null || activeShortForPlayer != null || activeChannelProfile != null || isSearchScreenOpen || selectedTab != BottomNavTab.HOME
    ) {
        when {
            showAnimeIntro -> showAnimeIntro = false
            isPlayerFullScreen -> isPlayerFullScreen = false
            activeChannelProfile != null -> activeChannelProfile = null
            activeVideoForPlayer != null -> {
                activeVideoForPlayer = null
                isPlayerFullScreen = false
            }
            activeShortForPlayer != null -> activeShortForPlayer = null
            isSearchScreenOpen -> isSearchScreenOpen = false
            selectedTab != BottomNavTab.HOME -> selectedTab = BottomNavTab.HOME
        }
    }

    // Keep activeVideo updated if repository state changes (e.g. liked, saved)
    val currentActiveVideo = activeVideoForPlayer?.let { v ->
        videos.find { it.id == v.id } ?: v
    }

    if (showAnimeIntro) {
        AnimeIntroScreen(
            onIntroFinished = { showAnimeIntro = false }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (currentActiveVideo == null && activeShortForPlayer == null && activeChannelProfile == null && !isPlayerFullScreen && !isSearchScreenOpen) {
                    ShotblastBottomNav(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        onCreateClick = { showCreateDialog = true }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isPlayerFullScreen) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
            ) {
            when {
                // Video Player Screen
                currentActiveVideo != null -> {
                    val comments = commentsMap[currentActiveVideo.id] ?: emptyList()
                    VideoDetailScreen(
                        video = currentActiveVideo,
                        allVideos = videos,
                        comments = comments,
                        snackbarHostState = snackbarHostState,
                        isFullScreen = isPlayerFullScreen,
                        onToggleFullScreen = { isPlayerFullScreen = !isPlayerFullScreen },
                        onBackClick = {
                            if (isPlayerFullScreen) {
                                isPlayerFullScreen = false
                            } else {
                                activeVideoForPlayer = null
                            }
                        },
                        onSelectVideo = { nextVideo ->
                            activeVideoForPlayer = nextVideo
                        },
                        onChannelClick = { channel ->
                            activeChannelProfile = channel
                        }
                    )
                }

                // Dedicated Shorts Player
                activeShortForPlayer != null -> {
                    ShortsPlayerScreen(
                        shortsList = videos.filter { it.isShort },
                        initialShortId = activeShortForPlayer?.id,
                        onBackClick = { activeShortForPlayer = null },
                        onCreateShortClick = { showCreateDialog = true }
                    )
                }

                // Channel Profile Screen
                activeChannelProfile != null -> {
                    com.example.ui.screens.ChannelProfileScreen(
                        channelName = activeChannelProfile!!,
                        allVideos = videos,
                        snackbarHostState = snackbarHostState,
                        onBackClick = { activeChannelProfile = null },
                        onVideoClick = { video -> activeVideoForPlayer = video }
                    )
                }

                // Search Screen
                isSearchScreenOpen -> {
                    SearchScreen(
                        videos = videos,
                        channels = channels,
                        onBackClick = { isSearchScreenOpen = false },
                        onVideoClick = { video ->
                            activeVideoForPlayer = video
                            isSearchScreenOpen = false
                        },
                        onShortsClick = { short ->
                            activeShortForPlayer = short
                            isSearchScreenOpen = false
                        },
                        onChannelClick = { channel ->
                            activeChannelProfile = channel
                            isSearchScreenOpen = false
                        },
                        onVideoOptionsClick = { video -> videoForOptionsSheet = video }
                    )
                }

                // Tab destinations
                else -> {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "TabTransition"
                    ) { tab ->
                        when (tab) {
                            BottomNavTab.HOME -> {
                                HomeScreen(
                                    videos = videos,
                                    selectedCategory = selectedCategory,
                                    onCategorySelected = { VideoRepository.setCategory(it) },
                                    onVideoClick = { video -> activeVideoForPlayer = video },
                                    onShortsClick = { short -> activeShortForPlayer = short },
                                    onChannelClick = { channel -> activeChannelProfile = channel },
                                    onNotificationClick = { showNotificationDialog = true },
                                    onProfileClick = { showProfileDialog = true },
                                    onVideoOptionsClick = { video -> videoForOptionsSheet = video },
                                    onSearchClick = { isSearchScreenOpen = true }
                                )
                            }

                            BottomNavTab.SHORTS -> {
                                ShortsPlayerScreen(
                                    shortsList = videos.filter { it.isShort },
                                    onBackClick = { selectedTab = BottomNavTab.HOME },
                                    onCreateShortClick = { showCreateDialog = true }
                                )
                            }

                            BottomNavTab.SUBSCRIPTIONS -> {
                                SubscriptionsScreen(
                                    channels = channels,
                                    videos = videos,
                                    onVideoClick = { video -> activeVideoForPlayer = video }
                                )
                            }

                            BottomNavTab.LIBRARY -> {
                                LibraryScreen(
                                    videos = videos,
                                    onVideoClick = { video -> activeVideoForPlayer = video },
                                    onProfileClick = { showProfileDialog = true },
                                    onReplayIntro = { showAnimeIntro = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }

    // Create Modal Sheet
    if (showCreateDialog) {
        CreateVideoDialog(
            sheetState = createSheetState,
            onDismiss = { showCreateDialog = false },
            onVideoCreated = { newVideo ->
                coroutineScope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "\"${newVideo.title}\" published successfully!",
                        actionLabel = "Watch Now",
                        duration = androidx.compose.material3.SnackbarDuration.Short
                    )
                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        if (newVideo.isShort) {
                            activeShortForPlayer = newVideo
                        } else {
                            activeVideoForPlayer = newVideo
                        }
                    }
                }
            }
        )
    }

    // Video Options Bottom Sheet
    videoForOptionsSheet?.let { vid ->
        VideoOptionsBottomSheet(
            video = vid,
            sheetState = optionsSheetState,
            onDismiss = { videoForOptionsSheet = null },
            onSaveWatchLater = {
                val isSaved = VideoRepository.toggleSave(vid.id)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        if (isSaved) "Added to Watch Later!" else "Removed from Watch Later"
                    )
                }
            },
            onShare = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Watch: ${vid.title}\n${vid.videoUrl}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Video"))
            },
            onDownload = {
                val isDownloaded = VideoRepository.toggleDownload(vid.id)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        if (isDownloaded) "Downloaded for offline watching!" else "Download removed"
                    )
                }
            },
            onUpdateThumbnail = { newThumbUri ->
                VideoRepository.updateVideoThumbnail(vid.id, newThumbUri)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        "Thumbnail successfully updated from gallery!"
                    )
                }
            }
        )
    }

    // Notification Dialog
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            containerColor = ShotblastSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = ShotblastCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Notifications", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    NotificationItem(
                        title = "Nature Vibes uploaded a new video",
                        subtitle = "The Most Beautiful Places on Earth | 4K",
                        time = "3h ago"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    NotificationItem(
                        title = "Gaming Zone went live",
                        subtitle = "Cyberpunk Arena Championship Live",
                        time = "5h ago"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Close", color = ShotblastCyan)
                }
            }
        )
    }

    // User Authentication & Channel Creation Dialog
    if (showProfileDialog) {
        AuthAndChannelDialog(
            onDismiss = { showProfileDialog = false }
        )
    }
}

@Composable
private fun NotificationItem(
    title: String,
    subtitle: String,
    time: String
) {
    Surface(
        color = ShotblastSurfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = ShotblastCyan, fontSize = 12.sp)
            Text(time, color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
