package com.boat.hearables.pro.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.boat.hearables.pro.audio.AcousticTestGenerator
import com.boat.hearables.pro.audio.DspAudioController
import com.boat.hearables.pro.audio.DspAudioService
import com.boat.hearables.pro.audio.OutputChannel
import com.boat.hearables.pro.data.AccentTheme
import com.boat.hearables.pro.data.AncLevel
import com.boat.hearables.pro.data.AncMode
import com.boat.hearables.pro.data.AppPreferences
import com.boat.hearables.pro.data.BoatDeviceCatalog
import com.boat.hearables.pro.data.BoatDeviceSpec
import com.boat.hearables.pro.data.ChipsetSdk
import com.boat.hearables.pro.data.DeviceAutoProfile
import com.boat.hearables.pro.data.EqPreset
import com.boat.hearables.pro.data.RoomAcoustics
import com.boat.hearables.pro.data.SpatialMode
import com.boat.hearables.pro.data.TouchAction
import com.boat.hearables.pro.data.TouchGesture
import com.boat.hearables.pro.protocol.HearableProtocolEngine
import com.boat.hearables.pro.protocol.LiveTelemetry
import com.boat.hearables.pro.protocol.PairedBtDevice
import com.boat.hearables.pro.protocol.ProtocolPacketLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class HearablesUiState(
    val selectedDevice: BoatDeviceSpec = BoatDeviceCatalog.DEVICES.first(),
    val chipsetOverride: ChipsetSdk? = null,
    val effectiveSdk: ChipsetSdk = BoatDeviceCatalog.DEVICES.first().sdk,
    val activePresetId: String = "boat_signature",
    val bandGainsDb: List<Float> = BoatDeviceCatalog.BUILT_IN_PRESETS.first().gainsDb,
    val customPresets: List<EqPreset> = emptyList(),
    val preAmpDb: Float = 0f,
    val qFactor: Float = 1.41f,
    val limiterEnabled: Boolean = true,
    val bassBoostPercent: Int = 25,
    val subBassCutoffHz: Int = 65,
    val virtualizerPercent: Int = 20,
    val loudnessPercent: Int = 0,
    val channelBalance: Int = 0,
    val roomAcoustics: RoomAcoustics = RoomAcoustics.OFF,
    val ancMode: AncMode = AncMode.ANC_ON,
    val ancLevel: AncLevel = AncLevel.HIGH,
    val antiWindNoise: Boolean = true,
    val beastMode: Boolean = false,
    val spatialMode: SpatialMode = SpatialMode.OFF,
    val inEarDetection: Boolean = true,
    val multipointEnabled: Boolean = true,
    val smartTalkEnabled: Boolean = false,
    val bedTouchLock: Boolean = false,
    val touchMappings: Map<TouchGesture, TouchAction> = emptyMap(),
    val autoProfile: DeviceAutoProfile = DeviceAutoProfile("Nirvana Ivy Pro"),
    val accentTheme: AccentTheme = AccentTheme.BOAT_CRIMSON,
    val oledPitchBlack: Boolean = true,
    val hapticSliders: Boolean = true,
    val lowBatteryAlertPercent: Int = 20,
    val activeAcousticTest: String? = null,
    val testFrequencyHz: Float = 440f,
    val sealTestStatus: String = "Ready — put both earbuds in and press Run Seal Test"
)

class HearablesViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val prefs = AppPreferences(appContext)
    val protocolEngine = HearableProtocolEngine(appContext)
    private val acousticGenerator = AcousticTestGenerator()

    private val _uiState = MutableStateFlow(loadInitialState())
    val uiState: StateFlow<HearablesUiState> = _uiState.asStateFlow()

    val pairedDevices: StateFlow<List<PairedBtDevice>> = protocolEngine.pairedDevices
    val telemetry: StateFlow<LiveTelemetry> = protocolEngine.telemetry
    val packetLogs: StateFlow<List<ProtocolPacketLog>> = protocolEngine.packetLogs

    init {
        DspAudioController.applyFromPreferences(appContext)
        refreshBluetoothState()
    }

    private fun loadInitialState(): HearablesUiState {
        val spec = BoatDeviceCatalog.resolveDeviceSpec(prefs.selectedDeviceName)
        val overrideSdk = prefs.chipsetOverride
        return HearablesUiState(
            selectedDevice = spec,
            chipsetOverride = overrideSdk,
            effectiveSdk = overrideSdk ?: spec.sdk,
            activePresetId = prefs.activePresetId,
            bandGainsDb = prefs.currentBandGains,
            customPresets = prefs.getCustomPresets(),
            preAmpDb = prefs.preAmpDb,
            qFactor = prefs.qFactor,
            limiterEnabled = prefs.limiterEnabled,
            bassBoostPercent = prefs.bassBoostPercent,
            subBassCutoffHz = prefs.subBassCutoffHz,
            virtualizerPercent = prefs.virtualizerPercent,
            loudnessPercent = prefs.loudnessPercent,
            channelBalance = prefs.channelBalance,
            roomAcoustics = prefs.roomAcoustics,
            ancMode = prefs.ancMode,
            ancLevel = prefs.ancLevel,
            antiWindNoise = prefs.antiWindNoise,
            beastMode = prefs.beastMode,
            spatialMode = prefs.spatialMode,
            inEarDetection = prefs.inEarDetection,
            multipointEnabled = prefs.multipointEnabled,
            smartTalkEnabled = prefs.smartTalkEnabled,
            bedTouchLock = prefs.bedTouchLock,
            touchMappings = prefs.getTouchMappings(),
            autoProfile = prefs.getAutoProfile(spec.name),
            accentTheme = prefs.accentTheme,
            oledPitchBlack = prefs.oledPitchBlack,
            hapticSliders = prefs.hapticSliders,
            lowBatteryAlertPercent = prefs.lowBatteryAlertPercent
        )
    }

    fun refreshBluetoothState() {
        val bonded = protocolEngine.refreshPairedDevices()
        val connectedBt = bonded.firstOrNull { it.isConnected }
        if (connectedBt != null) {
            selectDeviceModel(connectedBt.name, connectedBt.address)
        }
    }

    fun selectDeviceModel(deviceName: String, btAddress: String? = null) {
        prefs.selectedDeviceName = deviceName
        val spec = BoatDeviceCatalog.resolveDeviceSpec(deviceName)
        val sdk = _uiState.value.chipsetOverride ?: spec.sdk
        _uiState.value = _uiState.value.copy(
            selectedDevice = spec,
            effectiveSdk = sdk,
            autoProfile = prefs.getAutoProfile(spec.name)
        )
        if (btAddress != null) {
            protocolEngine.connectToDevice(btAddress, sdk)
        }
    }

    fun setChipsetOverride(sdk: ChipsetSdk?) {
        prefs.chipsetOverride = sdk
        val effective = sdk ?: _uiState.value.selectedDevice.sdk
        _uiState.value = _uiState.value.copy(
            chipsetOverride = sdk,
            effectiveSdk = effective
        )
    }

    // =========================================================================
    // 10-BAND CUSTOM EQUALIZER & DSP ACTIONS
    // =========================================================================

    fun updateSingleBandGain(bandIndex: Int, gainDb: Float) {
        val clamped = gainDb.coerceIn(-12f, 12f)
        val updated = _uiState.value.bandGainsDb.toMutableList().apply {
            if (bandIndex in indices) this[bandIndex] = clamped
        }
        prefs.currentBandGains = updated
        prefs.activePresetId = "custom_live"
        _uiState.value = _uiState.value.copy(
            bandGainsDb = updated,
            activePresetId = "custom_live"
        )
        syncDspAndHardwareEq(updated)
    }

    fun selectEqPreset(preset: EqPreset) {
        prefs.activePresetId = preset.id
        prefs.currentBandGains = preset.gainsDb
        _uiState.value = _uiState.value.copy(
            activePresetId = preset.id,
            bandGainsDb = preset.gainsDb
        )
        syncDspAndHardwareEq(preset.gainsDb)
    }

    fun flattenEq() {
        val flat = List(10) { 0f }
        prefs.activePresetId = "flat"
        prefs.currentBandGains = flat
        _uiState.value = _uiState.value.copy(
            activePresetId = "flat",
            bandGainsDb = flat
        )
        syncDspAndHardwareEq(flat)
    }

    fun saveCurrentAsCustomPreset(name: String, subtitle: String = "Custom 10-Band Profile") {
        val cleanName = name.trim().ifEmpty { "My Custom EQ ${_uiState.value.customPresets.size + 1}" }
        val newPreset = EqPreset(
            id = "custom_${UUID.randomUUID().toString().take(8)}",
            name = cleanName,
            subtitle = subtitle,
            gainsDb = _uiState.value.bandGainsDb,
            isCustom = true
        )
        val updatedList = _uiState.value.customPresets + newPreset
        prefs.saveCustomPresets(updatedList)
        prefs.activePresetId = newPreset.id
        _uiState.value = _uiState.value.copy(
            customPresets = updatedList,
            activePresetId = newPreset.id
        )
    }

    fun deleteCustomPreset(presetId: String) {
        val updated = _uiState.value.customPresets.filterNot { it.id == presetId }
        prefs.saveCustomPresets(updated)
        _uiState.value = _uiState.value.copy(customPresets = updated)
    }

    fun updatePreAmp(db: Float) {
        prefs.preAmpDb = db
        _uiState.value = _uiState.value.copy(preAmpDb = db)
        DspAudioController.applyFromPreferences(appContext)
    }

    fun updateQFactor(q: Float) {
        prefs.qFactor = q
        _uiState.value = _uiState.value.copy(qFactor = q)
        DspAudioController.applyFromPreferences(appContext)
    }

    fun updateLimiter(enabled: Boolean) {
        prefs.limiterEnabled = enabled
        _uiState.value = _uiState.value.copy(limiterEnabled = enabled)
        DspAudioController.applyFromPreferences(appContext)
    }

    fun updateBassBoost(percent: Int) {
        prefs.bassBoostPercent = percent
        _uiState.value = _uiState.value.copy(bassBoostPercent = percent)
        DspAudioController.applyFromPreferences(appContext)
        protocolEngine.sendBassEngine(_uiState.value.effectiveSdk, percent > 0, percent)
    }

    fun updateSubBassCutoff(hz: Int) {
        prefs.subBassCutoffHz = hz
        _uiState.value = _uiState.value.copy(subBassCutoffHz = hz)
        DspAudioController.applyFromPreferences(appContext)
    }

    fun updateVirtualizer(percent: Int) {
        prefs.virtualizerPercent = percent
        _uiState.value = _uiState.value.copy(virtualizerPercent = percent)
        DspAudioController.applyFromPreferences(appContext)
    }

    fun updateLoudness(percent: Int) {
        prefs.loudnessPercent = percent
        _uiState.value = _uiState.value.copy(loudnessPercent = percent)
        DspAudioController.applyFromPreferences(appContext)
    }

    fun updateChannelBalance(balance: Int) {
        prefs.channelBalance = balance
        _uiState.value = _uiState.value.copy(channelBalance = balance)
    }

    fun updateRoomAcoustics(room: RoomAcoustics) {
        prefs.roomAcoustics = room
        _uiState.value = _uiState.value.copy(roomAcoustics = room)
    }

    private fun syncDspAndHardwareEq(gainsDb: List<Float>) {
        DspAudioController.applyFromPreferences(appContext)
        protocolEngine.sendCustomEq(_uiState.value.effectiveSdk, gainsDb)
    }

    // =========================================================================
    // HARDWARE COCKPIT CONTROLS (ANC, BEAST MODE, SPATIAL, MULTIPOINT, IN-EAR)
    // =========================================================================

    fun setAncMode(mode: AncMode) {
        prefs.ancMode = mode
        _uiState.value = _uiState.value.copy(ancMode = mode)
        protocolEngine.sendAncControl(
            _uiState.value.effectiveSdk,
            mode,
            _uiState.value.ancLevel,
            _uiState.value.antiWindNoise
        )
    }

    fun setAncLevel(level: AncLevel) {
        prefs.ancLevel = level
        _uiState.value = _uiState.value.copy(ancLevel = level)
        protocolEngine.sendAncControl(
            _uiState.value.effectiveSdk,
            _uiState.value.ancMode,
            level,
            _uiState.value.antiWindNoise
        )
    }

    fun setAntiWindNoise(enabled: Boolean) {
        prefs.antiWindNoise = enabled
        _uiState.value = _uiState.value.copy(antiWindNoise = enabled)
        protocolEngine.sendAncControl(
            _uiState.value.effectiveSdk,
            _uiState.value.ancMode,
            _uiState.value.ancLevel,
            enabled
        )
    }

    fun setBeastMode(enabled: Boolean) {
        prefs.beastMode = enabled
        _uiState.value = _uiState.value.copy(beastMode = enabled)
        protocolEngine.sendBeastMode(_uiState.value.effectiveSdk, enabled)
    }

    fun setSpatialMode(mode: SpatialMode) {
        prefs.spatialMode = mode
        _uiState.value = _uiState.value.copy(spatialMode = mode)
        protocolEngine.sendSpatialMode(_uiState.value.effectiveSdk, mode)
    }

    fun setInEarDetection(enabled: Boolean) {
        prefs.inEarDetection = enabled
        _uiState.value = _uiState.value.copy(inEarDetection = enabled)
        protocolEngine.sendInEarDetection(_uiState.value.effectiveSdk, enabled)
    }

    fun setMultipoint(enabled: Boolean) {
        prefs.multipointEnabled = enabled
        _uiState.value = _uiState.value.copy(multipointEnabled = enabled)
        protocolEngine.sendMultipoint(_uiState.value.effectiveSdk, enabled)
    }

    fun setSmartTalk(enabled: Boolean) {
        prefs.smartTalkEnabled = enabled
        _uiState.value = _uiState.value.copy(smartTalkEnabled = enabled)
    }

    // =========================================================================
    // TOUCH GESTURE REMAPPER & PER-DEVICE AUTOMATION
    // =========================================================================

    fun updateTouchMapping(gesture: TouchGesture, action: TouchAction) {
        prefs.setTouchMapping(gesture, action)
        val updated = prefs.getTouchMappings()
        _uiState.value = _uiState.value.copy(touchMappings = updated)
        protocolEngine.sendTouchKeyMapping(_uiState.value.effectiveSdk, gesture, action)
    }

    fun setBedTouchLock(locked: Boolean) {
        prefs.bedTouchLock = locked
        _uiState.value = _uiState.value.copy(bedTouchLock = locked)
        val mappings = _uiState.value.touchMappings
        TouchGesture.entries.forEach { g ->
            val act = if (locked && (g.gestureName.contains("Single") || g.gestureName.contains("Double"))) {
                TouchAction.DISABLED
            } else {
                mappings[g] ?: TouchAction.PLAY_PAUSE
            }
            protocolEngine.sendTouchKeyMapping(_uiState.value.effectiveSdk, g, act)
        }
    }

    fun saveAutoProfile(profile: DeviceAutoProfile) {
        prefs.saveAutoProfile(profile)
        _uiState.value = _uiState.value.copy(autoProfile = profile)
    }

    // =========================================================================
    // LAB, FIND MY BUDS & ACOUSTIC DIAGNOSTICS
    // =========================================================================

    fun triggerFindMyBuds(channel: OutputChannel) {
        val tag = "FIND_${channel.name}"
        if (_uiState.value.activeAcousticTest == tag) {
            stopAcousticTest()
            protocolEngine.sendFindMyBudsCommand(_uiState.value.effectiveSdk, false)
        } else {
            _uiState.value = _uiState.value.copy(activeAcousticTest = tag)
            protocolEngine.sendFindMyBudsCommand(_uiState.value.effectiveSdk, true)
            acousticGenerator.playFindMyBudsChirp(channel)
        }
    }

    fun triggerStereoOrToneTest(channel: OutputChannel, frequencyHz: Float = _uiState.value.testFrequencyHz) {
        val tag = "TONE_${channel.name}"
        if (_uiState.value.activeAcousticTest == tag) {
            stopAcousticTest()
        } else {
            _uiState.value = _uiState.value.copy(
                activeAcousticTest = tag,
                testFrequencyHz = frequencyHz
            )
            acousticGenerator.playContinuousTone(frequencyHz, channel)
        }
    }

    fun updateTestFrequency(frequencyHz: Float) {
        _uiState.value = _uiState.value.copy(testFrequencyHz = frequencyHz)
        val active = _uiState.value.activeAcousticTest ?: return
        if (active.startsWith("TONE_")) {
            val chName = active.removePrefix("TONE_")
            val ch = runCatching { OutputChannel.valueOf(chName) }.getOrDefault(OutputChannel.BOTH_STEREO)
            acousticGenerator.playContinuousTone(frequencyHz, ch)
        }
    }

    fun runEarTipSealTest() {
        _uiState.value = _uiState.value.copy(
            activeAcousticTest = "SEAL_TEST",
            sealTestStatus = "Testing 55 Hz Sub-Bass vs 500 Hz Reference... Listen for equal loudness"
        )
        acousticGenerator.playEarTipSealTest {
            _uiState.value = _uiState.value.copy(
                activeAcousticTest = null,
                sealTestStatus = "Seal Test Complete: If 55 Hz felt strong & full in both ears, your ear-tip seal is airtight."
            )
        }
    }

    fun stopAcousticTest() {
        acousticGenerator.stop()
        _uiState.value = _uiState.value.copy(activeAcousticTest = null)
    }

    fun sendRawHexPacket(hex: String): Boolean {
        return protocolEngine.sendRawHexCommand(_uiState.value.effectiveSdk, hex)
    }

    fun clearProtocolLogs() {
        protocolEngine.clearLogs()
    }

    // =========================================================================
    // THEME, FOREGROUND DSP SERVICE & EXPORT/IMPORT
    // =========================================================================

    fun setAccentTheme(theme: AccentTheme) {
        prefs.accentTheme = theme
        _uiState.value = _uiState.value.copy(accentTheme = theme)
    }

    fun setOledPitchBlack(enabled: Boolean) {
        prefs.oledPitchBlack = enabled
        _uiState.value = _uiState.value.copy(oledPitchBlack = enabled)
    }

    fun setHapticSliders(enabled: Boolean) {
        prefs.hapticSliders = enabled
        _uiState.value = _uiState.value.copy(hapticSliders = enabled)
    }

    fun setLowBatteryAlertPercent(percent: Int) {
        prefs.lowBatteryAlertPercent = percent
        _uiState.value = _uiState.value.copy(lowBatteryAlertPercent = percent)
    }

    fun startPersistentDspService() {
        runCatching {
            val intent = Intent(appContext, DspAudioService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.startForegroundService(intent)
            } else {
                appContext.startService(intent)
            }
        }
    }

    fun stopPersistentDspService() {
        runCatching {
            appContext.stopService(Intent(appContext, DspAudioService::class.java))
        }
    }

    fun exportProfilesJson(): String = prefs.exportAllPresetsJson()

    fun importProfilesJson(json: String): Boolean {
        val ok = prefs.importPresetsJson(json)
        if (ok) {
            _uiState.value = loadInitialState()
            DspAudioController.applyFromPreferences(appContext)
        }
        return ok
    }

    override fun onCleared() {
        super.onCleared()
        acousticGenerator.stop()
        protocolEngine.disconnectCurrent()
    }
}
