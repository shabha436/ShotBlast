package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VideoItem
import com.example.ui.theme.ShotblastBorder
import com.example.ui.theme.ShotblastCyan
import com.example.ui.theme.ShotblastSurface
import com.example.util.GalleryVideoHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoOptionsBottomSheet(
    video: VideoItem,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSaveWatchLater: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onSpeedSelected: (Float) -> Unit = {},
    onUpdateThumbnail: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pickThumbnailForExisting = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val cachedUri = GalleryVideoHelper.saveImageUriToCache(context, uri)
                onUpdateThumbnail(cachedUri)
                onDismiss()
            }
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ShotblastSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Video Options",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = video.title,
                color = Color.Gray,
                fontSize = 13.sp,
                maxLines = 1,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            HorizontalDivider(color = ShotblastBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // Watch Later item
            OptionRowItem(
                icon = if (video.isSaved) Icons.Filled.Bookmark else Icons.Filled.PlaylistAdd,
                title = if (video.isSaved) "Remove from Watch Later" else "Save to Watch Later",
                subtitle = "Save to your personal Watch Later list",
                isHighlighted = video.isSaved,
                onClick = {
                    onSaveWatchLater()
                    onDismiss()
                }
            )

            // Share item
            OptionRowItem(
                icon = Icons.Filled.Share,
                title = "Share Video",
                subtitle = "Share with friends & other apps",
                onClick = {
                    onShare()
                    onDismiss()
                }
            )

            // Download item
            OptionRowItem(
                icon = if (video.isDownloaded) Icons.Filled.FileDownloadDone else Icons.Filled.Download,
                title = if (video.isDownloaded) "Downloaded Offline" else "Download Video",
                subtitle = "Save video for offline watching",
                isHighlighted = video.isDownloaded,
                onClick = {
                    onDownload()
                    onDismiss()
                }
            )

            // Change Thumbnail from Gallery Photos
            OptionRowItem(
                icon = Icons.Filled.PhotoLibrary,
                title = "Change Thumbnail from Gallery",
                subtitle = "Pick custom photo from your device gallery",
                isHighlighted = true,
                onClick = {
                    pickThumbnailForExisting.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )

            HorizontalDivider(color = ShotblastBorder, modifier = Modifier.padding(vertical = 6.dp))

            // Quality
            OptionRowItem(
                icon = Icons.Filled.HighQuality,
                title = "Video Quality",
                subtitle = "Auto (1080p 60fps Ultra HD)",
                onClick = onDismiss
            )

            // Speed
            OptionRowItem(
                icon = Icons.Filled.Speed,
                title = "Playback Speed",
                subtitle = "Normal (1.0x)",
                onClick = onDismiss
            )

            // Sleep Timer
            OptionRowItem(
                icon = Icons.Filled.Timer,
                title = "Sleep Timer",
                subtitle = "Off",
                onClick = onDismiss
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OptionRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isHighlighted) ShotblastCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isHighlighted) ShotblastCyan else Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isHighlighted) ShotblastCyan else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}
