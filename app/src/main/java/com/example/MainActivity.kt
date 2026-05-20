package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Room and Repository Initialization
        val db = AppDatabase.getDatabase(applicationContext)
        val repository = KeyboardRepository(db)
        val viewModelFactory = KeyboardViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[KeyboardViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    MainDashboard(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainDashboard(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val themePreset by viewModel.themePreset.collectAsState()
    val customBgColor by viewModel.customBgColor.collectAsState()
    val customKeyColor by viewModel.customKeyColor.collectAsState()
    val customTextColor by viewModel.customTextColor.collectAsState()

    // Determine current theme profile colors
    val (primaryBg, keyBg, textAccent) = remember(themePreset, themeMode, customBgColor, customKeyColor, customTextColor) {
        val isDark = themeMode == "dark"
        when (themePreset) {
            "aura" -> Triple(
                if (isDark) Color.Black.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.4f),
                if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                if (isDark) Color(0xFFD0BCFF) else Color(0xFF7C3AED) // Radiant Lavender Accent
            )
            "helakuru" -> Triple(
                if (isDark) Color(0xFF15181C) else Color(0xFFF3F4F6),
                if (isDark) Color(0xFF282C34) else Color(0xFFFFFFFF),
                Color(0xFFFF7300) // Helakuru dynamic Orange
            )
            "cosmic" -> Triple(
                if (isDark) Color(0xFF0F172A) else Color(0xFFEFF6FF),
                if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF),
                if (isDark) Color(0xFF38BDF8) else Color(0xFF1D4ED8) // Tech Cyber Blue
            )
            "pastel" -> Triple(
                if (isDark) Color(0xFF2D1B2D) else Color(0xFFFAF5FF),
                if (isDark) Color(0xFF4A344A) else Color(0xFFFFF1F2),
                Color(0xFFEC4899) // Magenta Pastel
            )
            else -> Triple(
                parseColor(customBgColor, if (isDark) Color(0xFF222222) else Color(0xFFEEEEEE)),
                parseColor(customKeyColor, if (isDark) Color(0xFF333333) else Color(0xFFFFFFFF)),
                parseColor(customTextColor, Color(0xFFFF9800))
            )
        }
    }

    // Standard Theme overrides based on chosen mode (light/dark)
    val appBackground = if (themePreset == "aura") {
        if (themeMode == "dark") Color(0xFF0F1115) else Color(0xFFF1F5F9)
    } else {
        if (themeMode == "dark") Color(0xFF111317) else Color(0xFFFAFAFA)
    }

    val cardBackground = if (themePreset == "aura") {
        if (themeMode == "dark") Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
    } else {
        if (themeMode == "dark") Color(0xFF1E222B) else Color(0xFFFFFFFF)
    }

    val foregroundText = if (themePreset == "aura") {
        if (themeMode == "dark") Color(0xFFF1F5F9) else Color(0xFF0F172A)
    } else {
        if (themeMode == "dark") Color(0xFFF3F4F6) else Color(0xFF1F2937)
    }

    val dividerColor = if (themePreset == "aura") {
        if (themeMode == "dark") Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)
    } else {
        if (themeMode == "dark") Color(0xFF2D323E) else Color(0xFFE5E7EB)
    }

    var currentTab by remember { mutableStateOf("keyboard") } // keyboard, news, personalization, ai, clipboard, privacy

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(appBackground)
    ) {
        // --- High-Fidelity App Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (themePreset == "aura") {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Aura ",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (themeMode == "dark") Color.White else Color(0xFF0F172A)
                        )
                        Text(
                            text = "Keyboard",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = textAccent // Lavender `#D0BCFF`
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Pulsing Green Sync dot
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF4ADE80))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CLOUD SYNC: ACTIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = foregroundText.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(textAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ක",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "HelaSmart",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = foregroundText
                        )
                        Text(
                            text = "Sinhala & English AI Keyboard",
                            fontSize = 11.sp,
                            color = textAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Light/Dark Toggle & Status badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (themePreset == "aura") {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(cardBackground)
                            .border(1.dp, dividerColor, RoundedCornerShape(19.dp))
                            .clickable { viewModel.onThemeModeToggled() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (themeMode == "dark") Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Switch Appearance Mode",
                            tint = textAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(cardBackground)
                            .border(1.dp, dividerColor, RoundedCornerShape(19.dp))
                            .clickable { currentTab = "personalization" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Theme Studio Settings",
                            tint = foregroundText.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = { viewModel.onThemeModeToggled() },
                        modifier = Modifier.testTag("theme_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (themeMode == "dark") Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Switch Appearance Mode",
                            tint = textAccent
                        )
                    }
                }
                
                val incognito by viewModel.incognitoMode.collectAsState()
                if (incognito) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDC2626).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("INCOGNITO", fontSize = 9.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Divider(color = dividerColor, thickness = 1.dp)

        // --- Active Workspace Content ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentTab) {
                "keyboard" -> KeyboardPlaygroundScreen(
                    viewModel = viewModel,
                    appBg = appBackground,
                    cardBg = cardBackground,
                    fgText = foregroundText,
                    dividerCol = dividerColor,
                    primaryKeyboardBg = primaryBg,
                    keyBg = keyBg,
                    textAccent = textAccent,
                    themePreset = themePreset,
                    onTabChange = { currentTab = it }
                )
                "news" -> HelakuruNewsScreen(
                    viewModel = viewModel,
                    cardBg = cardBackground,
                    fgText = foregroundText,
                    dividerCol = dividerColor,
                    textAccent = textAccent
                )
                "personalization" -> ThemeSettingsScreen(
                    viewModel = viewModel,
                    cardBg = cardBackground,
                    fgText = foregroundText,
                    dividerCol = dividerColor,
                    textAccent = textAccent,
                    currentThemeBg = primaryBg,
                    currentKeyBg = keyBg
                )
                "ai" -> AiAssistantScreen(
                    viewModel = viewModel,
                    cardBg = cardBackground,
                    fgText = foregroundText,
                    dividerCol = dividerColor,
                    textAccent = textAccent
                )
                "clipboard" -> ClipboardManagerScreen(
                    viewModel = viewModel,
                    cardBg = cardBackground,
                    fgText = foregroundText,
                    dividerCol = dividerColor,
                    textAccent = textAccent
                )
                "privacy" -> PrivacyFeedbackScreen(
                    viewModel = viewModel,
                    cardBg = cardBackground,
                    fgText = foregroundText,
                    dividerCol = dividerColor,
                    textAccent = textAccent
                )
            }
        }

        Divider(color = dividerColor, thickness = 1.dp)

        // --- Bottom Navigation Tab Strip ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBackground)
                .padding(bottom = 8.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTabItem(
                label = "Keyboard",
                icon = Icons.Default.Keyboard,
                isSelected = currentTab == "keyboard",
                tint = textAccent,
                fg = foregroundText,
                onClick = { currentTab = "keyboard" }
            )
            BottomTabItem(
                label = "News Hub",
                icon = Icons.Default.Newspaper,
                isSelected = currentTab == "news",
                tint = textAccent,
                fg = foregroundText,
                onClick = { currentTab = "news" }
            )
            BottomTabItem(
                label = "Themes",
                icon = Icons.Default.Palette,
                isSelected = currentTab == "personalization",
                tint = textAccent,
                fg = foregroundText,
                onClick = { currentTab = "personalization" }
            )
            BottomTabItem(
                label = "Gemini AI",
                icon = Icons.Default.AutoAwesome,
                isSelected = currentTab == "ai",
                tint = textAccent,
                fg = foregroundText,
                onClick = { currentTab = "ai" }
            )
            BottomTabItem(
                label = "Clips",
                icon = Icons.Default.ContentPaste,
                isSelected = currentTab == "clipboard",
                tint = textAccent,
                fg = foregroundText,
                onClick = { currentTab = "clipboard" }
            )
            BottomTabItem(
                label = "Privacy",
                icon = Icons.Default.Security,
                isSelected = currentTab == "privacy",
                tint = textAccent,
                fg = foregroundText,
                onClick = { currentTab = "privacy" }
            )
        }
    }
}

@Composable
fun BottomTabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    tint: Color,
    fg: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .testTag("tab_${label.lowercase().replace(" ", "_")}")
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) tint else fg.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = if (isSelected) tint else fg.copy(alpha = 0.7f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

// --- TAB SCREEN 1: KEYBOARD PLAYGROUND & VIRTUAL KEYBOARD ---
@Composable
fun KeyboardPlaygroundScreen(
    viewModel: KeyboardViewModel,
    appBg: Color,
    cardBg: Color,
    fgText: Color,
    dividerCol: Color,
    primaryKeyboardBg: Color,
    keyBg: Color,
    textAccent: Color,
    themePreset: String,
    onTabChange: (String) -> Unit
) {
    val context = LocalContext.current
    val typedBuffer by viewModel.typedText.collectAsState()
    val activeBuffer by viewModel.activeWordBuffer.collectAsState()
    val predictions by viewModel.predictions.collectAsState()
    val currentLayout by viewModel.currentLayout.collectAsState()
    val activeFont by viewModel.keyboardFont.collectAsState()
    val fontSizeMultiplier by viewModel.keyboardFontSize.collectAsState()
    val keyCornersDp by viewModel.keyRoundness.collectAsState()
    val selectedEmojiCat by viewModel.selectedEmojiCategory.collectAsState()

    val fontStyle = when (activeFont) {
        "JetBrains Mono" -> FontFamily.Monospace
        "Playfair Serif" -> FontFamily.Serif
        else -> FontFamily.Default
    }

    var showEmojiPanel by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- Sub header info ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Typing Playground",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = fgText
            )
            
            // Switch layout modes
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(dividerCol.copy(alpha = 0.5f))
                    .padding(2.dp)
            ) {
                listOf(
                    "PHONETIC_SINGLISH" to "සිංග්ලිෂ්",
                    "SINHALA_DIRECT" to "සිංහල",
                    "ENGLISH_STANDARD" to "English"
                ).forEach { (layoutCode, name) ->
                    val sel = currentLayout == layoutCode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (sel) textAccent else Color.Transparent)
                            .clickable { 
                                viewModel.onKeyboardLayoutChanged(layoutCode)
                                showEmojiPanel = false
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (sel) Color.White else fgText.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        if (themePreset == "aura") {
            Spacer(modifier = Modifier.height(10.dp))
            
            // --- Helakuru-style News Feed (Horizontal Banner) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(
                                textAccent.copy(alpha = 0.15f),
                                Color(0xFF581C87).copy(alpha = 0.15f)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                // Live Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFEF4444))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "LIVE",
                        fontSize = 8.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📰", fontSize = 16.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Flash",
                            fontSize = 10.sp,
                            color = textAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "New AI features added to Singlish layout.",
                            fontSize = 12.sp,
                            color = fgText.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- Quick Access Tools Grid ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    Triple("📋", "Clipboard", "clipboard"),
                    Triple("🔤", "Fonts", "personalization"),
                    Triple("🛡️", "Privacy", "privacy"),
                    Triple("✨", "Aura AI", "ai")
                ).forEach { (emoji, label, tabCode) ->
                    val isAuraAi = label == "Aura AI"
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onTabChange(tabCode) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.2f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isAuraAi) textAccent else cardBg)
                                .border(
                                    1.dp,
                                    if (isAuraAi) Color.Transparent else Color.White.copy(alpha = 0.08f),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 20.sp,
                                color = if (isAuraAi) Color.DarkGray else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isAuraAi) FontWeight.Bold else FontWeight.Medium,
                            color = if (isAuraAi) textAccent else fgText.copy(alpha = 0.72f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- Styled Simulated Output Box (Edge to edge with blinking cursor) ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .border(1.dp, dividerCol, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (typedBuffer.isEmpty() && activeBuffer.isEmpty()) {
                        Text(
                            text = "Start typing on the smart keyboard below to test of the Singlish conversion...",
                            color = fgText.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    } else {
                        // Display composed sentence + active transient word bubble
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = typedBuffer,
                                    fontSize = 18.sp,
                                    color = fgText,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (activeBuffer.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(textAccent.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = activeBuffer,
                                            fontSize = 12.sp,
                                            color = textAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Divider(color = dividerCol.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 10.dp))

                // Interactive Buttons on the Playground Output
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        Button(
                            onClick = {
                                viewModel.copyCurrentToClipboard()
                                Toast.makeText(context, "Copied to Clipboard & Database!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = textAccent),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp).testTag("copy_typed_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Clip", fontSize = 11.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = { viewModel.onKeyClear() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = fgText),
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp).testTag("clear_playground")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp), tint = fgText.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear", fontSize = 11.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(textAccent.copy(0.1f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = when (currentLayout) {
                                "PHONETIC_SINGLISH" -> "Phonetic: typing 'hela' -> 'හෙළ'"
                                "SINHALA_DIRECT" -> "Direct Sinhala Grid"
                                "ENGLISH_STANDARD" -> "Regular English Layout"
                                else -> ""
                            },
                            fontSize = 10.sp,
                            color = textAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Suggestion & Candidate Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = textAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            
            if (predictions.isEmpty()) {
                Text(
                    text = "Suggestions will appear as you type...",
                    fontSize = 11.sp,
                    color = fgText.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                )
            } else {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Start
                ) {
                    predictions.forEach { word ->
                        val wordCleaned = word.replace("_", " ")
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(textAccent.copy(alpha = 0.12f))
                                .clickable { viewModel.onSelectPrediction(wordCleaned) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = wordCleaned,
                                color = textAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontStyle
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- VIRTUAL KEYBOARD PANEL ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(primaryKeyboardBg)
                .border(2.dp, textAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                
                // Emoji / Symbol Header bar inside keyboard
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showEmojiPanel = !showEmojiPanel },
                        modifier = Modifier.size(34.dp).testTag("emoji_key_trigger")
                    ) {
                        Icon(
                            imageVector = if (showEmojiPanel) Icons.Default.KeyboardAlt else Icons.Default.EmojiEmotions,
                            contentDescription = "Emojis Menu",
                            tint = textAccent
                        )
                    }

                    // Virtual Keyboard Space Bar feedback
                    if (activeBuffer.isNotEmpty()) {
                        Text(
                            text = "Matching: \"${activeBuffer}\" to \"${viewModel.getTransliteratedPreview(activeBuffer)}\"",
                            color = fgText.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = fontStyle
                        )
                    } else {
                        Text(
                            text = "HelaSmart Typing Suite v1.0",
                            color = fgText.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Safe Input Secured",
                        tint = if (viewModel.incognitoMode.collectAsState().value) Color.Gray else textAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (showEmojiPanel) {
                    // Render emoji layout
                    EmojiLayoutView(
                        selectedCategory = selectedEmojiCat,
                        onCategorySelect = { viewModel.onSelectedEmojiCategoryChanged(it) },
                        onEmojiSelect = { viewModel.onKeyPress(it) },
                        primaryAccent = textAccent,
                        fgCol = fgText
                    )
                } else {
                    // Keyboard layout mapping based on selected layout mode
                    when (currentLayout) {
                        "PHONETIC_SINGLISH" -> {
                            val rows = listOf(
                                listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
                                listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
                                listOf("shift", "z", "x", "c", "v", "b", "n", "m", "backspace")
                            )
                            KeyboardRowsBuilder(rows, keyBg, textAccent, fontStyle, fontSizeMultiplier, keyCornersDp, viewModel)
                        }
                        "SINHALA_DIRECT" -> {
                            val rows = listOf(
                                listOf("අ", "ආ", "ඇ", "ඈ", "ඉ", "ඊ", "උ", "ඌ"),
                                listOf("එ", "ඒ", "ඔ", "ඕ", "ක", "ග", "ච", "ජ"),
                                listOf("ත", "ද", "න", "ප", "බ", "ම", "ය", "ර"),
                                listOf("ල", "ව", "ස", "හ", "backspace")
                            )
                            KeyboardRowsBuilder(rows, keyBg, textAccent, fontStyle, fontSizeMultiplier, keyCornersDp, viewModel)
                        }
                        "ENGLISH_STANDARD" -> {
                            val rows = listOf(
                                listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
                                listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
                                listOf("shift", "Z", "X", "C", "V", "B", "N", "M", "backspace")
                            )
                            KeyboardRowsBuilder(rows, keyBg, textAccent, fontStyle, fontSizeMultiplier, keyCornersDp, viewModel)
                        }
                    }

                    // Common Action Row (Space bar, language toggles, Clear bar)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Globe button for seamless toggle
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(keyCornersDp.dp))
                                .background(keyBg)
                                .clickable {
                                    val next = when (currentLayout) {
                                        "PHONETIC_SINGLISH" -> "SINHALA_DIRECT"
                                        "SINHALA_DIRECT" -> "ENGLISH_STANDARD"
                                        else -> "PHONETIC_SINGLISH"
                                    }
                                    viewModel.onKeyboardLayoutChanged(next)
                                }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp), tint = textAccent)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("globe", fontSize = 10.sp, color = fgText, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Spacebar Key
                        Box(
                            modifier = Modifier
                                .weight(5f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(keyCornersDp.dp))
                                .background(keyBg)
                                .border(1.dp, textAccent.copy(alpha = 0.4f), RoundedCornerShape(keyCornersDp.dp))
                                .clickable { viewModel.onKeySpace() }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SPACE",
                                color = fgText.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }

                        // Return/Enter Trigger (Just mock a space or add line)
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(keyCornersDp.dp))
                                .background(textAccent)
                                .clickable {
                                    viewModel.copyCurrentToClipboard()
                                    Toast.makeText(context, "Text added to Local Clips!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Default.Send, contentDescription = "Enter Key", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// Helper methods handled natively inside the core KeyboardViewModel

@Composable
fun KeyboardRowsBuilder(
    rows: List<List<String>>,
    keyBg: Color,
    textAccent: Color,
    fontStyle: FontFamily,
    fontSizeMultiplier: Int,
    corners: Int,
    viewModel: KeyboardViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { letter ->
                    val isSpecial = letter == "backspace" || letter == "shift"
                    val weight = if (isSpecial) 1.5f else 1f
                    
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .height(42.dp)
                            .clip(RoundedCornerShape(corners.dp))
                            .background(if (isSpecial) keyBg.copy(alpha = 0.61f) else keyBg)
                            .clickable {
                                if (letter == "backspace") {
                                    viewModel.onKeyBackspace()
                                } else if (letter == "shift") {
                                    // simple toggled shift mode can be mocked
                                    val isEnglish = viewModel.currentLayout.value == "ENGLISH_STANDARD"
                                    viewModel.onKeyboardLayoutChanged(
                                        if (isEnglish) "PHONETIC_SINGLISH" else "ENGLISH_STANDARD"
                                    )
                                } else {
                                    viewModel.onKeyPress(letter)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (letter == "backspace") {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Delete key",
                                tint = Color.Red.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        } else if (letter == "shift") {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = "Shift",
                                tint = textAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = letter,
                                color = if (letter.any { it.code in 0x0D80..0x0DFF }) textAccent else Color.White,
                                fontSize = fontSizeMultiplier.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontStyle
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmojiLayoutView(
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    onEmojiSelect: (String) -> Unit,
    primaryAccent: Color,
    fgCol: Color
) {
    val emojis = remember(selectedCategory) {
        when (selectedCategory) {
            "SMILEYS" -> listOf("😀", "😂", "🥰", "😍", "😘", "😜", "😎", "🥳", "🤔", "🥺", "💀", "👍", "🔥", "🎉", "❤️")
            "FOOD" -> listOf("🍎", "🍌", "🍉", "🍇", "🥑", "🍕", "🍔", "🍟", "🍩", "🥞", "☕", "🍺", "🥥", "🍛", "🥞")
            "FLAGS" -> listOf("🇱🇰", "🇺🇸", "🇬🇧", "🇨🇦", "🇦🇺", "🇯🇵", "🇮🇳", "🇨🇳", "🇩🇪", "🇫🇷", "🇳🇵", "🇸🇬", "🇲🇾", "🇧🇷", "🇷🇺")
            else -> listOf("⚽", "🎸", "🚗", "✈️", "🧭", "💻", "📱", "🏠", "🎁", "🎈", "🔑", "❤️", "🔔", "🌟", "🔥")
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Tab Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf(
                "SMILEYS" to "Smileys",
                "FOOD" to "Foods",
                "FLAGS" to "Flags & SL",
                "OBJECTS" to "Misc"
            ).forEach { (code, lbl) ->
                val active = selectedCategory == code
                Text(
                    text = lbl,
                    fontSize = 11.sp,
                    color = if (active) primaryAccent else fgCol.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onCategorySelect(code) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(emojis) { emoji ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(fgCol.copy(alpha = 0.05f))
                        .clickable { onEmojiSelect(emoji) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 20.sp)
                }
            }
        }
    }
}

// --- TAB SCREEN 2: HELAKURU NEWS aggregated feed ---
@Composable
fun HelakuruNewsScreen(
    viewModel: KeyboardViewModel,
    cardBg: Color,
    fgText: Color,
    dividerCol: Color,
    textAccent: Color
) {
    val newsSearch by viewModel.newsSearchQuery.collectAsState()
    val activeCat by viewModel.newsCategoryFilter.collectAsState()
    val articles by viewModel.filteredNews.collectAsState()

    var activeArticleDetail by remember { mutableStateOf<NewsArticle?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Text(
            text = "Helakuru Style Sri Lankan News Feed",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textAccent,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Search text field
        OutlinedTextField(
            value = newsSearch,
            onValueChange = { viewModel.onNewsSearchQueryChanged(it) },
            prefix = { Icon(Icons.Default.Search, contentDescription = null, sizeChangeModifier(16.dp)) },
            placeholder = { Text("Search local stories...", fontSize = 13.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("news_search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = fgText,
                unfocusedTextColor = fgText,
                focusedBorderColor = textAccent,
                unfocusedBorderColor = dividerCol,
                focusedContainerColor = cardBg,
                unfocusedContainerColor = cardBg
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Pill bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("All", "Technology", "National", "Bookmarked").forEach { catName ->
                val sel = activeCat == catName
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (sel) textAccent else cardBg)
                        .border(1.dp, if (sel) Color.Transparent else dividerCol, RoundedCornerShape(20.dp))
                        .clickable { viewModel.onNewsCategoryChanged(catName) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    val label = if (catName == "Bookmarked") "⭐️ Bookmarked" else catName
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (sel) Color.White else fgText.copy(0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (articles.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Announcement, contentDescription = null, tint = fgText.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No local updates match the filters.", color = fgText.copy(alpha = 0.5f), fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(articles) { item ->
                    NewsItemCard(
                        article = item,
                        cardBg = cardBg,
                        accentColor = textAccent,
                        fgCol = fgText,
                        onBookmarkToggle = { viewModel.toggleNewsBookmark(item.id, !item.isBookmarked) },
                        onViewDetails = { activeArticleDetail = item }
                    )
                }
            }
        }

        // Article details Alert Dialog
        activeArticleDetail?.let { article ->
            AlertDialog(
                onDismissRequest = { activeArticleDetail = null },
                confirmButton = {
                    TextButton(onClick = { activeArticleDetail = null }) {
                        Text("Awesome", color = textAccent, fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = article.category.uppercase(),
                            color = textAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { viewModel.toggleNewsBookmark(article.id, !article.isBookmarked) }
                        ) {
                            Icon(
                                imageVector = if (article.isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Pin Article",
                                tint = textAccent
                            )
                        }
                    }
                },
                text = {
                    Column {
                        Text(
                            text = article.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = fgText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Published: ${article.publishDate}  |  Views: ${article.viewsCount}",
                            fontSize = 10.sp,
                            color = fgText.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = article.content,
                            fontSize = 13.sp,
                            color = fgText,
                            lineHeight = 20.sp,
                            fontFamily = FontFamily.Default
                        )
                    }
                },
                containerColor = cardBg
            )
        }
    }
}

@Composable
fun NewsItemCard(
    article: NewsArticle,
    cardBg: Color,
    accentColor: Color,
    fgCol: Color,
    onBookmarkToggle: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewDetails),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = article.category,
                        color = accentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = article.publishDate,
                        fontSize = 11.sp,
                        color = fgCol.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        onClick = onBookmarkToggle,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (article.isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Bookmark story",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = article.title,
                fontSize = 14.sp,
                color = fgCol,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = article.content,
                fontSize = 12.sp,
                color = fgCol.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥 ${article.viewsCount} read this",
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Read Article →",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- TAB SCREEN 3: PERSONALIZATION & CUSTOM COLORS STUDIO ---
@Composable
fun ThemeSettingsScreen(
    viewModel: KeyboardViewModel,
    cardBg: Color,
    fgText: Color,
    dividerCol: Color,
    textAccent: Color,
    currentThemeBg: Color,
    currentKeyBg: Color
) {
    val themePreset by viewModel.themePreset.collectAsState()
    val activeFont by viewModel.keyboardFont.collectAsState()
    val cornersRad by viewModel.keyRoundness.collectAsState()
    val activeSizeMultiplier by viewModel.keyboardFontSize.collectAsState()

    var inputBgHex by remember { mutableStateOf("#1E1E1E") }
    var inputKeyHex by remember { mutableStateOf("#2D2D2D") }
    var inputTextHex by remember { mutableStateOf("#FFA500") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "HelaSmart Custom Studio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textAccent
            )
            Text(
                text = "Design presets, key corners, styles, and font coordinates.",
                fontSize = 12.sp,
                color = fgText.copy(alpha = 0.6f)
            )
        }

        // Interactive Live Keyboard Thumbnail Demo
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("ACTIVE KEYBOARD ACCENT DEMO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textAccent)
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(currentThemeBg)
                            .padding(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("ක", "හෙ", "ල", "කු", "රු").forEach { key ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(cornersRad.dp))
                                        .background(currentKeyBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = activeSizeMultiplier.sp,
                                        color = textAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Keyboard Themes Presets selection
        item {
            Text("Select Keyboard Palette Preset", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fgText)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "aura" to "✨ Aura Glass",
                    "helakuru" to "🍊 Helakuru Orange",
                    "cosmic" to "🌌 Cosmic Dark",
                    "pastel" to "🌸 Pastel Mint",
                    "custom" to "🎨 Custom Lab"
                ).forEach { (code, lbl) ->
                    val sel = themePreset == code
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (sel) textAccent else cardBg)
                            .border(1.dp, if (sel) Color.Transparent else dividerCol, RoundedCornerShape(10.dp))
                            .clickable { viewModel.onThemePresetChanged(code) }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = lbl,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (sel) Color.White else fgText.copy(0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Custom Studio HEX settings (Only if 'custom' theme is checked)
        if (themePreset == "custom") {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Custom Color Hex Inputs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textAccent)
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        OutlinedTextField(
                            value = inputBgHex,
                            onValueChange = { inputBgHex = it },
                            label = { Text("Keyboard Background Hex (e.g. #121212)", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputKeyHex,
                            onValueChange = { inputKeyHex = it },
                            label = { Text("Individual Key Hex (e.g. #2C2C2C)", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputTextHex,
                            onValueChange = { inputTextHex = it },
                            label = { Text("Letter Highlight Hex (e.g. #00FF00)", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.setCustomColors(inputBgHex, inputKeyHex, inputTextHex)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = textAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("apply_custom_colors")
                        ) {
                            Text("Apply Custom Color Coordinates", color = Color.White)
                        }
                    }
                }
            }
        }

        // Sizing & Margin Sliders
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Corner Roundedness: ${cornersRad}dp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fgText)
                    Slider(
                        value = cornersRad.toFloat(),
                        onValueChange = { viewModel.onKeyRoundnessChanged(it.toInt()) },
                        valueRange = 0f..24f,
                        colors = SliderDefaults.colors(thumbColor = textAccent, activeTrackColor = textAccent)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Keyboard Letter Font Size: ${activeSizeMultiplier}sp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fgText)
                    Slider(
                        value = activeSizeMultiplier.toFloat(),
                        onValueChange = { viewModel.onFontSizeChanged(it.toInt()) },
                        valueRange = 12f..24f,
                        colors = SliderDefaults.colors(thumbColor = textAccent, activeTrackColor = textAccent)
                    )
                }
            }
        }

        // Font Style Chooser
        item {
            Text("Keyboard Font Typography Pairing", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fgText)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Space Grotesk", "JetBrains Mono", "Playfair Serif").forEach { fontName ->
                    val sel = activeFont == fontName
                    val family = when (fontName) {
                        "JetBrains Mono" -> FontFamily.Monospace
                        "Playfair Serif" -> FontFamily.Serif
                        else -> FontFamily.Default
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (sel) textAccent else cardBg)
                            .border(1.dp, if (sel) Color.Transparent else dividerCol, RoundedCornerShape(10.dp))
                            .clickable { viewModel.onFontChanged(fontName) }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fontName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (sel) Color.White else fgText.copy(0.8f),
                            fontFamily = family
                        )
                    }
                }
            }
        }
    }
}

// --- TAB SCREEN 4: GEMINI AI WRITING ASSISTANT ---
@Composable
fun AiAssistantScreen(
    viewModel: KeyboardViewModel,
    cardBg: Color,
    fgText: Color,
    dividerCol: Color,
    textAccent: Color
) {
    val typedBuffer by viewModel.typedText.collectAsState()
    val aiResult by viewModel.aiResult.collectAsState()
    val aiLoading by viewModel.aiLoading.collectAsState()

    var customAiPrompt by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = textAccent, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gemini Powered Writing Co-Pilot",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textAccent
                )
            }
            Text(
                text = "Improve typing style, translate phonetic Singlish into formal Sinhala script, and get automated context recommendations.",
                fontSize = 12.sp,
                color = fgText.copy(alpha = 0.6f)
            )
        }

        // Current typing buffer display for context
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CURRENT PLAYGROUND BUFFER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textAccent)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(textAccent.copy(0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Context Source", fontSize = 8.sp, color = textAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(fgText.copy(0.04f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = if (typedBuffer.isBlank()) "(Typing Playground buffer is empty! Type something there first to ask Gemini)" else typedBuffer,
                            fontSize = 13.sp,
                            color = if (typedBuffer.isBlank()) fgText.copy(alpha = 0.4f) else fgText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Action grid to trigger AI
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Select Quick AI Operation", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fgText)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.askGeminiToAssist(typedBuffer, "formal") },
                            colors = ButtonDefaults.buttonColors(containerColor = textAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_formal_btn"),
                            enabled = !aiLoading
                        ) {
                            Text("Formal Sinhala 🇱🇰", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.askGeminiToAssist(typedBuffer, "english") },
                            colors = ButtonDefaults.buttonColors(containerColor = textAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_english_btn"),
                            enabled = !aiLoading
                        ) {
                            Text("To English 🇬🇧", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.askGeminiToAssist(typedBuffer, "emoji") },
                        colors = ButtonDefaults.buttonColors(containerColor = textAccent.copy(alpha = 0.15f), contentColor = textAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_emoji_btn"),
                        enabled = !aiLoading
                    ) {
                        Text("Recommend Emoji & Smart Replies 🌟", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Custom Query prompt textfield
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Custom AI Prompt Inquiry", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fgText)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customAiPrompt,
                        onValueChange = { customAiPrompt = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_ai_query"),
                        placeholder = { Text("Ask anything about Sinhala-English grammar, transliteration rules...", fontSize = 12.sp) },
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { 
                            if (customAiPrompt.isNotBlank()) {
                                viewModel.askGeminiToAssist(customAiPrompt, "custom")
                                customAiPrompt = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = textAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send Inquiry to Gemini", color = Color.White)
                    }
                }
            }
        }

        // Result Showcase
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Gemini Response Output", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textAccent)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (aiLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            CircularProgressIndicator(color = textAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Analyzing and composing response with Gemini 3.5...", fontSize = 13.sp, color = fgText.copy(0.7f))
                        }
                    } else if (aiResult.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(textAccent.copy(alpha = 0.05f))
                                .border(1.dp, textAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = aiResult,
                                fontSize = 14.sp,
                                color = fgText,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            text = "Results and transliterated answers will show up here.",
                            fontSize = 11.sp,
                            color = fgText.copy(alpha = 0.4f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- TAB SCREEN 5: SECURE CLIPBOARD MANAGER ---
@Composable
fun ClipboardManagerScreen(
    viewModel: KeyboardViewModel,
    cardBg: Color,
    fgText: Color,
    dividerCol: Color,
    textAccent: Color
) {
    val clips by viewModel.clipboardHistory.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Encrypted Local Clipboard Hub",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textAccent
                )
                Text(
                    text = "Persist snippets safely. Synchronized offline via Room DB.",
                    fontSize = 11.sp,
                    color = fgText.copy(alpha = 0.6f)
                )
            }
            
            IconButton(
                onClick = { 
                    viewModel.wipeClipboard()
                    Toast.makeText(context, "All clips deleted securely!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("clear_all_clipboard")
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Wipe Clipboard Logs", tint = Color.Red)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (clips.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ContentPasteOff, contentDescription = null, sizeChangeModifier(48.dp), tint = fgText.copy(alpha = 0.25f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Clipboard is clean.", color = fgText.copy(alpha = 0.4f), fontSize = 13.sp)
                    Text("Copied blocks inside the playground save here.", color = fgText.copy(alpha = 0.35f), fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(clips) { clip ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = clip.text,
                                    fontSize = 14.sp,
                                    color = fgText,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(10.dp), tint = fgText.copy(alpha = 0.4f))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", clip.timestamp).toString(),
                                        fontSize = 10.sp,
                                        color = fgText.copy(alpha = 0.4f)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.toggleClipboardPinned(clip.id, !clip.isPinned) }
                                ) {
                                    Icon(
                                        imageVector = if (clip.isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                                        contentDescription = "Pin log",
                                        tint = if (clip.isPinned) textAccent else fgText.copy(alpha = 0.3f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { 
                                        viewModel.onSelectPrediction(clip.text)
                                        Toast.makeText(context, "Pasted into Typing Playground!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentPasteGo, contentDescription = "Paste Clip", tint = textAccent, modifier = Modifier.size(18.dp))
                                }

                                IconButton(
                                    onClick = { viewModel.deleteClipboardItem(clip.id) }
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Wipe", tint = Color.Red.copy(0.7f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB SCREEN 6: PRIVACY CONTROLLER & STAR FEEDBACKS ---
@Composable
fun PrivacyFeedbackScreen(
    viewModel: KeyboardViewModel,
    cardBg: Color,
    fgText: Color,
    dividerCol: Color,
    textAccent: Color
) {
    val context = LocalContext.current
    
    val isIncognito by viewModel.incognitoMode.collectAsState()
    val autocorrectEnabled by viewModel.autocorrectEnabled.collectAsState()
    val predictiveTextEnabled by viewModel.predictiveTextEnabled.collectAsState()
    val customFeedbackRating by viewModel.feedbackRating.collectAsState()
    val customFeedbackComment by viewModel.feedbackComment.collectAsState()
    val statusFeedbackSubmission by viewModel.feedbackStatus.collectAsState()
    val dictionaryWords by viewModel.learnedWords.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Privacy Guard & Feedback portal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textAccent
            )
            Text(
                text = "Secure local-first architecture built to CCPA & GDPR standards.",
                fontSize = 11.sp,
                color = fgText.copy(alpha = 0.6f)
            )
        }

        // GDPR Information Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("GDPR/CCPA Compliance Guarantee", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textAccent)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "We enforce that all keystroke logging and transliteration dictionaries operate exclusively offline on this device. When Incognito mode is active, autocomplete dictionary learning is completely suspended. We never sync personal keystrokes to third-party servers.",
                        fontSize = 11.sp,
                        color = fgText.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Privacy Controls Form UI
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Device Database Toggles", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fgText)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Incognito Typing Mode", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = fgText)
                            Text("Do not record new word entries in local dictionary", fontSize = 10.sp, color = fgText.copy(alpha = 0.5f))
                        }
                        Switch(
                            checked = isIncognito,
                            onCheckedChange = { viewModel.onIncognitoChanged(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = textAccent, checkedTrackColor = textAccent.copy(0.4f))
                        )
                    }

                    Divider(color = dividerCol.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Correct Functionality", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = fgText)
                            Text("Fix misspelled items instantly upon entry", fontSize = 10.sp, color = fgText.copy(alpha = 0.5f))
                        }
                        Switch(
                            checked = autocorrectEnabled,
                            onCheckedChange = { viewModel.onAutocorrectChanged(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = textAccent, checkedTrackColor = textAccent.copy(0.4f))
                        )
                    }

                    Divider(color = dividerCol.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Predictive Suggestions", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = fgText)
                            Text("Display smart typing options on predictions row", fontSize = 10.sp, color = fgText.copy(alpha = 0.5f))
                        }
                        Switch(
                            checked = predictiveTextEnabled,
                            onCheckedChange = { viewModel.onPredictiveTextChanged(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = textAccent, checkedTrackColor = textAccent.copy(0.4f))
                        )
                    }
                }
            }
        }

        // Personal dictionary memory viewer
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Personal Learnt Vocabulary", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fgText)
                        TextButton(
                            onClick = {
                                viewModel.clearLearnedDictionary()
                                Toast.makeText(context, "Learned dictionary cleared!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Clear Words", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    if (dictionaryWords.isEmpty()) {
                        Text("No vocabulary learned yet.", fontSize = 11.sp, color = fgText.copy(0.4f))
                    } else {
                        // Display list of words
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = dictionaryWords.joinToString(", "),
                                fontSize = 12.sp,
                                color = textAccent,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Star Feedback panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Submit App Improvement Suggestion", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Help us optimize your Singlish typing layout. Give your feed backs.", fontSize = 10.sp, color = fgText.copy(alpha = 0.5f))
                    
                    Spacer(modifier = Modifier.height(10.dp))

                    // Stars rating row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..5).forEach { star ->
                            val isFilled = star <= customFeedbackRating
                            Icon(
                                imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Rating $star",
                                tint = if (isFilled) Color(0xFFFFC107) else fgText.copy(alpha = 0.25f),
                                modifier = Modifier
                                    .size(34.dp)
                                    .clickable { viewModel.onFeedbackRatingChanged(star) }
                                    .padding(horizontal = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "${customFeedbackRating} / 5 Stars",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = fgText
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customFeedbackComment,
                        onValueChange = { viewModel.onFeedbackCommentChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feedback_comment_input"),
                        placeholder = { Text("What did you like? Mention any missing Sinhala characters...", fontSize = 12.sp) },
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.submitFeedback() },
                        colors = ButtonDefaults.buttonColors(containerColor = textAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feedback_submit_btn"),
                        enabled = statusFeedbackSubmission != "Submitting" && customFeedbackComment.isNotBlank()
                    ) {
                        Text(
                            text = if (statusFeedbackSubmission == "Submitting") "Submitting secure feedback..." else "Send Safe Feedback",
                            color = Color.White
                        )
                    }

                    if (statusFeedbackSubmission == "Success") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF10B981).copy(0.1f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "✨ Thank you! Your typing feedback was securely stored locally & registered.",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { viewModel.resetFeedbackStatus() }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Custom Helper functions
fun sizeChangeModifier(size: androidx.compose.ui.unit.Dp): Modifier {
    return Modifier.size(size)
}

fun parseColor(hex: String, fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
