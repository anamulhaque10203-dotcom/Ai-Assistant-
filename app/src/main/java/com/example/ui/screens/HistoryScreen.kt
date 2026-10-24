package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConversationEntity
import com.example.ui.components.NovaBottomBar
import com.example.ui.components.NovaNavTab
import com.example.ui.components.NovaTopBar
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaDarkBg
import com.example.ui.theme.NovaDarkBorder
import com.example.ui.theme.NovaDarkSurface
import com.example.ui.theme.NovaDarkSurfaceVariant
import com.example.ui.theme.NovaElectricPurple
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.viewmodel.NovaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: NovaViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSelectConversation: (ConversationEntity) -> Unit
) {
    val conversations by viewModel.allConversations.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = NovaDarkBg,
        topBar = {
            NovaTopBar(
                title = "Conversation History",
                showBack = false,
                actions = {
                    if (conversations.isNotEmpty()) {
                        TextButton(
                            onClick = { showClearConfirm = true },
                            modifier = Modifier.testTag("history_clear_all_button")
                        ) {
                            Text(
                                text = "Clear All",
                                fontSize = 14.sp,
                                color = NovaError,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NovaBottomBar(
                currentTab = NovaNavTab.HISTORY,
                onTabSelected = { tab ->
                    when (tab) {
                        NovaNavTab.HOME -> onNavigateToHome()
                        NovaNavTab.HISTORY -> {}
                        NovaNavTab.SETTINGS -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            if (conversations.isEmpty()) {
                // Empty state illustration
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(NovaDarkSurface)
                            .border(1.dp, NovaDarkBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "No History",
                            tint = NovaTextMuted,
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "No Conversations Yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your voice queries and responses will be saved here automatically.",
                        fontSize = 13.sp,
                        color = NovaTextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(conversations, key = { it.id }) { item ->
                        ConversationHistoryItem(
                            conversation = item,
                            onClick = { onSelectConversation(item) },
                            onDelete = { viewModel.deleteConversation(item.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = {
                Text("Clear History", color = NovaTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to delete all saved conversations? This cannot be undone.",
                    color = NovaTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllConversations()
                        showClearConfirm = false
                    }
                ) {
                    Text("Clear All", color = NovaError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = NovaTextSecondary)
                }
            },
            containerColor = NovaDarkSurfaceVariant,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun ConversationHistoryItem(
    conversation: ConversationEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(conversation.timestamp) { formatter.format(Date(conversation.timestamp)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NovaDarkSurface)
            .border(1.dp, NovaDarkBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(NovaElectricPurple.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = null,
                tint = NovaCyanBright,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.question,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = NovaTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formattedTime,
                fontSize = 12.sp,
                color = NovaTextMuted
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete conversation",
                tint = NovaTextMuted.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
