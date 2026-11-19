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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import com.boat.hearables.pro.audio.OutputChannel
import com.boat.hearables.pro.protocol.LiveTelemetry
import com.boat.hearables.pro.protocol.ProtocolPacketLog
import com.boat.hearables.pro.viewmodel.HearablesUiState
import com.boat.hearables.pro.viewmodel.HearablesViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiagnosticsScreen(
    uiState: HearablesUiState,
    telemetry: LiveTelemetry,
    packetLogs: List<ProtocolPacketLog>,
    viewModel: HearablesViewModel
) {
    var rawHexInput by remember { mutableStateOf("08 EE 00 00 00 02 07 0B 00 01 0B") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Acoustic Lab & Find My Buds",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Chirp locator, Ear-Tip Seal Test, 20Hz-20kHz Sweep & Hex Protocol Console",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (uiState.activeAcousticTest != null) {
                Button(
                    onClick = { viewModel.stopAcousticTest() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744))
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stop Tone", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 1. FIND MY EARBUDS (LEFT / RIGHT INDEPENDENT ACOUSTIC CHIRP + RSSI RADAR)
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
                    Column {
                        Text("Find My Earbuds (High-SPL Chirp + Radar)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Emits a 3.1kHz/4.1kHz acoustic beacon + hardware opcode 0x2A",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.LocationSearching,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // RSSI Signal Proximity Bar
                val proximityNorm = ((telemetry.rssiDbm + 100).coerceIn(0, 65) / 65f)
                val proximityZone = when {
                    telemetry.rssiDbm >= -55 -> "VERY CLOSE (< 2m)"
                    telemetry.rssiDbm >= -72 -> "NEARBY (2m – 6m)"
                    else -> "WEAK SIGNAL (> 6m)"
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Bluetooth Signal Radar: ${telemetry.rssiDbm} dBm",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = proximityZone,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { proximityNorm },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        OutputChannel.LEFT_ONLY to "Ring Left",
                        OutputChannel.RIGHT_ONLY to "Ring Right",
                        OutputChannel.BOTH_STEREO to "Ring Both"
                    ).forEach { (ch, title) ->
                        val isRinging = uiState.activeAcousticTest == "FIND_${ch.name}"
                        Button(
                            onClick = { viewModel.triggerFindMyBuds(ch) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRinging) Color(0xFFFF1744)
                                else MaterialTheme.colorScheme.primaryContainer,
                                contentColor = if (isRinging) Color.White
                                else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = if (isRinging) Icons.Default.Stop else Icons.Default.Campaign,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isRinging) "Stop" else title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. EAR-TIP ACOUSTIC SEAL & SUB-BASS FIT TEST
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
                        Text("Ear-Tip Acoustic Seal & Fit Test", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Compares 55 Hz Sub-Bass vs 500 Hz Reference to detect bass leakage",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.sealTestStatus,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.runEarTipSealTest() },
                    enabled = uiState.activeAcousticTest != "SEAL_TEST",
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.activeAcousticTest == "SEAL_TEST") "Running Sub-Bass Seal Comparison..."
                        else "Run 4-Second Ear-Tip Seal Test",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. STEREO L/R PHASE CHECK & 20 Hz - 20 kHz DRIVER SWEEP GENERATOR
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
                    Column {
                        Text("20 Hz – 20 kHz Driver & Stereo Phase Generator", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Test sub-bass extension, driver rattle, L/R wiring & polarity", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Generator Frequency", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                    valueRange = 20f..16000f
                )

                // Quick Reference Frequencies
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        30f to "30Hz Sub",
                        60f to "60Hz Punch",
                        125f to "125Hz Bass",
                        440f to "440Hz A4",
                        1000f to "1kHz Ref",
                        4000f to "4kHz Vocal",
                        8000f to "8kHz Treble",
                        14000f to "14kHz Air"
                    ).forEach { (hz, label) ->
                        FilterChip(
                            selected = uiState.testFrequencyHz.roundToInt() == hz.roundToInt(),
                            onClick = { viewModel.updateTestFrequency(hz) },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutputChannel.entries.forEach { ch ->
                        val active = uiState.activeAcousticTest == "TONE_${ch.name}"
                        OutlinedButton(
                            onClick = { viewModel.triggerStereoOrToneTest(ch) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (active) MaterialTheme.colorScheme.primary
                                else Color.Transparent,
                                contentColor = if (active) Color.Black
                                else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = ch.label.substringBefore(" "),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 4. LIVE MULTI-CHIPSET PROTOCOL INSPECTOR & RAW HEX SENDER
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
                            Text("Live Protocol Packet Console", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Active Encoder: ${uiState.effectiveSdk.displayName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        label = { Text("Send Custom Raw Hex Frame (SPP / GATT)") },
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
                            text = "No packets logged yet — change an EQ band, toggle ANC, or press Send above to inspect raw chipset frames.",
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
                                        text = "[${log.timestamp}] ${log.direction} • ${log.sdkBadge}",
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
