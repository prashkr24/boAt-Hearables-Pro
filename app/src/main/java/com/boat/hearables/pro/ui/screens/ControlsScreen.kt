package com.boat.hearables.pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.boat.hearables.pro.data.BoatDeviceCatalog
import com.boat.hearables.pro.data.LuxeLedMode
import com.boat.hearables.pro.data.LuxeSoundMode
import com.boat.hearables.pro.protocol.ProtocolPacketLog
import com.boat.hearables.pro.viewmodel.HearablesUiState
import com.boat.hearables.pro.viewmodel.HearablesViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ControlsScreen(
    uiState: HearablesUiState,
    packetLogs: List<ProtocolPacketLog>,
    viewModel: HearablesViewModel
) {
    var rawHexInput by remember { mutableStateOf("13 04 03 00 1D 02 02") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Luxe Automation & Speaker Lab",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Auto-apply settings on Nirvana Luxe connect, Sleep Timer, Bass Sweep & BES Console",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. AUTO-APPLY ON BOAT NIRVANA LUXE CONNECT
        val profile = uiState.autoProfile
        val allPresets = BoatDeviceCatalog.BUILT_IN_PRESETS + uiState.customPresets
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
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Auto-Apply When Nirvana Luxe Connects",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Strictly triggers ONLY when 'Nirvana Luxe' connects via Bluetooth",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = profile.enabled,
                        onCheckedChange = {
                            viewModel.saveAutoProfile(profile.copy(enabled = it))
                        }
                    )
                }

                Text(
                    text = "Startup EQ Profile:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    allPresets.forEach { preset ->
                        FilterChip(
                            selected = profile.eqPresetId == preset.id,
                            onClick = {
                                viewModel.saveAutoProfile(profile.copy(eqPresetId = preset.id))
                            },
                            label = { Text(preset.name, fontSize = 11.sp) }
                        )
                    }
                }

                Text(
                    text = "Startup Sound Mode:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LuxeSoundMode.entries.forEach { mode ->
                        FilterChip(
                            selected = profile.soundMode == mode,
                            onClick = {
                                viewModel.saveAutoProfile(profile.copy(soundMode = mode))
                            },
                            label = { Text(mode.label, fontSize = 11.sp) }
                        )
                    }
                }

                Text(
                    text = "Startup LED Light Show:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LuxeLedMode.entries.forEach { led ->
                        FilterChip(
                            selected = profile.ledMode == led,
                            onClick = {
                                viewModel.saveAutoProfile(profile.copy(ledMode = led))
                            },
                            label = { Text(led.label, fontSize = 11.sp) }
                        )
                    }
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Default Speaker Volume on Connect", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "${profile.targetVolumePercent}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = profile.targetVolumePercent.toFloat(),
                        onValueChange = {
                            viewModel.saveAutoProfile(profile.copy(targetVolumePercent = it.roundToInt()))
                        },
                        valueRange = 10f..100f
                    )
                }
            }
        }

        // 2. SPEAKER SLEEP TIMER (AUTO VOLUME FADE & LED SHUTOFF)
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nirvana Luxe Sleep Timer",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.sleepTimerMinutesRemaining > 0)
                                "Active: Auto-muting & turning off LEDs in ${uiState.sleepTimerMinutesRemaining} min"
                            else "Automatically turns off LEDs & mutes speaker when timer ends",
                            fontSize = 12.sp,
                            color = if (uiState.sleepTimerMinutesRemaining > 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "Off", 15 to "15 min", 30 to "30 min", 45 to "45 min", 60 to "60 min", 90 to "90 min").forEach { (mins, label) ->
                        FilterChip(
                            selected = uiState.sleepTimerMinutesRemaining == mins,
                            onClick = { viewModel.setSleepTimerMinutes(mins) },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // 3. WOOFER, PASSIVE RADIATOR & TWEETER FREQUENCY GENERATOR
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Speaker Woofer & Passive Radiator Test", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Test Nirvana Luxe sub-bass excursion (45–120Hz) & tweeter clarity",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Test Frequency", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "${uiState.testFrequencyHz.roundToInt()} Hz",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = uiState.testFrequencyHz,
                    onValueChange = { viewModel.updateTestFrequency(it) },
                    valueRange = 35f..15000f
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        45f to "45Hz Sub",
                        60f to "60Hz Radiator",
                        120f to "120Hz Punch",
                        250f to "250Hz Warmth",
                        750f to "750Hz Vocal",
                        2000f to "2kHz Presence",
                        5000f to "5kHz Detail",
                        15000f to "15kHz Air"
                    ).forEach { (hz, label) ->
                        FilterChip(
                            selected = uiState.testFrequencyHz.roundToInt() == hz.roundToInt(),
                            onClick = { viewModel.updateTestFrequency(hz) },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val isPlaying = uiState.activeAcousticTest == "SPEAKER_TONE"
                Button(
                    onClick = { viewModel.triggerSpeakerToneTest() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) Color(0xFFFF1744)
                        else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlaying) "Stop Speaker Test Tone"
                        else "Play ${uiState.testFrequencyHz.roundToInt()} Hz Reference Tone on Nirvana Luxe",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4. LIVE BES 0x0413 (`13 04`) HEX PROTOCOL CONSOLE
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Nirvana Luxe BES Protocol Console", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Live BES 0x0413 (13 04 ...) SPP/GATT Frames", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = { viewModel.clearProtocolLogs() }) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear Logs")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = rawHexInput,
                        onValueChange = { rawHexInput = it },
                        label = { Text("Send Custom BES Frame (13 04 ...)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { viewModel.sendRawHexPacket(rawHexInput) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send Hex", modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF07090D))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (packetLogs.isEmpty()) {
                        Text(
                            text = "No packets logged yet — change a 7-band EQ slider, switch Sound Mode, or change LED Light Show to inspect the live BES 13 04 frames.",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        packetLogs.take(15).forEach { log ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "[${log.timestamp}] ${log.direction} • BES-LUXE",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (log.direction == "TX") MaterialTheme.colorScheme.primary
                                        else Color(0xFF00E676)
                                    )
                                    Text(
                                        text = log.commandTitle,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = log.hexPayload,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFD5E3FF)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
