package com.boat.hearables.pro.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boat.hearables.pro.data.BoatDeviceCatalog
import com.boat.hearables.pro.data.RoomAcoustics
import com.boat.hearables.pro.viewmodel.HearablesUiState
import com.boat.hearables.pro.viewmodel.HearablesViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EqualizerScreen(
    uiState: HearablesUiState,
    viewModel: HearablesViewModel
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var customProfileName by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HEADER & HARDWARE + DSP SYNC BADGE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "10-Band Custom Studio EQ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Unlocked for ${uiState.selectedDevice.name} (${uiState.effectiveSdk.badge} + Android DSP)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { viewModel.flattenEq() },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Flat", fontSize = 12.sp)
                }
                Button(
                    onClick = { showSaveDialog = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", fontSize = 12.sp)
                }
            }
        }

        // 2. INTERACTIVE SPLINE FREQUENCY RESPONSE CURVE CANVAS
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SPLINE RESPONSE (TOUCH & DRAG CURVE)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Pre-Amp: ${"%+.1f".format(uiState.preAmpDb)} dB • Q: ${"%.2f".format(uiState.qFactor)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                SplineEqCurveCanvas(
                    gainsDb = uiState.bandGainsDb,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.secondary,
                    onBandDragged = { index, newGain ->
                        if (uiState.hapticSliders) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        viewModel.updateSingleBandGain(index, newGain)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. 10-BAND INDIVIDUAL VERTICAL TOUCH COLUMNS & STEPPERS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BoatDeviceCatalog.EQ_LABELS.forEachIndexed { idx, freqLabel ->
                        val gain = uiState.bandGainsDb.getOrElse(idx) { 0f }
                        VerticalBandColumn(
                            freqLabel = freqLabel,
                            gainDb = gain,
                            accentColor = MaterialTheme.colorScheme.primary,
                            onGainChange = { newDb ->
                                if (uiState.hapticSliders) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.updateSingleBandGain(idx, newDb)
                            }
                        )
                    }
                }
            }
        }

        // 4. USER CUSTOM PROFILES & 14 UNLOCKED PRESETS
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
                    Text("Custom & Unlocked EQ Profiles", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { showSaveDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Custom")
                    }
                }

                if (uiState.customPresets.isNotEmpty()) {
                    Text(
                        text = "YOUR SAVED CUSTOM PROFILES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uiState.customPresets.forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (uiState.activePresetId == preset.id) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { viewModel.selectEqPreset(preset) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                                ) {
                                    Text(
                                        text = preset.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.activePresetId == preset.id) Color.Black
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteCustomPreset(preset.id) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Preset",
                                            tint = if (uiState.activePresetId == preset.id) Color.Black
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "UNLOCKED STUDIO & BOAT HARDWARE CURVES (14)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    BoatDeviceCatalog.BUILT_IN_PRESETS.forEach { preset ->
                        FilterChip(
                            selected = uiState.activePresetId == preset.id,
                            onClick = { viewModel.selectEqPreset(preset) },
                            label = { Text(preset.name, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // 5. MASTERING DSP RACK (BASS ENGINE, PRE-AMP, LIMITER, SPATIAL WIDTH, L/R BALANCE)
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Mastering DSP & Sub-Bass Engine", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Real-time Android DynamicsProcessing + Hardware Sync", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }

                // Pre-Amp Gain
                DspSliderRow(
                    title = "Pre-Amp Input Gain",
                    valueText = "${"%+.1f".format(uiState.preAmpDb)} dB",
                    value = uiState.preAmpDb,
                    valueRange = -12f..12f,
                    onValueChange = { viewModel.updatePreAmp(it) }
                )

                // Parametric Q-Factor
                DspSliderRow(
                    title = "Parametric Bandwidth (Q-Factor)",
                    valueText = "${"%.2f".format(uiState.qFactor)} Q",
                    value = uiState.qFactor,
                    valueRange = 0.5f..4.0f,
                    onValueChange = { viewModel.updateQFactor(it) }
                )

                // Dynamic Bass Engine
                DspSliderRow(
                    title = "Dynamic Sub-Bass Punch",
                    valueText = "${uiState.bassBoostPercent}%",
                    value = uiState.bassBoostPercent.toFloat(),
                    valueRange = 0f..100f,
                    onValueChange = { viewModel.updateBassBoost(it.roundToInt()) }
                )

                // Sub-Bass Crossover Frequency
                DspSliderRow(
                    title = "Sub-Bass Punch Crossover",
                    valueText = "${uiState.subBassCutoffHz} Hz",
                    value = uiState.subBassCutoffHz.toFloat(),
                    valueRange = 40f..120f,
                    onValueChange = { viewModel.updateSubBassCutoff(it.roundToInt()) }
                )

                // 360° Stereo Widener / Virtualizer
                DspSliderRow(
                    title = "360° Soundstage Widener",
                    valueText = "${uiState.virtualizerPercent}%",
                    value = uiState.virtualizerPercent.toFloat(),
                    valueRange = 0f..100f,
                    onValueChange = { viewModel.updateVirtualizer(it.roundToInt()) }
                )

                // Loudness Enhancer
                DspSliderRow(
                    title = "Loudness Enhancer (Quiet Track Boost)",
                    valueText = "+${(uiState.loudnessPercent * 0.12f).roundToInt()} dB (${uiState.loudnessPercent}%)",
                    value = uiState.loudnessPercent.toFloat(),
                    valueRange = 0f..100f,
                    onValueChange = { viewModel.updateLoudness(it.roundToInt()) }
                )

                // Left / Right Channel Balance
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("L / R Driver Balance", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val balLabel = when {
                                uiState.channelBalance < 0 -> "L +${-uiState.channelBalance}%"
                                uiState.channelBalance > 0 -> "R +${uiState.channelBalance}%"
                                else -> "Center (0%)"
                            }
                            Text(balLabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            if (uiState.channelBalance != 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Reset",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.clickable { viewModel.updateChannelBalance(0) }
                                )
                            }
                        }
                    }
                    Slider(
                        value = uiState.channelBalance.toFloat(),
                        onValueChange = { viewModel.updateChannelBalance(it.roundToInt()) },
                        valueRange = -100f..100f
                    )
                }

                // Room Acoustics Environment
                Column {
                    Text("Acoustic Environment Simulation", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RoomAcoustics.entries.forEach { room ->
                            FilterChip(
                                selected = uiState.roomAcoustics == room,
                                onClick = { viewModel.updateRoomAcoustics(room) },
                                label = { Text(room.label, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Anti-Clip Brickwall Limiter Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Brickwall Anti-Clipping Limiter (-0.5 dBFS)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Prevents bass distortion when bands are boosted up to +12 dB", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = uiState.limiterEnabled,
                        onCheckedChange = { viewModel.updateLimiter(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Custom 10-Band EQ Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Give your custom curve a name. You can also bind it to auto-apply whenever ${uiState.selectedDevice.name} connects.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customProfileName,
                        onValueChange = { customProfileName = it },
                        placeholder = { Text("e.g., My Nirvana Bass + Vocal") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCurrentAsCustomPreset(customProfileName)
                        customProfileName = ""
                        showSaveDialog = false
                    }
                ) {
                    Text("Save Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SplineEqCurveCanvas(
    gainsDb: List<Float>,
    primaryColor: Color,
    secondaryColor: Color,
    onBandDragged: (Int, Float) -> Unit
) {
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val zeroLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(185.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF080A0F))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val bandCount = 10
                    val stepX = size.width.toFloat() / bandCount
                    val bandIdx = (offset.x / stepX).toInt().coerceIn(0, bandCount - 1)
                    val normalizedY = 1f - (offset.y / size.height.toFloat()).coerceIn(0f, 1f)
                    val gainDb = (normalizedY * 24f - 12f).coerceIn(-12f, 12f)
                    onBandDragged(bandIdx, (gainDb * 2f).roundToInt() / 2f)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val bandCount = 10
                    val stepX = size.width.toFloat() / bandCount
                    val bandIdx = (change.position.x / stepX).toInt().coerceIn(0, bandCount - 1)
                    val normalizedY = 1f - (change.position.y / size.height.toFloat()).coerceIn(0f, 1f)
                    val gainDb = (normalizedY * 24f - 12f).coerceIn(-12f, 12f)
                    onBandDragged(bandIdx, (gainDb * 2f).roundToInt() / 2f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 14.dp)) {
            val w = size.width
            val h = size.height
            val midY = h / 2f

            // Horizontal dB reference lines (+12, +6, 0, -6, -12 dB)
            listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { frac ->
                val y = h * frac
                drawLine(
                    color = if (frac == 0.5f) zeroLineColor else gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = if (frac == 0.5f) 1.5.dp.toPx() else 1.dp.toPx()
                )
            }

            val points = List(10) { idx ->
                val x = (idx / 9f) * w
                val db = gainsDb.getOrElse(idx) { 0f }.coerceIn(-12f, 12f)
                val y = midY - (db / 12f) * midY
                Offset(x, y)
            }

            // Vertical band grid lines
            points.forEach { pt ->
                drawLine(
                    color = gridColor,
                    start = Offset(pt.x, 0f),
                    end = Offset(pt.x, h),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Smooth cubic spline path through the 10 band points
            val curvePath = Path()
            val fillPath = Path()
            if (points.isNotEmpty()) {
                curvePath.moveTo(points.first().x, points.first().y)
                fillPath.moveTo(points.first().x, h)
                fillPath.lineTo(points.first().x, points.first().y)

                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val ctrlX1 = (p0.x + p1.x) / 2f
                    curvePath.cubicTo(ctrlX1, p0.y, ctrlX1, p1.y, p1.x, p1.y)
                    fillPath.cubicTo(ctrlX1, p0.y, ctrlX1, p1.y, p1.x, p1.y)
                }
                fillPath.lineTo(points.last().x, h)
                fillPath.close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.42f),
                        secondaryColor.copy(alpha = 0.12f),
                        Color.Transparent
                    )
                )
            )

            drawPath(
                path = curvePath,
                brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor)),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw control nodes
            points.forEach { pt ->
                drawCircle(
                    color = Color(0xFF080A0F),
                    radius = 6.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = primaryColor,
                    radius = 4.5.dp.toPx(),
                    center = pt
                )
            }
        }
    }
}

@Composable
private fun VerticalBandColumn(
    freqLabel: String,
    gainDb: Float,
    accentColor: Color,
    onGainChange: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(30.dp)
    ) {
        Text(
            text = "%+.0f".format(gainDb),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (gainDb != 0f) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Vertical drag bar
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(132.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val norm = 1f - (offset.y / size.height.toFloat()).coerceIn(0f, 1f)
                        val db = ((norm * 24f - 12f) * 2f).roundToInt() / 2f
                        onGainChange(db.coerceIn(-12f, 12f))
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val norm = 1f - (change.position.y / size.height.toFloat()).coerceIn(0f, 1f)
                        val db = ((norm * 24f - 12f) * 2f).roundToInt() / 2f
                        onGainChange(db.coerceIn(-12f, 12f))
                    }
                }
        ) {
            val fillFraction = ((gainDb + 12f) / 24f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fillFraction)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(accentColor, accentColor.copy(alpha = 0.45f))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = freqLabel,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DspSliderRow(
    title: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = valueText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}
