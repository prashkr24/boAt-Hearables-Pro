package com.boat.hearables.pro.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.NoiseControlOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SpatialAudio
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boat.hearables.pro.data.AncLevel
import com.boat.hearables.pro.data.AncMode
import com.boat.hearables.pro.data.BoatDeviceCatalog
import com.boat.hearables.pro.data.SpatialMode
import com.boat.hearables.pro.protocol.LiveTelemetry
import com.boat.hearables.pro.protocol.PairedBtDevice
import com.boat.hearables.pro.viewmodel.HearablesUiState
import com.boat.hearables.pro.viewmodel.HearablesViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CockpitScreen(
    uiState: HearablesUiState,
    telemetry: LiveTelemetry,
    pairedDevices: List<PairedBtDevice>,
    viewModel: HearablesViewModel,
    onNavigateToEq: () -> Unit
) {
    var showDevicePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HERO DEVICE & CHIPSET TELEMETRY CARD
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = uiState.effectiveSdk.badge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (telemetry.isConnected) Color(0xFF00E676).copy(alpha = 0.18f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (telemetry.isConnected) "LINKED" else "DSP READY",
                                        color = if (telemetry.isConnected) Color(0xFF00E676)
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.selectedDevice.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${uiState.selectedDevice.category.label} • ${telemetry.transportType} • ${telemetry.rssiDbm} dBm",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.refreshBluetoothState() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Bluetooth",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // TRIPLE RADIAL BATTERY GAUGES (Left Bud, Right Bud, Charging Case)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BatteryRingGauge(
                        label = "Left Bud",
                        percent = telemetry.leftBattery,
                        isCharging = telemetry.leftCharging,
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                    BatteryRingGauge(
                        label = "Right Bud",
                        percent = telemetry.rightBattery,
                        isCharging = telemetry.rightCharging,
                        accentColor = MaterialTheme.colorScheme.secondary
                    )
                    BatteryRingGauge(
                        label = "Smart Case",
                        percent = telemetry.caseBattery,
                        isCharging = telemetry.caseCharging,
                        accentColor = Color(0xFF00E676)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDevicePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.BluetoothConnected, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Switch Device", fontSize = 12.sp)
                    }
                    Button(
                        onClick = onNavigateToEq,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("10-Band Custom EQ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. ACTIVE NOISE CANCELLATION & AMBIENT DECK
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
                        Text(
                            text = "Noise Control & Transparency",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.ancMode.subtitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AncMode.entries.forEach { mode ->
                        val selected = uiState.ancMode == mode
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setAncMode(mode) }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = when (mode) {
                                        AncMode.ANC_ON -> Icons.Default.Hearing
                                        AncMode.ADAPTIVE_ANC -> Icons.Default.SurroundSound
                                        AncMode.TRANSPARENCY -> Icons.Default.RecordVoiceOver
                                        AncMode.NORMAL -> Icons.Default.NoiseControlOff
                                    },
                                    contentDescription = mode.label,
                                    tint = if (selected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = mode.label.substringBefore(" "),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (uiState.ancMode == AncMode.ANC_ON || uiState.ancMode == AncMode.TRANSPARENCY) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Cancellation Depth / Transparency Gain",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AncLevel.entries.forEach { level ->
                            FilterChip(
                                selected = uiState.ancLevel == level,
                                onClick = { viewModel.setAncLevel(level) },
                                label = { Text("${level.label} (${level.dbReduction})", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Anti-Wind Noise Reduction", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Suppresses mic turbulence outdoors or on a bike", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = uiState.antiWindNoise,
                        onCheckedChange = { viewModel.setAntiWindNoise(it) }
                    )
                }
            }
        }

        // 3. 360° SPATIAL AUDIO & DOLBY STAGE SELECTOR
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
                        Text(
                            text = "boAt 360° Spatial & Soundstage",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.spatialMode.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.SpatialAudio,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SpatialMode.entries.forEach { mode ->
                        FilterChip(
                            selected = uiState.spatialMode == mode,
                            onClick = { viewModel.setSpatialMode(mode) },
                            label = { Text(mode.label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // 4. QUICK PERFORMANCE MATRIX (BEAST Mode, Multipoint, In-Ear, Smart Talk)
        Text(
            text = "Hardware & Smart Features",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureToggleCard(
                modifier = Modifier.weight(1f),
                title = "BEAST™ Mode",
                subtitle = if (uiState.beastMode) "40ms Gaming Latency" else "Standard A2DP Buffer",
                icon = Icons.Default.Speed,
                checked = uiState.beastMode,
                onCheckedChange = { viewModel.setBeastMode(it) }
            )
            FeatureToggleCard(
                modifier = Modifier.weight(1f),
                title = "Dual Multipoint",
                subtitle = if (uiState.multipointEnabled) "Phone + Laptop Active" else "Single Device Link",
                icon = Icons.Default.Devices,
                checked = uiState.multipointEnabled,
                onCheckedChange = { viewModel.setMultipoint(it) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureToggleCard(
                modifier = Modifier.weight(1f),
                title = "In-Ear Detection",
                subtitle = if (uiState.inEarDetection) "Auto Play/Pause On" else "Optical Sensor Off",
                icon = Icons.Default.Headphones,
                checked = uiState.inEarDetection,
                onCheckedChange = { viewModel.setInEarDetection(it) }
            )
            FeatureToggleCard(
                modifier = Modifier.weight(1f),
                title = "boAt Smart Talk",
                subtitle = if (uiState.smartTalkEnabled) "Auto-Ambient on Voice" else "Manual ANC Control",
                icon = Icons.Default.RecordVoiceOver,
                checked = uiState.smartTalkEnabled,
                onCheckedChange = { viewModel.setSmartTalk(it) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showDevicePicker) {
        DevicePickerDialog(
            pairedDevices = pairedDevices,
            currentDeviceName = uiState.selectedDevice.name,
            onSelectDevice = { name, addr ->
                viewModel.selectDeviceModel(name, addr)
                showDevicePicker = false
            },
            onDismiss = { showDevicePicker = false }
        )
    }
}

@Composable
private fun BatteryRingGauge(
    label: String,
    percent: Int,
    isCharging: Boolean,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(76.dp)) {
            val sweep = (percent.coerceIn(0, 100) / 100f) * 300f
            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            Canvas(modifier = Modifier.size(70.dp)) {
                drawArc(
                    color = trackColor,
                    startAngle = 120f,
                    sweepAngle = 300f,
                    useCenter = false,
                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = accentColor,
                    startAngle = 120f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$percent%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isCharging) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Charging",
                            tint = Color(0xFFFFD600),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeatureToggleCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.clickable { onCheckedChange(!checked) }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (checked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(checked = checked, onCheckedChange = onCheckedChange)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DevicePickerDialog(
    pairedDevices: List<PairedBtDevice>,
    currentDeviceName: String,
    onSelectDevice: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filteredCatalog = remember(query) {
        if (query.isBlank()) BoatDeviceCatalog.DEVICES
        else BoatDeviceCatalog.DEVICES.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.sdk.displayName.contains(query, ignoreCase = true) ||
                it.category.label.contains(query, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select boAt Hearable (95+ Models)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.height(420.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search Airdopes, Nirvana, Rockerz, Stone...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (pairedDevices.isNotEmpty()) {
                        item {
                            Text(
                                text = "PAIRED BLUETOOTH DEVICES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(pairedDevices) { bt ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectDevice(bt.name, bt.address) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(bt.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            "${bt.address} • ${bt.detectedSdk.badge}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (bt.isConnected) {
                                        Text(
                                            "CONNECTED",
                                            color = Color(0xFF00E676),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ALL SUPPORTED BOAT MODELS (${filteredCatalog.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(filteredCatalog) { spec ->
                        val isSelected = spec.name.equals(currentDeviceName, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectDevice(spec.name, null) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(spec.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(
                                        "${spec.category.label} • ${spec.sdk.badge}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${spec.hardwareEqBands}-Band EQ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
