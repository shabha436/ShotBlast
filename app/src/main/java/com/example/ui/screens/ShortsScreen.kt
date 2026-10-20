package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.model.VideoItem

@Composable
fun ShortsScreen(
    shortsList: List<VideoItem>,
    initialShortId: String? = null,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ShortsPlayerScreen(
        shortsList = shortsList,
        initialShortId = initialShortId,
        onBackClick = onBackClick,
        modifier = modifier
    )
}
