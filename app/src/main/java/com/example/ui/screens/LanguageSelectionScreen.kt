package com.example.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
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
import com.example.ui.theme.NovaElectricPurple
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.viewmodel.NovaViewModel

data class LanguageItem(
    val code: String,
    val nativeName: String,
    val flagEmoji: String
)

@Composable
fun LanguageSelectionScreen(
    viewModel: NovaViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by viewModel.preferredLanguage.collectAsState()
    var selectedLang by remember(currentLang) { mutableStateOf(currentLang) }

    val languages = listOf(
        LanguageItem("bn", "বাংলা (Bengali)", "🇧🇩"),
        LanguageItem("en", "English", "🇺🇸"),
        LanguageItem("hi", "हिन्दी (Hindi)", "🇮🇳"),
        LanguageItem("ar", "العربية (Arabic)", "🇸🇦"),
        LanguageItem("es", "Español (Spanish)", "🇪🇸")
    )

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
                    title = "Select Language",
                    showBack = true,
                    onNavigationClick = onNavigateBack
                )

                Spacer(modifier = Modifier.height(16.dp))

                languages.forEach { lang ->
                    val isSelected = selectedLang == lang.code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(NovaDarkSurface)
                            .border(
                                1.dp,
                                if (isSelected) NovaCyanBright.copy(alpha = 0.6f) else NovaDarkBorder,
                                RoundedCornerShape(18.dp)
                            )
                            .clickable { selectedLang = lang.code }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("lang_option_${lang.code}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lang.flagEmoji,
                            fontSize = 22.sp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = lang.nativeName,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = NovaTextPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedLang = lang.code },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = NovaCyanBright,
                                unselectedColor = NovaDarkBorder
                            )
                        )
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.savePreferredLanguage(selectedLang)
                    Toast.makeText(context, "Language updated", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_language_button"),
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
}
