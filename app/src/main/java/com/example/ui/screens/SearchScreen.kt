package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.VideoRepository
import com.example.model.ChannelItem
import com.example.model.VideoItem
import com.example.ui.theme.ShotblastBackground
import com.example.ui.theme.ShotblastBorder
import com.example.ui.theme.ShotblastCoral
import com.example.ui.theme.ShotblastCyan
import com.example.ui.theme.ShotblastSurface
import com.example.ui.theme.ShotblastSurfaceVariant
import com.example.ui.theme.ShotblastTextPrimary
import com.example.ui.theme.ShotblastTextSecondary
import com.example.ui.theme.ShotblastTextTertiary
import java.util.Locale

enum class SearchFilterType(val label: String) {
    ALL("All"),
    VIDEOS("Videos"),
    SHORTS("Shorts"),
    CHANNELS("Channels")
}

enum class SearchSortType(val label: String) {
    RELEVANCE("Most Relevant"),
    LATEST("Latest Uploads"),
    MOST_VIEWED("Most Viewed"),
    MOST_LIKED("Most Liked")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    videos: List<VideoItem>,
    channels: List<ChannelItem>,
    onBackClick: () -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onShortsClick: (VideoItem) -> Unit,
    onChannelClick: (String) -> Unit,
    onVideoOptionsClick: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(SearchFilterType.ALL) }
    var activeSort by remember { mutableStateOf(SearchSortType.RELEVANCE) }
    var isSortMenuOpen by remember { mutableStateOf(false) }
    var hasExecutedSearch by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }

    val searchHistory by VideoRepository.searchHistory.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Trending suggested topics
    val trendingTopics = listOf(
        "4K Cinematic",
        "Cute Dog #shorts",
        "Gaming Zone",
        "Nature Vibes",
        "Anime Opening",
        "Cyber Arena",
        "Waterfall in 4K",
        "Relaxing Lo-Fi"
    )

    // Voice recognition launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                searchQuery = spokenText
                hasExecutedSearch = true
                VideoRepository.addSearchHistory(spokenText)
                focusManager.clearFocus()
            }
        }
    }

    // Auto-focus search input on initial screen entry
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    // Hardware back navigation handler
    BackHandler {
        if (hasExecutedSearch && searchQuery.isNotEmpty()) {
            searchQuery = ""
            hasExecutedSearch = false
        } else {
            onBackClick()
        }
    }

    fun executeSearch(term: String) {
        val trimmed = term.trim()
        if (trimmed.isNotBlank()) {
            searchQuery = trimmed
            hasExecutedSearch = true
            VideoRepository.addSearchHistory(trimmed)
            focusManager.clearFocus()
        }
    }

    // Dynamic suggestions based on current typed text
    val liveSuggestions = remember(searchQuery, videos, channels) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val q = searchQuery.trim().lowercase(Locale.getDefault())
            val fromTitles = videos.map { it.title }.filter { it.lowercase(Locale.getDefault()).contains(q) }
            val fromChannels = channels.map { it.name }.filter { it.lowercase(Locale.getDefault()).contains(q) }
            val fromTags = videos.flatMap { it.tags }.filter { it.lowercase(Locale.getDefault()).contains(q) }
            (fromTitles + fromChannels + fromTags).distinct().take(6)
        }
    }

    // Filter and sort videos
    val matchingVideos = remember(videos, searchQuery, activeFilter, activeSort, hasExecutedSearch) {
        val q = searchQuery.trim()
        if (q.isEmpty() && !hasExecutedSearch) {
            emptyList()
        } else {
            var list = videos.filter { v ->
                val matchesQuery = q.isEmpty() ||
                        v.title.contains(q, ignoreCase = true) ||
                        v.channelName.contains(q, ignoreCase = true) ||
                        v.category.contains(q, ignoreCase = true) ||
                        v.tags.any { it.contains(q, ignoreCase = true) }

                val matchesType = when (activeFilter) {
                    SearchFilterType.ALL -> true
                    SearchFilterType.VIDEOS -> !v.isShort
                    SearchFilterType.SHORTS -> v.isShort
                    SearchFilterType.CHANNELS -> false
                }
                matchesQuery && matchesType
            }

            // Apply sorting
            list = when (activeSort) {
                SearchSortType.RELEVANCE -> list
                SearchSortType.LATEST -> list.sortedByDescending { it.id }
                SearchSortType.MOST_VIEWED -> list.sortedByDescending { it.viewsCount }
                SearchSortType.MOST_LIKED -> list.sortedByDescending { it.likeCount }
            }
            list
        }
    }

    // Matching channels
    val matchingChannels = remember(channels, searchQuery, activeFilter) {
        val q = searchQuery.trim()
        if (q.isEmpty() || (activeFilter != SearchFilterType.ALL && activeFilter != SearchFilterType.CHANNELS)) {
            emptyList()
        } else {
            channels.filter { ch ->
                ch.name.contains(q, ignoreCase = true) || ch.handle.contains(q, ignoreCase = true)
            }
        }
    }

    val regularMatchingVideos = remember(matchingVideos) { matchingVideos.filter { !it.isShort } }
    val shortMatchingVideos = remember(matchingVideos) { matchingVideos.filter { it.isShort } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ShotblastBackground)
            .statusBarsPadding()
            .testTag("search_screen_container")
    ) {
        // TOP SEARCH BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("search_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    hasExecutedSearch = false
                },
                placeholder = {
                    Text("Search Shotblast...", color = ShotblastTextTertiary, fontSize = 14.sp)
                },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ShotblastCyan,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                    hasExecutedSearch = false
                                },
                                modifier = Modifier.testTag("clear_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = ShotblastTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(
                                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                        )
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search on Shotblast")
                                    }
                                    speechLauncher.launch(intent)
                                } catch (_: Exception) {
                                    showVoiceDialog = true
                                }
                            },
                            modifier = Modifier.testTag("voice_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Search",
                                tint = ShotblastCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        executeSearch(searchQuery)
                    }
                ),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShotblastCyan,
                    unfocusedBorderColor = ShotblastBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = ShotblastCyan,
                    focusedContainerColor = ShotblastSurface,
                    unfocusedContainerColor = ShotblastSurface
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .focusRequester(focusRequester)
                    .testTag("search_text_field")
            )
        }

        HorizontalDivider(color = ShotblastBorder, thickness = 0.5.dp)

        // CONTENT SECTION: Suggestions vs History vs Search Results
        when {
            // 1. User has submitted a search query or results are active
            hasExecutedSearch || (searchQuery.isNotBlank() && liveSuggestions.isEmpty()) -> {
                // Filter & Sort Pills Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filter options
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SearchFilterType.entries.forEach { filter ->
                            val isSelected = activeFilter == filter
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) ShotblastCyan else ShotblastSurfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { activeFilter = filter }
                                    .testTag("filter_chip_${filter.name.lowercase()}")
                            ) {
                                Text(
                                    text = filter.label,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Sort button with dropdown
                    Box {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ShotblastSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { isSortMenuOpen = true }
                                .testTag("sort_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Sort",
                                    tint = ShotblastCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = activeSort.label,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isSortMenuOpen,
                            onDismissRequest = { isSortMenuOpen = false },
                            modifier = Modifier.background(ShotblastSurface)
                        ) {
                            SearchSortType.entries.forEach { sortOption ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = sortOption.label,
                                                color = if (activeSort == sortOption) ShotblastCyan else Color.White,
                                                fontWeight = if (activeSort == sortOption) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (activeSort == sortOption) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = ShotblastCyan,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        activeSort = sortOption
                                        isSortMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = ShotblastBorder, thickness = 0.5.dp)

                // Results List
                if (matchingVideos.isEmpty() && matchingChannels.isEmpty()) {
                    // Empty Results State
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .testTag("empty_search_results"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(ShotblastSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = ShotblastCyan,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No results found for \"$searchQuery\"",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Try different keywords or check out trending searches below",
                                color = ShotblastTextSecondary,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            // Quick trending suggestion chips
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                trendingTopics.take(4).forEach { topic ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = ShotblastSurfaceVariant,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { executeSearch(topic) }
                                    ) {
                                        Text(
                                            text = topic,
                                            color = ShotblastCyan,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        // 1. Matching Channels section
                        if (matchingChannels.isNotEmpty()) {
                            item {
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                    Text(
                                        text = "Channels",
                                        color = ShotblastCyan,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                            items(matchingChannels, key = { "ch_${it.id}" }) { channel ->
                                SearchChannelRow(
                                    channel = channel,
                                    onChannelClick = onChannelClick
                                )
                            }
                            item {
                                HorizontalDivider(
                                    color = ShotblastBorder,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }

                        // 2. Shorts shelf if matching shorts exist and filter is ALL or SHORTS
                        if (shortMatchingVideos.isNotEmpty() && (activeFilter == SearchFilterType.ALL || activeFilter == SearchFilterType.SHORTS)) {
                            item {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Whatshot,
                                            contentDescription = null,
                                            tint = ShotblastCoral,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Shorts",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(shortMatchingVideos, key = { "short_${it.id}" }) { shortVideo ->
                                            SearchShortCard(
                                                short = shortVideo,
                                                onShortClick = onShortsClick
                                            )
                                        }
                                    }
                                }
                            }

                            if (regularMatchingVideos.isNotEmpty()) {
                                item {
                                    HorizontalDivider(
                                        color = ShotblastBorder,
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        // 3. Regular Video Cards
                        if (activeFilter != SearchFilterType.SHORTS) {
                            items(regularMatchingVideos, key = { "vid_${it.id}" }) { video ->
                                SearchVideoCard(
                                    video = video,
                                    onVideoClick = onVideoClick,
                                    onChannelClick = onChannelClick,
                                    onOptionsClick = onVideoOptionsClick
                                )
                            }
                        }
                    }
                }
            }

            // 2. User is currently typing: show live instant suggestions
            searchQuery.isNotBlank() && liveSuggestions.isNotEmpty() -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Search for query directly
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { executeSearch(searchQuery) }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .testTag("suggestion_direct_search"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = ShotblastCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Search for \"$searchQuery\"",
                                color = ShotblastCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        HorizontalDivider(color = ShotblastBorder, thickness = 0.5.dp)
                    }

                    // Autocomplete suggestions
                    items(liveSuggestions) { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { executeSearch(suggestion) }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .testTag("suggestion_item_$suggestion"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = ShotblastTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = suggestion,
                                color = ShotblastTextPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    searchQuery = suggestion
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NorthWest,
                                    contentDescription = "Insert text",
                                    tint = ShotblastTextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Search query is empty: Show Recent Searches & Trending Topics
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Recent Searches
                    if (searchHistory.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Searches",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(
                                    onClick = { VideoRepository.clearSearchHistory() },
                                    modifier = Modifier.testTag("clear_all_history_button")
                                ) {
                                    Text(
                                        text = "Clear All",
                                        color = ShotblastCyan,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        items(searchHistory) { historyItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { executeSearch(historyItem) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .testTag("history_item_$historyItem"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = ShotblastTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = historyItem,
                                    color = ShotblastTextPrimary,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { VideoRepository.removeSearchHistory(historyItem) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = ShotblastTextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        item {
                            HorizontalDivider(
                                color = ShotblastBorder,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    // Trending Topics Section
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = null,
                                    tint = ShotblastCoral,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Trending Searches",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            // Grid of trending search pills
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                trendingTopics.chunked(2).forEach { rowTopics ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowTopics.forEach { topic ->
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = ShotblastSurfaceVariant,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { executeSearch(topic) }
                                                    .testTag("trending_pill_$topic")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.TrendingUp,
                                                        contentDescription = null,
                                                        tint = ShotblastCyan,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = topic,
                                                        color = Color.White,
                                                        fontSize = 13.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                        if (rowTopics.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Browse by Categories
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text(
                                text = "Explore Categories",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val exploreCategories = listOf("Gaming", "Nature", "Travel", "Anime", "Shorts", "Music")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                exploreCategories.forEach { cat ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = ShotblastSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ShotblastBorder),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { executeSearch(cat) }
                                    ) {
                                        Text(
                                            text = cat,
                                            color = ShotblastCyan,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Voice Search Simulation Dialog (used when system speech recognizer is unavailable)
    if (showVoiceDialog) {
        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = ShotblastCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Voice Search", color = Color.White)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Speak into microphone or select a quick voice query prompt:",
                        color = ShotblastTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf(
                        "Nature Vibes 4K",
                        "Cute Dog #shorts",
                        "Best Game Moments",
                        "Cyber Arena Finals"
                    ).forEach { voicePrompt ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ShotblastSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    showVoiceDialog = false
                                    executeSearch(voicePrompt)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = ShotblastCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "\"$voicePrompt\"",
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVoiceDialog = false }) {
                    Text("Close", color = ShotblastCyan)
                }
            },
            containerColor = ShotblastSurface
        )
    }
}

/**
 * Channel Row in search results
 */
@Composable
private fun SearchChannelRow(
    channel: ChannelItem,
    onChannelClick: (String) -> Unit
) {
    var isSubscribed by remember(channel.isSubscribed) { mutableStateOf(channel.isSubscribed) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChannelClick(channel.name) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("search_channel_row_${channel.name}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channel Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(ShotblastCyan, ShotblastSurfaceVariant)
                    )
                )
                .border(1.5.dp, ShotblastCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = channel.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified",
                    tint = ShotblastCyan,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${channel.handle} • ${channel.subscribers} • ${channel.videoCount} videos",
                color = ShotblastTextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Subscribe / Subscribed Button
        if (isSubscribed) {
            OutlinedButton(
                onClick = {
                    isSubscribed = VideoRepository.toggleChannelSubscription(channel.name)
                },
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ShotblastBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ShotblastTextSecondary,
                    containerColor = ShotblastSurfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("subscribe_btn_${channel.name}")
            ) {
                Text("Subscribed", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Button(
                onClick = {
                    isSubscribed = VideoRepository.toggleChannelSubscription(channel.name)
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("subscribe_btn_${channel.name}")
            ) {
                Text("Subscribe", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Shorts Card in search results horizontal shelf
 */
@Composable
private fun SearchShortCard(
    short: VideoItem,
    onShortClick: (VideoItem) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ShotblastSurfaceVariant,
        modifier = Modifier
            .width(130.dp)
            .height(210.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onShortClick(short) }
            .testTag("search_short_card_${short.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (short.thumbnailDrawable != null) {
                AsyncImage(
                    model = short.thumbnailDrawable,
                    contentDescription = short.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ShotblastSurface)
                )
            }

            // Gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 180f
                        )
                    )
            )

            // Play badge at top
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = ShotblastCyan,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Title and views at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = short.title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = short.viewsStr,
                    color = ShotblastCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Regular Video Card in Search results
 */
@Composable
private fun SearchVideoCard(
    video: VideoItem,
    onVideoClick: (VideoItem) -> Unit,
    onChannelClick: (String) -> Unit,
    onOptionsClick: (VideoItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVideoClick(video) }
            .padding(vertical = 6.dp)
            .testTag("search_video_card_${video.id}")
    ) {
        // Thumbnail with duration badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(ShotblastSurface)
        ) {
            if (video.thumbnailDrawable != null) {
                AsyncImage(
                    model = video.thumbnailDrawable,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Duration Pill
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.8f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                Text(
                    text = video.durationStr,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Details Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Channel Avatar
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ShotblastSurfaceVariant)
                    .border(1.dp, ShotblastCyan, CircleShape)
                    .clickable { onChannelClick(video.channelName) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = ShotblastCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title and Metadata
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = video.channelName,
                        color = ShotblastTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onChannelClick(video.channelName) }
                    )
                    if (video.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = ShotblastCyan,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•  ${video.viewsStr}  •  ${video.timeAgo}",
                        color = ShotblastTextTertiary,
                        fontSize = 11.sp
                    )
                }
            }

            // 3-dots more options button
            IconButton(
                onClick = { onOptionsClick(video) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = ShotblastTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
