package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AnimatedBarrierGate
import com.example.ui.components.Button3D
import com.example.ui.components.SettingsDialog
import com.example.ui.components.Shield3DIcon
import com.example.viewmodel.AppViewModel

@Composable
fun AuthScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val pinInput by viewModel.pinInput.collectAsState()
    val activationError by viewModel.activationError.collectAsState()
    val isActivating by viewModel.isActivating.collectAsState()
    val appState by viewModel.appStateFlow.collectAsState()
    val lockoutTimerText by viewModel.lockoutTimerText.collectAsState()
    val isAdminTestMode by viewModel.isAdminTestMode.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1424)) // Deep Obsidian background
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Administrative Test Mode Toggle Switch (Top bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isAdminTestMode) "TRYB TESTOWY: AKTYWNY" else "TRYB STANDARDOWY",
                fontSize = 11.sp,
                color = if (isAdminTestMode) Color(0xFFF59E0B) else Color(0xFF475569),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.testTag("test_mode_status_label")
            )
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isAdminTestMode) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFF1E293B))
                    .clickable { viewModel.toggleAdminTestMode() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("admin_test_mode_toggle"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isAdminTestMode) "WYŁĄCZ" else "WŁĄCZ TEST",
                    fontSize = 11.sp,
                    color = if (isAdminTestMode) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Center Content Block
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            // Shield 3D Icon centered at top of context
            Shield3DIcon(modifier = Modifier.testTag("shield_3d_icon"))

            Spacer(modifier = Modifier.height(16.dp))

            // Heading Text
            Text(
                text = "AUTORYZACJA",
                fontSize = 26.sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "WPISZ PIN",
                fontSize = 15.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Pin digit display row (6 slots with perfectly equal widths)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(bottom = 24.dp)
                    .testTag("pin_code_fields")
            ) {
                for (i in 0 until 6) {
                    val hasDigit = pinInput.length > i
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (hasDigit) Color(0xFF3B82F6).copy(alpha = 0.2f)
                                else Color(0xFF1E293B)
                            )
                            .testTag("pin_digit_slot_$i"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasDigit) {
                            // Circular solid index representing typing
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6))
                            )
                        } else {
                            // Empty box symbol
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF475569))
                            )
                        }
                    }
                }
            }

            // Error Message Box
            Box(
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isActivating) {
                    CircularProgressIndicator(
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.size(28.dp)
                    )
                } else if (lockoutTimerText != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Blokada bezpieczeństwa",
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Pozostało: $lockoutTimerText",
                            color = Color(0xFFEF4444),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("lockout_countdown")
                        )
                    }
                } else if (activationError != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = activationError ?: "",
                            color = Color(0xFFEF4444),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("activation_failed_message")
                        )
                        val remaining = 3 - appState.errorCount
                        if (remaining > 0) {
                            Text(
                                text = "Pozostało prób: $remaining",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Numeric keypad grid (0-9, delete)
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val buttonRows = listOf(
                    listOf('1', '2', '3'),
                    listOf('4', '5', '6'),
                    listOf('7', '8', '9'),
                    listOf(' ', '0', 'D') // Blank space placeholder instead of CLR to maintain perfectly equal sizes
                )

                buttonRows.forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowKeys.forEach { key ->
                            val isSpecial = key == ' ' || key == 'D'
                            val isPlaceholder = key == ' '
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isPlaceholder) Color.Transparent
                                        else if (isSpecial) Color(0xFF1E293B)
                                        else Color(0xFF161E36)
                                    )
                                    .clickable(
                                        enabled = !isPlaceholder && lockoutTimerText == null && !isActivating,
                                        onClick = {
                                            when (key) {
                                                'D' -> viewModel.onDeletePress()
                                                else -> viewModel.onKeyPress(key)
                                            }
                                        }
                                    )
                                    .testTag("keypad_$key"),
                                contentAlignment = Alignment.Center
                            ) {
                                when (key) {
                                    'D' -> {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Usuń",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    ' ' -> {
                                        // Invisible empty placeholder maintaining identical cell aspect & dimensions
                                    }
                                    else -> {
                                        Text(
                                            text = key.toString(),
                                            color = Color.White,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Standardized footer with high-fidelity identity
        Text(
            text = "Stanisław Piwowarski\nModern-Expo S.A.",
            color = Color(0xFF8FA0B4),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .testTag("footer_author")
        )
    }
}

@Composable
fun OperationScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val isOnline by viewModel.isOnline.collectAsState()
    val isOpening by viewModel.isOpening.collectAsState()
    val barrierResponse by viewModel.barrierResponse.collectAsState()
    val barrierState by viewModel.barrierState.collectAsState()
    val barrierCountdown by viewModel.barrierCountdown.collectAsState()
    val isAdminTestMode by viewModel.isAdminTestMode.collectAsState()

    var showDiagDialog by remember { mutableStateOf(false) }

    val activity = LocalContext.current as? Activity

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1424)) // Dark premium Obsidian
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Heartbeat status indicator & optional Test Mode badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF161E36))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("status_indicator")
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) Color(0xFF10B981) else Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOnline) "ONLINE" else "OFFLINE",
                        color = if (isOnline) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                if (isAdminTestMode) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF59E0B)) // Amber color
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("admin_badge")
                    ) {
                        Text(
                            text = "TRYB TESTOWY",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Top action buttons row (diagnostic settings & exit launcher)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { showDiagDialog = true },
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ustawienia",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        activity?.finish()
                    },
                    modifier = Modifier.testTag("close_app_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Zatrzymaj",
                        tint = Color.White
                    )
                }
            }
        }

        // Settings Dialog trigger
        if (showDiagDialog) {
            SettingsDialog(
                viewModel = viewModel,
                onDismiss = { showDiagDialog = false }
            )
        }

        // CENTER Area (Barrier Gate Animation & Open Controls)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Label Header
            Text(
                text = "KONTROLA WJAZDU",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Animated view container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF161E36))
                    .padding(16.dp)
                    .testTag("barrier_view_panel"),
                contentAlignment = Alignment.Center
            ) {
                AnimatedBarrierGate(
                    barrierState = barrierState,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // State information label block under the animation (with crossfade fade out/in animation)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Crossfade(
                    targetState = barrierState,
                    animationSpec = tween(durationMillis = 400)
                ) { currentState ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val stateText = when (currentState) {
                            "OPENING" -> "OTWIERANIE"
                            "OPEN" -> "SZLABAN OTWARTY"
                            "CLOSING" -> "ZAMYKANIE"
                            else -> "SZLABAN ZAMKNIĘTY"
                        }

                        Text(
                            text = stateText,
                            fontSize = 18.sp,
                            color = when (currentState) {
                                "OPENING", "CLOSING" -> Color(0xFFF59E0B)
                                "OPEN" -> Color(0xFF10B981)
                                else -> Color(0xFFEF4444)
                            },
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            modifier = Modifier.testTag("barrier_state_label")
                        )

                        // Render remaining timer in active animation
                        if (currentState != "CLOSED") {
                            Text(
                                text = when (currentState) {
                                    "OPEN" -> "Czas do zamknięcia: $barrierCountdown s"
                                    "OPENING" -> "Pełne otwarcie za: $barrierCountdown s"
                                    "CLOSING" -> "Pełne zamknięcie za: $barrierCountdown s"
                                    else -> ""
                                },
                                fontSize = 14.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .testTag("barrier_timer_label")
                            )
                        } else {
                            Text(
                                text = "Stan gotowości do otwarcia",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Interactive Tactile 3D Action Button
            Button3D(
                text = "Otwórz szlaban",
                onClick = { viewModel.openBarrier() },
                modifier = Modifier.testTag("open_barrier_button"),
                backgroundColor = Color(0xFF3B82F6),
                shadowColor = Color(0xFF1D4ED8),
                enabled = barrierState == "CLOSED" && !isOpening
            )

            // Server outcome message container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (barrierResponse != null) {
                    val isFail = barrierResponse!!.contains("daleko", ignoreCase = true) || 
                                 barrierResponse!!.contains("Błąd", ignoreCase = true) ||
                                 barrierResponse!!.contains("Brak połączenia", ignoreCase = true)

                    if (isFail) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("server_response_box"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = barrierResponse ?: "",
                                color = Color(0xFFF87171),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Standardized footer with high-fidelity identity
        Text(
            text = "Stanisław Piwowarski\nModern-Expo S.A.",
            color = Color(0xFF8FA0B4),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .testTag("footer_author_op")
        )
    }
}
