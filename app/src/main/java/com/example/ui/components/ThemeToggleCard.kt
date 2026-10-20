package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.ShotblastBorder
import com.example.ui.theme.ShotblastCyan
import com.example.ui.theme.ShotblastSurface
import com.example.ui.theme.ShotblastSurfaceVariant
import com.example.ui.theme.ShotblastTextPrimary
import com.example.ui.theme.ShotblastTextSecondary
import com.example.ui.theme.ThemeManager

/**
 * Modern Card with Dark / Light Mode segmented toggle buttons.
 * Supports clear Telugu and English instructions.
 */
@Composable
fun ThemeToggleCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark by ThemeManager.isDarkTheme.collectAsStateWithLifecycle()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("theme_toggle_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShotblastSurface),
        border = BorderStroke(1.dp, ShotblastCyan.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ShotblastCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                            contentDescription = "Theme Icon",
                            tint = ShotblastCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "App Appearance",
                            color = ShotblastTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isDark) "Dark Theme Active" else "Light Theme Active",
                            color = ShotblastTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Quick Switch
                Switch(
                    checked = isDark,
                    onCheckedChange = { checked ->
                        ThemeManager.setDarkTheme(checked, context)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = ShotblastCyan,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = ShotblastBorder
                    ),
                    modifier = Modifier.testTag("theme_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Segmented Theme Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ShotblastSurfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Dark Mode Button
                val darkBtnBg by animateColorAsState(
                    targetValue = if (isDark) ShotblastCyan else Color.Transparent,
                    animationSpec = tween(200),
                    label = "darkBtnBg"
                )
                val darkBtnText = if (isDark) Color.Black else ShotblastTextPrimary

                Surface(
                    color = darkBtnBg,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { ThemeManager.setDarkTheme(true, context) }
                        .testTag("select_dark_theme_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DarkMode,
                            contentDescription = null,
                            tint = darkBtnText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dark Mode",
                            color = darkBtnText,
                            fontSize = 12.sp,
                            fontWeight = if (isDark) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }

                // Light Mode Button
                val lightBtnBg by animateColorAsState(
                    targetValue = if (!isDark) ShotblastCyan else Color.Transparent,
                    animationSpec = tween(200),
                    label = "lightBtnBg"
                )
                val lightBtnText = if (!isDark) Color.Black else ShotblastTextPrimary

                Surface(
                    color = lightBtnBg,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { ThemeManager.setDarkTheme(false, context) }
                        .testTag("select_light_theme_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LightMode,
                            contentDescription = null,
                            tint = lightBtnText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Light Mode",
                            color = lightBtnText,
                            fontSize = 12.sp,
                            fontWeight = if (!isDark) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
