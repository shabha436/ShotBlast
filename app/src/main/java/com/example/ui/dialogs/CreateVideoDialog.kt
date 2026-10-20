package com.example.ui.dialogs

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.data.FirebaseVideoService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.R
import com.example.data.VideoRepository
import com.example.model.VideoItem
import com.example.ui.theme.ShotblastBorder
import com.example.ui.theme.ShotblastCoral
import com.example.ui.theme.ShotblastCyan
import com.example.ui.theme.ShotblastCyanDark
import com.example.ui.theme.ShotblastSurface
import com.example.ui.theme.ShotblastSurfaceVariant
import com.example.util.GalleryVideoHelper
import com.example.util.GalleryVideoMetadata
import com.example.util.SampleGalleryPreset
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CATEGORIES = listOf(
    "Anime", "Gaming", "Music", "Tech", "Travel", "Nature", "Entertainment", "Comedy", "Education"
)

private val SOUND_TRACKS = listOf(
    "Original Audio",
    "Cyberpunk Synthwave - Neon City",
    "Anime Sakuga Battle OST - Final Stance",
    "Lo-Fi Beats - Rainy Days",
    "Tokyo Phonk Beat - Night Drift"
)

private val ANIME_FILTERS = listOf(
    "None", "Anime Sakuga Glow", "Cyberpunk Neon", "VHS Retro Film", "Dramatic Cinema"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVideoDialog(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onVideoCreated: (VideoItem) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Mode: 0 = Selection Hub, 1 = Upload / Edit Form
    var showUploadForm by remember { mutableStateOf(false) }

    // Upload Fields
    var isShort by remember { mutableStateOf(false) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var directVideoUrl by remember { mutableStateOf("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4") }
    var videoSourceLabel by remember { mutableStateOf("Direct Stream") }
    var isFromDeviceGallery by remember { mutableStateOf(false) }

    var videoTitle by remember { mutableStateOf("") }
    var videoDescription by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Anime") }
    var selectedSoundTrack by remember { mutableStateOf(SOUND_TRACKS[0]) }
    var selectedFilter by remember { mutableStateOf(ANIME_FILTERS[0]) }

    var videoDurationStr by remember { mutableStateOf("0:45") }
    var videoDurationSec by remember { mutableStateOf(45L) }
    var videoDimensions by remember { mutableStateOf("1080x1920 (9:16)") }
    var videoFileSize by remember { mutableStateOf("14.2 MB") }

    var extractedThumbnailUri by remember { mutableStateOf<String?>(null) }
    var extractedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var customThumbnailRes by remember { mutableStateOf<Int?>(null) }
    var isCustomGalleryThumbnail by remember { mutableStateOf(false) }

    // Settings
    var visibility by remember { mutableStateOf("Public") } // Public, Unlisted, Private
    var allowComments by remember { mutableStateOf(true) }
    var allowDownloads by remember { mutableStateOf(true) }
    var madeForKids by remember { mutableStateOf(false) }

    // Tags
    var tagInput by remember { mutableStateOf("") }
    var tagsList by remember { mutableStateOf(listOf("#shotblast", "#trending", "#viral")) }

    // Gallery Picker Loading state
    var isExtractingMedia by remember { mutableStateOf(false) }

    // Publishing Progress state
    var isPublishing by remember { mutableStateOf(false) }
    var publishingProgress by remember { mutableFloatStateOf(0f) }
    var publishingStatusText by remember { mutableStateOf("") }

    // Custom Thumbnail Image Picker (from Device Gallery Photos)
    val pickCustomThumbnail = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val cachedUri = GalleryVideoHelper.saveImageUriToCache(context, uri)
                extractedThumbnailUri = cachedUri
                extractedBitmap = null
                customThumbnailRes = null
                isCustomGalleryThumbnail = true
            }
        }
    }

    // Zero-permission Android Photo Picker for Videos
    val pickVideoFromGallery = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            isFromDeviceGallery = true
            videoSourceLabel = "Device Gallery"
            isExtractingMedia = true
            showUploadForm = true

            scope.launch {
                val metadata = GalleryVideoHelper.extractMetadata(context, uri)
                isExtractingMedia = false
                videoTitle = metadata.titleSuggestion
                videoFileSize = metadata.sizeFormatted
                videoDurationStr = metadata.durationFormatted
                videoDurationSec = if (metadata.durationMs > 0) metadata.durationMs / 1000 else 45L
                videoDimensions = if (metadata.width > 0 && metadata.height > 0) {
                    "${metadata.width}x${metadata.height} (${if (metadata.isVertical) "9:16" else "16:9"})"
                } else "1080p Full HD"
                isShort = metadata.isShortRecommended
                if (!isCustomGalleryThumbnail) {
                    extractedThumbnailUri = metadata.thumbnailUri
                    extractedBitmap = metadata.thumbnailBitmap
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ShotblastSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 44.dp, height = 4.dp),
                shape = CircleShape,
                color = ShotblastBorder
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            if (!showUploadForm) {
                // ==================== SCREEN 1: CREATION HUB ====================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ShotblastCyan.copy(alpha = 0.2f))
                                .border(1.5.dp, ShotblastCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = ShotblastCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Create & Upload",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Shotblast Creator Studio",
                                color = ShotblastCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Highlight: Import from Gallery Button
                Surface(
                    onClick = {
                        pickVideoFromGallery.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.5.dp,
                            brush = Brush.horizontalGradient(listOf(ShotblastCyan, ShotblastCoral)),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .testTag("import_gallery_primary_button")
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(ShotblastCyan.copy(alpha = 0.16f), ShotblastCoral.copy(alpha = 0.16f))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(ShotblastCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoLibrary,
                                    contentDescription = "Gallery",
                                    tint = Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Import from Gallery",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = ShotblastCoral,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "RECOMMENDED",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Select any video file directly from your device storage with instant frame preview",
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Option: Create a Short
                CreateHubOptionRow(
                    icon = Icons.Filled.Bolt,
                    iconBg = ShotblastCoral,
                    title = "Create / Upload a Short",
                    subtitle = "Vertical 9:16 video (up to 60s) with anime sounds & filters",
                    badge = "VIRAL",
                    badgeColor = ShotblastCoral,
                    onClick = {
                        isShort = true
                        videoTitle = "Epic Anime Short"
                        videoDurationStr = "0:45"
                        videoDurationSec = 45L
                        videoDimensions = "1080x1920 (9:16)"
                        directVideoUrl = "https://media.w3.org/2010/05/sintel/trailer.mp4"
                        customThumbnailRes = R.drawable.thumb_anime_fight
                        showUploadForm = true
                    },
                    testTag = "create_short_option"
                )

                // Option: Upload Full Video
                CreateHubOptionRow(
                    icon = Icons.Filled.Upload,
                    iconBg = ShotblastCyan,
                    title = "Upload Full Video",
                    subtitle = "High-definition 16:9 full-length episode, tutorial, or vlog",
                    badge = "HD / 4K",
                    badgeColor = ShotblastCyan,
                    onClick = {
                        isShort = false
                        videoTitle = "Stunning 4K Adventure Episode"
                        videoDurationStr = "9:30"
                        videoDurationSec = 570L
                        videoDimensions = "1920x1080 (16:9)"
                        directVideoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"
                        customThumbnailRes = R.drawable.thumb_tropical_beach
                        showUploadForm = true
                    },
                    testTag = "upload_full_video_option"
                )

                // Option: Import Photo from Gallery for Thumbnail
                CreateHubOptionRow(
                    icon = Icons.Filled.PhotoLibrary,
                    iconBg = ShotblastCyan,
                    title = "Import Custom Thumbnail from Gallery",
                    subtitle = "Select custom photo or art from your device gallery to use as video cover",
                    badge = "THUMBNAIL",
                    badgeColor = ShotblastCyan,
                    onClick = {
                        pickCustomThumbnail.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                        showUploadForm = true
                    },
                    testTag = "import_thumbnail_photo_hub_option"
                )

                // Option: Go Live
                CreateHubOptionRow(
                    icon = Icons.Filled.LiveTv,
                    iconBg = Color(0xFFE91E63),
                    title = "Go Live",
                    subtitle = "Start a real-time live stream to your subscribers",
                    badge = "LIVE",
                    badgeColor = Color(0xFFE91E63),
                    onClick = {
                        isShort = false
                        videoTitle = "🔴 Live Anime Sakuga Reaction & Chill Lounge"
                        videoDurationStr = "LIVE"
                        videoDurationSec = 3600L
                        videoDimensions = "1920x1080 (16:9)"
                        directVideoUrl = "https://media.w3.org/2010/05/sintel/trailer.mp4"
                        customThumbnailRes = R.drawable.thumb_anime_fight
                        showUploadForm = true
                    },
                    testTag = "go_live_option"
                )

                // Option: Create Community Post
                CreateHubOptionRow(
                    icon = Icons.Filled.Edit,
                    iconBg = Color(0xFF4CAF50),
                    title = "Create Community Post",
                    subtitle = "Share updates, polls, and teaser images in community",
                    badge = "COMMUNITY",
                    badgeColor = Color(0xFF4CAF50),
                    onClick = {
                        isShort = false
                        videoTitle = "New Anime Episode Dropping This Friday! Vote below!"
                        showUploadForm = true
                    },
                    testTag = "community_post_option"
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ShotblastBorder, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Test with Sample Gallery Presets Carousel
                Text(
                    text = "Quick Test with Gallery Presets",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Instant 1-tap test clips if device gallery has no videos yet",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(GalleryVideoHelper.SAMPLE_GALLERY_PRESETS) { preset ->
                        SampleGalleryPresetCard(
                            preset = preset,
                            onClick = {
                                isShort = preset.isShort
                                videoTitle = preset.title
                                videoDescription = preset.subtitle
                                directVideoUrl = preset.videoUrl
                                customThumbnailRes = preset.thumbnailRes
                                videoDurationStr = preset.durationFormatted
                                videoDurationSec = preset.durationSeconds
                                videoDimensions = if (preset.isShort) "1080x1920 (9:16)" else "1920x1080 (16:9)"
                                selectedCategory = preset.category
                                tagsList = preset.tags
                                isFromDeviceGallery = true
                                videoSourceLabel = "Gallery Preset Clip"
                                showUploadForm = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            } else {
                // ==================== SCREEN 2: UPLOAD & EDIT DETAILS ====================
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(580.dp)
                        .verticalScroll(scrollState)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showUploadForm = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Back", tint = Color.White)
                            }
                            Text(
                                text = if (isShort) "Upload Short" else "Upload Full Video",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Re-pick from gallery button
                        TextButton(
                            onClick = {
                                pickVideoFromGallery.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            }
                        ) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = ShotblastCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Re-Pick", color = ShotblastCyan, fontSize = 13.sp)
                        }
                    }

                    // Creator Channel Banner
                    val activeProfile = com.example.data.AuthManager.userProfile.value
                    Surface(
                        color = Color(0xFF0F1E24),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShotblastCyan.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                                    .border(1.dp, ShotblastCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = ShotblastCyan, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Channel: ${activeProfile?.channelName ?: "Shotblast Creator"}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${activeProfile?.channelHandle ?: "@creator"} • Cloud Firestore Linked",
                                    color = ShotblastCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Format Switcher: Short vs Full Video
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = ShotblastSurfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            // Short Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isShort) ShotblastCoral else Color.Transparent)
                                    .clickable {
                                        isShort = true
                                        videoDimensions = "1080x1920 (9:16)"
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Bolt,
                                        contentDescription = null,
                                        tint = if (isShort) Color.White else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "⚡ Short (9:16)",
                                        color = if (isShort) Color.White else Color.Gray,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Full Video Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!isShort) ShotblastCyan else Color.Transparent)
                                    .clickable {
                                        isShort = false
                                        videoDimensions = "1920x1080 (16:9)"
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Movie,
                                        contentDescription = null,
                                        tint = if (!isShort) Color.Black else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "🎬 Full Video (16:9)",
                                        color = if (!isShort) Color.Black else Color.Gray,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Interactive Live Video Preview & Media Card
                    VideoPreviewCard(
                        videoUrl = selectedVideoUri?.toString() ?: directVideoUrl,
                        isShort = isShort,
                        extractedBitmap = extractedBitmap,
                        extractedThumbnailUri = extractedThumbnailUri,
                        customThumbnailRes = customThumbnailRes,
                        durationStr = videoDurationStr,
                        dimensions = videoDimensions,
                        fileSize = videoFileSize,
                        sourceLabel = videoSourceLabel,
                        isExtracting = isExtractingMedia,
                        onPickFromGallery = {
                            pickVideoFromGallery.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title Input
                    OutlinedTextField(
                        value = videoTitle,
                        onValueChange = { if (it.length <= 100) videoTitle = it },
                        label = { Text("Title (required)", color = Color.Gray) },
                        supportingText = {
                            Text(
                                text = "${videoTitle.length}/100",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        trailingIcon = {
                            if (videoTitle.isNotEmpty()) {
                                IconButton(onClick = { videoTitle = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShotblastCyan,
                            unfocusedBorderColor = ShotblastBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_title_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description Input
                    OutlinedTextField(
                        value = videoDescription,
                        onValueChange = { videoDescription = it },
                        label = { Text("Description & Hashtags", color = Color.Gray) },
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShotblastCyan,
                            unfocusedBorderColor = ShotblastBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_description_input")
                    )

                    // Quick hashtag insertion chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("#Shotblast", if (isShort) "#Shorts" else "#Anime", "#Trending", "#Viral", "#Gaming", "#4K").forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ShotblastSurfaceVariant,
                                modifier = Modifier.clickable {
                                    if (!videoDescription.contains(tag)) {
                                        videoDescription = (videoDescription + " " + tag).trim()
                                    }
                                }
                            ) {
                                Text(
                                    text = "+ $tag",
                                    color = ShotblastCyan,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Selection Chips
                    Text("Category", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CATEGORIES.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (isShort) ShotblastCoral else ShotblastCyan,
                                    selectedLabelColor = if (isShort) Color.White else Color.Black,
                                    containerColor = ShotblastSurfaceVariant,
                                    labelColor = Color.LightGray
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // SHORT-SPECIFIC OR FULL VIDEO-SPECIFIC CONTROLS
                    if (isShort) {
                        // Sound Track Selector for Shorts
                        Text("Audio Track / Sound", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SOUND_TRACKS.forEach { track ->
                                val isSelected = selectedSoundTrack == track
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) ShotblastCyan.copy(alpha = 0.2f) else ShotblastSurfaceVariant,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, ShotblastCyan) else null,
                                    modifier = Modifier.clickable { selectedSoundTrack = track }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.MusicNote,
                                            contentDescription = null,
                                            tint = if (isSelected) ShotblastCyan else Color.Gray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = track,
                                            color = if (isSelected) ShotblastCyan else Color.LightGray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual Anime / Cyberpunk Filter
                        Text("Visual Filter & Style", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ANIME_FILTERS.forEach { filter ->
                                val isSelected = selectedFilter == filter
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) ShotblastCoral.copy(alpha = 0.2f) else ShotblastSurfaceVariant,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, ShotblastCoral) else null,
                                    modifier = Modifier.clickable { selectedFilter = filter }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = if (isSelected) ShotblastCoral else Color.Gray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = filter,
                                            color = if (isSelected) ShotblastCoral else Color.LightGray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Full Video: Custom Thumbnail & Tags
                        Text("Custom Video Thumbnail", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    pickCustomThumbnail.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ShotblastSurfaceVariant,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Choose Image from Gallery", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tags Input
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = tagInput,
                                onValueChange = { tagInput = it },
                                label = { Text("Add Tag (e.g. anime, 4k)", color = Color.Gray) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ShotblastCyan,
                                    unfocusedBorderColor = ShotblastBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (tagInput.isNotBlank()) {
                                        val cleanTag = if (tagInput.startsWith("#")) tagInput.trim() else "#${tagInput.trim()}"
                                        if (!tagsList.contains(cleanTag)) {
                                            tagsList = tagsList + cleanTag
                                        }
                                        tagInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ShotblastCyan, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Add")
                            }
                        }

                        // Added tags chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            tagsList.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = ShotblastSurfaceVariant
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(tag, color = Color.White, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Remove tag",
                                            tint = Color.LightGray,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { tagsList = tagsList - tag }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Visibility / Privacy Setting
                    Text("Visibility", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("Public", Icons.Filled.Public, "Anyone can search and watch"),
                            Triple("Unlisted", Icons.Filled.Link, "Anyone with link can watch"),
                            Triple("Private", Icons.Filled.VisibilityOff, "Only you can watch")
                        ).forEach { (vis, icon, desc) ->
                            val isSelected = visibility == vis
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) ShotblastCyan.copy(alpha = 0.2f) else ShotblastSurfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, ShotblastCyan) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { visibility = vis }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = vis,
                                        tint = if (isSelected) ShotblastCyan else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        vis,
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Advanced Switches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Allow Comments", color = Color.White, fontSize = 13.sp)
                            Text("Viewers can engage with your video", color = Color.Gray, fontSize = 11.sp)
                        }
                        Switch(
                            checked = allowComments,
                            onCheckedChange = { allowComments = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ShotblastCyan,
                                checkedTrackColor = ShotblastCyan.copy(alpha = 0.4f)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Allow Offline Downloads", color = Color.White, fontSize = 13.sp)
                            Text("Let subscribers save for offline watching", color = Color.Gray, fontSize = 11.sp)
                        }
                        Switch(
                            checked = allowDownloads,
                            onCheckedChange = { allowDownloads = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ShotblastCyan,
                                checkedTrackColor = ShotblastCyan.copy(alpha = 0.4f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Upload Progress Bar during Publishing
                    if (isPublishing) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = ShotblastSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = publishingStatusText,
                                        color = ShotblastCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${(publishingProgress * 100).toInt()}%",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { publishingProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = if (isShort) ShotblastCoral else ShotblastCyan,
                                    trackColor = Color.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Action Buttons (Back & Publish)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showUploadForm = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ShotblastSurfaceVariant,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Back")
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                if (isPublishing) return@Button
                                isPublishing = true
                                scope.launch {
                                    val generatedId = "vid_${System.currentTimeMillis()}"
                                    var finalVideoUrl = selectedVideoUri?.toString() ?: directVideoUrl
                                    var finalThumbUri = extractedThumbnailUri

                                    publishingStatusText = "Connecting to Firebase (${FirebaseVideoService.PROJECT_ID})..."
                                    publishingProgress = 0.20f
                                    delay(200)

                                    // Upload thumbnail to Firebase Storage if available
                                    if (extractedThumbnailUri != null && extractedThumbnailUri!!.startsWith("content://")) {
                                        publishingStatusText = "Uploading thumbnail to Firebase Storage..."
                                        publishingProgress = 0.40f
                                        try {
                                            val uploadedThumb = FirebaseVideoService.uploadThumbnail(
                                                Uri.parse(extractedThumbnailUri!!),
                                                generatedId
                                            )
                                            if (uploadedThumb != null) {
                                                finalThumbUri = uploadedThumb
                                            }
                                        } catch (e: Exception) {
                                            // Fallback to local
                                        }
                                    }

                                    // Upload video media to Firebase Storage if picked from gallery
                                    if (selectedVideoUri != null) {
                                        publishingStatusText = "Uploading video to Firebase Storage..."
                                        try {
                                            val uploadedVideo = FirebaseVideoService.uploadVideo(
                                                selectedVideoUri!!,
                                                generatedId
                                            ) { progress ->
                                                publishingProgress = 0.45f + (progress * 0.40f)
                                            }
                                            if (uploadedVideo != null) {
                                                finalVideoUrl = uploadedVideo
                                            }
                                        } catch (e: Exception) {
                                            // Fallback to local
                                        }
                                    }

                                    publishingStatusText = "Saving to Cloud Firestore..."
                                    publishingProgress = 0.90f
                                    delay(200)

                                    val currentProfile = com.example.data.AuthManager.userProfile.value
                                    val createdVideo = VideoRepository.addUploadedVideo(
                                        title = videoTitle.ifBlank { if (isShort) "Awesome Short" else "New Shotblast Video" },
                                        url = finalVideoUrl,
                                        category = selectedCategory,
                                        description = videoDescription,
                                        isShort = isShort,
                                        thumbnailUri = finalThumbUri,
                                        thumbnailDrawable = customThumbnailRes,
                                        durationStr = videoDurationStr,
                                        durationSeconds = videoDurationSec,
                                        tags = tagsList,
                                        isFromGallery = isFromDeviceGallery,
                                        visibility = visibility,
                                        soundTrack = if (isShort) selectedSoundTrack else null,
                                        channelName = currentProfile?.channelName,
                                        channelSubscribers = currentProfile?.let { "${it.subscribersCount} subscribers" },
                                        userId = currentProfile?.uid,
                                        channelHandle = currentProfile?.channelHandle
                                    )

                                    publishingProgress = 1.0f
                                    publishingStatusText = "Published to Firebase successfully!"
                                    delay(250)
                                    onVideoCreated(createdVideo)
                                    onDismiss()
                                }
                            },
                            enabled = !isPublishing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isShort) ShotblastCoral else ShotblastCyan,
                                contentColor = if (isShort) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("publish_video_button")
                        ) {
                            if (isPublishing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = if (isShort) Color.White else Color.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Uploading...")
                            } else {
                                Icon(
                                    Icons.Filled.Upload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (isShort) "Publish Short" else "Publish Full Video",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateHubOptionRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    badge: String? = null,
    badgeColor: Color = ShotblastCyan,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = ShotblastSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBg.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconBg,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = badgeColor.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = badge,
                                color = badgeColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SampleGalleryPresetCard(
    preset: SampleGalleryPreset,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = ShotblastSurfaceVariant,
        modifier = Modifier
            .width(140.dp)
            .testTag("gallery_preset_${preset.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
            ) {
                Image(
                    painter = painterResource(id = preset.thumbnailRes),
                    contentDescription = preset.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Duration badge
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = preset.durationFormatted,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                // Type badge
                Surface(
                    color = if (preset.isShort) ShotblastCoral else ShotblastCyan,
                    shape = RoundedCornerShape(bottomEnd = 4.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = if (preset.isShort) "SHORT" else "VIDEO",
                        color = if (preset.isShort) Color.White else Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = preset.title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = preset.category,
                    color = ShotblastCyan,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun VideoPreviewCard(
    videoUrl: String,
    isShort: Boolean,
    extractedBitmap: Bitmap?,
    extractedThumbnailUri: String?,
    customThumbnailRes: Int?,
    durationStr: String,
    dimensions: String,
    fileSize: String,
    sourceLabel: String,
    isExtracting: Boolean,
    onPickFromGallery: () -> Unit
) {
    val context = LocalContext.current
    var isPlayingPreview by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ShotblastSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Video / Thumbnail View Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (isShort) 9f / 12f else 16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (isPlayingPreview) {
                    // Embedded Mini ExoPlayer
                    val previewPlayer = remember(videoUrl) {
                        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                            .setUserAgent("Mozilla/5.0 (Linux; Android 14)")
                            .setAllowCrossProtocolRedirects(true)
                        val defaultDataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
                        val mediaSourceFactory = DefaultMediaSourceFactory(defaultDataSourceFactory)

                        ExoPlayer.Builder(context)
                            .setMediaSourceFactory(mediaSourceFactory)
                            .build().apply {
                                val item = MediaItem.fromUri(videoUrl)
                                setMediaItem(item)
                                repeatMode = Player.REPEAT_MODE_ONE
                                prepare()
                                playWhenReady = true
                            }
                    }

                    DisposableEffect(previewPlayer) {
                        onDispose {
                            previewPlayer.release()
                        }
                    }

                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = previewPlayer
                                useController = true
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Close preview button
                    IconButton(
                        onClick = { isPlayingPreview = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .size(28.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close Preview", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                } else {
                    // Image Thumbnail Preview
                    when {
                        extractedBitmap != null -> {
                            Image(
                                bitmap = extractedBitmap.asImageBitmap(),
                                contentDescription = "Video Frame",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        extractedThumbnailUri != null -> {
                            AsyncImage(
                                model = extractedThumbnailUri,
                                contentDescription = "Custom Thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        customThumbnailRes != null -> {
                            Image(
                                painter = painterResource(id = customThumbnailRes),
                                contentDescription = "Thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            AsyncImage(
                                model = R.drawable.ic_shotblast_logo,
                                contentDescription = "Default Thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Play Preview Button Overlay
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(1.5.dp, if (isShort) ShotblastCoral else ShotblastCyan, CircleShape)
                            .clickable { isPlayingPreview = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Play Video Preview",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Duration Badge
                    Surface(
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = durationStr,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Source Badge
                    Surface(
                        color = (if (isShort) ShotblastCoral else ShotblastCyan).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = sourceLabel.uppercase(),
                            color = if (isShort) Color.White else Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    // Extracting indicator
                    if (isExtracting) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = ShotblastCyan, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Extracting video metadata & frame...", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Movie, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(dimensions, color = Color.LightGray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(Icons.Filled.VideoFile, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(fileSize, color = Color.LightGray, fontSize = 11.sp)
                }

                Text(
                    text = if (isShort) "Vertical Short" else "Full Video",
                    color = if (isShort) ShotblastCoral else ShotblastCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
