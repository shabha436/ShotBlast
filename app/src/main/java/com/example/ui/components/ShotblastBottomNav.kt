package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ShotblastBorder
import com.example.ui.theme.ShotblastCoral
import com.example.ui.theme.ShotblastCyan
import com.example.ui.theme.ShotblastSurface

enum class BottomNavTab {
    HOME, SHORTS, SUBSCRIPTIONS, LIBRARY
}

@Composable
fun ShotblastBottomNav(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ShotblastSurface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            HorizontalDivider(color = ShotblastBorder, thickness = 0.8.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                BottomNavItem(
                    label = "Home",
                    icon = if (selectedTab == BottomNavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    isSelected = selectedTab == BottomNavTab.HOME,
                    onClick = { onTabSelected(BottomNavTab.HOME) },
                    modifier = Modifier.weight(1f).testTag("nav_home")
                )

                // Shorts
                BottomNavItem(
                    label = "Shorts",
                    icon = if (selectedTab == BottomNavTab.SHORTS) Icons.Filled.Bolt else Icons.Outlined.Bolt,
                    isSelected = selectedTab == BottomNavTab.SHORTS,
                    onClick = { onTabSelected(BottomNavTab.SHORTS) },
                    modifier = Modifier.weight(1f).testTag("nav_shorts")
                )

                // Center "+" Create Button (Solid Cyan Circle with Black Plus)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(onClick = onCreateClick),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ShotblastCyan)
                            .testTag("nav_create_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Create or Import Video",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Subscriptions with badge
                BottomNavItem(
                    label = "Subscriptions",
                    icon = if (selectedTab == BottomNavTab.SUBSCRIPTIONS) Icons.Filled.Subscriptions else Icons.Outlined.Subscriptions,
                    isSelected = selectedTab == BottomNavTab.SUBSCRIPTIONS,
                    hasBadge = true,
                    onClick = { onTabSelected(BottomNavTab.SUBSCRIPTIONS) },
                    modifier = Modifier.weight(1f).testTag("nav_subscriptions")
                )

                // Library
                BottomNavItem(
                    label = "Library",
                    icon = if (selectedTab == BottomNavTab.LIBRARY) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary,
                    isSelected = selectedTab == BottomNavTab.LIBRARY,
                    onClick = { onTabSelected(BottomNavTab.LIBRARY) },
                    modifier = Modifier.weight(1f).testTag("nav_library")
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    hasBadge: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) ShotblastCyan else Color(0xFFB0B3C0),
                modifier = Modifier.size(24.dp)
            )
            if (hasBadge) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(ShotblastCoral, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) ShotblastCyan else Color(0xFF8E92A4),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
