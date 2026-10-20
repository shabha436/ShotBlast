package com.example.data

import com.example.R
import com.example.model.ChannelItem
import com.example.model.CommentItem
import com.example.model.VideoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object VideoRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        startFirestoreSync()
    }

    private fun startFirestoreSync() {
        repositoryScope.launch {
            try {
                FirebaseVideoService.observeVideos().collect { firestoreList ->
                    if (firestoreList.isNotEmpty()) {
                        _videos.update { current ->
                            val remoteIds = firestoreList.map { it.id }.toSet()
                            val localPresets = current.filter { it.id !in remoteIds && !it.id.startsWith("custom_") }
                            firestoreList + localPresets
                        }
                    }
                }
            } catch (e: Exception) {
                // Keep local data safe
            }
        }
    }

    // Verified 200 OK direct MP4 video streams that play without 403 authentication errors
    private val sampleVideos = listOf(
        VideoItem(
            id = "vid_travel_01",
            title = "The Most Beautiful Places on Earth | 4K Cinematic Travel Video",
            videoUrl = "https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4",
            thumbnailDrawable = R.drawable.thumb_nature_travel,
            durationStr = "12:48",
            durationSeconds = 768,
            viewsStr = "2.4M views",
            viewsCount = 2400000,
            timeAgo = "3 days ago",
            channelName = "Nature Vibes",
            channelSubscribers = "1.2M subscribers",
            isVerified = true,
            category = "Travel",
            tags = listOf("#travel", "#nature", "#4k", "#cinematic"),
            description = "Explore the most beautiful places on Earth in stunning 4K! From majestic mountain peaks and alpine lakes to serene valleys, this cinematic travel video will take you on an unforgettable journey around the globe.",
            likeCount = 68000,
            isLiked = false,
            isDisliked = false,
            isSaved = false,
            isDownloaded = false,
            isSubscribed = false,
            isShort = false
        ),
        VideoItem(
            id = "short_dog_01",
            title = "Cute Dog Moments That Will Make You Smile #shorts",
            videoUrl = "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4",
            thumbnailDrawable = R.drawable.thumb_short_dog,
            durationStr = "0:45",
            durationSeconds = 45,
            viewsStr = "1.2M views",
            viewsCount = 1200000,
            timeAgo = "1 day ago",
            channelName = "Pet Lovers",
            channelSubscribers = "450K subscribers",
            isVerified = true,
            category = "Shorts",
            tags = listOf("#shorts", "#dogs", "#cute", "#puppy"),
            description = "Cutest golden retriever moments captured on camera! Don't forget to like and subscribe for daily doses of pure joy.",
            likeCount = 142000,
            isLiked = true,
            isDisliked = false,
            isSaved = true,
            isDownloaded = false,
            isSubscribed = true,
            isShort = true
        ),
        VideoItem(
            id = "short_game_01",
            title = "Best Game Moments #shorts",
            videoUrl = "https://flutter.github.io/assets-for-api-docs/assets/videos/butterfly.mp4",
            thumbnailDrawable = R.drawable.thumb_short_game,
            durationStr = "0:32",
            durationSeconds = 32,
            viewsStr = "856K views",
            viewsCount = 856000,
            timeAgo = "2 days ago",
            channelName = "Gaming Zone",
            channelSubscribers = "890K subscribers",
            isVerified = true,
            category = "Gaming",
            tags = listOf("#shorts", "#gaming", "#cyberpunk", "#esports"),
            description = "Insane clutch moment in the Cyber Arena championship finals! Did you see that lightning finisher?",
            likeCount = 98000,
            isLiked = false,
            isDisliked = false,
            isSaved = false,
            isDownloaded = false,
            isSubscribed = false,
            isShort = true
        ),
        VideoItem(
            id = "short_waterfall_01",
            title = "Beautiful Waterfall in 4K #shorts",
            videoUrl = "https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4",
            thumbnailDrawable = R.drawable.thumb_short_waterfall,
            durationStr = "0:37",
            durationSeconds = 37,
            viewsStr = "642K views",
            viewsCount = 642000,
            timeAgo = "4 days ago",
            channelName = "Nature Vibes",
            channelSubscribers = "1.2M subscribers",
            isVerified = true,
            category = "Nature",
            tags = listOf("#shorts", "#waterfall", "#relaxing", "#nature"),
            description = "Listen to the tranquil sound of this hidden tropical jungle waterfall with a miniature rainbow mist.",
            likeCount = 74000,
            isLiked = false,
            isDisliked = false,
            isSaved = false,
            isDownloaded = false,
            isSubscribed = false,
            isShort = true
        ),
        VideoItem(
            id = "vid_anime_01",
            title = "Top 10 Anime Fights That Will Blow Your Mind",
            videoUrl = "https://media.w3.org/2010/05/sintel/trailer.mp4",
            thumbnailDrawable = R.drawable.thumb_anime_fight,
            durationStr = "10:21",
            durationSeconds = 621,
            viewsStr = "3.8M views",
            viewsCount = 3800000,
            timeAgo = "5 days ago",
            channelName = "Anime World",
            channelSubscribers = "2.1M subscribers",
            isVerified = true,
            category = "Anime",
            tags = listOf("#anime", "#top10", "#action", "#animation"),
            description = "Ranking the most incredible, high-octane animated combat sequences in anime history with mind-blowing sakuga and sound design.",
            likeCount = 184000,
            isLiked = false,
            isDisliked = false,
            isSaved = false,
            isDownloaded = false,
            isSubscribed = true,
            isShort = false
        ),
        VideoItem(
            id = "vid_beach_01",
            title = "Top 10 Most Beautiful Beaches in the World",
            videoUrl = "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/big_buck_bunny.mp4",
            thumbnailDrawable = R.drawable.thumb_tropical_beach,
            durationStr = "11:32",
            durationSeconds = 692,
            viewsStr = "1.5M views",
            viewsCount = 1500000,
            timeAgo = "1 week ago",
            channelName = "Nature Vibes",
            channelSubscribers = "1.2M subscribers",
            isVerified = true,
            category = "Travel",
            tags = listOf("#travel", "#beaches", "#summer", "#vacation"),
            description = "Crystal clear waters, pristine white sands, and swaying palms. Discover the highest-rated paradise shores across the globe.",
            likeCount = 52000,
            isLiked = false,
            isDisliked = false,
            isSaved = false,
            isDownloaded = false,
            isSubscribed = false,
            isShort = false
        )
    )

    private val initialComments = mapOf(
        "vid_travel_01" to listOf(
            CommentItem(
                id = "c1",
                authorName = "TravelWithMe",
                authorHandle = "@TravelWithMe",
                timeAgo = "2 days ago",
                text = "This is absolutely breathtaking! Nature is truly the best! ❤️",
                likesCount = 1200,
                isLiked = true
            ),
            CommentItem(
                id = "c2",
                authorName = "Alex Horizon",
                authorHandle = "@AlexHorizon",
                timeAgo = "1 day ago",
                text = "The 4K camera quality in the drone shots is insane. What gear did you shoot this with?",
                likesCount = 345,
                isLiked = false
            ),
            CommentItem(
                id = "c3",
                authorName = "Sophia Wanderlust",
                authorHandle = "@SophiaW",
                timeAgo = "18 hours ago",
                text = "Added Switzerland and Lake Oeschinen to my travel bucket list immediately!",
                likesCount = 89,
                isLiked = false
            )
        )
    )

    private val _videos = MutableStateFlow(sampleVideos)
    val videos: StateFlow<List<VideoItem>> = _videos.asStateFlow()

    private val _comments = MutableStateFlow(initialComments)
    val comments: StateFlow<Map<String, List<CommentItem>>> = _comments.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchHistory = MutableStateFlow(
        listOf(
            "The Most Beautiful Places",
            "Cute Dog #shorts",
            "Gaming Zone",
            "Nature Vibes",
            "Cyber Arena Finals",
            "Anime opening"
        )
    )
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _channels = MutableStateFlow(
        listOf(
            ChannelItem("ch1", "Nature Vibes", "@NatureVibes", "1.2M subscribers", true, 86),
            ChannelItem("ch2", "Anime World", "@AnimeWorld", "2.1M subscribers", true, 142),
            ChannelItem("ch3", "Gaming Zone", "@GamingZone", "890K subscribers", false, 230),
            ChannelItem("ch4", "Pet Lovers", "@PetLovers", "450K subscribers", true, 95),
            ChannelItem("ch5", "TechVision", "@TechVision", "1.8M subscribers", false, 110)
        )
    )
    val channels: StateFlow<List<ChannelItem>> = _channels.asStateFlow()

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addSearchHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        _searchHistory.update { current ->
            (listOf(trimmed) + current.filterNot { it.equals(trimmed, ignoreCase = true) }).take(15)
        }
    }

    fun removeSearchHistory(query: String) {
        _searchHistory.update { current ->
            current.filterNot { it.equals(query, ignoreCase = true) }
        }
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
    }

    fun toggleLike(videoId: String) {
        var updatedCount = 0L
        _videos.update { list ->
            list.map { video ->
                if (video.id == videoId) {
                    val wasLiked = video.isLiked
                    val newCount = if (wasLiked) (video.likeCount - 1L).coerceAtLeast(0L) else video.likeCount + 1L
                    updatedCount = newCount
                    video.copy(
                        isLiked = !wasLiked,
                        isDisliked = false,
                        likeCount = newCount
                    )
                } else video
            }
        }
        repositoryScope.launch {
            try {
                FirebaseVideoService.updateLike(videoId, updatedCount)
            } catch (e: Exception) {
                // Ignore failure
            }
        }
    }

    fun toggleDislike(videoId: String) {
        _videos.update { list ->
            list.map { video ->
                if (video.id == videoId) {
                    val wasDisliked = video.isDisliked
                    video.copy(
                        isDisliked = !wasDisliked,
                        isLiked = false,
                        likeCount = if (video.isLiked) video.likeCount - 1 else video.likeCount
                    )
                } else video
            }
        }
    }

    fun toggleSave(videoId: String): Boolean {
        var newState = false
        _videos.update { list ->
            list.map { video ->
                if (video.id == videoId) {
                    newState = !video.isSaved
                    video.copy(isSaved = newState)
                } else video
            }
        }
        return newState
    }

    fun toggleDownload(videoId: String): Boolean {
        var newState = false
        _videos.update { list ->
            list.map { video ->
                if (video.id == videoId) {
                    newState = !video.isDownloaded
                    video.copy(isDownloaded = newState)
                } else video
            }
        }
        return newState
    }

    fun toggleChannelSubscription(channelName: String): Boolean {
        var newSubscribed = false
        _channels.update { list ->
            list.map { ch ->
                if (ch.name.equals(channelName, ignoreCase = true)) {
                    newSubscribed = !ch.isSubscribed
                    ch.copy(isSubscribed = newSubscribed)
                } else ch
            }
        }
        _videos.update { list ->
            list.map { video ->
                if (video.channelName.equals(channelName, ignoreCase = true)) {
                    video.copy(isSubscribed = newSubscribed)
                } else video
            }
        }
        return newSubscribed
    }

    fun addComment(videoId: String, text: String, author: String = "You") {
        if (text.isBlank()) return
        val newComment = CommentItem(
            id = "c_${System.currentTimeMillis()}",
            authorName = author,
            authorHandle = "@you",
            timeAgo = "Just now",
            text = text.trim(),
            likesCount = 0,
            isLiked = false
        )
        _comments.update { map ->
            val currentList = map[videoId] ?: emptyList()
            map + (videoId to (listOf(newComment) + currentList))
        }
        repositoryScope.launch {
            try {
                FirebaseVideoService.addComment(videoId, newComment)
            } catch (e: Exception) {
                // Ignore failure
            }
        }
    }

    fun addUploadedVideo(
        title: String,
        url: String,
        category: String = "General",
        description: String = "",
        isShort: Boolean = false,
        thumbnailUri: String? = null,
        thumbnailDrawable: Int? = null,
        durationStr: String? = null,
        durationSeconds: Long? = null,
        tags: List<String> = emptyList(),
        isFromGallery: Boolean = false,
        visibility: String = "Public",
        soundTrack: String? = null,
        channelName: String? = null,
        channelSubscribers: String? = null,
        userId: String? = null,
        channelHandle: String? = null
    ): VideoItem {
        val assignedThumbnail = thumbnailDrawable ?: if (isShort) R.drawable.thumb_short_game else R.drawable.thumb_nature_travel
        val durStr = durationStr ?: if (isShort) "0:45" else "5:20"
        val durSec = durationSeconds ?: if (isShort) 45L else 320L
        val assignedTags = if (tags.isNotEmpty()) tags else listOf("#shotblast", if (isShort) "#shorts" else "#video", "#$category".lowercase())

        val profile = AuthManager.userProfile.value
        val resolvedChannelName = channelName ?: profile?.channelName ?: "Shotblast Creator"
        val resolvedHandle = channelHandle ?: profile?.channelHandle ?: "@creator"
        val resolvedUserId = userId ?: AuthManager.currentUser.value?.uid ?: ""
        val resolvedSubs = channelSubscribers ?: if (profile != null) "${profile.subscribersCount} subscribers" else "1.2M subscribers"

        val newVideo = VideoItem(
            id = "custom_${System.currentTimeMillis()}",
            title = title.ifBlank { if (isShort) "My Awesome Short" else "New Uploaded Video" },
            videoUrl = url.ifBlank { "https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4" },
            thumbnailDrawable = assignedThumbnail,
            thumbnailUri = thumbnailUri,
            durationStr = durStr,
            durationSeconds = durSec,
            viewsStr = "Just published",
            viewsCount = 1,
            timeAgo = "Just now",
            channelName = resolvedChannelName,
            channelSubscribers = resolvedSubs,
            isVerified = true,
            category = category.ifBlank { "General" },
            tags = assignedTags,
            description = description.ifBlank { "Uploaded directly from Android gallery to Shotblast." },
            likeCount = 1,
            isLiked = true,
            isDisliked = false,
            isSaved = false,
            isDownloaded = false,
            isSubscribed = true,
            isShort = isShort,
            isFromGallery = isFromGallery,
            visibility = visibility,
            soundTrack = soundTrack,
            userId = resolvedUserId,
            channelHandle = resolvedHandle
        )
        _videos.update { listOf(newVideo) + it }
        repositoryScope.launch {
            try {
                FirebaseVideoService.syncVideoToFirestore(newVideo)
            } catch (e: Exception) {
                // Fallback to local
            }
        }
        return newVideo
    }

    fun updateVideoThumbnail(videoId: String, thumbnailUri: String) {
        _videos.update { list ->
            list.map { video ->
                if (video.id == videoId) {
                    video.copy(thumbnailUri = thumbnailUri)
                } else {
                    video
                }
            }
        }
    }
}
