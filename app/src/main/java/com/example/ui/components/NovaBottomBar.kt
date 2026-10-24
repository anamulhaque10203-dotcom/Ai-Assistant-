package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaDarkBorder
import com.example.ui.theme.NovaDarkSurface
import com.example.ui.theme.NovaElectricPurple
import com.example.ui.theme.NovaTextMuted

enum class NovaNavTab {
    HOME,
    HISTORY,
    SETTINGS
}

@Composable
fun NovaBottomBar(
    currentTab: NovaNavTab,
    onTabSelected: (NovaNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(NovaDarkSurface.copy(alpha = 0.95f))
            .border(1.dp, NovaDarkBorder, RoundedCornerShape(26.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTabItem(
                label = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                isSelected = currentTab == NovaNavTab.HOME,
                testTag = "nav_home",
                onClick = { onTabSelected(NovaNavTab.HOME) }
            )

            NavTabItem(
                label = "History",
                selectedIcon = Icons.Filled.History,
                unselectedIcon = Icons.Outlined.History,
                isSelected = currentTab == NovaNavTab.HISTORY,
                testTag = "nav_history",
                onClick = { onTabSelected(NovaNavTab.HISTORY) }
            )

            NavTabItem(
                label = "Settings",
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                isSelected = currentTab == NovaNavTab.SETTINGS,
                testTag = "nav_settings",
                onClick = { onTabSelected(NovaNavTab.SETTINGS) }
            )
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (isSelected) NovaElectricPurple.copy(alpha = 0.22f) else Color.Transparent)
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = if (isSelected) NovaCyanBright else NovaTextMuted,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) NovaCyanBright else NovaTextMuted
        )
    }
}
