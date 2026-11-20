package com.boat.hearables.pro.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boat.hearables.pro.audio.AcousticTestGenerator
import com.boat.hearables.pro.audio.DspAudioController
import com.boat.hearables.pro.audio.DspAudioService
import com.boat.hearables.pro.audio.OutputChannel
import com.boat.hearables.pro.data.AccentTheme
import com.boat.hearables.pro.data.AppPreferences
import com.boat.hearables.pro.data.BoatDeviceCatalog
import com.boat.hearables.pro.data.EqPreset
import com.boat.hearables.pro.data.LuxeAutoProfile
import com.boat.hearables.pro.data.LuxeLedMode
import com.boat.hearables.pro.data.LuxeSoundMode
import com.boat.hearables.pro.data.RoomPlacement
import com.boat.hearables.pro.protocol.HearableProtocolEngine
import com.boat.hearables.pro.protocol.LuxeSpeakerTelemetry
import com.boat.hearables.pro.protocol.ProtocolPacketLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.roundToInt

data class HearablesUiState(
    val activePresetId: String = "luxe_signature",
    // 7 native BES hardware bands: 60Hz, 120Hz, 250Hz, 750Hz, 2kHz, 5kHz, 15kHz
    val bandGainsDb: List<Float> = BoatDeviceCatalog.BUILT_IN_PRESETS.first().gainsDb,
    val customPresets: List<EqPreset> = emptyList(),
    val hardwareEqEnabled: Boolean = true,
    val soundMode: LuxeSoundMode = LuxeSoundMode.SIGNATURE_SOUND,
    val ledMode: LuxeLedMode = LuxeLedMode.MODE_ONE,
    val roomPlacement: RoomPlacement = RoomPlacement.FREE_STANDING,
    val broadcastPartySync: Boolean = false,
    val multipointEnabled: Boolean = true,
    val batteryBoosterEnabled: Boolean = false,
    val preAmpDb: Float = 0f,
    val qFactor: Float = 1.41f,
    val limiterEnabled: Boolean = true,
    val bassBoostPercent: Int = 30,
    val subBassCutoffHz: Int = 70,
    val virtualizerPercent: Int = 25,
    val loudnessPercent: Int = 15,
    val speakerVolumePercent: Int = 65,
    val sleepTimerMinutesRemaining: Int = 0,
    val autoProfile: LuxeAutoProfile = LuxeAutoProfile(),
    val accentTheme: AccentTheme = AccentTheme.LUXE_GOLD,
    val oledPitchBlack: Boolean = true,
    val hapticSliders: Boolean = true,
    val activeAcousticTest: String? = null,
    val testFrequencyHz: Float = 65f
)

class HearablesViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val prefs = AppPreferences(appContext)
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    val protocolEngine = HearableProtocolEngine(appContext)
    private val acousticGenerator = AcousticTestGenerator()

    private var sleepTimerJob: Job? = null

    private val _uiState = MutableStateFlow(loadInitialState())
    val uiState: StateFlow<HearablesUiState> = _uiState.asStateFlow()

    val telemetry: StateFlow<LuxeSpeakerTelemetry> = protocolEngine.telemetry
    val packetLogs: StateFlow<List<ProtocolPacketLog>> = protocolEngine.packetLogs

    init {
        DspAudioController.applyFromPreferences(appContext)
        refreshNirvanaLuxeConnection()
    }

    private fun loadInitialState(): HearablesUiState {
        return HearablesUiState(
            activePresetId = prefs.activePresetId,
            bandGainsDb = prefs.currentBandGains,
            customPresets = prefs.getCustomPresets(),
            hardwareEqEnabled = prefs.hardwareEqEnabled,
            soundMode = prefs.soundMode,
            ledMode = prefs.ledMode,
            roomPlacement = prefs.roomPlacement,
            broadcastPartySync = prefs.broadcastPartySync,
            multipointEnabled = prefs.multipointEnabled,
            batteryBoosterEnabled = prefs.batteryBoosterEnabled,
            preAmpDb = prefs.preAmpDb,
            qFactor = prefs.qFactor,
            limiterEnabled = prefs.limiterEnabled,
            bassBoostPercent = prefs.bassBoostPercent,
            subBassCutoffHz = prefs.subBassCutoffHz,
            virtualizerPercent = prefs.virtualizerPercent,
            loudnessPercent = prefs.loudnessPercent,
            speakerVolumePercent = readCurrentSystemVolumePercent(),
            autoProfile = prefs.getAutoProfile(),
            accentTheme = prefs.accentTheme,
            oledPitchBlack = prefs.oledPitchBlack,
            hapticSliders = prefs.hapticSliders
        )
    }

    private fun readCurrentSystemVolumePercent(): Int {
        val am = audioManager ?: return 65
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        return ((cur.toFloat() / max.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
    }

    fun setSpeakerVolumePercent(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        _uiState.value = _uiState.value.copy(speakerVolumePercent = clamped)
        runCatching {
            val am = audioManager ?: return@runCatching
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val target = ((clamped / 100f) * max).roundToInt()
            am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        }
    }

    /**
     * Checks paired Bluetooth devices strictly for `boAt Nirvana Luxe` and connects to it.
     */
    fun refreshNirvanaLuxeConnection() {
        _uiState.value = _uiState.value.copy(speakerVolumePercent = readCurrentSystemVolumePercent())
        protocolEngine.detectAndConnectNirvanaLuxe()
    }

    /**
     * Triggers active Bluetooth scan & connection exclusively for `boAt Nirvana Luxe`.
     */
    fun scanAndConnectNirvanaLuxe() {
        protocolEngine.connectToNirvanaLuxe()
    }

    // =========================================================================
    // NIRVANA LUXE SOUND MODES, LED LIGHT SHOW & HARDWARE FEATURES
    // =========================================================================

    fun setSoundMode(mode: LuxeSoundMode) {
        prefs.soundMode = mode
        _uiState.value = _uiState.value.copy(soundMode = mode)
        protocolEngine.sendLuxeSoundMode(mode)

        // Synchronize DSP spatial/loudness characteristics for Outdoor Party & Late Night modes
        when (mode) {
            LuxeSoundMode.SPATIAL_360 -> updateVirtualizer(75)
            LuxeSoundMode.SPATIAL_CINEMA -> updateVirtualizer(55)
            LuxeSoundMode.OUTDOOR_PARTY -> {
                updateLoudness(55)
                updateBassBoost(65)
            }
            LuxeSoundMode.LATE_NIGHT -> {
                updateLoudness(10)
                updateBassBoost(10)
                updateLimiter(true)
            }
            LuxeSoundMode.SIGNATURE_SOUND -> {
                updateVirtualizer(20)
            }
        }
    }

    fun setLedMode(mode: LuxeLedMode) {
        prefs.ledMode = mode
        _uiState.value = _uiState.value.copy(ledMode = mode)
        protocolEngine.sendLuxeLedMode(mode)
    }

    fun setRoomPlacement(placement: RoomPlacement) {
        prefs.roomPlacement = placement
        _uiState.value = _uiState.value.copy(roomPlacement = placement)
        syncDspAndHardwareEq(_uiState.value.bandGainsDb)
    }

    fun setBroadcastPartySync(enabled: Boolean) {
        prefs.broadcastPartySync = enabled
        _uiState.value = _uiState.value.copy(broadcastPartySync = enabled)
        protocolEngine.sendLuxeBroadcastPartySync(enabled)
    }

    fun setMultipoint(enabled: Boolean) {
        prefs.multipointEnabled = enabled
        _uiState.value = _uiState.value.copy(multipointEnabled = enabled)
        protocolEngine.sendLuxeMultipoint(enabled)
    }

    fun setBatteryBooster(enabled: Boolean) {
        prefs.batteryBoosterEnabled = enabled
        _uiState.value = _uiState.value.copy(batteryBoosterEnabled = enabled)
        if (enabled) {
            // Battery Booster automatically turns off LEDs and optimizes sub-bass draw (matching Lwh0.z)
            setLedMode(LuxeLedMode.OFF)
        }
    }

    // =========================================================================
    // 7-BAND BES HARDWARE EQUALIZER + DSP MASTERING ACTIONS
    // =========================================================================

    fun setHardwareEqEnabled(enabled: Boolean) {
        prefs.hardwareEqEnabled = enabled
        _uiState.value = _uiState.value.copy(hardwareEqEnabled = enabled)
        syncDspAndHardwareEq(_uiState.value.bandGainsDb)
    }

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
        val flat = List(7) { 0f }
        prefs.activePresetId = "flat_studio"
        prefs.currentBandGains = flat
        _uiState.value = _uiState.value.copy(
            activePresetId = "flat_studio",
            bandGainsDb = flat
        )
        syncDspAndHardwareEq(flat)
    }

    fun saveCurrentAsCustomPreset(name: String, subtitle: String = "Custom Nirvana Luxe 7-Band Profile") {
        val cleanName = name.trim().ifEmpty { "My Luxe Profile ${_uiState.value.customPresets.size + 1}" }
        val newPreset = EqPreset(
            id = "luxe_custom_${UUID.randomUUID().toString().take(8)}",
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

    fun updateLimiter(enabled: Boolean) {
        prefs.limiterEnabled = enabled
        _uiState.value = _uiState.value.copy(limiterEnabled = enabled)
        DspAudioController.applyFromPreferences(appContext)
    }

    fun updateBassBoost(percent: Int) {
        prefs.bassBoostPercent = percent
        _uiState.value = _uiState.value.copy(bassBoostPercent = percent)
        DspAudioController.applyFromPreferences(appContext)
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

    private fun syncDspAndHardwareEq(gainsDb: List<Float>) {
        DspAudioController.applyFromPreferences(appContext)
        protocolEngine.sendLuxe7BandCustomEq(
            gainsDb = gainsDb,
            roomPlacement = _uiState.value.roomPlacement,
            enabled = _uiState.value.hardwareEqEnabled
        )
    }

    // =========================================================================
    // AUTO-CONNECT PROFILE, SLEEP TIMER & SPEAKER ACOUSTIC LAB
    // =========================================================================

    fun saveAutoProfile(profile: LuxeAutoProfile) {
        prefs.saveAutoProfile(profile)
        _uiState.value = _uiState.value.copy(autoProfile = profile)
    }

    fun setSleepTimerMinutes(minutes: Int) {
        sleepTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(sleepTimerMinutesRemaining = minutes)
        if (minutes <= 0) return

        sleepTimerJob = viewModelScope.launch {
            var remaining = minutes
            while (remaining > 0) {
                delay(60_000L)
                remaining--
                _uiState.value = _uiState.value.copy(sleepTimerMinutesRemaining = remaining)
            }
            // Fade down volume and pause audio when sleep timer expires
            setSpeakerVolumePercent(0)
            setLedMode(LuxeLedMode.OFF)
        }
    }

    fun triggerSpeakerToneTest(frequencyHz: Float = _uiState.value.testFrequencyHz) {
        val tag = "SPEAKER_TONE"
        if (_uiState.value.activeAcousticTest == tag) {
            stopAcousticTest()
        } else {
            _uiState.value = _uiState.value.copy(
                activeAcousticTest = tag,
                testFrequencyHz = frequencyHz
            )
            acousticGenerator.playContinuousTone(frequencyHz, OutputChannel.BOTH_STEREO)
        }
    }

    fun updateTestFrequency(frequencyHz: Float) {
        _uiState.value = _uiState.value.copy(testFrequencyHz = frequencyHz)
        if (_uiState.value.activeAcousticTest == "SPEAKER_TONE") {
            acousticGenerator.playContinuousTone(frequencyHz, OutputChannel.BOTH_STEREO)
        }
    }

    fun stopAcousticTest() {
        acousticGenerator.stop()
        _uiState.value = _uiState.value.copy(activeAcousticTest = null)
    }

    fun sendRawHexPacket(hex: String): Boolean = protocolEngine.sendCustomHexFrame(hex)

    fun clearProtocolLogs() = protocolEngine.clearLogs()

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
