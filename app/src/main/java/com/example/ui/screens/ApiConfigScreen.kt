package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NovaTopBar
import com.example.ui.theme.NovaCyanBright
import com.example.ui.theme.NovaDarkBg
import com.example.ui.theme.NovaDarkBorder
import com.example.ui.theme.NovaDarkSurface
import com.example.ui.theme.NovaDarkSurfaceVariant
import com.example.ui.theme.NovaElectricPurple
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.viewmodel.NovaViewModel

@Composable
fun ApiConfigScreen(
    viewModel: NovaViewModel,
    onNavigateBack: () -> Unit,
    onEditKey: () -> Unit
) {
    val context = LocalContext.current
    val savedKey by viewModel.savedApiKey.collectAsState()
    val effectiveKey = viewModel.getEffectiveApiKey()
    val isConnected by viewModel.isApiConnected.collectAsState()
    val testLoading by viewModel.testConnectionLoading.collectAsState()
    val testMsg by viewModel.testConnectionMessage.collectAsState()

    var showDeleteConfirm by remember { mutableStateOf(false) }

    val maskedKey = if (effectiveKey.isNotBlank()) {
        "•".repeat(20)
    } else {
        "No API key configured"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                NovaTopBar(
                    title = "API Configuration",
                    showBack = true,
                    onNavigationClick = onNavigateBack
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Current API Key Card
                Text(
                    text = "Current API Key",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(NovaDarkSurface)
                        .border(1.dp, NovaDarkBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = NovaCyanBright,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = maskedKey,
                                fontSize = 16.sp,
                                letterSpacing = 2.sp,
                                color = if (effectiveKey.isNotBlank()) NovaTextPrimary else NovaTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onEditKey,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("api_edit_key_button"),
                                shape = RoundedCornerShape(22.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NovaElectricPurple,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("api_delete_key_button"),
                                shape = RoundedCornerShape(22.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = NovaError
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(NovaError.copy(alpha = 0.5f))
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Delete", fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Test Connection Status Card
                Text(
                    text = "Test Connection",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(NovaDarkSurface)
                        .border(1.dp, NovaDarkBorder, RoundedCornerShape(18.dp))
                        .clickable {
                            if (effectiveKey.isNotBlank()) {
                                viewModel.testApiConnection(effectiveKey)
                            } else {
                                Toast.makeText(context, "Please configure an API key first.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                        .testTag("api_test_connection_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (testLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = NovaCyanBright,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Testing connection...", fontSize = 14.sp, color = NovaTextSecondary)
                            } else if (isConnected == true || effectiveKey.isNotBlank()) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Connected",
                                    tint = NovaSuccess,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Connected", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NovaSuccess)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Not connected",
                                    tint = NovaError,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Not Connected", fontSize = 14.sp, color = NovaError)
                            }
                        }

                        Text(
                            text = "Tap to verify",
                            fontSize = 12.sp,
                            color = NovaTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Usage & Quota link
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(NovaDarkSurface)
                        .border(1.dp, NovaDarkBorder, RoundedCornerShape(18.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/"))
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Visit https://aistudio.google.com/", Toast.LENGTH_LONG).show()
                            }
                        }
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                        .testTag("api_usage_quota_link")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Usage & Quota",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NovaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Check in Google AI Studio",
                                fontSize = 12.sp,
                                color = NovaCyanBright
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = NovaTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Save button
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("api_config_save_button"),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaElectricPurple,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Save",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text("Delete API Key", color = NovaTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to remove your saved Gemini API key from the app?",
                    color = NovaTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteApiKey()
                        showDeleteConfirm = false
                        Toast.makeText(context, "API Key removed", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = NovaError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = NovaTextSecondary)
                }
            },
            containerColor = NovaDarkSurfaceVariant,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
