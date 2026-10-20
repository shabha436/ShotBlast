package com.example.model

data class VideoItem(
    val id: String,
    val title: String,
    val videoUrl: String,
    val thumbnailDrawable: Int? = null,
    val durationStr: String,
    val durationSeconds: Long,
    val viewsStr: String,
    val viewsCount: Long,
    val timeAgo: String,
    val channelName: String,
    val channelSubscribers: String,
    val isVerified: Boolean = true,
    val category: String,
    val tags: List<String> = emptyList(),
    val description: String,
    val likeCount: Long = 68000,
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,
    val isSaved: Boolean = false,
    val isDownloaded: Boolean = false,
    val isSubscribed: Boolean = false,
    val isShort: Boolean = false,
    val thumbnailUri: String? = null,
    val isFromGallery: Boolean = false,
    val visibility: String = "Public",
    val soundTrack: String? = null,
    val userId: String = "",
    val channelHandle: String = ""
)

data class CommentItem(
    val id: String,
    val authorName: String,
    val authorHandle: String,
    val timeAgo: String,
    val text: String,
    val likesCount: Long,
    val isLiked: Boolean = false
)

data class ChannelItem(
    val id: String,
    val name: String,
    val handle: String,
    val subscribers: String,
    val isSubscribed: Boolean = true,
    val videoCount: Int = 120
)

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val channelName: String = "",
    val channelHandle: String = "",
    val bio: String = "",
    val subscribersCount: Long = 0L,
    val isVerified: Boolean = false,
    val isChannelCreated: Boolean = false
)
