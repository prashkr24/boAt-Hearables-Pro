package com.boat.hearables.pro.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
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
import com.boat.hearables.pro.data.RoomPlacement
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
        // 1. HEADER & BES 0x13 HARDWARE EQ SYNC
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Nirvana Luxe Custom EQ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "7-Band BES Hardware EQ (60Hz–15kHz • Opcode 0x13) + Speaker DSP",
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

        // 2. INTERACTIVE 7-BAND SPLINE FREQUENCY RESPONSE GRAPH + SLIDERS
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BES 7-BAND RESPONSE (DRAG CURVE OR BARS)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (uiState.hardwareEqEnabled) "EQ ON" else "BYPASS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.hardwareEqEnabled) Color(0xFF00E676)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = uiState.hardwareEqEnabled,
                            onCheckedChange = { viewModel.setHardwareEqEnabled(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                SplineEqCurveCanvas7Band(
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

                Spacer(modifier = Modifier.height(14.dp))

                // 7 NATIVE BES BANDS: 60Hz, 120Hz, 250Hz, 750Hz, 2kHz, 5kHz, 15kHz
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    BoatDeviceCatalog.LUXE_EQ_LABELS.forEachIndexed { idx, freqLabel ->
                        val gain = uiState.bandGainsDb.getOrElse(idx) { 0f }
                        VerticalBandColumn7(
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

        // 3. NIRVANA LUXE SOUND PROFILES & USER CUSTOM PRESETS
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
                    Text("Nirvana Luxe Sound Profiles", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { showSaveDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Custom")
                    }
                }

                if (uiState.customPresets.isNotEmpty()) {
                    Text(
                        text = "YOUR CUSTOM NIRVANA LUXE PROFILES",
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
                    text = "SPEAKER-TUNED & STOCK BES PROFILES (12)",
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

        // 4. ROOM PLACEMENT ACOUSTIC COMPENSATION
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
                        Text("Speaker Room Placement Calibration", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = uiState.roomPlacement.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoomPlacement.entries.forEach { place ->
                        FilterChip(
                            selected = uiState.roomPlacement == place,
                            onClick = { viewModel.setRoomPlacement(place) },
                            label = { Text(place.label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // 5. NIRVANA LUXE PASSIVE RADIATOR & MASTERING DSP RACK
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
                        Text("Speaker Bass & Acoustic Mastering", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Fine-tune passive radiator punch, 360° dispersion & woofer protection", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }

                DspSliderRow(
                    title = "Passive Radiator Sub-Bass Punch",
                    valueText = "${uiState.bassBoostPercent}%",
                    value = uiState.bassBoostPercent.toFloat(),
                    valueRange = 0f..100f,
                    onValueChange = { viewModel.updateBassBoost(it.roundToInt()) }
                )

                DspSliderRow(
                    title = "Sub-Bass Crossover Frequency",
                    valueText = "${uiState.subBassCutoffHz} Hz",
                    value = uiState.subBassCutoffHz.toFloat(),
                    valueRange = 45f..120f,
                    onValueChange = { viewModel.updateSubBassCutoff(it.roundToInt()) }
                )

                DspSliderRow(
                    title = "360° Room Soundstage Widener",
                    valueText = "${uiState.virtualizerPercent}%",
                    value = uiState.virtualizerPercent.toFloat(),
                    valueRange = 0f..100f,
                    onValueChange = { viewModel.updateVirtualizer(it.roundToInt()) }
                )

                DspSliderRow(
                    title = "Party Volume & Loudness Enhancer",
                    valueText = "+${(uiState.loudnessPercent * 0.12f).roundToInt()} dB (${uiState.loudnessPercent}%)",
                    value = uiState.loudnessPercent.toFloat(),
                    valueRange = 0f..100f,
                    onValueChange = { viewModel.updateLoudness(it.roundToInt()) }
                )

                DspSliderRow(
                    title = "Pre-Amp Headroom Gain",
                    valueText = "${"%+.1f".format(uiState.preAmpDb)} dB",
                    value = uiState.preAmpDb,
                    valueRange = -12f..12f,
                    onValueChange = { viewModel.updatePreAmp(it) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Woofer Excursion & Anti-Clipping Limiter", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Protects Nirvana Luxe speaker driver from distortion at 90–100% volume", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            title = { Text("Save Custom Nirvana Luxe EQ Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Save your custom 7-band curve (60Hz–15kHz). You can also set it to auto-apply whenever boAt Nirvana Luxe connects.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customProfileName,
                        onValueChange = { customProfileName = it },
                        placeholder = { Text("e.g., Living Room Deep Bass") },
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
private fun SplineEqCurveCanvas7Band(
    gainsDb: List<Float>,
    primaryColor: Color,
    secondaryColor: Color,
    onBandDragged: (Int, Float) -> Unit
) {
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val zeroLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    val bandCount = 7

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(185.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF080A0F))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
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
                    val stepX = size.width.toFloat() / bandCount
                    val bandIdx = (change.position.x / stepX).toInt().coerceIn(0, bandCount - 1)
                    val normalizedY = 1f - (change.position.y / size.height.toFloat()).coerceIn(0f, 1f)
                    val gainDb = (normalizedY * 24f - 12f).coerceIn(-12f, 12f)
                    onBandDragged(bandIdx, (gainDb * 2f).roundToInt() / 2f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp)) {
            val w = size.width
            val h = size.height
            val midY = h / 2f

            listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { frac ->
                val y = h * frac
                drawLine(
                    color = if (frac == 0.5f) zeroLineColor else gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = if (frac == 0.5f) 1.5.dp.toPx() else 1.dp.toPx()
                )
            }

            val points = List(bandCount) { idx ->
                val x = (idx / (bandCount - 1f)) * w
                val db = gainsDb.getOrElse(idx) { 0f }.coerceIn(-12f, 12f)
                val y = midY - (db / 12f) * midY
                Offset(x, y)
            }

            points.forEach { pt ->
                drawLine(
                    color = gridColor,
                    start = Offset(pt.x, 0f),
                    end = Offset(pt.x, h),
                    strokeWidth = 1.dp.toPx()
                )
            }

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

            points.forEach { pt ->
                drawCircle(
                    color = Color(0xFF080A0F),
                    radius = 6.5.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = primaryColor,
                    radius = 5.dp.toPx(),
                    center = pt
                )
            }
        }
    }
}

@Composable
private fun VerticalBandColumn7(
    freqLabel: String,
    gainDb: Float,
    accentColor: Color,
    onGainChange: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(42.dp)
    ) {
        Text(
            text = "%+.1f".format(gainDb),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (gainDb != 0f) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .width(24.dp)
                .height(138.dp)
                .clip(RoundedCornerShape(12.dp))
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

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = freqLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
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
