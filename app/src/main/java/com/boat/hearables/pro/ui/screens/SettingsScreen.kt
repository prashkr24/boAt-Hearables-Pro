package com.boat.hearables.pro.ui.screens

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.boat.hearables.pro.data.BoatDeviceCatalog
import com.boat.hearables.pro.viewmodel.HearablesUiState
import com.boat.hearables.pro.viewmodel.HearablesViewModel

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
                text = "Nirvana Luxe Studio Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Customize themes, background DSP service & export your Nirvana Luxe EQ profiles",
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
                    Text("Luxe Accent Theme & Display", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                        Text("Micro-vibrations when dragging the 7-band spline curve", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = uiState.hapticSliders,
                        onCheckedChange = { viewModel.setHapticSliders(it) }
                    )
                }
            }
        }

        // 2. PERSISTENT FOREGROUND DSP SERVICE & JSON PROFILE BACKUP
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Background Speaker DSP & Profile Backup", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Keep 7-band custom EQ & Room Calibration active across all music apps", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("Export / Import Nirvana Luxe EQ Profiles (JSON)")
                }
            }
        }

        // 3. NIRVANA LUXE HARDWARE PROTOCOL ARCHITECTURE
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
                    Text("boAt Nirvana Luxe BES Hardware Spec", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Target Device: ${BoatDeviceCatalog.TARGET_DEVICE_NAME} (Strict Filter)\n" +
                            "RFCOMM SPP: ${BoatDeviceCatalog.BES_SPP_UUID}\n" +
                            "BLE Service: ${BoatDeviceCatalog.BES_BLE_SERVICE_UUID}\n" +
                            "BES Header: 13 04 [len_lo] [len_hi] [opcode] [payload]\n" +
                            "• 0x13 (19): 7-Band Custom EQ (60, 120, 250, 750, 2k, 5k, 15k Hz)\n" +
                            "• 0x1D (29): Sound Mode (00=Signature, 02=360° Spatial, 01=Cinema)\n" +
                            "• 0x1C (28): LED Light Show (01..04=Modes 1..4, 05=LED Off)\n" +
                            "• 0x85 (-123): Broadcast PartySync Multi-Speaker Link\n" +
                            "• 0x16 (22): Dual Multipoint Connection\n" +
                            "• 0x80 (-128): Speaker Battery Telemetry",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // 4. DE-BLOATED BUILD SUMMARY
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
                    Text("Dedicated Nirvana Luxe Build vs Stock APK", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF00E676))
                }

                Text(
                    text = "• Strictly detects and connects ONLY to your boAt Nirvana Luxe speaker (ignores all other Bluetooth devices).\n" +
                        "• Unlocks custom 7-band BES hardware EQ (opcode 0x13) + Room Placement calibration which the stock v1.9.0 app hid from the UI.\n" +
                        "• ~2.6 MB footprint vs 138.8 MB stock APK (-98% size), zero ads, zero trackers, zero mandatory phone OTP login.",
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
            title = { Text("Export / Import Nirvana Luxe EQ JSON", fontWeight = FontWeight.Bold) },
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
