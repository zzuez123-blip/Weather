package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DockConfig(
    val opacity: Float = 0.90f,
    val roundnessDp: Float = 32f, // matching h-16 rounded-[32px]
    val blurIntensityDp: Float = 16f,
    val isBlurEnabled: Boolean = true
)

enum class DockTab(val index: Int, val title: String) {
    HOME(0, "Home"),
    FORECAST(1, "Forecast"),
    LOCATIONS(2, "Locations"),
    SETTINGS(3, "Settings")
}

@Composable
fun FloatingDockBar(
    selectedTab: DockTab,
    onTabSelected: (DockTab) -> Unit,
    config: DockConfig,
    modifier: Modifier = Modifier
) {
    val animatedRoundness by animateDpAsState(
        targetValue = config.roundnessDp.dp,
        animationSpec = spring(),
        label = "roundnessAnim"
    )

    val animatedBlur by animateDpAsState(
        targetValue = if (config.isBlurEnabled) config.blurIntensityDp.dp else 0.dp,
        animationSpec = spring(),
        label = "blurAnim"
    )

    val animatedOpacity by animateFloatAsState(
        targetValue = config.opacity,
        animationSpec = spring(),
        label = "opacityAnim"
    )

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .widthIn(max = 380.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(animatedRoundness),
                clip = false
            ),
        contentAlignment = Alignment.Center
    ) {
        // Split backdrop/blur from foreground contents to keep text and icons 100% crispy
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(animatedBlur)
                .background(
                    color = Color(0xFF1D1B20).copy(alpha = animatedOpacity),
                    shape = RoundedCornerShape(animatedRoundness)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(animatedRoundness)
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                val tabIcon = getTabIcon(tab, isSelected)
                
                // Animated high-contrast styling tokens matching HTML description
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        Color(0xFF381E72)
                    } else {
                        Color(0xFFEADDFF).copy(alpha = 0.82f)
                    },
                    label = "iconColor"
                )

                val pillBgColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        Color(0xFFD0BCFF)
                    } else {
                        Color.Transparent
                    },
                    label = "pillBgColor"
                )

                Box(
                    modifier = Modifier
                        .testTag("dock_tab_${tab.title.lowercase()}")
                        .clip(RoundedCornerShape(animatedRoundness))
                        .background(pillBgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = Color(0xFFD0BCFF)),
                            onClick = { onTabSelected(tab) }
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tabIcon,
                            contentDescription = tab.title,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.title,
                            color = iconColor,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                            lineHeight = 11.sp
                        )
                    }
                }
            }
        }
    }
}

private fun getTabIcon(tab: DockTab, isSelected: Boolean): ImageVector {
    return when (tab) {
        DockTab.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
        DockTab.FORECAST -> if (isSelected) Icons.Filled.CloudQueue else Icons.Outlined.CloudQueue
        DockTab.LOCATIONS -> if (isSelected) Icons.Filled.LocationOn else Icons.Outlined.LocationOn
        DockTab.SETTINGS -> if (isSelected) Icons.Filled.Settings else Icons.Outlined.Settings
    }
}
