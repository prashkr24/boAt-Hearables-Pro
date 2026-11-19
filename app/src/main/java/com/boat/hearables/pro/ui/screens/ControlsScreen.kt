package com.boat.hearables.pro.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boat.hearables.pro.data.AncMode
import com.boat.hearables.pro.data.BoatDeviceCatalog
import com.boat.hearables.pro.data.TouchAction
import com.boat.hearables.pro.data.TouchGesture
import com.boat.hearables.pro.viewmodel.HearablesUiState
import com.boat.hearables.pro.viewmodel.HearablesViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ControlsScreen(
    uiState: HearablesUiState,
    viewModel: HearablesViewModel
) {
    var editingGesture by remember { mutableStateOf<TouchGesture?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Touch Remapper & Smart Automation",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Customize Left/Right taps & auto-apply profile when ${uiState.selectedDevice.name} connects",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. BED MODE / ACCIDENTAL TOUCH LOCK
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.bedTouchLock) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Bed Mode (Accidental Pillow Touch Lock)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Disables Single & Double taps so lying on a pillow never pauses music or skips tracks",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = uiState.bedTouchLock,
                    onCheckedChange = { viewModel.setBedTouchLock(it) }
                )
            }
        }

        // 2. LEFT & RIGHT EARBUD TOUCH GESTURE MATRIX
        listOf("Left Bud", "Right Bud").forEach { side ->
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
                        Text(
                            text = "$side Touch Controls",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TouchGesture.entries.filter { it.side == side }.forEach { gesture ->
                        val assignedAction = uiState.touchMappings[gesture] ?: TouchAction.PLAY_PAUSE
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { editingGesture = gesture }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = gesture.gestureName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tap to reassign hardware opcode (0x22 / 08EE)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = assignedAction.label,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. PER-DEVICE SMART AUTO-CONNECT PROFILE
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
                                text = "Auto-Profile on Connect (${uiState.selectedDevice.name})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Automatically apply EQ, ANC & volume when Bluetooth connects",
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
                    text = "Auto-Apply EQ Preset:",
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
                    text = "Auto-Apply Noise Control:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AncMode.entries.forEach { mode ->
                        FilterChip(
                            selected = profile.ancMode == mode,
                            onClick = {
                                viewModel.saveAutoProfile(profile.copy(ancMode = mode))
                            },
                            label = { Text(mode.label, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto-Enable BEAST™ Gaming Mode", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = profile.beastMode,
                        onCheckedChange = {
                            viewModel.saveAutoProfile(profile.copy(beastMode = it))
                        }
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Safe Startup Media Volume", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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

        Spacer(modifier = Modifier.height(12.dp))
    }

    editingGesture?.let { gesture ->
        AlertDialog(
            onDismissRequest = { editingGesture = null },
            title = {
                Text("${gesture.side} • ${gesture.gestureName}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TouchAction.entries.forEach { action ->
                        val isCurrent = uiState.touchMappings[gesture] == action
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateTouchMapping(gesture, action)
                                    editingGesture = null
                                }
                        ) {
                            Text(
                                text = action.label,
                                fontSize = 14.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { editingGesture = null }) {
                    Text("Close")
                }
            }
        )
    }
}
