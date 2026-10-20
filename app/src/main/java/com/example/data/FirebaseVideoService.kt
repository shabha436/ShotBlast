package com.example.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.R
import com.example.model.CommentItem
import com.example.model.VideoItem
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Service managing real-time Cloud Firestore synchronization and
 * Firebase Storage uploads for SHOTBLAST videos and thumbnails.
 */
object FirebaseVideoService {

    private const val TAG = "FirebaseVideoService"
    const val PROJECT_ID = "shotblast-8cd76"
    const val STORAGE_BUCKET = "shotblast-8cd76.firebasestorage.app"

    val isFirebaseInitialized: Boolean
        get() = try {
            FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()
        } catch (e: Exception) {
            false
        }

    val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Firestore instance", e)
            null
        }

    val storage: FirebaseStorage?
        get() = try {
            FirebaseStorage.getInstance("gs://$STORAGE_BUCKET")
        } catch (e: Exception) {
            try {
                FirebaseStorage.getInstance()
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to get FirebaseStorage instance", ex)
                null
            }
        }

    val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }

    /**
     * Upload a thumbnail image file directly to Firebase Storage bucket.
     * Returns the permanent HTTPS download URL if successful.
     */
    suspend fun uploadThumbnail(uri: Uri, videoId: String): String? {
        val st = storage ?: return null
        return try {
            val thumbRef = st.reference.child("thumbnails/$videoId.jpg")
            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .setCustomMetadata("videoId", videoId)
                .build()

            thumbRef.putFile(uri, metadata).awaitTask()
            val downloadUrl = thumbRef.downloadUrl.awaitTask().toString()
            Log.d(TAG, "Thumbnail uploaded to Storage: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.w(TAG, "Thumbnail upload to Firebase Storage failed or skipped: ${e.message}")
            null
        }
    }

    /**
     * Upload a video media file directly to Firebase Storage bucket with progress updates.
     */
    suspend fun uploadVideo(
        uri: Uri,
        videoId: String,
        onProgress: (Float) -> Unit = {}
    ): String? {
        val st = storage ?: return null
        return try {
            val videoRef = st.reference.child("videos/$videoId.mp4")
            val metadata = StorageMetadata.Builder()
                .setContentType("video/mp4")
                .setCustomMetadata("videoId", videoId)
                .build()

            val uploadTask = videoRef.putFile(uri, metadata)
            uploadTask.addOnProgressListener { snapshot ->
                val progress = if (snapshot.totalByteCount > 0) {
                    snapshot.bytesTransferred.toFloat() / snapshot.totalByteCount.toFloat()
                } else {
                    0f
                }
                onProgress(progress)
            }

            uploadTask.awaitTask()
            val downloadUrl = videoRef.downloadUrl.awaitTask().toString()
            Log.d(TAG, "Video uploaded to Storage: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.w(TAG, "Video upload to Firebase Storage failed or skipped: ${e.message}")
            null
        }
    }

    /**
     * Syncs a video item to Cloud Firestore 'videos' collection.
     */
    suspend fun syncVideoToFirestore(video: VideoItem) {
        val db = firestore ?: return
        try {
            val videoDoc = db.collection("videos").document(video.id)
            val data = hashMapOf(
                "id" to video.id,
                "title" to video.title,
                "videoUrl" to video.videoUrl,
                "thumbnailUri" to (video.thumbnailUri ?: ""),
                "durationStr" to video.durationStr,
                "durationSeconds" to video.durationSeconds,
                "viewsStr" to video.viewsStr,
                "viewsCount" to video.viewsCount,
                "timeAgo" to video.timeAgo,
                "channelName" to video.channelName,
                "channelSubscribers" to video.channelSubscribers,
                "isVerified" to video.isVerified,
                "category" to video.category,
                "tags" to video.tags,
                "description" to video.description,
                "likeCount" to video.likeCount,
                "isShort" to video.isShort,
                "visibility" to video.visibility,
                "soundTrack" to (video.soundTrack ?: ""),
                "userId" to video.userId,
                "channelHandle" to video.channelHandle,
                "syncedAt" to FieldValue.serverTimestamp()
            )
            videoDoc.set(data, SetOptions.merge()).awaitTask()
            Log.d(TAG, "Video synced to Firestore: ${video.id}")
        } catch (e: Exception) {
            Log.w(TAG, "Firestore syncVideo failed: ${e.message}")
        }
    }

    /**
     * Realtime observation of Cloud Firestore 'videos' collection.
     */
    fun observeVideos(): Flow<List<VideoItem>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close()
            return@callbackFlow
        }

        val registration: ListenerRegistration = db.collection("videos")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Firestore video listener error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val title = doc.getString("title") ?: "Video"
                            val videoUrl = doc.getString("videoUrl") ?: ""
                            val thumbnailUri = doc.getString("thumbnailUri")?.takeIf { it.isNotBlank() }
                            val durationStr = doc.getString("durationStr") ?: "3:45"
                            val durationSeconds = doc.getLong("durationSeconds") ?: 225L
                            val viewsStr = doc.getString("viewsStr") ?: "1 view"
                            val viewsCount = doc.getLong("viewsCount") ?: 1L
                            val timeAgo = doc.getString("timeAgo") ?: "Recently"
                            val channelName = doc.getString("channelName") ?: "Shotblast Creator"
                            val channelSubscribers = doc.getString("channelSubscribers") ?: "1K subscribers"
                            val isVerified = doc.getBoolean("isVerified") ?: false
                            val category = doc.getString("category") ?: "General"
                            @Suppress("UNCHECKED_CAST")
                            val tags = (doc.get("tags") as? List<String>) ?: listOf("#shotblast")
                            val description = doc.getString("description") ?: ""
                            val likeCount = doc.getLong("likeCount") ?: 0L
                            val isShort = doc.getBoolean("isShort") ?: false
                            val isFromGallery = doc.getBoolean("isFromGallery") ?: false
                            val visibility = doc.getString("visibility") ?: "Public"
                            val soundTrack = doc.getString("soundTrack")?.takeIf { it.isNotBlank() }
                            val userId = doc.getString("userId") ?: ""
                            val channelHandle = doc.getString("channelHandle") ?: ""

                            VideoItem(
                                id = id,
                                title = title,
                                videoUrl = videoUrl,
                                thumbnailDrawable = if (isShort) R.drawable.thumb_short_game else R.drawable.thumb_nature_travel,
                                thumbnailUri = thumbnailUri,
                                durationStr = durationStr,
                                durationSeconds = durationSeconds,
                                viewsStr = viewsStr,
                                viewsCount = viewsCount,
                                timeAgo = timeAgo,
                                channelName = channelName,
                                channelSubscribers = channelSubscribers,
                                isVerified = isVerified,
                                category = category,
                                tags = tags,
                                description = description,
                                likeCount = likeCount,
                                isLiked = false,
                                isDisliked = false,
                                isSaved = false,
                                isDownloaded = false,
                                isSubscribed = false,
                                isShort = isShort,
                                isFromGallery = isFromGallery,
                                visibility = visibility,
                                soundTrack = soundTrack,
                                userId = userId,
                                channelHandle = channelHandle
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parsing Firestore doc ${doc.id}", e)
                            null
                        }
                    }
                    trySend(items)
                }
            }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Update like count in Firestore.
     */
    suspend fun updateLike(videoId: String, likeCount: Long) {
        val db = firestore ?: return
        try {
            db.collection("videos").document(videoId)
                .update("likeCount", likeCount).awaitTask()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore updateLike failed: ${e.message}")
        }
    }

    /**
     * Add a comment to Firestore under videos/{videoId}/comments.
     */
    suspend fun addComment(videoId: String, comment: CommentItem) {
        val db = firestore ?: return
        try {
            val commentData = hashMapOf(
                "id" to comment.id,
                "authorName" to comment.authorName,
                "authorHandle" to comment.authorHandle,
                "timeAgo" to comment.timeAgo,
                "text" to comment.text,
                "likesCount" to comment.likesCount,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("videos").document(videoId)
                .collection("comments").document(comment.id)
                .set(commentData).awaitTask()
            Log.d(TAG, "Comment added to Firestore: ${comment.id}")
        } catch (e: Exception) {
            Log.w(TAG, "Firestore addComment failed: ${e.message}")
        }
    }
}

/**
 * Extension helper converting Android Task into a coroutine suspend function.
 */
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        continuation.resume(result)
    }
    addOnFailureListener { exception ->
        continuation.resumeWithException(exception)
    }
    addOnCanceledListener {
        continuation.cancel()
    }
}
