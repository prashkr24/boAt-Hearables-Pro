package com.boat.hearables.pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boat.hearables.pro.data.AccentTheme
import com.boat.hearables.pro.data.ChipsetSdk
import com.boat.hearables.pro.viewmodel.HearablesUiState
import com.boat.hearables.pro.viewmodel.HearablesViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    uiState: HearablesUiState,
    viewModel: HearablesViewModel
) {
    var showJsonDialog by remember { mutableStateOf(false) }
    var jsonContent by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Studio Preferences & Protocol Engine",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Customize themes, override chipset SDK protocols & manage background DSP",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. UI THEME STUDIO & OLED PITCH BLACK
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("UI Accent Theme & Display", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AccentTheme.entries.forEach { theme ->
                        val selected = uiState.accentTheme == theme
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { viewModel.setAccentTheme(theme) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(theme.primaryHex))
                                    .border(
                                        width = if (selected) 3.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = theme.label.substringBefore(" "),
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("True OLED Pitch-Black Mode", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Uses #040507 pure dark background for AMOLED battery savings", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = uiState.oledPitchBlack,
                        onCheckedChange = { viewModel.setOledPitchBlack(it) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tactile Haptic Feedback on EQ Sliders", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Micro-vibrations when dragging 10-band spline curve", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = uiState.hapticSliders,
                        onCheckedChange = { viewModel.setHapticSliders(it) }
                    )
                }
            }
        }

        // 2. CHIPSET PROTOCOL OVERRIDE
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hardware Chipset Protocol Engine", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Auto-resolved: ${uiState.selectedDevice.sdk.displayName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.chipsetOverride == null,
                        onClick = { viewModel.setChipsetOverride(null) },
                        label = { Text("Auto (${uiState.selectedDevice.sdk.badge})", fontSize = 11.sp) }
                    )
                    ChipsetSdk.entries.forEach { sdk ->
                        FilterChip(
                            selected = uiState.chipsetOverride == sdk,
                            onClick = { viewModel.setChipsetOverride(sdk) },
                            label = { Text(sdk.badge, fontSize = 11.sp) }
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Active Wire Spec: ${uiState.effectiveSdk.displayName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "SPP UUID: ${uiState.effectiveSdk.sppUuid}\nBLE Service: ${uiState.effectiveSdk.bleServiceUuid}\n${uiState.effectiveSdk.protocolSummary}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. PERSISTENT FOREGROUND DSP SERVICE & BATTERY THRESHOLD
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Background DSP & Profile Backup", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Keep 10-band EQ alive across Spotify, YouTube & system audio", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { viewModel.startPersistentDspService() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start Foreground DSP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { viewModel.stopPersistentDspService() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Stop Service", fontSize = 12.sp)
                    }
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Low-Battery Warning Threshold", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "${uiState.lowBatteryAlertPercent}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = uiState.lowBatteryAlertPercent.toFloat(),
                        onValueChange = { viewModel.setLowBatteryAlertPercent(it.roundToInt()) },
                        valueRange = 5f..50f
                    )
                }

                OutlinedButton(
                    onClick = {
                        jsonContent = viewModel.exportProfilesJson()
                        showJsonDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export / Import Custom EQ Profiles (JSON)")
                }
            }
        }

        // 4. REVERSE-ENGINEERED DE-BLOAT & OPTIMIZATION SUMMARY
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("De-Bloated Pro Build vs Official v1.9.0 APK", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF00E676))
                }

                Text(
                    text = "• APK Footprint: Reduced from 138.8 MB to ~4 MB (-97% size; stripped >80 MB of bloated Lottie JSONs & 3.1 MB JioSaavn ad GIFs).\n" +
                        "• Custom 10-Band EQ Unlocked: Removed Constants.j2 device whitelist and non-SDK seekbar lock; works on ALL 95+ boAt devices.\n" +
                        "• 100% Ad & Tracker Free: Removed UniversalAdSlot, Monatisation, JioSaavn/KukuFM/Wynk/Spotify ad widgets, CartActivity store, Instagram promo stories, CleverTap, Mixpanel & Firebase In-App Messaging.\n" +
                        "• Zero Mandatory Login: Removed forced phone OTP / email registration and BoatAI subscription paywalls.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showJsonDialog) {
        AlertDialog(
            onDismissRequest = { showJsonDialog = false },
            title = { Text("Export / Import EQ Profiles JSON", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = jsonContent,
                    onValueChange = { jsonContent = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.importProfilesJson(jsonContent)
                        showJsonDialog = false
                    }
                ) {
                    Text("Apply / Import JSON")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJsonDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
