package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

data class GalleryVideoMetadata(
    val uri: Uri,
    val fileName: String,
    val titleSuggestion: String,
    val sizeBytes: Long,
    val sizeFormatted: String,
    val durationMs: Long,
    val durationFormatted: String,
    val width: Int,
    val height: Int,
    val isVertical: Boolean,
    val isShortRecommended: Boolean,
    val thumbnailUri: String? = null,
    val thumbnailBitmap: Bitmap? = null
)

data class SampleGalleryPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val videoUrl: String,
    val thumbnailRes: Int,
    val durationFormatted: String,
    val durationSeconds: Long,
    val isShort: Boolean,
    val category: String,
    val tags: List<String>
)

object GalleryVideoHelper {

    val SAMPLE_GALLERY_PRESETS = listOf(
        SampleGalleryPreset(
            id = "preset_anime_01",
            title = "Ultimate Anime Sakuga Clash",
            subtitle = "High-energy combat sequence with cyberpunk sound",
            videoUrl = "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/echo-hereweare.mp4",
            thumbnailRes = R.drawable.thumb_anime_fight,
            durationFormatted = "0:52",
            durationSeconds = 52,
            isShort = true,
            category = "Anime",
            tags = listOf("#anime", "#shorts", "#sakuga", "#action")
        ),
        SampleGalleryPreset(
            id = "preset_waterfall_02",
            title = "Hidden Waterfall Paradise",
            subtitle = "Vertical rainforest mist captured in 4K HDR",
            videoUrl = "https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4",
            thumbnailRes = R.drawable.thumb_short_waterfall,
            durationFormatted = "0:15",
            durationSeconds = 15,
            isShort = true,
            category = "Nature",
            tags = listOf("#shorts", "#waterfall", "#nature", "#relax")
        ),
        SampleGalleryPreset(
            id = "preset_beach_03",
            title = "Exotic Tropical Coastline Drone Tour",
            subtitle = "Full-length 16:9 4K travel exploration",
            videoUrl = "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/big_buck_bunny.mp4",
            thumbnailRes = R.drawable.thumb_tropical_beach,
            durationFormatted = "9:56",
            durationSeconds = 596,
            isShort = false,
            category = "Travel",
            tags = listOf("#travel", "#4k", "#drone", "#beaches")
        ),
        SampleGalleryPreset(
            id = "preset_game_04",
            title = "Cyber Speedrun Championship Finals",
            subtitle = "World record glitchless speedrun showcase",
            videoUrl = "https://flutter.github.io/assets-for-api-docs/assets/videos/butterfly.mp4",
            thumbnailRes = R.drawable.thumb_short_game,
            durationFormatted = "6:42",
            durationSeconds = 402,
            isShort = false,
            category = "Gaming",
            tags = listOf("#gaming", "#speedrun", "#esports")
        )
    )

    suspend fun extractMetadata(context: Context, uri: Uri): GalleryVideoMetadata = withContext(Dispatchers.IO) {
        var fileName = "Gallery_Video_${System.currentTimeMillis() % 10000}"
        var sizeBytes = 0L

        // 1. Query ContentResolver for display name and size
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        cursor.getString(nameIndex)?.let { fileName = it }
                    }
                    if (sizeIndex != -1) {
                        sizeBytes = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (_: Exception) {}

        // Clean title suggestion from filename (remove extension and snake/kebab-case)
        val cleanName = fileName.substringBeforeLast(".")
            .replace("_", " ")
            .replace("-", " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }
        val titleSuggestion = cleanName.ifBlank { "My Imported Video" }

        // 2. Extract media metadata via MediaMetadataRetriever
        var durationMs = 0L
        var width = 0
        var height = 0
        var savedThumbnailUri: String? = null
        var frameBitmap: Bitmap? = null

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0

            // Check rotation
            val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
            if (rotation == 90 || rotation == 270) {
                val temp = width
                width = height
                height = temp
            }

            // Extract frame at 1 sec or middle
            val targetTimeUs = if (durationMs > 2000L) 1000000L else (durationMs * 500L)
            frameBitmap = retriever.getFrameAtTime(targetTimeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            if (frameBitmap == null) {
                frameBitmap = retriever.frameAtTime
            }

            // Save thumbnail to cache file so Coil can load it directly
            frameBitmap?.let { bmp ->
                try {
                    val cacheFile = File(context.cacheDir, "thumb_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(cacheFile).use { out ->
                        bmp.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                    savedThumbnailUri = Uri.fromFile(cacheFile).toString()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // Ignore extraction errors gracefully
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }

        val isVertical = (height > width && height > 0)
        val isShort = isVertical || (durationMs in 1..65000L)

        val durationFormatted = formatDuration(durationMs)
        val sizeFormatted = formatFileSize(sizeBytes)

        GalleryVideoMetadata(
            uri = uri,
            fileName = fileName,
            titleSuggestion = titleSuggestion,
            sizeBytes = sizeBytes,
            sizeFormatted = sizeFormatted,
            durationMs = durationMs,
            durationFormatted = durationFormatted,
            width = width,
            height = height,
            isVertical = isVertical,
            isShortRecommended = isShort,
            thumbnailUri = savedThumbnailUri,
            thumbnailBitmap = frameBitmap
        )
    }

    private fun formatDuration(millis: Long): String {
        if (millis <= 0) return "0:30"
        val totalSec = millis / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format(Locale.ROOT, "%d:%02d", min, sec)
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "12.4 MB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1.0) {
            String.format(Locale.ROOT, "%.1f MB", mb)
        } else {
            String.format(Locale.ROOT, "%.0f KB", kb)
        }
    }

    suspend fun saveImageUriToCache(context: Context, imageUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val destFile = File(context.filesDir, "custom_thumb_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(imageUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destFile).toString()
        } catch (_: Exception) {
            imageUri.toString()
        }
    }
}
