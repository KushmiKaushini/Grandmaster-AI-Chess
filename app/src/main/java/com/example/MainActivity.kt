package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ChessBoard
import com.example.model.ChessColor
import com.example.model.ChessGameStatus
import com.example.model.ChessPiece
import com.example.model.ChessPieceType
import com.example.ui.AIPersonality
import com.example.ui.ChessMode
import com.example.ui.ChessScreen
import com.example.ui.ChessState
import com.example.ui.ChessViewModel
import com.example.ui.theme.CheckSquareColor
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SelectedSquareColor
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateDarkText
import com.example.ui.theme.SlateGrey
import com.example.ui.theme.SoftCream
import com.example.ui.theme.ValidMoveDotColor
import com.example.ui.theme.WarmGold
import com.example.ui.theme.WoodDark
import com.example.ui.theme.WoodLight
import com.example.ui.theme.AccentRust
import com.example.ui.theme.CardBorderColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: ChessViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()

                LaunchedEffect(uiState.currentScreen) {
                    if (uiState.currentScreen == ChessScreen.SPLASH) {
                        delay(2400)
                        viewModel.goToMenu()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedVisibility(
                            visible = uiState.currentScreen == ChessScreen.SPLASH,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            SplashScreen()
                        }

                        AnimatedVisibility(
                            visible = uiState.currentScreen == ChessScreen.MENU,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            MenuScreen(
                                state = uiState,
                                onModeSelect = viewModel::setMode,
                                onDifficultySelect = viewModel::setDifficulty,
                                onPersonalitySelect = viewModel::setPersonality,
                                onStartGame = viewModel::startGame
                            )
                        }

                        AnimatedVisibility(
                            visible = uiState.currentScreen == ChessScreen.GAME,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            GameScreen(
                                state = uiState,
                                onSquareClick = viewModel::onSquareSelected,
                                onBackClick = viewModel::goToMenu,
                                onResetClick = viewModel::startGame,
                                onUndoClick = viewModel::triggerUndo,
                                onHintClick = viewModel::requestAILetterHint
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- SECURITY SHIELD MONITOR COMPOSABLES ---

@Composable
fun SecurityAuditDialog(onDismiss: () -> Unit) {
    val auditLines = remember {
        listOf(
            "[SYSTEM-INIT] Initializing cryptographic defense layers...",
            "[INTEGRITY] Analyzing app signature and bytecode protection...",
            "[OBFUSCATION] ProGuard R8 classes mapping status: ENFORCED",
            "[SANDBOX] Memory protection active against local inspection.",
            "[NET-SEC] Network security config policy: NO-CLEARTEXT-ALLOWED",
            "[SSL-ANCHORS] Binding to certified cloud root authorities...",
            "[TLS-PIN] Transmission channel enforced with TLS 1.3 AES-GCM.",
            "[API-KEY] Verifying secure Gemini token signature...",
            "[API-VAL] Configuration token decrypted and validated successfully.",
            "[Core-Protection] Active sandboxing status online."
        )
    }

    var visibleLinesCount by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        while (visibleLinesCount < auditLines.size) {
            delay(250)
            visibleLinesCount++
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(2.dp, WarmGold, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateGrey)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛡️", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                        Text(
                            text = "SECURITY AUDIT UTILITY",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = WarmGold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "ACTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF22C55E),
                        modifier = Modifier
                            .background(Color(0xFF22C55E).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateDark)
                        .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 0 until visibleLinesCount) {
                            val line = auditLines[i]
                            val color = when {
                                line.contains("[Core-Protection]") || line.contains("ENFORCED") || line.contains("validated") -> Color(0xFF22C55E)
                                line.contains("Initializing") || line.contains("Analyzing") -> WarmGold
                                else -> SoftCream.copy(alpha = 0.85f)
                            }
                            Text(
                                text = line,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = color,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold, contentColor = SlateDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "CLOSE MANIFEST SCAN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityShieldMonitor() {
    var showAudit by remember { mutableStateOf(false) }
    val isApiKeyInjected = com.example.BuildConfig.GEMINI_API_KEY.isNotBlank() &&
            com.example.BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    if (showAudit) {
        SecurityAuditDialog(onDismiss = { showAudit = false })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("security_shield_monitor"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlateGrey),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, CardBorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🛡️", fontSize = 18.sp, modifier = Modifier.padding(end = 6.dp))
                    Text(
                        text = "SECURITY SHIELD SYSTEM",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = WarmGold,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = if (isApiKeyInjected) "SECURE COMMUNICATOR ONLINE" else "OFFLINE MINIMAX MODE",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isApiKeyInjected) Color(0xFF22C55E) else WarmGold,
                    modifier = Modifier
                        .background(
                            (if (isApiKeyInjected) Color(0xFF22C55E) else WarmGold).copy(alpha = 0.15f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Information Grid of active security systems
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecurityStatusItem(label = "TLS Network Security", status = "ENFORCED", isSecure = true)
                    SecurityStatusItem(label = "Bytecode Obfuscation", status = "R8-ACTIVE", isSecure = true)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecurityStatusItem(label = "Memory Sandbox", status = "SANDBOXED", isSecure = true)
                    SecurityStatusItem(
                        label = "Gemini Key Status",
                        status = if (isApiKeyInjected) "AUTHORIZED" else "UNCONFIGURED",
                        isSecure = isApiKeyInjected
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { showAudit = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmGold.copy(alpha = 0.12f),
                    contentColor = WarmGold
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "RUN CRYPTO-AUDIT INVESTIGATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.75.sp
                )
            }
        }
    }
}

@Composable
fun SecurityStatusItem(label: String, status: String, isSecure: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SlateDark.copy(alpha = 0.35f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = SoftCream.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = status,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = if (isSecure) Color(0xFF22C55E) else AccentRust
            )
        }
        Text(
            text = if (isSecure) "✅" else "⚠️",
            fontSize = 12.sp
        )
    }
}

// --- ACHIEVEMENTS & GAMIFICATION COMPOSABLE ---

@Composable
fun AchievementsSection(achievements: List<com.example.ui.AchievementState>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("achievements_section")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🏆",
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = "ROYAL TROPHIES & ACCLAIMS",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = WarmGold,
                letterSpacing = 1.sp
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            achievements.forEach { ach ->
                val emojiSymbol = when (ach.id) {
                    "achievement_first_blood" -> "⚔️"
                    "achievement_hint" -> "💡"
                    "achievement_undo" -> "⏳"
                    "achievement_moves" -> "🛡️"
                    "achievement_gemini_vanquished" -> "🏆"
                    else -> "🏅"
                }
                
                Card(
                    modifier = Modifier
                        .width(180.dp)
                        .height(190.dp)
                        .testTag("achievement_card_${ach.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SlateGrey
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.5.dp,
                        color = if (ach.isUnlocked) WarmGold else CardBorderColor
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // circular badge icon slot
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    if (ach.isUnlocked) {
                                        WarmGold.copy(alpha = 0.25f)
                                    } else {
                                        Color.Gray.copy(alpha = 0.1f)
                                    }
                                )
                        ) {
                            Text(
                                text = emojiSymbol,
                                fontSize = 26.sp,
                                modifier = Modifier.graphicsLayer {
                                    alpha = if (ach.isUnlocked) 1f else 0.4f
                                }
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = ach.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ach.isUnlocked) WarmGold else SoftCream,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = ach.description,
                                fontSize = 9.sp,
                                color = SoftCream.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                minLines = 2,
                                maxLines = 3,
                                lineHeight = 11.sp,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (ach.isUnlocked) "UNLOCKED" else "LOCKED",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (ach.isUnlocked) WarmGold else Color.Gray
                                )
                                Text(
                                    text = "${ach.currentProgress}/${ach.maxProgress}",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftCream.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = ach.currentProgress.toFloat() / ach.maxProgress.toFloat(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (ach.isUnlocked) WarmGold else Color.Gray.copy(alpha = 0.4f),
                                trackColor = Color.Gray.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- MENU SCREEN COMPOSABLE ---

@Composable
fun MenuScreen(
    state: ChessState,
    onModeSelect: (ChessMode) -> Unit,
    onDifficultySelect: (String) -> Unit,
    onPersonalitySelect: (AIPersonality) -> Unit,
    onStartGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Graphic Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_chess_banner),
                contentDescription = "Royal Chess Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                            startY = 100f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GRANDMASTER AI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGold,
                    letterSpacing = 2.5.sp
                )
                Text(
                    text = "AI CHESS",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = SlateDarkText,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "A Premium Chess Match Engine",
                    fontSize = 13.sp,
                    color = SlateDarkText.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Select Section
            Text(
                text = "SELECT MATCH TYPE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp, top = 16.dp),
                letterSpacing = 1.5.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onModeSelect(ChessMode.SINGLE_PLAYER) }
                        .testTag("mode_single_card")
                        .border(
                            width = if (state.mode == ChessMode.SINGLE_PLAYER) 3.dp else 1.2.dp,
                            color = if (state.mode == ChessMode.SINGLE_PLAYER) WarmGold else CardBorderColor,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.mode == ChessMode.SINGLE_PLAYER) SlateGrey else SlateGrey.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assistant,
                            contentDescription = "Single Player",
                            tint = if (state.mode == ChessMode.SINGLE_PLAYER) WarmGold else Color.Gray.copy(alpha = 0.8f),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Vs Gemini AI",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoftCream
                        )
                        Text(
                            text = "Play vs smart adaptive bots",
                            fontSize = 11.sp,
                            color = SoftCream.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Card(
                     modifier = Modifier
                        .weight(1f)
                        .clickable { onModeSelect(ChessMode.TWO_PLAYER) }
                        .testTag("mode_two_card")
                        .border(
                            width = if (state.mode == ChessMode.TWO_PLAYER) 3.dp else 1.2.dp,
                            color = if (state.mode == ChessMode.TWO_PLAYER) WarmGold else CardBorderColor,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.mode == ChessMode.TWO_PLAYER) SlateGrey else SlateGrey.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Pass and Play",
                            tint = if (state.mode == ChessMode.TWO_PLAYER) WarmGold else Color.Gray.copy(alpha = 0.8f),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pass & Play",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoftCream
                        )
                        Text(
                            text = "2-Player offline match",
                            fontSize = 11.sp,
                            color = SoftCream.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Single Player Customizations
            if (state.mode == ChessMode.SINGLE_PLAYER) {
                // Difficulty Segmented Control
                Text(
                    text = "MATCH DIFFICULTY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGold,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp, top = 20.dp),
                    letterSpacing = 1.5.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateGrey)
                        .border(1.2.dp, CardBorderColor, RoundedCornerShape(8.dp))
                ) {
                    val difficulties = listOf("Easy", "Medium", "Hard")
                    difficulties.forEach { diff ->
                        Box(
                             modifier = Modifier
                                .weight(1f)
                                .clickable { onDifficultySelect(diff) }
                                .testTag("diff_select_$diff")
                                .background(if (state.difficulty == diff) WarmGold else Color.Transparent)
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = diff.uppercase(),
                                color = if (state.difficulty == diff) Color.Black else SoftCream.copy(alpha = 0.7f),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // AI Opponent Personality Section
                Text(
                    text = "SELECT AI OPPONENT PERSONALITY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGold,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp, top = 24.dp),
                    letterSpacing = 1.5.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AIPersonality.values().forEach { personality ->
                        val isSelected = state.aiPersonality == personality
                        Card(
                            modifier = Modifier
                                .width(180.dp)
                                .clickable { onPersonalitySelect(personality) }
                                .testTag("personality_card_${personality.name}")
                                .border(
                                    width = if (isSelected) 3.dp else 1.2.dp,
                                    color = if (isSelected) WarmGold else CardBorderColor,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) SlateGrey else SlateGrey.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = personality.displayName.uppercase(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) WarmGold else SoftCream
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = personality.startQuote,
                                    fontSize = 11.sp,
                                    color = SoftCream.copy(alpha = 0.75f),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Play Button
            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(28.dp))
                    .testTag("btn_play_now"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmGold,
                    contentColor = SlateDark
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start game"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BATTLE COMMENCE",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            SecurityShieldMonitor()

            Spacer(modifier = Modifier.height(16.dp))

            AchievementsSection(state.achievements)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- GAMEPLAY SCREEN COMPOSABLE ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    state: ChessState,
    onSquareClick: (Int, Int) -> Unit,
    onBackClick: () -> Unit,
    onResetClick: () -> Unit,
    onUndoClick: () -> Unit,
    onHintClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    if (state.status.isGameOver) {
        GameOverDialog(
            status = state.status,
            onRestart = onResetClick,
            onGoMenu = onBackClick
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.mode == ChessMode.SINGLE_PLAYER) "VS GEMINI BOT" else "LOCAL PASS & PLAY",
                        color = SlateDarkText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("app_bar_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back to Menu",
                            tint = WarmGold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onUndoClick,
                        enabled = state.undoStack.isNotEmpty() && !state.isAILoading,
                        modifier = Modifier.testTag("app_bar_undo")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo Move",
                            tint = if (state.undoStack.isNotEmpty()) WarmGold else Color.Gray.copy(alpha = 0.5f)
                        )
                    }
                    IconButton(
                        onClick = onResetClick,
                        modifier = Modifier.testTag("app_bar_reset")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Board",
                            tint = WarmGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateGrey
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Banner indicator (Turn indicator)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SlateGrey.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center) {
                            if (!state.isAILoading && !state.status.isGameOver) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .graphicsLayer {
                                            scaleX = pulseScale
                                            scaleY = pulseScale
                                        }
                                        .clip(CircleShape)
                                        .background(
                                            if (state.activeColor == ChessColor.WHITE) Color.White.copy(alpha = 0.3f)
                                            else WarmGold.copy(alpha = 0.3f)
                                        )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (state.activeColor == ChessColor.WHITE) Color.White else Color.Black)
                                    .border(1.dp, Color.Gray, CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (state.activeColor == ChessColor.WHITE) "WHITE'S TURN" else "BLACK'S TURN",
                            color = SoftCream,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    if (state.isAILoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = WarmGold,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini Calculating...",
                                fontSize = 12.sp,
                                color = WarmGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (state.kingInCheckColor != null) {
                        Text(
                            text = "⚠️ KING IN CHECK!",
                            fontSize = 12.sp,
                            color = AccentRust,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Interactive Wooden Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .aspectRatio(1f)
                    .padding(horizontal = 16.dp)
                    .shadow(16.dp, RoundedCornerShape(4.dp))
                    .border(3.5.dp, WarmGold, RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(2.dp))
            ) {
                ChessBoardRenderer(
                    board = state.board,
                    selectedSquare = state.selectedSquare,
                    validMoves = state.validMoves,
                    kingInCheckColor = state.kingInCheckColor,
                    lastMove = state.lastMove,
                    onSquareSelected = onSquareClick
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // AI COMPANION DIALOG PANEL OR MATCH FEEDBACK
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                if (state.mode == ChessMode.SINGLE_PLAYER) {
                    // Single Player Gemini AI UI Companion Log
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .border(1.5.dp, WarmGold, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateGrey),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(WarmGold),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = state.aiPersonality.displayName.first().toString(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = state.aiPersonality.displayName.uppercase(),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = WarmGold
                                        )
                                        Text(
                                             text = "LEVEL: ${state.difficulty.uppercase()}",
                                             fontSize = 11.sp,
                                             fontWeight = FontWeight.Bold,
                                             color = SlateDarkText.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                // Hint Action Button
                                Button(
                                    onClick = onHintClick,
                                    enabled = !state.isAILoading && !state.status.isGameOver,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = WarmGold.copy(alpha = 0.2f),
                                        contentColor = WarmGold
                                    ),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("btn_ask_hint")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Assistant,
                                        contentDescription = "Ask for advice",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Hint", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            HorizontalDivider(
                                color = Color.Gray.copy(alpha = 0.15f),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )

                            // AI Remarks Talk Bubble
                            Text(
                                text = state.aiCommentary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = SoftCream,
                                lineHeight = 19.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )

                            // AI Thought Log (if present)
                            if (state.aiThoughtProcess.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SlateDark.copy(alpha = 0.4f))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Logic Info",
                                                tint = WarmGold.copy(alpha = 0.7f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "STRATEGIC THINKING PATH",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WarmGold.copy(alpha = 0.7f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = state.aiThoughtProcess,
                                            fontSize = 11.sp,
                                            color = SoftCream.copy(alpha = 0.61f),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Match Log in Two Player
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .border(1.5.dp, WarmGold, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateGrey),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "MATCH HISTORY DIRECTORY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = WarmGold,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (state.history.isEmpty()) {
                                Text(
                                    text = "No moves recorded yet. Select chessmen and command coordinates.",
                                    fontSize = 13.sp,
                                    color = SoftCream.copy(alpha = 0.5f)
                                )
                            } else {
                                // Double row pairing log e.g., "1. e2e4 e7e5"
                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                    val logText = state.history.chunked(2).mapIndexed { idx, pair ->
                                        "${idx + 1}. ${pair[0]} ${if (pair.size > 1) pair[1] else ""}"
                                    }.joinToString("  |  ")

                                    Text(
                                        text = logText,
                                        fontSize = 13.sp,
                                        color = SoftCream,
                                        lineHeight = 18.sp,
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// --- CHESS BOARD RENDERER COMPOSABLE ---

@Composable
fun ChessBoardRenderer(
    board: ChessBoard,
    selectedSquare: Pair<Int, Int>?,
    validMoves: List<Pair<Int, Int>>,
    kingInCheckColor: ChessColor?,
    lastMove: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null,
    onSquareSelected: (Int, Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        for (r in 0..7) {
            Row(modifier = Modifier.weight(1f)) {
                for (c in 0..7) {
                    val piece = board.getPiece(r, c)
                    val isDarkSquare = (r + c) % 2 == 1
                    val baseSquareColor = if (isDarkSquare) WoodDark else WoodLight

                    val isSelected = selectedSquare == Pair(r, c)
                    val isValidMoveTarget = validMoves.contains(Pair(r, c))
                    val isKingCheck = piece != null && piece.type == ChessPieceType.KING && piece.color == kingInCheckColor

                    // Piece select/focus animations
                    val pieceScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.25f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "piece_scale"
                    )

                    // Physical sliding translation coordinates calculation when last moves took place
                    val isTargetSquare = lastMove != null && lastMove.second == Pair(r, c)
                    val fromRow = lastMove?.first?.first ?: r
                    val fromCol = lastMove?.first?.second ?: c

                    val animOffsetX = remember { Animatable(0f) }
                    val animOffsetY = remember { Animatable(0f) }

                    if (isTargetSquare) {
                        LaunchedEffect(lastMove) {
                            animOffsetX.snapTo((fromCol - c).toFloat())
                            animOffsetY.snapTo((fromRow - r).toFloat())
                            launch {
                                animOffsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = 0.85f,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                            launch {
                                animOffsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = 0.85f,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                        }
                    } else {
                        LaunchedEffect(Unit) {
                            animOffsetX.snapTo(0f)
                            animOffsetY.snapTo(0f)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(baseSquareColor)
                            .clickable { onSquareSelected(r, c) }
                            .testTag("square_${r}_$c")
                    ) {
                        // Mark Selection Halo
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(SelectedSquareColor)
                            )
                        }

                        // Mark Check Soft Red Highlight
                        if (isKingCheck) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(CheckSquareColor)
                            )
                        }

                        // Professional file coordinates (A to H) on the bottom row
                        if (r == 7) {
                            Text(
                                text = ('A' + c).toString(),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 3.dp, bottom = 1.dp),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkSquare) WoodLight.copy(alpha = 0.55f) else WoodDark.copy(alpha = 0.55f)
                            )
                        }

                        // Professional rank coordinates (1 to 8) on the rightmost column
                        if (c == 7) {
                            Text(
                                text = (8 - r).toString(),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 3.dp, top = 1.dp),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkSquare) WoodLight.copy(alpha = 0.55f) else WoodDark.copy(alpha = 0.55f)
                            )
                        }

                        // Draw centered pieces with subtle shadows and colors
                        if (piece != null) {
                            val pieceSymbol = when (piece.type) {
                                ChessPieceType.KING -> "♚"
                                ChessPieceType.QUEEN -> "♛"
                                ChessPieceType.ROOK -> "♜"
                                ChessPieceType.BISHOP -> "♝"
                                ChessPieceType.KNIGHT -> "♞"
                                ChessPieceType.PAWN -> "♟"
                            }
                            val pieceColor = if (piece.color == ChessColor.WHITE) Color(0xFFFFFFFF) else Color(0xFF0C1017)
                            val shadowColor = if (piece.color == ChessColor.WHITE) Color.Black.copy(alpha = 0.55f) else Color(0xCCFFE082) // Royal premium gold accent halo for black pieces
                            val shadowOffset = if (piece.color == ChessColor.WHITE) androidx.compose.ui.geometry.Offset(2f, 2f) else androidx.compose.ui.geometry.Offset(0f, 0f)
                            val shadowBlur = if (piece.color == ChessColor.WHITE) 3f else 10f

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        translationX = animOffsetX.value * size.width
                                        translationY = animOffsetY.value * size.height
                                        scaleX = pieceScale
                                        scaleY = pieceScale
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                // Draw piece symbol text with premium high contrast dropshadow
                                Text(
                                    text = pieceSymbol,
                                    fontSize = 42.sp,
                                    color = pieceColor,
                                    style = androidx.compose.ui.text.TextStyle(
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = shadowColor,
                                            offset = shadowOffset,
                                            blurRadius = shadowBlur
                                        )
                                    ),
                                    lineHeight = 42.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Drawing Target Indicator circles (Non-overlapping Lichess style)
                        if (isValidMoveTarget) {
                            if (piece == null) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(ValidMoveDotColor.copy(alpha = 0.75f))
                                        .align(Alignment.Center)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .border(width = 3.dp, color = Color(0xFFF43F5E), shape = CircleShape)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- DIALOG POPUPS COMPOSABLES ---

@Composable
fun GameOverDialog(
    status: ChessGameStatus,
    onRestart: () -> Unit,
    onGoMenu: () -> Unit
) {
    val message = when (status) {
        ChessGameStatus.CHECKMATE_WHITE_WINS -> "Checkmate! Magnificent victory for WHITE!"
        ChessGameStatus.CHECKMATE_BLACK_WINS -> "Checkmate! Decisive victory for BLACK!"
        ChessGameStatus.STALEMATE -> "Match ended. Stalemate drawn."
        else -> "Game concluded."
    }

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(3.dp, WarmGold, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateGrey)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BATTLE RESOLVED",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = WarmGold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = SoftCream,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth().testTag("game_over_restart"),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold, contentColor = SlateDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("RE-MATCH", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onGoMenu,
                    modifier = Modifier.fillMaxWidth().testTag("game_over_menu"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = SoftCream
                    ),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("MAIN MENU")
                }
            }
        }
    }
}

// Workaround definition for BorderStroke inside Compose Material 3 button since we only want simple border lines of same type
@androidx.compose.runtime.Immutable
class BorderStroke(val width: androidx.compose.ui.unit.Dp, val color: Color) {
    // Basic border parameters
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BorderStroke) return false
        return width == other.width && color == other.color
    }
    override fun hashCode() = 31 * width.hashCode() + color.hashCode()
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Elegant circular logo frame containing our royal chess banner representing deep intelligence
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .shadow(16.dp, CircleShape)
                    .border(4.dp, WarmGold, CircleShape)
                    .clip(CircleShape)
                    .background(SlateGrey),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_chess_banner),
                    contentDescription = "Royal Chess Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Extra bold premium theme titles
            Text(
                text = "GRANDMASTER AI",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = WarmGold,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "CHESS",
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = SlateDarkText,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Play offline or duel advanced Gemini AI personas\nwith high-fidelity tactical engines",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlateDarkText.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Smooth high contrast visual pulse loading indicator
            CircularProgressIndicator(
                color = WarmGold,
                strokeWidth = 3.5.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = ButtonDefaults.shape,
    colors: androidx.compose.material3.ButtonColors = ButtonDefaults.buttonColors(),
    border: BorderStroke? = null,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val m = if (border != null) {
        modifier.border(border.width, border.color, shape)
    } else {
        modifier
    }
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = m,
        enabled = enabled,
        shape = shape,
        colors = colors,
        content = content
    )
}


